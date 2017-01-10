package asbridge.me.uk.MMusic.GUIfragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;
import asbridge.me.uk.MMusic.R;
import asbridge.me.uk.MMusic.activities.SongListtActivity;
import asbridge.me.uk.MMusic.adapters.PlayQueueAdapter;
import asbridge.me.uk.MMusic.classes.Song;
import asbridge.me.uk.MMusic.utils.AppConstants;

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
        playQueueAdapter = new PlayQueueAdapter(getContext(), this, playQueue);
        lvPlayQueue.setAdapter(playQueueAdapter);
        registerForContextMenu(lvPlayQueue);

        return v;
    }

    // Context menu for long click on play queue song
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.menu_context_view_songs_by, menu);
    }

    // Context menu for long click on play queue song
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info=
                (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int postion = (info.position);
        Song s = (Song)playQueueAdapter.getItem(postion);
        Intent intent = new Intent(getActivity(), SongListtActivity.class);

        switch(item.getItemId()) {

            case R.id.context_menu_item_artist:
                intent.putExtra(AppConstants.INTENT_EXTRA_NAME, s.getArtist());
                intent.putExtra(AppConstants.INTENT_EXTRA_TYPE, AppConstants.INTENT_EXTRA_VALUE_ARTIST);
                startActivity(intent);
                return true;
            case R.id.context_menu_item_album:
                intent.putExtra(AppConstants.INTENT_EXTRA_NAME, s.getAlbum());
                intent.putExtra(AppConstants.INTENT_EXTRA_TYPE, AppConstants.INTENT_EXTRA_VALUE_ALBUM);
                startActivity(intent);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume size "+ playQueue.size());
        super.onResume();
    }

    public void updatePlayQueue(ArrayList<Song> newPlayQueue) {
        playQueue.clear();
        playQueue.addAll(newPlayQueue);
        playQueueAdapter.notifyDataSetChanged();
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
