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
import android.widget.Toast;
import asbridge.me.uk.MMusic.R;
import asbridge.me.uk.MMusic.adapters.ArtistListAdapter;
import asbridge.me.uk.MMusic.classes.RetainFragment;
import asbridge.me.uk.MMusic.classes.Song;
import asbridge.me.uk.MMusic.settings.SettingsActivity;
import asbridge.me.uk.MMusic.utils.AppConstants;
import asbridge.me.uk.MMusic.utils.MusicContent;

import java.util.ArrayList;
import java.util.List;

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

        String where = null;
        ContentResolver cr = this.getContentResolver();
        final Uri uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        final String _id = MediaStore.Audio.Albums._ID;
        final String album_name =MediaStore.Audio.Albums.ALBUM;
        final String artist = MediaStore.Audio.Albums.ARTIST;
        final String[] cursorColumns={_id,album_name, artist};
        Cursor cursor = cr.query(uri,cursorColumns,where,null, null);



        // The desired columns to be bound
        String[] columns = new String[] {
                artist /*,
        CountriesDbAdapter.KEY_NAME,
        CountriesDbAdapter.KEY_CONTINENT,
        CountriesDbAdapter.KEY_REGION*/
        };

        // the XML defined views which the data will be bound to
        int[] to = new int[] { R.id.tv_artist }; /*,
            R.id.name,
            R.id.continent,
            R.id.region,
          };*/

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
                        cursor.getString(cursor.getColumnIndexOrThrow("_id"));
                String artistName =
                        cursor.getString(cursor.getColumnIndexOrThrow("artist"));
                //Toast.makeText(getApplicationContext(),artistName, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), SongsByArtistActivity.class);
                intent.putExtra("artist", artistName);
                startActivity(intent);
            }
        });
    }
}