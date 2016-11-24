package at.fhhgb.mc.hike.app;

import android.content.Context;
import android.content.Intent;

import at.fhhgb.mc.hike.R;

/**
 * @author Florian Schrofner
 */

public class Helper {
    public static long generateUniqueId(){
        String uniqueString = String.valueOf((long)(Math.random() * 1000)) + String.valueOf(System.currentTimeMillis());
        return Long.valueOf(uniqueString);
    }

    public static void showShareIntent(Context context, String body){
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
        context.startActivity(Intent.createChooser(sharingIntent, context.getResources().getString(R.string.share_using)));
    }
}
