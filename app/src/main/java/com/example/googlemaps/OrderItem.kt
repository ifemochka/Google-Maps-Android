package com.example.googlemaps

data class OrderItem(
    val type: String,
    val number: Int,
    val address: String,
    val name: String,
    val orderTime: String,
    val volume: Double,
    val weight: Double,
    val price: Double
)

object Data {
    var orders : List<OrderItem>? = emptyList()
}
