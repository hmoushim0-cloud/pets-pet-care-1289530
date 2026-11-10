package sa.factory.petspetcare1289530.di

import sa.factory.petspetcare1289530.data.repository.PetsPetRepositoryImpl
import sa.factory.petspetcare1289530.domain.repository.PetsPetRepository
import sa.factory.petspetcare1289530.domain.usecase.*
import sa.factory.petspetcare1289530.hardware.HardwareSensorDriver
import sa.factory.petspetcare1289530.ui.viewmodel.PetsPetViewModel

class DiContainer {
    val sensorDriver = HardwareSensorDriver()
    val repository: PetsPetRepository = PetsPetRepositoryImpl(sensorDriver)

    val getCatalogUseCase = GetCatalogUseCase(repository)
    val manageCartUseCase = ManageCartUseCase(repository)
    val processCheckoutUseCase = ProcessCheckoutUseCase(repository)
    val observeHardwareTelemetryUseCase = ObserveHardwareTelemetryUseCase(repository)

    fun provideViewModel(): PetsPetViewModel = PetsPetViewModel(
        getCatalogUseCase,
        manageCartUseCase,
        processCheckoutUseCase,
        observeHardwareTelemetryUseCase
    )
}
