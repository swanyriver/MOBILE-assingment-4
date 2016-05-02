package com.brandonswanson.assignment4;

import android.os.AsyncTask;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String JsonStr = null;

            try {
                // Construct the URL for the query
                URL url = new URL( Constants.API_ROOT + "/playlist.json");

                // Create the request to my api, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                JsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }

            return JsonStr;
        }

        @Override
        protected void onPostExecute(String s) {
            //Log.d("ASYNC", s);

            try {
                JSONArray plists = new JSONArray(s);
                for(int n = 0; n < plists.length(); n++)
                {
                    JSONObject object = plists.getJSONObject(n);
                    Log.d("ASYNC", "onPostExecute: " + object.toString());

                    layout.addView(new PlaylistRow(layout.getContext(), object.getString("title") + "-" + object.getString("creator")));
                }

                Log.d("ASYNC", "onPostExecute: " + layout.getChildCount());


            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }
}
