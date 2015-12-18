package asbridge.me.uk.MMusic.activities;

import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import asbridge.me.uk.MMusic.R;
import asbridge.me.uk.MMusic.adapters.MusicTabsAdapter;
import asbridge.me.uk.MMusic.classes.Song;
import asbridge.me.uk.MMusic.GUIfragments.ArtistFragment;
import asbridge.me.uk.MMusic.GUIfragments.MusicPlayerFragment;
import asbridge.me.uk.MMusic.services.MusicService;
import asbridge.me.uk.MMusic.utils.Content;

import java.util.ArrayList;

/**
 * Created by David on 12/12/2015.
 */
public class PagerActivity extends FragmentActivity implements
        MusicPlayerFragment.MusicPlayerFragmentListener,
        ArtistFragment.OnArtistsChangedListener
{

    private String TAG = "DAVE:FragActivity";

    private MusicService musicSrv;

    private MusicTabsAdapter tabsAdapter;
    private MusicPlayerFragment mMusicPlayerFragment;
    private ArtistFragment mArtistFragment;

    private ArrayList<Song> songList;
    private boolean paused=false, playbackPaused=false;

    private Intent playIntent;

    private boolean mDualPane;

    // from artist fragment
    public void onArtistsChanged(ArrayList<String > artists)
    {
        Log.d(TAG, "onArtistsChanged:"+artists.size());
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
    }

    // from fragment music player
    public void onNextClicked()
    {
        Log.d(TAG, "onNextClicked");
        playbackPaused = false;
        musicSrv.playNext();
    }

    // from fragment music player
    public void onPlayClicked()
    {
        Log.d(TAG, "onPlayClicked");
        if (playbackPaused) {
            musicSrv.resumePlaying();// playFirst();
            playbackPaused = false;
        } else {
            Log.d(TAG, "playing -> pausing");


            musicSrv.pausePlayer();
            playbackPaused = true;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager);


        View detailsFrame = findViewById(R.id.landscape_fragments_container);


        // Check that a view exists (indicated landscape mode)
        mDualPane = detailsFrame != null
                && detailsFrame.getVisibility() == View.VISIBLE;

        Log.d(TAG, "dual pane=" + (mDualPane?"true":"false"));

        mMusicPlayerFragment = new MusicPlayerFragment();
        mArtistFragment = new ArtistFragment();
        if (mDualPane)
        {
            FragmentManager fm = getSupportFragmentManager();
            if (fm.findFragmentById(R.id.artist_fragment_container) == null) {
                fm.beginTransaction().add(R.id.artist_fragment_container, mArtistFragment).commit();
            }

            if (fm.findFragmentById(R.id.music_player_fragment_container) == null) {
                fm.beginTransaction().add(R.id.music_player_fragment_container, mMusicPlayerFragment).commit();
            }
    } else {
            ViewPager viewPager = (ViewPager) findViewById(R.id.pagertabs);
            tabsAdapter = new MusicTabsAdapter(getSupportFragmentManager(), mMusicPlayerFragment, mArtistFragment);
            viewPager.setAdapter(tabsAdapter);
        }

        mArtistFragment.setOnArtistsChangedListener(this);
        mMusicPlayerFragment.setListener(this);

        songList = new ArrayList<Song>();

        // if (playlistType.equals("all")) {
            Content.getAllSongs(this, songList);
    }

    // uses the saved paused state
    @Override
    protected void onResume(){
        super.onResume();

        Log.d(TAG, "songList " + (songList==null?"null":"not null"));
        Log.d(TAG, "mMusicPlayerFragment " + (mMusicPlayerFragment==null?"null":"not null"));
        mMusicPlayerFragment.setSongList(songList);

/*
        if (dataUpdateReceiver == null) dataUpdateReceiver = new SongPlayingReceiver();
        IntentFilter intentFilter = new IntentFilter("SONG_PLAYING");
        registerReceiver(dataUpdateReceiver, intentFilter);
        */
        if(paused){
            paused=false;
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

//            musicBound = true;
            //playRandom();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
//            musicBound = false;
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

    // when the app exits
    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy playintent is "+(playIntent==null?"null":"not null"));
        stopService(playIntent);
        musicSrv=null;
        super.onDestroy();
    }


}