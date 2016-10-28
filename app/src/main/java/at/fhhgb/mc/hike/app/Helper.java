package at.fhhgb.mc.hike.app;

/**
 * @author Florian Schrofner
 */

public class Helper {
    public static long generateUniqueId(){
        String uniqueString = String.valueOf((long)(Math.random() * 1000)) + String.valueOf(System.currentTimeMillis());
        return Long.valueOf(uniqueString);
    }
}
