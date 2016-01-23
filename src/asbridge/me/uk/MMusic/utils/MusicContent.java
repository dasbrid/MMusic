package asbridge.me.uk.MMusic.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import asbridge.me.uk.MMusic.classes.Song;
import asbridge.me.uk.MMusic.contentprovider.PlaylistsContentProvider;
import asbridge.me.uk.MMusic.database.PlaylistSongsTable;
import asbridge.me.uk.MMusic.database.PlaylistsDatabaseHelper;

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

    public static Song getSongBySongID(Context context, long songID) {
        String selection = MediaStore.Audio.Media._ID + "=?";
        String[] selectionargs = new String[1];
        selectionargs[0] = Long.toString(songID);
        ArrayList<Song> songList = new ArrayList<>();
        getSongs(context, selection, selectionargs, songList);
        if (songList.size() == 0) {
            return null;
        } else if (songList.size() > 1) {
            Log.e(TAG, "More than one song with ID = " + songID);
            return songList.get(0);
            //throw new Exception("More than one song with ID = " + songID);
        }
        return songList.get(0);
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

    public static void removeSongFromCurrentPlaylist(Context context, Song song) {
        String[] selectionArgs = {"0", Long.toString(song.getID())};

        int numDeleted;
        numDeleted = context.getContentResolver().delete(
                PlaylistsContentProvider.CONTENT_URI_SONGS,
                PlaylistSongsTable.COLUMN_NAME_PLAYLIST_ID + " = ? AND " + PlaylistSongsTable.COLUMN_NAME_SONG_ID + " = ?",
                selectionArgs);

    }

    public static void addSongToCurrentPlaylist(Context context, Song song) {
        addSongToPlaylist(context, 0 /*current playlist*/, song.getID());
    }

    public static void addSongToPlaylist(Context context, int playlistID, long songID) {
        Uri mNewUri; // result of the insertion, not used here

        // Defines an object to contain the new values to insert
        ContentValues mNewValues = new ContentValues();

        mNewValues.put(PlaylistSongsTable.COLUMN_NAME_PLAYLIST_ID, playlistID);
        mNewValues.put(PlaylistSongsTable.COLUMN_NAME_SONG_ID, songID);

        mNewUri = context.getContentResolver().insert(
                PlaylistsContentProvider.CONTENT_URI_SONGS,
                mNewValues                          // the values to insert
        );
    }

    public static ArrayList<Long> getSongsInPlaylist(Context context, int playlistID) {
        Uri uri = Uri.parse(PlaylistsContentProvider.CONTENT_URI_SONGS + "/" + playlistID);
        ArrayList<Long> songIDs = new ArrayList<>();
        Log.d(TAG, "getting playlist "+playlistID+" using uri "+uri.toString());
        String[] projection = {PlaylistSongsTable.COLUMN_NAME_PLAYLIST_ID, PlaylistSongsTable.COLUMN_NAME_SONG_ID };

        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

        int songIDColumn = cursor.getColumnIndex(PlaylistSongsTable.COLUMN_NAME_SONG_ID);

        if (cursor != null  && cursor.moveToFirst()) {

            do {
                long songID = cursor.getInt(songIDColumn);
                songIDs.add(songID);
            } while (cursor.moveToNext());

            // always close the cursor
            cursor.close();
        }
        return songIDs;
    }

    public static int getNumSongsInPlaylist(Context context, int playlistID ) {

        Uri uri = Uri.parse(PlaylistsContentProvider.CONTENT_URI_SONGS + "/" + playlistID);
        Log.d(TAG, uri.toString());
        String[] projection = {"count(*)"};

        Cursor cursor = context.getContentResolver().query(uri,projection, null, null, null);

        if (cursor != null  && cursor.getCount() == 0) {
            cursor.close();
            return 0;
        } else {
            cursor.moveToFirst();
            int result = cursor.getInt(0);
            cursor.close();
            return result;
        }
    }

    /* This would be MUCH better if we didn't return ALL the songs in a playlist get the nth one*/
    /* Better to have the database / content provider so that we can get the nth song in the playlist with one query */
    // ... but it works!
    public static Song getSongInCurrentPlaylist(Context context, int  songINDEX) {
        ArrayList<Long> songIDs = getSongsInPlaylist(context, 0 /*Current playlist*/);
        Long songID = songIDs.get(songINDEX);
        Song song = getSongBySongID(context, songID);

        return song;

    }

    // add these songs to the playlist
    // we pass ID
    // TODO: pass name of new playlist and calculate playlist ID automatically
    public static void createNewPlaylist(Context context, int playlistID, ArrayList<Long> selectedSongIDs) {
        // TODO: implement bulkinsert in content provider to avoid looping here
        for (Long songID : selectedSongIDs) {
            addSongToPlaylist(context, playlistID, songID);
        }
    }

    public static ArrayList<Integer> getPlaylists(Context context) {
        Uri uri = PlaylistsContentProvider.CONTENT_URI_SONGS;
        ArrayList<Integer> playlistIDs = new ArrayList<>();
        Log.d(TAG, "getting playlists using uri "+uri.toString());
        String[] projection = {"Distinct " + PlaylistSongsTable.COLUMN_NAME_PLAYLIST_ID};
        String selection = PlaylistSongsTable.COLUMN_NAME_PLAYLIST_ID + " <> ?";
        String[] selectionArgs = new String[] {"0"};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

        int playlistIDColumn = cursor.getColumnIndex(PlaylistSongsTable.COLUMN_NAME_PLAYLIST_ID);

        if (cursor != null  && cursor.moveToFirst()) {

            do {
                int playlistID = cursor.getInt(playlistIDColumn);
                playlistIDs.add(playlistID);
            } while (cursor.moveToNext());

            // always close the cursor
            cursor.close();
        }
        return playlistIDs;
    }

    public static void setCurrentBucketFromSavedBucket(Context context, int playBucketID) {
        PlaylistsDatabaseHelper database;
        database = new PlaylistsDatabaseHelper(context);
        SQLiteDatabase db = database.getWritableDatabase();
        String[] args={ Integer.toString(playBucketID)};
        String selectQuery = "insert into playlistsongs select NULL, 0, songid from playlistsongs pids where playlistid = " + playBucketID + " and not exists (select 1 from playlistsongs where playlistid = 0 AND songid = pids.songid);";
//        String selectQuery = "insert into playlistsongs values (NULL, 0, 61);";
        Log.d(TAG, selectQuery);
        db.execSQL(selectQuery);

        selectQuery = "delete from playlistsongs where playlistsongs.playlistid = 0 and not exists (select 1 from playlistsongs AS pids where pids.playlistid = " + playBucketID + " and playlistsongs.songid = pids.songid);";
        db.execSQL(selectQuery);

    }

    /*
    public static void setCurrentPlaylist(Context context, ArrayList<Song> selectedSongs) {

        final int playlistID = 0; // current playlist
        String[] selectionArgs = {"0"};

        int numDeleted;
        numDeleted = context.getContentResolver().delete(
                PlaylistsContentProvider.CONTENT_URI_PLAYLISTS,   // the user dictionary content URI
                PlaylistSongsTable.COLUMN_NAME_PLAYLIST_ID + " = ?",
                selectionArgs);


        for (Song s : selectedSongs) {
            addSongToPlaylist(context, 0, s.getID());
        }
    }
*/
}
