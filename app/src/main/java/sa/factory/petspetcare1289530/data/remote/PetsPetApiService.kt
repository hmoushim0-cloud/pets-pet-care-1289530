package sa.factory.petspetcare1289530.data.remote

import sa.factory.petspetcare1289530.model.PetsPetItem
import sa.factory.petspetcare1289530.model.PetsPetOrder

interface PetsPetApiService {
    suspend fun fetchCatalog(): List<PetsPetItem>
    suspend fun submitOrder(order: PetsPetOrder): Boolean
}
