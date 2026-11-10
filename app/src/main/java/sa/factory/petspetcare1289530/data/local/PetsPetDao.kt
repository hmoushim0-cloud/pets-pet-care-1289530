package sa.factory.petspetcare1289530.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PetsPetDao {
    @Query("SELECT * FROM pet_care_items_cache ORDER BY id ASC")
    fun observeAllItems(): Flow<List<PetsPetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<PetsPetEntity>)

    @Query("DELETE FROM pet_care_items_cache")
    suspend fun clearAll()
}
