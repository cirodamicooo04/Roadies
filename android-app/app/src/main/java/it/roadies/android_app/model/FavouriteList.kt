package it.roadies.android_app.model
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID
@Entity(tableName = "favourite_list")
data class FavouriteList(
    @PrimaryKey val id: UUID,
    @ColumnInfo(name = "owner_id") val ownerId: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "length") val length: Int,
    @ColumnInfo(name = "visibility") val visibility: String
)