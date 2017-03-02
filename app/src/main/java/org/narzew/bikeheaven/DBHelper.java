package org.narzew.bikeheaven;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.content.res.AssetManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

public class DBHelper {
	
	protected Context context;
	private String PREFS_NAME = "AltimetrPrefs";
	private static final String DB_NAME = "altimetr.db";
	ContextWrapper cw;
	String db_path;
	Integer DB_VERSION = 1618; // Wersja bazy danych
	
	// Zmienne do nachylenia na mapie
	Integer POINT_NORMAL = 0;
	Integer POINT_START = 1;
	Integer POINT_FINISH = 2;
	
	public DBHelper(Context context){
		//super(context, "altimetr.db", null, 1);
		this.context = context;
		cw = new ContextWrapper(context);
		db_path = cw.getFilesDir().getAbsolutePath()+ "/databases/";
	}

	public Boolean initialize_database(){
		openDatabase();
		return true;
	}
	
	public SQLiteDatabase openDatabase() {
        File dbFile = context.getDatabasePath(DB_NAME);
        Log.d("AltimetrDB","dbFile variable = "+dbFile.toString());
        if (!dbFile.exists()) {
            copyDatabase();
            put_int("db_version", DB_VERSION);
        } else {
			SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
			if(DB_VERSION>sharedpreferences.getInt("db_version",0)){
				// Nieaktualna baza danych
				copyDatabase();
				put_int("db_version", DB_VERSION);
				Log.d("AltimetrDB", "Database updated");
			} else {
				Log.d("AltimetrDB", "Database up to date");
			}
		}
        return SQLiteDatabase.openDatabase(dbFile.getPath(), null, SQLiteDatabase.OPEN_READONLY);
    }

    private void copyDatabase(){
        Log.d("AltimetrDB", "Prepare for database copying");
        String path = "/data/data/org.narzew.altimetr/databases/altimetr.db";
        File dbfolder = new File("/data/data/org.narzew.altimetr/databases");
        if(!dbfolder.exists()){
			dbfolder.mkdir();
		}
        byte[] buffer = new byte[1024];
        OutputStream myOutput = null;
        int length;
        InputStream myInput = null;
        try
        {
            myInput = context.getAssets().open(DB_NAME);
            //myOutput =new FileOutputStream(db_path + DB_NAME);
            myOutput = new FileOutputStream("/data/data/org.narzew.altimetr/databases/altimetr.db");
            while((length = myInput.read(buffer)) > 0){
                myOutput.write(buffer, 0, length);
            }
            myOutput.close();
            myOutput.flush();
            myInput.close();
            Log.d("AltimetrDB","Database copied");
        }
        catch(IOException e)
        {
			Log.d("AltimetrDB", "Fail to copy database");
            e.printStackTrace();
        }
    }
	
	public void put_int(String name, Integer value){
		SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedpreferences.edit();
		editor.putInt(name, value);
		editor.commit();
	}
	
	public String file_read(String filename){
		InputStream input;
		String text = "";
		try {
			AssetManager assetManager = context.getAssets();
			input = assetManager.open(filename);
			int size = input.available();
			byte[] buffer = new byte[size];
			input.read(buffer);
			input.close();
			text = new String(buffer);
		} catch(IOException e){
			e.printStackTrace();
		}
		return text;
	}
	
	public Cursor getAllClimbs(){
		SQLiteDatabase database = openDatabase();
		return database.rawQuery("select id, name, slope, description, author, points, start_x, start_y, end_x, end_y, region, comments, dbname, stravaseg from climbs order by id asc", null, null);
	}
	
	public Cursor getRegionClimbs(int region_id){
		SQLiteDatabase database = openDatabase();
		return database.rawQuery("select id, name, slope, description, author, points, start_x, start_y, end_x, end_y, region, comments, dbname, stravaseg from climbs where region = "+region_id+" order by id asc", null, null);
	}
	
	public Cursor getClimb(int id){
		SQLiteDatabase database = openDatabase();
		return database.rawQuery("select id, name, slope, description, author, points, start_x, start_y, end_x, end_y, region, comments, dbname, stravaseg from climbs where id = "+id, null, null);
	}
	
	public String getClimbName(int id){
		Cursor cursor = getClimb(id);
		cursor.moveToFirst();
		return cursor.getString(1);
	}
	
