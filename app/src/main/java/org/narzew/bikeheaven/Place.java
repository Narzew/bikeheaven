package org.narzew.bikeheaven;

import com.google.android.gms.maps.model.LatLng;

public class Place {

    Integer id;
    String name;
    String description;
    Integer category;
    LatLng coords;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    public LatLng getCoords() {
        return coords;
    }

    public void setCoords(LatLng coords) {
        this.coords = coords;
    }

    public int getCategoryResId(){
        switch(this.category){
            case 1:
                return R.drawable.cat1;
            case 2:
                return R.drawable.cat2;
            case 3:
                return R.drawable.cat3;
            case 4:
                return R.drawable.cat4;
            case 5:
                return R.drawable.cat5;
            case 6:
                return R.drawable.cat6;
            case 7:
                return R.drawable.cat7;
            case 8:
                return R.drawable.cat8;
            case 9:
                return R.drawable.cat9;
            case 10:
                return R.drawable.cat10;
            case 11:
                return R.drawable.cat11;
            case 12:
                return R.drawable.cat12;
            case 13:
                return R.drawable.cat13;
            case 14:
                return R.drawable.cat14;
            case 15:
                return R.drawable.cat15;
            default:
                // Anti Error (Remove in release mode)
                return R.drawable.cat0;
        }
    }

    public Place(Integer id, String name, String description, Integer category, LatLng coords){
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.coords = coords;
    }

}
