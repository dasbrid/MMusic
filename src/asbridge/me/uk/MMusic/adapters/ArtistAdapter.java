package asbridge.me.uk.MMusic.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;

/**
 * Created by AsbridgeD on 08/12/2015.
 */
public class ArtistAdapter extends BaseAdapter {

    private String TAG = "DAVE:ArtistAdapter";

    private ArrayList<String> artists;
    private LayoutInflater artistInf;

    // Constructor
    public ArtistAdapter(Context c, ArrayList<String> theSongs){
        artists=theSongs;
        artistInf=LayoutInflater.from(c);
    }

    @Override
    public int getCount() {
        return artists.size();
    }

    @Override
    public Object getItem(int index) {
        // TODO Auto-generated method stub
        return artists.get(index);
    }

    public String getArtist(int position) {
        return artists.get(position);
    }

    @Override
    public long getItemId(int index) {
        return index;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //get album using position
        String currArtist = artists.get(position);

        // using standard android layout, but could copy anc customise, but must be a checked text view...
        CheckedTextView songLay = (CheckedTextView)artistInf.inflate(android.R.layout.simple_list_item_multiple_choice/*R.layout.bucket_in_list_android*/, parent, false);
        TextView tvArtist = (TextView)songLay.findViewById(android.R.id.text1);
        tvArtist.setText(currArtist);

        //set position as tag
        songLay.setTag(position);
        return songLay;
    }

}
