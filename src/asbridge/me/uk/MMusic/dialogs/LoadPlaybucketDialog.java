package asbridge.me.uk.MMusic.dialogs;

import android.app.DialogFragment;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import asbridge.me.uk.MMusic.R;
import asbridge.me.uk.MMusic.database.PlaybucketsTable;
import asbridge.me.uk.MMusic.utils.MusicContent;

/**
 * Created by David on 30/11/2015.
 */
public class LoadPlaybucketDialog extends DialogFragment {

    private ListView lvPlaybuckets;

    public interface OnPlaybucketSelectedListener {
        void onPlayBucketSelected(int playlistID);
    }

    private OnPlaybucketSelectedListener listener = null;
    public void setOnPlaybucketSelectedListener(OnPlaybucketSelectedListener l) {
        listener = l;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_loadplaybucket, container, false);

        getDialog().setTitle("Playbuckets");

        Button btnCancel = (Button) rootView.findViewById(R.id.btnLoadPlaybucketDialogCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        String[] columns = new String[] {
                PlaybucketsTable.COLUMN_NAME_PLAYBUCKET_ID,
                PlaybucketsTable.COLUMN_NAME_PLAYBUCKET_NAME,
        };

        // the XML defined views which the data will be bound to
        int[] to = new int[] {
                R.id.playbucketID,
                R.id.playbucketName
        };

        Cursor playBucketsCursor = MusicContent.getPlaylistsCursor(getActivity());

        lvPlaybuckets = (ListView) rootView.findViewById(R.id.frag_lvPlaybuckets);
        SimpleCursorAdapter dataAdapter;
        // create the adapter using the cursor pointing to the desired data
        //as well as the layout information
        dataAdapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.row_playbucket,
                playBucketsCursor,
                columns,
                to,
                0);
        lvPlaybuckets.setAdapter(dataAdapter);

        lvPlaybuckets.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listView, View view,
                                    int position, long id) {
                // Get the cursor, positioned to the corresponding row in the result set
                Cursor cursor = (Cursor) listView.getItemAtPosition(position);
                // Get the playbucket from this row in the database.
                String playbucketIDString =
                        cursor.getString(cursor.getColumnIndexOrThrow(PlaybucketsTable.COLUMN_NAME_PLAYBUCKET_ID));
                int playbucketID = Integer.parseInt(playbucketIDString);
                if (listener != null)
                    listener.onPlayBucketSelected(playbucketID);

                dismiss();
            }
        });

        return rootView;
    }
}