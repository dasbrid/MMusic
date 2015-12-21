package asbridge.me.uk.MMusic.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by David on 19/12/2015.
 */
public class Settings {

    public static int getPlayQueueSize(Context context) {
/*
        SharedPreferences sharedPref = context.getSharedPreferences("myapp",Context.MODE_PRIVATE);
        int pqs = sharedPref.getInt("playqueuesize", 8);
*/
        SharedPreferences defaultSharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String playQueueSize = defaultSharedPref.getString("playqueuesize", "3");

        return Integer.parseInt(playQueueSize);
    }

    public static void setShuffleState(Context context, boolean shuffleState) {
        SharedPreferences defaultSharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor1 = defaultSharedPref.edit();
        editor1.putBoolean("shufflestate", shuffleState);
        editor1.commit();
    }

    public static boolean getShuffleState(Context context) {
        SharedPreferences defaultSharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean shufflestate = defaultSharedPref.getBoolean("shufflestate", true);
        return shufflestate;
    }

}