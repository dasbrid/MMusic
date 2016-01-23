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
import android.widget.EditText;
import android.widget.ListView;
import asbridge.me.uk.MMusic.R;
import asbridge.me.uk.MMusic.database.PlaybucketsTable;
import asbridge.me.uk.MMusic.utils.MusicContent;

/**
 * Created by David on 30/11/2015.
 */
public class SavePlaybucketDialog extends DialogFragment {

    private EditText etBucketname;

    public interface OnPlaybucketNameEnteredListener {
        void onPlaybucketNameEntered(String playBucketName);
    }

    private OnPlaybucketNameEnteredListener listener = null;
    public void setOnPlaybucketNameEnteredListener(OnPlaybucketNameEnteredListener l) {
        listener = l;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_saveplaybucket, container, false);

        getDialog().setTitle("Save Playbucket");

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

        etBucketname = (EditText) rootView.findViewById(R.id.frag_etBucketname);

        return rootView;
    }

    private void btnOKClicked() {
        String playbucketName = etBucketname.getText().toString();
        if (listener != null)
            listener.onPlaybucketNameEntered(playbucketName);

        dismiss();
    }
}
