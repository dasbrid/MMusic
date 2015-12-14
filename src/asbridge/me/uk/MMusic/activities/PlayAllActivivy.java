package asbridge.me.uk.MMusic.activities;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import asbridge.me.uk.MMusic.R;
import asbridge.me.uk.MMusic.classes.Song;
import asbridge.me.uk.MMusic.services.SimpleMusicService;
import asbridge.me.uk.MMusic.utils.Content;

import java.util.ArrayList;

/**
 * Created by David on 13/12/2015.
 http://www.101apps.co.za/index.php/articles/binding-to-a-service-a-tutorial.html
 */
public class PlayAllActivivy extends Activity {

    private String TAG = "DAVE:PlayAllActivivy";
    private boolean musicBound;
    private ArrayList<Song> songList;
    private SimpleMusicService musicSrv;

    ///////
    private boolean isBound;
    private SimpleMusicService serviceReference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_play_all);

        Intent playIntent = new Intent(this, SimpleMusicService.class);
        startService(playIntent);
        sendNotification();
    }

    //start the Service instance when the Activity instance starts
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        doBindService();
    }

    /* if we include this then closing the activity unbinds and stops the service ...
    ... not what we want
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        Log.d(TAG, "unBindService");
        Intent bindIntent = new Intent(this, SimpleMusicService.class);
        unbindService(myConnection);
        isBound = false;
    }
*/
    // Don't stop the playback when the backbutton is pressed
    @Override
    public void onBackPressed() {
        Log.d(TAG, "onbackpressed");
        moveTaskToBack(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (isFinishing()) {
            Log.d(TAG, "isFinishing");
            // genuinly finishing, not orientation change etc
            Intent intentStopService = new Intent (this, SimpleMusicService.class);
            stopService(intentStopService);
            unbindService(myConnection); ///???
        }
    }

    public void btnPlayClicked(View v) {
        Log.d(TAG, "btnPlayClicked");
        if (isBound)
            serviceReference.startPlay();
    }

    public void btnPauseClicked(View v) {
        Log.d(TAG, "btnPauseClicked");
        if (isBound)
            serviceReference.stopPlay();

    }

    private void sendNotification() {
        Intent startIntent = new Intent(this, PlayAllActivivy.class);
        startIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                startIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        /*
        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.play)
                .setTicker("Music Playing(ish)")
                .setOngoing(true)
                .setContentTitle("Service Running")
                .setContentText("not actually playing");
        Notification notification = builder.build();

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(-34, notification);
        */
    }

    //connect to the service
    private ServiceConnection myConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "Service Connected");
            SimpleMusicService.SimpleMusicBinder binder = (SimpleMusicService.SimpleMusicBinder)service;
            //get service
            serviceReference = binder.getService();

            // set the list of songs in the service
            ArrayList<Song> songList = new ArrayList<>();
            Content.getAllSongs(getApplicationContext(), songList);
            serviceReference.setSongList(songList);

            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
            serviceReference = null;
            isBound = false;
        }
    };



    private void doBindService() {
        Log.d(TAG, "BindService");
        if (!isBound) {
            Log.d(TAG, "binding");
            Intent bindIntent = new Intent(this, SimpleMusicService.class);
            isBound = bindService(bindIntent, myConnection, Context.BIND_AUTO_CREATE);
        }
    }
/*
    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            SimpleMusicService.SimpleMusicBinder binder = (SimpleMusicService.SimpleMusicBinder)service;
            //get service
            musicSrv = binder.getService();
            //pass list
            //musicSrv.setList(songList);

            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
            musicBound = false;
        }
    };

    */
/*

*/
}