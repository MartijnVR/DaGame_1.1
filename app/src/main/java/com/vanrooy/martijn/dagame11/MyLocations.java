package com.vanrooy.martijn.dagame11;

import android.util.Log;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

class MyLocations {

    private final List<LatLng> mylocations = new ArrayList<>();
    private final List<LatLng> targetlocations = new ArrayList<>();

    public void addMyLocation(LatLng latLng){
        mylocations.add(latLng);
    }

    public void addTargetLocation(LatLng latLng){
        targetlocations.add(latLng);
    }

    public LatLng getMyLocation(int place) {
        return mylocations.get(place);
    }

    public int getMySize(){
        return mylocations.size();
    }

    public LatLng getTargetLocation(int place) {
        return targetlocations.get(place);
    }

    public int getTargetSize(){
        return targetlocations.size();
    }

}
