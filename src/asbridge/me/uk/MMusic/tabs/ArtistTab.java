package asbridge.me.uk.MMusic.tabs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import asbridge.me.uk.MMusic.R;
import asbridge.me.uk.MMusic.adapters.ArtistAdapter;
import asbridge.me.uk.MMusic.utils.Content;

import java.util.ArrayList;
import android.support.v4.app.Fragment;
/**
 * Created by AsbridgeD on 08/12/2015.
 */
public class ArtistTab extends Fragment {

    private ArrayList<String> artistList;
    ListView lvBucketList;
    ArtistAdapter artistAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_artist, container, false);

        lvBucketList = (ListView)v.findViewById(R.id.lvArtistList);

        lvBucketList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        artistList = new ArrayList<String>();
        artistList.add("Ben lÓncle Soul");
        artistList.add("Other");
//        artistList = Content.getAlbumsFromMedia(getContext());
        artistAdapter = new ArtistAdapter(getContext(), artistList);
        lvBucketList.setAdapter(artistAdapter);
        return v;
    }
}