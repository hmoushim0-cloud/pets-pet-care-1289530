package sa.factory.petspetcare1289530.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import sa.factory.petspetcare1289530.model.PetsPetItem

@Entity(tableName = "pet_care_items_cache")
data class PetsPetEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val priceSar: Double,
    val badge: String,
    val category: String,
    val status: String
) {
    fun toDomain(): PetsPetItem = PetsPetItem(
        id = id,
        title = title,
        description = description,
        priceSar = priceSar,
        badge = badge,
        category = category,
        status = status
    )
}
