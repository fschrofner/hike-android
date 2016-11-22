package at.fhhgb.mc.hike.model.database;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Florian Schrofner
 */

public class HikeTag implements Serializable {
    private TagType mTagType;
    private String mTitle;
    private String mDescription;
    private String mPhoto;
    private double mLongitude;
    private double mLatitude;
    private long mTime;

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

    public void setTagType(TagType tagType) {
        mTagType = tagType;
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

    public TagType getTagType() {
        return mTagType;
    }

    private String getTagTypeString(){
        switch(getTagType()){
            case Title:
                return "title";
            case Text:
                return "paragraph";
            case Image:
                return "title";
            case Poi:
                return "poi";
        }
        return "title";
    }

    public double getTime() {
        return mTime;
    }

    public void setTime(long time) {
        mTime = time;
    }

    public Map<String, Object> toKeyValueMap(){
        Map<String, Object> map = new HashMap<String, Object>();
        //TODO: Set these values
        map.put("type",getTagTypeString()); //enum: header, paragraph, image, poi
        map.put("time",getTime());
        map.put("duration",0);
        map.put("order",0);
        Map<String,Object> attributes = new HashMap<>();

        if(getTagType() == TagType.Image){
            //use the uri from FirebaseAdapter.uploadImageAndGetUri for this
            attributes.put("image_src", getPhoto());
        }

        if(getTagType() == TagType.Text || getTagType() == TagType.Image){
            attributes.put("text", getDescription());
        }

        attributes.put("title",getTitle());
        map.put("attributes",attributes);

        return map;
    }

    public enum TagType{
        Title,Text,Image,Poi;
    }
}
