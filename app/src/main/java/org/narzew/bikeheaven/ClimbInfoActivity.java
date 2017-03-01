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

public class ClimbInfoActivity extends ActionBarActivity implements OnItemClickListener {
	
	public String PREFS_NAME = "AltimetrPrefs";
	private DrawerLayout drawerLayout;
	private ListView listView; // For Navigation Drawer
	private ListView climbList; // For Climb List
	private String[] menuitems;
	Context context = this;
	Integer climb_id; // Climb id
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
		climb_id = b.getInt("climb_id");
		
        DBHelper dbhelper = new DBHelper(context);
        Cursor cursor = dbhelper.getClimb(climb_id);
        cursor.moveToFirst();
        // Dopasowanie do pól w layoucie
        TextView climb_name = (TextView)findViewById(R.id.climb_name);
        TextView climb_slope = (TextView)findViewById(R.id.climb_slope);
        TextView climb_region = (TextView)findViewById(R.id.climb_region);
        TextView climb_description = (TextView)findViewById(R.id.climb_description);
        TextView climb_comments = (TextView)findViewById(R.id.climb_comments);
        TextView climb_author = (TextView)findViewById(R.id.climb_author);
        TextView climb_points = (TextView)findViewById(R.id.climb_points);
        TextView climb_slope_list = (TextView)findViewById(R.id.climb_slope_list);
        TextView category_text = (TextView)findViewById(R.id.category_text);
        TextView show_map_button = (TextView)findViewById(R.id.show_map_button);
        ImageView climb_category = (ImageView)findViewById(R.id.category_pic);
        // Wypełnienie opisu trasy
        climb_name.setText(cursor.getString(1));
        setTitle(cursor.getString(1));
        climb_slope.setText(cursor.getString(2));
        climb_description.setText(cursor.getString(3));
        climb_author.setText(cursor.getString(4));
        region_id = cursor.getInt(10); // Global variable
        climb_region.setText(dbhelper.get_region_name(region_id));
        String comments = cursor.getString(11);
        if(comments.length()<5){
			comments = context.getString(R.string.climbinfo_nocomments);
		}
		long stravasegment = cursor.getLong(12);
		TextView strava_segment_text = (TextView)findViewById(R.id.strava_text);
		Button strava_segment_button = (Button)findViewById(R.id.strava_button);
		if(stravasegment==0){
			// Ukryj przyciski Stravy jeśli w bazie jest segment
			strava_segment_text.setVisibility(View.GONE);
			strava_segment_button.setVisibility(View.GONE);
		} else {
			// Pokaż przyciski Stravy
			strava_segment_text.setVisibility(View.VISIBLE);
			strava_segment_button.setVisibility(View.VISIBLE);
		}
		climb_comments.setText(comments);
        String points = Double.toString(cursor.getDouble(5));
        climb_points.setText(points);
        // Ustaw przycisk w zależności od coords mode
		show_map_button.setText(context.getString(R.string.climb_map));
        // Pokaż obrazek kategorii
        String category = dbhelper.get_category(cursor.getDouble(5));
        Integer category_resid = dbhelper.get_category_resid(category);
        climb_category.setImageResource(category_resid);
		category_text.setText(context.getString(R.string.climbinfo_category2) +" " + category);
        // Wyświetl nachylenia
        String slope_str = dbhelper.getSlopesStr(cursor.getInt(0));
        climb_slope_list.setText(slope_str);
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
	
	public void show_climb_map(View v){
		Intent intent = new Intent(ClimbInfoActivity.this, ClimbMapActivity.class);
		intent.putExtra("climbmap_id", climb_id);
		startActivity(intent);
	}
	
	public void show_region_info(View v){
		Intent intent = new Intent(this, RegionInfoActivity.class);
		intent.putExtra("region_id", region_id);
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
	
	public void show_strava_segment(View w){
		Bundle b = getIntent().getExtras();
		climb_id = b.getInt("climb_id");
		DBHelper dbhelper = new DBHelper(context);
		Cursor cursor = dbhelper.getClimb(climb_id);
		cursor.moveToFirst();
		String segment = cursor.getString(13);
		segment = "http://strava.com/segments/"+segment;
		Uri url = Uri.parse(segment);
		Intent open_browser = new Intent(Intent.ACTION_VIEW, url);
		startActivity(open_browser);
	}
	
	public void show_climb_page(View v){
		Bundle b = getIntent().getExtras();
		climb_id = b.getInt("climb_id");
		DBHelper dbhelper = new DBHelper(context);
		Cursor cursor = dbhelper.getClimb(climb_id);
		cursor.moveToFirst();
		String link = cursor.getString(12);
		link = "http://www.altimetr.pl/"+link+".html";
		Uri url = Uri.parse(link);
		Intent open_browser = new Intent(Intent.ACTION_VIEW, url);
		startActivity(open_browser);
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
		
}
