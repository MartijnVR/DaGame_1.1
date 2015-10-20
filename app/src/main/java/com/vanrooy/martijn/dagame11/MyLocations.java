package com.vanrooy.martijn.dagame11;

import android.util.Log;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

class MyLocations {

    private final List<LatLng> locations = new ArrayList<LatLng>();

    public void addLocation(LatLng latLng){
        locations.add(latLng);
    }

    public LatLng getLocation(int place) {
        return locations.get(place);
    }

    public int getSize(){
        return locations.size();
    }
}
