package asbridge.me.uk.MMusic.activities;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.*;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.Toast;
import asbridge.me.uk.MMusic.GUIfragments.SelectSongsFragment;
import asbridge.me.uk.MMusic.R;
import asbridge.me.uk.MMusic.adapters.GroupAdapter;
import asbridge.me.uk.MMusic.classes.SelectedSong;
import asbridge.me.uk.MMusic.classes.SongGroup;
import asbridge.me.uk.MMusic.classes.RetainFragment;
import asbridge.me.uk.MMusic.classes.Song;
import asbridge.me.uk.MMusic.controls.TriStateButton;
import asbridge.me.uk.MMusic.dialogs.DeletePlaybucketDialog;
import asbridge.me.uk.MMusic.dialogs.LoadPlaybucketDialog;
import asbridge.me.uk.MMusic.dialogs.SavePlaybucketDialog;
import asbridge.me.uk.MMusic.dialogs.SearchSongsDialog;
import asbridge.me.uk.MMusic.services.SimpleMusicService;
import asbridge.me.uk.MMusic.settings.SettingsActivity;
import asbridge.me.uk.MMusic.utils.AppConstants;
import asbridge.me.uk.MMusic.utils.MusicContent;
import asbridge.me.uk.MMusic.utils.Settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by David on 20/12/2015.
 */
public class SelectSongsActivity extends FragmentActivity
        implements
