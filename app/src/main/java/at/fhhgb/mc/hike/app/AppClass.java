package at.fhhgb.mc.hike.app;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.multidex.MultiDexApplication;
import android.support.v4.app.ActivityCompat;

import at.fhhgb.mc.hike.model.database.DatabaseException;

/**
 * @author Florian Schrofner
 */

public class AppClass extends MultiDexApplication {
    private static AppClass mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static AppClass getInstance(){
        return mInstance;
    }

    public boolean checkLocationPermission(){
        return !(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED);
    }

    public boolean checkExternalStoragePermission(){
        return !(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED);
    }

    public static Context getAppContext(){
        return getInstance().getApplicationContext();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        try {
            Database.close();
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }
}
