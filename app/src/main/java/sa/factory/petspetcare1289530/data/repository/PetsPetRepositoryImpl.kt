package sa.factory.petspetcare1289530.data.repository

import sa.factory.petspetcare1289530.domain.repository.PetsPetRepository
import sa.factory.petspetcare1289530.hardware.HardwareSensorDriver
import sa.factory.petspetcare1289530.model.PetsPetItem
import sa.factory.petspetcare1289530.model.PetsPetOrder
import sa.factory.petspetcare1289530.model.PetsPetTelemetry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PetsPetRepositoryImpl(
    private val sensorDriver: HardwareSensorDriver
) : PetsPetRepository {

    private val _catalog = MutableStateFlow<List<PetsPetItem>>(listOf(
        PetsPetItem("1", "تفعيل شريحة RFID", "ربط شريحة ISO 11784/11785 بمعرف الحاج عبر قارئ مخصص", 120.0, "أساسي", "تسجيل الحجاج"),
        PetsPetItem("2", "تتبع الموقع الآني", "مراقبة مسار الحاج في المشاعر المقدسة عبر إشارات RFID", 250.0, "مباشر", "إدارة الرحلة"),
        PetsPetItem("3", "تحقق التطعيمات", "مطابقة بيانات التحصين مع متطلبات وزارة الحج عبر ماسح QR", 85.0, "معتمد", "السلامة الصحية")
    ))
    override val catalogFlow: Flow<List<PetsPetItem>> = _catalog.asStateFlow()

    private val _cart = MutableStateFlow<List<PetsPetItem>>(emptyList())
    override val activeCart: StateFlow<List<PetsPetItem>> = _cart.asStateFlow()

    override val telemetryFlow: Flow<PetsPetTelemetry> = sensorDriver.observePeripheralStream()
        .map { frame ->
            PetsPetTelemetry(
                peripheralName = frame.peripheralName,
                status = frame.connectionState,
                liveReading = frame.livePayload,
                batteryPct = 96,
                signalDbm = -54
            )
        }

    override fun addToCart(item: PetsPetItem) {
        val current = _cart.value.toMutableList()
        current.add(item)
        _cart.value = current
    }

    override fun removeFromCart(item: PetsPetItem) {
        val current = _cart.value.toMutableList()
        current.remove(item)
        _cart.value = current
    }

    override fun clearCart() {
        _cart.value = emptyList()
    }

    override suspend fun checkout(paymentMethod: String): PetsPetOrder {
        val currentItems = _cart.value.toList()
        val subtotal = currentItems.sumOf { it.priceSar }
        val vat = subtotal * 0.15
        val total = subtotal + vat
        val order = PetsPetOrder(
            orderId = "ORD-${System.currentTimeMillis() % 100000}",
            items = currentItems,
            subtotalSar = subtotal,
            vatSar = vat,
            totalSar = total,
            paymentMethod = paymentMethod
        )
        clearCart()
        return order
    }

    override suspend fun refreshCatalog() {
        // Offline-first sync logic
    }
}
