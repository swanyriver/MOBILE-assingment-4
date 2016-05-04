package com.brandonswanson.assignment4;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class SnippetViewActivity extends AppCompatActivity {

    private static final String TAG = "SNIPPET_VIEW_ACTIVITY";
    private Toolbar mToolbar;
    private MenuItem mAutoplayView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snippet_view);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void setTitle(String title){
        mToolbar.setTitle(title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_snippet_view, menu);

        mAutoplayView = menu.findItem(R.id.action_autoplay);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        SnippetViewActivityFragment myFragment = (SnippetViewActivityFragment)
                getSupportFragmentManager().findFragmentById(R.id.snippet_fragment);

        String snippetUrl = myFragment.getSnippetUrl();

        if (snippetUrl == null && item.getItemId() != R.id.action_autoplay){
            return super.onOptionsItemSelected(item);
        }

        Log.d(TAG, "onOptionsItemSelected: snippet URL:" + snippetUrl);

        switch (item.getItemId()){
            case R.id.action_autoplay:
                item.setChecked(!item.isChecked());
                break;
            case R.id.action_add_snippet:
                Log.d(TAG, "onOptionsItemSelected: add snippet");
                break;
            case R.id.action_delete_snippet:
                Log.d(TAG, "onOptionsItemSelected: delete snippet");
                break;
            case R.id.action_edit_snippet:
                Log.d(TAG, "onOptionsItemSelected: edit snippet");
                break;
        }


        return super.onOptionsItemSelected(item);
    }

    public boolean isAutoPlayEnabled(){
        return mAutoplayView.isChecked();
    }
}
