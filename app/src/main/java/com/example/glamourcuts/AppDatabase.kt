package com.example.glamourcuts

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Booking::class,
        User::class,
        Service::class,
        UserFeedback::class,
        InventoryItem::class,
        Message::class,
        AppSetting::class,
    ],
    version = 16,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookingDao(): BookingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "glamour_cuts_database",
                )
                .fallbackToDestructiveMigration(true)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        Log.d("AppDatabase", "Database created")
                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.let { database ->
                                populateDatabase(database.bookingDao())
                            }
                        }
                    }
                    
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        Log.d("AppDatabase", "Database opened")
                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.let { database ->
                                val dao = database.bookingDao()
                                if (dao.getUserCount() == 0 || dao.getServiceCount() == 0) {
                                    populateDatabase(dao)
                                }
                            }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateDatabase(dao: BookingDao) {
            // Only insert users if table is empty
            if (dao.getUserCount() == 0) {
                // Default Admin
                dao.insertUser(User(username = "Admin", password = SecurityUtils.hashPassword("admin123"), role = "Admin", phone = "0123456789"))
                // Default Owner
                dao.insertUser(User(username = "Owner", password = SecurityUtils.hashPassword("owner123"), role = "Owner", phone = "0987654321"))
                // Default Stylist
                dao.insertUser(User(username = "stylist", password = SecurityUtils.hashPassword("123"), role = "Stylist", phone = "0555555555"))
                // Default Client
                dao.insertUser(User(username = "client", password = SecurityUtils.hashPassword("123"), role = "Client", phone = "0777777777"))
            }

            // Only insert services if table is empty
            if (dao.getServiceCount() == 0) {
                // Services from Image
                dao.insertService(Service(name = "Precision Cut", description = "✂", price = "R350", duration = "45 min"))
                dao.insertService(Service(name = "Colour & Highlights", description = "🎨", price = "R850", duration = "2.5 hrs"))
                dao.insertService(Service(name = "Blowout & Style", description = "✨", price = "R280", duration = "60 min"))
                dao.insertService(Service(name = "Keratin Treatment", description = "🌟", price = "R1,200", duration = "3 hrs"))
                dao.insertService(Service(name = "Scalp Treatment", description = "🌿", price = "R200", duration = "30 min"))
                dao.insertService(Service(name = "Bridal Package", description = "👑", price = "R2,500", duration = "4 hrs"))
            }

            // Only insert inventory if table is empty
            if (dao.getAllInventory().isEmpty()) {
                // Seed Inventory
                dao.insertInventoryItem(InventoryItem(name = "Shampoo", quantity = 10, lowStockThreshold = 3))
                dao.insertInventoryItem(InventoryItem(name = "Conditioner", quantity = 2, lowStockThreshold = 5))
                dao.insertInventoryItem(InventoryItem(name = "Hair Dye (Black)", quantity = 15, lowStockThreshold = 5))
            }

            // Seed default settings if empty
            if (dao.getAllSettings().isEmpty()) {
                dao.saveSetting(AppSetting(key = "APP_LANGUAGE", value = "en"))
                dao.saveSetting(AppSetting(key = "NOTIFICATIONS_ENABLED", value = "true"))
                dao.saveSetting(AppSetting(key = "THEME_MODE", value = "light"))
                dao.saveSetting(AppSetting(key = "SALON_NAME", value = "Glamour Cuts"))
            }

            Log.d("AppDatabase", "Database populated")
        }
    }
}
