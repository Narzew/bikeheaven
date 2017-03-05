package org.narzew.bikeheaven;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
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

public class ListActivity extends ActionBarActivity implements OnItemClickListener {
	
	public String PREFS_NAME = "AltimetrPrefs";
	private DrawerLayout drawerLayout;
	private ListView listView; // For Navigation Drawer
	private ListView climbList; // For Climb List
	private String[] menuitems;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedpreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        setContentView(R.layout.climb_list);
        drawerLayout=(DrawerLayout) findViewById(R.id.drawerLayout);
        menuitems=getResources().getStringArray(R.array.menu);
        // Navigation Drawer
        listView=(ListView) findViewById(R.id.drawerList);
        MyAdapter myadapter = new MyAdapter(this, Arrays.asList(menuitems));
        listView.setAdapter(myadapter);
        listView.setOnItemClickListener(this);
        // Main list
        climbList=(ListView) findViewById(R.id.climbList);
        // Initialzie db
        DBHelper dbhelper = new DBHelper(ListActivity.this);
        dbhelper.initialize_database();
        Cursor cursor = dbhelper.getAllClimbs();
        
        // Create links
        ArrayList<Climb> list = new ArrayList<Climb>();
        cursor.moveToFirst();
        for(int i=0;i<cursor.getCount();i++){
			list.add(new Climb(
			cursor.getInt(0),
			cursor.getString(1),
			cursor.getString(2),
			cursor.getString(3),
			cursor.getString(4),
			cursor.getDouble(5),
			cursor.getDouble(6),
			cursor.getDouble(7),
			cursor.getDouble(8),
			cursor.getDouble(9)
			));
			cursor.moveToNext();
			Log.d(Config.LOG_KEY, "Moved cursor");
		}
			
        ClimbAdapter climbadapter = new ClimbAdapter(ListActivity.this, list);
        climbList.setAdapter(climbadapter);
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
			case R.id.menu_list:
				list_clicked();
				break;
			case R.id.menu_navigation:
				navigation_clicked();
				break;
			case R.id.menu_settings:
				settings_clicked();
				break;
		}
		addDrawerListener();
		return true;
	}
	
	public void map_clicked(){
		Intent map_activity = new Intent(this, MapsActivity.class);
		startActivity(map_activity);
	}
    
    public void list_clicked(){
		// I'm in the list class!
	}
    
    public void navigation_clicked(){}
    
    public void settings_clicked(){}
	
	public void selectItem(int position){
		listView.setItemChecked(position, true);
	}
	
	public void setTitle(String title){
		getSupportActionBar().setTitle(title);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id){
		switch(position){
			case 0:
			map_clicked();
			break;
		case 1:
			list_clicked();
			break;
		case 2:
			navigation_clicked();
			break;
		case 3:
			settings_clicked();
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
    
    class ClimbAdapter extends BaseAdapter {
        Context c;
        List<Climb> lista;
        LayoutInflater inflater;
        ImageView imgView;

        public ClimbAdapter (Context c,List<Climb> list){
            this.lista = list;
            String count = new Integer(list.size()).toString(); // DEBUG
            Log.d(Config.LOG_KEY,count); // DEBUG
            this.c = c;
			inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        
        @Override
        public int getCount() {
			return lista.size();
		}
		
		@Override
		public Climb getItem(int position) {
			return lista.get(position);
		}
		
		@Override 
		public long getItemId(int position) {
			return position;
		}

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = inflater.inflate(R.layout.climb_list_item,null);
            TextView climbName = (TextView)row.findViewById(R.id.climbName);
            TextView climbSlope = (TextView)row.findViewById(R.id.climbSlope);
            Climb climb2 = (Climb)lista.get(position);
            climbName.setText(climb2.getName());
            climbSlope.setText("   "+climb2.getSlope());
            /*
            switch(lista.get(position)){
				case "Mapa podjazdów":
					imgView.setImageDrawable(c.getResources().getDrawable(R.drawable.map));
					break;
				case "Lista podjazdów":
					imgView.setImageDrawable(c.getResources().getDrawable(R.drawable.map));
					break;
				case "Nawigacja":
					imgView.setImageDrawable(c.getResources().getDrawable(R.drawable.navigation));
					break;
				case "Ustawienia":
					imgView.setImageDrawable(c.getResources().getDrawable(R.drawable.settings));
					break;
			}
			*/
            return row;
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
				case "Lista podjazdów":
					imgView.setImageDrawable(c.getResources().getDrawable(R.drawable.map));
					break;
				case "Nawigacja":
					imgView.setImageDrawable(c.getResources().getDrawable(R.drawable.map));
					break;
				case "Ustawienia":
					imgView.setImageDrawable(c.getResources().getDrawable(R.drawable.settings));
					break;
			}		
            return row;
       }
    }
    
}