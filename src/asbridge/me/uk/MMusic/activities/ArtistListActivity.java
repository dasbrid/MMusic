package asbridge.me.uk.MMusic.activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.*;
import asbridge.me.uk.MMusic.R;
import asbridge.me.uk.MMusic.adapters.ArtistListAdapter;
import asbridge.me.uk.MMusic.classes.RetainFragment;
import asbridge.me.uk.MMusic.classes.Song;
import asbridge.me.uk.MMusic.controls.ClearableEditText;
import asbridge.me.uk.MMusic.cursors.ArtistCursor;
import asbridge.me.uk.MMusic.utils.AppConstants;
import asbridge.me.uk.MMusic.utils.MusicContent;

import java.util.ArrayList;

/**
 * Created by asbridged on 20/12/2016.
 */
public class ArtistListActivity extends Activity
implements ArtistListAdapter.artistListActionsListener,  RetainFragment.RetainFragmentListener {

    private static final String TAG = "ArtistListActivity";

    // call back from retainfragment which binds to the music service
    @Override
    public void onMusicServiceReady() {
        Log.d(TAG, "onMusicServiceReady");
    }

    private ClearableEditText editsearch;

    private ArtistListAdapter dataAdapter;
    private RetainFragment retainFragment = null;
    public void onAddArtistToPlaylistClicked(String artist) {

        ArrayList<Song> songsByArtist = new ArrayList<>();
        MusicContent.getSongsForGivenArtist(this, artist, songsByArtist);
        Log.d(TAG, "adding " + songsByArtist.size() + " by " + artist);
        Toast.makeText(getApplicationContext(),"adding " + songsByArtist.size() + " by " + artist, Toast.LENGTH_SHORT).show();
        if (retainFragment != null) {
            if (retainFragment.serviceReference != null) {
                for (Song s : songsByArtist) {
                    retainFragment.serviceReference.insertThisSongIntoPlayQueue(s);

                }
            }
        }
    }

    // bind to the Service instance when the Activity instance starts
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        retainFragment.doBindService();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_list);

        FragmentManager fm = getFragmentManager();
        retainFragment = (RetainFragment) fm.findFragmentByTag(AppConstants.TAG_RETAIN_FRAGMENT);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (retainFragment == null) {
            Log.d(TAG, "creating and adding retain Fragment");
            retainFragment = new RetainFragment();
            fm.beginTransaction().add(retainFragment, AppConstants.TAG_RETAIN_FRAGMENT).commit();
        }

        Cursor cursor = ArtistCursor.getArtistsCursor(this);
        // create the adapter using the cursor pointing to the desired data
        //as well as the layout information
        //dataAdapter = new SimpleCursorAdapter(this, R.layout.row_artist, cursor, columns, to, 0);
        dataAdapter = new ArtistListAdapter(this,  cursor);
        ListView listView = (ListView) findViewById(R.id.artistListView);
        // Assign adapter to ListView
        listView.setAdapter(dataAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listView, View view,
                                    int position, long id) {
                // Get the cursor, positioned to the corresponding row in the result set
                Cursor cursor = (Cursor) listView.getItemAtPosition(position);

                // Get the state's capital from this row in the database.
                String artistId =
                        cursor.getString(cursor.getColumnIndexOrThrow(ArtistCursor._ID));
                String artistName =
                        cursor.getString(cursor.getColumnIndexOrThrow(ArtistCursor.ARTIST));

                Intent intent = new Intent(getApplicationContext(), SongListtActivity.class);
                intent.putExtra(AppConstants.INTENT_EXTRA_ARTIST, artistName);
                startActivity(intent);
            }
        });

        // Locate the EditText in listview_main.xml
        editsearch = (ClearableEditText) findViewById(R.id.search);
        editsearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {


            }

            @Override
            public void afterTextChanged(Editable s) {
                dataAdapter.getFilter().filter(s.toString());

            }
        });


        dataAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                String partialValue = constraint.toString();
                Log.d (TAG, partialValue);
                return ArtistCursor.getFilteredArtistsCursor(getApplicationContext(), partialValue);
            }
        });

    }
}