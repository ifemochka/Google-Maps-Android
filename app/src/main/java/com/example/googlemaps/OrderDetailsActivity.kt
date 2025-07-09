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
        val id  = intent.getIntExtra("id", 0)
        completedOrder = Data.completedOrders[id]

        val senderText = findViewById<TextView>(R.id.senderAddressText)
        val recipientText = findViewById<TextView>(R.id.recipientAddressText)
        val imageView = findViewById<ImageView>(R.id.productImageView)
        val pickupButton = findViewById<Button>(R.id.pickupDateButton)
        val deliveryButton = findViewById<Button>(R.id.deliveryDateButton)
        val photoButton = findViewById<Button>(R.id.addPhotoButton)
        val saveButton = findViewById<Button>(R.id.saveButton)
        val orderNumberText = findViewById<TextView>(R.id.orderNumberText)
        val orderNamingText = findViewById<TextView>(R.id.orderNamingText)
        val orderTimeText = findViewById<TextView>(R.id.orderTimeText)
        val orderVolumeText = findViewById<TextView>(R.id.orderVolumeText)
        val orderWeightText = findViewById<TextView>(R.id.orderWeightText)
        val orderPriceText = findViewById<TextView>(R.id.orderPriceText)

        orderNumberText.text = "Номер заказа: ${order.number}"
        orderNamingText.text = "Название груза: ${order.naming}"
        orderTimeText.text = "Время заказа: ${order.orderTime}"
        orderVolumeText.text = "Объём: ${order.volume} м³"
        orderWeightText.text = "Вес: ${order.weight} кг"
        orderPriceText.text = "Стоимость: ${order.price} ₽"


        senderText.text = "Отправитель: ${order.senderAddress}"
        recipientText.text = "Получатель: ${order.recipientAddress}"

        // ✅ Показать сохранённые даты
        completedOrder.pickupDate?.let {
            pickupButton.text = "Дата забора: $it"
        }
        completedOrder.deliveryDate?.let {
            deliveryButton.text = "Дата доставки: $it"
        }

        // ✅ Показать сохранённое фото
        completedOrder.productPhotoUri?.let {
            imageView.setImageURI(it)
        }

        pickupButton.setOnClickListener {
            showDatePicker { date ->
                completedOrder.pickupDate = date
                pickupButton.text = "Дата забора: $date"
            }
        }

        deliveryButton.setOnClickListener {
            showDatePicker { date ->
                completedOrder.deliveryDate = date
                deliveryButton.text = "Дата доставки: $date"
            }
        }

        photoButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
            }
            startActivityForResult(intent, 100)

        }

        // ✅ Кнопка сохранения
        saveButton.setOnClickListener {
            Data.completedOrders[id] = completedOrder // Обновим список
            Toast.makeText(this, "Сохранено", Toast.LENGTH_SHORT).show()
            finish() // ⬅️ Закрыть текущую активность и вернуться на карту
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

            // ✅ Запрашиваем долгосрочные права на URI
            selectedImageUri?.let { uri ->
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                completedOrder.productPhotoUri = uri
                findViewById<ImageView>(R.id.productImageView).setImageURI(uri)
            }
        }
    }

}
