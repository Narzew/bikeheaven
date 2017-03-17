package org.narzew.bikeheaven;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import android.util.Log;

/**
 * Activity used to login user
 */

public class LoginActivity extends ActionBarActivity
{
    /**
     * Class context
     */
    protected Context context = this;
    /**
     * Variable containing key to logcat tool
     */
    private String LOG_KEY = Config.LOG_KEY;
    /**
     * Variable containing SharedPreferences variable
     */
    public String PREFS_NAME = Config.PREFS_NAME;
    /**
     * Reference to field containing login entered by user
     */
    private EditText loginField;
    /**
     * Reference to field containing password entered by user
     */
    private EditText passwordField;
    /**
     * Field containing user login
     * Default empty
     */
    String login = "";
    /**
     * Field containing user password
     * Default empty
     */
    String password = "";

    /** Called when the activity is first created.
     *
     * @param savedInstanceState Saved Instance State
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        loginField = (EditText)findViewById(R.id.login_field);
        passwordField = (EditText)findViewById(R.id.password_field);
    }

    /** Called when user hasn't internet connection
     */
    public void no_internet_alert(){
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        builder.setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Powtórzenie akcji łączenia sie z serwerem
                new InternetConnect().execute();
                dialog.cancel();
            }
        });
        builder.setNegativeButton(context.getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setMessage(context.getResources().getString(R.string.no_internet2));
        builder.setTitle (context.getString(R.string.no_internet));
        AlertDialog alertDialog=builder.create();
        alertDialog.show();
    }

    /** Called when user clicks Login button
     * Not completed
     *
     * @param view View reference
     */
    public void loginClicked(View view){
        login = loginField.getText()+"";
        password = passwordField.getText()+"";
        // Validate data
        new InternetConnect().execute();

    }

    /** Called when user clicks Recover Password button
     * Opens {@link RecoverPasswordActivity}
     *
     * @param view View reference
     */

    public void recoverPasswordClicked(View view){
        Intent intent = new Intent(context, RecoverPasswordActivity.class);
        startActivity(intent);
    }

    public class InternetConnect extends AsyncTask<String,Integer,Integer> {

        Integer response_code;

        @Override
        protected Integer doInBackground(String ... strings) {
            DigestHash digestHash = new DigestHash(context);
            String hashPass;
            hashPass = digestHash.sha1(password);
            APIHelper apihelper = new APIHelper(context);
            apihelper.login(login, hashPass);
            return null;
        }

        @Override
        protected void onPostExecute (Integer i ){
            super.onPostExecute(i);
            SharedPreferences sharedpreferences=context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            if (!sharedpreferences.getBoolean("HttpConnect", true)) {
                // Brak netu
                no_internet_alert();
            } else {
                // Sprawdź odpowiedź
                response_code = sharedpreferences.getInt("login_result", -1);
                if(response_code == 0){
                    // Access granted
                    startActivity(new Intent(context, UserMenuActivity.class));
                    finish();
                    //startActivity(new Intent(context,EventsMapActivity.class));
                    // Do login process
                } else if(response_code == 2){
                    // Account inactive
                    Toast.makeText(context, context.getResources().getString(R.string.account_inactive), Toast.LENGTH_LONG).show();
                } else if(response_code == 4){
                    Toast.makeText(context, context.getResources().getString(R.string.access_denied), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, context.getResources().getString(R.string.unknown_error), Toast.LENGTH_LONG).show();
                }
            }
        }

    }

}
