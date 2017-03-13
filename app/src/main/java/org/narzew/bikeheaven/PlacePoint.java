package org.narzew.bikeheaven;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Nikodem on 10.04.2016.
 */
public class PlacePoint {

    Integer placeId;

    public Integer getPlaceId() {
        return placeId;
    }

    public void setPlaceId(Integer placeId) {
        this.placeId = placeId;
    }

    public Integer getPointId() {
        return pointId;
    }

    public void setPointId(Integer pointId) {
        this.pointId = pointId;
    }

    public Integer getSeq() {
        return seq;
    }

    public void setSeq(Integer seq) {
        this.seq = seq;
    }

    public LatLng getCoords() {
        return coords;
    }

    public void setCoords(LatLng coords) {
        this.coords = coords;
    }

    Integer pointId;
    Integer seq;
    LatLng coords;

    public PlacePoint(Integer placeId, Integer pointId, Integer seq, LatLng coords){
        this.placeId = placeId;
        this.pointId = pointId;
        this.seq = seq;
        this.coords = coords;
    }

}