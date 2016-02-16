package asbridge.me.uk.MMusic.dialogs;

import android.support.v4.app.DialogFragment;
//import android.app.DialogFragment;
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
public class SearchSongsDialog extends DialogFragment {

    private EditText etSearchString;

    public interface OnSearchSongsActionListener {
        void onSearchStringEntered(String searchString);
    }

    private OnSearchSongsActionListener listener = null;
    public void setOnSearchStringEntered(OnSearchSongsActionListener l) {
        listener = l;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_searchsongs, container, false);

        getDialog().setTitle("Search songs");

        Button btnCancel = (Button) rootView.findViewById(R.id.btnSearchSongsDialogCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        Button btnOK = (Button) rootView.findViewById(R.id.btnSearchSongsDialogOK);
        btnOK.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                btnOKClicked();
            }
        });

        etSearchString = (EditText) rootView.findViewById(R.id.etSearchSongsString);

        return rootView;
    }

    private void btnOKClicked() {
        String searchString = etSearchString.getText().toString();
        if (searchString.isEmpty())
            return;
        if (listener != null)
            listener.onSearchStringEntered(searchString);
        dismiss();
    }
}
