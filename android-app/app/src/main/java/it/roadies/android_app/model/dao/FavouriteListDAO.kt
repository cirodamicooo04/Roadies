package it.roadies.android_app.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import it.roadies.android_app.model.FavouriteList
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouriteListDao {

    @Query("SELECT * FROM favourite_list WHERE owner_id = :ownerId ORDER BY name ASC")
    fun observeByOwner(ownerId: String): Flow<List<FavouriteList>>

    @Query("SELECT * FROM favourite_list WHERE owner_id = :ownerId ORDER BY name ASC")
    suspend fun getByOwner(ownerId: String): List<FavouriteList>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(lists: List<FavouriteList>)

    @Query("DELETE FROM favourite_list WHERE owner_id = :ownerId")
    suspend fun deleteByOwner(ownerId: String)

    @Query("DELETE FROM favourite_list")
    suspend fun clearAll()
}
