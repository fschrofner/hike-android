package at.fhhgb.mc.hike.model.database;

import java.io.Serializable;

/**
 * @author Florian Schrofner
 */

public class HikeTag implements Serializable {
    private String mDescription;
    private double mLongitude;
    private double mLatitude;

    public void setDescription(String description) {
        mDescription = description;
    }

    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }

    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    public String getDescription() {
        return mDescription;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public double getLatitude() {
        return mLatitude;
    }
}
