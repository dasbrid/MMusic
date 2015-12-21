package asbridge.me.uk.MMusic.classes;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AsbridgeD on 21/12/2015.
 */
public class ArtistGroup {

    public static class SelectedSong {

        public Song song;
        public boolean selected;

        public SelectedSong(Song theSong, boolean isSelected) {
            song = theSong;
            selected = isSelected;
        }
    }
    public String artistName;
    public final List<SelectedSong> songs = new ArrayList<>();

    public ArtistGroup(String string) {
        this.artistName = string;
    }
}
