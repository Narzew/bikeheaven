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
     * Get climb data (with points and elevations)
     *
     * @param id Climb ID
     * @return Climb information without points and slopes) in 3 parts of JSON format, separated by ^climb_split^.
     */

    public String get_climb_points(Integer id) {

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
                    HttpPost httppost = new HttpPost(SERVER_PATH + "/climbs/get_climb_points.php");
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

}
