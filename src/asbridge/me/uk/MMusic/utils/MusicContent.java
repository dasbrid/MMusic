package asbridge.me.uk.MMusic.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import asbridge.me.uk.MMusic.classes.Song;
import asbridge.me.uk.MMusic.contentprovider.PlaybucketsContentProvider;
import asbridge.me.uk.MMusic.database.PlaybucketsView;
import asbridge.me.uk.MMusic.database.PlaylistSongsTable;
import asbridge.me.uk.MMusic.database.PlaylistsDatabaseHelper;
import asbridge.me.uk.MMusic.database.PlaybucketsTable;

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

    // Does DB call to just remove all the songs in the current playlist
    // Used when doing 'select none'
    public static void removeAllSongsFromCurrentPlaylist(Context context) {
        Log.d(TAG, "Remove all songs from current playlist");

        String selection = PlaylistSongsTable.COLUMN_NAME_PLAYLIST_ID + " = ?";
        String[] selectionArgs = {"0"};

        int numDeleted;
        numDeleted = context.getContentResolver().delete(
                PlaybucketsContentProvider.CONTENT_URI_SONGS,
                selection,
                selectionArgs);
    }

    public static void removeSongsFromCurrentPlaylist(Context context, ArrayList<Song> songs) {
        Log.d(TAG, "Remove "+ songs.size()+ " songs from current playlist");
        if (songs.size()==0) { return; }
        int i = 0;
        String inClause = "";
        for (; i < (songs.size() -1) ; i++) {
            inClause += songs.get(i).getID();
            inClause += ",";
        }
        inClause += songs.get(i).getID();

        String selection = PlaylistSongsTable.COLUMN_NAME_PLAYLIST_ID + " = ? AND " + PlaylistSongsTable.COLUMN_NAME_SONG_ID + " in (" + inClause + ")";

        String[] selectionArgs = {"0"};
        Log.d(TAG, "Inclause = "+ inClause + "... selection"+ selection);
        int numDeleted;
        numDeleted = context.getContentResolver().delete(
                PlaybucketsContentProvider.CONTENT_URI_SONGS,
                selection,
                selectionArgs);

    }

    public static void removeSongFromCurrentPlaylist(Context context, Song song) {
        String[] selectionArgs = {"0", Long.toString(song.getID())};

        int numDeleted;
        numDeleted = context.getContentResolver().delete(
                PlaybucketsContentProvider.CONTENT_URI_SONGS,
                PlaylistSongsTable.COLUMN_NAME_PLAYLIST_ID + " = ? AND " + PlaylistSongsTable.COLUMN_NAME_SONG_ID + " = ?",
                selectionArgs);

    }

    public static void addSongsToCurrentPlaylist(Context context, ArrayList<Song> songsToAdd) {
        Log.d(TAG, "Add "+ songsToAdd.size()+ " songs to current playlist");
        if (songsToAdd.size()==0) { return; }

        PlaylistsDatabaseHelper database = new PlaylistsDatabaseHelper(context);
        SQLiteDatabase db = database.getWritableDatabase();

        db.beginTransactionNonExclusive(); // allows queries by other threads
        try {
            SQLiteStatement stmt = db.compileStatement(
                    "INSERT INTO '" + PlaylistSongsTable.TABLE_NAME + "'('" + PlaylistSongsTable.COLUMN_NAME_PLAYLIST_ID + "', '" + PlaylistSongsTable.COLUMN_NAME_SONG_ID + "') VALUES (?, ?);"
            );
            for(Song songToAdd : songsToAdd){
                stmt.bindLong(1, 0);
                stmt.bindLong(2, songToAdd.getID());
                stmt.executeInsert();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }

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
                PlaybucketsContentProvider.CONTENT_URI_SONGS,
                mNewValues                          // the values to insert
        );
    }

    public static ArrayList<Long> getSongsInPlaylist(Context context, int playlistID) {
        Uri uri = Uri.parse(PlaybucketsContentProvider.CONTENT_URI_SONGS + "/" + playlistID);
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

        Uri uri = Uri.parse(PlaybucketsContentProvider.CONTENT_URI_SONGS + "/" + playlistID);
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

    // Update an existing playbucket to be the same as the current playbucket (save the current playbucket)
    // Do the insert new then delete old trick (could just clear the savedPlaybucket and copy everything from current
    // Note: Playbucket 0 is the current playbucket
    public static void updateSavedPlaybucket(Context context, int savePlaybucketID) {
        PlaylistsDatabaseHelper database;
        database = new PlaylistsDatabaseHelper(context);
        SQLiteDatabase db = database.getWritableDatabase();
        db.beginTransactionNonExclusive(); // allows queries by other threads
        try {
            String selectQuery = "insert into playlistsongs select NULL, "+ savePlaybucketID + ", songid from playlistsongs pids where playlistid = 0 and not exists (select 1 from playlistsongs where playlistid = " + savePlaybucketID + " AND songid = pids.songid);";
            db.execSQL(selectQuery);

            selectQuery = "delete from playlistsongs where playlistsongs.playlistid = "+ savePlaybucketID + " and not exists (select 1 from playlistsongs AS pids where pids.playlistid = 0 and playlistsongs.songid = pids.songid);";
            db.execSQL(selectQuery);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }


    }

    // delete a given playbucket from the database
    public static void deletePlaybucket(Context context, int deletePlaybucketID) {
        Uri uri = Uri.parse(PlaybucketsContentProvider.CONTENT_URI_PLAYLISTS + "/" + deletePlaybucketID);
        context.getContentResolver().delete(uri, null, null);
    }

    // Create a new playbucket with the given name and the list of songs to it
    // TODO: Transactionify, more complicated as we have a content provider call
    // to add the new playlist, then a call to an existing transactionified method
    // to add the current songs to the playlist.
    public static void createNewBucket(Context context, String playlistName) {
        // First insert the playlist
        Uri mNewUri; // result of insertion, returns the id of the created playbucket

        // Defines an object to contain the new values to insert
        ContentValues mNewValues = new ContentValues();

        mNewValues.put(PlaybucketsTable.COLUMN_NAME_PLAYBUCKET_NAME, playlistName);

        mNewUri = context.getContentResolver().insert(
                PlaybucketsContentProvider.CONTENT_URI_PLAYLISTS,
                mNewValues                          // the values to insert
        );

        // use mNewUri to get the recently inserted playlist
        String newPlayBucketidString = mNewUri.getLastPathSegment();
        Log.d(TAG, "NEW PLAYLIST HAS ID:"+newPlayBucketidString);
        int newPlayBucketID = Integer.parseInt(newPlayBucketidString);

        // just update to the current
        updateSavedPlaybucket(context, newPlayBucketID);
    }

    // Return a cursor of playlists, used to display a list of all the playbuckets
    public static Cursor getPlaybucketsCursor(Context context) {
        Uri uri = PlaybucketsContentProvider.CONTENT_URI_PLAYBUCKETSVIEW;
        String[] projection = {PlaybucketsView.COLUMN_NAME_PLAYBUCKET_ID, PlaybucketsView.COLUMN_NAME_PLAYBUCKET_NAME, PlaybucketsView.COLUMN_NAME_NUMSONGS };
        String selection = null;
        String[] selectionArgs = null;
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        return cursor;
    }

    public static ArrayList<Integer> getPlaylists(Context context) {
        ArrayList<Integer> playlistIDs = new ArrayList<>();
        Cursor cursor = getPlaybucketsCursor(context);

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

    // used when we load a saved bucket and want to update the current playbucket
    // First inserts all songs in the saved bucket NOT in the current bucket.
    // Then deletes all songs in current bucket but NOT in saved bucket.
    // Would be possible just to clear the current bucket then insert everything in from the saved bucket.
    public static void setCurrentBucketFromSavedBucket(Context context, int playBucketID) {
        PlaylistsDatabaseHelper database;
        database = new PlaylistsDatabaseHelper(context);
        SQLiteDatabase db = database.getWritableDatabase();
        db.beginTransactionNonExclusive(); // allows queries by other threads
        try {
            String selectQuery = "insert into playlistsongs select NULL, 0, songid from playlistsongs pids where playlistid = " + playBucketID + " and not exists (select 1 from playlistsongs where playlistid = 0 AND songid = pids.songid);";
            db.execSQL(selectQuery);

            selectQuery = "delete from playlistsongs where playlistsongs.playlistid = 0 and not exists (select 1 from playlistsongs AS pids where pids.playlistid = " + playBucketID + " and playlistsongs.songid = pids.songid);";
            db.execSQL(selectQuery);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }
}
