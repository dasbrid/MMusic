package asbridge.me.uk.MMusic.database;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by AsbridgeD on 18/01/2016.
 */
public final class PlaylistsTable {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public PlaylistsTable() {}

    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";

        public static final String TABLE_NAME = "playlists";
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_NAME_PLAYLIST_ID = "playlistid";
        public static final String COLUMN_NAME_SONG_ID = "songid";


    private static final String SQL_CREATE_PLAYLIST_TABLE =
            "CREATE TABLE " + PlaylistsTable.TABLE_NAME + " (" +
                    PlaylistsTable.COLUMN_ID + " INTEGER PRIMARY KEY," +
                    PlaylistsTable.COLUMN_NAME_PLAYLIST_ID + INTEGER_TYPE + COMMA_SEP +
                    PlaylistsTable.COLUMN_NAME_SONG_ID + INTEGER_TYPE + COMMA_SEP +
                    " )";

    private static final String SQL_DROP_PLAYLIST_TABLE = "DROP TABLE IF EXISTS " + PlaylistsTable.TABLE_NAME;

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(SQL_CREATE_PLAYLIST_TABLE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {
        database.execSQL(SQL_DROP_PLAYLIST_TABLE);

        // create fresh playlist table
        onCreate(database);
    }
}
