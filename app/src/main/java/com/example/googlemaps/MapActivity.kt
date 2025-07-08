package com.example.googlemaps

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject

import java.io.IOException

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var map: IMap
    private var currentPolyline: IMapPolyline? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_map)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = GoogleMapWrapper(googleMap)
        showCurrentLocation()
        plotOrders()
    }

    private fun plotOrders() {
        val sendersLatLng = mutableListOf<LatLng>()
        val recipientsLatLng = mutableListOf<LatLng>()
        val sendersMarkers = mutableListOf<IMapMarker>()
        val recipientsMarkers = mutableListOf<IMapMarker>()


        val size = Data.orders?.size ?: 0
        Log.d("size", "$size")

        for (i in 0 until size) {
            val order = Data.orders!![i]
            val geocoder = Geocoder(this)
            val senderAddress = geocoder.getFromLocationName(order.senderAddress, 1)?.getOrNull(0)
            val recipientAddress = geocoder.getFromLocationName(order.recipientAddress, 1)?.getOrNull(0)
            if (senderAddress != null && recipientAddress != null) {
                val senderLatLng = LatLng(senderAddress.latitude, senderAddress.longitude)
                val recipientLatLng = LatLng(recipientAddress.latitude, recipientAddress.longitude)
                sendersLatLng.add(senderLatLng)
                recipientsLatLng.add(recipientLatLng)
            }
        }

        for (i in sendersLatLng.indices) {
            sendersMarkers.add(
                map.addMarker(
                    position = sendersLatLng[i],
                    title = "Sender ${i + 1}",
                    snippet = "Отправитель: ${Data.orders!!.get(i).senderAddress}",
                    type = "s",
                    id = i
                )
            )

            recipientsMarkers.add(
                map.addMarker(
                    position = recipientsLatLng[i],
                    title = "Recipient ${i + 1}",
                    snippet = "Получатель: ${Data.orders!!.get(i).recipientAddress}",
                    type = "r",
                    id = i
                )
            )

        }

        if (sendersLatLng.isNotEmpty()) {
            map.moveCamera(sendersLatLng[0], 14f)
        }

        var selectedSender: IMapMarker? = null
        var selectedRecipient: IMapMarker? = null

        map.setOnMarkerClickListener { type, id, marker ->
            Log.d("MarkerClicker", "clicked")
            selectedSender?.setIcon(BitmapDescriptorFactory.HUE_YELLOW)
            selectedRecipient?.setIcon(BitmapDescriptorFactory.HUE_GREEN)
            

            if (type == "s") {
                marker.setIcon(BitmapDescriptorFactory.HUE_RED)
                selectedSender = marker
                val recipient = recipientsMarkers[id]
                recipient?.setIcon(BitmapDescriptorFactory.HUE_RED)
                selectedRecipient = recipient
            } else {
                marker.setIcon(BitmapDescriptorFactory.HUE_RED)
                selectedRecipient = marker
                val sender = sendersMarkers[id]
                sender?.setIcon(BitmapDescriptorFactory.HUE_RED)
                selectedSender = sender
            }

            marker.showInfoWindow()

            if (selectedSender != null && selectedRecipient != null) {
                getDirections(selectedSender!!.getPosition(), selectedRecipient!!.getPosition())
            }
        }

        map.setOnInfoWindowClickListener { marker ->
            val tag = marker.getTag() as? String
            val parts = tag?.split("_")
            val type = parts?.getOrNull(0)
            val id = parts?.getOrNull(1)?.toIntOrNull()
            Log.d("Info", "Заказ: $tag")

            var senderAddress : String
            var reecipientAddress : String

            val order = Data.orders?.get(id!! )
            senderAddress = order!!.senderAddress
            reecipientAddress = order!!.recipientAddress

            if (order != null) {
                val message = """
           
            Номер: ${order.number}
            Адрес отправителя: ${senderAddress}
            Адрес получателя: ${reecipientAddress}
            Имя: ${order.naming}
            Время заказа: ${order.orderTime}
            Объём: ${order.volume}
            Вес: ${order.weight}
            Цена: ${order.price}
        """.trimIndent()

                AlertDialog.Builder(this)
                    .setTitle("Детали заказа")
                    .setMessage(message)
                    .setPositiveButton("OK", null)
                    .show()
            }

        }


    }

    private fun showCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val currentLatLng = LatLng(it.latitude, it.longitude)
                map.showCurrentLocationMarker(currentLatLng)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("Permission", "Permission granted")
            showCurrentLocation()
        }
    }

    private fun getDirections(origin: LatLng, destination: LatLng) {
        val url = "https://router.project-osrm.org/route/v1/driving/" +
                "${origin.longitude},${origin.latitude};" +
                "${destination.longitude},${destination.latitude}" +
                "?overview=full&geometries=polyline"

        val request = Request.Builder()
            .url(url)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { responseData ->
                    val json = JSONObject(responseData)
                    val routes = json.getJSONArray("routes")
                    if (routes.length() > 0) {
                        val overviewPolyline = routes.getJSONObject(0)
                            .getString("geometry") // в OSRM поле называется geometry

                        val points = decodePolyline(overviewPolyline)

                        runOnUiThread {
                            currentPolyline?.remove()
                            currentPolyline = map.addPolyline(points)
                        }
                    }
                }
            }
        })
    }

    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if ((result and 1) != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if ((result and 1) != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            poly.add(LatLng(lat / 1E5, lng / 1E5))
        }
        return poly
    }
}
