package org.narzew.bikeheaven;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;
import android.util.Log;
import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.content.SharedPreferences;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import org.narzew.bikeheaven.GPSTracker;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.AsyncTask;

public class MapsActivity extends ActionBarActivity implements OnItemClickListener {

	public GoogleMap googleMap;
	private String PREFS_NAME = "AltimetrPrefs";
	private DrawerLayout drawerLayout;
	private ListView listView; // For Navigation Drawer
	private String[] menuitems;
	Context context = this;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_layout);

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

		// For Navigation Drawer
        drawerLayout=(DrawerLayout) findViewById(R.id.drawerLayout);
        menuitems=getResources().getStringArray(R.array.menu);
        listView=(ListView) findViewById(R.id.drawerList);
        MyAdapter myadapter = new MyAdapter(this, Arrays.asList(menuitems));
        listView.setAdapter(myadapter);
        listView.setOnItemClickListener(this);
			
        // Database & Map Initialization
		try {
			initializeMap();
			// Zmień kamerę na środek podkarpacia
			changeCamera(new LatLng(49.8733977, 21.9149780));
		} catch(Exception e){
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
		GPSTracker gpstracker = new GPSTracker(this);
		Location location = gpstracker.getLocation();
		setTitle("Podjazdy Rowerowe");
		//changeCamera(new LatLng(49.570,22.209));
		addClimbMarkers();
		//changeCamera(location);
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
	
	public void addRegionMarkers(int region_id){
		double start_x,start_y, points;
		int category_min, category_max, category_int, category_resid;
		String name, slope, category, markertext;
		Boolean coords_mode, regions_mode;
		DBHelper dbhelper = new DBHelper(this);
        SharedPreferences sharedpreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        coords_mode = sharedpreferences.getBoolean("coords_mode",false);
	
		Cursor cursor = dbhelper.getRegionClimbs(region_id);
		cursor.moveToFirst();
        do {
			name = cursor.getString(1);
			slope = cursor.getString(2);
			points = cursor.getDouble(5);
			// start_x i start_y może być również współrzędnymi mety (zależność od ustawień)
			if(coords_mode == false){
				// Pokazuj start podjazdu
				start_x = cursor.getDouble(6);
				start_y = cursor.getDouble(7);
			} else {
				// Pokazuj metę podjazdu
				start_x = cursor.getDouble(8);
				start_y = cursor.getDouble(9);
			}
			markertext = name+"\n"+slope;
			category = dbhelper.get_category(points);
			// Pomiń dodanie podjazdu gdy kategoria nie jest w ustawieniach
			category_min = sharedpreferences.getInt("category_min",3);
			category_max = sharedpreferences.getInt("category_max",7);
			category_int = dbhelper.get_int_category(category);
			if(category_int<category_min||category_int>category_max){
				continue;
			}
			// Pobierz grafikę i dodaj marker
			category_resid = dbhelper.get_category_resid(category);
			addMarker(new LatLng(start_x, start_y),markertext, category_resid);
		} while(cursor.moveToNext());
	}
	
	public void addClimbMarkers(){
		double start_x,start_y, points;
		int category_min, category_max, category_int, category_resid;
		String name, slope, category, markertext;
		Boolean coords_mode, regions_mode;
		DBHelper dbhelper = new DBHelper(this);
        SharedPreferences sharedpreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        coords_mode = sharedpreferences.getBoolean("coords_mode",false);
        regions_mode = sharedpreferences.getBoolean("regions_mode", false);
        
        if(regions_mode==true){
			// Tryb regionów
			Cursor cursor = dbhelper.getAllRegions();
			cursor.moveToFirst();
			do {
				name = cursor.getString(1);
				start_x = cursor.getDouble(3);
				start_y = cursor.getDouble(4);
				markertext = "region";
				addMarker(new LatLng(start_x, start_y),markertext, R.drawable.region);
			} while(cursor.moveToNext());
			
		} else {
			// Tryb zwykły
			Cursor cursor = dbhelper.getAllClimbs();
			cursor.moveToFirst();
	        do {
				name = cursor.getString(1);
				slope = cursor.getString(2);
				points = cursor.getDouble(5);
				// start_x i start_y może być również współrzędnymi mety (zależność od ustawień)
				if(coords_mode == false){
					// Pokazuj start podjazdu
					start_x = cursor.getDouble(6);
					start_y = cursor.getDouble(7);
				} else {
					// Pokazuj metę podjazdu
					start_x = cursor.getDouble(8);
					start_y = cursor.getDouble(9);
				}
				markertext = name+"\n"+slope;
				category = dbhelper.get_category(points);
				// Pomiń dodanie podjazdu gdy kategoria nie jest w ustawieniach
				category_min = sharedpreferences.getInt("category_min",3);
				category_max = sharedpreferences.getInt("category_max",7);
				category_int = dbhelper.get_int_category(category);
				if(category_int<category_min||category_int>category_max){
					continue;
				}
				// Pobierz grafikę i dodaj marker
				category_resid = dbhelper.get_category_resid(category);
				addMarker(new LatLng(start_x, start_y),markertext, category_resid);
			} while(cursor.moveToNext());
		}
	}
	
	public void addMarker(LatLng i, String title, int category_resid){
		MarkerOptions markerOptions = new MarkerOptions().
		position(i)
		.title(title)
		.icon(BitmapDescriptorFactory.fromResource(category_resid));
		// Dodaj marker
		googleMap.addMarker(markerOptions);
		//Log.d("AltimetrDB", "Added marker "+title);
		// Dodaj listener do markera
		googleMap.setOnMarkerClickListener(new OnMarkerClickListener(){
			@Override
            public boolean onMarkerClick(Marker arg0) {
				Integer region_id, marker_id;
                LatLng position = arg0.getPosition();
				double point_x = position.latitude;
				double point_y = position.longitude;
				DBHelper dbhelper = new DBHelper(MapsActivity.this);
				// Pobierz współrzędne regionu
				region_id = dbhelper.getRegionIdLatLng(point_x, point_y);
				if (region_id == 0){
					// Marker nie jest regionem
					marker_id = dbhelper.getClimbIdLatLng(point_x, point_y);
					if(marker_id != 0){
						Intent intent = new Intent(MapsActivity.this, ClimbInfoActivity.class);
						intent.putExtra("climb_id", marker_id);
						startActivity(intent);
					}
				} else {
					// Marker jest regionem, dodaj pozostałe markery i usuń go
					arg0.setVisible(false);
					arg0.remove();
					addRegionMarkers(region_id);
				}
				return true;
			}
		});
	}
	
	public void changeCamera(LatLng coords){
          CameraPosition cameraPosition = new CameraPosition.Builder()
           .target(coords)
             .zoom(8)
             .build();
              googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
    
    public void setTitle(String title){
		getSupportActionBar().setTitle(title);
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

	public class GetPlaceData extends AsyncTask <Integer,String,String>{
		String result;
		int[] climb_id;
		Double[] points, start_x, start_y;
		String[] name, slope;

		@Override
		protected String doInBackground(Integer... params) {
			APIHelper apiHelper=new APIHelper(context);
			result=apiHelper.get_all_climbs();
			get_api_data(result);
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

		public void get_api_data(String jsonstr){

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
