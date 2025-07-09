package com.example.googlemaps

import android.net.Uri
import java.io.Serializable

data class OrderItem(
    val number: Int,
    val senderAddress: String,
    val recipientAddress: String,
    val naming: String,
    val orderTime: String,
    val volume: Double,
    val weight: Double,
    val price: Double
): Serializable

data class CompletedOrderItem(
    val baseOrder: OrderItem,
    var pickupDate: String? = null,
    var deliveryDate: String? = null,
    var productPhotoUri: Uri? = null
) : Serializable


object Data {
    var orders : MutableList<OrderItem>? = mutableListOf()
    var completedOrders : MutableList<CompletedOrderItem> = mutableListOf()
}
