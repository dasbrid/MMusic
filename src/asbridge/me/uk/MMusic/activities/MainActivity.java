package asbridge.me.uk.MMusic.activities;

import android.app.Activity;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.MediaController;
import asbridge.me.uk.MMusic.R;
import asbridge.me.uk.MMusic.adapters.SongAdapter;
import asbridge.me.uk.MMusic.classes.MusicController;
import asbridge.me.uk.MMusic.classes.Song;
import asbridge.me.uk.MMusic.services.MusicService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends Activity implements MediaController.MediaPlayerControl {

    private ArrayList<Song> songList;
    private ListView songView;

    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound=false;
    private static String TAG = "DAVE:";

    private MusicController controller;

    private boolean paused=false, playbackPaused=false;

    // used to save paused state so it can be resumed
    @Override
    protected void onPause(){
        super.onPause();
        paused=true;
    }

    // uses the saved paused state
    @Override
    protected void onResume(){
        super.onResume();
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
        controller.setAnchorView(findViewById(R.id.song_list));
        controller.setEnabled(true);
    }

    //play next (calls the method in the music controller)
    private void playNext(){
        musicSrv.playNext();
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

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        TAG = TAG+(getClass().getSimpleName());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // setup the music controller
        setController();

        songView = (ListView)findViewById(R.id.song_list);

        songList = new ArrayList<Song>();
        getSongList();

        // sort the songs by title
        Collections.sort(songList, new Comparator<Song>(){
            public int compare(Song a, Song b){
                return a.getTitle().compareTo(b.getTitle());
            }
        });

        SongAdapter songAdt = new SongAdapter(this, songList);
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
                    if(playbackPaused){
                        setController();
                        playbackPaused=false;
                    }
                    controller.show(0);
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
        moveTaskToBack(true);
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

    public void getSongList() {
        //retrieve song info
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        Log.d(TAG, "got songs from ContentResolver, count = " + (musicCursor == null ? null : musicCursor.getCount()));
        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                // Log.d(TAG, "adding song, title = " + thisTitle);
                songList.add(new Song(thisId, thisTitle, thisArtist));
            }
            while (musicCursor.moveToNext());
        }
    }
}
