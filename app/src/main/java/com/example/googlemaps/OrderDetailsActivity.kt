package com.example.googlemaps

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class OrderDetailsActivity : AppCompatActivity() {

    private var selectedImageUri: Uri? = null
    private lateinit var completedOrder: CompletedOrderItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_details)

        val order = intent.getSerializableExtra("order") as? OrderItem ?: return
        completedOrder = CompletedOrderItem(order)

        val senderText = findViewById<TextView>(R.id.senderAddressText)
        val recipientText = findViewById<TextView>(R.id.recipientAddressText)
        val imageView = findViewById<ImageView>(R.id.productImageView)
        val pickupButton = findViewById<Button>(R.id.pickupDateButton)
        val deliveryButton = findViewById<Button>(R.id.deliveryDateButton)
        val photoButton = findViewById<Button>(R.id.addPhotoButton)

        senderText.text = "Отправитель: ${order.senderAddress}"
        recipientText.text = "Получатель: ${order.recipientAddress}"

        pickupButton.setOnClickListener {
            showDatePicker { date -> completedOrder.pickupDate = date }
        }

        deliveryButton.setOnClickListener {
            showDatePicker { date -> completedOrder.deliveryDate = date }
        }

        photoButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }
    }

    private fun showDatePicker(callback: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val date = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                    .format(Calendar.getInstance().apply {
                        set(year, month, dayOfMonth)
                    }.time)
                callback(date)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            selectedImageUri = data?.data
            completedOrder.productPhotoUri = selectedImageUri
            findViewById<ImageView>(R.id.productImageView).setImageURI(selectedImageUri)
        }
    }
}
