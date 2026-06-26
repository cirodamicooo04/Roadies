package it.roadies.android_app.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import it.roadies.android_app.client.models.booking.DepartureType
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity(tableName = "booking")
class Booking(@PrimaryKey val id: UUID,
              @ColumnInfo(name = "principal_id") val principalId: UUID?,
              @ColumnInfo(name = "total_price") val totalPrice: BigDecimal,
              @ColumnInfo(name = "people_count") val peopleCount: Int,
              @ColumnInfo(name = "start_date") val startDate: LocalDateTime,
              @ColumnInfo(name = "end_date") val endDate: LocalDateTime,
              @ColumnInfo(name = "title") val title: String,
              @ColumnInfo(name = "departure_type") val departureType: DepartureType?
)

