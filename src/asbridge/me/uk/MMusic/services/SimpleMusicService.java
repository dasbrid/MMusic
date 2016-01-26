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
    private Random rand;

    private LinkedList<Song> playQueue;
    private Queue<Song> playedList;
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

    // music sleeplistener fires when sleep timer is reached
    public interface OnMusicSleepListener {
        public void onMusicSleep();
    }
    private OnMusicSleepListener onMusicSleepListener = null;
    public void setOnMusicSleepListener (OnMusicSleepListener l) {
        onMusicSleepListener = l;
    }

    private IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    // broadcast receiver to kill audio when headphones unplugged
    private NoisyAudioStreamReceiver becomingNoisyListener;
    private class NoisyAudioStreamReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                Log.v(TAG, "NoisyAudioStreamReceiver:OnReceive"+intent.getAction());
                pausePlayback();
            }
        }
    }

    // broadcast receiver receives actions from notification
    private MusicControlListener musicControlListener;
    public class MusicControlListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
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
    }

    private Calendar sleepTime = null;

    public void setSleepTimer(int mins) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, mins);
        sleepTime = c;
    }

    public long getSecsTillSleep() {
        if (sleepTime == null) return -1;
        Calendar currentTime = Calendar.getInstance();
        long diff = sleepTime.getTimeInMillis() - currentTime.getTimeInMillis();
        long mins = diff / 1000;
        return mins;
    }

    public boolean isSleepTimerActive() {
        return (sleepTime != null);
    }

    public void cancelSleepTimer() {
        Log.v(TAG, "cancel sleep timer");
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

    // returns the current playing song
    public Song getCurrentSong() {
        return currentSong;
    }

    public ArrayList<Song> getPlayQueue() {
        return new ArrayList<Song> (playQueue);
    }

    public ArrayList<Song> getPlayedList() {

        if (playedList.size() > 0) {
            Song currSong = playedList.peek();
        }

        return new ArrayList<Song> (playedList);
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
        return START_STICKY; // Ensures that onStartCommand is called if the Service needs to restart after being killed by the system
    }

    public void onCreate(){
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
        playQueue = new LinkedList<>();
        playedList = new LinkedList<>();
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
        if(player.getCurrentPosition() > 0){
            mp.reset();
            playNextSongInPlayQueue(true); // true means check sleep timer
        }
    }

    private boolean timeToGoToSleep() {
        Log.v(TAG, "timeToGoToSleep (test) "+((sleepTime==null)?" null":sleepTime.getTimeInMillis()));
        if (sleepTime == null)
            return false; // no sleep timer set
        Calendar currentTime = Calendar.getInstance();
        if (currentTime.after(sleepTime))
            return true;
        Log.v(TAG, "timeToGoToSleep (test) - not time yet");
        return false;
    }

    // choose a random song (index) from the list
    // should NOT be the same as currentIndex
    private int getRandomSongIndex() {
        int numSongsInPlaylist;
        numSongsInPlaylist = MusicContent.getNumSongsInPlaylist(getApplicationContext(), 0);
        int songIndex=rand.nextInt(numSongsInPlaylist);
        return songIndex;
    }

    private boolean playqueueContainsSong(long songID) {
        for (Song s : playQueue)
        {
            if (s.getID() == songID)
                return true;
        }
        return false;
    }

    public void fillPlayQueue() {
        // get number of songs in the current playbucket
        int numSongsInBucket;
        numSongsInBucket = MusicContent.getNumSongsInPlaylist(getApplicationContext(), 0);
        if (numSongsInBucket == 0) {
            Log.v(TAG, "No songs in playbucket");
            return;
        }
        int numItemsInQueue = playQueue.size();
        int queueSize = Settings.getPlayQueueSize(getApplicationContext());
        Song nextSong;
        // repeat while ...
        // the playqueue is not full && there are still some songs left for us to choose

        while ( playQueue.size() < queueSize && (numSongsInBucket > playQueue.size() )) {
            int nextSongIndex;
            if (shuffleOn) {
                do {
                    nextSongIndex = getRandomSongIndex();
                    nextSong = MusicContent.getSongInCurrentPlaylist(getApplicationContext(), nextSongIndex);
                } while (playqueueContainsSong(nextSong.getID()));
            } else {
                currentPickedSong++;
                if (currentPickedSong >= numSongsInBucket) currentPickedSong = 0;
                nextSongIndex = currentPickedSong;
                nextSong = MusicContent.getSongInCurrentPlaylist(getApplicationContext(), nextSongIndex);
            }
            if (nextSongPID++ > 100) nextSongPID = 0; // PID for managing the playqueue (ot song ID or PID)

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
            if (s.getPID() == songPID) {
                playQueue.remove(s);
                fillPlayQueue();
                break;
            }
        }
    }

    public void moveThisSongToTopOfPlayQueue(int songPID) {
        Log.d(TAG, "SimpleMusicService moveThisSongToTopOfPlayQueue:id="+songPID);
        for (Song s : playQueue) {
            if (s.getPID() == songPID) {
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

    // overload of PNSIPQ which DOESN'T check the sleep timer
    public void playNextSongInPlayQueue() {
        playNextSongInPlayQueue(false);
    }

    // play button pressed in the activity or current song finished
    // get and play the next song
    // if check4sleep is true then check if the sleep timer is passed.
    // otherwise don't check (if called from play button fro example)
    public void playNextSongInPlayQueue(boolean check4sleep) {
        Log.d(TAG, "SimpleMusicService playNext");

        player.reset();

        if (playQueue.size() < Settings.getPlayQueueSize(getApplicationContext())) {
            // nothing in the queue, so initialise
            fillPlayQueue();
        }
        // get the next song to play (from the queue)
        Song nextSong = playQueue.remove(); // retrieve and remove

        // add a song into the queue
        fillPlayQueue();

        if (check4sleep && timeToGoToSleep()) {
            // if we have reached (passed) the sleep timer.
            // turn the sleep timer off and don't play any more songs
            Log.v(TAG, "time to sleep");
            stopPlayback();
            sleepTime = null;
            // call back to the activity to hide the sleep timer
            if (onMusicSleepListener != null) {
                onMusicSleepListener.onMusicSleep();
            }
            return;
        }
        playThisSongNow(nextSong);
    }

    // call this from outside
    public void playThisSong(Song songToPlay) {
        player.reset();
        playThisSongNow(songToPlay);
    }

    private void playThisSongNow(Song songToPlay) {
        Log.v(TAG, "playThisSongNow "+songToPlay.getTitle() + " id=" + songToPlay.getID());

        if (currentSong != null)
            playedList.add(currentSong);
        if (playedList.size() > AppConstants.PLAYEDQUEUE_SIZE) {
            playedList.remove();
        }

        currentSong = songToPlay;
        long theSongId = songToPlay.getID();

        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, theSongId);
        try{
            player.setDataSource(getApplicationContext(), trackUri);

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