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
import android.os.Bundle;
import android.net.Uri;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
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

public class RegionInfoActivity extends ActionBarActivity implements OnItemClickListener {
	
	public String PREFS_NAME = "AltimetrPrefs";
	private DrawerLayout drawerLayout;
	private ListView listView; // For Navigation Drawer
	private ListView climbList; // For Climb List
	private String[] menuitems;
	Context context = this;
	Integer region_id; // Region id
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.climb_info);
        SharedPreferences sharedpreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    
		Boolean coords_mode = sharedpreferences.getBoolean("coords_mode", false);
    
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
		
		// Get Climb Id
		Bundle b = getIntent().getExtras();
		region_id = b.getInt("region_id");
		
        DBHelper dbhelper = new DBHelper(context);
        
        // Dopasowanie do pól w layoucie
        TextView region_name = (TextView)findViewById(R.id.region_name);
        TextView region_description = (TextView)findViewById(R.id.region_description);
        //ListView region_climbs = (ListView)findViewById(R.id.region_climb_list);
        
        // Wypełnienie danymi
        
        Log.d("AltimetrDB", "Region ID: "+region_id);
        Cursor region_data = dbhelper.getRegion(region_id);
        if(region_data != null && region_data.moveToFirst()){
			region_name.setText(region_data.getString(0));
			region_description.setText(region_data.getString(1));
		}
        
    }
    
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
	
	public void show_region_map(View v){
		Intent intent = new Intent(this, RegionMapActivity.class);
		intent.putExtra("regionmap_id", region_id);
		startActivity(intent);
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
            String l = lista.get(position);
            if(l==context.getString(R.string.menu_map)){
				imgView.setImageDrawable(c.getResources().getDrawable(R.drawable.map));
			} else if(l==context.getString(R.string.menu_settings)){
				imgView.setImageDrawable(c.getResources().getDrawable(R.drawable.settings));
			} else if(l==context.getString(R.string.menu_informations)){
				imgView.setImageDrawable(c.getResources().getDrawable(R.drawable.info));
			}
            return row;
       }
    }
    
}
    
