package asbridge.me.uk.MMusic.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.location.Address;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import asbridge.me.uk.MMusic.classes.Song;
import asbridge.me.uk.MMusic.contentprovider.PlaylistsContentProvider;
import asbridge.me.uk.MMusic.database.PlaylistsDatabaseHelper;
import asbridge.me.uk.MMusic.database.PlaylistsTable;

import java.util.ArrayList;

/**
 * Created by David on 05/12/2015.
 */
public class MusicContent {

    private static String TAG = "DAVE:MusicContent";

    public static void getArtists(Context context, ArrayList<String> artistList) {
        //retrieve song info
        ContentResolver musicResolver = context.getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {
                MediaStore.Audio.Media.ARTIST,
        };

        String sortOrder = MediaStore.Audio.Media.ARTIST + " ASC";
        String groupBy = "1) GROUP BY (1"; // this is really WHERE (1) GROUP BY (1)
        Cursor musicCursor = musicResolver.query(musicUri, projection, groupBy, null, sortOrder);
        Log.d(TAG, "got artists from ContentResolver, count = " + (musicCursor == null ? null : musicCursor.getCount()));
        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns
            int artistColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST);
            //add items to list
            do {
                String thisArtist = musicCursor.getString(artistColumn);
                // Log.d(TAG, "adding song, title = " + thisTitle);
                artistList.add(thisArtist);
            }
            while (musicCursor.moveToNext());
        }
    }


    public static void getAllSongs(Context context, ArrayList<Song> songList) {
        String selection = null;
        String[] selectionargs = null;
        String sortOrder = MediaStore.Audio.Media.ARTIST + " ASC";
        getSongs(context, selection, selectionargs, sortOrder, songList);
    }

    public static void getSongsForGivenArtistList(Context context, ArrayList<String> artistList, ArrayList<Song> songList) {
        for (String artist : artistList) {
            getSongsForGivenArtist(context, artist, songList);
        }
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
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

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
                    (MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST);
            int albumColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisAlbum = musicCursor.getString(albumColumn);
                // Log.d(TAG, "adding song, title = " + thisTitle);
                songList.add(new Song(thisId, thisTitle, thisArtist, thisAlbum, -1));
            } while (musicCursor.moveToNext());
            // always close the cursor
            musicCursor.close();
        }
    }

    public static void addSongToPlaylist(Context context, int playlistID, long songID) {
        Log.d(TAG,"addsong "+ songID + " to playlist "+playlistID);
        // Defines a new Uri object that receives the result of the insertion
        Uri mNewUri;
        // Defines an object to contain the new values to insert
        ContentValues mNewValues = new ContentValues();

        /*
         * Sets the values of each column and inserts the word. The arguments to the "put"
         * method are "column name" and "value"
         */
        mNewValues.put(PlaylistsTable.COLUMN_NAME_PLAYLIST_ID, playlistID);
        mNewValues.put(PlaylistsTable.COLUMN_NAME_SONG_ID, songID);

        mNewUri = context.getContentResolver().insert(
                PlaylistsContentProvider.CONTENT_URI,   // the user dictionary content URI
                mNewValues                          // the values to insert
        );
    }


    public static ArrayList<Long> getSongsInPlaylist(Context context, int playlistID) {
        Uri uri = Uri.parse(PlaylistsContentProvider.CONTENT_URI + "/#" + playlistID);
        ArrayList<Long> songIDs = new ArrayList<>();
        Log.d(TAG, "getting playlist "+playlistID);
        String[] projection = {PlaylistsTable.COLUMN_NAME_PLAYLIST_ID, PlaylistsTable.COLUMN_NAME_SONG_ID };

        Cursor cursor = context.getContentResolver().query(uri, projection, null, null,
                null);

        int songIDColumn = cursor.getColumnIndex(PlaylistsTable.COLUMN_NAME_SONG_ID);

        if (cursor != null  && cursor.moveToFirst()) {

            do {
                long songID = cursor.getInt(songIDColumn);
                Log.d(TAG, "  playlist " + playlistID  + " contains song "+songID);
                songIDs.add(songID);
            } while (cursor.moveToNext());

            // always close the cursor
            cursor.close();
        }
        return songIDs;
    }
}
