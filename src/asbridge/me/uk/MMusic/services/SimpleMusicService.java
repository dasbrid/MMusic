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
import asbridge.me.uk.MMusic.activities.PlayQueueActivity;
import asbridge.me.uk.MMusic.classes.Song;
import asbridge.me.uk.MMusic.utils.AppConstants;
import asbridge.me.uk.MMusic.utils.MusicContent;
import asbridge.me.uk.MMusic.utils.Settings;

import java.util.*;

/**
 * Created by David on 13/12/2015.
 * http://www.101apps.co.za/index.php/articles/binding-to-a-service-a-tutorial.html
 */
public class SimpleMusicService extends Service
        implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener
{

    private String TAG="DAVE:SimpleMusicService";

    private static final int NOTIFY_ID=1;
    private static final String ACTION_PLAY = "com.example.action.PLAY";
    private MediaPlayer player = null;
    //song list
//    private ArrayList<Song> songs = null;
    private Random rand;

    private LinkedList<Song> playQueue;
    private Song currentSong;

    // current position in playing song
    private int currentPos = 0;

    private boolean shuffleOn = true;
    private int currentPickedSong = -1;

    private int nextSongPID = 0;

    private static final int STOPPED = 0;
    private static final int PAUSED = 1;
    private static final int PLAYING = 2;
    private int currentState = STOPPED;


    private IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    // broadcast receiver to kill audio when headphones unplugged
    private NoisyAudioStreamReceiver becomingNoisyListener;
    private class NoisyAudioStreamReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                Log.d(TAG, "NoisyAudioStreamReceiver:OnReceive"+intent.getAction());
                pausePlayback();
            }
        }
    }

    // broadcast receeiver receives actions from notification
    private MusicControlListener musicControlListener;
    public class MusicControlListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "SwitchButtonListener:OnReceive"+intent.getAction());
            if (intent.getAction().equals(AppConstants.INTENT_ACTION_STOP_PLAYBACK)) {
                stopPlayback();
            } else if (intent.getAction().equals(AppConstants.INTENT_ACTION_PLAY_NEXT_SONG)) {
                playNextSongInPlayQueue();
            } else if (intent.getAction().equals(AppConstants.INTENT_ACTION_PAUSEORRESUME_PLAYBACK)) {
                pauseOrResumePlayack();
            }
        }
    }

    public boolean getShuffleState() {
        return shuffleOn;
    }

    public void setShuffleState(boolean newState) {
        shuffleOn = newState;
        Log.d(TAG, "shuffle set "+ (shuffleOn?"on":"off"));
    }

    private Calendar sleepTime = null;

    public void setSleepTimer(int mins) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, mins);
        sleepTime = c;
    }

    public long getTimeTillSleep() {
        if (sleepTime == null) return -1;
        Calendar currentTime = Calendar.getInstance();
        long diff = sleepTime.getTimeInMillis() - currentTime.getTimeInMillis();
        long mins = diff / 1000 / 60;
        return mins;
    }

    public void cancelSleepTimer() {
        sleepTime = null;
    }

    @Override
    public void onDestroy() {
        if (musicControlListener != null) unregisterReceiver(musicControlListener);
        if (becomingNoisyListener != null) unregisterReceiver(becomingNoisyListener);
        Log.d(TAG, "stored shuffle " + (shuffleOn?"on":"off"));
        Settings.setShuffleState(getApplicationContext(), shuffleOn);
        super.onDestroy();
    }
