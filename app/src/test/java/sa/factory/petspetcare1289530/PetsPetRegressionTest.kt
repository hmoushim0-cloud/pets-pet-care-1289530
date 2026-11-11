package sa.factory.petspetcare1289530

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import sa.factory.petspetcare1289530.data.repository.PetsPetRepositoryImpl
import sa.factory.petspetcare1289530.engine.Iso11784MicrochipProtocolEngine
import sa.factory.petspetcare1289530.engine.Iso11784MicrochipProtocolEngine.*
import sa.factory.petspetcare1289530.engine.VeterinaryCareScheduleEngine
import sa.factory.petspetcare1289530.engine.VeterinaryCareScheduleEngine.*
import sa.factory.petspetcare1289530.hardware.HardwareSensorDriver
import sa.factory.petspetcare1289530.model.PetsPetItem
import java.time.LocalDate

class PetsPetRegressionTest {

    private val rfidEngine = Iso11784MicrochipProtocolEngine()
    private val careEngine = VeterinaryCareScheduleEngine()

    @Test
    fun test15DigitMicrochipValidation() {
        // Saudi Arabia National Prefix (682)
        val saudiChip = rfidEngine.parse15DigitMicrochip("682000123456789")
        assertTrue(saudiChip.checksumValid)
        assertTrue(saudiChip.isSaudiMewaCompliant)
        assertEquals(682, saudiChip.countryOrManufacturerCode)
        assertEquals(123456789L, saudiChip.nationalAnimalId)

        // ICAR accredited manufacturer prefix (900-998)
        val icarChip = rfidEngine.parse15DigitMicrochip("985141002345678")
        assertTrue(icarChip.checksumValid)
        assertTrue(icarChip.isSaudiMewaCompliant)
        assertEquals(985, icarChip.countryOrManufacturerCode)

        // Invalid chip cases
        val shortChip = rfidEngine.parse15DigitMicrochip("68200012345678") // 14 digits
        assertFalse(shortChip.checksumValid)
        assertFalse(shortChip.isSaudiMewaCompliant)

        val nonDigit = rfidEngine.parse15DigitMicrochip("68200012345678A")
        assertFalse(nonDigit.checksumValid)
    }

    @Test
    fun testMicrochipFormatting() {
        val raw = "682000123456789"
        val formatted = rfidEngine.formatIcarStandardDisplay(raw)
        assertEquals("682 000 123 456 789", formatted)
    }

    @Test
    fun testBioThermoTemperatureDecoding() {
        // Raw hex 0x140 (320 decimal): 32.0 + (320 * 0.02) = 38.4 C
        val temp = rfidEngine.decodeBioThermoTemperature("0140")
        assertNotNull(temp)
        assertEquals(38.4, temp!!, 0.1)

        // Out-of-bounds analog reading (extreme fever or cryogenic)
        val invalidTemp = rfidEngine.decodeBioThermoTemperature("0FFF")
        assertNull(invalidTemp)
    }

    @Test
    fun testCrc16CcittChecksumDeterminism() {
        val payloadA = "ISO11784_TELEGRAM_SAMPLE_A".toByteArray(Charsets.US_ASCII)
        val payloadB = "ISO11784_TELEGRAM_SAMPLE_A".toByteArray(Charsets.US_ASCII)
        val payloadC = "ISO11784_TELEGRAM_SAMPLE_B".toByteArray(Charsets.US_ASCII)

        val crcA = rfidEngine.computeCrc16Ccitt(payloadA)
        val crcB = rfidEngine.computeCrc16Ccitt(payloadB)
        val crcC = rfidEngine.computeCrc16Ccitt(payloadC)

        assertEquals(crcA, crcB)
        assertNotEquals(crcA, crcC)
        assertTrue(crcA in 0x0000..0xFFFF)
    }

    @Test
    fun testRestingEnergyRequirementAllometricFormula() {
        // 10 kg dog: 70 * (10^0.75) ≈ 70 * 5.6234 ≈ 393.6 kcal
        val rer10kg = careEngine.calculateRerKcal(10.0)
        assertEquals(393.6, rer10kg, 1.0)

        // 4 kg cat: 70 * (4^0.75) = 70 * 2.8284 ≈ 198.0 kcal
        val rer4kg = careEngine.calculateRerKcal(4.0)
        assertEquals(198.0, rer4kg, 1.0)
    }

