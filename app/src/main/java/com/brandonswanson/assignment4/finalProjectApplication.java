package com.brandonswanson.assignment4;

import android.app.Application;

import java.net.HttpURLConnection;

/**
 * Created by brandon on 5/31/16.
 */
public class finalProjectApplication extends Application {

    private int mID = 0;
    private String mTokenStr = null;
    private String mUserName = null;

    public void setUser(String name, int id, String token){
        mID = id;
        mTokenStr = token;
        mUserName = name;
    }

    public void logOutUser(){
        mID = 0;
        mTokenStr = null;
        mTokenStr = null;
    }

    public String getUserName(){
        return mUserName;
    }

    public void addAuthenticationToCall(HttpURLConnection urlConnection){
        if(mID != 0 && mTokenStr != null && mUserName != null) {
            urlConnection.setRequestProperty("id", Integer.toString(mID));
            urlConnection.setRequestProperty("token", mTokenStr);
        }
    }

}
