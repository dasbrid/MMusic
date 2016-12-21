package asbridge.me.uk.MMusic.activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import asbridge.me.uk.MMusic.R;
import asbridge.me.uk.MMusic.classes.RetainFragment;
import asbridge.me.uk.MMusic.classes.Song;
import asbridge.me.uk.MMusic.cursors.ArtistCursor;
import asbridge.me.uk.MMusic.utils.AppConstants;
import asbridge.me.uk.MMusic.utils.MusicContent;

/**
 * Created by asbridged on 20/12/2016.
 */
public class SongsByArtistActivity extends Activity
    implements RetainFragment.RetainFragmentListener
{
    private static final String TAG = "SongsByArtistActivity";

    private SimpleCursorAdapter dataAdapter;
    private RetainFragment retainFragment = null;

    private TextView tv_artist;
    // call back from retainfragment which binds to the music service
    @Override
    public void onMusicServiceReady() {
        Log.d(TAG, "onMusicServiceReady");
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
        String artistName = "";

        if (extras != null) {
            artistName = extras.getString(AppConstants.INTENT_EXTRA_ARTIST);
        }
        tv_artist = (TextView)findViewById(R.id.tv_artist);
        tv_artist.setText(artistName);
        String where = null;
        String selection = MediaStore.Audio.Media.ARTIST + "=?";
        String[] selectionArgs = {artistName};
        ContentResolver cr = this.getContentResolver();
        final Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        final String _id = MediaStore.Audio.Media._ID;
        final String title = MediaStore.Audio.Media.TITLE;
        final String album_name =MediaStore.Audio.Media.ALBUM;
        final String artist = MediaStore.Audio.Albums.ARTIST;
        final String[] cursorColumns={_id,title, artist};
        Cursor cursor = cr.query(uri,cursorColumns,selection, selectionArgs, null);



        // The desired columns to be bound
        String[] columns = new String[] {
                title /*,
        CountriesDbAdapter.KEY_NAME,
        CountriesDbAdapter.KEY_CONTINENT,
        CountriesDbAdapter.KEY_REGION*/
        };

        // the XML defined views which the data will be bound to
        int[] to = new int[] { R.id.tv_song_title }; /*,
            R.id.name,
            R.id.continent,
            R.id.region,
          };*/

        // create the adapter using the cursor pointing to the desired data
        //as well as the layout information
        dataAdapter = new SimpleCursorAdapter(this, R.layout.row_song_by_artist, cursor, columns, to, 0);

        ListView listView = (ListView) findViewById(R.id.lvSongsByArtist);
        // Assign adapter to ListView
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
    }
}