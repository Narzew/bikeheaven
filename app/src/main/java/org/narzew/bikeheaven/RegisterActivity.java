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
 * Activity used to register user
 */

public class RegisterActivity extends ActionBarActivity{
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
     * Reference to field containing repeated password entered by user
     */
    private EditText passwordField2;
    /**
     * Reference to field containing email entered by user
     */
    private EditText emailField;
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
    /**
     * Field containing repeated user password
     * Default empty
     */
    String password2 = "";
    /**
     * Field containing user email
     * Default empty
     */
    String email = "";

    /** Called when the activity is first created.
     *
     * @param savedInstanceState Saved Instance State
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        loginField = (EditText)findViewById(R.id.login_field);
        passwordField = (EditText)findViewById(R.id.password_field);
        passwordField2 = (EditText)findViewById(R.id.password_field2);
        emailField = (EditText)findViewById(R.id.email_field);
    }
    public void no_internet_alert(){
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        builder.setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // powtórzenie akcji łączenia sie z serwerem
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
        builder.setMessage(context.getString(R.string.no_internet));
        AlertDialog alertDialog=builder.create();
        alertDialog.show();
    }

    /** Called when user clicks Register button
     * Not completed
     *
     * @param view View reference
     */

    public void registerClicked(View view) {
        int errors = 0;
        login = loginField.getText() + "";
        password = passwordField.getText() + "";
        password2 = passwordField2.getText() + "";
        email = emailField.getText() + "";
        APIHelper apihelper = new APIHelper(context);
        Config config = new Config(context);

        if (!password.equals(password2)) {
            Toast.makeText(this, context.getResources().getString(R.string.passwords_mismatch), Toast.LENGTH_LONG).show();
            errors++;
        }

        if (email.contains(" ")) {
            Toast.makeText(this, context.getResources().getString(R.string.email_with_spaces), Toast.LENGTH_LONG).show();
            errors++;
        }
        if (!email.contains("@")) {
            errors++;
            Toast.makeText(this, context.getResources().getString(R.string.email_not_correct), Toast.LENGTH_LONG).show();
        }

        if (login.length() < config.LOGIN_MIN_CHARS) {
            errors++;
            Toast.makeText(this, context.getResources().getString(R.string.login_too_short), Toast.LENGTH_LONG).show();
        } else if (login.length() > config.LOGIN_MAX_CHARS) {
            errors++;
            Toast.makeText(this, context.getResources().getString(R.string.login_too_long), Toast.LENGTH_LONG).show();
        }


        if (password.length() < config.PASSWORD_MIN_CHARS) {
            errors++;
            Toast.makeText(this, context.getResources().getString(R.string.password_too_short), Toast.LENGTH_LONG).show();
        }
        if (password.length() > config.PASSWORD_MAX_CHARS) {
            errors++;
            Toast.makeText(this, context.getResources().getString(R.string.password_too_long), Toast.LENGTH_LONG).show();
        }

        if (apihelper.invalidLength(email, config.EMAIL_MIN_CHARS, config.EMAIL_MAX_CHARS)) {
            Log.d(LOG_KEY, context.getResources().getString(R.string.invalid_email_length));
            errors++;
        }
        if (errors == 0) {
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
            apihelper.register(login, password, email);
            return null;
        }

        @Override
        protected void onPostExecute(Integer i) {
            super.onPostExecute(i);
            SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            if (!sharedpreferences.getBoolean("HttpConnect", true)) {
                // Brak netu
                no_internet_alert();
            } else {
                // Sprawdź odpowiedź
                response_code = sharedpreferences.getInt("register_result", -1);
                if (response_code == 0) {
                    // Access granted
                    Toast.makeText(context, context.getResources().getString(R.string.register_ok), Toast.LENGTH_LONG).show();
                    startActivity(new Intent(context, MainActivity.class));
                } else if (response_code == 1) {
                    // Invalid login length
                    Toast.makeText(context, context.getResources().getString(R.string.invalid_login_length), Toast.LENGTH_LONG).show();

                } else if (response_code == 3) {
                    // Invalid email length
                    Toast.makeText(context, context.getResources().getString(R.string.invalid_email_length), Toast.LENGTH_LONG).show();
                } else if (response_code == 4) {
                    // User exist
                    Toast.makeText(context, context.getResources().getString(R.string.user_exist), Toast.LENGTH_LONG).show();
                } else if (response_code == 5) {
                    // Email exists
                    Toast.makeText(context, context.getResources().getString(R.string.email_exist), Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(context, context.getResources().getString(R.string.unknown_error), Toast.LENGTH_LONG).show();
                }
            }
        }

    }


}