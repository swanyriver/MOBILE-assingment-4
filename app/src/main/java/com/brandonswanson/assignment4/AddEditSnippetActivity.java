package com.brandonswanson.assignment4;

import android.content.DialogInterface;
import android.content.Intent;
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

import com.google.android.youtube.player.YouTubePlayer;

import org.w3c.dom.Text;

public class AddEditSnippetActivity extends AppCompatActivity {

    private static final String TAG = "ADD_EDIT_SNIPPET";
    private String mYTVideoID = null;
    private AlertDialog mDialog;
    private YouTubePlayer mPlayer;


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

            final EditText ID_edt = (EditText) dialogView.findViewById(R.id.youtube_picker_edittext);
            final ProgressBar progress = (ProgressBar) dialogView.findViewById(R.id.youtube_dialog_progress);
            final TextView statusText = (TextView) dialogView.findViewById(R.id.youtube_dialog_status_text);
            final Button okButton = (Button) dialogView.findViewById(R.id.youtube_dialog_ok_button);
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    okButton.setVisibility(View.GONE);
                    progress.setVisibility(View.VISIBLE);

                    String id = ID_edt.getText().toString();
                    Log.d(TAG, "onClick dialog ok: " + id);

                    //todo make network call to validate id, load video and dismiss dialog in network response if posotive
                }
            });

            mDialog.show();
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
        mPlayer.cueVideo(mYTVideoID);

    }

}
