package com.brandonswanson.assignment4;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by brandon on 5/1/16.
 */
public class PlaylistRow extends LinearLayout implements View.OnClickListener {

    private PlaylistPreview mPlaylistPreview;
    private Fragment mFragment;

    public PlaylistRow(Context context, PlaylistPreview playlistPreview, Fragment fragment) {
        super(context);
        mPlaylistPreview = playlistPreview;
        setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        mFragment = fragment;

        inflate(context, R.layout.playlist_row, this);

        setOnClickListener(this);

        ((TextView) findViewById(R.id.plistText)).setText(mPlaylistPreview.Title + mPlaylistPreview.Creator);
    }

    @Override
    public void onClick(View v) {
        Intent pViewIntent = new Intent(mFragment.getActivity(), SnippetViewActivity.class);
        pViewIntent.putExtra(Constants.URL_KEY, mPlaylistPreview.JSON_URL);
        mFragment.startActivity(pViewIntent);
    }

    static public class PlaylistPreview {
        static public ArrayList<PlaylistPreview> PlaylistPreviewFactory(String JSONplaylists){
            ArrayList<PlaylistPreview> playlistList = new ArrayList<PlaylistPreview>();
            try {
                JSONArray plists = new JSONArray(JSONplaylists);
                for(int n = 0; n < plists.length(); n++)
                {
                    try {
                        JSONObject plist = plists.getJSONObject(n);
                        playlistList.add(new PlaylistPreview(
                                plist.getString("title"),
                                plist.getString("creator"),
                                plist.getString("json")
                        ));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return playlistList;
        }

        public final String Title;
        public final String Creator;
        public final String JSON_URL;

        public PlaylistPreview(String title, String creator, String jsonUrl){
            Title = title;
            Creator = creator;
            JSON_URL = jsonUrl;
        }
    }
}
