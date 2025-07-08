package com.example.googlemaps

import com.google.android.gms.maps.model.Polyline

class GoogleMapPolyline(private val polyline: Polyline) : IMapPolyline {
    override fun remove() {
        polyline.remove()
    }
}
