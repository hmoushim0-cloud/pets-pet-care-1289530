package sa.factory.petspetcare1289530.domain.repository

import sa.factory.petspetcare1289530.model.PetsPetItem
import sa.factory.petspetcare1289530.model.PetsPetOrder
import sa.factory.petspetcare1289530.model.PetsPetTelemetry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface PetsPetRepository {
    val catalogFlow: Flow<List<PetsPetItem>>
    val activeCart: StateFlow<List<PetsPetItem>>
    val telemetryFlow: Flow<PetsPetTelemetry>
    
    fun addToCart(item: PetsPetItem)
    fun removeFromCart(item: PetsPetItem)
    fun clearCart()
    suspend fun checkout(paymentMethod: String): PetsPetOrder
    suspend fun refreshCatalog()
}
