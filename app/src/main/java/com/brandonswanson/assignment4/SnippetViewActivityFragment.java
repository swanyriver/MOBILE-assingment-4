package com.brandonswanson.assignment4;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import android.os.Handler;

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
    private GestureDetector mDetector;
    private ImageButton mPrevButton;
    private ImageButton mNextButton;
    private TextView mTitleText;
    private TextView mNotesText;
    private Handler mStopHandler;
    private Runnable mStopRunnable;

    public SnippetViewActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainview = (LinearLayout) inflater.inflate(R.layout.fragment_snippet_view, container, false);

        mDetector = new GestureDetector(getActivity().getApplicationContext(), new myGuestureDetect());
        mMainview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //Log.d(TAG, "onTouch: touchytouchy");
                mDetector.onTouchEvent(event);

                return true;
            }
        });

        mPrevButton = (ImageButton) mMainview.findViewById(R.id.prev);
        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prevSnippet();
            }
        });
        mNextButton = (ImageButton) mMainview.findViewById(R.id.next);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextSnippet();
            }
        });

        mTitleText = (TextView) mMainview.findViewById(R.id.Title_textView);
        mNotesText = (TextView) mMainview.findViewById(R.id.notes_textView);



        final YouTubePlayerFragment youTubePlayerFragment = (YouTubePlayerFragment) getActivity().getFragmentManager().findFragmentById(R.id.youtube_fragment);
        youTubePlayerFragment.initialize(Secret.API_KEY, new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                mPlayer = youTubePlayer;
                Log.d(TAG, "onInitializationSuccess: " + "youtube player initialized");

                loadSnippet();
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                Log.e(TAG, "onInitializationFailure: " + youTubeInitializationResult.toString() );
                Snackbar failureNotification = Snackbar
                        .make(mMainview, "YouTube unable to initialize", Snackbar.LENGTH_LONG);
                failureNotification.show();
            }
        });

        mStopHandler = new Handler();
        mStopRunnable = new Runnable() {
            @Override
            public void run() {
                if (mPlayer != null && mSnippets != null
                        && mPlayer.getCurrentTimeMillis() >= mSnippets.get(mIndex).endTime * Constants.MILLIS_PER_SECOND){

                    mPlayer.seekToMillis(mSnippets.get(mIndex).startTime * Constants.MILLIS_PER_SECOND);
                    mPlayer.pause();

                    if (((SnippetViewActivity) getActivity()).isAutoPlayEnabled()){
                        nextSnippet();
                    }

                }

                mStopHandler.postDelayed(this,Constants.MILLIS_PER_SECOND);
            }
        };
        mStopHandler.postDelayed(mStopRunnable, Constants.MILLIS_PER_SECOND);


        //recieve entity url from deeplink or intent
        Intent intent = getActivity().getIntent();
        if (intent.getAction() == Intent.ACTION_VIEW && intent.getDataString() != null){
            String url = intent.getDataString();
            Log.d(TAG, "onCreateView from deeplink: " + url);
            mURL = url.substring(url.indexOf(".com/") + 4);
            mURL = mURL.substring(0, mURL.length()-1);
            mURL += ".json";
        } else {
            mURL = intent.getExtras().getString(Constants.URL_KEY);
        }

        Log.d(TAG, "onCreate: json url:" + mURL);

        //GET playlist information from http in AsyncTask
        new getPlaylist().execute();

        return mMainview;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPlayer = null;
        mStopHandler.removeCallbacks(mStopRunnable);
    }

    private void nextSnippet(){
        Log.d(TAG, "nextSnippet: requested");
        if (mSnippets == null || mIndex >= mSnippets.size() -1 ) return;
        mIndex += 1;
        loadSnippet(true);
    }

    private void prevSnippet(){
        Log.d(TAG, "prevSnippet: requested");
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

        Snippet snpt = mSnippets.get(mIndex);

        Log.d(TAG, "loadSnippet: " + snpt);

        //load new video at starting time
        mPlayer.loadVideo(snpt.videoID, snpt.startTime * Constants.MILLIS_PER_SECOND);

        //change title and notes text
        mNotesText.setText(snpt.notes != null ? snpt.notes : "");
        mTitleText.setText(snpt.title);

        //correctly enable/dissable next/prev buttons
        mPrevButton.setVisibility(mIndex > 0 ? View.VISIBLE : View.INVISIBLE);
        mNextButton.setVisibility((mIndex < mSnippets.size() - 1) ? View.VISIBLE : View.INVISIBLE);
    }

    public String getSnippetUrl() {
        if (mSnippets != null && ! mSnippets.isEmpty())
            return mSnippets.get(mIndex).url;
        else
            return null;
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
                if (mSnippets == null || mSnippets.size() == 0) {
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
                            snippet.isNull("notes") ? null : snippet.getString("notes"),
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

        return playlistList.isEmpty() ? null : playlistList;
    }
}

    public Bundle getSerializedSnippet(){
        Bundle output = new Bundle();
        Snippet snpt = mSnippets.get(mIndex);
        output.putString("VideoID", snpt.videoID);
        output.putString("url", snpt.url);
        output.putString("title", snpt.title);
        output.putString("startTime", "" +snpt.startTime);
        output.putString("endTime", "" + snpt.endTime);
        if (snpt.notes != null) output.putString("notes", snpt.notes);
        return output;
    }

    class myGuestureDetect extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 2000;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.d(TAG, "onFling: velocityX:" + velocityX);
            if (velocityX  < -SWIPE_THRESHOLD){
                nextSnippet();
            } else if (velocityX > SWIPE_THRESHOLD){
                prevSnippet();
            }
            return true;
        }
    }

    public NetworkFetcher.APICallFactory deleteSnippet =
            new NetworkFetcher.APICallFactory("DELETE", new NetworkFetcher.NetworkFinish() {
        @Override
        public void onNetworkResponse(int responseCode, String responseMsg) {
            if (responseCode == 202) {
                // success
                Snackbar sucessNotification = Snackbar
                        .make(mMainview, "Snippet Deleted", Snackbar.LENGTH_LONG);
                sucessNotification.show();

                mSnippets.remove(mIndex);

                if (mSnippets.isEmpty()){
                    mSnippets = null;
                    mNotesText.setText("");
                    mTitleText.setText("");
                    mPlayer.release();
                    mNextButton.setVisibility(View.INVISIBLE);
                    mPrevButton.setVisibility(View.INVISIBLE);
                } else {
                    if (mIndex == mSnippets.size()) mIndex--;
                    loadSnippet();
                }
            } else {
                Snackbar failureNotification = Snackbar
                        .make(mMainview, "Unable to Delete Snippet", Snackbar.LENGTH_LONG);
                failureNotification.show();
            }
        }
    });

    public NetworkFetcher.APICallFactory createSnippet =
            new NetworkFetcher.APICallFactory("POST", new NetworkFetcher.NetworkFinish() {
        @Override
        public void onNetworkResponse(int responseCode, String responseMsg) {
            if (responseCode == 201) {
                // success
                Snippet snpt = null;

                try {
                    JSONObject response = new JSONObject(responseMsg);
                    JSONObject snippet = response.getJSONObject("snippet");
                    snpt = new Snippet(
                            snippet.getString("videoID"),
                            snippet.getString("url"),
                            snippet.getString("title"),
                            snippet.isNull("notes") ? null : snippet.getString("notes"),
                            snippet.getInt("startTime"),
                            snippet.getInt("endTime"));

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (snpt == null){
                    Snackbar failureNotification = Snackbar
                            .make(mMainview, "Unable to Parse Response from Server", Snackbar.LENGTH_LONG);
                    failureNotification.show();
                } else {
                    Snackbar successNotification = Snackbar
                            .make(mMainview, "Snippet Created and Added to End of Playlist", Snackbar.LENGTH_LONG);
                    successNotification.show();

                    if (mSnippets == null){
                        mSnippets = new ArrayList<>();
                    }

                    mIndex = mSnippets.size();
                    mSnippets.add(snpt);
                    loadSnippet();
                }

            } else {
                // failure
                Snackbar failureNotification = Snackbar
                        .make(mMainview, "Unable to Create Snippet", Snackbar.LENGTH_LONG);
                failureNotification.show();
            }
        }
    });

    public NetworkFetcher.APICallFactory editSnippet =
        new NetworkFetcher.APICallFactory("PUT", new NetworkFetcher.NetworkFinish() {
            @Override
            public void onNetworkResponse(int responseCode, String responseMsg) {
                if (responseCode == 200) {
                    // success
                    Snippet snpt = null;

                    try {
                        JSONObject response = new JSONObject(responseMsg);
                        String url = response.getString("url");
                        JSONObject snippet = response.getJSONObject("snippet");
                        snpt = new Snippet(
                                snippet.getString("videoID"),
                                url,
                                snippet.getString("title"),
                                snippet.isNull("notes") ? null : snippet.getString("notes"),
                                snippet.getInt("startTime"),
                                snippet.getInt("endTime"));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (snpt == null){
                        Snackbar failureNotification = Snackbar
                                .make(mMainview, "Unable to Parse Response from Server", Snackbar.LENGTH_LONG);
                        failureNotification.show();
                    } else {
                        Snackbar successNotification = Snackbar
                                .make(mMainview, "Snippet Updated", Snackbar.LENGTH_LONG);
                        successNotification.show();

                        mSnippets.set(mIndex, snpt);
                        loadSnippet();
                    }

                } else {
                    // failure
                    Snackbar failureNotification = Snackbar
                            .make(mMainview, "Unable to Update Snippet", Snackbar.LENGTH_LONG);
                    failureNotification.show();
                }
            }
    });

    @Override
    public void onResume() {

        loadSnippet();

        super.onResume();
    }
}
