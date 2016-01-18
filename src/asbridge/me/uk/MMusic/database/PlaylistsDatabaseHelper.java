package asbridge.me.uk.MMusic.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by AsbridgeD on 18/01/2016.
 */
public class PlaylistsDatabaseHelper extends SQLiteOpenHelper {



    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "MusicDB";



    private static final String SQL_DROP_PLAYLIST_TABLE = "DROP TABLE IF EXISTS " + PlaylistsTable.TABLE_NAME;

    public PlaylistsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // create playlists table
        PlaylistsTable.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        PlaylistsTable.onUpgrade(db, oldVersion, newVersion);
    }

}
