package org.narzew.bikeheaven;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.Toast;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import com.google.android.gms.maps.model.LatLng;


public class APIHelper {

    protected Context context;
    protected String SERVER_PATH = Config.SERVER_PATH;
    static Boolean httpConnect = true;
    private String LOG_KEY = Config.LOG_KEY;
    private String PREFS_NAME = Config.PREFS_NAME;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;


    public APIHelper(Context context) {
        this.context = context;
        SharedPreferencesDeclaracion();
    }

    private void SharedPreferencesDeclaracion(){
        sharedPreferences=context.getSharedPreferences(Config.PREFS_NAME,Context.MODE_PRIVATE);
        editor=sharedPreferences.edit();
    }

    public String split_result(String result){
        try {
            String resultargs[] = result.split("\\^api\\^");
            result = resultargs[1];
            resultargs= result.split("\\^api\\^");
            result = resultargs[0];
        } catch (ArrayIndexOutOfBoundsException e){
            Log.d(LOG_KEY, "Split result = " + result);
            return result;
        }
        Log.d(LOG_KEY, "Split result = "+result);
        return result;

    }

    /**
     * Check that user has active internet connection
     * Sprawdź czy użytkownik ma połączenie internetowe
     *
     * @return	True if user have active internet connection
     * False if user doesn't have active internet connection
     * Prawda jeśli użytkownik ma połączenie sieciowe
     * Fałsz jeśli użytkownik nie ma połączenia sieciowego
     */

    public  boolean hasActiveInternetConnection() {
        try {
            HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.pl").openConnection());
            urlc.setRequestProperty("User-Agent", "Test");
            urlc.setRequestProperty("Connection", "close");
            urlc.setConnectTimeout(1500);
            urlc.connect();
            //httpConnect=(urlc.getResponseCode() == 200);
            httpConnect = true;

            return true;
        } catch (IOException e) {
            httpConnect=false;
            return false;
        }

    }

    /**
     * Check that value has specified length between min and max parameters
     * Sprawdź czy wartość mieści się w określonej długości
     *
     * @param value	Value to be checked
     *              Wartość do sprawdzenia
     * @param min	Minimal length
     *              Minimalna długość
     * @param max	Maximal length
     *              Maksymalna długość
     * @return True or False
     * Prawda lub fałsz
     */

    public Boolean checkLength(String value, int min, int max){
        if(value.length()>max||value.length()<min){
            return false;
        } else {
            return true;
        }
    }

    /**
     * Check that value has invalid length with min and max parameters
     *
     * Sprawdź czy podana wartość nie mieści się w określonej długości
     * Przeciwieństwo do checkLength()
     *
     * @param value	Value to be checked
     * @param min	Minimal length
     * @param max	Maximal length
     * @return True or False
     */

    public Boolean invalidLength(String value, int min, int max){
        if(value.length()>max||value.length()<min){
            return true;
        } else {
            return false;
        }
    }

    /**
     * Remove polish chars
     * @param str to remove polish chars
     * @return Text with removed polish chars
     */

    public String remove_polish_chars(String str){
        str = str.replace("ą","a");
        str = str.replace("ć","c");
        str = str.replace("ę","e");
        str = str.replace("ł‚","l");
        str = str.replace("ń","n");
        str = str.replace("ó","o");
        str = str.replace("ś","s");
        str = str.replace("ź","z");
        str = str.replace("ż","z");
        return str;
    }

    /**
     * Download custom photo
     * @param id Photo ID

     */

    public Drawable download_photo(Integer id){
        String url="http://narzew.org/bikeheaven/photos/"+id+".png";
        try {
            InputStream is = (InputStream) new URL(url).getContent();
            Drawable d = Drawable.createFromStream(is,"zdj"+id+".png");
            return d;
        } catch (Exception e) {
            return null;
        }

    }

    /**
     * Get local database SQL
     * This method contain SQL that destroys current local categories table
     * and create new categories table from actual categories server data
     * @return SQL code of local database deletion and creation with categories
     */

