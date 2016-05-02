package com.brandonswanson.assignment4;

import android.content.Context;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by brandon on 5/1/16.
 */
public class PlaylistRow extends LinearLayout {

    //todo change second param to plist object
    public PlaylistRow(Context context, String text) {
        super(context);
        setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));

        inflate(context, R.layout.playlist_row, this);

        ((TextView) findViewById(R.id.plistText)).setText(text);
        Log.d("ASYNC", "PlaylistRow: " + text);
    }
}
