package at.fhhgb.mc.hike.model.database;

import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import org.osmdroid.util.GeoPoint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import at.fhhgb.mc.hike.adapter.FirebaseAdapter;
import java8.util.Optional;

import java8.util.function.Function;
import java8.util.stream.StreamSupport;

/**
 * @author Florian Schrofner
 */

public class HikeRoute implements Serializable {
    final static String TAG = HikeRoute.class.getSimpleName();

    private String mFirebaseId;
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

    public void deleteTag(HikeTag tag){
        if(getTags().contains(tag)){
            Log.d(TAG, "found tag to remove, removing..");
            ArrayList tags = getTags();
            tags.remove(tag);
            mTags = tags;
        } else {
            Log.d(TAG, "did not find tag to remove");
        }
    }

    public HikeTimestamp getLastTimeStamp(){
        return getPath().get(getPath().size() - 1);
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


    public Map<String, Object> toKeyValueMap(){
        Map<String, Object> map = new HashMap<String, Object>();
        //TODO: Define title
        map.put("title","");
        map.put("is_completed",mCompleted);
        map.put("sport_type_id",1); //Will always be 1 (=hiking)
        map.put("start_date",getStartTime());

        Object[] path = StreamSupport.stream(getPath()).map(new Function<HikeTimestamp, Object>() {
            @Override
            public Object apply(HikeTimestamp hikeTimestamp) {
                return hikeTimestamp.toKeyValueMap();
            }
        }).toArray();

        map.put("gps_trace", Arrays.asList(path));

        Object[] tags = StreamSupport.stream(getTags()).map(new Function<HikeTag, Object>() {
            @Override
            public Object apply(HikeTag hikeTag) {
                return hikeTag.toKeyValueMap();
            }
        }).toArray();

        map.put("tags", Arrays.asList(tags));

        Map<String, Object> summary = mHikeStats.toKeyValueMap();
        double maxElevation = 0;
        Optional<HikeTimestamp> maxAltTimestamp = StreamSupport.stream(getPath()).max(new Comparator<HikeTimestamp>() {
            @Override
            public int compare(HikeTimestamp a, HikeTimestamp b) {
                return Double.compare(a.getAltitude(), b.getAltitude());
            }
        });
        if(maxAltTimestamp.isPresent()) {
            maxElevation = maxAltTimestamp.get().getAltitude();
        }
        summary.put("max_elevation", maxElevation);
        map.put("summary",summary);
        return map;
    }

    public String getmFirebaseId() {
        return mFirebaseId;
    }

    public void setmFirebaseId(String mFirebaseId) {
        this.mFirebaseId = mFirebaseId;
    }

    public boolean hasFirebaseId(){
        return mFirebaseId !=null;
    }

}
