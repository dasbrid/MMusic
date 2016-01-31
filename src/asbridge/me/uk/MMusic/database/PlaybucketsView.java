package asbridge.me.uk.MMusic.database;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by AsbridgeD on 18/01/2016.
 */


// This describes the DB view for playbuckets, which lists playbuckets with the number of songs in each
public final class PlaybucketsView {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public PlaybucketsView() {}

    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";

        public static final String VIEW_NAME = "playbucketsview";
        public static final String COLUMN_NAME_PLAYBUCKET_ID = PlaybucketsTable.COLUMN_NAME_PLAYBUCKET_ID;
        public static final String COLUMN_NAME_PLAYBUCKET_NAME = PlaybucketsTable.COLUMN_NAME_PLAYBUCKET_NAME;
        public static final String COLUMN_NAME_NUMSONGS = "numsongs";

    // string to create the table
    private static final String SQL_CREATE_PLAYBUCKETS_VIEW =
            "CREATE VIEW " + VIEW_NAME + " AS " +
                    "select " +
                    PlaybucketsTable.COLUMN_NAME_PLAYBUCKET_ID + " AS " + COLUMN_NAME_PLAYBUCKET_ID +
                    ", " + PlaybucketsTable.COLUMN_NAME_PLAYBUCKET_NAME + " AS " + COLUMN_NAME_PLAYBUCKET_NAME +
                    ", (select count(*) from " + PlaylistSongsTable.TABLE_NAME + "  where " + PlaylistSongsTable.TABLE_NAME +"." + PlaylistSongsTable.COLUMN_NAME_PLAYLIST_ID + " = " + PlaybucketsTable.TABLE_NAME + "." + PlaybucketsTable.COLUMN_NAME_PLAYBUCKET_ID + ") AS " + COLUMN_NAME_NUMSONGS +
                    " from " + PlaybucketsTable.TABLE_NAME + ";";

    // string to drop the table
    private static final String SQL_DROP_PLAYBUCKETS_VIEW = "DROP VIEW IF EXISTS " + VIEW_NAME;

    // static method to create the table in the specified DB
    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(SQL_CREATE_PLAYBUCKETS_VIEW);
    }

    // static method to drop and create the table in the specified DB
    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL(SQL_DROP_PLAYBUCKETS_VIEW);
        onCreate(database);
    }
}
