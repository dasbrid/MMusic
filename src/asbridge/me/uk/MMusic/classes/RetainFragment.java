package asbridge.me.uk.MMusic.classes;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import asbridge.me.uk.MMusic.services.SimpleMusicService;
import asbridge.me.uk.MMusic.utils.Content;

import java.util.ArrayList;

/**
 * Created by AsbridgeD on 18/12/2015.
 */
public class RetainFragment extends Fragment {

    private static final String TAG = "DAVE:RetainFragment";

    // public variables for accessing the service which are retained
    // during configuration changes
    public SimpleMusicService serviceReference;
    public boolean isBound;

    /**
     * Hold a reference to the parent Activity so we can report the
     * task's current progress and results. The Android framework
     * will pass us a reference to the newly created Activity after
     * each configuration change.
     */
    @Override
    public void onAttach(Activity activity) {
        Log.d(TAG, "onAttach");
        super.onAttach(activity);
    }

    /**
     * This method will only be called once when the retained
     * Fragment is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        // Retain this fragment across configuration changes.
        setRetainInstance(true);

    }

    /**
     * Set the callback to null so we don't accidentally leak the
     * Activity instance.
     */
    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach");
        super.onDetach();
    }


    public void doUnbindService() {
        Log.d(TAG, "doUnbindService");
        getActivity().getApplicationContext().unbindService(myConnection);
    }

    public  void doBindService() {
        Log.d(TAG, "doBindService");
        if (!isBound) {
            Log.d(TAG, "binding");
            Intent bindIntent = new Intent(getActivity(), SimpleMusicService.class);
            isBound = getActivity().getApplicationContext().bindService(bindIntent, myConnection, Context.BIND_AUTO_CREATE);
        }
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
            Content.getAllSongs(getActivity().getApplicationContext(), songList);
            serviceReference.setSongList(songList);
            serviceReference.fillPlayQueue();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
            serviceReference = null;
            isBound = false;
        }
    };

}