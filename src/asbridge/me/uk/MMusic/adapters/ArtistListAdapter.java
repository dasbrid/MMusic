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

import java.util.Locale;

import static android.content.ContentValues.TAG;

/**
 * Created by asbridged on 20/12/2016.
 */
public class ArtistListAdapter extends CursorAdapter {

    private artistListActionsListener artistListActionsListener = null;
    public interface artistListActionsListener {
        void onAddArtistToPlaylistClicked(String artistName);
    }

    public ArtistListAdapter (Context activity, Cursor cursor) {
        super(activity, cursor, 0);
        artistListActionsListener = (artistListActionsListener)activity;
    }

    // The newView method is used to inflate a new view and return it,
    // you don't bind any data to the view at this point.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view =  LayoutInflater.from(context).inflate(R.layout.row_artist, parent, false);
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.tv_artist = (TextView) view.findViewById(R.id.tv_artist);
        viewHolder.btnAddToPlayqueue = (ImageButton) view.findViewById(R.id.btnAddToPlayqueue);
        int position = cursor.getPosition();
        String artistName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST));
        viewHolder.btnAddToPlayqueue.setOnClickListener(new btnAddToPlayqueueClickListener(artistName));
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
        public TextView tv_artist;
        public ImageButton btnAddToPlayqueue;
        public TextView tv_numTracks;
        public View view;
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        String artistName = cursor.getString(cursor.getColumnIndexOrThrow(ArtistCursor.ARTIST));
        int numTracks = cursor.getInt(cursor.getColumnIndexOrThrow(ArtistCursor.NUMBER_OF_TRACKS));
        viewHolder.tv_artist.setText(artistName + " (" + Integer.toString(numTracks) + ")");
        //viewHolder.tv_numTracks.setText(Integer.toString(numTracks));
    }

}
