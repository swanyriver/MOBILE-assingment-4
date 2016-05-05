package com.brandonswanson.assignment4;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class SnippetViewActivity extends AppCompatActivity {

    private static final String TAG = "SNIPPET_VIEW_ACTIVITY";
    public static final int CREATE_SNIPPET_ACTIVITY = 101;
    private static final int EDIT_SNIPPET_ACTIVITY = 102;
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

    //todo could be moved to fragment if necessary
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        final SnippetViewActivityFragment myFragment = (SnippetViewActivityFragment)
                getSupportFragmentManager().findFragmentById(R.id.snippet_fragment);

        final String snippetUrl = myFragment.getSnippetUrl();

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
                Intent intent = new Intent(this, AddEditSnippetActivity.class);
                startActivityForResult(intent, CREATE_SNIPPET_ACTIVITY);
                break;
            case R.id.action_delete_snippet:
                Log.d(TAG, "onOptionsItemSelected: delete snippet");

                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle("Delete Snippet");
                alertDialog.setMessage("Would you like to delete this snippet?");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("DIALOG", "delete snippet: clicked yes");
                        myFragment.deleteSnippet.execute(snippetUrl);
                    }
                });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("DIALOG", "delete snippet: clicked no");
                        //do nothing
                    }
                });
                alertDialog.show();

                break;
            case R.id.action_edit_snippet:
                Log.d(TAG, "onOptionsItemSelected: edit snippet");
                Intent editIntent = new Intent(this, AddEditSnippetActivity.class);
                editIntent.putExtra("url", snippetUrl);
                startActivityForResult(editIntent, EDIT_SNIPPET_ACTIVITY);
                break;
        }


        return super.onOptionsItemSelected(item);
    }

    public boolean isAutoPlayEnabled(){
        return mAutoplayView.isChecked();
    }
}