    @Test
    fun testMaintenanceEnergyAdjustmentByBcs() {
        val profileIdeal = PetMedicalProfile(
            petId = "PET-1",
            petNameAr = "سيمبا",
            microchipNumber = "682000123456789",
            weightKg = 10.0,
            ageMonths = 24,
            bcs = BodyConditionScore.IDEAL,
            isNeutered = true
        )
        val dailyCaloriesIdeal = careEngine.calculateDailyCaloricNeeds(profileIdeal)

        val profileObese = profileIdeal.copy(bcs = BodyConditionScore.OBESE)
        val dailyCaloriesObese = careEngine.calculateDailyCaloricNeeds(profileObese)

        // Obese pet should receive a 30% reduction in maintenance calories (factor 0.7 vs 1.0)
        assertTrue(dailyCaloriesObese < dailyCaloriesIdeal)
        assertEquals(dailyCaloriesIdeal * 0.7, dailyCaloriesObese, 1.0)
    }

    @Test
    fun testInternationalTravelReadinessCompliance() {
        val today = LocalDate.now()
        val travelDate = today.plusDays(30)

        val profile = PetMedicalProfile(
            petId = "PET-2",
            petNameAr = "لونا",
            microchipNumber = "682000123456789",
            weightKg = 4.5,
            ageMonths = 18,
            bcs = BodyConditionScore.IDEAL_SOLID,
            isNeutered = true
        )

        val validVaccinations = listOf(
            VaccinationRecord(VaccineType.RABIES, today.minusDays(25), "RAB-2026-X", "عيادة الرياض البيطرية"),
            VaccinationRecord(VaccineType.DEWORMING, today.minusDays(5), "DEW-991", "عيادة الرياض البيطرية")
        )

        val auditPassed = careEngine.auditTravelReadiness(profile, validVaccinations, travelDate)
        assertTrue(auditPassed.isReadyForTravel)
        assertTrue(auditPassed.microchipVerified)
        assertTrue(auditPassed.rabiesValid)
        assertTrue(auditPassed.dewormingValid)
        assertTrue(auditPassed.remarksAr.isEmpty())

        // Fails if Rabies was administered less than 21 days before travel
        val prematureVaccinations = listOf(
            VaccinationRecord(VaccineType.RABIES, travelDate.minusDays(10), "RAB-2026-X", "عيادة الرياض البيطرية"),
            VaccinationRecord(VaccineType.DEWORMING, today, "DEW-991", "عيادة الرياض البيطرية")
        )
        val auditFailed = careEngine.auditTravelReadiness(profile, prematureVaccinations, travelDate)
        assertFalse(auditFailed.isReadyForTravel)
        assertFalse(auditFailed.rabiesValid)
    }

    @Test
    fun testVaccineBoosterCadence() {
        val adminDate = LocalDate.of(2026, 1, 15)
        val rabies = VaccinationRecord(VaccineType.RABIES, adminDate, "B1", "Clinic A")
        val deworm = VaccinationRecord(VaccineType.DEWORMING, adminDate, "B2", "Clinic A")

        assertEquals(LocalDate.of(2027, 1, 15), rabies.nextDueDate)
        assertEquals(LocalDate.of(2026, 4, 15), deworm.nextDueDate)
    }

    @Test
    fun testCartAndRepositoryCheckoutIntegration() = runBlocking {
        val sensorDriver = HardwareSensorDriver()
        val repository = PetsPetRepositoryImpl(sensorDriver)

        val item1 = PetsPetItem("SV-1", "فحص دوري وقراءة شريحة", "فحص شامل", 120.0, "معتمد", "الرعاية")
        val item2 = PetsPetItem("SV-2", "تطعيم السعار السنوي", "لقاح معتمد", 80.0, "معتمد", "التطعيمات")

        repository.addToCart(item1)
        repository.addToCart(item2)
        assertEquals(2, repository.activeCart.value.size)

        val order = repository.checkout("مدى Mada Contactless")

        assertEquals(200.0, order.subtotalSar, 0.01)
        assertEquals(30.0, order.vatSar, 0.01)
        assertEquals(230.0, order.totalSar, 0.01)
        assertEquals(0, repository.activeCart.value.size)

        val telemetry = sensorDriver.observePeripheralStream().first()
        assertNotNull(telemetry.peripheralName)
    }

    @Test
    fun testZeroEmojiComplianceAcrossEntities() {
        val regexEmoji = "[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+".toRegex()

        for (species in SpeciesCategory.values()) {
            assertFalse("Species category must not contain emoji: ${species.nameAr}", regexEmoji.containsMatchIn(species.nameAr))
        }

        for (bcs in BodyConditionScore.values()) {
            assertFalse("BCS label must not contain emoji: ${bcs.labelAr}", regexEmoji.containsMatchIn(bcs.labelAr))
        }

        for (vax in VaccineType.values()) {
            assertFalse("Vaccine name must not contain emoji: ${vax.vaccineNameAr}", regexEmoji.containsMatchIn(vax.vaccineNameAr))
        }
    }
}
