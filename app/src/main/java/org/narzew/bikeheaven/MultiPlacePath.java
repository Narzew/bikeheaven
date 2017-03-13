package org.narzew.bikeheaven;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class MultiPlacePath {

    Integer id;
    String name;
    String description;
    String category;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public ArrayList<LatLng> getCoords() {
        return coords;
    }

    public void setCoords(ArrayList<LatLng> coords) {
        this.coords = coords;
    }

    ArrayList<LatLng> coords;

    public MultiPlacePath(Integer id, String name, String description, String category, ArrayList<LatLng> coords){
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.coords = coords;
    }

}
