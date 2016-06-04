package com.brandonswanson.assignment4;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity {

    private static final int ADD_PLAYLIST_ACTIVITY = 100;
    private static final int LOG_IN_REQUEST = 101;
    public static final int LOG_IN_TO_ADD_REQUEST = 102;
    private static final String TAG = "MAIN Activity";
    private Button mPublicPlaylistButton;
    private Button mUserPlaylistButton;
    private FrameLayout mPublicPlaylistButtonHighlight;
    private FrameLayout mUserPlaylistButtonHighlight;
    private boolean mShowingPublic = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final LayoutInflater inflater = getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View dialogView = inflater.inflate(R.layout.sign_in_pop_up, null);
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();
        dialogView.findViewById(R.id.pop_up_sign_in_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        launchLogin(LOG_IN_TO_ADD_REQUEST);
                        dialog.dismiss();
                    }
                });

        //Add Playlist FAB
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Credentials.getsInstance().isUserLoggedIn()){
                        startAddPlaylistActivity();
                    } else {
                        dialog.show();
                    }
                }
            });
        }

        mPublicPlaylistButton = (Button) findViewById(R.id.public_playlist_button);
        mUserPlaylistButton = (Button) findViewById(R.id.user_playlist_button);
        mPublicPlaylistButtonHighlight = (FrameLayout) findViewById(R.id.public_playlist_button_highlight);
        mUserPlaylistButtonHighlight = (FrameLayout) findViewById(R.id.user_playlist_button_highlight);
    }

    private void startAddPlaylistActivity() {
        Intent intent = new Intent(getApplicationContext(),
                AddPlaylistActivity.class);
        startActivityForResult(intent, ADD_PLAYLIST_ACTIVITY);
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
            launchLogin(LOG_IN_REQUEST);
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
        } else if ((requestCode == LOG_IN_REQUEST || requestCode == LOG_IN_TO_ADD_REQUEST )
                && resultCode == RESULT_OK) {
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

            if(mShowingPublic){
                switchPlaylists();
            }

            if (requestCode == LOG_IN_TO_ADD_REQUEST) {
                startAddPlaylistActivity();
            }

        }


        super.onActivityResult(requestCode, resultCode, data);
    }

    public void launchLogin(int RequestCode){
        //launch register log in/ log out activity
        Intent launchLoginIntent = new Intent(getApplicationContext(),
                LoginActivity.class);
        startActivityForResult(launchLoginIntent, RequestCode);
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

    public NetworkFetcher.APICallFactory deletePlaylistAPI =
            new NetworkFetcher.APICallFactory("DELETE", new NetworkFetcher.NetworkFinish() {
                @Override
                public void onNetworkResponse(int responseCode, String responseMsg) {
            if (responseCode == 202) {
                //success
                Snackbar successNotification = Snackbar
                        .make(findViewById(R.id.mainOuterLayout), "Playlist deleted", Snackbar.LENGTH_LONG);
                successNotification.show();
                MainActivityFragment myFragment = (MainActivityFragment)
                        getSupportFragmentManager().findFragmentById(R.id.plistFragment);
                myFragment.refreshPlaylists();
            } else {
                //failure
                Snackbar failureNotification = Snackbar
                        .make(findViewById(R.id.mainOuterLayout), "Unable to delete playlist", Snackbar.LENGTH_LONG);
                failureNotification.show();
            }
        }
    });

    public void onTabButton(View button) {
        /*mUserPlaylistButton.setEnabled(button == mPublicPlaylistButton);
        mPublicPlaylistButton.setEnabled(button == mUserPlaylistButton);*/

        if (button == mUserPlaylistButton && mShowingPublic){
            Log.d(TAG, "onTabButton: switching to USER playlists");
            switchPlaylists();
        } else if (button == mPublicPlaylistButton && !mShowingPublic) {
            Log.d(TAG, "onTabButton: switching to PUBLIC playlists");
            switchPlaylists();
        }
    }

    private void switchPlaylists(){
        mShowingPublic = !mShowingPublic;
        switchVis(mPublicPlaylistButtonHighlight);
        switchVis(mUserPlaylistButtonHighlight);
        MainActivityFragment myFragment = (MainActivityFragment)
                getSupportFragmentManager().findFragmentById(R.id.plistFragment);
        myFragment.refreshPlaylists();
    }

    private void switchVis(View view){
        view.setVisibility((view.getVisibility() == View.VISIBLE) ? View.INVISIBLE : View.VISIBLE);
    }

    public boolean showingPublic() {
        return mShowingPublic;
    }
}
