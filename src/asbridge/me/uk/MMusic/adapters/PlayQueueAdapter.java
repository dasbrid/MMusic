package asbridge.me.uk.MMusic.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
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

    private PlayQueueActionsListener playqueueActionsListener = null;
    public interface PlayQueueActionsListener  {
        void onRemoveSongClicked(Song song);
        void onMoveSongToTopClicked(Song song);
    }


    // Constructor
    public PlayQueueAdapter(Context activity, ArrayList<Song> theSongs){
        songs=theSongs;
        songInf=LayoutInflater.from(activity);
        playqueueActionsListener = (PlayQueueActionsListener)activity;
    }

    private Context activity;
    // Constructor
    public PlayQueueAdapter(Context activity, PlayQueueActionsListener listener, ArrayList<Song> theSongs){
        Log.d(TAG, "Ctor: context is " + (activity==null?"null":"not null"));
        songs=theSongs;
        this.activity = activity;
        // moved to createview songInf=LayoutInflater.from(activity);
        playqueueActionsListener = listener;
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

    public static class ViewHolder{
        public TextView songTitle;
        public TextView songArtist;
        public ImageButton btnRemoveSong;
        public ImageButton btnMoveToTop;
        public View view;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        ViewHolder holder;
        songInf=LayoutInflater.from(activity);
        if(convertView==null) {
            vi = songInf.inflate(R.layout.playqueuesong, null);


            holder = new ViewHolder();
            holder.view = vi;
            holder.songTitle = (TextView) vi.findViewById(R.id.pqsong_title);
            holder.songArtist =(TextView)vi.findViewById(R.id.pqsong_artist);
            holder.btnRemoveSong =(ImageButton)vi.findViewById(R.id.pqbtnRemoveSong);
            holder.btnMoveToTop =(ImageButton)vi.findViewById(R.id.pqbtnMoveToTop);

            vi.setTag( holder );
        } else {
            holder=(ViewHolder)vi.getTag();
        }

        Song currSong = songs.get(position);
        if (position % 2 == 1) {
            holder.view.setBackgroundColor(Color.rgb(0x22,0x22,0x22));
        } else {
            holder.view.setBackgroundColor(Color.rgb(0x44,0x44,0x44));
        }

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
            if (playqueueActionsListener != null)
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
            if (playqueueActionsListener != null)
                playqueueActionsListener.onRemoveSongClicked(song);
        }
    }


}
