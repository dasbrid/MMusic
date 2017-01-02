package asbridge.me.uk.MMusic.cursors;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * Created by AsbridgeD on 21/12/2016.
 */
public class SongCursor {
    public static final Uri uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
    final static String _ID = MediaStore.Audio.Media._ID;
    final static String TITLE = MediaStore.Audio.Media.TITLE;
    final static String ARTIST = MediaStore.Audio.Albums.ARTIST;

    public static Cursor getSongsCursor(Context context, String artistName) {
        final Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        final String[] cursorColumns={_ID,TITLE, ARTIST};
        String selection = null;
        String[] selectionArgs = null;
        if (artistName != null && !artistName.isEmpty()) {
            selection = MediaStore.Audio.Media.ARTIST + "=?";
            selectionArgs = new String [1];
            selectionArgs[0] = artistName;
        }
        ContentResolver cr = context.getContentResolver();
        return cr.query(uri, cursorColumns, selection, selectionArgs, null);
    }

    public static Cursor getFilteredSongsCursor(Context context, String artistName, String filterString) {
        final Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        final String[] cursorColumns={_ID,TITLE, ARTIST};
        String selection;
        String [] selectionArgs;
        if (artistName != null && !artistName.isEmpty()) {
            selection = TITLE +" LIKE ? AND " + ARTIST + " LIKE ?";
            selectionArgs = new String [2];
            selectionArgs[0] = "%" + filterString + "%";
            selectionArgs[1] = "%" + artistName + "%";
        } else {
            selection = TITLE +" LIKE ?";
            selectionArgs = new String [1];
            selectionArgs[0] = "%" + filterString + "%";
        }

        ContentResolver cr = context.getContentResolver();
        return cr.query(uri, cursorColumns, selection, selectionArgs, null);
    }

}
