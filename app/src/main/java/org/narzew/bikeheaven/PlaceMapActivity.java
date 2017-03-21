package org.narzew.bikeheaven;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Activity showing map on events
 */

public class PlaceMapActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {

    private String PREFS_NAME = Config.PREFS_NAME;
    private String LOG_KEY = Config.LOG_KEY;
    Bundle b;
    public GoogleMap googleMap;
    public ArrayList<Place> places;
    Context context = this;
    SharedPreferences sp;
    Integer id;
    String authkey;
    Integer filter_mode = 0;
    Integer ary_size;
    JSONObject jObject;
    Integer clicked_id;
    LatLng position;
    MarkerOptions markerOptions;
    LatLng location;
    private DrawerLayout drawerLayout;
    private ListView listView; // For Navigation Drawer
    private String[] menuitems;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);
        b = this.getIntent().getExtras();
        filter_mode = b.getInt("filter_mode",0);
        GPSTracker gpstracker = new GPSTracker(context);
        Location location1 = gpstracker.getLocation();
        location = new LatLng(location1.getLatitude(), location1.getLongitude());

        // Screen Orientation
        SharedPreferences sharedpreferences = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int screen_orientation = sharedpreferences.getInt("screen_orientation", 0);
        switch(screen_orientation){
            case 0:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case 1:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            default:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

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

        // Database & Map Initialization
        try {
            initializeMap();
            // Zmień kamerę na miejsce gdzie się znajdujesz
            changeCamera(location1);
        } catch(Exception e){
            e.printStackTrace();
        }

        drawerLayout=(DrawerLayout) findViewById(R.id.drawerLayout);
        menuitems=getResources().getStringArray(R.array.menu);
        listView=(ListView) findViewById(R.id.drawerList);
        MyAdapter myadapter = new MyAdapter(this, Arrays.asList(menuitems));
        listView.setAdapter(myadapter);
        listView.setOnItemClickListener(this);

    }



    public void addMarker(LatLng i, String title, int category_resid){
        markerOptions = new MarkerOptions().position(i)
                .title(title)
                .icon(BitmapDescriptorFactory.fromResource(category_resid));;
        googleMap.addMarker(markerOptions);
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener(){
            @Override
            public boolean onMarkerClick(Marker arg0){
                position = arg0.getPosition();
                return true;
                // Apply position to given event (TODO)
            }
        });
    }

    private void show_places(){
        sp = getSharedPreferences(Config.PREFS_NAME, MODE_PRIVATE);
        try {
            id = sp.getInt("id", 0);
            authkey = sp.getString("authkey", "");
            APIHelper apihelper = new APIHelper(context);
            JSONArray jsonArray;
            // Here can be result based on category etc, currently all places
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
                    // Visited places near you
                    jsonArray = new JSONArray(apihelper.get_visited_places_near_you(id, authkey, location));
                    break;
                case 5:
                    // Unvisited places near you
                    jsonArray = new JSONArray(apihelper.get_unvisited_places_near_you(id, authkey, location));
                    break;
                default:
                    Log.d(Config.LOG_KEY, "No id parameter, getting default one");
                    // DEBUG: Remove that
                    jsonArray = new JSONArray(apihelper.get_all_places());
                    //jsonArray = new JSONArray();
            }

            ary_size = jsonArray.length();
            Log.d(Config.LOG_KEY, "Ary size = " + ary_size);
            places = new ArrayList<Place>();
            for (int i = 0; i < ary_size; i++) {
                try {
                    Log.d(Config.LOG_KEY, "Adding event");
                    // Error below
                    jObject = jsonArray.getJSONObject(i);
                    places.add(new Place(
                            jObject.getInt("id"),
                            jObject.getString("name"),
                            jObject.getString("description"),
                            jObject.getInt("cat"),
                            new LatLng(jObject.getDouble("lat"),jObject.getDouble("lng"))
                    ));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeMap(){
        if (googleMap == null){
            googleMap = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();

            // Ustaw typ mapy (z ustawień)
            SharedPreferences sharedpreferences = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            int map_type = sharedpreferences.getInt("map_type",1);
            switch(map_type){
                case 0:
                    // Normalna mapa
                    googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    break;
                case 1:
                    // Satelita (z napisami)
                    googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    break;
                case 2:
                    // Satelita (bez napisów)
                    googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    break;
                case 3:
                    // Teren
                    googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                    break;
                default:
                    googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }

            googleMap.setMyLocationEnabled(true);
        }
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        //setTitle("Mapa wydarzeń");
        new AddMapMarkers().execute();
    }

    @Override
    protected void onResume(){
        super.onResume();
        initializeMap();
    }

    @Override
    public void onBackPressed(){
        finish();
    }

    public void changeCamera(Location location){
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(location.getLatitude(),location.getLongitude()))
                .zoom(16)
                .build();

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
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

    public void map_clicked(){
        Intent map_activity = new Intent(this, MapsActivity.class);
        startActivity(map_activity);
    }

    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        switch(id){
            case R.id.menu_map:
                map_clicked();
                break;
            case R.id.menu_settings:
                settings_clicked();
                break;
            case R.id.menu_informations:
                informations_clicked();
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
        listView.setOnItemClickListener(this);
    }

    public void put_int(String name, Integer value){
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt(name, value);
        editor.commit();
    }

    public void put_bool(String name, Boolean value){
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean(name, value);
        editor.commit();
    }

    public class AddMapMarkers extends AsyncTask<String, Integer, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            places = new ArrayList<Place>();
            show_places();
            return 0;
        }

        @Override
        protected void onPostExecute(Integer i2) {
            // Dodanie markerów w pętli
            for(Place e: places){
                Log.d(Config.LOG_KEY, "Adding marker");
                addMarker(e.getCoords(), e.getName(), e.getCategoryResId());
            }
        }
    }

}

