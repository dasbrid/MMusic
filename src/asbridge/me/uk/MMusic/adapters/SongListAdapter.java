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
import asbridge.me.uk.MMusic.cursors.SongCursor;

import static android.content.ContentValues.TAG;

/**
 * Created by asbridged on 20/12/2016.
 */
public class SongListAdapter extends CursorAdapter {

    public SongListAdapter(Context activity, Cursor cursor) {
        super(activity, cursor, 0);
    }

    // The newView method is used to inflate a new view and return it,
    // you don't bind any data to the view at this point.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view =  LayoutInflater.from(context).inflate(R.layout.row_song, parent, false);
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.tv_artist = (TextView) view.findViewById(R.id.tv_artist);
        viewHolder.tv_title = (TextView) view.findViewById(R.id.tv_title);

        view.setTag(viewHolder);
        return view;
    }


    public static class ViewHolder{
        public TextView tv_title;
        public TextView tv_artist;
        public View view;
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        String artistName = cursor.getString(cursor.getColumnIndexOrThrow(SongCursor.ARTIST));
        String songTitle = cursor.getString(cursor.getColumnIndexOrThrow(SongCursor.TITLE));
        viewHolder.tv_artist.setText(artistName);
        viewHolder.tv_title.setText(songTitle);
    }

}
