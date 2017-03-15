package org.narzew.bikeheaven;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v7.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;

public class Alarm extends BroadcastReceiver  {
    Context context;
    SharedPreferences sp;
    SharedPreferences.Editor edit;
    Integer id;
    String authkey;
    @Override
    public void onReceive(Context context, Intent intent){
        this.context=context;
        sp=context.getSharedPreferences(Config.PREFS_NAME, Context.MODE_PRIVATE);
        edit=sp.edit();
        try {
            GPSTracker gpStracker = new GPSTracker(context);
            Location location = gpStracker.getLocation();
            new CheckingAlarms().execute(location.getLatitude() + "", location.getLongitude() + "");
        }catch(Exception e ){
            Log.d("Error:",e+"");
        }

    }

    public void notification(Context context){
        long [] a=new long[]{500,500,500,500,500};
        NotificationCompat.Builder builder=new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.common_signin_btn_icon_dark);
        builder.setContentTitle("Zwiedziłeś nowe miejsce!");
        builder.setAutoCancel(true);
        builder.setVibrate(a);
        Intent e=new Intent(context,PlaceMapActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(PlaceMapActivity.class);
        stackBuilder.addNextIntent(e);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        builder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, builder.build());
    }


    public class CheckingAlarms extends AsyncTask <String, Integer, Integer>{
        Boolean alarms=false;
        @Override
        protected Integer doInBackground(String... strings) {
            APIHelper apihelper=new APIHelper(context);
            id = sp.getInt("id", 0);
            authkey = sp.getString("authkey", authkey);
            String result_id=apihelper.visit_near_places(id, authkey, new LatLng(Double.valueOf(strings[0]),Double.valueOf(strings[1])));
            if(result_id=="PLACE_VISITED"){
                // Show alarm
                alarms = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer i2) {
            if(alarms){
                notification(context);
            }
        }
    }
}
