package at.fhhgb.mc.hike.model.database;

/**
 * @author Florian Schrofner
 */

public class HikeStats {
    private long mTotalDistanceMeters = 0;
    private long mStartTimeMilliSeconds = 0;
    private long mPositiveElevationChangeMeters = 0;
    private long mNegativeElevationChangeMeters = 0;

    public long getTotalDistanceMeters() {
        return mTotalDistanceMeters;
    }

    public long getTotalTimeSeconds() {
        if(mStartTimeMilliSeconds > 0){
            return (System.currentTimeMillis() - mStartTimeMilliSeconds)/1000;
        } else {
            return 0;
        }
    }

    public long getStartTimeMilliSeconds(){
        return mStartTimeMilliSeconds;
    }

    public long getPositiveElevationChangeMeters() {
        return mPositiveElevationChangeMeters;
    }

    public long getNegativeElevationChangeMeters() {
        return mNegativeElevationChangeMeters;
    }

    public void setStartTimeMilliSeconds(long startTimeMilliSeconds) {
        mStartTimeMilliSeconds = startTimeMilliSeconds;
    }

    public void addDistance(long meters){
        mTotalDistanceMeters += meters;
    }

    public void addElevationChange(long meters){
        if(meters > 0){
            mPositiveElevationChangeMeters += meters;
        } else {
            mNegativeElevationChangeMeters += Math.abs(meters);
        }
    }
}
