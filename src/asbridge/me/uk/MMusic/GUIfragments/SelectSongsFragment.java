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
import asbridge.me.uk.MMusic.controls.TriStateButton;
import asbridge.me.uk.MMusic.utils.MusicContent;

/**
 * Created by AsbridgeD on 08/12/2015.
 */
public class SelectSongsFragment extends Fragment implements
        View.OnClickListener,
        ArtistGroupAdapter.OnSelectionStateChangedListener
{

    private String TAG = "SelectSongsFragment";

    private static final int GROUPBY_ARTIST = 0;
    private static final int GROUPBY_ALBUM = 1;
    private int groupby;

    private SparseArray<ArtistGroup> artistGroups;
    private ArrayList<Song> songs = new ArrayList<>() ;

    private ExpandableListView elvArtistGroupList;
    private ArtistGroupAdapter artistGroupAdapter;
    private TriStateButton btnSongsSelect;

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

    private static final String STATE_ALLSONGS = "ALLSONGS";
    private static final String STATE_SELECTEDSONGS = "SELECTEDSONGS";
    private static final String STATE_GROUPBY = "GROUPBY";

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Make sure to call the super method so that the states of our views are saved
        super.onSaveInstanceState(outState);
        // Save our own state now
        outState.putInt(STATE_GROUPBY, groupby);
        outState.putParcelableArrayList(STATE_ALLSONGS, songs);
        outState.putParcelableArrayList(STATE_SELECTEDSONGS, getSelectedSongs());
    }

    public void setSongList() {
        // This displays ALL songs on the device
        songs = new ArrayList<>();
        MusicContent.getAllSongsGroupedByArtist(getContext(), songs);
        // Songs are set selected (ticked) based on the current playlist
        ArrayList<Long> selectedSongs = MusicContent.getSongsInPlaylist(getContext(), 0);
        setListViewContentsGrouped(/*songs, */selectedSongs);
    }

    private void changeGroupBy() {
        if (groupby == GROUPBY_ALBUM)
            groupby = GROUPBY_ARTIST;
        else
            groupby = GROUPBY_ALBUM;

        setListViewContentsGrouped(getSelectedSongIDs());
    }

    private void setListViewContentsGrouped(/*ArrayList<Song> songs, */ArrayList<Long> selectedSongs) {
        int i=0;
        ArtistGroup newGroup = null;
        for (Song s : songs) {
            if (newGroup == null
                    || (groupby == GROUPBY_ALBUM && !newGroup.artistName.equals(s.getAlbum()))
                    || (groupby == GROUPBY_ARTIST && !newGroup.artistName.equals(s.getArtist()))
            ) {
                newGroup = new ArtistGroup(groupby == GROUPBY_ALBUM? s.getAlbum():s.getArtist());
                artistGroups.append(i++, newGroup);
            }
            newGroup.songs.add(new ArtistGroup.SelectedSong(s, selectedSongs.contains(s.getID())));

        }
        artistGroupAdapter.notifyDataSetChanged();
    }

    private void setListViewContentsGroupedByAlbum(/*ArrayList<Song> songs, */ArrayList<Long> selectedSongs) {
        int i=0;
        ArtistGroup newGroup = null;
        for (Song s : songs) {

            if (newGroup == null || !newGroup.artistName.equals(s.getAlbum())) {
                newGroup = new ArtistGroup(s.getAlbum());
                artistGroups.append(i++, newGroup);
            }
            newGroup.songs.add(new ArtistGroup.SelectedSong(s, selectedSongs.contains(s.getID())));

        }
        artistGroupAdapter.notifyDataSetChanged();
    }

    private void setListViewContentsGroupedByArtist(/*ArrayList<Song> songs, */ArrayList<Long> selectedSongs) {
        int i=0;
        ArtistGroup newGroup = null;
        for (Song s : songs) {

            if (newGroup == null || !newGroup.artistName.equals(s.getArtist())) {
                newGroup = new ArtistGroup(s.getArtist());
                artistGroups.append(i++, newGroup);
            }
            newGroup.songs.add(new ArtistGroup.SelectedSong(s, selectedSongs.contains(s.getID())));

        }
        artistGroupAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSongsSelect:
                selectSongs();
                break;
            case R.id.btnGroupBy:
                changeGroupBy();
                break;
        }
    }

    // The select (all/none) button has been pressed
    private void selectSongs() {
        int currentState = artistGroupAdapter.getSelectionState();
        if (currentState == 2) {
            btnSongsSelect.setState(0);
            artistGroupAdapter.selectAllorNone(false);
        } else {
            btnSongsSelect.setState(2);
            artistGroupAdapter.selectAllorNone(true);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
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
        int groupPos;
        int childPos;
        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            groupPos = ExpandableListView.getPackedPositionGroup(menuInfo.packedPosition);
            childPos = ExpandableListView.getPackedPositionChild(menuInfo.packedPosition);

            int key = artistGroups.keyAt(groupPos);
            // get the object by the key.
            ArtistGroup ag = artistGroups.get(key);
            Song s = ag.songs.get(childPos).song;

            switch (item.getItemId()) {
                case R.id.menu_song_long_click_playnext:
                    listener.playThisSongNext(s);
                    return true;
                case R.id.menu_song_long_click_addtoqueue:
                    listener.addThisSongToPlayQueue(s);
                    return true;
                case R.id.menu_song_long_click_playnow:
                    listener.playThisSongNow(s);
                    return true;
            }

        } else if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
            groupPos = ExpandableListView.getPackedPositionGroup(menuInfo.packedPosition);

            int key = artistGroups.keyAt(groupPos);
            // get the object by the key.
            ArtistGroup ag = artistGroups.get(key);

            switch (item.getItemId()) {
                case R.id.menu_artist_long_click_addsongstoqueue:
                    listener.addArtistsSongsToPlayQueue(ag);
                    return true;
            }

            switch (item.getItemId()) {
                case R.id.menu_artist_long_click_clearandaddsongstoqueue:
                    listener.clearPlayQueueAndaddArtistsSongsToPlayQueue(ag);
                    return true;
            }
        }
        return super.onContextItemSelected(item);
    }

    // an Event from the List Adapter
    // A song has been selected or deselcted and as a result the selected state has changed
    @Override
    public void onSelectionStateChanged(int state) {
        btnSongsSelect.setState(state);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_artist, container, false);

        btnSongsSelect = (TriStateButton) v.findViewById(R.id.btnSongsSelect);
        btnSongsSelect.setOnClickListener(this);

        Button btnGroupBy = (Button) v.findViewById(R.id.btnGroupBy);
        btnGroupBy.setOnClickListener(this);

        elvArtistGroupList = (ExpandableListView) v.findViewById(R.id.lvSongsByArtist);

        artistGroups = new SparseArray<>();

        artistGroupAdapter = new ArtistGroupAdapter(getActivity(), artistGroups);
        elvArtistGroupList.setAdapter(artistGroupAdapter);
        registerForContextMenu(elvArtistGroupList);
        artistGroupAdapter.setOnSelectionStateChangedListener(this);

        // if we have a saved instance then we are returning (e.g. rotate)
        // get the list of songs and the list of selected songs from the saved instance
        if (savedInstanceState != null) {
            // Restore last state for checked position.
            songs = savedInstanceState.getParcelableArrayList(STATE_ALLSONGS);
            groupby = savedInstanceState.getInt(STATE_GROUPBY);
            ArrayList<Song> selectedSongs = savedInstanceState.getParcelableArrayList(STATE_SELECTEDSONGS);
            ArrayList<Long> selectedSongIDs = new ArrayList<>();
            for (Song s : selectedSongs) {
                selectedSongIDs.add(s.getID());
            }
            setListViewContentsGroupedByArtist(/*songs, */selectedSongIDs);
        } else {
            // if there is no saved instance then we will get songs from the device content provider
            groupby = GROUPBY_ALBUM;
            setSongList();
        }
        return v;
    }

    public ArrayList<Long> getSelectedSongIDs() {
        ArrayList<Long> selectedSongIDs = new ArrayList<>();
        for(int i = 0; i < artistGroups.size(); i++) {
            int key = artistGroups.keyAt(i);
            // get the object by the key.
            ArtistGroup ag = artistGroups.get(key);
            {
                List<ArtistGroup.SelectedSong> songs = ag.songs;
                for (ArtistGroup.SelectedSong ss : songs) {
                    if (ss.selected) {
                        selectedSongIDs.add(ss.song.getID());
                    }
                }
            }
        }
        return selectedSongIDs;
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