//        SelectSongsFragment.OnSongsChangedListener,
        RetainFragment.RetainFragmentListener
        , LoadPlaybucketDialog.OnLoadPlaybucketSelectedListener
        , SavePlaybucketDialog.OnSavePlaybucketActionListener
        , DeletePlaybucketDialog.OnDeletePlaybucketClickedListener
        , View.OnClickListener
        , SearchSongsDialog.OnSearchSongsActionListener
        , GroupAdapter.OnSelectionStateChangedListener
{
    private static final String TAG = "SelectSongsActivity";

    private RetainFragment retainFragment = null;
//    private SelectSongsFragment artistsFragment = null;

    // from fragment
    Button btnGroupByAlbum;
    Button btnGroupByArtist;
    Button btnGroupBySong;
    ImageButton btnSearchSongs;

    private ProgressDialog loadingProgressDialog;

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

    private static final String STATE_ALLSONGS = "ALLSONGS";
    private static final String STATE_SELECTEDSONGS = "SELECTEDSONGS";
    private static final String STATE_GROUPBY = "GROUPBY";
    private static final String STATE_FILTERSTRING = "FILTERSTRING";

    private static final String SYMBOL_GROUP_STRING = "*?!"; // group heading for songs starting with non-alphanumeric
     // end from fragment


    // call back from retainfragment which binds to the music service
    @Override
    public void onMusicServiceReady() {
        Log.d(TAG, "onMusicServiceReady");
//        artistsFragment.setSongList();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_songs);

        FragmentManager fm = getFragmentManager();
        retainFragment = (RetainFragment) fm.findFragmentByTag(AppConstants.TAG_RETAIN_FRAGMENT);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (retainFragment == null) {
            Log.d(TAG, "creating and adding retain Fragment");
            retainFragment = new RetainFragment();
            fm.beginTransaction().add(retainFragment, AppConstants.TAG_RETAIN_FRAGMENT).commit();
        }

        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(prefslistener);

        btnSongsSelect = (TriStateButton) findViewById(R.id.btnSongsSelect);
        btnSongsSelect.setOnClickListener(this);

        btnGroupByAlbum = (Button) findViewById(R.id.btnGroupByAlbum);
        btnGroupByAlbum.setOnClickListener(this);
        btnGroupByArtist = (Button) findViewById(R.id.btnGroupByArtist);
        btnGroupByArtist.setOnClickListener(this);
        btnGroupBySong = (Button) findViewById(R.id.btnGroupBySong);
        btnGroupBySong.setOnClickListener(this);
        btnSearchSongs = (ImageButton) findViewById(R.id.btnSearchSongs);
        btnSearchSongs.setOnClickListener(this);

        elvGroupList = (ExpandableListView) findViewById(R.id.lvSongsByArtist);

        artistGroups = new SparseArray<>();

        groupAdapter = new GroupAdapter(this, artistGroups);
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
/* fragment moved to activity
        artistsFragment = (SelectSongsFragment)getSupportFragmentManager().findFragmentById(R.id.fragArtists);
        if (artistsFragment != null)
        {
            artistsFragment.setOnSongsChangedListener(this);
        }
*/
    }



    // bind to the Service instance when the Activity instance starts
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        retainFragment.doBindService();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        ExpandableListView.ExpandableListContextMenuInfo elvcmi = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
        int type = ExpandableListView.getPackedPositionType(elvcmi.packedPosition);
        MenuInflater menuInflater = getMenuInflater();
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
                    playThisSongNext(s);
                    return true;
                case R.id.menu_song_long_click_addtoqueue:
                    addThisSongToPlayQueue(s);
                    return true;
                case R.id.menu_song_long_click_playnow:
                    playThisSongNow(s);
                    return true;
            }

        } else if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
            groupPos = ExpandableListView.getPackedPositionGroup(menuInfo.packedPosition);

            int key = artistGroups.keyAt(groupPos);
            // get the object by the key.
            SongGroup ag = artistGroups.get(key);

            switch (item.getItemId()) {
                case R.id.menu_artist_long_click_addsongstoqueue:
                    addArtistsSongsToPlayQueue(ag);
                    return true;
            }

            switch (item.getItemId()) {
                case R.id.menu_artist_long_click_clearandaddsongstoqueue:
                    clearPlayQueueAndaddArtistsSongsToPlayQueue(ag);
                    return true;
            }
        }
        return super.onContextItemSelected(item);
    }

    /* listener from the artistFragment */
    //@Override
    public void playThisSongNext(Song s) {
        if (retainFragment != null) {
            if (retainFragment.serviceReference != null) {
                retainFragment.serviceReference.insertThisSongAtTopOfPlayQueue(s);
            }
        }
    }

    /* listener from the artistFragment */
    //@Override
    public void addThisSongToPlayQueue(Song s) {
        if (retainFragment != null) {
            if (retainFragment.serviceReference != null) {
                retainFragment.serviceReference.insertThisSongIntoPlayQueue(s);
            }
        }
    }

    /* listener from the artistFragment */
    //@Override
    public void playThisSongNow(Song s) {
        if (retainFragment != null) {
            if (retainFragment.serviceReference != null) {
                retainFragment.serviceReference.playThisSong(s);
            }
        }
    }

    /* listener from the artistFragment */
    //@Override
    public void addArtistsSongsToPlayQueue(SongGroup ag) {
        if (retainFragment != null) {
            if (retainFragment.serviceReference != null) {
                for (SelectedSong ss : ag.songs) {
                    retainFragment.serviceReference.insertThisSongIntoPlayQueue(ss.song);
                }
            }
        }
    }

    /* listener from the artistFragment */
    //@Override
    public void clearPlayQueueAndaddArtistsSongsToPlayQueue(SongGroup ag) {
        if (ag.songs.size() == 0)
            return;
        if (retainFragment != null) {
            if (retainFragment.serviceReference != null) {
                retainFragment.serviceReference.clearPlayQueue();
                for (SelectedSong ss : ag.songs) {
                    retainFragment.serviceReference.insertThisSongIntoPlayQueue(ss.song);
                }
            }
        }
    }

    /* listener from the artistFragment */
    //@Override
    public void onSongsChanged() {
        Log.d(TAG, "onSongsChanged");
        Log.d(TAG, "retain fragment is " + (retainFragment==null?"null":"not null"));
        if (retainFragment != null) {
            Log.d(TAG, "serviceref fragment is " + (retainFragment.serviceReference==null?"null":"not null"));
            if (retainFragment.serviceReference != null) {
                ArrayList<Song> selectedSongs;
                selectedSongs = getSelectedSongs();
                Log.d(TAG, "setting list: "+ selectedSongs.size() + " songs");
            }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_options_selectsongs, menu);
        return true;
    }

    // handle user interaction with the menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_save_playbucket:
                saveCurrentAsPlaybucket();
                return true;
            case R.id.action_load_playbucket:
                loadPlaybucket();
                return true;
            case R.id.action_delete_playbucket:
                deletePlayBucket();
                return true;
            case R.id.action_end:
                Intent playIntent = new Intent(this, SimpleMusicService.class);
                stopService(playIntent);
                retainFragment.serviceReference=null;
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deletePlayBucket() {
        FragmentManager fm = getFragmentManager();
        DeletePlaybucketDialog deletePlaybucketDialog = new DeletePlaybucketDialog();
        deletePlaybucketDialog.setOnDeletePlaybucketClickedListener(this);
        deletePlaybucketDialog.show(fm, "fragment_deleteplaylist_dialog");
    }

    // Callback when playbucket is clicked in the delete dialog
    @Override
    public void onDeletePlaybucketClicked(int playbucketID, String playbucketName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm playbucket delete")
                .setMessage("Are you sure you want to delete playbucket " + playbucketName)
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MusicContent.deletePlaybucket(getApplicationContext(), playbucketID);
                        dialog.dismiss();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void saveCurrentAsPlaybucket() {
        FragmentManager fm = getFragmentManager();
        SavePlaybucketDialog savePlaybucketDialog = new SavePlaybucketDialog();
        savePlaybucketDialog.setOnPlaybucketNameEnteredListener(this);
        savePlaybucketDialog.show(fm, "fragment_saveplaylist_dialog");
    }

    // Callback from SavePlaybucketDialog when a new playbucket name has been entered
    @Override
    public void onNewPlaybucketNameEntered(String playBucketName) {
        MusicContent.createNewBucket(this, playBucketName);
    }

    // Callback from SavePlaybucketDialog when an existing playbucket is clicked
    @Override
    public void onSavePlayBucketSelected(int savePlaybucketID) {
        MusicContent.updateSavedPlaybucket(this, savePlaybucketID);
        setSongList();
    }

    private void loadPlaybucket() {
        FragmentManager fm = getFragmentManager();
        LoadPlaybucketDialog loadPlaybucketDialog = new LoadPlaybucketDialog();
        loadPlaybucketDialog.setOnPlaybucketSelectedListener(this);
        loadPlaybucketDialog.show(fm, "fragment_loadplaylist_dialog");
    }

    // Callback from LoadPlaybucketDialog when playbucket is ticked
    @Override
    public void onLoadPlayBucketSelected(int playbucketID) {
        MusicContent.setCurrentBucketFromSavedBucket(this, playbucketID);
        setSongList();
    }

    // From Fragment
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

    public void setSongList() {
        Log.d(TAG, "setSongList");
        // This displays ALL songs on the device
        songs = new ArrayList<>();
        MusicContent.getAllSongsGroupedByArtist(this, songs);
        // Songs are set selected (ticked) based on the current playlist
        setListViewContentsGrouped();
    }

    // Used to load the grouped, filtered and selected songs in the background (Async)
    // Be careful with non-static inner Handler classes. The can be a risk for memory leaks
    private class LoadSongsAsyncTask extends AsyncTask<Void, Integer, ArrayList<Long>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            loadingProgressDialog = ProgressDialog.show(SelectSongsActivity.this,"Please Wait", "Organising Songs ...", false);
        }

        @Override
        protected ArrayList<Long> doInBackground(Void... params) {
            // set up non-UI things in the background
            Collections.sort(songs, getComparator());
            ArrayList<Long> selectedSongs = MusicContent.getSongsInPlaylist(getApplicationContext(), 0);
            return selectedSongs;
        }

        @Override
        protected void onPostExecute(ArrayList<Long> resultSelectedSongs) {
            super.onPostExecute(resultSelectedSongs);
            Log.d(TAG, "Async oPE");
            getSongGroups(resultSelectedSongs);
            updateUIAfterLoadingSongs();
            loadingProgressDialog.dismiss();
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
//        = MusicContent.getSongsInPlaylist(getContext(), 0);

//        HashMap<String, SongGroup> groupMap = new HashMap<>();
        SongGroup group = null;
        artistGroups.clear();
        int i = 0;

        // No need for the MAP approach, because the songs are always ordered
        String currentKey = null;
        for (Song s : songs) {
            if (s.getDuration() > Settings.getMinDurationInSeconds(this) * 1000) {
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

    private void changeGroupBy(int newGroupBy) {
        groupby = newGroupBy;
        setListViewContentsGrouped();
    }

    private void filterSongs() {
        if (filterString == null) {
            android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
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

    // an Event from the List Adapter
    // A song has been selected or deselcted and as a result the selected state has changed
    @Override
    public void onSelectionStateChanged(int state) {
        btnSongsSelect.setState(state);
    }
}