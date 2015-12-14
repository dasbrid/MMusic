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

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by David on 05/12/2015.
 * See http://developer.android.com/guide/components/services.html
 * A Bound service.
 *
 * To create a bound service, you must implement the onBind() callback method to
 * return an IBinder that defines the interface for communication with the service.
 * Other application components can then call bindService() to retrieve the interface
 * and begin calling methods on the service.
 * The service lives only to serve the application component that is bound to it,
 * so when there are no components bound to the service,
 * the system destroys it (you do not need to stop a bound service in the way you must
 * when the service is started through onStartCommand()).
 */
public class MusicService extends Service implements
    MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
    MediaPlayer.OnCompletionListener {

    private static String TAG = "DAVE:";

    //media player
    private MediaPlayer player = null;
    //song list
    private ArrayList<Song> songs;
    //current position
    private int songPosn;

    public interface NewSong {
        public void newSongPlaying(String title);
    }
/*
    private NewSong nsi = null;
    public void setnsi (NewSong newSong)
    {
        nsi=newSong;
    }
*/
    /**
     * This allows binding. class MusicBinder implements the IBinder interface.
     * It implements the method getService() which returns the
     * singleton instance of this service, allowing access to the service by an application
      */
    // Single instance of the IBinder used to bind to the service
    // returned by onBind
    private final IBinder musicBind = new MusicBinder();

    // Class implementing IBinder. Allows interaction from the app to the service
    // Just returns the instance of the service so that we can call it's methods
    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
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
        return musicBind;
    }

    /** Overrides method of service
     * Service will be unbound when all
     * @param intent
     * @return
     */
    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }

    private Song currentSong = null;
//    private String songTitle="";
    private static final int NOTIFY_ID=1;

    private boolean shuffle=false;
    private Random rand;

    // Toggles shuffle on & off
    public void setShuffle(){
        if(shuffle) shuffle=false;
        else shuffle=true;
    }

    // when service is destroyed stop the notification
    @Override
    public void onDestroy() {
        stopForeground(true);
    }

    public void playSong(){
        Log.d(TAG, "playSong player is "+(player==null?"null":"not null"));
        player.reset();
        //get song
        Song playSong = songs.get(songPosn);
        currentSong=playSong;
        Log.d(TAG, "playSong is "+(playSong==null?"null":"not null"));

//get id
        long currSong = playSong.getID();
//set uri
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);

        try{
            player.setDataSource(getApplicationContext(), trackUri);
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }

        player.prepareAsync();
    }

    // call to set the song to play
    public void setSong(int songIndex){
        Log.d(TAG, "setSong:"+songIndex);
        songPosn=songIndex;
    }



    // required by interfaces
    // callback when media player is ready, triggered after we have called prepareAsync
    public void onPrepared(MediaPlayer mp) {
        //start playback
        mp.start();

        // Broadcast the fact that a new song is now playing
        Intent songPlayingIntent = new Intent("SONG_PLAYING");
        songPlayingIntent.putExtra("SONG_TITLE", currentSong.getTitle());
        songPlayingIntent.putExtra("SONG_ARTIST", currentSong.getArtist());
        sendBroadcast(songPlayingIntent);


        // Create notification
        Intent notIntent = new Intent(this, MusicPlayerActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);

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
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(player.getCurrentPosition() > 0){
            mp.reset();
            playNext();
        }
    }

    public void onCreate(){
        Log.d(TAG, "MusicService onCreate");
        //create the service
        super.onCreate();
        rand=new Random();
        //initialize position
        songPosn=0;
        //create player
        player = new MediaPlayer();
        initMusicPlayer();
    }

    public void initMusicPlayer(){
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public void setList(ArrayList<Song> theSongs){
        Log.d(TAG, "setList count ="+theSongs.size());
        songs=theSongs;
    }


    private int currentPos = 0;

    /*****************************
     * Following methods used by activity to control playback
     *****************************/
    public int getPosn(){
        return player.getCurrentPosition();
    }

    public int getDur(){
        return player.getDuration();
    }

    public boolean isPng(){
        return player.isPlaying();
    }

    public void pausePlayer(){
        player.pause();
        currentPos = player.getCurrentPosition();
    }

    public void resumePlaying() {
        player.seekTo(currentPos);
        player.start();
    }

    public void seek(int posn){
        player.seekTo(posn);
    }

    public void go(){
        player.start();
    }

    public void playRandom() {
        int newSong = songPosn;
        while(newSong==songPosn){
            newSong=rand.nextInt(songs.size());
        }
        setSong(newSong);
        playSong();
    }

    public void playFirst() {
        if (songs.size() > 0){
            setSong(0);
            playSong();
        }
    }

    // skip to previous
    public void playPrev(){
        songPosn--;
        if(songPosn < 0) songPosn=songs.size()-1;
        playSong();
    }

    //skip to next
    public void playNext(){
        if (shuffle) {
            playRandom();
        } else {
            songPosn++;
            if (songPosn >= songs.size()) songPosn = 0;
            playSong();
        }
    }
    /*****************************
     * Previous methods used by activity to control playback
     *****************************/
}