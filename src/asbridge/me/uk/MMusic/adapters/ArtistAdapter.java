package asbridge.me.uk.MMusic.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by AsbridgeD on 08/12/2015.
 */
public class ArtistAdapter extends BaseAdapter {

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

        //map to album layout
//        LinearLayout songLay = (LinearLayout)songInf.inflate(R.layout.bucket_in_list, parent, false);

        // using standard android layout, but could copy anc customise, but must be a checked text view...
        CheckedTextView songLay = (CheckedTextView)artistInf.inflate(android.R.layout.simple_list_item_multiple_choice/*R.layout.bucket_in_list_android*/, parent, false);
        TextView tvBucketName = (TextView)songLay.findViewById(android.R.id.text1);
        tvBucketName.setText(currArtist);

        //set position as tag
        songLay.setTag(position);
        return songLay;
    }

}
