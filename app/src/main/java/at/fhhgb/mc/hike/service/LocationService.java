package at.fhhgb.mc.hike.service;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.UploadTask;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import at.fhhgb.mc.hike.R;
import at.fhhgb.mc.hike.adapter.FirebaseAdapter;
import at.fhhgb.mc.hike.app.AppClass;
import at.fhhgb.mc.hike.app.Database;
import at.fhhgb.mc.hike.app.Helper;
import at.fhhgb.mc.hike.model.database.DatabaseException;
import at.fhhgb.mc.hike.model.database.HikeRoute;
import at.fhhgb.mc.hike.model.database.HikeStats;
import at.fhhgb.mc.hike.model.database.HikeTag;
import at.fhhgb.mc.hike.model.database.HikeTimestamp;
import at.fhhgb.mc.hike.model.events.LocationUpdateEvent;
import at.fhhgb.mc.hike.model.events.StartHikeTrackingEvent;
import at.fhhgb.mc.hike.model.events.StatsUpdateEvent;
import at.fhhgb.mc.hike.model.events.StopHikeTrackingEvent;
import at.fhhgb.mc.hike.model.events.TagSavedEvent;
import at.fhhgb.mc.hike.ui.activity.MainActivity;
import at.flosch.logwrap.Log;
import id.zelory.compressor.Compressor;

/**
 * @author Florian Schrofner
 */

public class LocationService extends Service implements LocationListener {
    private final static int NOTIFICATION_ID = 3372;
    private final static String TAG = LocationService.class.getSimpleName();
    private final static int LOCATION_UPDATE_MIN_TIME = 10000;
    private final static float LOCATION_UPDATE_MIN_DISTANCE = 10;
    private LocationManager mLocationManager;
    private HikeRoute mHikeRoute;
    private static Long mHikeUniqueId = null;
    private static Long mHikeStartTime = null;

    public static Long ongoingHikeId(){
        return mHikeUniqueId;
    }

    public static Long startTime(){
        if(ongoingHike() && mHikeStartTime != null){
            return mHikeStartTime;
        } else {
            return null;
        }
    }

    public static boolean ongoingHike(){
        return ongoingHikeId() != null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        Log.d(TAG, "location service created");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onStartHikeTracking(StartHikeTrackingEvent event) {
        Log.d(TAG, "received start hike tracking event");
        mHikeUniqueId = event.getUniqueId();
        mHikeRoute = new HikeRoute(event.getUniqueId());
        mHikeStartTime = System.currentTimeMillis();
        startLocationTracking();
    }

    @Subscribe
    public void onStopHikeTracking(StopHikeTrackingEvent event) {
        mHikeUniqueId = Long.MIN_VALUE;
        mHikeRoute = null;
        mHikeUniqueId = null;
        mHikeStartTime = null;
        stopLocationTracking();
    }

    @Subscribe
    public void onTagSaved(TagSavedEvent event){
        Log.d(TAG, "on tag save event received");
        HikeTag tag = event.getHikeTag();

        //adapt time to nearest gps_trace
        tag.setTime(mHikeRoute.getLastTimeStamp().getTime());

        mHikeRoute.addTag(tag);

        try {
            Database.saveHikeRouteInDatabase(mHikeRoute);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }

        if(tag.getTagType() == HikeTag.TagType.Image){
            //upload image first
            if(tag.getPhoto() != null && !tag.getPhoto().isEmpty()){
                try {
                    compressAndUploadImage(tag);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            uploadCompleteHike();
        }
    }

    private void stopLocationTracking() {
        if(AppClass.getInstance().checkLocationPermission()){
            mLocationManager.removeUpdates(this);
        }
        stopForeground(true);
        mHikeRoute.completed();

        //TODO: actually the hikeroute could be cleared from the offline database
        try {
            Database.saveHikeRouteInDatabase(mHikeRoute);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }

        uploadCompleteHike();
    }

    private void startLocationTracking() {
        if(AppClass.getInstance().checkLocationPermission()){
            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

            Notification notification = new NotificationCompat.Builder(this)
                    .setTicker(getString(R.string.notification_title))
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(getString(R.string.notification_title))
                    .setContentText(getString(R.string.notification_text))
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build();

            startForeground(NOTIFICATION_ID, notification);
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    LOCATION_UPDATE_MIN_TIME, LOCATION_UPDATE_MIN_DISTANCE, this);
        } else {
            Log.e(TAG, "error: permission for location tracking not yet granted");
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "received location update: " + location.getLatitude() + "," + location.getLongitude());
        sendLocationUpdate(location);

        //save timestamp
        HikeTimestamp timestamp = new HikeTimestamp();
        timestamp.setTime((location.getTime() - mHikeRoute.getStartTime())/1000);
        timestamp.setLatitude(location.getLatitude());
        timestamp.setLongitude(location.getLongitude());
        timestamp.setAltitude(location.getAltitude());

        mHikeRoute.addTimestamp(timestamp);
        sendStatsUpdate(mHikeRoute.getStats());

        try {
            Database.saveHikeRouteInDatabase(mHikeRoute);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }

        uploadCompleteHike();
    }

    private void sendLocationUpdate(Location location){
        EventBus.getDefault().post(new LocationUpdateEvent(location));
    }

    private void sendStatsUpdate(HikeStats stats){
        EventBus.getDefault().post(new StatsUpdateEvent(stats));
    }

    private void uploadCompleteHike(){
        Log.d(TAG, "uploading hike..");
        boolean success = FirebaseAdapter.getInstance().uploadHike(mHikeRoute);
        if(success){
            Log.d(TAG, "successfully handed over to firebase");
        } else {
            Log.d(TAG, "error when handing over to firebase");
        }
    }

    private void compressAndUploadImage(final HikeTag tag) throws IOException {
        String path = tag.getPhoto();
        File compressedImageFile = Compressor.getDefault(this).compressToFile(new File(path));
        Log.d(TAG, "compressed image: " + compressedImageFile.getAbsolutePath());
        InputStream stream = getContentResolver().openInputStream(Uri.fromFile(compressedImageFile));

        String uploadFileName = Helper.generateUniqueId() + ".jpeg";

        Log.d(TAG, "hike id: " + mHikeUniqueId);
        Log.d(TAG, "file name: " + uploadFileName);

        OnSuccessListener successListener = new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "uploaded image: " + taskSnapshot.getDownloadUrl());
                mHikeRoute.deleteTag(tag);
                tag.setPhoto(taskSnapshot.getDownloadUrl().toString());
                mHikeRoute.addTag(tag);

                try {
                    Database.saveHikeRouteInDatabase(mHikeRoute);
                } catch (DatabaseException e) {
                    e.printStackTrace();
                }

                uploadCompleteHike();
            }
        };

        OnFailureListener failureListener = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "upload failed: " + e.getMessage());
            }
        };

        FirebaseAdapter.getInstance().uploadImageAndGetUri(String.valueOf(mHikeUniqueId), uploadFileName, stream, successListener, failureListener);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
