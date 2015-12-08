package asbridge.me.uk.MMusic.activities;

import android.app.Activity;
import android.support.v4.app.FragmentManager;
import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import asbridge.me.uk.MMusic.R;
import asbridge.me.uk.MMusic.adapters.SongAdapter;
import asbridge.me.uk.MMusic.classes.MusicController;
import asbridge.me.uk.MMusic.classes.Song;
import asbridge.me.uk.MMusic.services.MusicService;
import asbridge.me.uk.MMusic.tabs.ArtistTab;
import asbridge.me.uk.MMusic.utils.Content;
import android.support.v4.app.FragmentActivity;

import java.util.ArrayList;

public class MusicPlayerActivity extends FragmentActivity implements ArtistTab.OnArtistsChangedListener,/*MusicService.NewSong,*/ View.OnClickListener, MediaController.MediaPlayerControl {

    private ArrayList<Song> songList;
    private ListView songView;
    private SongAdapter songAdt;
    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound=false;
    private static String TAG = "DAVE:";

    private MusicController controller;

    private boolean paused=false, playbackPaused=false;

    private SongPlayingReceiver dataUpdateReceiver;

    // When the service starts playing a song it will broadcast the title
    private class SongPlayingReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("SONG_PLAYING")) {
                String songTitle = intent.getStringExtra("SONG_TITLE");
                String songArtist = intent.getStringExtra("SONG_ARTIST");
                TextView tvNowPlaying = (TextView) findViewById(R.id.tvNowPlaying);
                tvNowPlaying.setText(songArtist + "--" + songTitle);
            }
        }
    }

    public void onArtistsChanged(ArrayList<String > artists)
    {
        songList.clear();
        Content.getSongsForGivenArtistList(this, artists, songList );
        musicSrv.pausePlayer();
        musicSrv.setList(songList);
        songAdt.notifyDataSetChanged();
        musicSrv.playFirst();
        musicSrv.setList(songList);
        songAdt.notifyDataSetChanged();
        musicSrv.playFirst();
    }

    // used to save paused state so it can be resumed
    @Override
    protected void onPause(){
        super.onPause();
        if (dataUpdateReceiver != null) unregisterReceiver(dataUpdateReceiver);
        paused=true;
    }

    // uses the saved paused state
    @Override
    protected void onResume(){
        super.onResume();
        if (dataUpdateReceiver == null) dataUpdateReceiver = new SongPlayingReceiver();
        IntentFilter intentFilter = new IntentFilter("SONG_PLAYING");
        registerReceiver(dataUpdateReceiver, intentFilter);
        if(paused){
            setController();
            paused=false;
        }
    }

    @Override
    protected void onStop() {
        controller.hide();
        super.onStop();
    }

    /*********************************************************
     * Following methods from the mediaPlayerControl interface
     *********************************************************/
    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getCurrentPosition() {
        if(musicSrv!=null && musicBound && musicSrv.isPng())
        return musicSrv.getPosn();
        else return 0;
    }

    @Override
    public int getDuration() {
        if(musicSrv!=null && musicBound && musicSrv.isPng())
        return musicSrv.getDur();
        else return 0;
    }

    @Override
    public boolean isPlaying() {
        if(musicSrv!=null && musicBound)
        return musicSrv.isPng();
        return false;
    }

    @Override
    public void pause() {
        playbackPaused=true;
        musicSrv.pausePlayer();
    }

    @Override
    public void seekTo(int pos) {
        musicSrv.seek(pos);
    }

    @Override
    public void start() {
        musicSrv.go();
    }

    @Override
    public int getBufferPercentage() {
        if (getDuration() == 0)
            return 0;
        return (getCurrentPosition()*100)/getDuration();
    }
    /*********************************************************
     * Previous methods are from the mediaPlayerControl interface
     *********************************************************/

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnNext:
                musicSrv.playNext();;
                break;
            case R.id.btnPlay:
                if (playbackPaused) {
                    musicSrv.playFirst();
                    playbackPaused = false;
                } else {
                    musicSrv.pausePlayer();
                    playbackPaused = true;
                }
                break;
            case R.id.btnPlayAll:
                playAll();
                break;
            default:
                break;
        }
    }

    public void playAll() {
        Log.d(TAG, "playAll");
        songList.clear();
        Content.getAllSongs(this, songList );
        musicSrv.pausePlayer();
        musicSrv.setList(songList);
        songAdt.notifyDataSetChanged();
        musicSrv.playFirst();
    }

    // called in oncreate to set up the music controller
    private void setController(){
        //set the controller up
        controller = new MusicController(this);
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });

        controller.setMediaPlayer(this);

        Button btnNext = (Button) findViewById(R.id.btnNext);
        btnNext.setOnClickListener(this);
        Button btnPlay = (Button) findViewById(R.id.btnPlay);
        btnPlay.setOnClickListener(this);
        Button btnPlayAll = (Button) findViewById(R.id.btnPlayAll);
        btnPlayAll.setOnClickListener(this);

        controller.setAnchorView(findViewById(R.id.song_list));
        controller.setEnabled(true);
    }

    private void playFirst() {
        musicSrv.playFirst();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }

    private void playRandom() {
        musicSrv.playRandom();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }

    // play next (calls the method in the music controller)
    // called when user clicks on next button in the controller
    private void playNext(){
        // call the music service method to play the next tune
        musicSrv.playNext();
        // starts playing if the playing was psused
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }

    //play previous (calls the method in the music controller)
    private void playPrev(){
        musicSrv.playPrev();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    public void setFragment(Fragment frag)
    {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentById(R.id.artist_fragment_container) == null) {
            fm.beginTransaction().add(R.id.artist_fragment_container, frag).commit();
        }

    }

    private ArtistTab mArtistFragment;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        TAG = TAG+(getClass().getSimpleName());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_musicplayer);

        mArtistFragment = new ArtistTab();

        //Now you can set the fragment to be visible here
        setFragment(mArtistFragment);
        mArtistFragment.setOnArtistsChangedListener(this);
        String playlistType = "all";
        String artistname = null;

        Bundle parameters = getIntent().getExtras();
        if (parameters != null) {
            playlistType = parameters.getString("playlistType");
            artistname = parameters.getString("artistname");
        }

        // setup the music controller
        setController();

        songView = (ListView)findViewById(R.id.song_list);
        songList = new ArrayList<Song>();

        if (playlistType.equals("all")) {
            Content.getAllSongs(this, songList);
        } else if (playlistType.equals("artist")) {
            Content.getSongsForGivenArtist(this, artistname, songList);
        }

