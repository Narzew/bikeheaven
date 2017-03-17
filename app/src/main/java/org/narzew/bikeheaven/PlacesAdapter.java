package org.narzew.bikeheaven;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import java.util.List;

class PlacesAdapter extends BaseAdapter {
    Context c;
    List<Place> lista;
    LayoutInflater inflater;
    ImageView imgView;
    View row;
    SharedPreferences sp;
    TextView place_name;
    TextView place_description;
    ImageView place_category;
    LatLng coords;
    Place place;
    Integer category_id;

    public PlacesAdapter (Context c,List<Place> list){
        this.lista = list;
        this.c = c;
        inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        sp = c.getSharedPreferences(Config.PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public int getCount() {
        return lista.size();
    }

    @Override
    public Place getItem(int position) {
        return lista.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(sp.getInt("id",0)>0){
            // User is logged in
            row = inflater.inflate(R.layout.place_list_item,null);
        } else {
            // Unlogged layout
            row = inflater.inflate(R.layout.place_list_item,null);
        }
        place = (Place)lista.get(position);
        // Przypisanie elementów do layoutu
        place_name = (TextView)row.findViewById(R.id.place_name);
        place_description = (TextView)row.findViewById(R.id.place_description);
        place_category = (ImageView)row.findViewById(R.id.place_icon);
        place_name.setTextColor(Color.BLACK);
        place_description.setTextColor(Color.BLACK);
        //place_category = (TextView)row.findViewById(R.id.place_category);
        // Wpisanie odpowiednich wartości do elementów

        place_name.setText(place.getName()+"\n");
        place_description.setText(place.getDescription()+"\n");
        category_id = place.getCategory();
        //place_category = place.getCategory();
        // Set background
        row.setBackgroundColor(0xDCDCDC);
        switch (category_id){
            case 0:
                place_category.setImageDrawable(c.getResources().getDrawable(R.drawable.place_cat0));
                break;
            case 1:
                place_category.setImageDrawable(c.getResources().getDrawable(R.drawable.place_cat1));
                break;
            case 2:
                place_category.setImageDrawable(c.getResources().getDrawable(R.drawable.place_cat2));
                break;
            case 3:
                place_category.setImageDrawable(c.getResources().getDrawable(R.drawable.place_cat3));
                break;
            case 4:
                place_category.setImageDrawable(c.getResources().getDrawable(R.drawable.place_cat4));
                break;
            case 5:
                place_category.setImageDrawable(c.getResources().getDrawable(R.drawable.place_cat5));
                break;
            case 6:
                place_category.setImageDrawable(c.getResources().getDrawable(R.drawable.place_cat6));
                break;
            case 7:
                place_category.setImageDrawable(c.getResources().getDrawable(R.drawable.place_cat7));
                break;
            case 8:
                place_category.setImageDrawable(c.getResources().getDrawable(R.drawable.place_cat8));
                break;
            case 9:
                place_category.setImageDrawable(c.getResources().getDrawable(R.drawable.place_cat9));
                break;
            case 10:
                place_category.setImageDrawable(c.getResources().getDrawable(R.drawable.place_cat10));
                break;
            case 11:
                place_category.setImageDrawable(c.getResources().getDrawable(R.drawable.place_cat11));
                break;
            case 12:
                place_category.setImageDrawable(c.getResources().getDrawable(R.drawable.place_cat12));
                break;
            case 13:
                place_category.setImageDrawable(c.getResources().getDrawable(R.drawable.place_cat13));
                break;
            case 14:
                place_category.setImageDrawable(c.getResources().getDrawable(R.drawable.place_cat14));
                break;
            case 15:
                place_category.setImageDrawable(c.getResources().getDrawable(R.drawable.place_cat15));
                break;
            default:
                place_category.setImageDrawable(c.getResources().getDrawable(R.drawable.place_cat0));
        }

        return row;
    }
}