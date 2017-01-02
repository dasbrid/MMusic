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
    public static final String NUMBER_OF_TRACKS = MediaStore.Audio.Artists.NUMBER_OF_TRACKS;
    public static final String ARTIST = MediaStore.Audio.Artists.ARTIST;

    public static Cursor getArtistsCursor(Context context) {
        final Uri uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
        final String[] cursorColumns={_ID, NUMBER_OF_TRACKS, ARTIST};
        final String where = null;
        ContentResolver cr = context.getContentResolver();
        return cr.query(uri, cursorColumns, where, null, null);
    }

    public static Cursor getFilteredArtistsCursor(Context context, String filterString) {
        final Uri uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
        final String[] cursorColumns={_ID, NUMBER_OF_TRACKS, ARTIST};
        final String where = ARTIST+" LIKE ?";
        final String [] whereArgs = {"%" + filterString + "%"};
        ContentResolver cr = context.getContentResolver();
        return cr.query(uri, cursorColumns, where, whereArgs, null);
    }

}
