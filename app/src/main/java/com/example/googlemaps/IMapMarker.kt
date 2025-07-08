package com.example.googlemaps

import com.google.android.gms.maps.model.LatLng

interface IMapMarker {
    fun setIcon(colorHue: Float)
    fun showInfoWindow()
    fun getTitle(): String?
    fun getSnippet(): String?
    fun getTag(): Any?
    fun getPosition(): LatLng
}
