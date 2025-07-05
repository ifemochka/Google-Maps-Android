package com.example.googlemaps

import android.app.Activity
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
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
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class MainActivity : AppCompatActivity() {

    private val pickFileRequestCode = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pickFileButton: Button = findViewById(R.id.button_pick_file)
        pickFileButton.setOnClickListener {
            openFilePicker()
        }
        val goToMapButton: Button = findViewById(R.id.button_go_to_map)
        goToMapButton.setOnClickListener{
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        startActivityForResult(intent, pickFileRequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == pickFileRequestCode && resultCode == Activity.RESULT_OK) {
            val uri: Uri? = data?.data
            if (uri != null) {
                Log.d("FilePicker", "Выбран файл: $uri")
                processSelectedFile(uri)
            }
        }
    }

    private fun processSelectedFile(uri: Uri) {
        try {
            val fileContent = contentResolver.openInputStream(uri)?.bufferedReader().use { reader ->
                reader?.readText()
            }

            if (fileContent.isNullOrEmpty()) {
                Toast.makeText(this, "Файл пуст", Toast.LENGTH_SHORT).show()
                return
            }

            Log.d("FilePicker", "Содержимое файла:\n$fileContent")

            val moshi = Moshi.Builder()
                .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                .build()
            val type = Types.newParameterizedType(List::class.java, OrderItem::class.java)
            val adapter = moshi.adapter<List<OrderItem>>(type)


            Data.orders = adapter.fromJson(fileContent)

            if (Data.orders != null) {
                Log.d("FilePicker", "Прочитано ${Data.orders!!.size} заказов")
                Data.orders!!.forEach { order ->
                    Log.d("FilePicker", "Заказ: $order")
                }
                Toast.makeText(this, "Прочитано ${Data.orders!!.size} заказов", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Не удалось распарсить JSON", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Ошибка при обработке файла", Toast.LENGTH_SHORT).show()
        }
    }

}