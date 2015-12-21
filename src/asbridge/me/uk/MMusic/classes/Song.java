package asbridge.me.uk.MMusic.classes;

/**
 * Created by David on 04/12/2015.
 */
public class Song {
    private long id;
    private String title;
    private String artist;
    private String album;
    private int PID;

    public Song(long songID, String songTitle, String songArtist, String songAlbum, int songPID) {
        id=songID;
        title=songTitle;
        artist=songArtist;
        album=songAlbum;
        PID = songPID;
    }

    public Song(Song theSong, int songPID) {
        id=theSong.id;
        title=theSong.title;
        artist=theSong.artist;
        album=theSong.album;
        PID = songPID;
    }

    public long getID(){return id;}
    public String getTitle(){return title;}
    public String getArtist(){return artist;}
    public String getAlbum(){return album;}
    public int getPID(){return PID;}

}
