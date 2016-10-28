package at.fhhgb.mc.hike.model.database;

import android.support.annotation.NonNull;

import org.osmdroid.util.GeoPoint;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author Florian Schrofner
 */

public class HikeRoute implements Serializable {
    long mUniqueId;
    ArrayList<HikeTimestamp> mPath;
    ArrayList<HikeTag> mTags;
    boolean mCompleted = false;

    public long getUniqueId() {
        return mUniqueId;
    }

    public HikeRoute(){
        super();
    }

    public HikeRoute(long uniqueId) {
        mUniqueId = uniqueId;
    }

    @NonNull
    public ArrayList<HikeTimestamp> getPath() {
        if(mPath == null){
            mPath = new ArrayList<>();
        }
        return mPath;
    }

    @NonNull
    public ArrayList<HikeTag> getTags() {
        if(mTags == null){
            mTags = new ArrayList<>();
        }
        return mTags;
    }

    public void addTimestamp(HikeTimestamp timestamp){
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
