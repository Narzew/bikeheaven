package org.narzew.bikeheaven;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class PlacesList extends AppCompatActivity {

    Context context = this;
    ListView mainlist;
    SharedPreferences sp;
    APIHelper apihelper = new APIHelper(context);
    JSONArray jsonArray;
    Integer ary_size;
    ArrayList<Place> places;
    PlacesAdapter placesadapter;
    Integer filter_mode = 0;
    Bundle b;
    Integer id;
    String authkey;
    Location location1;
    LatLng location;
    DrawerLayout drawerLayout;
    String[] menuitems;
    ListView listView;
    final int REPEAT_TIME=1000*10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places_list);
        sp = context.getSharedPreferences(Config.PREFS_NAME, MODE_PRIVATE);
        b = getIntent().getExtras();
        filter_mode = b.getInt("filter_mode");
        id = sp.getInt("id", 0);
        authkey = sp.getString("authkey", "");
        GPSTracker gpStracker = new GPSTracker(context);
        Location location1 = gpStracker.getLocation();
        location = new LatLng(location1.getLatitude(), location1.getLongitude());

        // Start AlarmManager
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, Alarm.class);
        PendingIntent pending = PendingIntent.getBroadcast(context, 0, i,0);
        Calendar cal = Calendar.getInstance();
        // Start 60 seconds after boot completed
        cal.add(Calendar.SECOND, Config.ALARM_WAIT_TIME);
        //
        // Fetch every 60 seconds
        // InexactRepeating allows Android to optimize the energy consumption
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                cal.getTimeInMillis(), Config.REPEAT_TIME, pending);

        try {
            // Select mainlist ListView
            mainlist = (ListView) findViewById(R.id.mainlist);
            new InternetConnection().execute();
        } catch(Exception e){
            e.printStackTrace();
        }

    }

    /**
     * Show all events
     */

    public void show_all_places(Integer filter_mode){
        try {
            id = sp.getInt("id", 0);
            authkey = sp.getString("authkey", "");
            switch(filter_mode){
                case 0:
                    // All places
                    jsonArray = new JSONArray(apihelper.get_all_places());
                    break;
                case 1:
                    // Visited places
                    jsonArray = new JSONArray(apihelper.get_visited_places(id, authkey));
                    break;
                case 2:
                    // Unvisited places
                    jsonArray = new JSONArray(apihelper.get_unvisited_places(id, authkey));
                    break;
                case 3:
                    // All places near you
                    jsonArray = new JSONArray(apihelper.get_all_places_near_you(location));
                    break;
                case 4:
                    // Visited places
                    jsonArray = new JSONArray(apihelper.get_visited_places_near_you(id, authkey, location));
                    break;
                case 5:
                    // Unvisited places
                    jsonArray = new JSONArray(apihelper.get_unvisited_places_near_you(id, authkey, location));
                    break;
                default:
                    jsonArray = new JSONArray();
            }
            ary_size = jsonArray.length();
            places = new ArrayList<Place>();
            for (int i = 0; i < ary_size; i++) {
                try {
                    // Error below
                    JSONObject jObject = jsonArray.getJSONObject(i);
                    places.add(new Place(
                            jObject.getInt("id"),
                            jObject.getString("name"),
                            jObject.getString("description"),
                            jObject.getInt("category"),
                            new LatLng(jObject.getDouble("lat"),jObject.getDouble("lng"))
                    ));
                    // Error up
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class InternetConnection extends AsyncTask<String, Integer, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            try {
                show_all_places(filter_mode);
                return null;
            } catch(Exception e){
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Integer i2) {
            super.onPostExecute(i2);

            if(sp.getBoolean("HttpConnect",false)) {
                placesadapter = new PlacesAdapter(context, places);
                mainlist.setAdapter(placesadapter);
            }else {
                no_internet_alert();
            }
        }

        /** Called when user hasn't internet connection
         */
        public void no_internet_alert(){
            AlertDialog.Builder builder=new AlertDialog.Builder(context);
            builder.setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Powtórzenie akcji łączenia sie z serwerem
                    new InternetConnection().execute();
                    dialog.cancel();
                }
            });
            builder.setNegativeButton(context.getString(R.string.no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.setMessage(context.getResources().getString(R.string.no_internet2));
            builder.setTitle(context.getResources().getString(R.string.no_internet));
            AlertDialog alertDialog=builder.create();
            alertDialog.show();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu){
        SharedPreferences sp = context.getSharedPreferences(Config.PREFS_NAME, MODE_PRIVATE);
        id = sp.getInt("id",0);
        if(id>0){
            // User is logged in
            getMenuInflater().inflate(R.menu.menu2, menu);
        } else {
            // User is not logged in
            getMenuInflater().inflate(R.menu.menu1, menu);
        }

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        switch(id){
            case R.id.menu_todiscover:
                undiscovered_clicked();
                break;
            case R.id.menu_settings:
                settings_clicked();
                break;
            case R.id.menu_about:
                about_clicked();
                break;
            case R.id.menu_discovered:
                discovered_clicked();
                break;
            case R.id.menu_login:
                login_clicked();
                break;
            case R.id.menu_register:
                register_clicked();
                break;
            case R.id.menu_remind_pasword:
                remind_password_clicked();
                break;
        }
        addDrawerListener();
        return true;
    }

    public void setTitle(String title){
        getSupportActionBar().setTitle(title);
    }

    public void login_clicked(){
        Intent login_activity = new Intent(this, LoginActivity.class);
        startActivity(login_activity);
    }

    public void register_clicked(){
        Intent register_activity = new Intent(this, RegisterActivity.class);
        startActivity(register_activity);
    }

    public void remind_password_clicked(){
        Intent remind_password = new Intent(this, RecoverPasswordActivity.class);
        startActivity(remind_password);
    }

    public void undiscovered_clicked(){
        Intent undiscovered_intent = new Intent(this, PlaceMapActivity.class);
        // Undiscovered places near you
        undiscovered_intent.putExtra("filter_type", 0);
        startActivity(undiscovered_intent);
    }

    public void discovered_clicked(){
        Intent discovered_intent = new Intent(this, PlaceMapActivity.class);
        // Undiscovered places near you
        discovered_intent.putExtra("filter_type", 1);
        startActivity(discovered_intent);
    }

    public void settings_clicked(){
        Intent settings_activity = new Intent(this, SettingsActivity.class);
        startActivity(settings_activity);
    }

    public void quests_clicked() {
        Intent quest_activity = new Intent(this, PlaceQuestActivity.class);
        startActivity(quest_activity);
    }

    public void about_clicked(){
        Intent about_activity = new Intent(this, AppInfoActivity.class);
        startActivity(about_activity);
    }

    public void informations_clicked(){
        Intent informations_activity = new Intent(this, PlacesList.class);
        informations_activity.putExtra("filter_mode", 1);
        startActivity(informations_activity);
    }

    public void logout_clicked(){
        Intent logout_activity = new Intent(this, LogOutActivity.class);
        startActivity(logout_activity);
    }

    public void selectItem(int position){
        listView.setItemChecked(position, true);
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id){
        switch(position){
            case 0:
                // Undiscovered places
                undiscovered_clicked();
                break;
            case 1:
                // Discovered places
                discovered_clicked();
                break;
            case 2:
                quests_clicked();
                break;
            case 3:
                settings_clicked();
                break;
            case 4:
                about_clicked();
                break;
            case 5:
                logout_clicked();
                break;
        }
        addDrawerListener();
        selectItem(position);
    }


    public void addDrawerListener(){
        drawerLayout=(DrawerLayout) findViewById(R.id.drawerLayout);
        menuitems=getResources().getStringArray(R.array.menu);
        listView=(ListView) findViewById(R.id.drawerList);
        org.narzew.bikeheaven.MyAdapter myadapter = new org.narzew.bikeheaven.MyAdapter(this, Arrays.asList(menuitems));
        listView.setAdapter(myadapter);
        listView.setOnItemClickListener((AdapterView.OnItemClickListener) this);
    }

    public void put_int(String name, Integer value){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Config.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt(name, value);
        editor.commit();
    }

    public void put_bool(String name, Boolean value){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Config.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean(name, value);
        editor.commit();
    }

}
