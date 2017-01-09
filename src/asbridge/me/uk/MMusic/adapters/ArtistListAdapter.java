package asbridge.me.uk.MMusic.adapters;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import asbridge.me.uk.MMusic.R;
import asbridge.me.uk.MMusic.cursors.ArtistCursor;
import asbridge.me.uk.MMusic.cursors.ListCursor;

import static android.content.ContentValues.TAG;

/**
 * Created by asbridged on 20/12/2016.
 */
public class ArtistListAdapter extends CursorAdapter {

    private String numItemsColumn;
    private String nameColumn;
    private artistListActionsListener artistListActionsListener = null;
    public interface artistListActionsListener {
        void onAddArtistToPlaylistClicked(String artistName);
    }

    public ArtistListAdapter (Context activity, ListCursor listCursor) {
        super(activity, listCursor.cursor, 0);
        artistListActionsListener = (artistListActionsListener)activity;
        nameColumn = listCursor.nameColumn;
        Log.v(TAG, "nameColumn="+nameColumn );
        numItemsColumn = listCursor.numItemsColumn;
    }

    // The newView method is used to inflate a new view and return it,
    // you don't bind any data to the view at this point.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view =  LayoutInflater.from(context).inflate(R.layout.row_artist, parent, false);
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.tv_name = (TextView) view.findViewById(R.id.tv_name);
        viewHolder.btnAddToPlayqueue = (ImageButton) view.findViewById(R.id.btnAddToPlayqueue);

        String name = cursor.getString(cursor.getColumnIndexOrThrow(nameColumn));
        viewHolder.btnAddToPlayqueue.setOnClickListener(new btnAddToPlayqueueClickListener(name));
        view.setTag(viewHolder);
        return view;
    }

    class btnAddToPlayqueueClickListener implements View.OnClickListener {
        int position;
        String artistName;
        // constructor
        public btnAddToPlayqueueClickListener(String artistName) {
            this.artistName = artistName;
        }
        @Override
        public void onClick(View v) {
            // checkbox clicked
            Log.d(TAG, "add ic_artists to playqueue "+artistName);
            if (artistListActionsListener != null)
                artistListActionsListener.onAddArtistToPlaylistClicked(artistName);
        }
    }
    public static class ViewHolder{
        public TextView tv_name;
        public ImageButton btnAddToPlayqueue;
        public View view;
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        String artistName = cursor.getString(cursor.getColumnIndexOrThrow(nameColumn));
        int numTracks = cursor.getInt(cursor.getColumnIndexOrThrow(numItemsColumn));
        viewHolder.tv_name.setText(artistName + " (" + Integer.toString(numTracks) + ")");
    }

}
