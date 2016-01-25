package asbridge.me.uk.MMusic.activities;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.*;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import asbridge.me.uk.MMusic.GUIfragments.PlayQueueFragment;
import asbridge.me.uk.MMusic.GUIfragments.PlayedListFragment;
import asbridge.me.uk.MMusic.R;
import asbridge.me.uk.MMusic.classes.RetainFragment;
import asbridge.me.uk.MMusic.classes.Song;
import asbridge.me.uk.MMusic.database.PlaylistsDatabaseHelper;
import asbridge.me.uk.MMusic.dialogs.SetTimerDialog;
import asbridge.me.uk.MMusic.services.SimpleMusicService;
import asbridge.me.uk.MMusic.settings.SettingsActivity;
import asbridge.me.uk.MMusic.utils.AppConstants;

import java.util.ArrayList;

/**
 * Created by AsbridgeD on 22/12/2015.
 */
public class PlayQueueActivity extends FragmentActivity
        implements
        RetainFragment.RetainFragmentListener
        ,PlayQueueFragment.OnPlayQueueListener
        ,SetTimerDialog.OnSleepTimerChangedListener
{

    private static final String TAG = "PlayQueueActivity";

    private RetainFragment retainFragment = null;
    private TextView tvNowPlaying;

    private boolean shuffleOn = true;

    private PlayQueueFragment mPlayQueueFragment = null;
    private PlayedListFragment mPlayedListFragment = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playqueue);
        // Create or Upgrade the database as necessary
        // TODO: problem???? where the DB is only created when this activity starts ???
        // only a problem when activity is first installed and DB does not already exist
        // probably OK if this is the first activity started
        // actually the same call is made from the content resolver onCreate
        PlaylistsDatabaseHelper db = new PlaylistsDatabaseHelper(this);

        FragmentManager fm = getFragmentManager();
        retainFragment = (RetainFragment) fm.findFragmentByTag(AppConstants.TAG_RETAIN_FRAGMENT);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (retainFragment == null) {
            retainFragment = new RetainFragment();
            fm.beginTransaction().add(retainFragment, AppConstants.TAG_RETAIN_FRAGMENT).commit();
        }

        tvNowPlaying = (TextView) findViewById(R.id.pqa_tvPlaying);

        //mPlayQueueFragment = new PlayQueueFragment();
        mPlayQueueFragment = (PlayQueueFragment)getSupportFragmentManager().findFragmentById(R.id.fragplayqueue);
        mPlayQueueFragment.setOnPlayQueueListener(this);

        //mPlayQueueFragment = new PlayQueueFragment();
        mPlayedListFragment = (PlayedListFragment)getSupportFragmentManager().findFragmentById(R.id.fragplayedqueue);
        //mPlayQueueFragment.setOnPlayQueueListener(this);
    }

    // bind to the Service instance when the Activity instance starts
    @Override
    protected void onStart() {
        super.onStart();
        retainFragment.doBindService();
    }

    private SongPlayingReceiver songPlayingReceiver;
    // When the service starts playing a song it will broadcast the title
    private class SongPlayingReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(AppConstants.INTENT_ACTION_SONG_PLAYING)) {
                String songTitle = intent.getStringExtra(AppConstants.INTENT_EXTRA_SONG_TITLE);
                String songArtist = intent.getStringExtra(AppConstants.INTENT_EXTRA_SONG_ARTIST);
                updateNowPlaying(songArtist, songTitle);
                updatePlayQueue();
            }
        }
    }

    private NextSongChangedReceiver nextSongChangedReceiver;
    // When the service starts playing a song it will broadcast the title
    private class NextSongChangedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(AppConstants.INTENT_ACTION_PLAY_QUEUE_CHANGED)) {
                updatePlayQueue();
            }
        }
    }

    // called from fragment
    // pass the change onto the service
    private void updatePlayQueue() {
        if (retainFragment.serviceReference != null) {
            ArrayList<Song> newPlayQueue = retainFragment.serviceReference.getPlayQueue();
            mPlayQueueFragment.updatePlayQueue(newPlayQueue);
            // TODO: why also update played list?
            ArrayList<Song> newPlayedList = retainFragment.serviceReference.getPlayedList();
            mPlayedListFragment.updatePlayedList(newPlayedList);
        }
    }

    private void updateNowPlaying(String songArtist, String songTitle) {
        tvNowPlaying.setText(songArtist + " - " + songTitle);
    }

    // used to save paused state so it can be resumed
    @Override
    protected void onPause(){
        super.onPause();
        if (songPlayingReceiver != null) unregisterReceiver(songPlayingReceiver);
        if (nextSongChangedReceiver != null) unregisterReceiver(nextSongChangedReceiver);

        // paused=true; // TODO: used to remember the paused state (see onResume)
    }

    // uses the saved paused state
    @Override
    protected void onResume(){
        super.onResume();
        // set up the listener for broadcast from the service for new song playing
        if (songPlayingReceiver == null) songPlayingReceiver = new SongPlayingReceiver();
        IntentFilter intentFilter = new IntentFilter(AppConstants.INTENT_ACTION_SONG_PLAYING);
        registerReceiver(songPlayingReceiver, intentFilter);

        // set up the listener for broadcast from the service for play queue changes
        if (nextSongChangedReceiver == null) nextSongChangedReceiver = new NextSongChangedReceiver();
        registerReceiver(nextSongChangedReceiver, new IntentFilter(AppConstants.INTENT_ACTION_PLAY_QUEUE_CHANGED));

        if (retainFragment.serviceReference != null) {
            Song currentSong = retainFragment.serviceReference.getCurrentSong();
            if (currentSong != null)
                updateNowPlaying(currentSong.getArtist(), currentSong.getTitle());
            ArrayList <Song> newPlayQueue = retainFragment.serviceReference.getPlayQueue();
            mPlayQueueFragment.updatePlayQueue(newPlayQueue);
            ArrayList <Song> newPlayedList = retainFragment.serviceReference.getPlayedList();
            mPlayedListFragment.updatePlayedList(newPlayedList);

        }
    }

    @Override
    public void onMusicServiceReady() {
        // music service is bound and ready
        shuffleOn = retainFragment.serviceReference.getShuffleState();
    }
    MenuItem shuffleMenuItem;
    // Don't stop the playback when the backbutton is pressed
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_options_player, menu);

        shuffleMenuItem = menu.findItem(R.id.action_shuffle);
        if(shuffleOn){
            shuffleMenuItem.setIcon(R.drawable.ic_action_shuffle_on);
            shuffleMenuItem.setTitle("Turn Shuffle Off");
        }else{
            shuffleMenuItem.setIcon(R.drawable.ic_action_shuffle_off);
            shuffleMenuItem.setTitle("Turn Shuffle On");
        }

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
            case R.id.action_playbuckets:
                startActivity(new Intent(this, SelectSongsActivity.class));
                return true;
            case R.id.action_timer:
                showTimerDialog();
                return true;
            case R.id.action_end:
                Intent playIntent = new Intent(this, SimpleMusicService.class);
                stopService(playIntent);
                retainFragment.serviceReference=null;
                finish();
                break;
            case R.id.action_shuffle:
                if(shuffleOn){
                    shuffleOn = false;
                    //change your view and sort it by Alphabet
                    item.setIcon(R.drawable.ic_action_shuffle_off);
                    item.setTitle("Turn Shuffle On");
                }else{
                    shuffleOn = true;
                    //change your view and sort it by Alphabet
                    item.setIcon(R.drawable.ic_action_shuffle_on);
                    item.setTitle("Turn Shuffle Off");
                }
                retainFragment.serviceReference.setShuffleState(shuffleOn);

        }
        return super.onOptionsItemSelected(item);
    }

    public void showTimerDialog() {
        long secsTillSleep = -1;
        if (retainFragment != null && retainFragment.serviceReference != null) {
            secsTillSleep = retainFragment.serviceReference.getSecsTillSleep();
        }
        if (secsTillSleep < 0) {
            FragmentManager fm = getFragmentManager();
            SetTimerDialog setSleepTimerDialog = new SetTimerDialog();
            setSleepTimerDialog.setOnSetSleepTimerListener(this);
            setSleepTimerDialog.show(fm, "fragment_settimer_dialog");
        } else {
            // sleep timer is active ... allow user to cancel
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            String msg;
            if (secsTillSleep < 60) {
                msg = "less than one minute";
            } else {
                long minsTillSleep = secsTillSleep / 60;
                msg = Long.toString(minsTillSleep) + " minutes";
            }

            builder.setTitle("Cancel sleep timer")
                    .setMessage("Sleep in "+msg+"\nCancel?")
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Do nothing
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            shuffleMenuItem.setIcon(R.drawable.ic_action_shuffle_off);
                            retainFragment.serviceReference.cancelSleepTimer();



                            dialog.dismiss();
                        }
                    });

            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    @Override
    public void onSleepTimerChanged(int minsTillSleep) {
        if (retainFragment != null && retainFragment.serviceReference != null) {
            retainFragment.serviceReference.setSleepTimer(minsTillSleep);
            shuffleMenuItem.setIcon(R.drawable.ic_action_shuffle_on);

        }
    }

    ///////////////////////////
    // These methods deal with music control button clicks
    public void btnNextClicked(View v) {
        playNextSong();
    }

    public void playNextSong() {
        if (retainFragment.isBound)
            retainFragment.serviceReference.playNextSongInPlayQueue();
    }

    public void btnStopClicked(View v) {
        stopPlayback();
    }

    private void stopPlayback() {
        if (retainFragment.isBound)
            retainFragment.serviceReference.stopPlayback();
    }

    public void btnPlayPauseClicked(View v) {
        pauseOrResumePlayack();
    }

    private void pauseOrResumePlayack() {
        if (retainFragment.isBound)
            retainFragment.serviceReference.pauseOrResumePlayack();

    }
    ///////////////////////////

    // callback from the playqueue fragment
    @Override
    public void onRemoveSongClicked(Song song) {
        if (retainFragment.isBound) {
            retainFragment.serviceReference.removeSongFromPlayQueue(song.getPID());
        }
    }

    // callback from the playqueue fragment
    @Override
    public void onMoveSongToTopClicked(Song song) {
        if (retainFragment.isBound) {
            retainFragment.serviceReference.moveThisSongToTopOfPlayQueue(song.getPID());
        }
    }
}