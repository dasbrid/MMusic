package asbridge.me.uk.MMusic.contentprovider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import asbridge.me.uk.MMusic.database.PlaylistsDatabaseHelper;
import asbridge.me.uk.MMusic.database.PlaylistsTable;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by AsbridgeD on 18/01/2016.
 */
public class PlaylistsContentProvider  extends ContentProvider {

    private final static String TAG = "ContentProvider";

    // database
    private PlaylistsDatabaseHelper database;

    // used for the UriMacher
    private static final int PLAYLISTS = 10;
    private static final int TODO_ID = 20;
    private static final int SONGS = 30;
    private static final int SONG_BY_ID = 40;

    private static final String AUTHORITY = "asbridge.me.uk.mmusic.playlists.contentprovider";

    private static final String BASE_PATH_PLAYLISTS = "playlists";
    private static final String BASE_PATH_SONG = "song";

    public static final Uri CONTENT_URI_PLAYLISTS = Uri.parse("content://" + AUTHORITY
            + "/" + BASE_PATH_PLAYLISTS);
    public static final Uri CONTENT_URI_SONGS = Uri.parse("content://" + AUTHORITY
            + "/" + BASE_PATH_SONG);

    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/playlists";
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/playlist";

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_PLAYLISTS, PLAYLISTS);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_PLAYLISTS + "/#", TODO_ID);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_SONG, SONGS);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_SONG + "/#", SONG_BY_ID);
    }

    @Override
    public boolean onCreate() {
        database = new PlaylistsDatabaseHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        // Uisng SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // check if the caller has requested a column which does not exists
        checkColumns(projection);

        // Set the table
        queryBuilder.setTables(PlaylistsTable.TABLE_NAME);
        Log.d(TAG, "query:"+uri.toString());
        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case SONG_BY_ID:
                Log.d(TAG, "SONG_BY_ID");
                queryBuilder.appendWhere(/*PlaylistsTable.COLUMN_NAME_PLAYLIST_ID + "=0 AND " + */PlaylistsTable.COLUMN_NAME_SONG_ID + "="
                        + uri.getLastPathSegment());
                break;
            case TODO_ID:
                Log.d(TAG, "TODO_ID");
                // adding the ID to the original query
                queryBuilder.appendWhere(PlaylistsTable.COLUMN_NAME_PLAYLIST_ID + "="
                        + uri.getLastPathSegment());
                break;
            case PLAYLISTS:
                Log.d(TAG, "TODO");
                break;
            case SONGS:
                Log.d(TAG, "SONGS");
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
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
            case PLAYLISTS:
                id = sqlDB.insert(PlaylistsTable.TABLE_NAME, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH_PLAYLISTS + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted = 0;
        switch (uriType) {
            case PLAYLISTS:
                rowsDeleted = sqlDB.delete(PlaylistsTable.TABLE_NAME, selection,
                        selectionArgs);
                break;
            case TODO_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(PlaylistsTable.TABLE_NAME,
                            PlaylistsTable.COLUMN_ID + "=" + id,
                            null);
                } else {
                    rowsDeleted = sqlDB.delete(PlaylistsTable.TABLE_NAME,
                            PlaylistsTable.COLUMN_ID + "=" + id
                                    + " and " + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
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
        switch (uriType) {
            case PLAYLISTS:
                rowsUpdated = sqlDB.update(PlaylistsTable.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            case TODO_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(PlaylistsTable.TABLE_NAME,
                            values,
                            PlaylistsTable.COLUMN_ID + "=" + id,
                            null);
                } else {
                    rowsUpdated = sqlDB.update(PlaylistsTable.TABLE_NAME,
                            values,
                            PlaylistsTable.COLUMN_ID + "=" + id
                                    + " and "
                                    + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private void checkColumns(String[] projection) {
        String[] available = {
                "count(*)",
                PlaylistsTable.COLUMN_NAME_PLAYLIST_ID,
                PlaylistsTable.COLUMN_NAME_SONG_ID,
                PlaylistsTable.COLUMN_ID };
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
