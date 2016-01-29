package asbridge.me.uk.MMusic.activities;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import asbridge.me.uk.MMusic.GUIfragments.SelectSongsFragment;
import asbridge.me.uk.MMusic.R;
import asbridge.me.uk.MMusic.classes.ArtistGroup;
import asbridge.me.uk.MMusic.classes.RetainFragment;
import asbridge.me.uk.MMusic.classes.Song;
import asbridge.me.uk.MMusic.dialogs.LoadPlaybucketDialog;
import asbridge.me.uk.MMusic.dialogs.SavePlaybucketDialog;
import asbridge.me.uk.MMusic.services.SimpleMusicService;
import asbridge.me.uk.MMusic.settings.SettingsActivity;
import asbridge.me.uk.MMusic.utils.AppConstants;
import asbridge.me.uk.MMusic.utils.MusicContent;

import java.util.ArrayList;

/**
 * Created by David on 20/12/2015.
 */
public class SelectSongsActivity extends FragmentActivity
        implements SelectSongsFragment.OnSongsChangedListener
        , RetainFragment.RetainFragmentListener
        , LoadPlaybucketDialog.OnLoadPlaybucketSelectedListener
        ,SavePlaybucketDialog.OnSavePlaybucketActionListener
{
    private static final String TAG = "SelectSongsActivity";

    private RetainFragment retainFragment = null;
    private SelectSongsFragment artistsFragment = null;

    @Override
    public void onMusicServiceReady() {
        Log.d(TAG, "onMusicServiceReady");
        artistsFragment.setSongList();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_songs);

        FragmentManager fm = getFragmentManager();
        retainFragment = (RetainFragment) fm.findFragmentByTag(AppConstants.TAG_RETAIN_FRAGMENT);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (retainFragment == null) {
            Log.d(TAG, "creating and adding retain Fragment");
            retainFragment = new RetainFragment();
            fm.beginTransaction().add(retainFragment, AppConstants.TAG_RETAIN_FRAGMENT).commit();
        }

        artistsFragment = (SelectSongsFragment)getSupportFragmentManager().findFragmentById(R.id.fragArtists);
        if (artistsFragment != null)
        {
            artistsFragment.setOnSongsChangedListener(this);
        }

    }

    // bind to the Service instance when the Activity instance starts
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        retainFragment.doBindService();
    }


    /* listener from the artistFragment */
    @Override
    public void playThisSongNext(Song s) {
        if (retainFragment != null) {
            if (retainFragment.serviceReference != null) {
                retainFragment.serviceReference.insertThisSongAtTopOfPlayQueue(s);
            }
        }
    }

    /* listener from the artistFragment */
    @Override
    public void addThisSongToPlayQueue(Song s) {
        if (retainFragment != null) {
            if (retainFragment.serviceReference != null) {
                retainFragment.serviceReference.insertThisSongIntoPlayQueue(s);
            }
        }
    }

    /* listener from the artistFragment */
    @Override
    public void playThisSongNow(Song s) {
        if (retainFragment != null) {
            if (retainFragment.serviceReference != null) {
                retainFragment.serviceReference.playThisSong(s);
            }
        }
    }

    /* listener from the artistFragment */
    @Override
    public void addArtistsSongsToPlayQueue(ArtistGroup ag) {
        if (retainFragment != null) {
            if (retainFragment.serviceReference != null) {
                for (ArtistGroup.SelectedSong ss : ag.songs) {
                    retainFragment.serviceReference.insertThisSongIntoPlayQueue(ss.song);
                }
            }
        }
    }

    /* listener from the artistFragment */
    @Override
    public void clearPlayQueueAndaddArtistsSongsToPlayQueue(ArtistGroup ag) {
        if (ag.songs.size() == 0)
            return;
        if (retainFragment != null) {
            if (retainFragment.serviceReference != null) {
                retainFragment.serviceReference.clearPlayQueue();
                for (ArtistGroup.SelectedSong ss : ag.songs) {
                    retainFragment.serviceReference.insertThisSongIntoPlayQueue(ss.song);
                }
            }
        }
    }

    /* listener from the artistFragment */
    @Override
    public void onSongsChanged() {
        Log.d(TAG, "onSongsChanged");
        Log.d(TAG, "retain fragment is " + (retainFragment==null?"null":"not null"));
        if (retainFragment != null) {
            Log.d(TAG, "serviceref fragment is " + (retainFragment.serviceReference==null?"null":"not null"));
            if (retainFragment.serviceReference != null) {
                ArrayList<Song> selectedSongs;
                selectedSongs = artistsFragment.getSelectedSongs();
                Log.d(TAG, "setting list: "+ selectedSongs.size() + " songs");
            }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_options_selectsongs, menu);
        return true;
    }

    // handle user interaction with the menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_save_playlist:
                saveCurrentAsPlaybucket();
                return true;
            case R.id.action_load_playlist:
                loadPlaylist();
                return true;
            case R.id.action_end:
                Intent playIntent = new Intent(this, SimpleMusicService.class);
                stopService(playIntent);
                retainFragment.serviceReference=null;
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveCurrentAsPlaybucket() {
        FragmentManager fm = getFragmentManager();
        SavePlaybucketDialog savePlaybucketDialog = new SavePlaybucketDialog();
        savePlaybucketDialog.setOnPlaybucketNameEnteredListener(this);
        savePlaybucketDialog.show(fm, "fragment_saveplaylist_dialog");
    }

    @Override
    public void onNewPlaybucketNameEntered(String playBucketName) {
        MusicContent.createNewBucket(this, playBucketName);
    }

    @Override
    public void onSavePlayBucketSelected(int savePlaybucketID) {
        MusicContent.updateSavedPlaybucket(this, savePlaybucketID);
        artistsFragment.setSongList();
    }

    private void loadPlaylist() {
        FragmentManager fm = getFragmentManager();
        LoadPlaybucketDialog loadPlaybucketDialog = new LoadPlaybucketDialog();
        loadPlaybucketDialog.setOnPlaybucketSelectedListener(this);
        loadPlaybucketDialog.show(fm, "fragment_loadplaylist_dialog");
    }

    @Override
    public void onLoadPlayBucketSelected(int playbucketID) {
        MusicContent.setCurrentBucketFromSavedBucket(this, playbucketID);
        artistsFragment.setSongList();
    }
}