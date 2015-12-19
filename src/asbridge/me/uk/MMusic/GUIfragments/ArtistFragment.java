package asbridge.me.uk.MMusic.GUIfragments;

import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import asbridge.me.uk.MMusic.R;
import asbridge.me.uk.MMusic.adapters.ArtistAdapter;
import asbridge.me.uk.MMusic.utils.Content;

import java.util.ArrayList;
import android.support.v4.app.Fragment;
/**
 * Created by AsbridgeD on 08/12/2015.
 */
public class ArtistFragment extends Fragment implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private String TAG = "DAVE: ArtistFragment";

    private CheckBox cbCheckAll;
    private ArrayList<String> artistList;
    ListView lvArtistList;
    ArtistAdapter artistAdapter;

    private OnArtistsChangedListener listener = null;
    public interface OnArtistsChangedListener{
        public void onArtistsChanged(ArrayList<String> artists);
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

    public void setOnArtistsChangedListener(OnArtistsChangedListener l) {
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

    public void changeArtist() {
        Log.d(TAG, "changeArtist");
        ArrayList<String> artists = getSelectedArtists();
        if (listener != null)
            listener.onArtistsChanged(artists);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.fragment_artist, container, false);
        Button btnArtist = (Button) v.findViewById(R.id.btnArtist);
        btnArtist.setOnClickListener(this);

        lvArtistList = (ListView)v.findViewById(R.id.lvArtistList);
        cbCheckAll = (CheckBox)v.findViewById(R.id.cbCheckAll);
        cbCheckAll.setOnCheckedChangeListener(this);
        lvArtistList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        artistList = new ArrayList<String>();

        Content.getArtists(getContext(), artistList);

        artistAdapter = new ArtistAdapter(getContext(), artistList);
        lvArtistList.setAdapter(artistAdapter);
        return v;
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