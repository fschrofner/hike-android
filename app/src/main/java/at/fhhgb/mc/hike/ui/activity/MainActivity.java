package at.fhhgb.mc.hike.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ui.ResultCodes;
import com.google.firebase.auth.FirebaseAuth;

import at.fhhgb.mc.hike.BuildConfig;
import at.fhhgb.mc.hike.R;
import at.fhhgb.mc.hike.app.AppClass;
import at.fhhgb.mc.hike.service.LocationService;
import at.fhhgb.mc.hike.ui.fragment.MapFragment;
import at.flosch.logwrap.Log;

public class MainActivity extends  GlobalActivity{
    private final String TAG = MainActivity.class.getSimpleName();
    private final static int LOCATION_PERMISSION_REQUEST_CODE = 4242;
    private final static int EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 4422;
    private static final int SIGN_IN_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setView(R.layout.activity_main);
        setTitle("");

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

        if(!AppClass.getInstance().checkExternalStoragePermission()){
            requestExternalStoragePermission();
        }

        tryToShowMapFragment();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            startActivityForResult(
                // Get an instance of AuthUI based on the default app
                AuthUI.getInstance().createSignInIntentBuilder().build(),
                SIGN_IN_REQUEST_CODE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(LocationService.ongoingHike()){
            changeMenu(R.menu.hike_menu);
        } else {
            changeMenu(R.menu.empty_menu);
        }
    }

    private void tryToShowMapFragment(){
        if(AppClass.getInstance().checkLocationPermission() && AppClass.getInstance().checkExternalStoragePermission()){
            addFragment(MapFragment.newInstance(), false);
        }
    }

    private void startLocationService(){
        Intent intent = new Intent(getApplicationContext(), LocationService.class);
        startService(intent);
    }

    private void requestLocationPermission(){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

    private void requestExternalStoragePermission(){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:
                if(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    startLocationService();
                } else {
                    //TODO: show error dialog that permission is needed
                }
                break;
            case EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE:
                if(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                } else {
                    //TODO: show error dialog that permission is needed
                }
                break;
        }

        tryToShowMapFragment();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //TODO: Maybe do something with the response
        if(requestCode==SIGN_IN_REQUEST_CODE){
            if (resultCode == RESULT_OK) {
            }

            // Sign in canceled
            if (resultCode == RESULT_CANCELED) {
            }

            // No network
            if (resultCode == ResultCodes.RESULT_NO_NETWORK) {

            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
