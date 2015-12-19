package asbridge.me.uk.MMusic.GUIfragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import asbridge.me.uk.MMusic.R;
import asbridge.me.uk.MMusic.adapters.SongAdapter;
import asbridge.me.uk.MMusic.classes.Song;

import java.util.ArrayList;

/**
 * Created by David on 09/12/2015.
 */
public class MusicPlayerFragment extends Fragment implements View.OnClickListener {

    private String TAG = "DAVE:MusicPlayerFragment";

    private SongAdapter songAdt;
    private ListView lvSongList;
    private TextView tvNowPlaying;
    private ArrayList<Song> songList;

    private MusicPlayerFragmentListener listener = null;
    public interface MusicPlayerFragmentListener{
        public void onPlayClicked();
        public void onNextClicked();
    }

    public MusicPlayerFragment () {
        super();
        songList = new ArrayList<Song>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_music_player, container, false);

        Log.d(TAG, "onCreateView");
        Button btnPlay = (Button) v.findViewById(R.id.frag_btnPlay);
        btnPlay.setOnClickListener(this);

        Button btnNext = (Button) v.findViewById(R.id.frag_btnNext);
        btnNext.setOnClickListener(this);

        tvNowPlaying = (TextView) v.findViewById(R.id.frag_tvNowPlaying);

        lvSongList = (ListView)v.findViewById(R.id.frag_song_list);


        songAdt = new SongAdapter(getContext(), songList);

        lvSongList.setAdapter(songAdt);
        return v;
    }

    public void setListener(MusicPlayerFragmentListener l) {
        listener = l;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.frag_btnPlay:
                if (listener != null);
                    listener.onPlayClicked();
                break;
            case R.id.frag_btnNext:
                if (listener != null);
                    listener.onNextClicked();
                break;
        }
    }

    public void setSongList (ArrayList<Song> songs) {
        Log.d(TAG, "setSongList");
        songList.clear();
        songList.addAll(songs);
        Log.d(TAG, "setSongList:"+songList.size());
//        songAdt.notifyDataSetChanged();
    }

    public void setNowPlaying(String songArtist, String songTitle) {
        tvNowPlaying.setText(songArtist + "--" + songTitle);
    }

}