/*
        // Ordering is done in the get* methods
        // sort the songs by title
        Collections.sort(songList, new Comparator<Song>(){
            public int compare(Song a, Song b){
                return a.getTitle().compareTo(b.getTitle());
            }
        });
*/
        songAdt = new SongAdapter(this, songList);
        songView.setAdapter(songAdt);
    }

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            //get service
            musicSrv = binder.getService();
            //pass list
            musicSrv.setList(songList);

            musicBound = true;
            //playRandom();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
            musicBound = false;
        }
    };

    //start the Service instance when the Activity instance starts
    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "onStart, playIntent="+(playIntent==null?"null":"not null"));
        if(playIntent==null){
            playIntent = new Intent(this, MusicService.class);
            Log.d(TAG, "binding the service");
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }

    }

    // when user clicks on song in the list
    public void songPicked(View view){
        Log.d(TAG, "picked view "+ (view==null?"null":"not null"));
        if (view!=null) {
            Object viewTag = view.getTag();
            Log.d(TAG, "picked viewTag " + (viewTag == null ? "null" : viewTag.toString()));
            if (viewTag != null) {
                int pickedSongIndex = Integer.parseInt(view.getTag().toString());
                Log.d(TAG, "picked song" + pickedSongIndex);

                Log.d(TAG, "musicsrv:" + (musicSrv == null ? "null" : musicSrv.toString()));
                if (musicSrv != null) {
                    musicSrv.setSong(pickedSongIndex);
                    musicSrv.playSong();
/* TEMP_CONTROLLER
                    if(playbackPaused){
                        setController();
                        playbackPaused=false;
                    }
                    controller.show(0);
*/
                }
            }
        }
    }

    // when the app exits
    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy playintent is "+(playIntent==null?"null":"not null"));
        stopService(playIntent);
        musicSrv=null;
        super.onDestroy();
    }

    // Don't stop the playback when the backbutton is pressed
    @Override
    public void onBackPressed() {
        Log.d(TAG, "onbackpressed");
        moveTaskToBack(true);
//        super.onBackPressed();
    }

    // handle user interaction with the menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_shuffle:
                musicSrv.setShuffle();
                break;
            case R.id.action_end:
                stopService(playIntent);
                musicSrv=null;
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
