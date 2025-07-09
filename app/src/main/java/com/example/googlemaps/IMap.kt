package com.example.googlemaps

import com.google.android.gms.maps.model.LatLng

interface IMap {
    fun clear()
    fun addMarker(position: LatLng, title: String, snippet: String, type: String, id: Int): IMapMarker
    fun moveCamera(position: LatLng, zoom: Float)
    fun setOnMarkerClickListener(listener: (type: String, id: Int, marker: IMapMarker) -> Unit)
    fun showCurrentLocationMarker(position: LatLng)
    fun setOnInfoWindowClickListener(listener: (IMapMarker) -> Unit)
    fun addPolyline(points: List<LatLng>) : IMapPolyline
}
