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
public class PlayQueueFragment extends Fragment
    implements PlayQueueAdapter.PlayQueueActionsListener
{

    private String TAG = "DAVE: PlayQueueFragment";

    private ListView lvPlayQueue;

    private ArrayList<Song> playQueue;
    private PlayQueueAdapter playQueueAdapter;
    private int counter;

    private OnPlayQueueListener listener = null;
    public interface OnPlayQueueListener {
        void onRemoveSongClicked(Song s);;
        void onMoveSongToTopClicked(Song s);;
    }

    public PlayQueueFragment() {
        playQueue = new ArrayList<>();
    }
    public void setOnPlayQueueListener(OnPlayQueueListener l) {
        listener = l;
    }

        @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.fragment_playqueue, container, false);

        lvPlayQueue = (ListView) v.findViewById(R.id.frag_lvrearangablePlayQueue);
/* move to constr
        playQueue = new ArrayList<>();
*/
        playQueueAdapter = new PlayQueueAdapter(getContext(), this, playQueue);
            counter++;

        lvPlayQueue.setAdapter(playQueueAdapter);
        Log.d(TAG, "size "+ playQueue.size()+", counter="+counter);
        return v;
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume size "+ playQueue.size());
        //playQueue = new ArrayList<>();
        super.onResume();
    }

    public void updatePlayQueue(ArrayList<Song> newPlayQueue) {
        Log.d(TAG, "updatePlazQueue, counter="+ counter + "PlayQueue"+(playQueue==null?"null":"not null")+", npq "+(newPlayQueue==null?"null":"not null")
                +", paq "+(playQueueAdapter==null?"null":"not null"));
        playQueue.clear();
        playQueue.addAll(newPlayQueue);
        playQueueAdapter.notifyDataSetChanged();// This crashes when rotating screen
    }

    // callback from the playqueue adapter
    @Override
    public void onRemoveSongClicked(Song song) {
        Log.d(TAG, "onRemoveSong "+song.getTitle());
        if (playQueue != null && playQueue.size() > 0) {
            if (listener != null)
                listener.onRemoveSongClicked(song);
        }
    }

    // callback from the playqueue adapter
    @Override
    public void onMoveSongToTopClicked(Song song) {
        Log.d(TAG, "onMoveSongToTopClicked "+song.getTitle());
        if (playQueue != null && playQueue.size() > 0) {
            if (listener != null)
                listener.onMoveSongToTopClicked(song);
        }
    }
}
