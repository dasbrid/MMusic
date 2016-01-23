package asbridge.me.uk.MMusic.database;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by AsbridgeD on 18/01/2016.
 */


// This describes the DB table playlistsongs
// stores which songs are in which playlists
public final class PlaylistSongsTable {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public PlaylistSongsTable() {}

    // private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";

        public static final String TABLE_NAME = "playlistsongs";
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_NAME_PLAYLIST_ID = "playlistid";
        public static final String COLUMN_NAME_SONG_ID = "songid";

    // string to create the table
    private static final String SQL_CREATE_PLAYLIST_SONGS_TABLE =
            "CREATE TABLE " + PlaylistSongsTable.TABLE_NAME + " (" +
                    PlaylistSongsTable.COLUMN_ID + " INTEGER PRIMARY KEY autoincrement," +
                    PlaylistSongsTable.COLUMN_NAME_PLAYLIST_ID + INTEGER_TYPE + COMMA_SEP +
                    PlaylistSongsTable.COLUMN_NAME_SONG_ID + INTEGER_TYPE +
                    " )";

    // string to drop the table
    private static final String SQL_DROP_PLAYLIST_TABLE = "DROP TABLE IF EXISTS " + PlaylistSongsTable.TABLE_NAME;

    // static method to create the table in the specified DB
    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(SQL_CREATE_PLAYLIST_SONGS_TABLE);
    }

    // static method to drop and create the table in the specified DB
    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL(SQL_DROP_PLAYLIST_TABLE);
        onCreate(database);
    }
}
