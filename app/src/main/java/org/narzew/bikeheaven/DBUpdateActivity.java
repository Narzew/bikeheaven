package org.narzew.bikeheaven;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ImageView;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DBUpdateActivity extends ActionBarActivity implements OnItemClickListener {

    public String PREFS_NAME = Config.PREFS_NAME;
    private DrawerLayout drawerLayout;
    private ListView listView; // For Navigation Drawer
    private String[] menuitems;
    Context context = this;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedpreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        setContentView(R.layout.app_info);

        // Screen Orientation
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

        // For Navigation Drawer
        drawerLayout=(DrawerLayout) findViewById(R.id.drawerLayout);
        menuitems=getResources().getStringArray(R.array.menu);
        listView=(ListView) findViewById(R.id.drawerList);
        MyAdapter myadapter = new MyAdapter(this, Arrays.asList(menuitems));
        listView.setAdapter(myadapter);
        listView.setOnItemClickListener(this);

        setTitle(context.getString(R.string.app_info));

    }

    public void update_database_clicked(View v){
        DBUpdateTask updatedb_task = new DBUpdateTask();
        updatedb_task.execute();
        // Update database code here
    }

    // Left Drawer processing code below

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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

    public void map_clicked(){
        Intent map_activity = new Intent(this, MapsActivity.class);
        startActivity(map_activity);
    }

    public void settings_clicked(){
        Intent settings_activity = new Intent(this, SettingsActivity.class);
        startActivity(settings_activity);
    }

    public void informations_clicked(){
        Intent informations_activity = new Intent(this, AppInfoActivity.class);
        startActivity(informations_activity);
    }

    public void selectItem(int position){
        listView.setItemChecked(position, true);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id){
        switch(position){
            case 0:
                map_clicked();
                break;
            case 1:
                settings_clicked();
                break;
            case 2:
                informations_clicked();
                break;
        }
        addDrawerListener();
        selectItem(position);
    }


    public void addDrawerListener(){
        drawerLayout=(DrawerLayout) findViewById(R.id.drawerLayout);
        menuitems=getResources().getStringArray(R.array.menu);
        listView=(ListView) findViewById(R.id.drawerList);
        MyAdapter myadapter = new MyAdapter(this, Arrays.asList(menuitems));
        listView.setAdapter(myadapter);
        listView.setOnItemClickListener(this);
    }

    public void put_int(String name, Integer value){
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt(name, value);
        editor.commit();
    }

    class MyAdapter extends ArrayAdapter<String>{
        Context c;
        List<String> lista;
        LayoutInflater inflater;
        ImageView imgView;

        public MyAdapter (Context c,List<String> list){
            super(c,R.layout.row, list);
            this.lista = list;
            this.c = c;
            inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = inflater.inflate(R.layout.drawer_list_item,null);
            imgView = (ImageView)row.findViewById(R.id.imgView);
            TextView navText = (TextView)row.findViewById(R.id.navText);
            navText.setText(lista.get(position));
            switch(lista.get(position)){
                case "Mapa podjazdów":
                    imgView.setImageDrawable(c.getResources().getDrawable(R.drawable.map));
                    break;
                case "Ustawienia":
                    imgView.setImageDrawable(c.getResources().getDrawable(R.drawable.settings));
                    break;
                case "Informacje":
                    imgView.setImageDrawable(c.getResources().getDrawable(R.drawable.info));
                    break;
            }
            return row;
        }
    }

    public class DBUpdateTask extends AsyncTask<Integer,String,String> {
        String result;
        int[] climb_id;
        Double[] points, start_x, start_y;
        String[] name, slope;

        @Override
        protected String doInBackground(Integer... params) {
            APIHelper apiHelper=new APIHelper(context);
            // Check current DB version on server
            // Compare it to local version
            // If version is different then delete whole database and download again;
            // DEBUG: Download test climbs
            result=apiHelper.get_test_climbs(30);
            parse_climb_json(result);
            return null;
        }

        @Override
        protected void onPostExecute(String i){
            super.onPostExecute(i);
        }

		/*
		Integer climb_id;
		Double points;
		LatLng coords;
		String name, slope;
		*/

        public void parse_climb_json(String jsonstr){

            Integer jsonlength;
            if (jsonstr != null){
                try {
                    JSONArray jsonArray = new JSONArray(jsonstr);
                    jsonlength = jsonArray.length();
                    climb_id = new int[jsonlength];
                    points = new Double[jsonlength];
                    start_x = new Double[jsonlength];
                    start_y = new Double[jsonlength];
                    name = new String[jsonlength];
                    slope = new String[jsonlength];

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jObject= jsonArray.getJSONObject(i);
                        climb_id[i] = jObject.getInt("id");
                        points[i] = jObject.getDouble("points");
                        start_x[i] = jObject.getDouble("start_x");
                        start_y[i] = jObject.getDouble("start_y");
                        name[i] = jObject.getString("name");
                        slope[i] = jObject.getString("slope");
                    }
                } catch(JSONException e){
                    e.printStackTrace();
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

}

