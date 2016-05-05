package com.brandonswanson.assignment4;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private static final int ADD_PLAYLIST_ACTIVITY = 100;
    private static final String TAG = "MAIN Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add_playlist) {
            Intent intent = new Intent(this, AddPlaylistActivity.class);
            startActivityForResult(intent, ADD_PLAYLIST_ACTIVITY);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == ADD_PLAYLIST_ACTIVITY && resultCode == RESULT_OK) {
            //todo make network call from this activity
            String postParams = NetworkFetcher.getHTTPPOST(data.getExtras());
            Log.d(TAG, "onActivityResult: " + postParams);

            createPlaylistAPI.execute("/", postParams);

        }


        super.onActivityResult(requestCode, resultCode, data);
    }

    private NetworkFetcher.APICallFactory createPlaylistAPI =
            new NetworkFetcher.APICallFactory("POST", new NetworkFetcher.NetworkFinish() {
        @Override
        public void onNetworkResponse(int responseCode, String responseMsg) {
            if (responseCode == 201){
                //success
                Snackbar sucessNotification = Snackbar
                        .make(findViewById(R.id.mainOuterLayout), "Playlist Created", Snackbar.LENGTH_LONG);
                sucessNotification.show();
                MainActivityFragment myFragment = (MainActivityFragment)
                        getSupportFragmentManager().findFragmentById(R.id.plistFragment);
                myFragment.refreshPlaylists();
            } else {
                //failure
                Snackbar failureNotification = Snackbar
                        .make(findViewById(R.id.mainOuterLayout), "Playlist Created", Snackbar.LENGTH_LONG);
                failureNotification.show();
            }
        }
    });
}
