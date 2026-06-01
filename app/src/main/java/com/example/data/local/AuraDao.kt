package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AuraDao {
    // Products
    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: String): ProductEntity?

    @Query("SELECT * FROM products WHERE brand LIKE :query OR name LIKE :query OR category LIKE :query")
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    // Click & Collect Reservations
    @Query("SELECT * FROM reservations ORDER BY timestamp DESC")
    fun getAllReservations(): Flow<List<ReservationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReservation(reservation: ReservationEntity)

    @Query("DELETE FROM reservations WHERE id = :id")
    suspend fun deleteReservationById(id: Int)

    // VIP Profile
    @Query("SELECT * FROM vip_profile WHERE id = 'vip_current_user' LIMIT 1")
    fun getVipProfile(): Flow<VipProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVipProfile(profile: VipProfileEntity)

    // Appointments
    @Query("SELECT * FROM appointments ORDER BY id DESC")
    fun getAllAppointments(): Flow<List<AppointmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: AppointmentEntity)

    // Concierge Chat Memory
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllChatMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages")
    suspend fun clearChatMessages()
}