	public Integer getRegionIdLatLng(double lat, double lng){
		Cursor cursor;
		SQLiteDatabase database = openDatabase();
		cursor = database.rawQuery("select id from regions where center_x = "+lat+ " and center_y = "+lng , null, null);
		if(cursor.getCount()>0){
			cursor.moveToFirst();
			return cursor.getInt(0);
		} else {
			return 0;
		}
	}
	
	public Integer getClimbIdLatLng(double lat, double lng){
		Cursor cursor;
		SQLiteDatabase database = openDatabase();
		SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		Boolean coords_mode = sharedpreferences.getBoolean("coords_mode", false);
		if(coords_mode==false){
			// Współrzędne startu
			cursor = database.rawQuery("select id from climbs where start_x = "+lat+ " and start_y = "+lng , null, null);
		} else {
			// Współrzędne mety
			cursor = database.rawQuery("select id from climbs where end_x = "+lat+ " and end_y = "+lng , null, null);
		}
		if(cursor.getCount()>0){
			cursor.moveToFirst();
			return cursor.getInt(0);
		} else {
			Log.d("DEBUG", "Error getting climb id");
			return 0;
		}
	}
	
	public Cursor getClimbPoints(int id){
		SQLiteDatabase database = openDatabase();
		return database.rawQuery("select climb_id, point_nr, point_x, point_y from climb_points where climb_id = " + id + " order by point_nr asc", null, null);
	}
	
	public Cursor getClimbSlopes(int id){
		SQLiteDatabase database = openDatabase();
		return database.rawQuery("select climb_id, point_distance, elevation from climb_slopes where climb_id = " + id + " order by point_distance asc", null, null);
	}
	
	public Cursor getAllRegions(){
		SQLiteDatabase database = openDatabase();
		return database.rawQuery("select id, name, description, center_x, center_y, zoom from regions", null, null);
	}
	
	public Cursor getRegion(int id){
		SQLiteDatabase database = openDatabase();
		return database.rawQuery("select name, description, center_x, center_y, zoom from regions where id = "+id+" limit 1", null, null);
	}
	
	public String get_region_name(int id){
		SQLiteDatabase database = openDatabase();
		Cursor cursor = database.rawQuery("select name from regions where id = "+id, null, null);
		cursor.moveToFirst();
		return cursor.getString(0);
	}
	
	public double calculate_slope(double elevmin, double elevmax){
		return elevmax-elevmin;
	}
	
	public String getPointSlope(int climb_id, int point, int special){
		// special 0 => just point
		// special 1 => start
		// special 2 => finish (last point)
		SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		int roundtype = sharedpreferences.getInt("sloperound_type", 1); // Typ zaokrąglenia
		int elevtype = sharedpreferences.getInt("elev_type", 0); // Typ wysokości
		Cursor slopecursor = getClimbSlopes(climb_id);
		double elev1=0.0, elev2=0.0, elevmin=0.0, elevmax=0.0, distance=0.0, setlength = 0.0;
		String slopetext = "";
		
		// Calculate setlength, minelev, maxelev
		slopecursor.moveToFirst();
		elevmin = slopecursor.getDouble(2);
		slopecursor.moveToPosition(1);
		setlength = slopecursor.getDouble(1);
		setlength = setlength*10;
		slopecursor.moveToLast();
		elevmax = slopecursor.getDouble(2);
		
		if(special == POINT_NORMAL){
			// Zwykły punkt
			slopecursor.moveToPosition(point);
		} else if(special == POINT_START) {
			// Start
			slopecursor.moveToFirst();
		} else if(special == POINT_FINISH){
			// Meta
			slopecursor.moveToLast();
		}
		
		if(special == POINT_NORMAL){
			elev1 = slopecursor.getDouble(2);
			distance = slopecursor.getDouble(1);
			slopecursor.move(-1);
			elev2 = slopecursor.getDouble(2);
			// Pokaż nachylenie w zależności od ustawień
			switch(elevtype){
				case 0: // Właściwa wysokość
					slopetext = "#"+ point + " ("+distance+"km) "+String.valueOf(round(calculate_slope(elev2, elev1)/setlength,roundtype)) + "% ("+round(elev1, roundtype)+"m)";
					break;
				case 1: // Przebyta wysokośc
					slopetext = "#"+ point + " ("+distance+"km) "+String.valueOf(round(calculate_slope(elev2, elev1)/setlength,roundtype)) + "% (+"+round(elev1-elevmin, roundtype)+"m)";
					break;
				case 2:  // Pozostała wysokość
					slopetext = "#"+ point + " ("+distance+"km) "+String.valueOf(round(calculate_slope(elev2, elev1)/setlength,roundtype)) + "% (-"+round(elevmax-elev2, roundtype)+"m)";
					break;
				default:
					slopetext = "#"+ point + " ("+distance+"km) "+String.valueOf(round(calculate_slope(elev2, elev1)/setlength,roundtype)) + "% ("+round(elev1, roundtype)+"m)";
			}
		} else if(special == POINT_START){
			slopetext = "Start podjazdu ("+String.valueOf(round(slopecursor.getDouble(2),roundtype))+"m)";
		} else if(special == POINT_FINISH){
			elev1 = slopecursor.getDouble(2);
			distance = slopecursor.getDouble(1);
			slopecursor.move(-1);
			elev2 = slopecursor.getDouble(2);
			slopetext = "Meta podjazdu ("+distance+"km) "+String.valueOf(round(calculate_slope(elev2, elev1)/setlength,roundtype)) + "% ("+round(elev1,roundtype)+"m)";
		}
		return slopetext;
	}
	
