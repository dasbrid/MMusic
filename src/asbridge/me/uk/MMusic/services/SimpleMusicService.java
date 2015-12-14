package asbridge.me.uk.MMusic.services;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import asbridge.me.uk.MMusic.classes.Song;
import asbridge.me.uk.MMusic.utils.Content;

import java.util.ArrayList;

/**
 * Created by David on 13/12/2015.
 * http://www.101apps.co.za/index.php/articles/binding-to-a-service-a-tutorial.html
 */
public class SimpleMusicService extends Service
        implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener
{

    private String TAG="DAVE:SimpleMusicService";

    private static final String ACTION_PLAY = "com.example.action.PLAY";
    private MediaPlayer player = null;

    /**
     * This allows binding. class MusicBinder implements the IBinder interface.
     * It implements the method getService() which returns the
     * singleton instance of this service, allowing access to the service by an application
     */
    // Single instance of the IBinder used to bind to the service
    // returned by onBind
    private final IBinder simpleMusicBinder = new SimpleMusicBinder();

    // Class implementing IBinder. Allows interaction from the app to the service
    // Just returns the instance of the service so that we can call it's methods
    public class SimpleMusicBinder extends Binder {
        public SimpleMusicService getService() {
            return SimpleMusicService.this;
        }
    }

    /**
     * Overrides abstract method of Service
     * This is called by applications that want to bind to our service.
     * Returns the singleton instance of binder object
     * @param intent
     * @return
     */
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Music Service onBind");
        return simpleMusicBinder;
    }



     // Service will be unbound when all bound activities have stopped
    @Override
    public boolean onUnbind(Intent intent){
        Log.d(TAG, "SimpleMusicService onUnbind");
        player.stop();
        player.release();
        return false;
    }

    // when the service starts from a call to startservice
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "SimpleMusicService onStartCommand");
        return START_STICKY; // Ensures that onStartCommand is called if the Service needs to restart after being killed by the system
    }

    public void onCreate(){
        Log.d(TAG, "SimpleMusicService onCreate");
        //create the service
        super.onCreate();

        // initialise the mediaplayer
        initialiseMediaPlayer();
    }

    private void initialiseMediaPlayer() {

        player = new MediaPlayer();
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        // set callback from prepareAsync
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        /*
        player.setOnErrorListener(this);
        */
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(TAG, "SimpleMusicService onCompletion");
        if(player.getCurrentPosition() > 0){
            mp.reset();
            // playNext(); // play the next song, but for now we just stop
        }
    }

    // required by interface MediaPlayer.OnPreparedListener
    // callback when ready to play a song, after calling prepareAsync
    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(TAG, "SimpleMusicService onPrepared");
        // start playback
        mp.start();
        // we can broadcast song started and set notification ... optionally
    }

    // play button pressed in the activity start playing the song
    public void startPlay() {
        Log.d(TAG, "SimpleMusicService startPlay");
        player.reset();
        //get a song
        ArrayList<Song> songList = new ArrayList<>();
        Content.getAllSongs(getApplicationContext(), songList);
        Song theSong = songList.get(0);
        long theSongId = theSong.getID();

        Log.d(TAG, "song="+theSong.getTitle()+" id="+ theSongId);
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, theSongId);
        try{
            player.setDataSource(getApplicationContext(), trackUri);
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        // prepare the player asynchronously and then the callback onPrepared will be called
        player.prepareAsync();
    }

    // stop button pressed in activity pause the player
    public void stopPlay() {
        Log.d(TAG, "SimpleMusicService stopPlay");
        player.pause();
    }

}