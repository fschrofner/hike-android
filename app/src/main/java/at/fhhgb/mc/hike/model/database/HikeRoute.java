package at.fhhgb.mc.hike.model.database;

import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.LocationUtils;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author Florian Schrofner
 */

public class HikeRoute implements Serializable {
    final static String TAG = HikeRoute.class.getSimpleName();

    long mUniqueId;
    ArrayList<HikeTimestamp> mPath;
    ArrayList<HikeTag> mTags;
    HikeStats mHikeStats;

    boolean mCompleted = false;

    public long getUniqueId() {
        return mUniqueId;
    }

    public long getStartTime(){
        return mHikeStats.getStartTimeMilliSeconds();
    }

    //needed for database instantiation
    public HikeRoute(){

    }

    public HikeRoute(long uniqueId) {
        mUniqueId = uniqueId;
        mHikeStats = new HikeStats();
        mHikeStats.setStartTimeMilliSeconds(System.currentTimeMillis());
    }

    @NonNull
    public ArrayList<HikeTimestamp> getPath() {
        if(mPath == null){
            mPath = new ArrayList<>();
        }
        return mPath;
    }

    @NonNull
    public HikeStats getStats(){
        if(mHikeStats == null){
            mHikeStats = new HikeStats();
        }
        return mHikeStats;
    }

    @NonNull
    public ArrayList<HikeTag> getTags() {
        if(mTags == null){
            mTags = new ArrayList<>();
        }
        return mTags;
    }

    public void addTimestamp(HikeTimestamp timestamp){
        //update stats
        if(getPath().size() > 0){
            HikeTimestamp lastTimeStamp = getPath().get(getPath().size() - 1);

            //distance change
            float[] distance = new float[1];
            Location.distanceBetween(lastTimeStamp.getLatitude(), lastTimeStamp.getLongitude(), timestamp.getLatitude(), timestamp.getLongitude(), distance);

            getStats().addDistance((long)distance[0]);

            //TODO: altitude should better be resolved by an online service
            //altitude change
            long altitudeChange = (long)(timestamp.getAltitude() - lastTimeStamp.getAltitude());
            getStats().addElevationChange(altitudeChange);

            Log.d(TAG, "elevation change by: " + altitudeChange);

            //TODO: send stats update
        }

        getPath().add(timestamp);
    }

    public void addTag(HikeTag tag){
        getTags().add(tag);
    }

    public void completed(){
        mCompleted = true;
    }

    public ArrayList<GeoPoint> getPathAsGeoPoints(){
        ArrayList<GeoPoint> path = new ArrayList<>();
        for(HikeTimestamp timestamp : mPath){
            path.add(new GeoPoint(timestamp.getLatitude(), timestamp.getLongitude()));
        }
        return path;
    }
}
