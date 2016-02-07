package asbridge.me.uk.MMusic.classes;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by David on 04/12/2015.
 */
public class Song implements Parcelable {
    private long id;
    private String title;
    private String artist;
    private String album;
    private int PID;
    private long duration; // in ms

    public Song(long songID, String songTitle, String songArtist, String songAlbum, int songPID, long songDuration) {
        id=songID;
        title=songTitle;
        artist=songArtist;
        album=songAlbum;
        PID = songPID;
        duration = songDuration;
    }

    public Song(Song theSong, int songPID) {
        id=theSong.id;
        title=theSong.title;
        artist=theSong.artist;
        album=theSong.album;
        PID = songPID;
        duration = theSong.duration;
    }

    public long getID(){return id;}
    public String getTitle(){return title;}
    public String getArtist(){return artist;}
    public String getAlbum(){return album;}
    public int getPID(){return PID;}
    public long getDuration(){return duration;}

    // Parcelling part
    public Song(Parcel in){
        String[] data = new String[5];

        in.readStringArray(data);
        this.id = Long.parseLong(data[0]);
        this.title = data[1];
        this.artist = data[3];
        this.album = data[4];
        this.PID = Integer.parseInt(data[5]);
        this.duration = Long.parseLong(data[6]);
    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {
                Long.toString(this.id),
                this.title,
                this.artist,
                this.album,
                Integer.toString(this.PID),
                Long.toString(this.duration)
        });
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

}
