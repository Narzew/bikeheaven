package org.narzew.bikeheaven;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class LogOutActivity extends AppCompatActivity {

    SharedPreferences sp;
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_out);
        sp = context.getSharedPreferences(Config.PREFS_NAME, Context.MODE_PRIVATE);
        // Reset id and authkey values
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("id", 0);
        editor.putString("authkey", "");
        editor.apply();
        // Move to main activity
        Intent i = new Intent(context, MainActivity.class);
        startActivity(i);
        Toast.makeText(context, "Wylogowano pomyślnie.", Toast.LENGTH_LONG);
    }
}
