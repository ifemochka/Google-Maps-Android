package com.example.googlemaps

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class MainActivity : AppCompatActivity() {

    private val pickFileRequestCode = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView: RecyclerView = findViewById(R.id.orderListRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)


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
            val adapter = moshi.adapter<MutableList<OrderItem>>(type)


            Data.orders = adapter.fromJson(fileContent)


            Data.orders?.let { list ->
                val adapter = OrderAdapter(list)
                findViewById<RecyclerView>(R.id.orderListRecyclerView).adapter = adapter
            }


            for(i in 0 until Data.orders!!.size){
                Data.completedOrders.add(CompletedOrderItem(Data.orders!![i]))
            }

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


