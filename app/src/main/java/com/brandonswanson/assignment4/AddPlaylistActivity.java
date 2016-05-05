package com.brandonswanson.assignment4;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

public class AddPlaylistActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_playlist);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_playlist_fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String title = ((EditText) findViewById(R.id.playlist_title_edit)).getText().toString();
                    String creator = ((EditText) findViewById(R.id.playlist_creator_edit)).getText().toString();
                    if (title.isEmpty()) {
                        new AlertDialog.Builder(AddPlaylistActivity.this)
                                .setMessage("Title field cannot be empty")
                                .setPositiveButton("OK", null)
                                .show();
                        return;
                    }

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("title",title);
                    if (!creator.isEmpty()) resultIntent.putExtra("creator", creator);

                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
            });
        }
    }

}
