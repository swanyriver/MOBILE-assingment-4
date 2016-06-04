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

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private static final String TAG = "MAIN_FRAG" ;
    public LinearLayout layout;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        layout = (LinearLayout) view.findViewById(R.id.playlistListView);

        return view;
    }

    private class GetAllPlaylists extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... params) {
            return NetworkFetcher.fetchJSON(Constants.PLAYLIST_LIST + params[0]);
        }

        @Override
        protected void onPostExecute(String s) {

            Snackbar failureNotification = Snackbar
                    .make(layout, "Playlists are currently Unavailable", Snackbar.LENGTH_LONG);

            Log.d("MAIN ACTIVITY", "onPostExecute: " + s);

            if (s == null){
                failureNotification.show();
            } else {
                layout.removeAllViews();
                for (PlaylistRow.PlaylistPreview plist:
                        PlaylistRow.PlaylistPreview.PlaylistPreviewFactory(s)) {
                    layout.addView(new PlaylistRow(layout.getContext(), plist, MainActivityFragment.this));
                }

                if (layout.getChildCount() == 0) {
                    if (Credentials.getsInstance().isUserLoggedIn()) {
                        showAddPlaylistSuggestion();
                    } else {
                        failureNotification.show();
                    }
                }
            }
        }
    }

    private void showAddPlaylistSuggestion() {
        //todo show add playlist suggestion
        Log.d(TAG, "showAddPlaylistSuggestion: called");
        layout.removeAllViews();
    }

    private void showLogInSuggestion() {
        Log.d(TAG, "showLogInSuggestion: called");
        layout.removeAllViews();
        View signInView = View.inflate(getContext(), R.layout.sign_in_pop_up, null);
        signInView.findViewById(R.id.pop_up_sign_in_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((MainActivity) getActivity()).launchLogin(MainActivity.LOG_IN_TO_ADD_REQUEST);
                    }
                });
        layout.addView(signInView);
    }

    public void refreshPlaylists(){
        if (((MainActivity) getActivity()).showingPublic()){
            new GetAllPlaylists().execute("?public");
        } else {
            if(!Credentials.getsInstance().isUserLoggedIn()){
                showLogInSuggestion();
            } else {
                new GetAllPlaylists().execute("");
            }
        }
    }
}
