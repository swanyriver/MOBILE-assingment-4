package com.brandonswanson.assignment4;

import android.content.Context;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by brandon on 5/1/16.
 */
public class PlaylistRow extends LinearLayout {

    private PlaylistPreview mPlaylistPreview;

    public PlaylistRow(Context context, PlaylistPreview playlistPreview) {
        super(context);
        mPlaylistPreview = playlistPreview;
        setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));

        inflate(context, R.layout.playlist_row, this);

        ((TextView) findViewById(R.id.plistText)).setText(mPlaylistPreview.Title + mPlaylistPreview.Creator);
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

        public String Title;
        public String Creator;
        public String JSON_URL;

        public PlaylistPreview(String title, String creator, String jsonUrl){
            Title = title;
            Creator = creator;
            JSON_URL = jsonUrl;
        }
    }
}
