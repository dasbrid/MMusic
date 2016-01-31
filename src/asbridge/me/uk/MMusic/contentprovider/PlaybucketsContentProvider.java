package asbridge.me.uk.MMusic.contentprovider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;
import asbridge.me.uk.MMusic.database.PlaylistSongsTable;
import asbridge.me.uk.MMusic.database.PlaylistsDatabaseHelper;
import asbridge.me.uk.MMusic.database.PlaybucketsTable;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by AsbridgeD on 18/01/2016.
 */
public class PlaybucketsContentProvider extends ContentProvider {

    private final static String TAG = "ContentProvider";

    // database
    private PlaylistsDatabaseHelper database;

    // used for the UriMacher
    private static final int SONGS_IN_PLAYLIST = 10;
    private static final int SONGS = 20;
    private static final int PLAYLISTS = 30;
    private static final int PLAYLISTID = 40;

    private static final String AUTHORITY = "asbridge.me.uk.mmusic";

    private static final String BASE_PATH_SONGS = "songs";
    private static final String BASE_PATH_PLAYLISTS = "playlists";

    public static final Uri CONTENT_URI_SONGS = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH_SONGS);
    public static final Uri CONTENT_URI_PLAYLISTS = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH_PLAYLISTS);

    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/playlists";
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/playlist";

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_SONGS, SONGS);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_SONGS + "/#", SONGS_IN_PLAYLIST);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_PLAYLISTS, PLAYLISTS);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_PLAYLISTS + "/#", PLAYLISTID);
    }

    @Override
    public boolean onCreate() {
        database = new PlaylistsDatabaseHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
//        checkColumns(projection); // check if the caller has requested a column which does not exists


        Log.d(TAG, "query:"+uri.toString());
        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case SONGS_IN_PLAYLIST:
                Log.d(TAG, "query SONGS_IN_PLAYLIST");
                queryBuilder.setTables(PlaylistSongsTable.TABLE_NAME); // Set the table
                // adding the ID to the original query
                queryBuilder.appendWhere(PlaylistSongsTable.COLUMN_NAME_PLAYLIST_ID + "="
                        + uri.getLastPathSegment());
                break;
            case SONGS:
                Log.d(TAG, "query SONGS");
                queryBuilder.setTables(PlaylistSongsTable.TABLE_NAME); // Set the table
                break;
            case PLAYLISTS:
                Log.d(TAG, "query PLAYLISTS");
                queryBuilder.setTables(PlaybucketsTable.TABLE_NAME); // Set the table
                break;
            default:
                throw new IllegalArgumentException("query unknown URI: " + uri);
        }

        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);
        // make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        long id = 0;
        switch (uriType) {
            case SONGS:
                Log.d(TAG, "insert SONGS");
                id = sqlDB.insert(PlaylistSongsTable.TABLE_NAME, null, values);
                break;
            case PLAYLISTS:
                Log.d(TAG, "insert PLAYLISTS");
                id = sqlDB.insert(PlaybucketsTable.TABLE_NAME, null, values);
                break;
            default:
                throw new IllegalArgumentException("insert unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH_SONGS + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted = 0;
        switch (uriType) {
            case SONGS:
                Log.v(TAG, "delete SONGS");
                rowsDeleted = sqlDB.delete(PlaylistSongsTable.TABLE_NAME, selection,
                        selectionArgs);
                break;
            case PLAYLISTID:
                String playlistIDString = uri.getLastPathSegment();
                Log.d(TAG, "delete PLAYLIST " + playlistIDString);
                String deleteSongsSelection = PlaylistSongsTable.COLUMN_NAME_PLAYLIST_ID + "=?";
                String deletePlaybucketSelection = PlaybucketsTable.COLUMN_NAME_PLAYBUCKET_ID + "=?";
                String[] deleteSelectionArgs = {playlistIDString};
                rowsDeleted = sqlDB.delete(PlaylistSongsTable.TABLE_NAME, deleteSongsSelection,
                        deleteSelectionArgs);
                rowsDeleted = sqlDB.delete(PlaybucketsTable.TABLE_NAME, deletePlaybucketSelection,
                        deleteSelectionArgs);
                break;
            default:
                throw new IllegalArgumentException("delete unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsUpdated = 0;
        throw new IllegalArgumentException("update unknown URI: " + uri);
    }

    private void checkColumns(String[] projection) {
        String[] available = {
                "count(*)",
                PlaylistSongsTable.COLUMN_NAME_PLAYLIST_ID,
                PlaylistSongsTable.COLUMN_NAME_SONG_ID,
                PlaylistSongsTable.COLUMN_ID };
        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
            // check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }
}
