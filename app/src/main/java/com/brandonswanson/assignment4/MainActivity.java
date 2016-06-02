package com.brandonswanson.assignment4;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private static final int ADD_PLAYLIST_ACTIVITY = 100;
    private static final int LOG_IN_REQUEST = 101;
    private static final String TAG = "MAIN Activity";
    private Button mPublicPlaylistButton;
    private Button mUserPlaylistButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Add Playlist FAB
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), AddPlaylistActivity.class);
                    startActivityForResult(intent, ADD_PLAYLIST_ACTIVITY);
                }
            });
        }

        mPublicPlaylistButton = (Button) findViewById(R.id.public_playlist_button);
        mUserPlaylistButton = (Button) findViewById(R.id.user_playlist_button);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MainActivityFragment myFragment = (MainActivityFragment)
                getSupportFragmentManager().findFragmentById(R.id.plistFragment);
        myFragment.refreshPlaylists();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_login) {
            launchLogin(null);
        } else if (id == R.id.action_share_main) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, Constants.API_ROOT);
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, "Share this with others"));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == ADD_PLAYLIST_ACTIVITY && resultCode == RESULT_OK) {
            String postParams = NetworkFetcher.getHTTPPOST(data.getExtras());
            Log.d(TAG, "onActivityResult: " + postParams);
            createPlaylistAPI.execute("/", postParams);
        } else if (requestCode == LOG_IN_REQUEST && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            String msg = null;
            if (extras != null){
                msg = extras.getString("msg");
            }
            if (msg != null) {
                Snackbar resultNotification = Snackbar
                        .make(findViewById(R.id.mainOuterLayout), msg, Snackbar.LENGTH_LONG);
                resultNotification.show();
            }
        }


        super.onActivityResult(requestCode, resultCode, data);
    }

    public void launchLogin(View view){
        //launch register log in/ log out activity
        Intent launchLoginIntent = new Intent(getApplicationContext(),
                LoginActivity.class);
        startActivityForResult(launchLoginIntent, LOG_IN_REQUEST);
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

    public void onTabButton(View button) {
        mUserPlaylistButton.setEnabled(button == mPublicPlaylistButton);
        mPublicPlaylistButton.setEnabled(button == mUserPlaylistButton);
        if (button == mPublicPlaylistButton){
            Log.d(TAG, "onTabButton: switching to PUBLIC playlists");
        } else {
            Log.d(TAG, "onTabButton: switching to USER playlists");
        }
    }
}
