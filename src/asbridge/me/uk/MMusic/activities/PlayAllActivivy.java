package asbridge.me.uk.MMusic.activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.*;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import asbridge.me.uk.MMusic.R;
import asbridge.me.uk.MMusic.adapters.PlayQueueAdapter;
import asbridge.me.uk.MMusic.classes.RetainFragment;
import asbridge.me.uk.MMusic.classes.Song;
import asbridge.me.uk.MMusic.controls.RearrangeableListView;
import asbridge.me.uk.MMusic.services.SimpleMusicService;
import asbridge.me.uk.MMusic.settings.SettingsActivity;
import asbridge.me.uk.MMusic.utils.AppConstants;

import java.util.ArrayList;

/**
 * Created by David on 13/12/2015.
 http://www.101apps.co.za/index.php/articles/binding-to-a-service-a-tutorial.html
 */
public class PlayAllActivivy extends Activity implements RearrangeableListView.RearrangeListener {

    private String TAG = "DAVE:PlayAllActivivy";

    private TextView tvNowPlaying;
    private TextView tvPlayingNext;

    private ListView lvPlayQueue;
    private RearrangeableListView rlvPlayQueue;

    private ArrayList<Song> playQueue;
    private PlayQueueAdapter playQueueAdapter;

    private RetainFragment retainFragment;

    private boolean shuffleOn = false;

    // from list view rearange
    // https://blogactivity.wordpress.com/2011/10/02/rearranging-items-in-a-listview/
    @Override
    public void onGrab(int index) {
        Log.d(TAG, "onGrab "+index);
        /*
        getItem(index).doSomething();
        notifyDataSetChanged();
        */
    }

    @Override
    public boolean onRearrangeRequested(int fromIndex, int toIndex) {
        Log.d(TAG, "onRearrangeRequested "+fromIndex+ " to " +toIndex);
/*
        retainFragment.serviceReference.playThisSongNext(fromSongID);


        if (toIndex >= 0 && toIndex < getCount()) {
            Object item = getItem(fromIndex);

            remove(item);
            insert(item, toIndex);
            notifyDataSetChanged();

            return true;
        }
        */
        return true;
    }

    @Override
    public void onDrop() {
        Log.d(TAG, "onDrop");
        /*
        doSomethingElse();
        notifyDataSetChanged();
        */
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

    private void updatePlayQueue() {
        Log.d(TAG, "updatePlayQueue");
        if (retainFragment.serviceReference != null) {
            ArrayList<Song> newPlayQueue = retainFragment.serviceReference.getPlayQueue();
            Log.d(TAG, "new play queue="+newPlayQueue.size());
            playQueue.clear();
            playQueue.addAll(newPlayQueue);
            //updatePlayingNext(nextPlayingSong.getArtist(), nextPlayingSong.getTitle());
            //playQueue.add(nextPlayingSong);
            playQueueAdapter.notifyDataSetChanged();

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

    private void updateNowPlaying(String songArtist, String songTitle) {
        tvNowPlaying.setText(songArtist + "-" + songTitle);
    }

    private void updatePlayingNext(String songArtist, String songTitle) {
        tvPlayingNext.setText(songArtist + "-" + songTitle);
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
        // TODO: get current song and update now playing

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
            playQueue.clear();
            playQueue.addAll(newPlayQueue);
            //updatePlayingNext(nextPlayingSong.getArtist(), nextPlayingSong.getTitle());
            //playQueue.add(nextPlayingSong);
            playQueueAdapter.notifyDataSetChanged();
        }
        /* restore the paused state (see onPause)
        if(paused){
            paused=false;
        }
        */
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_play_all);

        FragmentManager fm = getFragmentManager();
        retainFragment = (RetainFragment) fm.findFragmentByTag(AppConstants.TAG_RETAIN_FRAGMENT);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (retainFragment == null) {
            Log.d(TAG, "creating and adding retain Fragment");
            retainFragment = new RetainFragment();
            fm.beginTransaction().add(retainFragment, AppConstants.TAG_RETAIN_FRAGMENT).commit();
        }

        tvNowPlaying = (TextView) findViewById(R.id.tvPlaying);

//        lvPlayQueue = (ListView) findViewById(R.id.lvPlayQueue);
        rlvPlayQueue = (RearrangeableListView) findViewById(R.id.lvrearangablePlayQueue);
        rlvPlayQueue.setRearrangeListener(this);
        //rlvPlayQueue.setRearrangeEnabled(true);
        playQueue = new ArrayList<>();
        playQueueAdapter = new PlayQueueAdapter(this, playQueue);

//        lvPlayQueue.setAdapter(playQueueAdapter);
        rlvPlayQueue.setAdapter(playQueueAdapter);

        Log.d(TAG, "starting the service");
        Intent playIntent = new Intent(this, SimpleMusicService.class);
        startService(playIntent);
    }

    // bind to the Service instance when the Activity instance starts
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        retainFragment.doBindService();
    }

    // Don't stop the playback when the backbutton is pressed
    @Override
    public void onBackPressed() {
        Log.d(TAG, "onbackpressed");
        moveTaskToBack(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (isFinishing()) {
            Log.d(TAG, "isFinishing");
            // genuinely finishing, not orientation change etc
            Intent intentStopService = new Intent (this, SimpleMusicService.class);
            stopService(intentStopService);
            retainFragment.doUnbindService();
        }
    }

    public void btnNextClicked(View v) {
        Log.d(TAG, "btnNextClicked");
        playNextSong();
    }

    public void playNextSong() {
        if (retainFragment.isBound)
            retainFragment.serviceReference.playRandomSong();
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

    public void btnChooseSongsClicked(View v) {
        Log.d(TAG, "btnChooseSongs");
        Intent intent = new Intent(this, SelectSongsActivity.class);
        startActivity(intent);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
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
            case R.id.action_end:
                Intent playIntent = new Intent(this, SimpleMusicService.class);
                stopService(playIntent);
                retainFragment.serviceReference=null;
                System.exit(0);
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

    private  void removeSongbyID(int songPID) {
        if (retainFragment.isBound) {
            if (playQueue != null && playQueue.size() > 0) {
                retainFragment.serviceReference.removeSongFromPlayQueue(songPID);
            }
        }
    }

    private  void moveSongToTop(int songPID) {
        if (retainFragment.isBound) {
            if (playQueue != null && playQueue.size() > 0) {
                retainFragment.serviceReference.playThisSongNext(songPID);
            }
        }
    }

    // when user clicks the remove button on a song in the playqueue
    public void btnRemoveSongClicked(View view){
        Log.d(TAG, "picked view "+ (view==null?"null":"not null"));
        if (view!=null) {
            Object viewTag = view.getTag();
            Log.d(TAG, "picked viewTag " + (viewTag == null ? "null" : viewTag.toString()));
            if (viewTag != null) {
                int pickedSongPID = Integer.parseInt(viewTag.toString());
                removeSongbyID(pickedSongPID);
            }
        }
    }

    // when user clicks the up button on a song in the playqueue
    public void btnMoveToTopClicked(View view){
        Log.d(TAG, "picked view to move to top "+ (view==null?"null":"not null"));
        if (view!=null) {
            Object viewTag = view.getTag();
            Log.d(TAG, "picked viewTag " + (viewTag == null ? "null" : viewTag.toString()));
            if (viewTag != null) {
                int pickedSongPID = Integer.parseInt(viewTag.toString());
                moveSongToTop(pickedSongPID);
            }
        }
    }
}