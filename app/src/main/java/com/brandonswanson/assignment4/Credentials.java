package com.brandonswanson.assignment4;

import android.app.Application;
import android.util.Log;

import java.net.HttpURLConnection;

/**
 * Created by brandon on 5/31/16.
 */
public class Credentials extends Application {

    private String mIDstr = null;
    private String mTokenStr = null;
    private String mUserName = null;
    private static Credentials sInstance = null;
    
    private static String TAG = "CREDENTIALS";

    private Credentials(){

    }

    public static Credentials getsInstance(){
        if (sInstance == null){
            sInstance = new Credentials();
        }
        return sInstance;
    }

    public void logInUser(String name, String id, String token){
        mIDstr = id;
        mTokenStr = token;
        mUserName = name;
        Log.d(TAG, "logInUser: id:" + id + " token:" + token);
    }

    public void logOutUser(){
        mIDstr = null;
        mTokenStr = null;
        mUserName = null;
        Log.d(TAG, "logOutUser");
    }

    public String userName(){
        return mUserName;
    }

    public boolean belongsToLoggedInUser(String name){
        Log.d(TAG, "belongsTologgedInUser: " + name + "==" + mUserName + ":" + (name.equals(mUserName)));
        return this.isUserLoggedIn() && name.equals(mUserName);
    }

    public boolean isUserLoggedIn(){
        return mIDstr != null && mTokenStr != null && mUserName != null;
    }

    public void addAuthenticationToCall(HttpURLConnection urlConnection){
        if(mIDstr != null && mTokenStr != null && mUserName != null) {
            urlConnection.setRequestProperty("id", mIDstr);
            urlConnection.setRequestProperty("token", mTokenStr);
            Log.d(TAG, "addAuthenticationToCall: adding auth to url" + urlConnection.getRequestProperties().toString());
        }
    }

}
