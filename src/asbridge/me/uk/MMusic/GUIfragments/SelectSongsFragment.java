package asbridge.me.uk.MMusic.GUIfragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.SparseArray;
import android.view.*;
import android.widget.*;
import asbridge.me.uk.MMusic.R;
import asbridge.me.uk.MMusic.adapters.GroupAdapter;
import asbridge.me.uk.MMusic.classes.SelectedSong;
import asbridge.me.uk.MMusic.classes.SongGroup;
import asbridge.me.uk.MMusic.classes.Song;

import java.util.*;

import android.support.v4.app.Fragment;
import asbridge.me.uk.MMusic.controls.TriStateButton;
import asbridge.me.uk.MMusic.utils.MusicContent;
import asbridge.me.uk.MMusic.utils.Settings;

/**
 * Created by AsbridgeD on 08/12/2015.
 */
public class SelectSongsFragment extends Fragment implements
        View.OnClickListener,
        GroupAdapter.OnSelectionStateChangedListener
{

    private String TAG = "SelectSongsFragment";
    Button btnGroupByAlbum;
    Button btnGroupByArtist;
    private static final int GROUPBY_ARTIST = 0;
    private static final int GROUPBY_ALBUM = 1;
    private int groupby;

    private SparseArray<SongGroup> artistGroups;
    private ArrayList<Song> songs = new ArrayList<>() ;

    private ExpandableListView elvArtistGroupList;
    private GroupAdapter groupAdapter;
    private TriStateButton btnSongsSelect;

    // Use instance field for listener
// It will not be gc'd as long as this instance is kept referenced
    SharedPreferences.OnSharedPreferenceChangeListener prefslistener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            changeGroupBy(groupby); // NOT ACTUALLY CHANGING, JUST UPDATE THE LIST
        }
    };


    private OnSongsChangedListener listener = null;
    public interface OnSongsChangedListener {
        void onSongsChanged();
        void playThisSongNext(Song s);
        void addThisSongToPlayQueue(Song s);
        void playThisSongNow(Song s);
        void addArtistsSongsToPlayQueue(SongGroup ag);
        void clearPlayQueueAndaddArtistsSongsToPlayQueue(SongGroup ag);
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
        setListViewContentsGrouped(selectedSongs);
    }

    private void changeGroupBy(int newGroupBy) {
        groupby = newGroupBy;
        setListViewContentsGrouped(getSelectedSongIDs());
    }

    // The songs are loadad and we have the selected songs.
    // Group the songs into either artists or albums, depending on the groups
    private void setListViewContentsGrouped(ArrayList<Long> selectedSongs) {
        HashMap<String, SongGroup> groupMap = new HashMap<>();
        SongGroup group = null;
        artistGroups.clear();
        int i=0;
        for (Song s : songs) {
            if (s.getDuration() > Settings.getMinDurationInSeconds(getContext()) * 1000) {
                if (groupMap.containsKey(groupby == GROUPBY_ALBUM ? s.getAlbum() : s.getArtist())) {
                    group = groupMap.get(groupby == GROUPBY_ALBUM ? s.getAlbum() : s.getArtist());
                    if (groupby == GROUPBY_ALBUM) {
                        if (!s.getArtist().equals(group.groupDetail)) {
                            group.groupDetail = "various artists";
                        }
                    }
                } else {
                    group = new SongGroup(groupby == GROUPBY_ALBUM ? s.getAlbum() : s.getArtist(), groupby == GROUPBY_ALBUM ? s.getArtist() : null);
                    artistGroups.append(i++, group);
                    groupMap.put(groupby == GROUPBY_ALBUM ? s.getAlbum() : s.getArtist(), group);
                }
                group.songs.add(new SelectedSong(s, selectedSongs.contains(s.getID()), groupby == GROUPBY_ALBUM ? s.getArtist() : s.getAlbum()));
            }
        }
        btnGroupByAlbum.setEnabled(groupby!=GROUPBY_ALBUM);
        btnGroupByArtist.setEnabled(groupby!=GROUPBY_ARTIST);
        groupAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSongsSelect:
                selectSongs();
                break;
            case R.id.btnGroupByArtist:
                changeGroupBy(GROUPBY_ARTIST);
                break;
            case R.id.btnGroupByAlbum:
                changeGroupBy(GROUPBY_ALBUM);
                break;
        }
    }

    // The select (all/none) button has been pressed
    private void selectSongs() {
        int currentState = groupAdapter.getSelectionState();
        if (currentState == 2) {
            btnSongsSelect.setState(0);
            groupAdapter.selectAllorNone(false);
        } else {
            btnSongsSelect.setState(2);
            groupAdapter.selectAllorNone(true);
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
            SongGroup ag = artistGroups.get(key);
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
            SongGroup ag = artistGroups.get(key);

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

        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.registerOnSharedPreferenceChangeListener(prefslistener);

        View v = inflater.inflate(R.layout.fragment_select_songs, container, false);

        btnSongsSelect = (TriStateButton) v.findViewById(R.id.btnSongsSelect);
        btnSongsSelect.setOnClickListener(this);

        btnGroupByAlbum = (Button) v.findViewById(R.id.btnGroupByAlbum);
        btnGroupByAlbum.setOnClickListener(this);
        btnGroupByArtist = (Button) v.findViewById(R.id.btnGroupByArtist);
        btnGroupByArtist.setOnClickListener(this);

        elvArtistGroupList = (ExpandableListView) v.findViewById(R.id.lvSongsByArtist);

        artistGroups = new SparseArray<>();

        groupAdapter = new GroupAdapter(getActivity(), artistGroups);
        elvArtistGroupList.setAdapter(groupAdapter);
        registerForContextMenu(elvArtistGroupList);
        groupAdapter.setOnSelectionStateChangedListener(this);

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
            setListViewContentsGrouped(selectedSongIDs);
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
            SongGroup ag = artistGroups.get(key);
            {
                List<SelectedSong> songs = ag.songs;
                for (SelectedSong ss : songs) {
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
            SongGroup ag = artistGroups.get(key);
            {
                List<SelectedSong> songs = ag.songs;
                for (SelectedSong ss : songs) {
                    if (ss.selected) {
                        selectedSongs.add(ss.song);
                    }
                }
            }
        }
        return selectedSongs;
    }
}