package asbridge.me.uk.MMusic.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import asbridge.me.uk.MMusic.R;

/**
 * Created by David on 05/12/2015.
 */
public class MainActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void btnPlayAllClicked(View v) {
        // start the slideshow activity
        Intent intent = new Intent(this, MusicPlayerActivity.class);
        // Don't add any parameters if playing all songs
        this.startActivity(intent);
    }

    public void btnPlayArtistClicked(View v) {
        // start the slideshow activity
        Intent intent = new Intent(this, ActivityTemp.class);
        intent.putExtra("playlistType", "artist");
        intent.putExtra("artistname", "Bob Marley");
        this.startActivity(intent);
    }
}