package asbridge.me.uk.MMusic.classes;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AsbridgeD on 21/12/2015.
 */
public class SongGroup {

    public String groupName;
    public String groupDetail;

    public final List<SelectedSong> songs = new ArrayList<>();

    public SongGroup(String name) {
        this.groupName = name;
        this.groupDetail = null;
    }

    public SongGroup(String name, String detail) {
        this.groupName = name;
        this.groupDetail = detail;
    }
/*
    public static class SelectedSong {
        public Song song;
        public boolean selected;
        public String songDetails;

        public SelectedSong(Song theSong, boolean isSelected, String theSongDetails) {
            song = theSong;
            selected = isSelected;
            songDetails = theSongDetails;
        }
    }
*/
    public int getNumSelected() {
        int n = 0;
        for (SelectedSong ss : songs) {
            if (ss.selected)
                n += 1;
        }
        return n;
    }

    public int getNumSongs() {
        return songs.size();
    }

      // returns:
    // 2 if ALL the artists songs are selected
    // 0 if none selected
    // 1 if some selected
    public int getSelectedState() {
        int numselected = 0;
        int numSongs = songs.size();
        for (SelectedSong ss : songs) {
            if (ss.selected) numselected++;
        }
        if (numselected == 0 )
            return 0;
        else if (numselected == numSongs)
            return 2;
        else
            return 1;
    }

}
