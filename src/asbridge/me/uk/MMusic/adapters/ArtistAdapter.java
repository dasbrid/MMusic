package asbridge.me.uk.MMusic.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import asbridge.me.uk.MMusic.R;

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

    /********* Create a holder Class to contain inflated xml file elements *********/
    public static class ViewHolder{
        public TextView tvArtist;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        ViewHolder holder;

        if(convertView==null) {
            /****** Inflate tabitem.xml file for each row ( Defined below ) *******/
            vi = artistInf.inflate(android.R.layout.simple_list_item_multiple_choice, null);
            holder = new ViewHolder();
            holder.tvArtist = (TextView) vi.findViewById(android.R.id.text1);

            vi.setTag( holder );
        } else {
            holder=(ViewHolder)vi.getTag();
        }
        //get album using position
        String currArtist = artists.get(position);
        holder.tvArtist.setText(currArtist);

        return vi;
    }

}