package asbridge.me.uk.MMusic.database;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by AsbridgeD on 18/01/2016.
 */


// This describes the DB table playbucketsongs
// stores which songs are in which playbuckets
public final class PlaybucketsTable {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public PlaybucketsTable() {}

    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";

        public static final String TABLE_NAME = "playbuckets";
        public static final String COLUMN_NAME_PLAYBUCKET_ID = "_id";
        public static final String COLUMN_NAME_PLAYBUCKET_NAME = "playbucketname";

    // string to create the table
    private static final String SQL_CREATE_PLAYBUCKETS_TABLE =
            "CREATE TABLE " + PlaybucketsTable.TABLE_NAME + " (" +
                    PlaybucketsTable.COLUMN_NAME_PLAYBUCKET_ID + " INTEGER PRIMARY KEY autoincrement," +
                    PlaybucketsTable.COLUMN_NAME_PLAYBUCKET_NAME + TEXT_TYPE +
                    " )";

    // string to drop the table
    private static final String SQL_DROP_PLAYBUCKETS_TABLE = "DROP TABLE IF EXISTS " + PlaybucketsTable.TABLE_NAME;

    // static method to create the table in the specified DB
    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(SQL_CREATE_PLAYBUCKETS_TABLE);
    }

    // static method to drop and create the table in the specified DB
    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL(SQL_DROP_PLAYBUCKETS_TABLE);
        onCreate(database);
    }
}
