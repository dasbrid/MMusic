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

    private OnPlayedListClickedListener onPlayedListClickedListener = null;
    public interface OnPlayedListClickedListener  {
        void onPlayedListSongClicked(Song song);
    }

    // Constructor
    public PlayedListAdapter(Context activity, OnPlayedListClickedListener listener, ArrayList<Song> theSongs){
        songs=theSongs;
        this.activity = activity;
        this.onPlayedListClickedListener = listener;
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
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        ViewHolder holder;
        songInf=LayoutInflater.from(activity);
        if(convertView==null) {
            vi = songInf.inflate(R.layout.playedlistsong, null);

            holder = new ViewHolder();
            holder.songTitle = (TextView) vi.findViewById(R.id.pedqsong_title);
            vi.setTag( holder );
        } else {
            holder=(ViewHolder)vi.getTag();
        }

        //get song using position
        Song currSong = songs.get(position);
        holder.songTitle.setText(currSong.getArtist() + " - " + currSong.getTitle());
        vi.setOnClickListener(new OnSongClickListener(position));
        return vi;
    }

    class OnSongClickListener implements View.OnClickListener {
        int position;
        public OnSongClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            final Song s = songs.get(position);
            if (onPlayedListClickedListener != null) {
                onPlayedListClickedListener.onPlayedListSongClicked(s);
            }
        }

    }

}

