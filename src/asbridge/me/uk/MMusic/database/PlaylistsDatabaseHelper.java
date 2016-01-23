package asbridge.me.uk.MMusic.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by AsbridgeD on 18/01/2016.
 */
public class PlaylistsDatabaseHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 3;
    // Database Name
    private static final String DATABASE_NAME = "MusicDB";

    private static final String SQL_DROP_PLAYLIST_TABLE = "DROP TABLE IF EXISTS " + PlaylistSongsTable.TABLE_NAME;

    public PlaylistsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Overriden from base class. Will be called if the DB does not exist
    @Override
    public void onCreate(SQLiteDatabase db) {
        // create playlists table
        PlaylistSongsTable.onCreate(db);
    }

    // Overriden from base class. Will be called if the DB has a new version
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        PlaylistSongsTable.onUpgrade(db, oldVersion, newVersion);
    }

}