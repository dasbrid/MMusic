package asbridge.me.uk.MMusic.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import asbridge.me.uk.MMusic.R;
import asbridge.me.uk.MMusic.adapters.SongAdapter;
import asbridge.me.uk.MMusic.classes.Song;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by David on 09/12/2015.
 */
public class MusicPlayerFragment extends Fragment {

    private String TAG = "DAVE:MusicPlayerFragment";

    private SongAdapter songAdt;
    private ListView lvSongList;
    private ArrayList<Song> songList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_music_player, container, false);

        Log.d(TAG, "onCreateView");

        lvSongList = (ListView)v.findViewById(R.id.frag_song_list);

        songList = new ArrayList<Song>();

        songAdt = new SongAdapter(getContext(), songList);
        lvSongList.setAdapter(songAdt);
        return v;
    }

    public void setSongList (ArrayList<Song> songs) {
        Log.d(TAG, "setSongList");
        songList.clear();
        songList.addAll(songs);
        Log.d(TAG, "setSongList:"+songList.size());
        songAdt.notifyDataSetChanged();
    }
}