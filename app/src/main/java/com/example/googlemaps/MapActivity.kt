package com.example.googlemaps

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
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

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var map: IMap

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
        val sendersOrders = mutableListOf<OrderItem>()
        val recipientsOrders = mutableListOf<OrderItem>()
        val sendersLatLng = mutableListOf<LatLng>()
        val recipientsLatLng = mutableListOf<LatLng>()
        val sendersMarkers = mutableListOf<IMapMarker>()
        val recipientsMarkers = mutableListOf<IMapMarker>()


        val size = Data.orders?.size ?: 0
        Log.d("size", "$size")

        for (i in 0 until size) {
            val order = Data.orders!![i]
            val geocoder = Geocoder(this)
            val addresses = geocoder.getFromLocationName(order.address, 1)
            val location = addresses?.getOrNull(0)
            if (location != null) {
                val latLng = LatLng(location.latitude, location.longitude)
                if (order.type == "s") {
                    sendersOrders.add(order)
                    sendersLatLng.add(latLng)
                } else {
                    recipientsOrders.add(order)
                    recipientsLatLng.add(latLng)
                }
            }
        }

        for (i in sendersLatLng.indices) {
            sendersMarkers.add(
                map.addMarker(
                    position = sendersLatLng[i],
                    title = "Sender ${i + 1}",
                    snippet = "Отправитель: ${sendersOrders[i].address}",
                    type = "s",
                    id = i
                )
            )

            recipientsMarkers.add(
                map.addMarker(
                    position = recipientsLatLng[i],
                    title = "Recipient ${i + 1}",
                    snippet = "Получатель: ${recipientsOrders[i].address}",
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
        }

        map.setOnInfoWindowClickListener { marker ->
            val tag = marker.getTag() as? String
            val parts = tag?.split("_")
            val type = parts?.getOrNull(0)
            val id = parts?.getOrNull(1)?.toIntOrNull()
            Log.d("Info", "Заказ: $tag")

            var senderAdress : String
            var reecipientAdress : String

            val order : OrderItem?
            if (type == "s") {
                order = Data.orders?.get(id!! * 2)
                senderAdress = order!!.address
                reecipientAdress = Data.orders?.get(id!! * 2 + 1)!!.address
            }
            else{
                order = Data.orders?.get(id!! * 2 + 1)
                senderAdress = Data.orders?.get(id!! * 2)!!.address
                reecipientAdress = order!!.address
            }

            if (order != null) {
                val message = """
           
            Номер: ${order.number}
            Адрес отправителя: ${senderAdress}
            Адрес получателя: ${reecipientAdress}
            Имя: ${order.name}
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

}
