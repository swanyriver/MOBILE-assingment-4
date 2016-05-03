package com.brandonswanson.assignment4;

import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.Format;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class SnippetViewActivityFragment extends Fragment {

    private static final String TAG = "SNIPPET_VIEW";
    private String mURL;
    private LinearLayout mMainview;
    private ArrayList<Snippet> mSnippets;


    public SnippetViewActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainview = (LinearLayout) inflater.inflate(R.layout.fragment_snippet_view, container, false);

        mURL = getActivity().getIntent().getExtras().getString(Constants.URL_KEY);

        Log.d(TAG, "onCreate: url:" + mURL);

        new getPlaylist().execute();

        return mMainview;
    }

    private class getPlaylist extends AsyncTask<Void,Void,String> {

        @Override
        protected String doInBackground(Void... params) {
            return NetworkFetcher.fetchJSON(mURL);
        }

        @Override
        protected void onPostExecute(String s) {

            Snackbar failureNotification = Snackbar
                    .make(mMainview, "No Snippets are currently Unavailable", Snackbar.LENGTH_LONG);

            if (s == null){
                failureNotification.show();
            } else {
                //Log.d(TAG, "onPostExecute: \n" + s);
                mSnippets = Snippet.snippetFactory(s);
                if (mSnippets.size() == 0) {
                    failureNotification.show();
                } else {
                    // update UI
                }
            }

        }
    }

    private static class Snippet {
        public final String videoID;
        public final String url;
        public final String title;
        public final String notes;
        public final int startTime;
        public final int endTime;

        public Snippet(String videoID, String url, String title, String notes, int startTime, int endTime) {
            this.videoID = videoID;
            this.url = url;
            this.title = title;
            this.notes = notes;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        @Override
        public String toString() {
            return "Title:" + title + " ID:" + videoID + " times:" + startTime + "-" + endTime;
        }

        static public ArrayList<Snippet> snippetFactory(String JSONplaylist){
            ArrayList<Snippet> playlistList = new ArrayList<Snippet>();
            try {
                JSONObject playlist = new JSONObject(JSONplaylist);
                JSONArray snippets = playlist.getJSONArray("snippets");
                for(int n = 0; n < snippets.length(); n++)
                {
                    try {
                        JSONObject snippet = snippets.getJSONObject(n);
                        playlistList.add(new Snippet(
                                snippet.getString("videoID"),
                                snippet.getString("url"),
                                snippet.getString("title"),
                                snippet.getString("notes"),
                                snippet.getInt("startTime"),
                                snippet.getInt("endTime")
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
    }
}
