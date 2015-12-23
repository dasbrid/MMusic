package asbridge.me.uk.MMusic.adapters;

/**
 * Created by David on 12/12/2015.
 */

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;
import asbridge.me.uk.MMusic.GUIfragments.ArtistFragment;
import asbridge.me.uk.MMusic.GUIfragments.PlayQueueFragment;

/**
 * Created by David on 02/12/2015.
 */
public class NewTabsAdapter extends FragmentPagerAdapter {
    int mNumOfTabs;

    PlayQueueFragment.OnPlayQueueListener onPlayQueueListener;

    Fragment currentFragment;
    public NewTabsAdapter(FragmentManager fm, Activity a) {
        super(fm);
        this.mNumOfTabs = 2;
        onPlayQueueListener = (PlayQueueFragment.OnPlayQueueListener)a;
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
                PlayQueueFragment fragment = new PlayQueueFragment();
                currentFragment = fragment;
                fragment.setOnPlayQueueListener(onPlayQueueListener);
                break;
            case 1:
                currentFragment = new ArtistFragment();
                break;
        }
        return currentFragment;
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
