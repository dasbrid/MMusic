package asbridge.me.uk.MMusic.adapters;

/**
 * Created by David on 12/12/2015.
 */

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;
import asbridge.me.uk.MMusic.GUIfragments.SelectSongsFragment;
import asbridge.me.uk.MMusic.GUIfragments.PlayQueueFragment;

/**
 * Created by David on 02/12/2015.
 */
public class MusicFragmentsAdapter extends FragmentPagerAdapter {
    int mNumOfTabs;

    private PlayQueueFragment musicPlayerFragment;
    private SelectSongsFragment selectSongsFragment;

    Fragment currentFragment;
    public MusicFragmentsAdapter(FragmentManager fm, PlayQueueFragment theMusicPlayerFragment, SelectSongsFragment theSelectSongsFragment) {
        super(fm);
        musicPlayerFragment=theMusicPlayerFragment;
        selectSongsFragment = theSelectSongsFragment;
        this.mNumOfTabs = 2;
    }

    public Fragment getCurrentFragment() {
        return currentFragment;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {

        if (currentFragment != object) {
            currentFragment = (Fragment) object;
        }
        super.setPrimaryItem(container, position, object);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                Fragment tab0 = musicPlayerFragment;
                return tab0;
            case 1:
                Fragment tab1 = selectSongsFragment;
                return tab1;

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
