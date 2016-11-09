package at.fhhgb.mc.hike.model.database;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Florian Schrofner
 */

public class HikeTimestamp implements Serializable {
    long mTime;
    double mLatitude;
    double mLongitude;
    double mAltitude;

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

    public double getAltitude() {
        return mAltitude;
    }

    public void setAltitude(double altitude) {
        mAltitude = altitude;
    }

    public Map<String, Object> toKeyValueMap(){
        Map<String,Object> map = new HashMap<>();
        map.put("lon",getLongitude());
        map.put("lat",getLatitude());
        map.put("elevation",getAltitude());
        map.put("time",getTime());
        return map;
    }
}