    public String get_local_database_sql(){

        // Check internet connection
        hasActiveInternetConnection();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("HttpConnect", httpConnect);
        String result = "";
        if (httpConnect) {
            if (result == "") {
                InputStream is = null;
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(SERVER_PATH+"/database.sql");
                    // Tablica z wartościami dla POST'a
                    List<NameValuePair> params = new ArrayList<NameValuePair>(0);
                    httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                    // Odpowiedź serwera
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();
                    is = entity.getContent();
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error in http connection " + e.toString());
                }
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    result = sb.toString();
                    result = split_result(result);
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error converting result " + e.toString());
                }
                switch (result) {
                    case "NO_RESULTS":
                        editor.putInt("request_result", 1);
                        editor.apply();
                        return "";
                    default:
                        editor.putInt("request_result", 0);
                        editor.apply();
                        return result;
                }
            }
            return "";
        } else {
            editor.apply();
            return "";
        }
    }

    /**
     * Get local database version
     * This method return version of local database
     */

    public String get_local_database_version(){

        // Check internet connection
        hasActiveInternetConnection();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("HttpConnect", httpConnect);
        String result = "";
        if (httpConnect) {
            if (result == "") {
                InputStream is = null;
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(SERVER_PATH+"/version.php");
                    // Tablica z wartościami dla POST'a
                    List<NameValuePair> params = new ArrayList<NameValuePair>(0);
                    httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                    // Odpowiedź serwera
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();
                    is = entity.getContent();
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error in http connection " + e.toString());
                }
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    result = sb.toString();
                    result = split_result(result);
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error converting result " + e.toString());
                }
                switch (result) {
                    case "NO_RESULTS":
                        editor.putInt("request_result", 1);
                        editor.apply();
                        return "";
                    default:
                        editor.putInt("request_result", 0);
                        editor.apply();
                        return result;
                }
            }
            return "";
        } else {
            editor.apply();
            return "";
        }
    }

    /**
     * Get all climbs in JSON format
     *
     * @return All climbs in JSON format
     */

    public String get_all_climbs(){

        // Check internet connection
        hasActiveInternetConnection();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("HttpConnect", httpConnect);
        String result = "";
        if (httpConnect) {
            if (result == "") {
                InputStream is = null;
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(SERVER_PATH+"/climbs/get_all.php");
                    // Tablica z wartościami dla POST'a
                    List<NameValuePair> params = new ArrayList<NameValuePair>(0);
                    httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                    // Odpowiedź serwera
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();
                    is = entity.getContent();
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error in http connection " + e.toString());
                }
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    result = sb.toString();
                    result = split_result(result);
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error converting result " + e.toString());
                }
                switch (result) {
                    case "NO_RESULTS":
                        editor.putInt("request_result", 1);
                        editor.apply();
                        return "";
                    default:
                        editor.putInt("request_result", 0);
                        editor.apply();
                        return result;
                }
            }
            return "";
        } else {
            editor.apply();
            return "";
        }
    }

    /**
     * Get some test climbs
     * @param limit Maximum number of test climbs
     *
     * @return All climbs in JSON format
     */

    public String get_test_climbs(Integer limit){

        // Check internet connection
        hasActiveInternetConnection();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("HttpConnect", httpConnect);
        String result = "";
        if (httpConnect) {
            if (result == "") {
                InputStream is = null;
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(SERVER_PATH+"/climbs/get_all.php");
                    // Tablica z wartościami dla POST'a
                    List<NameValuePair> params = new ArrayList<NameValuePair>(1);
                    params.add(new BasicNameValuePair("number",limit+""));
                    httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                    // Odpowiedź serwera
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();
                    is = entity.getContent();
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error in http connection " + e.toString());
                }
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    result = sb.toString();
                    result = split_result(result);
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error converting result " + e.toString());
                }
                switch (result) {
                    case "NO_RESULTS":
                        editor.putInt("request_result", 1);
                        editor.apply();
                        return "";
                    default:
                        editor.putInt("request_result", 0);
                        editor.apply();
                        return result;
                }
            }
            return "";
        } else {
            editor.apply();
            return "";
        }
    }


    /**
     * Get all climbs that have specified difficulty
     *
     * @param mindifficulty Minimum difficulty (in points)
     * @param maxdifficulty Maximum difficulty (in points)
     * @return All climbs that have specified difficulty in JSON format
     */

    public String get_by_difficulty(Double mindifficulty, Double maxdifficulty) {

        // Check internet connection
        hasActiveInternetConnection();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("HttpConnect", httpConnect);
        String result = "";
        if (httpConnect) {
            if (result == "") {
                InputStream is = null;
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(SERVER_PATH + "/climbs/get_by_difficulty.php");
                    // Tablica z wartościami dla POST'a
                    List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                    params.add(new BasicNameValuePair("mindifficulty", mindifficulty + ""));
                    params.add(new BasicNameValuePair("maxdifficulty", maxdifficulty + ""));
                    httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                    // Odpowiedź serwera
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();
                    is = entity.getContent();
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error in http connection " + e.toString());
                }
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    result = sb.toString();
                    result = split_result(result);
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error converting result " + e.toString());
                }
                switch (result) {
                    case "NO_RESULTS":
                        editor.putInt("request_result", 1);
                        editor.apply();
                        return "";
                    default:
                        editor.putInt("request_result", 0);
                        editor.apply();
                        return result;
                }
            }
            return "";
        } else {
            editor.apply();
            return "";
        }
    }

    /**
     * Get all climbs that have specified difficulty and type
     *
     * @param mindifficulty Minimum difficulty (in points)
     * @param maxdifficulty Maximum difficulty (in points)
     * @param type Type
     * @return All climbs that have specified difficulty and type in JSON format
     */

    public String get_by_type(Double mindifficulty, Double maxdifficulty, Integer type) {

        // Check internet connection
        hasActiveInternetConnection();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("HttpConnect", httpConnect);
        String result = "";
        if (httpConnect) {
            if (result == "") {
                InputStream is = null;
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(SERVER_PATH + "/climbs/get_by_type.php");
                    // Tablica z wartościami dla POST'a
                    List<NameValuePair> params = new ArrayList<NameValuePair>(3);
                    params.add(new BasicNameValuePair("mindifficulty", mindifficulty + ""));
                    params.add(new BasicNameValuePair("maxdifficulty", maxdifficulty + ""));
                    params.add(new BasicNameValuePair("type", type+""));
                    httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                    // Odpowiedź serwera
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();
                    is = entity.getContent();
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error in http connection " + e.toString());
                }
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    result = sb.toString();
                    result = split_result(result);
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error converting result " + e.toString());
                }
                switch (result) {
                    case "NO_RESULTS":
                        editor.putInt("request_result", 1);
                        editor.apply();
                        return "";
                    default:
                        editor.putInt("request_result", 0);
                        editor.apply();
                        return result;
                }
            }
            return "";
        } else {
            editor.apply();
            return "";
        }
    }

    /**
     * Get all climbs that have specified type
     *
     * @param type Type
     * @return All climbs that have specified type in JSON format
     */

    public String get_by_difficulty_and_type(Integer type) {

        // Check internet connection
        hasActiveInternetConnection();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("HttpConnect", httpConnect);
        String result = "";
        if (httpConnect) {
            if (result == "") {
                InputStream is = null;
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(SERVER_PATH + "/climbs/get_by_difficulty_and_type.php");
                    // Tablica z wartościami dla POST'a
                    List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                    params.add(new BasicNameValuePair("type", type + ""));
                    httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                    // Odpowiedź serwera
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();
                    is = entity.getContent();
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error in http connection " + e.toString());
                }
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    result = sb.toString();
                    result = split_result(result);
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error converting result " + e.toString());
                }
                switch (result) {
                    case "NO_RESULTS":
                        editor.putInt("request_result", 1);
                        editor.apply();
                        return "";
                    default:
                        editor.putInt("request_result", 0);
                        editor.apply();
                        return result;
                }
            }
            return "";
        } else {
            editor.apply();
            return "";
        }
    }

    /**
     * Get climb data (only climb information, without points and elevations)
     *
     * @param id Climb ID
     * @return Climb information (without points and slopes) in JSON format
     */

    public String get_climb(Integer id) {

        // Check internet connection
        hasActiveInternetConnection();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("HttpConnect", httpConnect);
        String result = "";
        if (httpConnect) {
            if (result == "") {
                InputStream is = null;
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(SERVER_PATH + "/climbs/get_climb.php");
                    // Tablica z wartościami dla POST'a
                    List<NameValuePair> params = new ArrayList<NameValuePair>(1);
                    params.add(new BasicNameValuePair("id", id + ""));
                    httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                    // Odpowiedź serwera
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();
                    is = entity.getContent();
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error in http connection " + e.toString());
                }
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    result = sb.toString();
                    result = split_result(result);
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error converting result " + e.toString());
                }
                switch (result) {
                    case "NO_RESULTS":
                        editor.putInt("request_result", 1);
                        editor.apply();
                        return "";
                    default:
                        editor.putInt("request_result", 0);
                        editor.apply();
                        return result;
                }
            }
            return "";
        } else {
            editor.apply();
            return "";
        }
    }

    /**
     * Get climb data (with points, elevations and curves)
     *
     * @param id Climb ID
     * @return Climb information without points and slopes) in 3 parts of JSON format, separated by ^climb_split^.
     */

    public String get_climb_details(Integer id) {

        // Check internet connection
        hasActiveInternetConnection();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("HttpConnect", httpConnect);
        String result = "";
        if (httpConnect) {
            if (result == "") {
                InputStream is = null;
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(SERVER_PATH + "/climbs/get_climb_details.php");
                    // Tablica z wartościami dla POST'a
                    List<NameValuePair> params = new ArrayList<NameValuePair>(1);
                    params.add(new BasicNameValuePair("id", id + ""));
                    httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                    // Odpowiedź serwera
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();
                    is = entity.getContent();
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error in http connection " + e.toString());
                }
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    result = sb.toString();
                    result = split_result(result);
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error converting result " + e.toString());
                }
                switch (result) {
                    case "NO_RESULTS":
                        editor.putInt("request_result", 1);
                        editor.apply();
                        return "";
                    default:
                        editor.putInt("request_result", 0);
                        editor.apply();
                        return result;
                }
            }
            return "";
        } else {
            editor.apply();
            return "";
        }
    }

    /**
     * Search climb by name
     *
     * @param name Climb name
     * @return Matched climbs information without points and slopes) in 3 parts of JSON format, separated by ^climb_split^.
     */

    public String search_by_name(String name) {

        // Check internet connection
        hasActiveInternetConnection();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("HttpConnect", httpConnect);
        String result = "";
        if (httpConnect) {
            if (result == "") {
                InputStream is = null;
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(SERVER_PATH + "/climbs/search_by_name.php");
                    // Tablica z wartościami dla POST'a
                    List<NameValuePair> params = new ArrayList<NameValuePair>(1);
                    params.add(new BasicNameValuePair("name", name + ""));
                    httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                    // Odpowiedź serwera
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();
                    is = entity.getContent();
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error in http connection " + e.toString());
                }
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    result = sb.toString();
                    result = split_result(result);
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error converting result " + e.toString());
                }
                switch (result) {
                    case "NO_RESULTS":
                        editor.putInt("request_result", 1);
                        editor.apply();
                        return "";
                    default:
                        editor.putInt("request_result", 0);
                        editor.apply();
                        return result;
                }
            }
            return "";
        } else {
            editor.apply();
            return "";
        }
    }

    /**
     * Get climbs near you
     *
     * @param lat Your latitude
     * @param lng Your longitude
     * @parma distance Distance in km
     * @return Get climbs near you in JSON format
     */

    public String get_near_you(Double lat, Double lng, Double distance) {

        // Check internet connection
        hasActiveInternetConnection();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("HttpConnect", httpConnect);
        String result = "";
        if (httpConnect) {
            if (result == "") {
                InputStream is = null;
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(SERVER_PATH + "/climbs/get_near_you.php");
                    // Tablica z wartościami dla POST'a
                    List<NameValuePair> params = new ArrayList<NameValuePair>(3);
                    params.add(new BasicNameValuePair("lat", lat + ""));
                    params.add(new BasicNameValuePair("lng", lng+ ""));
                    params.add(new BasicNameValuePair("distance", distance + ""));
                    httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                    // Odpowiedź serwera
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();
                    is = entity.getContent();
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error in http connection " + e.toString());
                }
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    result = sb.toString();
                    result = split_result(result);
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error converting result " + e.toString());
                }
                switch (result) {
                    case "NO_RESULTS":
                        editor.putInt("request_result", 1);
                        editor.apply();
                        return "";
                    default:
                        editor.putInt("request_result", 0);
                        editor.apply();
                        return result;
                }
            }
            return "";
        } else {
            editor.apply();
            return "";
        }
    }

    /**
     * Get climbs near you with difficulty filter
     *
     * @param lat Your latitude
     * @param lng Your longitude
     * @parma distance Distance in km
     * @param mindifficulty Minimal difficulty (in points)
     * @param maxdifficulty Maximum difficulty (in points)
     * @return Get climbs near you in JSON format
     */

    public String get_near_you_filter_difficulty(Double lat, Double lng, Double distance, Double mindifficulty, Double maxdifficulty) {

        // Check internet connection
        hasActiveInternetConnection();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("HttpConnect", httpConnect);
        String result = "";
        if (httpConnect) {
            if (result == "") {
                InputStream is = null;
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(SERVER_PATH + "/climbs/get_near_you_filter_difficulty.php");
                    // Tablica z wartościami dla POST'a
                    List<NameValuePair> params = new ArrayList<NameValuePair>(5);
                    params.add(new BasicNameValuePair("lat", lat + ""));
                    params.add(new BasicNameValuePair("lng", lng+ ""));
                    params.add(new BasicNameValuePair("distance", distance + ""));
                    params.add(new BasicNameValuePair("mindifficulty", mindifficulty+ ""));
                    params.add(new BasicNameValuePair("maxdifficulty", maxdifficulty + ""));
                    httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                    // Odpowiedź serwera
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();
                    is = entity.getContent();
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error in http connection " + e.toString());
                }
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    result = sb.toString();
                    result = split_result(result);
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error converting result " + e.toString());
                }
                switch (result) {
                    case "NO_RESULTS":
                        editor.putInt("request_result", 1);
                        editor.apply();
                        return "";
                    default:
                        editor.putInt("request_result", 0);
                        editor.apply();
                        return result;
                }
            }
            return "";
        } else {
            editor.apply();
            return "";
        }
    }

    /**
     * Get total number of climbs
     *
     * @return Total number of climbs
     */

    public String get_total_climbs() {

        // Check internet connection
        hasActiveInternetConnection();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("HttpConnect", httpConnect);
        String result = "";
        if (httpConnect) {
            if (result == "") {
                InputStream is = null;
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(SERVER_PATH + "/climbs/get_total.php");
                    // Tablica z wartościami dla POST'a
                    List<NameValuePair> params = new ArrayList<NameValuePair>(0);
                    httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                    // Odpowiedź serwera
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();
                    is = entity.getContent();
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error in http connection " + e.toString());
                }
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    result = sb.toString();
                    result = split_result(result);
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error converting result " + e.toString());
                }
                switch (result) {
                    case "NO_RESULTS":
                        editor.putInt("request_result", 1);
                        editor.apply();
                        return "";
                    default:
                        editor.putInt("request_result", 0);
                        editor.apply();
                        return result;
                }
            }
            return "";
        } else {
            editor.apply();
            return "";
        }
    }

    /**
     * Get version of database structure
     *
     * @return Database structure version
     */

    public String get_db_scheme_version() {

        // Check internet connection
        hasActiveInternetConnection();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("HttpConnect", httpConnect);
        String result = "";
        if (httpConnect) {
            if (result == "") {
                InputStream is = null;
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(SERVER_PATH + "/database/get_scheme_version.php");
                    // Tablica z wartościami dla POST'a
                    List<NameValuePair> params = new ArrayList<NameValuePair>(0);
                    httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                    // Odpowiedź serwera
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();
                    is = entity.getContent();
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error in http connection " + e.toString());
                }
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    result = sb.toString();
                    result = split_result(result);
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error converting result " + e.toString());
                }
                switch (result) {
                    case "NO_RESULTS":
                        editor.putInt("request_result", 1);
                        editor.apply();
                        return "";
                    default:
                        editor.putInt("request_result", 0);
                        editor.apply();
                        return result;
                }
            }
            return "";
        } else {
            editor.apply();
            return "";
        }
    }

    /**
     * Get SQL of database creation
     *
     * @return Database creation sql
     */

    public String get_local_sql() {

        // Check internet connection
        hasActiveInternetConnection();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("HttpConnect", httpConnect);
        String result = "";
        if (httpConnect) {
            if (result == "") {
                InputStream is = null;
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(SERVER_PATH + "/database/get_local_sql.php");
                    // Tablica z wartościami dla POST'a
                    List<NameValuePair> params = new ArrayList<NameValuePair>(0);
                    httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                    // Odpowiedź serwera
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();
                    is = entity.getContent();
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error in http connection " + e.toString());
                }
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    result = sb.toString();
                    result = split_result(result);
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error converting result " + e.toString());
                }
                switch (result) {
                    case "NO_RESULTS":
                        editor.putInt("request_result", 1);
                        editor.apply();
                        return "";
                    default:
                        editor.putInt("request_result", 0);
                        editor.apply();
                        return result;
                }
            }
            return "";
        } else {
            editor.apply();
            return "";
        }
    }

    /** Old GeoMMO APIHelper methods */
    /**
     * Get all places in JSON format
     *
     * @return All places JSON format
     */

    public String get_all_places(){

        // Check internet connection
        hasActiveInternetConnection();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //editor.putBoolean("HttpConnect", httpConnect);
        editor.apply();
        String result = "";
        if (httpConnect) {
            if (result == "") {
                InputStream is = null;
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(SERVER_PATH+"/places/getall/");
                    // Tablica z wartościami dla POST'a
                    List<NameValuePair> params = new ArrayList<NameValuePair>(0);
                    httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                    // Odpowiedź serwera
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();
                    is = entity.getContent();
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error in http connection " + e.toString());
                }
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    result = sb.toString();
                    result = split_result(result);
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error converting result " + e.toString());
                }
                switch (result) {
                    case "NO_RESULTS":
                        editor.putInt("request_result", 1);
                        editor.apply();
                        return "";
                    default:
                        editor.putInt("request_result", 0);
                        editor.apply();
                        return result;
                }
            }
            return "";
        } else {
            editor.apply();
            return "";
        }
    }

    /**
     * Get all unvisited places in JSON format
     *
     * @param user_id User ID
     * @param authkey User authkey
     * @return All unvisited places in JSON format
     */

    public String get_unvisited_places(Integer user_id, String authkey){

        // Check internet connection
        hasActiveInternetConnection();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //editor.putBoolean("HttpConnect", httpConnect);
        editor.apply();
        String result = "";
        if (httpConnect) {
            if (result == "") {
                InputStream is = null;
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(SERVER_PATH+"/places/getunvisited/");
                    // Tablica z wartościami dla POST'a
                    List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                    params.add(new BasicNameValuePair("user_id",user_id+""));
                    params.add(new BasicNameValuePair("authkey",authkey));
                    httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                    // Odpowiedź serwera
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();
                    is = entity.getContent();
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error in http connection " + e.toString());
                }
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    result = sb.toString();
                    result = split_result(result);
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error converting result " + e.toString());
                }
                switch (result) {
                    case "NO_RESULTS":
                        editor.putInt("request_result", 1);
                        editor.apply();
                        return "";
                    default:
                        editor.putInt("request_result", 0);
                        editor.apply();
                        return result;
                }
            }
            return "";
        } else {
            editor.apply();
            return "";
        }
    }

    /**
     * Get all visited places in JSON format
     *
     * @param user_id User ID
     * @param authkey User authkey
     * @return All visited places in JSON format
     */

    public String get_visited_places(Integer user_id, String authkey){

        // Check internet connection
        hasActiveInternetConnection();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //editor.putBoolean("HttpConnect", httpConnect);
        editor.apply();
        String result = "";
        if (httpConnect) {
            if (result == "") {
                InputStream is = null;
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(SERVER_PATH+"/places/getvisited/");
                    // Tablica z wartościami dla POST'a
                    List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                    params.add(new BasicNameValuePair("user_id",user_id+""));
                    params.add(new BasicNameValuePair("authkey",authkey));
                    httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                    // Odpowiedź serwera
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();
                    is = entity.getContent();
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error in http connection " + e.toString());
                }
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    result = sb.toString();
                    result = split_result(result);
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error converting result " + e.toString());
                }
                switch (result) {
                    case "NO_RESULTS":
                        editor.putInt("request_result", 1);
                        editor.apply();
                        return "";
                    default:
                        editor.putInt("request_result", 0);
                        editor.apply();
                        return result;
                }
            }
            return "";
        } else {
            editor.apply();
            return "";
        }
    }

    /**
     * Get all places near you in JSON format
     *
     * @param location LatLng containg location
     * @return All places near youJSON format
     */

    public String get_all_places_near_you(LatLng location){

        // Check internet connection
        hasActiveInternetConnection();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //editor.putBoolean("HttpConnect", httpConnect);
        editor.apply();
        String result = "";
        if (httpConnect) {
            if (result == "") {
                InputStream is = null;
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(SERVER_PATH+"/places/getallnearyou/");
                    // Tablica z wartościami dla POST'a
                    List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                    params.add(new BasicNameValuePair("lat",String.valueOf(location.latitude)+""));
                    params.add(new BasicNameValuePair("lng",String.valueOf(location.longitude)+""));
                    httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                    // Odpowiedź serwera
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();
                    is = entity.getContent();
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error in http connection " + e.toString());
                }
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    result = sb.toString();
                    result = split_result(result);
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error converting result " + e.toString());
                }
                switch (result) {
                    case "NO_RESULTS":
                        editor.putInt("request_result", 1);
                        editor.apply();
                        return "";
                    default:
                        editor.putInt("request_result", 0);
                        editor.apply();
                        return result;
                }
            }
            return "";
        } else {
            editor.apply();
            return "";
        }
    }

    /**
     * Get all unvisited places near you in JSON format
     *
     * @param user_id User ID
     * @param authkey User authkey
     * @param location LatLng containing location
     * @return All unvisited places in JSON format
     */

    public String get_unvisited_places_near_you(Integer user_id, String authkey, LatLng location){

        // Check internet connection
        hasActiveInternetConnection();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //editor.putBoolean("HttpConnect", httpConnect);
        editor.apply();
        String result = "";
        if (httpConnect) {
            if (result == "") {
                InputStream is = null;
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(SERVER_PATH+"/places/getunvisitednearyou/");
                    // Tablica z wartościami dla POST'a
                    List<NameValuePair> params = new ArrayList<NameValuePair>(4);
                    params.add(new BasicNameValuePair("user_id",user_id+""));
                    params.add(new BasicNameValuePair("authkey",authkey));
                    params.add(new BasicNameValuePair("lat",String.valueOf(location.latitude)+""));
                    params.add(new BasicNameValuePair("lng",String.valueOf(location.longitude)+""));
                    httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                    // Odpowiedź serwera
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();
                    is = entity.getContent();
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error in http connection " + e.toString());
                }
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    result = sb.toString();
                    result = split_result(result);
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error converting result " + e.toString());
                }
                switch (result) {
                    case "NO_RESULTS":
                        editor.putInt("request_result", 1);
                        editor.apply();
                        return "";
                    default:
                        editor.putInt("request_result", 0);
                        editor.apply();
                        return result;
                }
            }
            return "";
        } else {
            editor.apply();
            return "";
        }
    }

    /**
     * Get all visited places near you in JSON format
     *
     * @param user_id User ID
     * @param authkey User authkey
     * @param location LatLng containing location
     * @return All visited places near you in JSON format
     */

    public String get_visited_places_near_you(Integer user_id, String authkey, LatLng location){

        // Check internet connection
        hasActiveInternetConnection();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //editor.putBoolean("HttpConnect", httpConnect);
        editor.apply();
        String result = "";
        if (httpConnect) {
            if (result == "") {
                InputStream is = null;
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(SERVER_PATH+"/places/getvisitednearyou/");
                    // Tablica z wartościami dla POST'a
                    List<NameValuePair> params = new ArrayList<NameValuePair>(4);
                    params.add(new BasicNameValuePair("user_id",user_id+""));
                    params.add(new BasicNameValuePair("authkey",authkey));
                    params.add(new BasicNameValuePair("lat",String.valueOf(location.latitude)+""));
                    params.add(new BasicNameValuePair("lng",String.valueOf(location.longitude)+""));
                    httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                    // Odpowiedź serwera
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();
                    is = entity.getContent();
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error in http connection " + e.toString());
                }
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    result = sb.toString();
                    result = split_result(result);
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error converting result " + e.toString());
                }
                switch (result) {
                    case "NO_RESULTS":
                        editor.putInt("request_result", 1);
                        editor.apply();
                        return "";
                    default:
                        editor.putInt("request_result", 0);
                        editor.apply();
                        return result;
                }
            }
            return "";
        } else {
            editor.apply();
            return "";
        }
    }

    /**
     * Visit all visited places near your location
     *
     * @param user_id User ID
     * @param authkey User authkey
     * @return All visited places in JSON format
     */

    public String visit_near_places(Integer user_id, String authkey, LatLng location){

        // Check internet connection
        hasActiveInternetConnection();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //editor.putBoolean("HttpConnect", httpConnect);
        editor.apply();
        String result = "";
        if (httpConnect) {
            if (result == "") {
                InputStream is = null;
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(SERVER_PATH+"/places/visitnear/");
                    // Tablica z wartościami dla POST'a
                    List<NameValuePair> params = new ArrayList<NameValuePair>(4);
                    params.add(new BasicNameValuePair("user_id",user_id+""));
                    params.add(new BasicNameValuePair("authkey",authkey));
                    params.add(new BasicNameValuePair("lat", String.valueOf(location.latitude)+""));
                    params.add(new BasicNameValuePair("lng", String.valueOf(location.longitude)+""));
                    httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                    // Odpowiedź serwera
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();
                    is = entity.getContent();
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error in http connection " + e.toString());
                }
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    result = sb.toString();
                    result = split_result(result);
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error converting result " + e.toString());
                }
                switch (result) {
                    case "NO_RESULTS":
                        editor.putInt("request_result", 1);
                        editor.apply();
                        return "";
                    case "NOTHING_CHANGED":
                        editor.putInt("request_result", 2);
                        editor.apply();
                        return "";
                    default:
                        editor.putInt("request_result", 0);
                        editor.apply();
                        return result;
                }
            }
            return "";
        } else {
            editor.apply();
            return "";
        }
    }

    /**
     * Visit places in quest near your location
     *
     * @param user_id User ID
     * @param authkey User authkey
     * @return All visited places in JSON format
     */

    public String visit_quest_places (Integer user_id, String authkey, LatLng location){

        // Check internet connection
        hasActiveInternetConnection();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //editor.putBoolean("HttpConnect", httpConnect);
        editor.apply();
        String result = "";
        if (httpConnect) {
            if (result == "") {
                InputStream is = null;
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(SERVER_PATH+"/places/visitnearquest/");
                    // Tablica z wartościami dla POST'a
                    List<NameValuePair> params = new ArrayList<NameValuePair>(4);
                    params.add(new BasicNameValuePair("user_id",user_id+""));
                    params.add(new BasicNameValuePair("authkey",authkey));
                    params.add(new BasicNameValuePair("lat", String.valueOf(location.latitude)+""));
                    params.add(new BasicNameValuePair("lng", String.valueOf(location.longitude)+""));
                    httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                    // Odpowiedź serwera
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();
                    is = entity.getContent();
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error in http connection " + e.toString());
                }
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    result = sb.toString();
                    result = split_result(result);
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error converting result " + e.toString());
                }
                switch (result) {
                    case "NO_RESULTS":
                        editor.putInt("request_result", 1);
                        editor.apply();
                        return "";
                    case "NOTHING_CHANGED":
                        editor.putInt("request_result", 2);
                        editor.apply();
                        return "";
                    default:
                        editor.putInt("request_result", 0);
                        editor.apply();
                        return result;
                }
            }
            return "";
        } else {
            editor.apply();
            return "";
        }
    }

    /**
     * Search user (by name)
     *
     * @param id User ID
     * @param authkey User authkey
     * @param pattern Search pattern
     * @return JSON containing search data (id+names pair) or NO_RESULTS
     */

    public String search_username(Integer id, String authkey, String pattern) {

        // Check internet connection
        hasActiveInternetConnection();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("HttpConnect", httpConnect);
        editor.apply();
        String result = "";
        if (httpConnect) {
            if (result == "") {
                InputStream is = null;
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(SERVER_PATH+"friends/search/");
                    // Tablica z wartościami dla POST'a
                    List<NameValuePair> params = new ArrayList<NameValuePair>(3);
                    params.add(new BasicNameValuePair("id", id + ""));
                    params.add(new BasicNameValuePair("authkey", authkey+""));
                    params.add(new BasicNameValuePair("name", remove_polish_chars(pattern)+""));
                    httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                    // Odpowiedź serwera
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();
                    is = entity.getContent();
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error in http connection " + e.toString());
                }
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    result = sb.toString();
                    result = split_result(result);
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error converting result " + e.toString());
                }
                switch (result) {
                    case "NO_RESULTS":
                        editor.putInt("request_result", 1);
                        editor.apply();
                        return "";
                    default:
                        editor.putInt("request_result", 0);
                        editor.apply();
                        return result;
                }
            }
            return "";
        } else {
            editor.apply();
            return "";
        }
    }

    /**
     * Get messages
     *
     * @param id User id
     * @param authkey User authkey
     * @param user User id you want message
     * @return All messages JSON (user_id, recipent_id, message, sent_date)
     */

    public String get_messages(Integer id, String authkey, Integer user){

        // Check internet connection
        hasActiveInternetConnection();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("HttpConnect", httpConnect);
        editor.apply();
        String result = "";
        if (httpConnect) {
            if (result == "") {
                InputStream is = null;
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(SERVER_PATH+"messages/get/");
                    // Tablica z wartościami dla POST'a
                    List<NameValuePair> params = new ArrayList<NameValuePair>(3);
                    params.add(new BasicNameValuePair("id", id+""));
                    params.add(new BasicNameValuePair("authkey", authkey+""));
                    params.add(new BasicNameValuePair("user", user+""));
                    httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                    // Odpowiedź serwera
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();
                    is = entity.getContent();
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error in http connection " + e.toString());
                }
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    result = sb.toString();
                    result = split_result(result);
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error converting result " + e.toString());
                }
                switch (result) {
                    case "iNVALID_USERNAME":
                        editor.putInt("request_result", 1);
                        editor.apply();
                        return "";
                    case "INVALID_USER":
                        editor.putInt("request_result", 2);
                        editor.apply();
                        return "";
                    case "INVALID_AUTHKEY":
                        editor.putInt("request_result", 3);
                        editor.apply();
                        return "";
                    default:
                        editor.putInt("request_result", 0);
                        editor.apply();
                        return result;
                }
            }
            return "";
        } else {
            editor.apply();
            return "";
        }
    }

    /**
     * Send message
     *
     * @param id User id
     * @param authkey User authkey
     * @param recipent_id Id of message recipent
     * @return Response code
     */

    public String send_message(Integer id, String authkey, Integer recipent_id, String message){

        // Check internet connection
        hasActiveInternetConnection();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("HttpConnect", httpConnect);
        editor.apply();
        String result = "";
        if (httpConnect) {
            if (result == "") {
                InputStream is = null;
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(SERVER_PATH+"messages/send/");
                    // Tablica z wartościami dla POST'a
                    List<NameValuePair> params = new ArrayList<NameValuePair>(4);
                    params.add(new BasicNameValuePair("id", id+""));
                    params.add(new BasicNameValuePair("authkey", authkey+""));
                    params.add(new BasicNameValuePair("recipent_id", recipent_id+""));
                    params.add(new BasicNameValuePair("message", message+""));
                    httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                    // Odpowiedź serwera
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();
                    is = entity.getContent();
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error in http connection " + e.toString());
                }
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    result = sb.toString();
                    result = split_result(result);
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error converting result " + e.toString());
                }
                switch (result) {
                    case "MESSAGE_SENT":
                        editor.putInt("request_result", 0);
                        editor.apply();
                        return "";
                    case "INAVLID_RECIPENT":
                        editor.putInt("request_result", 1);
                        editor.apply();
                        return "";
                    case "EMPTY_MESSAGE":
                        editor.putInt("request_result", 2);
                        editor.apply();
                        return "";
                    case "INVALID_AUTHKEY":
                        editor.putInt("request_result", 3);
                        editor.apply();
                        return "";
                    case "INVALID_USER":
                        editor.putInt("request_result", 3);
                        editor.apply();
                        return "";
                    default:
                        editor.putInt("request_result", -1);
                        editor.apply();
                        return result;
                }
            }
            return "";
        } else {
            editor.apply();
            return "";
        }
    }

    /**
     * Get all places in current quest in JSON format
     *
     * @param user_id User ID
     * @param authkey User authkey
     * @return All visited places in JSON format
     */

    public String get_quest_places(Integer user_id, String authkey){

        // Check internet connection
        hasActiveInternetConnection();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //editor.putBoolean("HttpConnect", httpConnect);
        editor.apply();
        String result = "";
        if (httpConnect) {
            if (result == "") {
                InputStream is = null;
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(SERVER_PATH+"/places/getvisited/");
                    // Tablica z wartościami dla POST'a
                    List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                    params.add(new BasicNameValuePair("user_id",user_id+""));
                    params.add(new BasicNameValuePair("authkey",authkey));
                    httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                    // Odpowiedź serwera
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();
                    is = entity.getContent();
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error in http connection " + e.toString());
                }
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    result = sb.toString();
                    result = split_result(result);
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error converting result " + e.toString());
                }
                switch (result) {
                    case "NO_RESULTS":
                        editor.putInt("request_result", 1);
                        editor.apply();
                        return "";
                    default:
                        editor.putInt("request_result", 0);
                        editor.apply();
                        return result;
                }
            }
            return "";
        } else {
            editor.apply();
            return "";
        }
    }

    /**
     * Register an user
     * @param login Login
     * @param email E-mail
     * @param password Password (SHA1)
     * @param fname First name
     * @param sname Surname
     * @param gender Gender. 0 - male; 1 - female int
     * @param city City
     * @param phone Phone number
     * @param description User description
     * @param pos Position: 0 - uczeń; 1 - korepetytor int
     *
     * TODO: Add input verification
     */

    public String register(String login, String email, String password, String fname, String sname, Integer gender, String city,
                           String phone, String description, Integer pos){
        hasActiveInternetConnection();
        String result = "";
        if(httpConnect) {
            // Check parameters length
            InputStream is = null;
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(SERVER_PATH + "/users/register.php");
                // Tablica z wartościami dla POST'a
                List<NameValuePair> params = new ArrayList<NameValuePair>(3);
                params.add(new BasicNameValuePair("login", login + ""));
                params.add(new BasicNameValuePair("email", email + ""));
                params.add(new BasicNameValuePair("password", password + ""));
                httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                // Odpowiedź serwera
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                is = entity.getContent();
            } catch (Exception e) {
                Log.e(LOG_KEY, "Error in http connection " + e.toString());
            }
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                is.close();
                result = sb.toString();
                result = split_result(result);
            } catch (Exception e) {
                Log.e(LOG_KEY, "Error converting result " + e.toString());
            }
        }
        return result;

    }


    /**
     * Login an user
     *
     * Notice: You can use email or login (caseignore) in the email field
     * @param email	User email or login
     * @param password	User password (SHA1)
     */

    public String login(String email, String password) {

        // Check internet connection
        hasActiveInternetConnection();
        String result = "";
        if (httpConnect) {
            InputStream is = null;
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(SERVER_PATH+"/users/login.php");
                // Tablica z wartościami dla POST'a
                List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                params.add(new BasicNameValuePair("email", email));
                params.add(new BasicNameValuePair("password", password));
                httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                // Odpowiedź serwera
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                is = entity.getContent();
            } catch (Exception e) {
                Log.e(LOG_KEY, "Error in http connection " + e.toString());
            }
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                is.close();
                result = sb.toString();
                result = split_result(result);
                int id=get_user_id(email);
                editor.putInt("id", id);
                editor.apply();
            } catch (Exception e) {
                Log.e(LOG_KEY, "Error converting result " + e.toString());
            }
        }
        return result;


    }

    /**
     * Get user id
     * Get user id when you know email and authkey
     *
     * @param email User email
     * @return User id
     */

    public Integer get_user_id(String email) {

        // Check internet connection
        hasActiveInternetConnection();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("HttpConnect", httpConnect);
        String result = "";
        if (httpConnect) {
            if (result == "") {
                InputStream is = null;
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(SERVER_PATH+"/users/getbyemail.php");
                    // Tablica z wartościami dla POST'a
                    List<NameValuePair> params = new ArrayList<NameValuePair>(1);
                    params.add(new BasicNameValuePair("email", email));
                    httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                    // Odpowiedź serwera
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();
                    is = entity.getContent();
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error in http connection " + e.toString());
                }
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    result = sb.toString();
                    result = split_result(result);
                } catch (Exception e) {
                    Log.e(LOG_KEY, "Error converting result " + e.toString());
                }
                switch (result) {
                    case "INVALID_USER":
                        editor.putInt("request_result", 1);
                        editor.apply();
                        return 0;
                    default:
                        editor.putInt("request_result", 0);
                        editor.apply();
                        return Integer.parseInt(result);
                }
            }
            return 0;
        } else {
            editor.apply();
            return 0;
        }
    }

    /**request
     * Send new password change request
     *
     * @param email User email
     * @param password New user password (SHA1)
     */


    public String send_password_request(String email, String password){

        // Check internet connection
        hasActiveInternetConnection();

        String result = "";
        if (httpConnect) {

            InputStream is = null;
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(SERVER_PATH+"/users/sendpassword.php");
                // Tablica z wartościami dla POST'a
                List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                params.add(new BasicNameValuePair("email", email +""));
                params.add(new BasicNameValuePair("password", password+""));
                httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                // Odpowiedź serwera
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                is = entity.getContent();
            } catch (Exception e) {
                Log.e(LOG_KEY, "Error in http connection " + e.toString());
            }
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                is.close();
                result = sb.toString();
                result = split_result(result);
            } catch (Exception e) {
                Log.e(LOG_KEY, "Error converting result " + e.toString());
            }
        }
        return result;
    }

}
