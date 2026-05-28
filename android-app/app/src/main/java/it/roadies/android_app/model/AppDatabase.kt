package it.roadies.android_app.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [User::class, Booking::class, FavouriteList::class], version = 1, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() { // We have to extend RoomDatabase

    companion object {
        // A volatile variable is never cached, and it is modified/read from the main memory.
        // Any change made by one thread is visible to all other threads.
        @Volatile
        private var instance: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {
            // We guarantee that the database is initialized once
            synchronized(this) {
                var localInstance = instance // Needed for smart cast
                if (localInstance == null) {
                    localInstance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "roadies-database" // The name of the database
                    ).build()
                    instance = localInstance
                }
                return localInstance
            }
        }
    }
}

