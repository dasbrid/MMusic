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
public class ArtistFragment extends Fragment implements
        View.OnClickListener
{

    private String TAG = "DAVE: ArtistFragment";

    private CheckBox cbCheckAll;

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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Make sure to call the super method so that the states of our views are saved
        super.onSaveInstanceState(outState);
        // Save our own state now
        outState.putParcelableArrayList(STATE_ALLSONGS, songs);
        outState.putParcelableArrayList(STATE_SELECTEDSONGS, getSelectedSongs());
    }

    public void setSongList() {
        // This displays ALL songs on the device
        songs = new ArrayList<>();
        MusicContent.getAllSongs(getContext(), songs);
        Log.d(TAG, "setSongList " + songs.size());

        // Songs are set selected based on the current playlist
        ArrayList<Long> selectedSongs = MusicContent.getSongsInPlaylist(getContext(), 0);

        setListViewContents(songs, selectedSongs);
    }

    private void setListViewContents(ArrayList<Song> songs, ArrayList<Long> selectedSongs) {
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
            case R.id.btnArtist:
                changeArtist();
                break;
            case R.id.btnSongsSelectAll:
                selectAllorNone(true);
                break;
            case R.id.btnSongsSelectNone:
                selectAllorNone(false);
                break;
            case R.id.btnSongsSelect:
                selectSongs(((TriStateButton)v).getState());
                break;
        }
    }

    private void selectSongs(int buttonState) {
/*
        int currentState = artistGroupAdapter.getSelectedState();
        if (currentState == 2) {
            btnSongsSelect.setState(0);
            selectAllorNone(false);

            artistGroupAdapter.notifyDataSetChanged();
        } else {
            btnSongsSelect.setState(2);
            selectAllorNone(false);
            artistGroupAdapter.notifyDataSetChanged();
        }
*/
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
        ArrayList<Song> selectedSongs;
        selectedSongs = getSelectedSongs();
        MusicContent.setCurrentPlaylist(getContext(), selectedSongs);

        if (listener != null)
            listener.onSongsChanged();
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
        ImageButton btnSongsSelectAll = (ImageButton) v.findViewById(R.id.btnSongsSelectAll);
        btnSongsSelectAll.setOnClickListener(this);
        Button btnSongsSelectNone = (Button) v.findViewById(R.id.btnSongsSelectNone);
        btnSongsSelectNone.setOnClickListener(this);

        btnSongsSelect = (TriStateButton) v.findViewById(R.id.btnSongsSelect);
        btnSongsSelect.setOnClickListener(this);


        elvArtistGroupList = (ExpandableListView) v.findViewById(R.id.lvSongsByArtist);

        artistGroups = new SparseArray<>();

        artistGroupAdapter = new ArtistGroupAdapter(getActivity(), artistGroups);
        elvArtistGroupList.setAdapter(artistGroupAdapter);
        registerForContextMenu(elvArtistGroupList);

        // if we have a saved instance then we are returning (e.g. rotate)
        // get the list of songs and the list of selected songs from the saved instance
        if (savedInstanceState != null) {
            // Restore last state for checked position.
            songs = savedInstanceState.getParcelableArrayList(STATE_ALLSONGS);
            ArrayList<Song> selectedSongs = savedInstanceState.getParcelableArrayList(STATE_SELECTEDSONGS);
            ArrayList<Long> selectedSongIDs = new ArrayList<>();
            for (Song s : selectedSongs) {
                selectedSongIDs.add(s.getID());
            }
            setListViewContents(songs, selectedSongIDs);
        } else {
            // if there is no saved instance then we will get songs from the device content provider
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