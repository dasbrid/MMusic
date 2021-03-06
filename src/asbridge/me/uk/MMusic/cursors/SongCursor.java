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
    private static final Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

    public final static String _ID = MediaStore.Audio.Media._ID;
    public final static String TITLE = MediaStore.Audio.Media.TITLE;
    public final static String ARTIST = MediaStore.Audio.Albums.ARTIST;

    private final static String[] cursorColumns={_ID,TITLE, ARTIST};

    private static final String orderby = TITLE + " COLLATE NOCASE";

    public static Cursor getSongsCursorForArtist(Context context, String artistName) {

        String selection = null;
        String[] selectionArgs = null;
        if (artistName != null && !artistName.isEmpty()) {
            selection = MediaStore.Audio.Media.ARTIST + "=?";
            selectionArgs = new String [1];
            selectionArgs[0] = artistName;
        }
        ContentResolver cr = context.getContentResolver();
        return cr.query(uri, cursorColumns, selection, selectionArgs, orderby);
    }

    public static Cursor getSongsCursorForAlbum(Context context, String artistName) {

        String selection = null;
        String[] selectionArgs = null;
        if (artistName != null && !artistName.isEmpty()) {
            selection = MediaStore.Audio.Media.ALBUM + "=?";
            selectionArgs = new String [1];
            selectionArgs[0] = artistName;
        }
        ContentResolver cr = context.getContentResolver();
        return cr.query(uri, cursorColumns, selection, selectionArgs, orderby);
    }



    public static Cursor getFilteredSongsCursorForArtist(Context context, String artistName, String filterString) {
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
        return cr.query(uri, cursorColumns, selection, selectionArgs, orderby);
    }

    public static Cursor getFilteredSongsCursorForAlbum(Context context, String artistName, String filterString) {
        String selection;
        String [] selectionArgs;
        if (artistName != null && !artistName.isEmpty()) {
            selection = TITLE +" LIKE ? AND " + MediaStore.Audio.Media.ALBUM + " LIKE ?";
            selectionArgs = new String [2];
            selectionArgs[0] = "%" + filterString + "%";
            selectionArgs[1] = "%" + artistName + "%";
        } else {
            selection = TITLE +" LIKE ?";
            selectionArgs = new String [1];
            selectionArgs[0] = "%" + filterString + "%";
        }

        ContentResolver cr = context.getContentResolver();
        return cr.query(uri, cursorColumns, selection, selectionArgs, orderby);
    }

}
