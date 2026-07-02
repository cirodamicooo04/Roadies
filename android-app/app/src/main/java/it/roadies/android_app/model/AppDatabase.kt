package it.roadies.android_app.model

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import it.roadies.android_app.model.dao.BookingDao
import it.roadies.android_app.model.dao.UserDao

@Database(entities = [User::class, Booking::class, FavouriteList::class], version = 1, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun bookingDao(): BookingDao
    abstract fun userDao(): UserDao
}
