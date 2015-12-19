package asbridge.me.uk.MMusic.adapters;

import android.content.Context;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import asbridge.me.uk.MMusic.R;
import asbridge.me.uk.MMusic.classes.Song;
import asbridge.me.uk.MMusic.controls.RearrangeableListView;

import java.util.ArrayList;

/**
 * Created by David on 05/12/2015.
 */
public class PlayQueueAdapter extends BaseAdapter {

    private final String TAG = "PlayQueueAdapter";
    private ArrayList<Song> songs;
    private LayoutInflater songInf;


    // Constructor
    public PlayQueueAdapter(Context c, ArrayList<Song> theSongs){
        songs=theSongs;
        songInf=LayoutInflater.from(c);
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    /********* Create a holder Class to contain inflated xml file elements *********/
    public static class ViewHolder{
        public TextView songTitle;
        public TextView songArtist;
        public ImageButton btnRemoveSong;
        public ImageButton btnMoveToTop;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        ViewHolder holder;

        if(convertView==null) {
            /****** Inflate tabitem.xml file for each row ( Defined below ) *******/
            vi = songInf.inflate(R.layout.playqueuesong, null);

            /****** View Holder Object to contain tabitem.xml file elements ******/

            holder = new ViewHolder();
            holder.songTitle = (TextView) vi.findViewById(R.id.pqsong_title);
            holder.songArtist =(TextView)vi.findViewById(R.id.pqsong_artist);
            holder.btnRemoveSong =(ImageButton)vi.findViewById(R.id.pqbtnRemoveSong);
            holder.btnMoveToTop =(ImageButton)vi.findViewById(R.id.pqbtnMoveToTop);

            /************  Set holder with LayoutInflater ************/
            vi.setTag( holder );
        } else {
            holder=(ViewHolder)vi.getTag();
        }

        //get song using position
        Song currSong = songs.get(position);
        holder.songTitle.setText(currSong.getTitle());
        holder.songArtist.setText(currSong.getArtist());
        holder.btnRemoveSong.setTag(Long.toString(currSong.getID()));
        holder.btnMoveToTop.setTag(Long.toString(currSong.getID()));

        return vi;
    }

}