	public String getSlopesStr(int id){
		// Pokaż listę nachyleń odcinka
		SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		int elevtype = sharedpreferences.getInt("elev_type",0); // Typ wysokości
		int roundtype = sharedpreferences.getInt("sloperound_type", 1); // Typ zaokrąglenia
		String odcinek = "";
		int count = 0;
		double nachylenie=0.0, elev1=0.0, elev2=0.0, elevmin=0.0, elevmax=0.0, elevdiff=0.0;
		double length1=0.0,length2=0.0, setlength=0.0;
		Cursor slopecursor = getClimbSlopes(id);
		// Pobierz maksymalną wysokość
		slopecursor.moveToLast();
		elevmax = slopecursor.getDouble(2);
		// Wyzeruj kursor
		slopecursor.moveToFirst();
		// Int(0) id, Double(1) dystans, Double(2) nachylenie
		do {
			// Pierwsza iteracja, nie ma poprzedniej wysokości
			if(count==0){
				elev1 = slopecursor.getDouble(2);
				length1= slopecursor.getDouble(1);
				elevmin=elev1;
				elevdiff=elevmax-elev1;
				// Pokaż przewyższenie
				odcinek = odcinek + "Przewyższenie podjazdu: "+round(elevdiff,roundtype)+"m\n";
				// Pokaż start
				odcinek = odcinek + "Start podjazdu ("+round(elev1,roundtype)+"m)\n";
				// szablon nachylenia
			} else {
				// Każda następna iteracja
				elev2 = elev1;
				if(setlength==0){
					length2 = slopecursor.getDouble(1);
					setlength = length2*10;
					//odcinek = odcinek + "Odcinki są co "+Math.round(setlength*100)+"m\n";
				}
				elev1 = slopecursor.getDouble(2);
				// Poprawka dla podjazdów z odcinkami co 200m
				nachylenie = round(calculate_slope(elev2, elev1)/setlength,roundtype);
				// nachylenie = Math.round(nachylenie);
				// dopisanie nachylenia
				switch(elevtype){
					case 0:
						// Właściwa wysokość
						odcinek = odcinek + String.valueOf(count) + " odcinek ("+String.valueOf(slopecursor.getDouble(1))+" km) o nachyleniu " + String.valueOf(nachylenie)+" % ("+String.valueOf(round(elev1,roundtype))+"m)\n";
						break;
					case 1:
						// Przebyta wysokość
						odcinek = odcinek + String.valueOf(count) + " odcinek ("+String.valueOf(slopecursor.getDouble(1))+" km) o nachyleniu " + String.valueOf(nachylenie)+" % (+"+String.valueOf(round(elev1-elevmin,roundtype))+"m)\n";
						break;
					case 2:
						// Pozostała wysokość
						odcinek = odcinek + String.valueOf(count) + " odcinek ("+String.valueOf(slopecursor.getDouble(1))+" km) o nachyleniu " + String.valueOf(nachylenie)+" % (-"+String.valueOf(round(elevmax-elev2,roundtype))+"m)\n";
						break;
					default: // Domyślna (właściwa wysokość)
						odcinek = odcinek + String.valueOf(count) + " odcinek ("+String.valueOf(slopecursor.getDouble(1))+" km) o nachyleniu " + String.valueOf(nachylenie)+" % ("+String.valueOf(round(elev1,roundtype))+"m)\n";
				}
			}
			// szablon nachylenia
			count += 1;
		} while(slopecursor.moveToNext());
		odcinek = odcinek + "Meta podjazdu ("+round(elev1,roundtype)+"m)\n";
		return odcinek;
	}
	
