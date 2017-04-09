package org.narzew.bikeheaven;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
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
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView; 
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.CheckBox;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.Spinner;
import android.widget.LinearLayout;

import org.narzew.bikeheaven.RangeSeekBar;
import org.narzew.bikeheaven.RangeSeekBar.OnRangeSeekBarChangeListener;

public class SettingsActivity extends ActionBarActivity implements OnItemClickListener {
	
	public String PREFS_NAME = "AltimetrPrefs";
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
        setContentView(R.layout.settings);
        
        // Screen orientation
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
        
        // Wybór typu mapy (Spinner)
        Spinner maptypespinner = (Spinner)findViewById(R.id.maptype_spinner);
        ArrayAdapter<CharSequence> spinadapter = ArrayAdapter.createFromResource(this, R.array.maptypes, android.R.layout.simple_spinner_item);
        spinadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        maptypespinner.setAdapter(spinadapter);
        maptypespinner.setSelection(sharedpreferences.getInt("map_type", 1));
        maptypespinner.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
				switch(position){
					case 0:
						// Mapa drogowa
						put_int("map_type", 0);
						break;
					case 1:
						// Mapa satelitarna z napisami
						put_int("map_type", 1);
						break;
					case 2:
						// Mapa satelitarna
						put_int("map_type", 2);
						break;
					case 3:
						// Mapa terenowa
						put_int("map_type", 3);
						break; 
					default:
						put_int("map_type", 1);
				}
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> parent){
			}
			
		});
		
		// Wybór typu wysokości (Spinner)
		Spinner elevtypespinner = (Spinner)findViewById(R.id.elevtype_spinner);
        ArrayAdapter<CharSequence> spinadapterelev = ArrayAdapter.createFromResource(this, R.array.elevtypes, android.R.layout.simple_spinner_item);
        spinadapterelev.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        elevtypespinner.setAdapter(spinadapterelev);
        elevtypespinner.setSelection(sharedpreferences.getInt("elev_type", 0));
        elevtypespinner.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
				switch(position){
					case 0:
						// Właściwa wysokość
						put_int("elev_type", 0);
						break;
					case 1:
						// Przebyta wysokość
						put_int("elev_type", 1);
						break;
					case 2:
						// Pozostała wysokość
						put_int("elev_type", 2);
						break;
					default:
						put_int("elev_type", 0);
				}
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> parent){
			}
			
		});
			
		// Wybór typu zaokrąglania nachyleń (Spinner)
		Spinner sloperoundspinner = (Spinner)findViewById(R.id.sloperound_spinner);
        ArrayAdapter<CharSequence> spinadapterround = ArrayAdapter.createFromResource(this, R.array.sloperoundtypes, android.R.layout.simple_spinner_item);
        spinadapterround.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sloperoundspinner.setAdapter(spinadapterround);
        sloperoundspinner.setSelection(sharedpreferences.getInt("sloperound_type", 1)-1);
        sloperoundspinner.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
				switch(position){
					case 0:
						// 1 miejsce po przecinku
						put_int("sloperound_type", 1);
						break;
					case 1:
						// 2 miejsca po przecinku
						put_int("sloperound_type", 2);
						break;
					case 2:
						// 3 miejsca po przecinku
						put_int("sloperound_type", 3);
						break;
					default:
						put_int("sloperound_type", 1);
				}
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> parent){
			}
			
		});
		
		// Wybór orientacji ekranu (Spinner)
		
		// Wybór typu zaokrąglania nachyleń (Spinner)
		Spinner orientationspinner = (Spinner)findViewById(R.id.orientation_spinner);
        ArrayAdapter<CharSequence> orientspinadapter = ArrayAdapter.createFromResource(this, R.array.orientationtypes, android.R.layout.simple_spinner_item);
        orientspinadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        orientationspinner.setAdapter(orientspinadapter);
        orientationspinner.setSelection(sharedpreferences.getInt("screen_orientation", 0));
        orientationspinner.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
				switch(position){
					case 0:
						// orientacja pionowa
						put_int("screen_orientation", 0);
						break;
					case 1:
						// orientacja pozioma
						put_int("screen_orientation", 1);
						break;
					default:
						put_int("sloperound_type", 0);
				}
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> parent){
			}
			
		});
		
		// CheckBox - Pokazuj metę podjazdu
		
		CheckBox checkbox_meta = (CheckBox)findViewById(R.id.checkbox_meta);
		CheckBox checkbox_regions = (CheckBox)findViewById(R.id.checkbox_regions);
		// Ustaw wartośći CheckBox w zależności od zapisanych danych
		if(sharedpreferences.getBoolean("coords_mode",false)==false){
			checkbox_meta.setChecked(false);
		} else {
			checkbox_meta.setChecked(true);
		}
		if(sharedpreferences.getBoolean("regions_mode",false)==false){
			checkbox_regions.setChecked(false);
		} else {
			checkbox_regions.setChecked(true);
		}
		
		// Range SeekBar z kategoriami
		
		//RangeSeekBar rangeseekbar = (RangeSeekBar)findViewById(R.id.categories_rangeseekbar);
		RangeSeekBar<Integer> rangeseekbar = new RangeSeekBar<Integer>(context);
		rangeseekbar.setRangeValues(1,9);
		rangeseekbar.setSelectedMinValue(sharedpreferences.getInt("category_min", 3));
		rangeseekbar.setSelectedMaxValue(sharedpreferences.getInt("category_max", 7));
		set_seekbar_text(sharedpreferences.getInt("category_min", 3),sharedpreferences.getInt("category_max", 7));
		rangeseekbar.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener<Integer>(){
			@Override
			public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer min, Integer max){
				set_seekbar_text(min, max);
				put_int("category_min", min);
				put_int("category_max", max);
			}
		});
		LinearLayout main_layout = (LinearLayout)findViewById(R.id.main_layout);
		main_layout.addView(rangeseekbar);
        
        // For Navigation Drawer
        drawerLayout=(DrawerLayout) findViewById(R.id.drawerLayout);
        menuitems=getResources().getStringArray(R.array.menu);
        listView=(ListView) findViewById(R.id.drawerList);
        MyAdapter myadapter = new MyAdapter(this, Arrays.asList(menuitems));
        listView.setAdapter(myadapter);
        listView.setOnItemClickListener(this);
    }
    
    public void set_seekbar_text(Integer min, Integer max){
		TextView rangeseekbar_text = (TextView)findViewById(R.id.rangeseekbar_text);
		String min2, max2;
		if(min == 1){
			min2 = "HC";
		} else if(min == 2){
			min2 = "1+";
		} else {
			min = min-2;
			min2 = min.toString();
		}
		if(max == 1){
			max2 = "HC";
		} else if(max == 2){
			max2 = "1+";
		} else {
			max = max-2;
			max2 = max.toString();
		}
		rangeseekbar_text.setText(context.getString(R.string.settings_cat1) + " " + min2 + " "+context.getString(R.string.settings_cat2) + " " +max2);
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
	
	public void put_bool(String name, Boolean value){
		SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedpreferences.edit();
		editor.putBoolean(name, value);
		editor.commit();
	}
	
	public void checkbox_clicked(View v){
		boolean checked = ((CheckBox)v).isChecked();
		switch(v.getId()){
			case R.id.checkbox_meta:
				// Show finish
				if(checked){
					put_bool("coords_mode", true);
				} else {
					put_bool("coords_mode", false);
				}
			case R.id.checkbox_regions:
				if(checked){
					put_bool("regions_mode", true);
				} else {
					put_bool("regions_mode", false);
				}
		}
	}
    
}
