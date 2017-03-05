package org.narzew.bikeheaven;

import java.lang.Math.*;
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

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.PolylineOptions;
import org.w3c.dom.Document;
import android.graphics.Color;

public class ClimbMapActivity extends ActionBarActivity implements OnItemClickListener {

	public GoogleMap googleMap;
	private String PREFS_NAME = "AltimetrPrefs";
	private DrawerLayout drawerLayout;
	private ListView listView; // For Navigation Drawer
	private String[] menuitems;
	Context context = this;
	Integer climbmap_id; // Id podjazdu do pokazania
	
	// Typy punktów
	Integer TYPE_START = 0;
	Integer TYPE_FINISH = 1;
	Integer TYPE_POINT = 2;
	// Zmienne do nachylenia na mapie
	Integer POINT_NORMAL = 0;
	Integer POINT_START = 1;
	Integer POINT_FINISH = 2;
	
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
        
        // Pobierz id mapy
        Bundle b = getIntent().getExtras();
		climbmap_id = b.getInt("climbmap_id");
        
        // Database & Map Initialization
		try {
			initializeMap();
			// Zmień kamerę na środek podkarpacia
			//changeCamera(new LatLng(49.8733977, 21.9149780));
		} catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	private void initializeMap(){
		if (googleMap == null){
			googleMap = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
			
			// Ustaw typ mapy (z ustawień)
			SharedPreferences sharedpreferences = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
			int map_type = sharedpreferences.getInt("map_type",2);
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
		DBHelper dbhelper = new DBHelper(this);
		setTitle(dbhelper.getClimbName(climbmap_id));
		addClimbMarkers(climbmap_id);
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
	
	public void addClimbMarkers(int climb_id){
		SharedPreferences sharedpreferences = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		boolean coords_mode = sharedpreferences.getBoolean("coords_mode", false);
		DBHelper dbhelper = new DBHelper(this);
		Cursor climb_data = dbhelper.getClimb(climb_id);
		climb_data.moveToFirst();
		// Change camera to start point
		if(coords_mode == false){
			changeCamera(new LatLng(climb_data.getDouble(6),climb_data.getDouble(7)));
		} else {
			changeCamera(new LatLng(climb_data.getDouble(8),climb_data.getDouble(9)));
		}
		// Add start and finish markers
		addMarker(new LatLng(climb_data.getDouble(6),climb_data.getDouble(7)),dbhelper.getPointSlope(climb_id, 0, POINT_START), TYPE_START);
		addMarker(new LatLng(climb_data.getDouble(8),climb_data.getDouble(9)),dbhelper.getPointSlope(climb_id, 0, POINT_FINISH), TYPE_FINISH);
		// Draw climb points
		Cursor climb_points = dbhelper.getClimbPoints(climb_id);
		climb_points.moveToFirst();
		// Wylicz nachylenia
		do {
			addMarker(new LatLng(climb_points.getDouble(2), climb_points.getDouble(3)),dbhelper.getPointSlope(climb_id, climb_points.getInt(1),POINT_NORMAL),TYPE_POINT);
		} while(climb_points.moveToNext());
		Log.d(Config.LOG_KEY, "Drawing polylines");
		drawPolylines(climb_id);
	}
	
	public void drawPolylines(int climb_id){
		SharedPreferences sharedpreferences = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		DBHelper dbhelper = new DBHelper(context);
		Cursor climb_points = dbhelper.getClimbPoints(climb_id);
		climb_points.moveToFirst();
		Cursor climb_slopes = dbhelper.getClimbSlopes(climb_id);
		climb_slopes.moveToFirst();
		Cursor climb_data = dbhelper.getClimb(climb_id);
		climb_data.moveToFirst();
		double tmp_x = climb_points.getDouble(2);
		double tmp_y = climb_points.getDouble(3);
		LatLng start_coords;
		LatLng end_coords;
		int count = 0;
		PolylineOptions rectLine;
		int poly_width = sharedpreferences.getInt("polyline_width", 10);
		int slope;
		double elev1 = 0.0, elev2 = 0.0;
		// Add points rectline
		do {
			if(count==0){
				tmp_x = climb_points.getDouble(2);
				tmp_y = climb_points.getDouble(3);
				// Add start rectline
				climb_slopes.moveToFirst();
				elev1 = climb_slopes.getDouble(2);
				climb_slopes.moveToPosition(1);
				elev2 = climb_slopes.getDouble(2);
				slope = (int)Math.round(elev2-elev1);
				rectLine = new PolylineOptions().width(poly_width).color(dbhelper.getColorBySlope(slope));
				rectLine.add(new LatLng(climb_data.getDouble(6), climb_data.getDouble(7)), new LatLng(tmp_x, tmp_y));
				googleMap.addPolyline(rectLine);
				count++;
				continue;
			} else {
				// Metoda rozdzielająca
				start_coords = new LatLng(tmp_x, tmp_y);
				end_coords = new LatLng(climb_points.getDouble(2), climb_points.getDouble(3));
				// Calculate slopes
				if(elev1 == 0.0){
					climb_slopes.moveToPosition(count);
					elev1 = climb_slopes.getDouble(2);
				}
				climb_slopes.moveToPosition(count);
				elev2 = climb_slopes.getDouble(2);
				slope = (int)Math.round(elev2-elev1);
				
				// Draw polyline
				rectLine = new PolylineOptions().width(poly_width).color(dbhelper.getColorBySlope(slope));
				rectLine.add(start_coords, end_coords);
				googleMap.addPolyline(rectLine);
				
				// Rest code
				tmp_x = climb_points.getDouble(2);
				tmp_y = climb_points.getDouble(3);
				elev1 = elev2;
				count++;
			}
		} while(climb_points.moveToNext());
		// Add finish rectline
		climb_slopes.moveToLast();
		
		elev2 = climb_slopes.getDouble(2);
		climb_slopes.moveToPrevious();
		elev1 = climb_slopes.getDouble(2);
		slope = (int)Math.round(elev2-elev1);
		rectLine = new PolylineOptions().width(poly_width).color(dbhelper.getColorBySlope(slope));
		rectLine.add(new LatLng(tmp_x, tmp_y), new LatLng(climb_data.getDouble(8), climb_data.getDouble(9)));
		googleMap.addPolyline(rectLine);
	}
	
	
	/*
	public void drawPolylineBetween(LatLng start_coords, LatLng end_coords){
        GMapV2Direction md = new GMapV2Direction();
        Document doc = md.getDocument(start_coords, end_coords, GMapV2Direction.MODE_DRIVING);
        ArrayList<LatLng> directionPoint = md.getDirection(doc);
        PolylineOptions rectLine = new PolylineOptions().width(5).color(Color.BLUE);
        for(int i = 0 ; i < directionPoint.size() ; i++) {          
			rectLine.add(directionPoint.get(i));
		}
		googleMap.addPolyline(rectLine);
	}
	*/
	
	public void addMarker(LatLng i, String title, int type){
		MarkerOptions markerOptions = new MarkerOptions().
		position(i)
		.title(title)
		.icon(BitmapDescriptorFactory.fromResource(get_point_type_resid(type)));
		// Dodaj marker
		googleMap.addMarker(markerOptions);
	}
	
	public void changeCamera(LatLng coords){
          CameraPosition cameraPosition = new CameraPosition.Builder()
           .target(coords)
             .zoom(14)
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
	
	public Integer get_point_type_resid(int type){
		switch(type){
			case 0: // Start
				return R.drawable.start;
			case 1: // Meta
				return R.drawable.finish;
			case 2: // Punkt
				return R.drawable.point;
			default:
				return 0;
		}
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
