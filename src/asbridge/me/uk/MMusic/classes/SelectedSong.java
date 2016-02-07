package asbridge.me.uk.MMusic.classes;

/**
 * Created by David on 07/02/2016.
 */
public class SelectedSong {
    public Song song;
    public boolean selected;
    public String songDetails;

    public SelectedSong(Song theSong, boolean isSelected, String theSongDetails) {
        song = theSong;
        selected = isSelected;
        songDetails = theSongDetails;
    }
}
