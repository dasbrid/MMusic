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
    Button btnGroupBySong;
    ImageButton btnSearchSongs;

    private static final int GROUPBY_ARTIST = 0;
    private static final int GROUPBY_ALBUM = 1;
    private static final int GROUPBY_SONG = 2;
    private int groupby;

    private SparseArray<SongGroup> artistGroups;
    private ArrayList<Song> songs = new ArrayList<>() ;

    private ExpandableListView elvGroupList;
    private GroupAdapter groupAdapter;
    private TriStateButton btnSongsSelect;

    private String filterString;

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
    private static final String STATE_FILTERSTRING = "FILTERSTRING";


    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Make sure to call the super method so that the states of our views are saved
        super.onSaveInstanceState(outState);
        // Save our own state now
        outState.putInt(STATE_GROUPBY, groupby);
        outState.putParcelableArrayList(STATE_ALLSONGS, songs);
        outState.putParcelableArrayList(STATE_SELECTEDSONGS, getSelectedSongs());
        outState.putString(STATE_FILTERSTRING, filterString);
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

    private void filterSongs() {
        if (filterString == null) {
            filterString = "the";
            btnSearchSongs.setImageResource(R.drawable.ic_search_off);
        } else {
            filterString = null; // no filter
            btnSearchSongs.setImageResource(R.drawable.ic_search);

        }
        setListViewContentsGrouped(getSelectedSongIDs());
    }

    private boolean songMatchesFilterCriteria(Song s) {
        String searchStringUpperCase = filterString.toUpperCase();
        if (s.getTitle().toUpperCase().contains(searchStringUpperCase))
            return true;
        if (s.getArtist().toUpperCase().contains(searchStringUpperCase))
            return true;
        if (s.getAlbum().toUpperCase().contains(searchStringUpperCase))
            return true;

        return false;
    }

    Comparator<Song> getComparator() {
        switch (groupby) {
            case GROUPBY_ARTIST:
                return Song.SongArtistComparator;
            case GROUPBY_ALBUM:
                return Song.SongAlbumComparator;
            case GROUPBY_SONG:
                return Song.SongTitleComparator;
            default:
                return null;
        }
    }

    // The songs are loadad and we have the selected songs.
    // Group the songs into either artists or albums, depending on the groups
    private void setListViewContentsGrouped(ArrayList<Long> selectedSongs) {
        HashMap<String, SongGroup> groupMap = new HashMap<>();
        SongGroup group = null;
        artistGroups.clear();
        int i=0;
        Collections.sort(songs, getComparator());
        for (Song s : songs) {
            if (s.getDuration() > Settings.getMinDurationInSeconds(getContext()) * 1000) {
                if (filterString == null || songMatchesFilterCriteria(s)) {
                    if (groupMap.containsKey(groupby == GROUPBY_ALBUM ? s.getAlbum() : (groupby == GROUPBY_ARTIST ? s.getArtist() : s.getTitle().substring(0, 1)))) {
                        // We already have a group with this key
                        group = groupMap.get(groupby == GROUPBY_ALBUM ? s.getAlbum() : (groupby == GROUPBY_ARTIST ? s.getArtist() : s.getTitle().substring(0, 1)));
                        if (groupby == GROUPBY_ALBUM) {
                            if (!s.getArtist().equals(group.groupDetail)) {
                                group.groupDetail = "various artists";
                            }
                        }
                    } else {
                        // New key, make new group
                        group = new SongGroup(groupby == GROUPBY_ALBUM ? s.getAlbum() : (groupby == GROUPBY_ARTIST ? s.getArtist() : s.getTitle().substring(0, 1)), groupby == GROUPBY_ALBUM ? s.getArtist() : null);
                        artistGroups.append(i++, group);
                        groupMap.put(groupby == GROUPBY_ALBUM ? s.getAlbum() : (groupby == GROUPBY_ARTIST ? s.getArtist() : s.getTitle().substring(0, 1)), group);
                    }
                    group.songs.add(new SelectedSong(s, selectedSongs.contains(s.getID()), groupby == GROUPBY_ALBUM ? s.getArtist() : (groupby == GROUPBY_ARTIST ? s.getAlbum() : s.getTitle())));
                }
            }
        }
        btnGroupByAlbum.setEnabled(groupby!=GROUPBY_ALBUM);
        btnGroupByArtist.setEnabled(groupby!=GROUPBY_ARTIST);
        btnGroupBySong.setEnabled(groupby!=GROUPBY_SONG);
        groupAdapter.notifyDataSetChanged();
        for (int g=0 ; g < groupAdapter.getGroupCount() ; g++)
        {
            elvGroupList.collapseGroup(g);
        }
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
            case R.id.btnGroupBySong:
                changeGroupBy(GROUPBY_SONG);
                break;
            case R.id.btnSearchSongs:
                filterSongs();
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
        btnGroupBySong = (Button) v.findViewById(R.id.btnGroupBySong);
        btnGroupBySong.setOnClickListener(this);
        btnSearchSongs = (ImageButton) v.findViewById(R.id.btnSearchSongs);
        btnSearchSongs.setOnClickListener(this);

        elvGroupList = (ExpandableListView) v.findViewById(R.id.lvSongsByArtist);

        artistGroups = new SparseArray<>();

        groupAdapter = new GroupAdapter(getActivity(), artistGroups);
        elvGroupList.setAdapter(groupAdapter);
        registerForContextMenu(elvGroupList);
        groupAdapter.setOnSelectionStateChangedListener(this);

        // if we have a saved instance then we are returning (e.g. rotate)
        // get the list of songs and the list of selected songs from the saved instance
        if (savedInstanceState != null) {
            // Restore last state for checked position.
            songs = savedInstanceState.getParcelableArrayList(STATE_ALLSONGS);
            groupby = savedInstanceState.getInt(STATE_GROUPBY);
            ArrayList<Song> selectedSongs = savedInstanceState.getParcelableArrayList(STATE_SELECTEDSONGS);
            filterString = savedInstanceState.getString(STATE_FILTERSTRING);
            if (filterString == null) {
                btnSearchSongs.setImageResource(R.drawable.ic_search);
            } else {
                btnSearchSongs.setImageResource(R.drawable.ic_search_off);

            }
            ArrayList<Long> selectedSongIDs = new ArrayList<>();
            for (Song s : selectedSongs) {
                selectedSongIDs.add(s.getID());
            }
            setListViewContentsGrouped(selectedSongIDs);
        } else {
            // if there is no saved instance then we will get songs from the device content provider
            filterString = null;
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