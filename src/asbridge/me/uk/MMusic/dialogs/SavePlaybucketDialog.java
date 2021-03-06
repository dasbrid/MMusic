package asbridge.me.uk.MMusic.dialogs;

import android.app.DialogFragment;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import asbridge.me.uk.MMusic.R;
import asbridge.me.uk.MMusic.adapters.PlaybucketCursorAdapter;
import asbridge.me.uk.MMusic.database.PlaybucketsTable;
import asbridge.me.uk.MMusic.database.PlaybucketsView;
import asbridge.me.uk.MMusic.utils.MusicContent;

/**
 * Created by David on 30/11/2015.
 */
public class SavePlaybucketDialog extends DialogFragment {

    private ListView lvPlaybuckets;
    private EditText etBucketname;

    public interface OnSavePlaybucketActionListener {
        void onNewPlaybucketNameEntered(String playBucketName);
        void onSavePlayBucketSelected(int playbucketID);
    }

    private OnSavePlaybucketActionListener listener = null;
    public void setOnPlaybucketNameEnteredListener(OnSavePlaybucketActionListener l) {
        listener = l;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_saveplaybucket, container, false);

        getDialog().setTitle("Save playbucket");

        Button btnCancel = (Button) rootView.findViewById(R.id.btnSavePlaybucketDialogCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        Button btnOK = (Button) rootView.findViewById(R.id.btnSavePlaybucketDialogOK);
        btnOK.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                btnOKClicked();
            }
        });

        String[] columns = new String[] {
//                PlaybucketsView.COLUMN_NAME_PLAYBUCKET_ID,
                PlaybucketsView.COLUMN_NAME_PLAYBUCKET_NAME,
                PlaybucketsView.COLUMN_NAME_NUMSONGS
        };

        // the XML defined views which the data will be bound to
        int[] to = new int[] {
//                R.id.playbucketID,
                R.id.playbucketName,
                R.id.playbucketnumsongs
        };

        Cursor playBucketsCursor = MusicContent.getPlaybucketsCursor(getActivity());

        lvPlaybuckets = (ListView) rootView.findViewById(R.id.frag_lvSavePlaybuckets);
        PlaybucketCursorAdapter dataAdapter;
        // create the adapter using the cursor pointing to the desired data
        //as well as the layout information
        dataAdapter = new PlaybucketCursorAdapter(
                getActivity(),
                R.layout.row_playbucket,
                playBucketsCursor,
                columns,
                to,
                0);
        lvPlaybuckets.setAdapter(dataAdapter);

        etBucketname = (EditText) rootView.findViewById(R.id.frag_etBucketname);

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
                    listener.onSavePlayBucketSelected(playbucketID);

                dismiss();
            }
        });



        return rootView;
    }

    private void btnOKClicked() {
        String playbucketName = etBucketname.getText().toString();
        if (listener != null)
            listener.onNewPlaybucketNameEntered(playbucketName);
        dismiss();
    }
}
