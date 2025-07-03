package com.example.googlemaps

import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
    lateinit var mMap: GoogleMap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_map)
        var mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val sendersOrders: MutableList<OrderItem> = mutableListOf()
        val recipientsOrders: MutableList<OrderItem> = mutableListOf()
        val sendersLatLng: MutableList<LatLng> = mutableListOf()
        val recipientsLatLng: MutableList<LatLng> = mutableListOf()


        val size = Data.orders?.size
        Log.d("size", "$size")

        for (i in 0 until size!!){
            if(Data.orders?.get(i)?.type == "s"){
                sendersOrders.add(Data.orders!![i])
                val geocoder = Geocoder(this)
                val addresses = geocoder.getFromLocationName(Data.orders!![i].address, 1)
                val location = addresses?.get(0)
                if (location != null) {
                    sendersLatLng.add(LatLng(location.latitude, location.longitude))
                }

            }
            else{
                recipientsOrders.add(Data.orders!![i])
                val geocoder = Geocoder(this)
                val addresses = geocoder.getFromLocationName(Data.orders!![i].address, 1)
                val location = addresses?.get(0)
                if (location != null) {
                    recipientsLatLng.add(LatLng(location.latitude, location.longitude))
                }

            }
        }


        val senders: MutableList<Marker?> = mutableListOf()
        val recipients: MutableList<Marker?> = mutableListOf()


        for (i in 0 until sendersLatLng.size) {
            var n = i + 1

            val senderAddressLine = sendersOrders[i].address

            val recipientAddressLine = recipientsOrders[i].address


            var marker = mMap.addMarker(
                MarkerOptions()
                    .position(sendersLatLng[i])
                    .title("Sender $n")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                    .snippet("Отправитель: $senderAddressLine")
            )
            marker?.tag = "s_$i"
            senders.add(marker)

            marker = mMap.addMarker(
                MarkerOptions()
                    .position(recipientsLatLng[i])
                    .title("Recipient $n")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    .snippet("Получатель: $recipientAddressLine")
            )
            marker?.tag = "r_$i"
            recipients.add(marker)
        }

        mMap.isBuildingsEnabled = true
        mMap.isIndoorEnabled = true

        if(size != 0){
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sendersLatLng[0], 14f))
        }

        var selectedSender: Marker? = null
        var selectedRecipient: Marker? = null

        mMap.setOnMarkerClickListener { marker ->
            selectedSender?.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
            selectedRecipient?.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))

            val tag = marker.tag as? String
            val parts = tag?.split("_")
            val type = parts?.get(0)
            val id = parts?.get(1)?.toIntOrNull()

            if (type == "s") {
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                selectedSender = marker
                recipients[id!!]?.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                selectedRecipient = recipients[id]
            }
            else{
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                selectedRecipient = marker
                senders[id!!]?.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                selectedSender = senders[id]
            }

            marker.showInfoWindow()

            true
        }
    }


}