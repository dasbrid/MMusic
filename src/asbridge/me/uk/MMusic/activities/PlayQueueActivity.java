package asbridge.me.uk.MMusic.activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import asbridge.me.uk.MMusic.GUIfragments.ArtistFragment;
import asbridge.me.uk.MMusic.GUIfragments.PlayQueueFragment;
import asbridge.me.uk.MMusic.R;
import asbridge.me.uk.MMusic.adapters.PlayQueueAdapter;
import asbridge.me.uk.MMusic.classes.RetainFragment;
import asbridge.me.uk.MMusic.classes.Song;
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
{

    private static final String TAG = "PlayQueueActivity";

    private RetainFragment retainFragment = null;
    private PlayQueueFragment playqueueFragment = null;
    private TextView tvNowPlaying;

    private boolean shuffleOn = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playqueue);

        FragmentManager fm = getFragmentManager();
        retainFragment = (RetainFragment) fm.findFragmentByTag(AppConstants.TAG_RETAIN_FRAGMENT);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (retainFragment == null) {
            Log.d(TAG, "creating and adding retain Fragment");
            retainFragment = new RetainFragment();
            fm.beginTransaction().add(retainFragment, AppConstants.TAG_RETAIN_FRAGMENT).commit();
        }

        tvNowPlaying = (TextView) findViewById(R.id.pqa_tvPlaying);

        playqueueFragment = (PlayQueueFragment)getSupportFragmentManager().findFragmentById(R.id.fragplayqueue);
        if (playqueueFragment != null)
        {
            // set listenter for callbacks from the fragment
            playqueueFragment.setOnPlayQueueListener(this);
        }
    }

    // bind to the Service instance when the Activity instance starts
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
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
                Log.d(TAG, "NextSongChangedReceiver");
                updatePlayQueue();
            }
        }
    }

    // TODO: Should this be done here, and pass play queue to the fragment, or should
    // we just tell the fragment to update 'itself'
    // i've added commented calls to unimplemented fragment methods ...
    private void updatePlayQueue() {
        Log.d(TAG, "updatePlayQueue");
        if (retainFragment.serviceReference != null) {
            ArrayList<Song> newPlayQueue = retainFragment.serviceReference.getPlayQueue();
            Log.d(TAG, "new play queue="+newPlayQueue.size());
            playqueueFragment.updatePlayQueue(newPlayQueue);
        }
    }

    private void updateNowPlaying(String songArtist, String songTitle) {
        Log.d(TAG, "updateNowPlaying " + songTitle);
        tvNowPlaying.setText(songArtist + "-" + songTitle);
    }

    // used to save paused state so it can be resumed
    @Override
    protected void onPause(){
        super.onPause();
        Log.d(TAG, "onPause");
        if (songPlayingReceiver != null) unregisterReceiver(songPlayingReceiver);
        if (nextSongChangedReceiver != null) unregisterReceiver(nextSongChangedReceiver);

        // paused=true; // TODO: used to remember the paused state (see onResume)
    }

    // uses the saved paused state
    @Override
    protected void onResume(){
        super.onResume();
        Log.d(TAG, "onResume");
        // set up the listener for broadcast from the service for new song playing
        if (songPlayingReceiver == null) songPlayingReceiver = new SongPlayingReceiver();
        IntentFilter intentFilter = new IntentFilter(AppConstants.INTENT_ACTION_SONG_PLAYING);
        registerReceiver(songPlayingReceiver, intentFilter);

        // set up the listener for broadcast from the service for play queue changes
        if (nextSongChangedReceiver == null) nextSongChangedReceiver = new NextSongChangedReceiver();
        registerReceiver(nextSongChangedReceiver, new IntentFilter(AppConstants.INTENT_ACTION_PLAY_QUEUE_CHANGED));

        if (retainFragment.serviceReference != null) {
            Log.d(TAG, "servicereference is not null");
            Song currentSong = retainFragment.serviceReference.getCurrentSong();
            if (currentSong != null)
                updateNowPlaying(currentSong.getArtist(), currentSong.getTitle());
            ArrayList <Song> newPlayQueue = retainFragment.serviceReference.getPlayQueue();
            playqueueFragment.updatePlayQueue(newPlayQueue);
        }
    }

    @Override
    public void onMusicServiceReady() {
        Log.d(TAG, "onMusicServiceReady");
        shuffleOn = retainFragment.serviceReference.getShuffleState();
        //TODO:????
        // music service is bound and ready.
        //ArrayList<Song> songList = retainFragment.serviceReference.getSongList();
        //playqueueFragment.setSongList(songList);
    }

    public void btnChooseSongsClicked(View v) {
        Log.d(TAG, "btnChooseSongs");
        Intent intent = new Intent(this, SelectSongsActivity.class);
        startActivity(intent);
    }

    // Don't stop the playback when the backbutton is pressed
    @Override
    public void onBackPressed() {
        Log.d(TAG, "onbackpressed");
        moveTaskToBack(true);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        MenuItem shuffleMenuItem = menu.findItem(R.id.action_shuffle);
        if(shuffleOn){
            shuffleMenuItem.setIcon(R.drawable.shuffle_on);
            shuffleMenuItem.setTitle("Turn Shuffle Off");
        }else{
            shuffleMenuItem.setIcon(R.drawable.shuffle_off);
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
            case R.id.action_timer:
                showSetTimerDialog();
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
                    item.setIcon(R.drawable.shuffle_off);
                    item.setTitle("Turn Shuffle On");
                }else{
                    shuffleOn = true;
                    //change your view and sort it by Alphabet
                    item.setIcon(R.drawable.shuffle_on);
                    item.setTitle("Turn Shuffle Off");
                }
                Log.d(TAG, "shuffle turned "+ (shuffleOn?"on":"off"));
                retainFragment.serviceReference.setShuffleState(shuffleOn);

        }
        return super.onOptionsItemSelected(item);
    }

    public void showSetTimerDialog() {
        long timeTillSleep = -1;
        if (retainFragment != null && retainFragment.serviceReference != null) {
            timeTillSleep = retainFragment.serviceReference.getTimeTillSleep();
        }
        if (timeTillSleep < 0) {
            FragmentManager fm = getFragmentManager();
            SetTimerDialog setSleepTimerDialog = new SetTimerDialog();
            setSleepTimerDialog.show(fm, "fragment_settimer_dialog");
        } else {
            retainFragment.serviceReference.cancelSleepTimer();
        }
    }

    ///////////////////////////
    // These methods deal with music control button clicks
    public void btnNextClicked(View v) {
        Log.d(TAG, "btnNextClicked");
        playNextSong();
    }

    public void playNextSong() {
        if (retainFragment.isBound)
            retainFragment.serviceReference.playNextSongInPlayQueue();
    }

    public void btnStopClicked(View v) {
        Log.d(TAG, "btnStopClicked");
        stopPlayback();
    }

    private void stopPlayback() {
        if (retainFragment.isBound)
            retainFragment.serviceReference.stopPlayback();
    }

    public void btnPlayPauseClicked(View v) {
        Log.d(TAG, "btnPauseClicked");
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
        Log.d(TAG, "onRemoveSong "+song.getTitle());
        if (retainFragment.isBound) {
            retainFragment.serviceReference.removeSongFromPlayQueue(song.getPID());
        }
    }

    // callback from the playqueue fragment
    @Override
    public void onMoveSongToTopClicked(Song song) {
        Log.d(TAG, "onMoveSongToTopClicked "+song.getTitle());
        if (retainFragment.isBound) {
            retainFragment.serviceReference.moveThisSongToTopOfPlayQueue(song.getPID());
        }
    }
}