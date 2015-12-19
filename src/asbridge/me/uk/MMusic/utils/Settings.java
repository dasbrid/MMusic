package asbridge.me.uk.MMusic.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by David on 19/12/2015.
 */
public class Settings {

    public static int getPlayQueueSize(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("Asbridge.Me.Uk.MMusic", Context.MODE_PRIVATE);
        int pqs = sharedPref.getInt("playqueuesize", 8);
        return pqs;
    }
}
