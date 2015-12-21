package asbridge.me.uk.MMusic.GUIfragments;

import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        public void onSongsChanged();
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