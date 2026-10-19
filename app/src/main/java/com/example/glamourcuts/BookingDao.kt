package com.example.glamourcuts

import androidx.room.*

@Dao
interface BookingDao {
    // Bookings
    @Insert
    suspend fun insert(booking: Booking)

    @Query("SELECT * FROM bookings ORDER BY id DESC")
    suspend fun getAllBookings(): List<Booking>

    @Query("SELECT * FROM bookings WHERE userId = :userId ORDER BY id DESC")
    suspend fun getBookingsForUser(userId: Int): List<Booking>

    @Query("SELECT * FROM bookings WHERE date = :date AND time = :time AND status != 'Cancelled' LIMIT 1")
    suspend fun getBookingAtTime(date: String, time: String): Booking?

    @Query("UPDATE bookings SET status = :status WHERE id = :id")
    suspend fun updateBookingStatus(id: Int, status: String)

    @Query("UPDATE bookings SET pricePaid = :price WHERE id = :id")
    suspend fun updatePricePaid(id: Int, price: String)

    // Users
    @Insert
    suspend fun insertUser(user: User)

    // Use COLLATE NOCASE for case-insensitive username matching
    @Query("SELECT * FROM users WHERE username = :username COLLATE NOCASE AND password = :password LIMIT 1")
    suspend fun login(username: String, password: String): User?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Query("SELECT * FROM users WHERE username = :username COLLATE NOCASE LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Int): User?

    @Query("SELECT * FROM users WHERE isConfirmed = 0")
    suspend fun getPendingUsers(): List<User>

    @Query("UPDATE users SET isConfirmed = 1 WHERE id = :userId")
    suspend fun confirmUser(userId: Int)

    @Update
    suspend fun updateUser(user: User)

    // Services
    @Insert
    suspend fun insertService(service: Service)

    @Query("SELECT * FROM services")
    suspend fun getAllServices(): List<Service>

    @Query("SELECT COUNT(*) FROM services")
    suspend fun getServiceCount(): Int

    // Feedback
    @Insert
    suspend fun insertFeedback(feedback: UserFeedback)

    @Query("SELECT * FROM feedback ORDER BY timestamp DESC")
    suspend fun getAllFeedback(): List<UserFeedback>

    // Inventory
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItem(item: InventoryItem)

    @Query("SELECT * FROM inventory")
    suspend fun getAllInventory(): List<InventoryItem>

    @Update
    suspend fun updateInventoryItem(item: InventoryItem)

    @Delete
    suspend fun deleteInventoryItem(item: InventoryItem)

    @Query("SELECT * FROM inventory WHERE quantity <= lowStockThreshold")
    suspend fun getLowStockItems(): List<InventoryItem>

    // Communication
    @Insert
    suspend fun insertMessage(message: Message)

    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    suspend fun getAllMessages(): List<Message>

    @Query("SELECT * FROM messages ORDER BY timestamp DESC LIMIT 1")
    fun getLatestMessage(): kotlinx.coroutines.flow.Flow<Message?>

    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    fun getAllMessagesFlow(): kotlinx.coroutines.flow.Flow<List<Message>>

    // Settings
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSetting(setting: AppSetting)

    @Query("SELECT * FROM settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): AppSetting?

    @Query("SELECT value FROM settings WHERE `key` = :key LIMIT 1")
    suspend fun getSettingValue(key: String): String?

    @Query("SELECT * FROM settings")
    suspend fun getAllSettings(): List<AppSetting>

    @Query("DELETE FROM settings WHERE `key` = :key")
    suspend fun deleteSetting(key: String)
}
