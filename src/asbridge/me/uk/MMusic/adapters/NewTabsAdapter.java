package asbridge.me.uk.MMusic.adapters;

/**
 * Created by David on 12/12/2015.
 */

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.util.SparseArray;
import android.view.ViewGroup;
import asbridge.me.uk.MMusic.GUIfragments.ArtistFragment;
import asbridge.me.uk.MMusic.GUIfragments.PlayQueueFragment;

/**
 * Created by David on 02/12/2015.
 */
public class NewTabsAdapter extends FragmentPagerAdapter {

    private final String TAG = "NewTabsAdapter";

    SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();

    private int mNumOfTabs;
    private PlayQueueFragment playQueueFragment;

    PlayQueueFragment.OnPlayQueueListener onPlayQueueListener;

    Fragment currentFragment;
    public NewTabsAdapter(FragmentManager fm, Activity a) {
        super(fm);
        Log.d(TAG,"NewTabsAdapter, ctor");
        this.mNumOfTabs = 2;
        onPlayQueueListener = (PlayQueueFragment.OnPlayQueueListener)a;
    }

    public Fragment getCurrentFragment() {
        return null;
    }
    public PlayQueueFragment getPlayQueueFragment() { return (PlayQueueFragment)getRegisteredFragment(0);}

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {

        if (currentFragment != object) {
            currentFragment = (Fragment) object;
        }
        super.setPrimaryItem(container, position, object);
    }

    //http://stackoverflow.com/questions/8785221/retrieve-a-fragment-from-a-viewpager/15261142#15261142
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Log.d(TAG, "Instantiate Item "+position);
        Fragment fragment = (Fragment)super.instantiateItem(container,position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public Fragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }

    @Override
    public Fragment getItem(int position) {
        Log.d(TAG, "getItem "+position);
        switch (position) {
            case 0:
                Fragment fragment = new PlayQueueFragment();
                ((PlayQueueFragment)fragment).setOnPlayQueueListener(onPlayQueueListener);
                return fragment;
            case 1:
                Fragment fragment1 = new ArtistFragment();
                return fragment1;
        }
        return null;
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
