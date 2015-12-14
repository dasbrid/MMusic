package asbridge.me.uk.MMusic.services;

import android.app.Notification;
import android.app.PendingIntent;
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
import asbridge.me.uk.MMusic.R;
import asbridge.me.uk.MMusic.activities.MusicPlayerActivity;
import asbridge.me.uk.MMusic.classes.Song;
import asbridge.me.uk.MMusic.utils.Content;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by David on 13/12/2015.
 * http://www.101apps.co.za/index.php/articles/binding-to-a-service-a-tutorial.html
 */
public class SimpleMusicService extends Service
        implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener
{

    private String TAG="DAVE:SimpleMusicService";

    private static final int NOTIFY_ID=1;
    private static final String ACTION_PLAY = "com.example.action.PLAY";
    private MediaPlayer player = null;
    //song list
    private ArrayList<Song> songs = null;
    private Random rand;

    private int currentSongIndex = -1;

    public void setSongList(ArrayList<Song> songList) {
        this.songs = songList;
    }

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
        rand=new Random();
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

    // required by interface MediaPlayer.OnPreparedListener
    // callback when ready to play a song, after calling prepareAsync
    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(TAG, "SimpleMusicService onPrepared");
        // start playback
        mp.start();
        // we can broadcast song started and set notification ... optionally
        // Create notification
        Intent notIntent = new Intent(this, MusicPlayerActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);
        Song currentSong = songs.get(currentSongIndex);
        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.play)
                .setTicker(currentSong.getTitle())
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(currentSong.getTitle());
        Notification not = builder.build();

        startForeground(NOTIFY_ID, not);

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(TAG, "SimpleMusicService onCompletion");
        if(player.getCurrentPosition() > 0){
            mp.reset();
            playNext();
        }
    }

    public void startPlay() {
        Log.d(TAG, "SimpleMusicService startPlay");
        playNext();
    }

    // play button pressed in the activity start playing the song
    public void playNext() {
        Log.d(TAG, "SimpleMusicService playNext");
        player.reset();
        //get a song
        if (this.songs == null || this.songs.size() < 1) {
            Log.d(TAG, "no songs to play");
            return;
        }

        int newSongIndex = currentSongIndex;
        while(newSongIndex == currentSongIndex){
            newSongIndex=rand.nextInt(songs.size());
        }

        Song theSong = this.songs.get(newSongIndex);
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