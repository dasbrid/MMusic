package asbridge.me.uk.MMusic.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.*;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import asbridge.me.uk.MMusic.R;
import asbridge.me.uk.MMusic.activities.PlayAllActivivy;
import asbridge.me.uk.MMusic.classes.Song;
import asbridge.me.uk.MMusic.utils.AppConstants;

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
    private int currentPos = 0;
    private boolean stopped = true;

    // broadcast receeiver receives actions from notification
    private MusicControlListener musicControlListener;
    public class MusicControlListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "SwitchButtonListener:OnReceive"+intent.getAction());
            if (intent.getAction().equals(AppConstants.INTENT_ACTION_STOP)) {
                Log.d(TAG, "SwitchButtonListener:OnReceive:STOP_EVENT");
                stopPlay();
            } else if (intent.getAction().equals(AppConstants.INTENT_ACTION_NEXT)) {
                Log.d(TAG, "SwitchButtonListener:OnReceive:NEXT_EVENT");
                playSong();
            }
        }
    }

    @Override
    public void onDestroy() {
        if (musicControlListener != null) unregisterReceiver(musicControlListener);
        super.onDestroy();
    }
    // called from activity to set the songs to play
    public void setSongList(ArrayList<Song> songList) {
        this.songs = songList;
    }

    // returns the current playing song
    public Song getCurrentSong() {
        if (songs == null || songs.size() == 0 || currentSongIndex == -1)
            return null;
        return songs.get(currentSongIndex);
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
        // set up the listener for the broadcast from the notification (play next and stop buttons)
        if (musicControlListener == null) musicControlListener = new MusicControlListener();
        registerReceiver(musicControlListener, new IntentFilter(AppConstants.INTENT_ACTION_NEXT));
        registerReceiver(musicControlListener, new IntentFilter(AppConstants.INTENT_ACTION_STOP));
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
        stopped = false;
        Song currentSong = songs.get(currentSongIndex);

        // Broadcast the fact that a new song is now playing
        // can be used by the activity to update its textview
        Intent songPlayingIntent = new Intent(AppConstants.INTENT_ACTION_SONG_PLAYING);
        songPlayingIntent.putExtra(AppConstants.INTENT_EXTRA_SONG_TITLE, currentSong.getTitle());
        songPlayingIntent.putExtra(AppConstants.INTENT_EXTRA_SONG_ARTIST, currentSong.getArtist());
        sendBroadcast(songPlayingIntent);

        // we can broadcast song started and set notification
        Intent notIntent = new Intent(this, PlayAllActivivy.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // pending intent to go back to the activity
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //this is the intent that is supposed to be called when the button is clicked
        Intent stopIntent = new Intent(AppConstants.INTENT_ACTION_STOP);
        PendingIntent pendingStopIntent = PendingIntent.getBroadcast(this, 0, stopIntent, 0);
        Intent nextIntent = new Intent(AppConstants.INTENT_ACTION_NEXT);
        PendingIntent pendingNextIntent = PendingIntent.getBroadcast(this, 0, nextIntent, 0);


        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendInt)
                .addAction(R.drawable.ic_launcher, "Stop", pendingStopIntent)
                .addAction(R.drawable.ic_launcher, "Next", pendingNextIntent)
                .setSmallIcon(R.drawable.play)
                .setTicker(currentSong.getTitle())
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(currentSong.getTitle())
                .setPriority(Notification.PRIORITY_MAX);
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

    // can be called from outside the service (e.g. from next button in the activity)
    public void playSong() {
        Log.d(TAG, "SimpleMusicService playNextSong");
        if (stopped)
            playNext();
        else
            resumePlaying();
    }

    // play button pressed in the activity start playing the song
    private void playNext() {
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
        currentSongIndex = newSongIndex;
        Song theSong = this.songs.get(currentSongIndex);
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
        //TODO: Cancel notification
        player.reset();
        stopped = true;
    }

    // stop button pressed in activity pause the player
    public void pausePlay() {
        Log.d(TAG, "SimpleMusicService pausePlay");
        // TODO: set pause flag and resume afterwards
        player.pause();
        currentPos = player.getCurrentPosition();
    }

    public void resumePlaying() {
        player.seekTo(currentPos);
        player.start();
    }

}