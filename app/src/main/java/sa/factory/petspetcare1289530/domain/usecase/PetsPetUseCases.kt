package sa.factory.petspetcare1289530.domain.usecase

import sa.factory.petspetcare1289530.domain.repository.PetsPetRepository
import sa.factory.petspetcare1289530.model.PetsPetItem
import sa.factory.petspetcare1289530.model.PetsPetOrder
import sa.factory.petspetcare1289530.model.PetsPetTelemetry
import kotlinx.coroutines.flow.Flow

class GetCatalogUseCase(private val repository: PetsPetRepository) {
    operator fun invoke(): Flow<List<PetsPetItem>> = repository.catalogFlow
}

class ManageCartUseCase(private val repository: PetsPetRepository) {
    val cart = repository.activeCart
    fun add(item: PetsPetItem) = repository.addToCart(item)
    fun remove(item: PetsPetItem) = repository.removeFromCart(item)
    fun clear() = repository.clearCart()
}

class ProcessCheckoutUseCase(private val repository: PetsPetRepository) {
    suspend operator fun invoke(paymentMethod: String = "Mada Contactless EMV"): PetsPetOrder =
        repository.checkout(paymentMethod)
}

class ObserveHardwareTelemetryUseCase(private val repository: PetsPetRepository) {
    operator fun invoke(): Flow<PetsPetTelemetry> = repository.telemetryFlow
}
