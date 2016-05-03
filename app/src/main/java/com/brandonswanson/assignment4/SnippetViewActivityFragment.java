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

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

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
    private YouTubePlayer mPlayer;
    private int mIndex = 0;


    public SnippetViewActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainview = (LinearLayout) inflater.inflate(R.layout.fragment_snippet_view, container, false);

/*        YouTubePlayerSupportFragment youTubePlayerFragment = (YouTubePlayerSupportFragment) getActivity().getSupportFragmentManager()
                .findFragmentById(R.id.youtube_fragment);*/
        YouTubePlayerFragment youTubePlayerFragment = (YouTubePlayerFragment) getActivity().getFragmentManager().findFragmentById(R.id.youtube_fragment);
        youTubePlayerFragment.initialize(Secret.API_KEY, new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                mPlayer = youTubePlayer;
                Log.d(TAG, "onInitializationSuccess: " + "youtube player initialized");

                loadSnippet();
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                Snackbar failureNotification = Snackbar
                        .make(mMainview, "YouTube unable to initialize", Snackbar.LENGTH_LONG);
                failureNotification.show();
                Log.e(TAG, "onInitializationFailure: " + youTubeInitializationResult.toString() );

            }
        });

        mURL = getActivity().getIntent().getExtras().getString(Constants.URL_KEY);

        Log.d(TAG, "onCreate: url:" + mURL);

        new getPlaylist().execute();

        return mMainview;
    }

    private void nextSnippet(){
        if (mSnippets == null || mIndex >= mSnippets.size() -1 ) return;
        mIndex += 1;
        loadSnippet(true);
    }

    private void prevSnippet(){
        if (mSnippets == null || mIndex <= 0 ) return;
        mIndex -= 1;
        loadSnippet(false);
    }

    private void loadSnippet(){
        loadSnippet(true);
    }

    private void loadSnippet(boolean next){
        if (mPlayer == null || mSnippets == null) {
            Log.d(TAG, "loadSnippet: Elements not initialized: "  +
                    ( mPlayer == null ? "player," : "") +
                    (mSnippets == null ? "snippets" : ""));
            return;
        }

        Snippet snp = mSnippets.get(mIndex);

        Log.d(TAG, "loadSnippet: " + snp);

        //load new video at starting time
        mPlayer.loadVideo(snp.videoID, snp.startTime * Constants.MILLIS_PER_SECOND);

        //stop previous handler
        //start handler again but watching for new time
        //change title and notes text
        //correctly enable/dissable next/prev buttons  //todo not entirly necesary, good for indicating begining and end of list though
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
                    loadSnippet();
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
