package it.roadies.android_app.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.util.UUID

@Entity(tableName = "booking")
class Booking(@PrimaryKey val id: UUID,
              @ColumnInfo(name = "status") val status: String,
              @ColumnInfo(name = "total_price") val totalPrice: Int,
              @ColumnInfo(name = "created_at") val createdAt: LocalDateTime,
              @ColumnInfo(name = "people_count") val peopleCount: Int,

              @ColumnInfo(name = "start_date") val startDate: LocalDateTime,
              @ColumnInfo(name = "end_date") val endDate: LocalDateTime,
              @ColumnInfo(name = "title") val title: String,
              @ColumnInfo(name = "destination") val destination: String,
              @ColumnInfo(name = "country") val country: String,
              @ColumnInfo(name = "continent") val continent: String,
)

