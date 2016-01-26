package asbridge.me.uk.MMusic.GUIfragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import asbridge.me.uk.MMusic.R;
import asbridge.me.uk.MMusic.adapters.PlayedListAdapter;
import asbridge.me.uk.MMusic.classes.Song;

import java.util.ArrayList;

/**
 * Created by AsbridgeD on 22/12/2015.
 */
public class PlayedListFragment extends Fragment
    implements PlayedListAdapter.OnPlayedListClickedListener
{

    private String TAG = "DAVE: PlayedListFragment";

    private ListView lvPlayedList;
    private ArrayList<Song> playedList;
    private PlayedListAdapter playedListAdapter;

    // for call back to the activity
    private OnPlayedListClickedListener onPlayedListSongClickedListener;
    public interface OnPlayedListClickedListener {
        public void onPlayedListClicked(Song playedListSong);
    }
    public void setOnPlayedListClickedListener(OnPlayedListClickedListener l) {
        onPlayedListSongClickedListener = l;
    }

    public PlayedListFragment() {
        playedList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_playedlist, container, false);

        lvPlayedList = (ListView) v.findViewById(R.id.frag_lvPlayedQueue);
        playedListAdapter = new PlayedListAdapter(getContext(), this, playedList);

        lvPlayedList.setAdapter(playedListAdapter);
        return v;
    }

    @Override
    public void onPlayedListSongClicked(Song playedListSong) {
        if (onPlayedListSongClickedListener != null) {
            onPlayedListSongClickedListener.onPlayedListClicked(playedListSong);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void updatePlayedList(ArrayList<Song> newPlayedQueue) {
        playedList.clear();
        playedList.addAll(newPlayedQueue);
        if (newPlayedQueue.size() > 0) {
            Song currSong = newPlayedQueue.get(0);
        }
        playedListAdapter.notifyDataSetChanged();
    }

}
