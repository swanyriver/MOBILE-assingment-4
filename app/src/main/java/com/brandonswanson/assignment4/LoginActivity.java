package com.brandonswanson.assignment4;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private TextView mCurrentUserText;
    private LinearLayout mLoginLayout;
    private LinearLayout mLogOutLayout;
    private LinearLayout mMainLayout;
    private EditText mNameEdit;
    private EditText mPasswordEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mCurrentUserText = (TextView) findViewById(R.id.current_user_text);
        mLoginLayout = (LinearLayout) findViewById(R.id.login_layout);
        mLogOutLayout = (LinearLayout) findViewById(R.id.logout_layout);
        mMainLayout = (LinearLayout) findViewById(R.id.login_main_layout);
        mNameEdit = (EditText) findViewById(R.id.name_edit_text);
        mPasswordEdit = (EditText) findViewById(R.id.password_edit_text);

        if (Credentials.getsInstance().isUserLoggedIn()){
            mCurrentUserText.setText(
                    "Currently logged in as:" + Credentials.getsInstance().userName());
            mLoginLayout.setVisibility(View.GONE);
            mLogOutLayout.setVisibility(View.VISIBLE);
        }

    }

    private String getPostParams(){
        return "name=" + mNameEdit.getText().toString() +
                "&password=" + mPasswordEdit.getText().toString();
    }

    public void signInButton(View view) {
        makeCallToServer("/login");
    }

    public void registerButton(View view) {
        makeCallToServer("/register");
    }

    public void makeCallToServer(String url){
        //todo progress indication disable buttons, layout disable doesnt visbly work
        mLoginLayout.setEnabled(false);
        loginAPI.execute(url, getPostParams());
    }


    public void logoutButton(View view) {
        Credentials.getsInstance().logOutUser();
        mLoginLayout.setVisibility(View.VISIBLE);
        mLogOutLayout.setVisibility(View.GONE);
    }

    private NetworkFetcher.APICallFactory loginAPI =
            new NetworkFetcher.APICallFactory("POST", new NetworkFetcher.NetworkFinish() {
        @Override
        public void onNetworkResponse(int responseCode, String responseMsg) {

            Log.d("LOGIN", "onNetworkResponse: code:" + responseCode);
            Log.d("LOGIN", "onNetworkResponse: json:" + responseMsg);

            mLoginLayout.setEnabled(true);

            if (responseCode == -1) {
                makeSnack("Unable to connect to server");
                return;
            }

            if (responseCode != 200){
                String msg = null;
                try {
                    JSONObject result = new JSONObject(responseMsg);
                    msg = result.getString("msg");
                } catch (JSONException e) {
                    e.printStackTrace();
                    makeSnack("Unable to log in");
                    return;
                }
                makeSnack(msg);
                return;
            }

            try {
                JSONObject result = new JSONObject(responseMsg);
                String name = result.getString("username");
                String token = result.getString("token");
                String id = "" + result.getLong("userid");
                String msg = result.getString("msg");
                Credentials.getsInstance().logInUser(name,id,token);
                Intent data = new Intent();
                data.putExtra("msg", msg);
                Log.d("LOGIN", "onNetworkResponse: exiting login with message" + msg);
                setResult(RESULT_OK, data);
                finish();
            } catch (JSONException e) {
                e.printStackTrace();
                makeSnack("Unable to log in");
            }
        }
    });

    private void makeSnack(String msg){
        Snackbar failureNotification = Snackbar
                .make(mMainLayout, msg, Snackbar.LENGTH_LONG);
        failureNotification.show();
    }
}
