package at.fhhgb.mc.hike.model.database;

import java.io.Serializable;

/**
 * @author Florian Schrofner
 */

public class HikeTag implements Serializable {
    private String mTitle;
    private String mDescription;
    private String mPhoto;
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

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setPhoto(String photo) {
        mPhoto = photo;
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

    public String getTitle() {
        return mTitle;
    }

    public String getPhoto() {
        return mPhoto;
    }
}
