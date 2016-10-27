package at.fhhgb.mc.hike.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import at.fhhgb.mc.hike.BuildConfig;
import at.fhhgb.mc.hike.R;
import at.fhhgb.mc.hike.app.AppClass;
import at.fhhgb.mc.hike.service.LocationService;
import at.fhhgb.mc.hike.ui.fragment.MapFragment;
import at.flosch.logwrap.Log;

public class MainActivity extends  GlobalActivity{
    private final String TAG = MainActivity.class.getSimpleName();
    private final static int LOCATION_PERMISSION_REQUEST_CODE = 4242;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setView(R.layout.activity_main);

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

        addFragment(MapFragment.newInstance(), false);
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
