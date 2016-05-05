package com.brandonswanson.assignment4;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
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
    private String mUrl = null;

    private AlertDialog mDialog;
    private EditText mDialogID_edt;
    private ProgressBar mDialogProgress;
    private TextView mDialogStatusText;
    private Button mDialogOkButton;

    private Button mMarkStartButton;
    private Button mMarkEndButton;
    private EditText mTitleEdit;
    private EditText mNotesEdit;
    private EditText mStartTimeEdit;
    private EditText mEndTimeEdit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_snippet);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mMarkStartButton = (Button) findViewById(R.id.mark_start_time_button);
        mMarkEndButton = (Button) findViewById(R.id.mark_end_time_button);
        mTitleEdit = (EditText) findViewById(R.id.snippet_title_edit);
        mNotesEdit = (EditText) findViewById(R.id.snippet_notes_edit);
        mStartTimeEdit = (EditText) findViewById(R.id.start_time_edittext);
        mEndTimeEdit = (EditText) findViewById(R.id.end_time_edittext);

        mMarkStartButton.setOnClickListener(new MarkTime(mStartTimeEdit));
        mMarkEndButton.setOnClickListener(new MarkTime(mEndTimeEdit));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //return to calling activity with extras
                    if (mTitleEdit.getText().length() == 0
                            || mStartTimeEdit.getText().length() == 0
                            || mEndTimeEdit.getText().length() == 0 ){
                        Snackbar failureNotification = Snackbar
                                .make(findViewById(R.id.add_edit_snippet_outer_layout), "Required fields must not be left blank", Snackbar.LENGTH_LONG);
                        failureNotification.show();
                    } else {
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("title",mTitleEdit.getText().toString());
                        resultIntent.putExtra("videoID", mYTVideoID);

                        resultIntent.putExtra("startTime", mStartTimeEdit.getText().toString());
                        resultIntent.putExtra("endTime", mEndTimeEdit.getText().toString());

                        if (mNotesEdit.getText().length() > 0) resultIntent.putExtra("notes", mNotesEdit.getText().toString());
                        if (mUrl != null) resultIntent.putExtra("url", mUrl);

                        setResult(Activity.RESULT_OK, resultIntent);
                        Log.d(TAG, "onClick: finishing add activity with extras");
                        finish();
                    }
                }
            });
        }

        Bundle extras = getIntent().getExtras();
        String url = (extras != null) ? extras.getString("url") : null;
        if (url != null && extras.getString("VideoID") != null){
            // edit snippet
            mUrl = url;
            String title = extras.getString("title");
            String notes = extras.getString("notes");
            mYTVideoID = extras.getString("VideoID");
            String startTime = extras.getString("startTime");
            String endTime = extras.getString("endTime");

            // load video
            loadVideo();

            // populate fields
            if(title != null) mTitleEdit.setText(title);
            if(notes != null) mNotesEdit.setText(notes);
            if(startTime != null) mStartTimeEdit.setText(startTime);
            if(endTime != null) mEndTimeEdit.setText(endTime);

            fab.setImageDrawable(getDrawable(R.drawable.ic_mode_edit_white_48dp));


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

    private class MarkTime implements View.OnClickListener {

        private final EditText mEdit;

        public MarkTime(EditText edit) {
            mEdit = edit;
        }

        @Override
        public void onClick(View v) {
            if (mPlayer == null) return;

            mEdit.setText("" + (mPlayer.getCurrentTimeMillis() / Constants.MILLIS_PER_SECOND));
        }
    }

    private void loadVideo(){
        if (mPlayer == null || mYTVideoID == null) {
            Log.d(TAG, "loadSnippet: Elements not initialized: "  +
                    ( mPlayer == null ? "player," : "") +
                    (mYTVideoID == null ? "snippets" : ""));
            return;
        }

        //load new video at starting time
        mPlayer.loadVideo(mYTVideoID);

        mMarkStartButton.setEnabled(true);
        mMarkEndButton.setEnabled(true);

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
