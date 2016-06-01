package com.brandonswanson.assignment4;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by brandon on 5/2/16.
 */
public class NetworkFetcher {

    // Boiler plate networking code with exception handling from
    // https://www.udacity.com/course/developing-android-apps--ud853

    // used as starting point and expanded to included POST, PUT, and DELETE request
    // as well as returning an object with both response code and response string by Brandon Swanson

    public static String fetchJSON(String suburl){
        return fetchJSON(Constants.API_ROOT, suburl);
    }
    public static String fetchJSON(String rootURL, String suburl){

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String JsonStr = null;

        try {
            // Construct the URL for the query
            URL url = new URL( rootURL + suburl);

            // Create the request to my api, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");

            Credentials.getsInstance().addAuthenticationToCall(urlConnection);

            Log.d("NETWORK", "fetchJSON: " + urlConnection.toString());

            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
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

    // Called with "GET", "POST", "DELETE", or "POST" as method
    // if method === "POST" postContent is urlEncoded kv string "key=value&key=value" else null
    private static NetworkResponse makeAPICAll(String suburl, String method, String postContent){

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String result = null;

        try {
            // Construct the URL for the query
            URL url = new URL( Constants.API_ROOT + suburl);
            Log.d("NETWORK", "makeAPICAll: " + url.toString());

            // Create the request to my api, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(method);

            Credentials.getsInstance().addAuthenticationToCall(urlConnection);

            //load POST contents
            if (postContent != null) {
                byte[] postData = postContent.getBytes();
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                urlConnection.setRequestProperty("Content-Length", String.valueOf(postData.length));
                urlConnection.setDoOutput(true);
                urlConnection.getOutputStream().write(postData);
            } else {
                urlConnection.connect();
            }

            // Read the input stream into a String
            int statusCode = urlConnection.getResponseCode();
            InputStream inputStream = (statusCode == 400 || statusCode == 401)
                    ? urlConnection.getErrorStream()
                    : urlConnection.getInputStream();

            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                return new NetworkResponse(Constants.NO_NETWORK_RESPONSE, "");
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
                buffer.append("\n");
            }

            result = buffer.toString();
            return new NetworkResponse(statusCode, result);


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

    // create URL formatted data key=value string from activity intent bundle
    public static String getHTTPPOST(Bundle extras){

        StringBuilder output = new StringBuilder();
        String prefix = "";

        for (String key : extras.keySet()) {
            String value = extras.get(key).toString();

            output.append(prefix);
            prefix = "&";
            output.append(URLEncoder.encode(key));
            output.append("=");
            output.append(URLEncoder.encode(value));
        }

        return output.toString();
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

    public static class APICallFactory {
        private String mMethod;
        private NetworkFetcher.NetworkFinish mNetworkListener;

        public APICallFactory (String method, NetworkFetcher.NetworkFinish networkFinish){
            mMethod = method;
            mNetworkListener = networkFinish;
        }

        public void execute(String... params){
            new APICall(mMethod, mNetworkListener).execute(params);
        }
    }

    public static class APICall extends AsyncTask <String, Void, NetworkResponse> {

        private String mMethod;
        private NetworkFetcher.NetworkFinish mNetworkListener;

        public APICall (String method, NetworkFetcher.NetworkFinish networkFinish){
            mMethod = method;
            mNetworkListener = networkFinish;
        }

        @Override
        // Receive SubURL and optional POST/PUT data string for making network call
        protected NetworkResponse doInBackground(String... urls) {
            String url = urls[0];
            String post = null;

            if (urls.length > 1){
                post = urls[1];
            }

            Log.d("Network", "doInBackground post params:" + post);

            return makeAPICAll(url, mMethod, post);
        }

        @Override
        protected void onPostExecute(NetworkResponse response) {
            Log.d("Network", String.format("onPostExecute: %d %s", response.responseCode, response.msg));
            mNetworkListener.onNetworkResponse(response.responseCode, response.msg);
        }
    }
}
