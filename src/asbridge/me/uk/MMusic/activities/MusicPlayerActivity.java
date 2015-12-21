package asbridge.me.uk.MMusic.activities;

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
import asbridge.me.uk.MMusic.classes.Song;
import asbridge.me.uk.MMusic.GUIfragments.MusicPlayerFragment;
import asbridge.me.uk.MMusic.services.MusicService;
import asbridge.me.uk.MMusic.GUIfragments.ArtistFragment;
import asbridge.me.uk.MMusic.utils.Content;
import android.support.v4.app.FragmentActivity;

import java.util.ArrayList;

public class MusicPlayerActivity extends FragmentActivity
        implements ArtistFragment.OnSongsChangedListener,
                    MusicPlayerFragment.MusicPlayerFragmentListener,
                    View.OnClickListener {

    private ArrayList<Song> songList;
    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound=false;
    private static String TAG = "DAVE:";


    private ArtistFragment mArtistFragment;
    private MusicPlayerFragment mMusicPlayerFragment;

    private boolean paused=false, playbackPaused=false;

    private SongPlayingReceiver dataUpdateReceiver;

    // When the service starts playing a song it will broadcast the title
    private class SongPlayingReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("SONG_PLAYING")) {
                String songTitle = intent.getStringExtra("SONG_TITLE");
                String songArtist = intent.getStringExtra("SONG_ARTIST");
                mMusicPlayerFragment.setNowPlaying(songArtist, songTitle);
            }
        }
    }



    public void onSongsChanged()
    {
        /*
        Log.d(TAG, "onSongsChanged:"+artists.size());
        if (artists.size()==0)
            return;

        songList.clear();
        Content.getSongsForGivenArtistList(this, artists, songList );
        musicSrv.pausePlayer();
        musicSrv.setList(songList);
        mMusicPlayerFragment.setSongList(songList);
        musicSrv.playFirst();
        musicSrv.setList(songList);

        mMusicPlayerFragment.setSongList(songList);
//        songAdt.notifyDataSetChanged();
        musicSrv.playFirst();
        */
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

        mMusicPlayerFragment.setSongList(songList);

        if (dataUpdateReceiver == null) dataUpdateReceiver = new SongPlayingReceiver();
        IntentFilter intentFilter = new IntentFilter("SONG_PLAYING");
        registerReceiver(dataUpdateReceiver, intentFilter);
        if(paused){
            paused=false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnPlayAll:
                playAll();
                break;
            default:
                break;
        }
    }
    // from fragment music player
    public void onPlayClicked()
    {
        Log.d(TAG, "onPlayClicked");
        if (playbackPaused) {
            //TODO: Don't start at the first song each time




            musicSrv.resumePlaying();// playFirst();
            playbackPaused = false;
        } else {
            Log.d(TAG, "playing -> pausing");


            musicSrv.pausePlayer();
            playbackPaused = true;
        }
    }

    // from fragment music player
    public void onNextClicked()
    {
        Log.d(TAG, "onNextClicked");
        playbackPaused = false;
        musicSrv.playNext();
    }

    // from button in activita
    public void playAll() {
        Log.d(TAG, "playAll");
        songList.clear();
        Content.getAllSongs(this, songList );
        musicSrv.pausePlayer();
        musicSrv.setList(songList);
        mMusicPlayerFragment.setSongList(songList);
        //songAdt.notifyDataSetChanged();
        musicSrv.playFirst();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    public void setArtistFragment(Fragment frag)
    {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentById(R.id.artist_fragment_container) == null) {
            fm.beginTransaction().add(R.id.artist_fragment_container, frag).commit();
        }
    }

    public void setMusicPlayerFragment(Fragment frag)
    {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentById(R.id.music_player_fragment_container) == null) {
            fm.beginTransaction().add(R.id.music_player_fragment_container, frag).commit();
        }
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        TAG = TAG+(getClass().getSimpleName());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_musicplayer);

        mArtistFragment = new ArtistFragment();
        //Now you can set the fragment to be visible here
        setArtistFragment(mArtistFragment);
        mArtistFragment.setOnSongsChangedListener(this);

        mMusicPlayerFragment = new MusicPlayerFragment();
        //Now you can set the fragment to be visible here
        setMusicPlayerFragment(mMusicPlayerFragment);
        mMusicPlayerFragment.setListener(this);

        String playlistType = "all";
        String artistname = null;

        Bundle parameters = getIntent().getExtras();
        if (parameters != null) {
            playlistType = parameters.getString("playlistType");
            artistname = parameters.getString("artistname");
        }

        Button btnPlayAll = (Button) findViewById(R.id.btnPlayAll);
        btnPlayAll.setOnClickListener(this);
        songList = new ArrayList<Song>();

        if (playlistType.equals("all")) {
            Content.getAllSongs(this, songList);
        } else if (playlistType.equals("artist")) {
            Content.getSongsForGivenArtist(this, artistname, songList);
        }
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
                return true;
            case R.id.action_end:
                stopService(playIntent);
                musicSrv=null;
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
