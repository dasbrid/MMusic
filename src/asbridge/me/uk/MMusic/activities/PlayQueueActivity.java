package asbridge.me.uk.MMusic.activities;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.*;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
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
        ,SimpleMusicService.OnMusicSleepListener
        ,PlayedListFragment.OnPlayedListClickedListener
{

    private static final String TAG = "PlayQueueActivity";

    private RetainFragment retainFragment = null;
    private TextView tvNowPlaying;
    private ImageButton btnPlayPause;
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

        btnPlayPause = (ImageButton) findViewById(R.id.pqa_btnPlayPause);

        FragmentManager fm = getFragmentManager();
        retainFragment = (RetainFragment) fm.findFragmentByTag(AppConstants.TAG_RETAIN_FRAGMENT);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (retainFragment == null) {
            retainFragment = new RetainFragment();
            fm.beginTransaction().add(retainFragment, AppConstants.TAG_RETAIN_FRAGMENT).commit();
        }

        tvNowPlaying = (TextView) findViewById(R.id.pqa_tvPlaying);
        
        mPlayQueueFragment = (PlayQueueFragment)getSupportFragmentManager().findFragmentById(R.id.fragplayqueue);
        mPlayQueueFragment.setOnPlayQueueListener(this);

        mPlayedListFragment = (PlayedListFragment)getSupportFragmentManager().findFragmentById(R.id.fragplayedqueue);
        mPlayedListFragment.setOnPlayedListClickedListener(this);
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
                btnPlayPause.setImageResource(R.drawable.ic_av_pause);
                updatePlayQueue();
            }
        }
    }


    private SongPausedReceiver songPausedReceiver;
    // When the service starts playing a song it will broadcast the title
    private class SongPausedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive "+intent.getAction());
            if (intent.getAction().equals(AppConstants.INTENT_ACTION_SONG_PAUSED)) {
                btnPlayPause.setImageResource(R.drawable.ic_av_play);
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
        if (nextSongChangedReceiver != null) unregisterReceiver(songPausedReceiver);
    }

    // uses the saved paused state
    @Override
    protected void onResume(){
        super.onResume();
        // set up the listener for broadcast from the service for new song playing
        if (songPlayingReceiver == null) songPlayingReceiver = new SongPlayingReceiver();
        IntentFilter intentFilter = new IntentFilter(AppConstants.INTENT_ACTION_SONG_PLAYING);
        registerReceiver(songPlayingReceiver, intentFilter);
        if (songPausedReceiver == null) songPausedReceiver = new SongPausedReceiver();
        IntentFilter songPausedIntentFilter = new IntentFilter(AppConstants.INTENT_ACTION_SONG_PAUSED);
        registerReceiver(songPausedReceiver, songPausedIntentFilter);

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
            retainFragment.serviceReference.setOnMusicSleepListener(this);
            Log.d(TAG, "set pp btn, state = " + retainFragment.serviceReference.getPlayState());
            if (retainFragment.serviceReference.getPlayState() == SimpleMusicService.STATE_PLAYING) {
                btnPlayPause.setImageResource(R.drawable.ic_av_pause);
            } else {
                btnPlayPause.setImageResource(R.drawable.ic_av_play);
            }
        }
    }

    @Override
    public void onMusicServiceReady() {
        // music service is bound and ready
        shuffleOn = retainFragment.serviceReference.getShuffleState();
    }

    MenuItem sleepIcon;
    // Don't stop the playback when the backbutton is pressed
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_options_player, menu);

        sleepIcon = menu.findItem(R.id.action_zzz);
        if (retainFragment != null && retainFragment.serviceReference != null) {
            long secsTillSleep = retainFragment.serviceReference.getSecsTillSleep();
            sleepIcon.setVisible(secsTillSleep > 0);
        }


        MenuItem shuffleMenuItem = menu.findItem(R.id.action_shuffle);
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
            case R.id.action_artists:
                startActivity(new Intent(this, ArtistListActivity.class));
                return true;
            case R.id.action_songs:
                startActivity(new Intent(this, SongListtActivity.class));
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
        long secsTillSleep;
        boolean isSleepTimerActive;
        if (retainFragment == null || retainFragment.serviceReference == null) {
            Toast.makeText(this, "Cannot set sleep timer", Toast.LENGTH_SHORT).show();
        } else {
            isSleepTimerActive = retainFragment.serviceReference.isSleepTimerActive();
            secsTillSleep = retainFragment.serviceReference.getSecsTillSleep();

            if (!isSleepTimerActive) {
                FragmentManager fm = getFragmentManager();
                SetTimerDialog setSleepTimerDialog = new SetTimerDialog();
                setSleepTimerDialog.setOnSetSleepTimerListener(this);
                setSleepTimerDialog.show(fm, "fragment_settimer_dialog");
            } else {
                // sleep timer is active ... allow user to cancel
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                String msg;
                if (secsTillSleep < 0) {
                    msg = "Sleep at end of playing song";
                } else if (secsTillSleep < 60) {
                    msg = "Sleep in less than one minute";
                } else {
                    long minsTillSleep = secsTillSleep / 60;
                    msg = "Sleep in " + Long.toString(minsTillSleep) + " minutes";
                }

                builder.setTitle("Cancel sleep timer")
                        .setMessage(msg + "\nCancel?")
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
                                sleepIcon.setVisible(false);
                                retainFragment.serviceReference.cancelSleepTimer();
                                dialog.dismiss();
                            }
                        });

                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }

    // callback from music service if the sleep timer has been reached
    @Override
    public void onMusicSleep() {
        sleepIcon.setVisible(false);
    }


    @Override
    public void onSleepTimerChanged(int minsTillSleep) {
        if (retainFragment != null && retainFragment.serviceReference != null) {
            retainFragment.serviceReference.setSleepTimer(minsTillSleep);
            sleepIcon.setVisible(true);
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

    @Override
    public void onPlayedListClicked(Song playedListSong) {
        if (retainFragment != null && retainFragment.serviceReference != null) {
            retainFragment.serviceReference.playThisSong(playedListSong);
        }
    }
}