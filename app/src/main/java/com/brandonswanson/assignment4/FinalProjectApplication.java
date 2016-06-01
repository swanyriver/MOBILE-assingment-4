package com.brandonswanson.assignment4;

import android.app.Application;
import android.util.Log;

import java.net.HttpURLConnection;

/**
 * Created by brandon on 5/31/16.
 */
public class FinalProjectApplication extends Application {

    private String mIDstr = null;
    private String mTokenStr = null;
    private String mUserName = null;
    private static FinalProjectApplication sInstance = null;
    
    private static String TAG = "APPLICATION";

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        sInstance = null;
    }

    static FinalProjectApplication getsInstance(){
        return sInstance;
    }

    public void logInUser(String name, String id, String token){
        mIDstr = id;
        mTokenStr = token;
        mUserName = name;
        Log.d(TAG, "logInUser: id" + id + " token:" + token);
    }

    public void logOutUser(){
        mIDstr = null;
        mTokenStr = null;
        mTokenStr = null;
        Log.d(TAG, "logOutUser");
    }

    public String getUserName(){
        return mUserName;
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
