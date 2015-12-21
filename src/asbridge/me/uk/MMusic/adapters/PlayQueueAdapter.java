package asbridge.me.uk.MMusic.adapters;

import android.app.Activity;
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

    private PlayQueueActionsListener playqueueActionsListener;
    public interface PlayQueueActionsListener  {
        void onRemoveSongClicked(Song song);
        void onMoveSongToTopClicked(Song song);
    }


    // Constructor
    public PlayQueueAdapter(Activity activity, ArrayList<Song> theSongs){
        songs=theSongs;
        songInf=LayoutInflater.from(activity);
        playqueueActionsListener = (PlayQueueActionsListener)activity;
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

            /****** View Holder Object to contain xml file elements ******/
            holder = new ViewHolder();
            holder.songTitle = (TextView) vi.findViewById(R.id.pqsong_title);
            holder.songArtist =(TextView)vi.findViewById(R.id.pqsong_artist);
            holder.btnRemoveSong =(ImageButton)vi.findViewById(R.id.pqbtnRemoveSong);
            holder.btnMoveToTop =(ImageButton)vi.findViewById(R.id.pqbtnMoveToTop);

            vi.setTag( holder );
        } else {
            holder=(ViewHolder)vi.getTag();
        }

        //get song using position
        Song currSong = songs.get(position);
        holder.songTitle.setText(currSong.getTitle());
        holder.songArtist.setText(currSong.getArtist());
        holder.btnRemoveSong.setOnClickListener(new OnRemoveButtonClickListener(position));
        holder.btnMoveToTop.setOnClickListener(new OnbtnMoveToTopClickListener(position));


        return vi;
    }

    class OnbtnMoveToTopClickListener implements View.OnClickListener {
        int songPosition;
        // constructor
        public OnbtnMoveToTopClickListener(int position) {
            this.songPosition = position;
        }
        @Override
        public void onClick(View v) {
            // checkbox clicked
            final Song song = (Song) getItem(songPosition);
            Log.d(TAG, "move to top song "+song.getPID()+","+song.getTitle()+","+songPosition);
            playqueueActionsListener.onMoveSongToTopClicked(song);
        }
    }

    class OnRemoveButtonClickListener implements View.OnClickListener {
        int songPosition;
        // constructor
        public OnRemoveButtonClickListener(int position) {
            this.songPosition = position;
        }
        @Override
        public void onClick(View v) {
            // checkbox clicked
            final Song song = (Song) getItem(songPosition);
            Log.d(TAG, "remove song "+song.getPID()+","+song.getTitle()+","+songPosition);
            playqueueActionsListener.onRemoveSongClicked(song);
        }
    }


}
