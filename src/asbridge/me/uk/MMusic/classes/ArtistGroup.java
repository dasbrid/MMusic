package asbridge.me.uk.MMusic.classes;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AsbridgeD on 21/12/2015.
 */
public class ArtistGroup {

    public String artistName;
    public final List<SelectedSong> songs = new ArrayList<>();

    public ArtistGroup(String string) {
        this.artistName = string;
    }

    public static class SelectedSong {
        public Song song;
        public boolean selected;

        public SelectedSong(Song theSong, boolean isSelected) {
            song = theSong;
            selected = isSelected;
        }
    }

    public void doSelectNone() {
        for (SelectedSong ss : songs) {
            ss.selected = false;
        }
    }

    public void doSelectAll() {
        for (SelectedSong ss : songs) {
            ss.selected = true;
        }
    }

    public void changeStateofAllSongs(boolean newState) {
        for (SelectedSong ss : songs) {
            ss.selected = newState;
        }
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
