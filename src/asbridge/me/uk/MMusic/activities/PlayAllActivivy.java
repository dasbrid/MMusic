package asbridge.me.uk.MMusic.activities;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import asbridge.me.uk.MMusic.R;
import asbridge.me.uk.MMusic.classes.Song;
import asbridge.me.uk.MMusic.services.SimpleMusicService;
import asbridge.me.uk.MMusic.utils.AppConstants;
import asbridge.me.uk.MMusic.utils.Content;

import java.util.ArrayList;

/**
 * Created by David on 13/12/2015.
 http://www.101apps.co.za/index.php/articles/binding-to-a-service-a-tutorial.html
 */
public class PlayAllActivivy extends Activity {

    private String TAG = "DAVE:PlayAllActivivy";
    private boolean musicBound;
    private ArrayList<Song> songList;
    private SimpleMusicService musicSrv;

    ///////
    private boolean isBound;
    private SimpleMusicService serviceReference;

    private TextView tvNowPlaying;

    private SongPlayingReceiver songPlayingReceiver;
    // When the service starts playing a song it will broadcast the title
    private class SongPlayingReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(AppConstants.INTENT_ACTION_SONG_PLAYING)) {
                String songTitle = intent.getStringExtra(AppConstants.INTENT_EXTRA_SONG_TITLE);
                String songArtist = intent.getStringExtra(AppConstants.INTENT_EXTRA_SONG_ARTIST);
                updateNowPlaying(songArtist, songTitle);
            }
        }
    }

    private void updateNowPlaying(String songArtist, String songTitle) {
        tvNowPlaying.setText(songArtist + "-" + songTitle);
    }

    // used to save paused state so it can be resumed
    @Override
    protected void onPause(){
        super.onPause();
        Log.d(TAG, "onPause");
        if (songPlayingReceiver != null) unregisterReceiver(songPlayingReceiver);

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
        IntentFilter intentFilter = new IntentFilter("SONG_PLAYING");
        registerReceiver(songPlayingReceiver, intentFilter);

        if (serviceReference != null) {
            Song currentSong = serviceReference.getCurrentSong();
            if (currentSong != null)
                updateNowPlaying(currentSong.getArtist(), currentSong.getTitle());
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

        tvNowPlaying = (TextView) findViewById(R.id.tvPlaying);

        Intent playIntent = new Intent(this, SimpleMusicService.class);
        startService(playIntent);
    }

    //start the Service instance when the Activity instance starts
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        doBindService();
    }

    /* if we include this then closing the activity unbinds and stops the service ...
    ... not what we want
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        Log.d(TAG, "unBindService");
        Intent bindIntent = new Intent(this, SimpleMusicService.class);
        unbindService(myConnection);
        isBound = false;
    }
*/
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
            unbindService(myConnection); ///???
        }
    }

    public void btnNextClicked(View v) {
        Log.d(TAG, "btnNextClicked");
        playNextSong();
    }

    public void playNextSong() {
        if (isBound)
            serviceReference.playNextSong();

    }

    public void btnStopClicked(View v) {
        Log.d(TAG, "btnStopClicked");
        stopPlayback();
    }

    private void stopPlayback() {
        if (isBound)
            serviceReference.stopPlay();

    }

    //connect to the service
    private ServiceConnection myConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "Service Connected");
            SimpleMusicService.SimpleMusicBinder binder = (SimpleMusicService.SimpleMusicBinder)service;
            //get service
            serviceReference = binder.getService();

            // set the list of songs in the service
            ArrayList<Song> songList = new ArrayList<>();
            Content.getAllSongs(getApplicationContext(), songList);
            serviceReference.setSongList(songList);

            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
            serviceReference = null;
            isBound = false;
        }
    };

    private void doBindService() {
        Log.d(TAG, "BindService");
        if (!isBound) {
            Log.d(TAG, "binding");
            Intent bindIntent = new Intent(this, SimpleMusicService.class);
            isBound = bindService(bindIntent, myConnection, Context.BIND_AUTO_CREATE);
        }
    }
/*
    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            SimpleMusicService.SimpleMusicBinder binder = (SimpleMusicService.SimpleMusicBinder)service;
            //get service
            musicSrv = binder.getService();
            //pass list
            //musicSrv.setList(songList);

            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
            musicBound = false;
        }
    };

    */
}