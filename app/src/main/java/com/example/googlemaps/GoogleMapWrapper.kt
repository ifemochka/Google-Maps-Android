package com.example.googlemaps

import android.graphics.Color
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PatternItem
import com.google.android.gms.maps.model.PolylineOptions

class GoogleMapWrapper(private val googleMap: GoogleMap) : IMap {

    private val markers: MutableMap<Pair<String, Int>, Marker> = mutableMapOf()

    override fun addMarker(position: LatLng, title: String, snippet: String, type: String, id: Int): IMapMarker {
        val marker = googleMap.addMarker(
            MarkerOptions()
                .position(position)
                .title(title)
                .snippet(snippet)
                .icon(BitmapDescriptorFactory.defaultMarker(
                    when (type) {
                        "s" -> BitmapDescriptorFactory.HUE_YELLOW
                        "r" -> BitmapDescriptorFactory.HUE_GREEN
                        else -> BitmapDescriptorFactory.HUE_BLUE
                    }
                ))
        )!!
        marker.tag = "${type}_$id"
        markers[type to id] = marker
        return GoogleMapMarkerWrapper(marker)
    }

    override fun clear() {
        googleMap.clear()
    }


    override fun moveCamera(position: LatLng, zoom: Float) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, zoom))
    }

    override fun setOnMarkerClickListener(listener: (type: String, id: Int, marker: IMapMarker) -> Unit) {
        googleMap.setOnMarkerClickListener { marker ->
            val tag = marker.tag as? String
            val parts = tag?.split("_")
            val type = parts?.getOrNull(0)
            val id = parts?.getOrNull(1)?.toIntOrNull()
            if (type != null && id != null) {
                listener(type, id, GoogleMapMarkerWrapper(marker))
            }
            true
        }
    }


    override fun showCurrentLocationMarker(position: LatLng) {
        val grayMarkerIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)

        googleMap.addMarker(
            MarkerOptions()
                .position(position)
                .title("Вы здесь")
                .icon(grayMarkerIcon)
        )

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15f))
    }


    override fun setOnInfoWindowClickListener(listener: (IMapMarker) -> Unit) {
        googleMap.setOnInfoWindowClickListener { marker ->
            listener(GoogleMapMarkerWrapper(marker))
        }
    }

    override fun addPolyline(points: List<LatLng>): IMapPolyline {
        val pattern: List<PatternItem> = listOf(Dash(30f), Gap(20f))

        val polyline = googleMap.addPolyline(
            PolylineOptions()
                .addAll(points)
                .color(Color.BLUE)
                .width(8f)
                .pattern(pattern)
        )
        return GoogleMapPolyline(polyline)
    }
}
