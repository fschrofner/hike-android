package at.fhhgb.mc.hike.model.database;

import java.io.Serializable;

/**
 * @author Florian Schrofner
 */

public class HikeTimestamp implements Serializable {
    long mTime;
    double mLatitude;
    double mLongitude;

    public long getTime() {
        return mTime;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setTime(long time) {
        mTime = time;
    }

    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }
}
