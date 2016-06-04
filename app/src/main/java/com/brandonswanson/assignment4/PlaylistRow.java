package com.brandonswanson.assignment4;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
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
public class PlaylistRow extends LinearLayout implements View.OnClickListener, View.OnLongClickListener {

    private PlaylistPreview mPlaylistPreview;
    private MainActivityFragment mFragment;

    public PlaylistRow(Context context, PlaylistPreview playlistPreview, Fragment fragment) {
        super(context);
        mPlaylistPreview = playlistPreview;
        setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        mFragment = (MainActivityFragment) fragment;

        inflate(context, R.layout.playlist_row, this);

        setOnClickListener(this);
        setOnLongClickListener(this);

        //todo make playlist row more attractive
        //todo create enough test data that it is scrollable
        ((TextView) findViewById(R.id.plistText)).setText(mPlaylistPreview.Title + "  by  " + mPlaylistPreview.Creator);

    }

    @Override
    public void onClick(View v) {
        Intent pViewIntent = new Intent(mFragment.getActivity(), SnippetViewActivity.class);
        pViewIntent.putExtra(Constants.URL_KEY, mPlaylistPreview.JSON_URL);
        pViewIntent.putExtra("entity_url", mPlaylistPreview.URL);
        pViewIntent.putExtra("creator", mPlaylistPreview.Creator);
        pViewIntent.putExtra("isPublic", mPlaylistPreview.isPublic);
        mFragment.startActivity(pViewIntent);
    }

    @Override
    public boolean onLongClick(View v) {
        //show popup, delete playlist

        if (!Credentials.getsInstance().belongsToLoggedInUser(mPlaylistPreview.Creator)){
            return false;
        }

        Log.d("playlistRow", "onLongClick: " + mPlaylistPreview.Title);

        AlertDialog alertDialog = new AlertDialog.Builder(mFragment.getContext()).create();
        alertDialog.setTitle("Delete Playlist");
        alertDialog.setMessage("Would you like to delete \"" + mPlaylistPreview.Title + "\" playlist?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("DIALOG", "delete playlist: clicked yes");
                ((MainActivity) mFragment.getActivity())
                        .deletePlaylistAPI.execute(mPlaylistPreview.URL);
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("DIALOG", "delete playlist: clicked no");
                //do nothing
            }
        });

        alertDialog.show();

        return true;
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
                                plist.getString("json"),
                                plist.getString("url"),
                                plist.getBoolean("isPublic")
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
        public final String URL;
        public final boolean isPublic;

        public PlaylistPreview(String title, String creator, String jsonUrl, String URL, boolean isPublic){
            this.Title = title;
            this.Creator = creator;
            this.JSON_URL = jsonUrl;
            this.URL = URL;
            this.isPublic = isPublic;
        }
    }
}
