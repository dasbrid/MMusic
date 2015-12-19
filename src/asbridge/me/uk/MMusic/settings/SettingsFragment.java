package asbridge.me.uk.MMusic.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import asbridge.me.uk.MMusic.R;

/**
 * Created by AsbridgeD on 18/11/2015.
 */
public class SettingsFragment extends PreferenceFragment  {
    private SharedPreferences prefs;
/*
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return null;
    }
*/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }
}