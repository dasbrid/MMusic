package asbridge.me.uk.MMusic.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by David on 19/12/2015.
 */
public class Settings {

    public static int getPlayQueueSize(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("myapp",Context.MODE_PRIVATE);
        int pqs = sharedPref.getInt("playqueuesize", 8);

        SharedPreferences defaultSharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String playQueueSize = defaultSharedPref.getString("playqueuesize", "3");

        return Integer.parseInt(playQueueSize);
    }
}
