package asbridge.me.uk.MMusic.adapters;

/**
 * Created by David on 12/12/2015.
 */

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.ViewGroup;
import asbridge.me.uk.MMusic.fragments.ArtistFragment;
import asbridge.me.uk.MMusic.fragments.MusicPlayerFragment;

/**
 * Created by David on 02/12/2015.
 */
public class MusicTabsAdapter extends FragmentPagerAdapter {
    int mNumOfTabs;

    private MusicPlayerFragment musicPlayerFragment;
    private ArtistFragment artistFragment;

    Fragment currentFragment;
    public MusicTabsAdapter(FragmentManager fm, MusicPlayerFragment theMusicPlayerFragment, ArtistFragment theArtistFragment ) {
        super(fm);
        musicPlayerFragment=theMusicPlayerFragment;
        artistFragment=theArtistFragment;
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
                Fragment tab1 = artistFragment;
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
