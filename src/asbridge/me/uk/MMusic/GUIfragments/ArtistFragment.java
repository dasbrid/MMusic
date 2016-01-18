package asbridge.me.uk.MMusic.GUIfragments;

import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.*;
import android.widget.*;
import asbridge.me.uk.MMusic.R;
import asbridge.me.uk.MMusic.adapters.ArtistGroupAdapter;
import asbridge.me.uk.MMusic.classes.ArtistGroup;
import asbridge.me.uk.MMusic.classes.Song;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.Fragment;
/**
 * Created by AsbridgeD on 08/12/2015.
 */
public class ArtistFragment extends Fragment implements
        View.OnClickListener
{

    private String TAG = "DAVE: ArtistFragment";

    private CheckBox cbCheckAll;
    private SparseArray<ArtistGroup> artistGroups;
    ExpandableListView elvArtistGroupList;
    ArtistGroupAdapter artistGroupAdapter;

    private OnSongsChangedListener listener = null;
    public interface OnSongsChangedListener {
        void onSongsChanged();
        void playThisSongNext(Song s);
        void addThisSongToPlayQueue(Song s);
        void playThisSongNow(Song s);
        void addArtistsSongsToPlayQueue(ArtistGroup ag);
        void clearPlayQueueAndaddArtistsSongsToPlayQueue(ArtistGroup ag);
    }

    public void setOnSongsChangedListener(OnSongsChangedListener l) {
        listener = l;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnArtist:
                changeArtist();
                break;
            case R.id.btnSongsSelectAll:
                selectAllorNone(true);
                break;
            case R.id.btnSongsSelectNone:
                selectAllorNone(false);
                break;
        }
    }

    private void selectAllorNone(boolean newState) {
        for(int i = 0; i < artistGroups.size(); i++) {
            int key = artistGroups.keyAt(i);
            // get the object by the key.
            ArtistGroup ag = artistGroups.get(key);
            ag.changeStateofAllSongs(newState);
        }
        artistGroupAdapter.notifyDataSetChanged();
    }
    // the changeartist button is clicked
    // Update the services list of artists
    public void changeArtist() {
        Log.d(TAG, "changeArtist");
        if (listener != null)
            listener.onSongsChanged();
    }

    public void setSongList(ArrayList<Song> songs) {
        Log.d(TAG, "setSongList "+songs.size());

        int i=0;
        ArtistGroup newGroup = null;
        for (Song s : songs) {
            if (newGroup == null || !newGroup.artistName.equals(s.getArtist())) {
                newGroup = new ArtistGroup(s.getArtist());
                artistGroups.append(i++, newGroup);
            }
            newGroup.songs.add(new ArtistGroup.SelectedSong(s, true));
        }
        artistGroupAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        Log.d(TAG, "onCreateContextMenu v="+v.toString()+","+v.getId());
        ExpandableListView.ExpandableListContextMenuInfo elvcmi = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
        int type = ExpandableListView.getPackedPositionType(elvcmi.packedPosition);
        MenuInflater menuInflater = getActivity().getMenuInflater();
        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            menuInflater.inflate(R.menu.menu_song_long_click, menu);
        } else {
            menuInflater.inflate(R.menu.menu_artist_long_click, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        ExpandableListView.ExpandableListContextMenuInfo menuInfo = (ExpandableListView.ExpandableListContextMenuInfo)item.getMenuInfo();
        int type = ExpandableListView.getPackedPositionType(menuInfo.packedPosition);
        int groupPos = -1;
        int childPos = -1;
        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            groupPos = ExpandableListView.getPackedPositionGroup(menuInfo.packedPosition);
            childPos = ExpandableListView.getPackedPositionChild(menuInfo.packedPosition);
            Log.d(TAG, ": Child " + childPos + " clicked in group " + groupPos);

            int key = artistGroups.keyAt(groupPos);
            // get the object by the key.
            ArtistGroup ag = artistGroups.get(key);
            Song s = ag.songs.get(childPos).song;

            switch (item.getItemId()) {
                case R.id.menu_song_long_click_playnext:
                    Log.d(TAG, "menu_song_long_click_playnext");
                    listener.playThisSongNext(s);
                    return true;
                case R.id.menu_song_long_click_addtoqueue:
                    Log.d(TAG, "menu_song_long_click_addtoqueue");
                    listener.addThisSongToPlayQueue(s);
                    return true;
                case R.id.menu_song_long_click_playnow:
                    Log.d(TAG, "menu_song_long_click_playnow");
                    listener.playThisSongNow(s);
                    return true;
            }

        } else if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
            groupPos = ExpandableListView.getPackedPositionGroup(menuInfo.packedPosition);
            Log.d(TAG, ": Group " + groupPos + " clicked");

            int key = artistGroups.keyAt(groupPos);
            // get the object by the key.
            ArtistGroup ag = artistGroups.get(key);

            switch (item.getItemId()) {
                case R.id.menu_artist_long_click_addsongstoqueue:
                    Log.d(TAG, "menu_song_long_click_addtoqueue");
                    listener.addArtistsSongsToPlayQueue(ag);
                    return true;
            }

            switch (item.getItemId()) {
                case R.id.menu_artist_long_click_clearandaddsongstoqueue:
                    Log.d(TAG, "menu_song_long_click_addtoqueue");
                    listener.clearPlayQueueAndaddArtistsSongsToPlayQueue(ag);
                    return true;
            }
        }


        return super.onContextItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.fragment_artist, container, false);
        Button btnArtist = (Button) v.findViewById(R.id.btnArtist);
        btnArtist.setOnClickListener(this);
        Button btnSongsSelectAll = (Button) v.findViewById(R.id.btnSongsSelectAll);
        btnSongsSelectAll.setOnClickListener(this);
        Button btnSongsSelectNone = (Button) v.findViewById(R.id.btnSongsSelectNone);
        btnSongsSelectNone.setOnClickListener(this);

        elvArtistGroupList = (ExpandableListView) v.findViewById(R.id.lvSongsByArtist);

        artistGroups = new SparseArray<>();

        artistGroupAdapter = new ArtistGroupAdapter(getActivity(), artistGroups);
        elvArtistGroupList.setAdapter(artistGroupAdapter);
        registerForContextMenu(elvArtistGroupList);

        return v;
    }

    public ArrayList<Song> getSelectedSongs() {
        ArrayList<Song> selectedSongs = new ArrayList<>();
        for(int i = 0; i < artistGroups.size(); i++) {
            int key = artistGroups.keyAt(i);
            // get the object by the key.
            ArtistGroup ag = artistGroups.get(key);
            {
                List<ArtistGroup.SelectedSong> songs = ag.songs;
                for (ArtistGroup.SelectedSong ss : songs) {
                    if (ss.selected) {
                        selectedSongs.add(ss.song);
                    }
                }
            }
        }

        return selectedSongs;
    }
}