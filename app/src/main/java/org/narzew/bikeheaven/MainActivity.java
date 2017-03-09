package org.narzew.bikeheaven;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    Integer climb_id;
    EditText edittext_climb_id;

    void climb_map_clicked(View v){
        Intent intent = new Intent(MainActivity.this, ClimbMapActivity.class);
        startActivity(intent);
    }

    void settings_clicked(View v){
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    void appinfo_clicked(View v){
        Intent intent = new Intent(MainActivity.this, AppInfoActivity.class);
        startActivity(intent);
    }

    void update_database_clicked(View v){
    }

    void climb_info_clicked(View v){
        Integer climb_id = Integer.parseInt(edittext_climb_id.getText().toString());
        Intent intent = new Intent(MainActivity.this, ClimbInfoActivity.class);
        intent.putExtra("climb_id", 1);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        edittext_climb_id = (EditText)findViewById(R.id.edittext_climb_id);
    }

}
