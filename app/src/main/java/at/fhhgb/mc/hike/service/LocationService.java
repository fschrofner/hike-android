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
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import at.fhhgb.mc.hike.R;
import at.fhhgb.mc.hike.app.AppClass;
import at.fhhgb.mc.hike.app.Database;
import at.fhhgb.mc.hike.model.database.DatabaseException;
import at.fhhgb.mc.hike.model.database.HikeRoute;
import at.fhhgb.mc.hike.model.database.HikeTag;
import at.fhhgb.mc.hike.model.database.HikeTimestamp;
import at.fhhgb.mc.hike.model.events.LocationUpdateEvent;
import at.fhhgb.mc.hike.model.events.StartHikeTrackingEvent;
import at.fhhgb.mc.hike.model.events.StopHikeTrackingEvent;
import at.fhhgb.mc.hike.model.events.TagSavedEvent;
import at.fhhgb.mc.hike.ui.activity.MainActivity;
import at.flosch.logwrap.Log;

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

    public static Long ongoingHikeId(){
        return mHikeUniqueId;
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
        startLocationTracking();
    }

    @Subscribe
    public void onStopHikeTracking(StopHikeTrackingEvent event) {
        mHikeUniqueId = Long.MIN_VALUE;
        mHikeRoute = null;
        mHikeUniqueId = null;
        stopLocationTracking();
    }

    @Subscribe
    public void onTagSaved(TagSavedEvent event){
        HikeTag tag = event.getHikeTag();
        //TODO: save with tag
        mHikeRoute.addTag(tag);

        try {
            Database.saveHikeRouteInDatabase(mHikeRoute);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }

    private void stopLocationTracking() {
        if(AppClass.getInstance().checkLocationPermission()){
            mLocationManager.removeUpdates(this);
        }
        stopForeground(true);
    }

    private void startLocationTracking() {
        if(AppClass.getInstance().checkLocationPermission()){
            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

            Notification notification = new NotificationCompat.Builder(this)
                    .setTicker(getString(R.string.notification_title))
                    .setSmallIcon(android.R.drawable.ic_menu_report_image)
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
        timestamp.setTime(location.getTime());
        timestamp.setLatitude(location.getLatitude());
        timestamp.setLongitude(location.getLongitude());

        mHikeRoute.addTimestamp(timestamp);

        try {
            Database.saveHikeRouteInDatabase(mHikeRoute);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }

    private void sendLocationUpdate(Location location){
        EventBus.getDefault().post(new LocationUpdateEvent(location));
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