	// Zaokrąglenie liczby
	/*
	public double round(double d, int n){
		java.text.NumberFormat nf = java.text.NumberFormat.getInstance();
		nf.setMaximumFractionDigits(n);
		nf.setMinimumFractionDigits(n);
		return Double.parseDouble((nf.format(d)).replaceAll(",", ".").replaceAll(" ",""));
	}
	*/
	public double round(double value, int places){
		if(places<0){
			throw new IllegalArgumentException();
		}
		long factor = (long)Math.pow(10,places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double)tmp/factor;
	}
	
	public String get_category(double points){
		if(points>=700){
			return "HC";
		} else if(points>=400){
			return "1+";
		} else if(points>=260){
			return "1";
		} else if(points>=160){
			return "2";
		} else if(points>=100){
			return "3";
		} else if(points>=60){
			return "4";
		} else if(points>=30){
			return "5";
		} else if(points>=15){
			return "6";
		} else {
			return "7";
		}
	}
	
	public Integer get_int_category(String category){
		switch(category){
			case "HC":
				return 1;
			case "1+":
				return 2;
			case "1":
				return 3;
			case "2":
				return 4;
			case "3":
				return 5;
			case "4":
				return 6;
			case "5":
				return 7;
			case "6":
				return 8;
			case "7":
				return 9;
		}
		return 0;
	}
	
	public int get_category_resid(String category){
		switch(category){
			case "HC":
				return R.drawable.cathc;
			case "1+":
				return R.drawable.cat1p;
			case "1":
				return R.drawable.cat1;
			case "2":
				return R.drawable.cat2;
			case "3":
				return R.drawable.cat3;
			case "4":
				return R.drawable.cat4;
			case "5":
				return R.drawable.cat5;
			case "6":
				return R.drawable.cat6;
			case "7":
				return R.drawable.cat7;
			default:
				// Anti Error (Remove in release mode)
				return R.drawable.cat7;
		}
	}
	
	public int getColorBySlope(int slope){
		Log.d("AltimetrDB", "Parsing slope: "+String.valueOf(slope));
		if(slope<0){
			return context.getResources().getColor(R.color.slope_downhill);
		} else {
			switch(slope){
				case 0:
					return context.getResources().getColor(R.color.slope_flat);
				case 1:
					return context.getResources().getColor(R.color.slope_1);
				case 2:
					return context.getResources().getColor(R.color.slope_2);
				case 3:
					return context.getResources().getColor(R.color.slope_3);
				case 4:
					return context.getResources().getColor(R.color.slope_4);
				case 5:
					return context.getResources().getColor(R.color.slope_5);
				case 6:
					return context.getResources().getColor(R.color.slope_6);
				case 7:
					return context.getResources().getColor(R.color.slope_7);
				case 8:
					return context.getResources().getColor(R.color.slope_8);
				case 9:
					return context.getResources().getColor(R.color.slope_9);
				case 10:
					return context.getResources().getColor(R.color.slope_10);
				case 11:
					return context.getResources().getColor(R.color.slope_11);
				case 12:
					return context.getResources().getColor(R.color.slope_12);
				case 13:
					return context.getResources().getColor(R.color.slope_13);
				case 14:
					return context.getResources().getColor(R.color.slope_14);
				case 15:
					return context.getResources().getColor(R.color.slope_15);
				case 16:
					return context.getResources().getColor(R.color.slope_16);
				case 17:
					return context.getResources().getColor(R.color.slope_17);
				case 18:
					return context.getResources().getColor(R.color.slope_18);
				case 19:
					return context.getResources().getColor(R.color.slope_19);
				case 20:
					return context.getResources().getColor(R.color.slope_20);
				default:
					// Very steep fragments
					return context.getResources().getColor(R.color.slope_steep);
			}
		}
	}
	
}
