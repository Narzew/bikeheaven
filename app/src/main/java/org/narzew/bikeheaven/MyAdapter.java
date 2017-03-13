package org.narzew.bikeheaven;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.narzew.bikeheaven.R;

import java.util.List;

class MyAdapter extends ArrayAdapter<String> {
    Context c;
    List<String> lista;
    LayoutInflater inflater;
    ImageView imgView;

    public MyAdapter (Context c,List<String> list){
        super(c, R.layout.row, list);
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