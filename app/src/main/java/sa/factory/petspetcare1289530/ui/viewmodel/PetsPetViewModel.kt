package sa.factory.petspetcare1289530.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import sa.factory.petspetcare1289530.domain.usecase.*
import sa.factory.petspetcare1289530.model.PetsPetItem
import sa.factory.petspetcare1289530.model.PetsPetOrder
import sa.factory.petspetcare1289530.model.PetsPetTelemetry
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PetsPetUiState(
    val catalog: List<PetsPetItem> = emptyList(),
    val filteredCatalog: List<PetsPetItem> = emptyList(),
    val selectedCategory: String = "الكل",
    val cart: List<PetsPetItem> = emptyList(),
    val telemetry: PetsPetTelemetry? = null,
    val lastOrder: PetsPetOrder? = null,
    val isCheckingOut: Boolean = false,
    val activeTab: String = "home"
)

class PetsPetViewModel(
    private val getCatalogUseCase: GetCatalogUseCase,
    private val manageCartUseCase: ManageCartUseCase,
    private val processCheckoutUseCase: ProcessCheckoutUseCase,
    private val observeHardwareTelemetryUseCase: ObserveHardwareTelemetryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PetsPetUiState())
    val uiState: StateFlow<PetsPetUiState> = _uiState.asStateFlow()

    init {
        // Observe Catalog
        getCatalogUseCase().onEach { list ->
            _uiState.update { it.copy(catalog = list, filteredCatalog = list) }
        }.launchIn(viewModelScope)

        // Observe Cart
        manageCartUseCase.cart.onEach { cartItems ->
            _uiState.update { it.copy(cart = cartItems) }
        }.launchIn(viewModelScope)

        // Observe Telemetry
        observeHardwareTelemetryUseCase().onEach { telem ->
            _uiState.update { it.copy(telemetry = telem) }
        }.launchIn(viewModelScope)
    }

    fun selectCategory(category: String) {
        _uiState.update { state ->
            val filtered = if (category == "الكل") state.catalog else state.catalog.filter { it.category == category }
            state.copy(selectedCategory = category, filteredCatalog = filtered)
        }
    }

    fun switchTab(tab: String) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun addToCart(item: PetsPetItem) = manageCartUseCase.add(item)
    fun removeFromCart(item: PetsPetItem) = manageCartUseCase.remove(item)

    fun checkout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingOut = true) }
            val order = processCheckoutUseCase()
            _uiState.update { it.copy(isCheckingOut = false, lastOrder = order, activeTab = "checkout") }
        }
    }
}
