package asbridge.me.uk.MMusic.cursors;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * Created by AsbridgeD on 21/12/2016.
 */
public class ArtistCursor  {
    public static final Uri uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
    public static final String _ID = MediaStore.Audio.Artists._ID;
    public static final String NUM_ITEMS_COLUMN = MediaStore.Audio.Artists.NUMBER_OF_TRACKS;
//    public static final String ARTIST = MediaStore.Audio.Artists.ARTIST;

    public static final String NAME_COLUMN = MediaStore.Audio.Artists.ARTIST;

    private static final String[] cursorColumns={_ID, NUM_ITEMS_COLUMN, NAME_COLUMN};
    private static final String orderby = NAME_COLUMN + " COLLATE NOCASE";




    public static Cursor getCursor(Context context) {
        final String where = null;
        ContentResolver cr = context.getContentResolver();
        return cr.query(uri, cursorColumns, where, null, orderby);
    }

    public static Cursor getFilteredCursor(Context context, String filterString) {
        final String where = NAME_COLUMN+" LIKE ?";
        final String [] whereArgs = {"%" + filterString + "%"};
        ContentResolver cr = context.getContentResolver();
        return cr.query(uri, cursorColumns, where, whereArgs, orderby);
    }

}
