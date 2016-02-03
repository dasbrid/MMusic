package asbridge.me.uk.MMusic.adapters;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import asbridge.me.uk.MMusic.R;
import asbridge.me.uk.MMusic.database.PlaybucketsView;


/**
 * Created by AsbridgeD on 03/02/2016.
 */
public class PlaybucketCursorAdapter extends SimpleCursorAdapter {

    private Context context;
    private int layout;

    public PlaybucketCursorAdapter (Context context, int layout, Cursor c, String[] from, int[] to, int x ) {
        super(context, layout, c, from, to, x);
        this.context = context;
        this.layout = layout;
    }

    @Override
    public void bindView(View v, Context context, Cursor c) {

        int nameCol = c.getColumnIndex(PlaybucketsView.COLUMN_NAME_PLAYBUCKET_NAME);
        int numsongsCol = c.getColumnIndex(PlaybucketsView.COLUMN_NAME_NUMSONGS);

        String name = c.getString(nameCol);
        String numsongs = c.getString(numsongsCol);

        /**
         * Next set the name of the entry.
         */
        TextView name_text = (TextView) v.findViewById(R.id.playbucketName);
        name_text.setText(name);
        TextView numSongs_text = (TextView) v.findViewById(R.id.playbucketnumsongs);
        numSongs_text.setText(numsongs);

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        Cursor c = getCursor();

        final LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(layout, parent, false);

        int nameCol = c.getColumnIndex(PlaybucketsView.COLUMN_NAME_PLAYBUCKET_NAME);
        int numsongsCol = c.getColumnIndex(PlaybucketsView.COLUMN_NAME_NUMSONGS);

        String name = c.getString(nameCol);
        String numsongs = c.getString(numsongsCol);

        /**
         * Next set the name of the entry.
         */
        TextView name_text = (TextView) v.findViewById(R.id.playbucketName);
        name_text.setText(name);
        TextView numSongs_text = (TextView) v.findViewById(R.id.playbucketnumsongs);
        numSongs_text.setText(numsongs);

        return v;
    }


}
