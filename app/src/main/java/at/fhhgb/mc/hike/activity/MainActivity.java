package at.fhhgb.mc.hike.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import at.fhhgb.mc.hike.BuildConfig;
import at.fhhgb.mc.hike.R;
import at.fhhgb.mc.hike.app.AppClass;
import at.fhhgb.mc.hike.model.events.LocationUpdateEvent;
import at.fhhgb.mc.hike.service.LocationService;
import at.flosch.logwrap.Log;

public class MainActivity extends AppCompatActivity{
    private final String TAG = MainActivity.class.getSimpleName();
    private final static int LOCATION_PERMISSION_REQUEST_CODE = 4242;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //enable logging for debug builds
        if (BuildConfig.DEBUG) {
            Log.enable();
        } else {
            Log.disable();
        }

        //request permissions if needed
        if(AppClass.getInstance().checkLocationPermission()){
            startLocationService();
        } else {
            requestLocationPermission();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onLocationUpdated(LocationUpdateEvent event){
        Log.d(TAG, "received location update from service");
    }

    private void startLocationService(){
        Intent intent = new Intent(getApplicationContext(), LocationService.class);
        startService(intent);
    }

    private void requestLocationPermission(){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:
                if(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    startLocationService();
                } else {
                    //TODO: show error dialog that location permission is needed
                }
                break;
        }
    }

}
