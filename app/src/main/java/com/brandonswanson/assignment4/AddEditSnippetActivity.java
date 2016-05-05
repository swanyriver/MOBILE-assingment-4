package com.brandonswanson.assignment4;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AddEditSnippetActivity extends AppCompatActivity {

    private static final String TAG = "ADD_EDIT_SNIPPET";
    private String mYTVideoID = null;
    private YouTubePlayer mPlayer;

    private AlertDialog mDialog;
    private EditText mDialogID_edt;
    private ProgressBar mDialogProgress;
    private TextView mDialogStatusText;
    private Button mDialogOkButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_snippet);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //return to calling activity with extras
                    //todo verify all fields but notes are entered
                    //todo pack into bundle and finish
                }
            });
        }

        Bundle extras = getIntent().getExtras();
        String url = (extras != null) ? extras.getString("url") : null;
        if (url != null){
            // edit
            // todo should i add the whole snippet as extra or pull from url
            //mYTVideoID =
            // load video
            loadVideo();
            // populate fields
            // todo populate fields
        } else {

            //pick youtube ID
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = this.getLayoutInflater();
            final View dialogView = inflater.inflate(R.layout.youtube_picker_dialog, null);
            builder.setView(dialogView);
            mDialog = builder.create();

            mDialogID_edt = (EditText) dialogView.findViewById(R.id.youtube_picker_edittext);
            mDialogProgress = (ProgressBar) dialogView.findViewById(R.id.youtube_dialog_progress);
            mDialogStatusText = (TextView) dialogView.findViewById(R.id.youtube_dialog_status_text);
            mDialogOkButton = (Button) dialogView.findViewById(R.id.youtube_dialog_ok_button);
            mDialogOkButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDialogOkButton.setVisibility(View.GONE);
                    mDialogProgress.setVisibility(View.VISIBLE);

                    String id = mDialogID_edt.getText().toString();
                    Log.d(TAG, "onClick dialog ok: " + id);

                    new verifyVideoID().execute(id);
                }
            });

            mDialog.show();
        }

        final YouTubePlayerFragment youTubePlayerFragment = (YouTubePlayerFragment) getFragmentManager().findFragmentById(R.id.add_edit_youtube_fragment);
        youTubePlayerFragment.initialize(Secret.API_KEY, new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                mPlayer = youTubePlayer;
                Log.d(TAG, "onInitializationSuccess: " + "youtube player initialized");
                loadVideo();
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                Log.e(TAG, "onInitializationFailure: " + youTubeInitializationResult.toString() );
                Snackbar failureNotification = Snackbar
                        .make(findViewById(R.id.add_edit_snippet_outer_layout), "YouTube unable to initialize", Snackbar.LENGTH_LONG);
                failureNotification.show();
            }
        });
    }

    private void loadVideo(){
        if (mPlayer == null || mYTVideoID == null) {
            Log.d(TAG, "loadSnippet: Elements not initialized: "  +
                    ( mPlayer == null ? "player," : "") +
                    (mYTVideoID == null ? "snippets" : ""));
            return;
        }

        //load new video at starting time
        mPlayer.cueVideo(mYTVideoID);

    }

    class verifyVideoID extends AsyncTask<String, Void, String> {
        @Override
        protected void onPostExecute(String result) {
            mDialogProgress.setVisibility(View.GONE);
            mDialogOkButton.setVisibility(View.VISIBLE);

            if (result == null){
                mDialogStatusText.setText("YouTube unreachable to verify ID");
                return;
            }

            try {
                JSONObject ytResult = new JSONObject(result);
                JSONArray items = ytResult.getJSONArray("items");
                if (items.length() == 0){
                    mDialogStatusText.setText("Invalid YouTube Video ID");
                } else {
                    mYTVideoID = items.getJSONObject(0).getString("id");
                    loadVideo();
                    mDialog.dismiss();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mDialogStatusText.setText("YouTube unreachable to verify ID");
            }

        }

        @Override
        protected String doInBackground(String... params) {
            return NetworkFetcher.fetchJSON("https://www.googleapis.com",
                    String.format("/youtube/v3/videos?id=%s&key=%s&part=id", params[0], Secret.API_KEY));
        }
    }

}