/*
    // called from activity to set the songs to play
    public void setSongList(ArrayList<Song> songList) {
        Log.d(TAG, "setSongList");
        this.songs = songList;
    }

    // called from activity to set the songs to play
    public ArrayList<Song> getSongList() {
        Log.d(TAG, "getSongList "+songs.size());
        return songs;
    }
*/
    // returns the current playing song
    public Song getCurrentSong() {
        return currentSong;
    }

    public ArrayList<Song> getPlayQueue() {
        Log.d(TAG, "getPlayQueue="+playQueue.size());
        return new ArrayList<Song> (playQueue);
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
        shuffleOn = Settings.getShuffleState(getApplicationContext());
        Log.d(TAG, "shuffle is " + (shuffleOn?"on":"off"));
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
        registerReceiver(musicControlListener, new IntentFilter(AppConstants.INTENT_ACTION_PLAY_NEXT_SONG));
        registerReceiver(musicControlListener, new IntentFilter(AppConstants.INTENT_ACTION_STOP_PLAYBACK));
        registerReceiver(musicControlListener, new IntentFilter(AppConstants.INTENT_ACTION_PAUSEORRESUME_PLAYBACK));
        if (becomingNoisyListener == null) becomingNoisyListener = new NoisyAudioStreamReceiver();
        registerReceiver(becomingNoisyListener, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));

        currentSong = null;
        playQueue = new LinkedList<>(); //ArrayList<>();
    }

    private void initialiseMediaPlayer() {

        player = new MediaPlayer();
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        // set callback from prepareAsync
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    // required by interface MediaPlayer.OnPreparedListener
    // callback when ready to play a song, after calling prepareAsync
    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(TAG, "SimpleMusicService onPrepared");
        // start playback
        mp.start();
        currentState = PLAYING;

        // Broadcast the fact that a new song is now playing
        // can be used by the activity to update its textview
        Intent songPlayingIntent = new Intent(AppConstants.INTENT_ACTION_SONG_PLAYING);
        songPlayingIntent.putExtra(AppConstants.INTENT_EXTRA_SONG_TITLE, currentSong.getTitle());
        songPlayingIntent.putExtra(AppConstants.INTENT_EXTRA_SONG_ARTIST, currentSong.getArtist());
        sendBroadcast(songPlayingIntent);

        // we can broadcast song started and set notification
        Intent notIntent = new Intent(this, PlayQueueActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // pending intent to go back to the activity
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //this is the intent that is supposed to be called when the button is clicked
        Intent stopIntent = new Intent(AppConstants.INTENT_ACTION_STOP_PLAYBACK);
        PendingIntent pendingStopIntent = PendingIntent.getBroadcast(this, 0, stopIntent, 0);
        Intent playnextIntent = new Intent(AppConstants.INTENT_ACTION_PLAY_NEXT_SONG);
        PendingIntent pendingNextIntent = PendingIntent.getBroadcast(this, 0, playnextIntent, 0);
        Intent pauseorresumePlaybackIntent = new Intent(AppConstants.INTENT_ACTION_PAUSEORRESUME_PLAYBACK);
        PendingIntent pendingpauseorresumeIntent = PendingIntent.getBroadcast(this, 0, pauseorresumePlaybackIntent, 0);


        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendInt)
                .addAction(R.drawable.playpause, "Pause/Play", pendingpauseorresumeIntent)
                .addAction(R.drawable.stop, "Stop", pendingStopIntent)
                .addAction(R.drawable.next, "Next", pendingNextIntent)
                .setSmallIcon(R.drawable.ic_launcher)
                .setTicker(currentSong.getTitle())
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(currentSong.getTitle())
                .setPriority(Notification.PRIORITY_MAX);
        Notification not = builder.build();

        startForeground(NOTIFY_ID, not);
    }

    // callback from media player when song finishes
    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(TAG, "SimpleMusicService onCompletion");
        if(player.getCurrentPosition() > 0){
            mp.reset();
            playNextSongInPlayQueue();
        }
    }

    private boolean timeToGoToSleep() {
        if (sleepTime == null)
            return false; // no sleep timer set
        Calendar currentTime = Calendar.getInstance();
        if (currentTime.after(sleepTime))
            return true;
        return false;
    }
    // can be called from outside the service (e.g. from next button in the activity)
    public void playSong() {
        Log.d(TAG, "SimpleMusicService playNextSong");
        if (currentState == STOPPED) { // if we are not playing anything, then play a random song
            playNextSongInPlayQueue();
        } else {
            resumePlaying(); // otherwise resume playing the current song
        }
    }

    // choose a random song (index) from the list
    // should NOT be the same as currentIndex
    private int getRandomSongIndex(int currentIndex) {
        int songIndex = currentIndex;
        int numSongsInPlaylist;
        numSongsInPlaylist = MusicContent.getNumSongsInPlaylist(getApplicationContext(), 0);
        Log.v(TAG, "getRandomSongIndex, numSongsInPlaylist="+numSongsInPlaylist);
        while(songIndex == currentIndex){
            songIndex=rand.nextInt(numSongsInPlaylist);
        }
        return songIndex;
    }

    public void fillPlayQueue() {
        int i = playQueue.size();
        for (; i< Settings.getPlayQueueSize(getApplicationContext()) ; i++) {
            int nextSongIndex;
            if (shuffleOn) {
                Log.d(TAG, "choosing random song");
                nextSongIndex = getRandomSongIndex(-1);

            } else {
                Log.d(TAG, "choosing next ordered song");
                currentPickedSong++;

                // get number of songs in the current playlist
                int numSongsInPlaylist;
                numSongsInPlaylist = MusicContent.getNumSongsInPlaylist(getApplicationContext(), 0);
                Log.v(TAG,  "num songs in playlist = "+numSongsInPlaylist);

                if (currentPickedSong >= numSongsInPlaylist) currentPickedSong = 0;

                // if (currentPickedSong >= songs.size()) currentPickedSong = 0;
                nextSongIndex = currentPickedSong;
            }
            Log.v(TAG, "nextSongIndex="+nextSongIndex);
            if (nextSongPID++ > 100) nextSongPID = 0; // PID for managing the playqueue (ot song ID or PID)

            Song nextSong = MusicContent.getSongInCurrentPlaylist(getApplicationContext(), nextSongIndex);
            //Song pqSong = new Song(songs.get(nextSongIndex), nextSongPID);
            Song pqSong = new Song(nextSong,nextSongPID);
            playQueue.add(pqSong);//songs.get(nextSongIndex)); // Adds at the END
        }
        // broadcast that the play queue has changed
        // can be used by the activity to update its playqueue
        Intent changeNextSongIntent = new Intent(AppConstants.INTENT_ACTION_PLAY_QUEUE_CHANGED);
        sendBroadcast(changeNextSongIntent);
    }

    public void removeSongFromPlayQueue(int songPID) {
        Log.d(TAG, "SimpleMusicService removeSongFromPlayQueue:id="+songPID+" size="+playQueue.size());
        for (Song s : playQueue) {
            Log.d(TAG, "s, id="+s.getID()+",PID="+s.getPID());
            if (s.getPID() == songPID) {
                Log.d(TAG, "found");
                playQueue.remove(s);
                fillPlayQueue();
                break;
            }
        }
    }

    public void moveThisSongToTopOfPlayQueue(int songPID) {
        Log.d(TAG, "SimpleMusicService moveThisSongToTopOfPlayQueue:id="+songPID);
        for (Song s : playQueue) {
            Log.d(TAG, "s, id="+s.getID()+",PID="+s.getPID());
            if (s.getPID() == songPID) {
                Log.d(TAG, "found");
                playQueue.remove(s);
                playQueue.add(0,s); // Add at the BEGINNING of the list
                fillPlayQueue();
                break;
            }
        }
    }

    public void insertThisSongAtTopOfPlayQueue(Song s) {
        playQueue.add(0,s); // Add at the BEGINNING of the list
    }

    public void insertThisSongIntoPlayQueue(Song s) {
        int playQueueIndex = 0;
        if (playQueue.size() > 0)
            playQueueIndex = rand.nextInt(playQueue.size());
        playQueue.add(playQueueIndex,s);
    }

    public void clearPlayQueue() {
        playQueue.clear();
    }

    // play button pressed in the activity start playing the song
    public void playNextSongInPlayQueue() {
        Log.d(TAG, "SimpleMusicService playNext");
        if (timeToGoToSleep()) {
            // if we have reached (passed) the sleep timer.
            // turn the sleep timer off and don't play any more songs
            sleepTime = null;
            return;
        }

        player.reset();

        //get a song
        /*
        if (this.songs == null || this.songs.size() < 1) {
            Log.d(TAG, "no songs to play");
            return;
        }
        */
        if (playQueue.size() < Settings.getPlayQueueSize(getApplicationContext())) {
            // nothing in the queue, so initialise
            fillPlayQueue();
        }
        // get the next song to play (from the queue)
        Song nextSong = playQueue.remove(); // retrieve and remove

        // add a song into the queue
        fillPlayQueue();
        playThisSongNow(nextSong);
    }

    public void playThisSongNow(Song songToPlay) {
        currentSong = songToPlay;
        long theSongId = songToPlay.getID();

        Log.d(TAG, "song="+songToPlay.getTitle()+" id="+ theSongId);
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, theSongId);
        try{
            player.setDataSource(getApplicationContext(), trackUri);
            Log.d(TAG, "song="+songToPlay.getTitle()+" id="+ trackUri);

        }
        catch(Exception e){
            Log.d("MUSIC SERVICE", "Error setting data source", e);
        }
        // prepare the player asynchronously and then the callback onPrepared will be called
        player.prepareAsync();
    }

    // stop button pressed in activity pause the player
    public void stopPlayback() {
        Log.d(TAG, "SimpleMusicService stopPlayback");
        //TODO: Cancel notification
        player.reset();
        currentState = STOPPED;
    }

    // pause or resume (depending on the current state)
    public void pauseOrResumePlayack() {
        Log.d(TAG, "SimpleMusicService pauseOrResumePlayack");

        switch (currentState) {
            case (STOPPED):
                playNextSongInPlayQueue();
                break;
            case (PLAYING):
                pausePlayback();
                break;
            case (PAUSED):
                resumePlaying();
                break;
        }
    }

    private void pausePlayback() {
        Log.d(TAG, "pausePlayback");
        if (currentState == PLAYING) {
            player.pause();
            currentPos = player.getCurrentPosition();
            currentState = PAUSED;
        }
    }

    public void resumePlaying() {
        Log.d(TAG, "resumePlaying");
        if (currentState == PAUSED) {
            player.seekTo(currentPos);
            player.start();
            currentState = PLAYING;
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.d(TAG,"Media plazer error: what="+what+", extra="+extra);
        mp.reset();
        return false;
    }

}