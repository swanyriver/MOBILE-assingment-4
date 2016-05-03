package com.brandonswanson.assignment4;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public LinearLayout layout;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        layout = (LinearLayout) view.findViewById(R.id.playlistListView);

        new getPs().execute();

        return view;
    }

    private class getPs extends AsyncTask<Void,Void,String> {

        @Override
        protected void onPreExecute() {
            Log.d("ASYNC", "pre execute");
        }

        @Override
        protected String doInBackground(Void... params) {
            return NetworkFetcher.fetchJSON(Constants.PLAYLIST_LIST);
        }

        @Override
        protected void onPostExecute(String s) {

            for (PlaylistRow.PlaylistPreview plist:
                    PlaylistRow.PlaylistPreview.PlaylistPreviewFactory(s)) {
                layout.addView(new PlaylistRow(layout.getContext(),plist));
            }

        }
    }
}
