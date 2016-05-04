package com.brandonswanson.assignment4;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Created by brandon on 5/2/16.
 */
public class NetworkFetcher {

    //do I have to use Async task, can I just start a thread in here?
    public interface networkFinishListener{
        void onResponse(int responseCode, String message);
    }


    // Boiler plate networking code with exception handling from
    // https://www.udacity.com/course/developing-android-apps--ud853
    // used as starting point

    public static String fetchJSON(String suburl){
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String JsonStr = null;

        try {
            // Construct the URL for the query
            URL url = new URL( Constants.API_ROOT + suburl);

            // Create the request to my api, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
                buffer.append("\n");
            }

            if (buffer.length() == 0) {
                return null;
            }
            JsonStr = buffer.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally{
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return JsonStr;
    }

    private static void makeAPICAll(String suburl, String Method, String postContent, networkFinishListener listener){
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String result = null;

        try {
            // Construct the URL for the query
            URL url = new URL( Constants.API_ROOT + suburl);

            // Create the request to my api, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(Method);


            //load package


            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                // Nothing to do.
                if (listener != null) listener.onResponse(Constants.NO_NETWORK_RESPONSE, "");
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
                buffer.append("\n");
            }

            result = buffer.toString();
            listener.onResponse(urlConnection.getResponseCode(), result);

        } catch (IOException e) {
            e.printStackTrace();
            if (listener != null) listener.onResponse(Constants.NO_NETWORK_RESPONSE, "");
            return;
        } finally{
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static String getHTTPPOST(Map<String,Object> map){
        //todo turn this into a k=v&
        return map.toString();
    }

    public static void deleteSnippet(final String URL, final networkFinishListener listener){
        Thread networkThread = new Thread(new Runnable() {
            @Override
            public void run() {
                makeAPICAll(URL, "DELETE", null, listener);
            }
        });
        networkThread.start();
    }
}
