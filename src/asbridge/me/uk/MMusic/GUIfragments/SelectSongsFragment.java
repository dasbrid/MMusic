package asbridge.me.uk.MMusic.GUIfragments;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v4.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
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
import asbridge.me.uk.MMusic.dialogs.SearchSongsDialog;
import asbridge.me.uk.MMusic.utils.MusicContent;
import asbridge.me.uk.MMusic.utils.Settings;

/**
 * Created by AsbridgeD on 08/12/2015.
 */
public class SelectSongsFragment extends Fragment implements
        View.OnClickListener,
        GroupAdapter.OnSelectionStateChangedListener,
        SearchSongsDialog.OnSearchSongsActionListener
{

    private String TAG = "SelectSongsFragment";
    Button btnGroupByAlbum;
    Button btnGroupByArtist;
    Button btnGroupBySong;
    ImageButton btnSearchSongs;

    ProgressDialog loadingProgressDialog;

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

    private static final String SYMBOL_GROUP_STRING = "*?!"; // group heading for songs starting with non-alphanumeric

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
        Log.d(TAG, "setSongList");
        // This displays ALL songs on the device
        songs = new ArrayList<>();
        MusicContent.getAllSongsGroupedByArtist(getContext(), songs);
        // Songs are set selected (ticked) based on the current playlist
        setListViewContentsGrouped();
    }

    private void changeGroupBy(int newGroupBy) {
        groupby = newGroupBy;
        setListViewContentsGrouped();
    }

    private void filterSongs() {
        if (filterString == null) {
            FragmentManager fm = getFragmentManager();
            SearchSongsDialog searchSongsDialog = new SearchSongsDialog();
            searchSongsDialog.setOnSearchStringEntered(this);
            searchSongsDialog.show(fm, "search_songs_dialog");
        } else {
            filterString = null; // no filter
            btnSearchSongs.setImageResource(R.drawable.ic_search);
            setListViewContentsGrouped();
        }
    }

    // callback from search dialog when a search string is entered
    @Override
    public void onSearchStringEntered(String searchString) {
        filterString = searchString;
        btnSearchSongs.setImageResource(R.drawable.ic_search_off);
        setListViewContentsGrouped();
    }

    // helper method, returns true if song is OK for the current filter
    private boolean songMatchesFilterCriteria(Song s) {
        String searchStringUpperCase = filterString.toUpperCase();
        String[] searchWords = searchStringUpperCase.split("\\s+");
        for (String word : searchWords) {
            if (s.getTitle().toUpperCase().contains(word))
                return true;
            if (s.getArtist().toUpperCase().contains(word))
                return true;
            if (s.getAlbum().toUpperCase().contains(word))
                return true;
        }
        return false;
    }

    // returns the comparator to use for sorting songs
    // used for grouping
    private Comparator<Song> getComparator() {
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

    // Used to load the grouped, filtered and selected songs in the background (Async)
    // Be careful with non-static inner Handler classes. The can be a risk for memory leaks
    private class LoadSongsAsyncTask extends AsyncTask <Void, Integer, ArrayList<Long>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingProgressDialog = ProgressDialog.show(getContext(),"Please Wait", "Organising Songs ...", false);
        }

        @Override
        protected ArrayList<Long> doInBackground(Void... params) {
            // set up non-UI things in the background
            Collections.sort(songs, getComparator());
            ArrayList<Long> selectedSongs = MusicContent.getSongsInPlaylist(getContext(), 0);
            return selectedSongs;
        }

        @Override
        protected void onPostExecute(ArrayList<Long> resultSelectedSongs) {
            super.onPostExecute(resultSelectedSongs);
            Log.d(TAG, "Async oPE");
            getSongGroups(resultSelectedSongs);
            updateUIAfterLoadingSongs();
        }
    }
    ////////////End of Async task ///////////////

    // Wrapper method to load the grouped list and update the GUI
    // Creates an async task to do it in the background
    private void setListViewContentsGrouped() {
        new LoadSongsAsyncTask().execute();
        // will call updateUIAfterLoadingSongs when async task is finished
    }

    // Called by post execute of AsyncTask
    // Back on the main UI thread with the groups all loaded
    // Update the UI accordingly
    private void updateUIAfterLoadingSongs() {
        Log.d(TAG, "Finish up after task...");
        btnGroupByAlbum.setEnabled(groupby!=GROUPBY_ALBUM);
        btnGroupByArtist.setEnabled(groupby!=GROUPBY_ARTIST);
        btnGroupBySong.setEnabled(groupby!=GROUPBY_SONG);
        groupAdapter.notifyDataSetChanged();
        for (int g=0 ; g < groupAdapter.getGroupCount() ; g++)
        {
            elvGroupList.collapseGroup(g);
        }
        loadingProgressDialog.dismiss();
    }

    // get the 'key' for grouping a song.
    // depends on the current 'groupby' (artist, album or song (initial letter)
    // If grouped by song then additionally group all non alphanumeric together
    private String getGroupKey(Song s) {
        switch (groupby) {
            case GROUPBY_ALBUM:
                return s.getAlbum();
            case GROUPBY_ARTIST:
                return s.getArtist();
            default: // grouping by first letter of title
                String key = s.getTitle().substring(0, 1).toUpperCase();
                char c = key.charAt(0);
                if (Character.isDigit(c) || Character.isLetter(c))
                    return key;
                else
                    return SYMBOL_GROUP_STRING;
        }
    }

    // This is called from the AsyncTask and happens in the background
    // Group the songs into either artists, albums or songs, depending on the current groupBy
    // Songs in the current playlist will be marked as selected
    // Songs are filtered depending on current search
    private void getSongGroups(ArrayList<Long> selectedSongs) {
        Log.d(TAG, "getSongGroups");
        SongGroup group = null;
        artistGroups.clear();
        int i = 0;

        // No need for the MAP approach, because the songs are always ordered
        String currentKey = null;
        for (Song s : songs) {
            if (s.getDuration() > Settings.getMinDurationInSeconds(getContext()) * 1000) {
                if (filterString == null || songMatchesFilterCriteria(s)) {
                    String key = getGroupKey(s);
                    if (currentKey == null || !(currentKey.equals(key.toUpperCase()))) {
                        // new group
                        currentKey = key.toUpperCase();
                        group = new SongGroup(key, groupby == GROUPBY_ALBUM ? s.getArtist() : null);
                        artistGroups.append(i++, group);
                    } else {
                        if (groupby == GROUPBY_ALBUM) {
                            if (!s.getArtist().equals(group.groupDetail)) {
                                group.groupDetail = "various artists";
                            }
                        }
                    }
                    group.songs.add(new SelectedSong(s, selectedSongs.contains(s.getID()), groupby == GROUPBY_ARTIST ? s.getAlbum() : s.getArtist()));
                }
            }
        }
    }

    /* the old way using MAP
        for (Song s : songs) {
            if (s.getDuration() > Settings.getMinDurationInSeconds(getContext()) * 1000) {
                if (filterString == null || songMatchesFilterCriteria(s)) {
                    // String key = groupby == GROUPBY_ALBUM ? s.getAlbum() : (groupby == GROUPBY_ARTIST ? s.getArtist() : s.getTitle().substring(0, 1).toUpperCase());
                    String key = getGroupKey(s);
                    if (groupMap.containsKey(key.toUpperCase())) {
                        // We already have a group with this key
                        group = groupMap.get(key.toUpperCase());
                        if (groupby == GROUPBY_ALBUM) {
                            if (!s.getArtist().equals(group.groupDetail)) {
                                group.groupDetail = "various artists";
                            }
                        }
                    } else {
                        // New key, make new group
                        group = new SongGroup(key, groupby == GROUPBY_ALBUM ? s.getArtist() : null);
                        artistGroups.append(i++, group);
                        groupMap.put(key.toUpperCase(), group);
                    }
                    group.songs.add(new SelectedSong(s, selectedSongs.contains(s.getID()), groupby == GROUPBY_ARTIST ? s.getAlbum() : s.getArtist()));
                }
            }
        }
    }
*/

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
            menuInflater.inflate(R.menu.menu_context_song, menu);
        } else {
            menuInflater.inflate(R.menu.menu_context_artist, menu);
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

            filterString = savedInstanceState.getString(STATE_FILTERSTRING);
            btnSearchSongs.setImageResource(filterString == null ? R.drawable.ic_search : R.drawable.ic_search_off);
            setListViewContentsGrouped();
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