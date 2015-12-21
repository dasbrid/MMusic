package asbridge.me.uk.MMusic.GUIfragments;

import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import asbridge.me.uk.MMusic.R;
import asbridge.me.uk.MMusic.adapters.ArtistAdapter;
import asbridge.me.uk.MMusic.adapters.ArtistGroupAdapter;
import asbridge.me.uk.MMusic.classes.ArtistGroup;
import asbridge.me.uk.MMusic.classes.Song;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.Fragment;
/**
 * Created by AsbridgeD on 08/12/2015.
 */
public class ArtistFragment extends Fragment implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private String TAG = "DAVE: ArtistFragment";

    private CheckBox cbCheckAll;
    private ArrayList<String> artistList;
//    private ArrayList<Song> songs;
    private SparseArray<ArtistGroup> artistGroups;
    ListView lvArtistList;
    ExpandableListView elvArtistGroupList;
    ArtistAdapter artistAdapter;
    ArtistGroupAdapter artistGroupAdapter;
    private OnSongsChangedListener listener = null;
    public interface OnSongsChangedListener {
        public void onSongsChanged();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d(TAG, "OnCheckedChanged");
        if (buttonView.getId() == R.id.cbCheckAll) {
            Log.d(TAG, "checkAll");
            cbCheckAll.setChecked(isChecked);
            for (int i = 0; i < lvArtistList.getChildCount(); i++) {
                lvArtistList.setItemChecked(i, isChecked);
            }
            artistAdapter.notifyDataSetChanged();
        }
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
        }
    }

    // the changeartist button is clicked
    // Update the services list of artists
    public void changeArtist() {
        Log.d(TAG, "changeArtist");
        ArrayList<String> artists = getSelectedArtists();
        if (listener != null)
            listener.onSongsChanged();
    }

    public void setSongList(ArrayList<Song> songs) {
        Log.d(TAG, "setSongList "+songs.size());
        //Content.getArtists(getContext(), artistList);
        //this.songs = songs;

            String currentArtist = null;
            int i=0;
            ArtistGroup newGroup = null;
            for (Song s : songs) {
                if (newGroup == null || !newGroup.artistName.equals(s.getArtist())) {
                    newGroup = new ArtistGroup(s.getArtist());
                    artistGroups.append(i++, newGroup);
                }
                newGroup.songs.add(new ArtistGroup.SelectedSong(s, true));
            }
        artistAdapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.fragment_artist, container, false);
        Button btnArtist = (Button) v.findViewById(R.id.btnArtist);
        btnArtist.setOnClickListener(this);

        elvArtistGroupList = (ExpandableListView) v.findViewById(R.id.lvSongsByArtist);
        lvArtistList = (ListView)v.findViewById(R.id.lvArtistList);
        cbCheckAll = (CheckBox)v.findViewById(R.id.cbCheckAll);
        cbCheckAll.setOnCheckedChangeListener(this);
        lvArtistList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        artistList = new ArrayList<String>();
        //this.songs = new ArrayList<>();
//        Content.getArtists(getContext(), artistList);



        artistAdapter = new ArtistAdapter(getContext(), artistList);
        lvArtistList.setAdapter(artistAdapter);

        artistGroups = new SparseArray<>();

        artistGroupAdapter = new ArtistGroupAdapter(getActivity(), artistGroups);
        elvArtistGroupList.setAdapter(artistGroupAdapter);

        return v;
    }

    public ArrayList<Song> getSelectedSongs() {
        ArrayList<Song> selectedSongs = new ArrayList<>();
        for(int i = 0; i < artistGroups.size(); i++) {
            int key = artistGroups.keyAt(i);
            // get the object by the key.
            ArtistGroup ag = (ArtistGroup) artistGroups.get(key);
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

    public ArrayList<String> getSelectedArtists() {
        SparseBooleanArray checked = lvArtistList.getCheckedItemPositions();
        ArrayList<String> selectedItems = new ArrayList<String>();
        ArrayList<String> selectedBucketIDs = new ArrayList<String>();
        for (int i = 0; i < checked.size(); i++) {
            // Item position in adapter
            int position = checked.keyAt(i);
            if (checked.valueAt(i)) {
                String selectedItem = artistAdapter.getArtist(position);
                selectedItems.add(selectedItem);
            }
        }

        return selectedItems;
    }
}