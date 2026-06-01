package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val brand: String,
    val name: String,
    val category: String,
    val price: Double,
    val imageUrl: String,
    val description: String,
    val isExclusive: Boolean,
    val isEarlyAccess: Boolean,
    val floor: Int,
    val section: String,
    val stockOnline: Int,
    val stockInStore: Int,
    val sizeOptions: String // Comma separated, e.g. "US 7,US 8,US 9" or "S,M,L"
)

@Entity(tableName = "reservations")
data class ReservationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: String,
    val productName: String,
    val brand: String,
    val size: String,
    val quantity: Int,
    val storeName: String,
    val pickupTime: String,
    val status: String, // "Pending", "Transferred", "PickedUp"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "vip_profile")
data class VipProfileEntity(
    @PrimaryKey val id: String = "vip_current_user",
    val name: String,
    val email: String,
    val membershipTier: String, // "Standard", "Premium", "VIP", "Private Client"
    val points: Int,
    val personalShopperName: String,
    val stylePreferences: String,
    val brandAffinity: String,
    val visitHistory: String // Comma separated months/dates
)

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val time: String,
    val shopperName: String,
    val categoryPreference: String, // e.g. "Haute Couture", "Fine Jewellery"
    val notes: String,
    val status: String // "Confirmed", "Completed"
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val role: String, // "user", "model"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
