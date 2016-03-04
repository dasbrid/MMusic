package asbridge.me.uk.MMusic.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by David on 19/12/2015.
 */
public class Settings {

    public static final String PREF_PLAYQUEUESIZE = "playqueuesize";
    public static final String PREF_MINDURATIONINSECONDS = "mindurationinseconds";
    public static final String PREF_SHUFFLESTATE = "shufflestate";

    public static int getPlayQueueSize(Context context) {

        SharedPreferences defaultSharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String playQueueSize = defaultSharedPref.getString(PREF_PLAYQUEUESIZE, "3");

        return Integer.parseInt(playQueueSize);
    }

    public static int getMinDurationInSeconds(Context context) {

        SharedPreferences defaultSharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String playQueueSize = defaultSharedPref.getString(PREF_MINDURATIONINSECONDS, "20");

        return Integer.parseInt(playQueueSize);
    }

    public static void setShuffleState(Context context, boolean shuffleState) {
        SharedPreferences defaultSharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor1 = defaultSharedPref.edit();
        editor1.putBoolean(PREF_SHUFFLESTATE, shuffleState);
        editor1.commit();
    }

    public static boolean getShuffleState(Context context) {
        SharedPreferences defaultSharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean shufflestate = defaultSharedPref.getBoolean(PREF_SHUFFLESTATE, true);
        return shufflestate;
    }

}
