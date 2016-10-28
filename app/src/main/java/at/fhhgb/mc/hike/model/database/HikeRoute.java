package at.fhhgb.mc.hike.model.database;

import android.support.annotation.NonNull;

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
}
