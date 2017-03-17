package org.narzew.bikeheaven;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.Arrays;

public class UserMenuActivity extends AppCompatActivity  {

    Context context = this;
    DrawerLayout drawerLayout;
    ListView listView;
    String[] menuitems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_menu);


        drawerLayout=(DrawerLayout) findViewById(R.id.drawerLayout);
        menuitems=getResources().getStringArray(R.array.menu2);
        listView=(ListView) findViewById(R.id.drawerList);
        MyAdapter myadapter = new MyAdapter(this, Arrays.asList(menuitems));
        listView.setAdapter(myadapter);
        //listView.setOnItemClickListener(this);
    }

    /**
     Open list containing all places
     filter_mode 0 => All places (All)
     filter_mode 1 => Visited places (All)
     filter_mode 2 => Unvisited places (All)
     filter_mode 3 => All places (Near 50 km)
     filter_mode = 4 => Visited places (Near 50 km)
     filter_mode = 5 => Unvisited places (Near 50 km)
     */

    public void openAllPlacesList(View v){
        Intent intent = new Intent(context, PlacesList.class);
        intent.putExtra("filter_mode", 0);
        startActivity(intent);
    }

    /**
     Open list containing visited places
     */

    public void openVisitedPlacesList(View v){
        Intent intent = new Intent(context, PlacesList.class);
        intent.putExtra("filter_mode", 1);
        startActivity(intent);
    }

    /**
     Open list containing visited places
     */

    public void openUnvisitedPlacesList(View v){
        Intent intent = new Intent(context, PlacesList.class);
        intent.putExtra("filter_mode", 2);
        startActivity(intent);
    }

    /**
     Open list containing all places near you
     */

    public void openNearPlacesList(View v){
        Intent intent = new Intent(context, PlacesList.class);
        intent.putExtra("filter_mode", 3);
        startActivity(intent);
    }

    /**
     Open list containing visited places near you
     */

    public void openNearVisitedPlacesList(View v){
        Intent intent = new Intent(context, PlacesList.class);
        intent.putExtra("filter_mode", 4);
        startActivity(intent);
    }

    /**
     Open list containing visited places near you
     */

    public void openNearUnvisitedPlacesList(View v){
        Intent intent = new Intent(context, PlacesList.class);
        intent.putExtra("filter_mode", 5);
        startActivity(intent);
    }


    /**
     Open map with all places
     */

    public void openAllPlacesMap(View v){
        Intent intent = new Intent(context, PlaceMapActivity.class);
        intent.putExtra("filter_mode", 0);
        startActivity(intent);
    }

    /**
     Open map with visited places
     */

    public void openVisitedPlacesMap(View v){
        Intent intent = new Intent(context, PlaceMapActivity.class);
        intent.putExtra("filter_mode", 1);
        startActivity(intent);
    }

    /**
     Open map with unvisited places
     */

    public void openUnvisitedPlacesMap(View v){
        Intent intent = new Intent(context, PlaceMapActivity.class);
        intent.putExtra("filter_mode", 2);
        startActivity(intent);
    }

    /**
     Open map with all places near you
     */

    public void openNearAllPlacesMap(View v){
        Intent intent = new Intent(context, PlaceMapActivity.class);
        intent.putExtra("filter_mode", 3);
        startActivity(intent);
    }

    /**
     Open map with visited places near you
     */

    public void openNearVisitedPlacesMap(View v){
        Intent intent = new Intent(context, PlaceMapActivity.class);
        intent.putExtra("filter_mode", 4);
        startActivity(intent);
    }

    /**
     Open map with unvisited places near you
     */

    public void openNearUnvisitedPlacesMap(View v){
        Intent intent = new Intent(context, PlaceMapActivity.class);
        intent.putExtra("filter_mode", 5);
        startActivity(intent);
    }


    /**
     Open friend search
     */

    public void openFriendSearch(View v){
        /*
        Intent intent = new Intent(context, AddFriends.class);
        startActivity(intent);
        */
    }

    /**
     Open friend list
     */

    public void openFriendList(View v){
        /*
        Intent intent = new Intent(context, FriendsList.class);
        startActivity(intent);
        */
    }

    /**
     Open friend invitations
     */

    public void openFriendInvitations(View v){
        /*
        Intent intent = new Intent(context, Invitations.class);
        startActivity(intent);
        */
    }

    /**
     Open settings
     */

    public void openSettings(View v){
        Intent intent = new Intent(context, SettingsActivity.class);
        startActivity(intent);
    }

    /**
     Open app info
     */

    public void openAppInfoActivity(View v){
        Intent intent = new Intent(context, AppInfoActivity.class);
        startActivity(intent);
    }

    /**
     Logout user
     */

    public void openLogOutActivity(View v){
        Intent intent = new Intent(context, LogOutActivity.class);
        startActivity(intent);
    }

}
