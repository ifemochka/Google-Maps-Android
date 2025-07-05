package com.example.googlemaps

import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker

class GoogleMapMarkerWrapper(private val marker: Marker) : IMapMarker {
    override fun setIcon(colorHue: Float) {
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(colorHue))
    }

    override fun showInfoWindow() {
        marker.showInfoWindow()
    }

    override fun getTitle(): String? {
        return marker.title
    }

    override fun getSnippet(): String? {
        return marker.snippet
    }

    override fun getTag(): Any? {
        return marker.tag
    }

}
