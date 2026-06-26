package it.roadies.android_app.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import it.roadies.android_app.model.Booking
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.util.UUID

@Dao
interface BookingDao {
    @Query("SELECT * FROM booking WHERE end_date > :now ORDER BY start_date DESC")
    fun getActiveBookingsFlow(now: LocalDateTime): Flow<List<Booking>>

    @Query("SELECT * FROM booking WHERE end_date <= :now ORDER BY start_date DESC")
    fun getPastBookingsFlow(now: LocalDateTime): Flow<List<Booking>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: Booking)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBookings(bookings: List<Booking>)

    @Query("DELETE FROM booking WHERE id = :id")
    suspend fun deleteBookingById(id: UUID)

    @Query("DELETE FROM booking")
    suspend fun deleteAllBookings()
}
