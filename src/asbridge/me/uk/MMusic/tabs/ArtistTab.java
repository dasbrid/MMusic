package asbridge.me.uk.MMusic.tabs;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import asbridge.me.uk.MMusic.R;
import asbridge.me.uk.MMusic.adapters.ArtistAdapter;
import asbridge.me.uk.MMusic.classes.Song;
import asbridge.me.uk.MMusic.utils.Content;

import java.util.ArrayList;
import android.support.v4.app.Fragment;
/**
 * Created by AsbridgeD on 08/12/2015.
 */
public class ArtistTab extends Fragment implements View.OnClickListener {

    private String TAG = "DAVE: ArtistTab";

    private ArrayList<String> artistList;
    ListView lvBucketList;
    ArtistAdapter artistAdapter;

    private OnArtistsChangedListener listener = null;
    public interface OnArtistsChangedListener{
        public void onArtistsChanged(ArrayList<String> artists);
    }

    public void setOnArtistsChangedListener(OnArtistsChangedListener l) {
        listener = l;
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onclick");
        switch (v.getId()) {
            case R.id.btnArtistxx:
                changeArtist();
                break;
        }
    }

    public void changeArtist() {
        Log.d(TAG, "changeArtist");
        ArrayList<String> artists = getSelectedArtists();
        if (listener != null)
            listener.onArtistsChanged(artists);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_artist, container, false);
        Button btnArtist = (Button) v.findViewById(R.id.btnArtistxx);
        btnArtist.setOnClickListener(this);

        lvBucketList = (ListView)v.findViewById(R.id.lvArtistList);

        lvBucketList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        artistList = new ArrayList<String>();

        Content.getArtists(getContext(), artistList);

        artistAdapter = new ArtistAdapter(getContext(), artistList);
        lvBucketList.setAdapter(artistAdapter);
        return v;
    }

    public ArrayList<String> getSelectedArtists() {
        SparseBooleanArray checked = lvBucketList.getCheckedItemPositions();
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