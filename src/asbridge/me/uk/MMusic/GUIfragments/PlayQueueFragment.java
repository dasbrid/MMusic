package asbridge.me.uk.MMusic.GUIfragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;
import asbridge.me.uk.MMusic.R;
import asbridge.me.uk.MMusic.adapters.ArtistGroupAdapter;
import asbridge.me.uk.MMusic.adapters.PlayQueueAdapter;
import asbridge.me.uk.MMusic.classes.RetainFragment;
import asbridge.me.uk.MMusic.classes.Song;
import asbridge.me.uk.MMusic.services.SimpleMusicService;

import java.util.ArrayList;

/**
 * Created by AsbridgeD on 22/12/2015.
 */
public class PlayQueueFragment extends Fragment {

    private String TAG = "DAVE: PlayQueueFragment";

    private TextView tvNowPlaying;

    private ListView lvPlayQueue;

    private ArrayList<Song> playQueue;
    private PlayQueueAdapter playQueueAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.fragment_playqueue, container, false);

        tvNowPlaying = (TextView) v.findViewById(R.id.pqa_tvPlaying);

        lvPlayQueue = (ListView) v.findViewById(R.id.frag_lvrearangablePlayQueue);

        playQueue = new ArrayList<>();
        playQueueAdapter = new PlayQueueAdapter(getActivity(), playQueue);

        lvPlayQueue.setAdapter(playQueueAdapter);

        /* don't think we need to do this ??
        Log.d(TAG, "starting the service");
        Intent playIntent = new Intent(this, SimpleMusicService.class);
        startService(playIntent);
        */

        return v;
    }

    public void updatePlayQueue(ArrayList<Song> newPlayQueue) {
        Log.d(TAG, "updatePlayQueue");
        playQueue.clear();
        playQueue.addAll(newPlayQueue);
        playQueueAdapter.notifyDataSetChanged();
    }

}
