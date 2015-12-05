package asbridge.me.uk.MMusic.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import asbridge.me.uk.MMusic.classes.Song;

import java.util.ArrayList;

/**
 * Created by David on 05/12/2015.
 */
public class Content {

    private static String TAG = "DAVE:Content";

    public static void getAllSongs(Context context, ArrayList<Song> songList) {
        String selection = null;
        String[] selectionargs = null;
        String sortOrder = MediaStore.Audio.Media.ARTIST + " ASC";
        getSongs(context, selection, selectionargs, sortOrder, songList);
    }

    public static void getSongsForGivenArtist(Context context, String artist, ArrayList<Song> songList) {
        String selection = MediaStore.Audio.Media.ARTIST + "=?";
        String[] selectionargs = new String[1];
        selectionargs[0] = artist;
        getSongs(context, selection, selectionargs, songList);
    }

    public static void getAllSongsByAlbum(Context context, String album, ArrayList<Song> songList) {
        String selection = MediaStore.Audio.Media.ALBUM + "=?";
        String[] selectionargs = new String[1];
        selectionargs[0] = album;
        getSongs(context, selection, selectionargs, songList);
    }

    // generic method called after the selection has been setup, ordered by title
    public static void getSongs(Context context, String selection, String[] selectionArgs, ArrayList<Song> songList) {
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        getSongs(context, selection, selectionArgs, sortOrder, songList);
    }

        // generic method called after the selection and order has been setup
    public static void getSongs(Context context, String selection, String[] selectionArgs, String sortOrder, ArrayList<Song> songList) {
        //retrieve song info
        ContentResolver musicResolver = context.getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,    // filepath of the audio file
                MediaStore.Audio.Media._ID,     // context id/ uri id of the file
        };

        Cursor musicCursor = musicResolver.query(musicUri, projection, selection, selectionArgs, sortOrder);
        Log.d(TAG, "got songs from ContentResolver, count = " + (musicCursor == null ? null : musicCursor.getCount()));
        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int albumColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisAlbum = musicCursor.getString(albumColumn);
                // Log.d(TAG, "adding song, title = " + thisTitle);
                songList.add(new Song(thisId, thisTitle, thisArtist, thisAlbum));
            }
            while (musicCursor.moveToNext());
        }
    }

}
