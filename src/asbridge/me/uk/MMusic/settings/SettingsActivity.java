package asbridge.me.uk.MMusic.settings;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by AsbridgeD on 18/11/2015.
 */
public class SettingsActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check whether this activity was created before
        if (savedInstanceState == null) {
            // Create a fragment
            SettingsFragment fragment = new SettingsFragment();
            getFragmentManager()
                    .beginTransaction()
                    .add(android.R.id.content, fragment,
                            fragment.getClass().getSimpleName())
                    .commit();
        }
    }
}