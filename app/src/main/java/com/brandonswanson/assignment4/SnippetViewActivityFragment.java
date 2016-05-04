package com.brandonswanson.assignment4;

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
    private boolean mAutoPlay = false;


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
                Log.d(TAG, "onTouch: touchytouchy");
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
                Snackbar failureNotification = Snackbar
                        .make(mMainview, "YouTube unable to initialize", Snackbar.LENGTH_LONG);
                failureNotification.show();
                Log.e(TAG, "onInitializationFailure: " + youTubeInitializationResult.toString() );

            }
        });

        mStopHandler = new Handler();
        mStopRunnable = new Runnable() {
            @Override
            public void run() {
                if (mPlayer != null && mSnippets != null
                        && mPlayer.getCurrentTimeMillis() >= mSnippets.get(mIndex).endTime * Constants.MILLIS_PER_SECOND){

                    if (mAutoPlay){
                        nextSnippet();
                    } else {
                        mPlayer.seekToMillis(mSnippets.get(mIndex).startTime * Constants.MILLIS_PER_SECOND);
                        mPlayer.pause();
                    }

                }

                mStopHandler.postDelayed(this,Constants.MILLIS_PER_SECOND);
            }
        };
        mStopHandler.postDelayed(mStopRunnable, Constants.MILLIS_PER_SECOND);


        mURL = getActivity().getIntent().getExtras().getString(Constants.URL_KEY);

        Log.d(TAG, "onCreate: url:" + mURL);

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

        //stop previous handler
        //start handler again but watching for new time

        //change title and notes text
        mNotesText.setText(snpt.notes != null ? snpt.notes : "");
        mTitleText.setText(snpt.title);

        //correctly enable/dissable next/prev buttons
        mPrevButton.setVisibility(mIndex > 0 ? View.VISIBLE : View.INVISIBLE);
        mNextButton.setVisibility((mIndex < mSnippets.size() - 1) ? View.VISIBLE : View.INVISIBLE);
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

                    try {
                        JSONObject playlist = new JSONObject(s);
                        getActivity().setTitle(playlist.getString("title"));
                    } catch (JSONException e) {
                        Log.e(TAG, "onPostExecute: unable to change title");
                        e.printStackTrace();
                    }

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
}
