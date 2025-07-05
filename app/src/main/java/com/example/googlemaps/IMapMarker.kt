package com.example.googlemaps

interface IMapMarker {
    fun setIcon(colorHue: Float)
    fun showInfoWindow()
    fun getTitle(): String?
    fun getSnippet(): String?
    fun getTag(): Any?
}
