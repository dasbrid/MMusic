package asbridge.me.uk.MMusic.activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.*;
import asbridge.me.uk.MMusic.R;
import asbridge.me.uk.MMusic.adapters.SongListAdapter;
import asbridge.me.uk.MMusic.classes.RetainFragment;
import asbridge.me.uk.MMusic.classes.Song;
import asbridge.me.uk.MMusic.controls.ClearableEditText;
import asbridge.me.uk.MMusic.cursors.ArtistCursor;
import asbridge.me.uk.MMusic.cursors.SongCursor;
import asbridge.me.uk.MMusic.utils.AppConstants;
import asbridge.me.uk.MMusic.utils.MusicContent;

/**
 * Created by asbridged on 20/12/2016.
 */
public class SongListtActivity extends Activity
    implements RetainFragment.RetainFragmentListener
{
    private static final String TAG = "SongListtActivity";

    private SongListAdapter dataAdapter;
    private RetainFragment retainFragment = null;

    private String name;
    private String type;

    private TextView tv_artist;
    // call back from retainfragment which binds to the music service
    @Override
    public void onMusicServiceReady() {
        Log.d(TAG, "onMusicServiceReady");
    }

    private ClearableEditText editsearch;

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
        setContentView(R.layout.activity_songs_by_artist);

        FragmentManager fm = getFragmentManager();
        retainFragment = (RetainFragment) fm.findFragmentByTag(AppConstants.TAG_RETAIN_FRAGMENT);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (retainFragment == null) {
            Log.d(TAG, "creating and adding retain Fragment");
            retainFragment = new RetainFragment();
            fm.beginTransaction().add(retainFragment, AppConstants.TAG_RETAIN_FRAGMENT).commit();
        }


        Bundle extras = getIntent().getExtras();
        //String name = "";
        type = "none";
        if (extras != null) {
            name = extras.getString(AppConstants.INTENT_EXTRA_NAME);
            type = extras.getString(AppConstants.INTENT_EXTRA_TYPE);
        }
        tv_artist = (TextView)findViewById(R.id.tv_name);

        tv_artist.setText(name ==null?getResources().getString(R.string.all_songs):
                (type.equals(AppConstants.INTENT_EXTRA_VALUE_ARTIST)?getResources().getString(R.string.songs_by_artist):getResources().getString(R.string.songs_on_album))
                        + " " + name);

        final String title = MediaStore.Audio.Media.TITLE;

        Cursor cursor;
        if (type.equals(AppConstants.INTENT_EXTRA_VALUE_ARTIST)) {
            cursor = SongCursor.getSongsCursorForArtist(this, name);
        } else {
            cursor = SongCursor.getSongsCursorForAlbum(this, name);
        }

        // create the adapter using the cursor pointing to the desired data
        dataAdapter = new SongListAdapter(this, cursor);

        ListView listView = (ListView) findViewById(R.id.lvSongsByArtist);
        listView.setAdapter(dataAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listView, View view,
                                    int position, long id) {
                // Get the cursor, positioned to the corresponding row in the result set
                Cursor cursor = (Cursor) listView.getItemAtPosition(position);

                // Get the state's capital from this row in the database.
                Long songId =
                        cursor.getLong(cursor.getColumnIndexOrThrow(ArtistCursor._ID));

                Song s;
                s = MusicContent.getSongBySongID(getApplicationContext(), songId);

                if (retainFragment != null) {
                    if (retainFragment.serviceReference != null) {
                        retainFragment.serviceReference.insertThisSongIntoPlayQueue(s);
                        Toast.makeText(getApplicationContext(),s.getTitle(), Toast.LENGTH_SHORT).show();
                    }
                }
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
                Cursor cursor;
                if (type.equals(AppConstants.INTENT_EXTRA_VALUE_ARTIST)) {
                    cursor = SongCursor.getFilteredSongsCursorForArtist(getApplicationContext(), name, partialValue);
                } else {
                    cursor = SongCursor.getFilteredSongsCursorForAlbum(getApplicationContext(), name, partialValue);
                }
                return cursor;
            }
        });
    }
}