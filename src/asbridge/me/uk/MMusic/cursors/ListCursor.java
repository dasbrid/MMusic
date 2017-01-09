package asbridge.me.uk.MMusic.cursors;

import android.database.Cursor;

/**
 * Created by asbridged on 09/01/2017.
 * Gathers together all things to do with a list of 'things'
 * Sharing a common layout. Could be list of artists or albums
 * Both using ArtistListAdapter for display
 */
public class ListCursor {
    public Cursor cursor;
    public String nameColumn;
    public String numItemsColumn;
}
