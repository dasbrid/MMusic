package asbridge.me.uk.MMusic.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import asbridge.me.uk.MMusic.R;
import asbridge.me.uk.MMusic.classes.Song;

import java.util.ArrayList;

/**
 * Created by David on 05/12/2015.
 */
public class PlayedListAdapter extends BaseAdapter {

    private final String TAG = "PlayedListAdapter";
    private ArrayList<Song> songs;
    private LayoutInflater songInf;

    private Context activity;
    // Constructor
    public PlayedListAdapter(Context activity, ArrayList<Song> theSongs){
        Log.d(TAG, "Ctor: context is " + (activity==null?"null":"not null"));
        songs=theSongs;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int position) {
        return songs.get(position);
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
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        ViewHolder holder;
        songInf=LayoutInflater.from(activity);
        if(convertView==null) {
            /****** Inflate tabitem.xml file for each row ( Defined below ) *******/
            vi = songInf.inflate(R.layout.playedlistsong, null);

            /****** View Holder Object to contain xml file elements ******/
            holder = new ViewHolder();
            holder.songTitle = (TextView) vi.findViewById(R.id.pedqsong_title);
            //holder.songArtist =(TextView)vi.findViewById(R.id.pedqsong_artist);
            vi.setTag( holder );
        } else {
            holder=(ViewHolder)vi.getTag();
        }

        //get song using position
        Song currSong = songs.get(position);
        Log.v(TAG, "position "+position+" songs size = " + songs.size()+(currSong==null?" null":" not null"));
        holder.songTitle.setText(currSong.getArtist() + " - " + currSong.getTitle());
        //holder.songArtist.setText(currSong.getArtist());
        return vi;
    }

}
