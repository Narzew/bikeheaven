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
 * Activity used to recover user password
 */

public class RecoverPasswordActivity extends ActionBarActivity {
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
    private String PREFS_NAME = Config.PREFS_NAME;
    /**
     * Reference to field containing email entered by user
     */
    private EditText emailField;
    /**
     * Reference to field containing password entered by user
     */
    private EditText passwordField;
    /**
     * Reference to field containing repeated password entered by user
     */
    private EditText passwordField2;
    /**
     * Field containing user email
     * Default empty
     */
    String email = "";
    /**
     * Field containing user password
     * Default empty
     */
    String password = "";
    /**
     * Field containing repeated user password
     * Default empty
     */
    String password2 = "";
    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState Saved Instance State
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recoverpassword);
        emailField = (EditText) findViewById(R.id.email_field);
        passwordField = (EditText) findViewById(R.id.password_field);
        passwordField2 = (EditText) findViewById(R.id.password_field2);
    }

    /**
     * Called when user clicks Recover Password button
     *
     * @param view View reference
     */


    public void recoverPasswordClicked(View view) {
        int errors = 0;
        email = emailField.getText() + "";
        password = passwordField.getText() + "";
        password2 = passwordField2.getText() + "";

        APIHelper apihelper = new APIHelper(context);
        Config config = new Config(context);

        if (!password.equals(password2)) {
            Log.d(LOG_KEY, context.getResources().getString(R.string.passwords_mismatch));
            errors++;
        }

        if (apihelper.invalidLength(email, config.LOGIN_MIN_CHARS, config.LOGIN_MAX_CHARS)) {
            Log.d(LOG_KEY, context.getResources().getString(R.string.invalid_email_length));
            errors++;
        }
        if (errors != 0) {
            // Errors occured
            Toast.makeText(this, context.getResources().getString(R.string.correct_form_errors), Toast.LENGTH_LONG).show();
        } else {
            // No errors
            Toast.makeText(this, context.getResources().getString(R.string.no_errors), Toast.LENGTH_LONG).show();
            new InternetConnect().execute();
        }

    }

    public class InternetConnect extends AsyncTask<String, Integer, Integer> {

        Integer response_code;

        @Override
        protected Integer doInBackground(String... strings) {
            DigestHash digesthash = new DigestHash(context);

            password = digesthash.sha1(password);
            APIHelper apihelper = new APIHelper(context);
            apihelper.send_password_request(email, password);
            return null;
        }

        @Override
        protected void onPostExecute(Integer i) {
            super.onPostExecute(i);
            SharedPreferences sp = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            if (sp.getBoolean("HttpConnect", true)) {
                response_code = sp.getInt("request_result", -1);
                if (response_code == 0) {
                    Toast.makeText(context, context.getResources().getString(R.string.new_password_sent), Toast.LENGTH_LONG).show();
                } else if (response_code == 1) {
                    Toast.makeText(context, context.getResources().getString(R.string.invalid_password_length), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, context.getResources().getString(R.string.unknown_error), Toast.LENGTH_LONG).show();

                }
            } else {
                no_internet_alert();
            }

        }
    }
    public void no_internet_alert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
        builder.setTitle(context.getResources().getString(R.string.no_internet));
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}


