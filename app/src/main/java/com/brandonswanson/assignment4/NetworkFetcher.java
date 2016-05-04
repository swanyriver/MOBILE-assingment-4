package com.brandonswanson.assignment4;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Created by brandon on 5/2/16.
 */
public class NetworkFetcher {

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

    private static NetworkResponse makeAPICAll(String suburl, String Method, String postContent){
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


            //load package  //todo make post calls


            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                // Nothing to do.
                return new NetworkResponse(Constants.NO_NETWORK_RESPONSE, "");
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
                buffer.append("\n");
            }

            result = buffer.toString();
            return new NetworkResponse(urlConnection.getResponseCode(), result);


        } catch (IOException e) {
            e.printStackTrace();
            return new NetworkResponse(Constants.NO_NETWORK_RESPONSE, "");
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

    private static class NetworkResponse {
        public final int responseCode;
        public final String msg;

        public NetworkResponse(int responseCode, String msg) {
            this.responseCode = responseCode;
            this.msg = msg;
        }
    }

    public interface NetworkFinish {
        void onNetworkResponse(int responseCode, String responseMsg);
    }

    public static class APICall extends AsyncTask <String, Void, NetworkResponse> {

        private String mMethod;
        private NetworkFetcher.NetworkFinish mNetworkListener;

        public APICall (String method, NetworkFetcher.NetworkFinish networkFinish){
            mMethod = method;
            mNetworkListener = networkFinish;
        }

        @Override
        protected NetworkResponse doInBackground(String... urls) {
            String url = urls[0];
            String post = null;

            if (urls.length > 1){
                post = urls[1];
            }

            return makeAPICAll(url, mMethod, post);
        }

        @Override
        protected void onPostExecute(NetworkResponse response) {
            mNetworkListener.onNetworkResponse(response.responseCode, response.msg);
        }
    }
}
