package sa.factory.petspetcare1289530.engine

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.pow

/**
 * Advanced Veterinary Care & Preventative Medical Schedule Engine.
 * Manages core vaccination timelines, Body Condition Score (BCS) evaluations,
 * resting energy requirements (RER), and international pet travel health audits.
 * Strictly zero emojis.
 */
class VeterinaryCareScheduleEngine {

    enum class VaccineType(val codeName: String, val vaccineNameAr: String, val frequencyMonths: Int, val isCore: Boolean) {
        RABIES("RABIES", "داء الكلب (السعار)", 12, true),
        DHPP_CANINE("DHPP", "اللقاح الخماسي للكلاب (البارفو والديستمبر)", 12, true),
        FVRCP_FELINE("FVRCP", "اللقاح الثلاثي للقطط", 12, true),
        BORDETELLA("KC", "سعال الكلاب (بورديتيلا)", 6, false),
        LEUKEMIA_FELINE("FeLV", "لوكيميا القطط", 12, false),
        DEWORMING("DEWORM", "التجريع الوقائي للطفيليات", 3, true)
    }

    enum class BodyConditionScore(val score: Int, val labelAr: String, val calorieAdjustmentFactor: Double) {
        EMACIATED(1, "هزال شديد", 1.4),
        VERY_THIN(2, "نحيل جدا", 1.25),
        THIN(3, "نحيل", 1.1),
        IDEAL(4, "وزن مثالي متناسق", 1.0),
        IDEAL_SOLID(5, "وزن مثالي وقوي", 1.0),
        OVERWEIGHT(6, "زيادة طفيفة في الوزن", 0.9),
        HEAVY(7, "وزن زائد", 0.8),
        OBESE(8, "سمنة مفرطة", 0.7),
        SEVERELY_OBESE(9, "سمنة حرجة", 0.6)
    }

    data class PetMedicalProfile(
        val petId: String,
        val petNameAr: String,
        val microchipNumber: String,
        val weightKg: Double,
        val ageMonths: Int,
        val bcs: BodyConditionScore,
        val isNeutered: Boolean = true
    )

    data class VaccinationRecord(
        val vaccine: VaccineType,
        val administeredDate: LocalDate,
        val batchNumber: String,
        val clinicName: String
    ) {
        val nextDueDate: LocalDate get() = administeredDate.plusMonths(vaccine.frequencyMonths.toLong())
        val isOverdue: Boolean get() = LocalDate.now().isAfter(nextDueDate)
    }

    data class TravelReadinessAudit(
        val isReadyForTravel: Boolean,
        val microchipVerified: Boolean,
        val rabiesValid: Boolean,
        val dewormingValid: Boolean,
        val daysUntilNextAction: Long,
        val remarksAr: List<String>
    )

    /**
     * Computes Resting Energy Requirement (RER) in kcal/day:
     * Standard Kleiber-Brody allometric equation: RER = 70 * (Weight in kg)^0.75
     */
    fun calculateRerKcal(weightKg: Double): Double {
        if (weightKg <= 0.0) return 0.0
        val rer = 70.0 * weightKg.pow(0.75)
        return String.format(Locale.US, "%.1f", rer).toDouble()
    }

    /**
     * Computes Daily Maintenance Energy Requirement (MER) adjusted for neuter status and BCS.
     */
    fun calculateDailyCaloricNeeds(profile: PetMedicalProfile): Double {
        val rer = calculateRerKcal(profile.weightKg)
        val neuterFactor = if (profile.isNeutered) 1.6 else 1.8
        val adjusted = rer * neuterFactor * profile.bcs.calorieAdjustmentFactor
        return String.format(Locale.US, "%.1f", adjusted).toDouble()
    }

    /**
     * Checks compliance for Saudi Ministry of Environment, Water and Agriculture (MEWA) pet passport
     * and international travel certification (e.g. EU / GSO standards).
     */
    fun auditTravelReadiness(
        profile: PetMedicalProfile,
        vaccinations: List<VaccinationRecord>,
        travelDate: LocalDate
    ): TravelReadinessAudit {
        val remarks = mutableListOf<String>()

        // 1. Microchip check
        val cleanChip = profile.microchipNumber.replace("\\D".toRegex(), "")
        val microchipValid = cleanChip.length == 15

        if (!microchipValid) {
            remarks.add("Microchip number must be a valid 15-digit ISO 11784 standard code.")
        }

        // 2. Rabies check (must be administered at least 21 days prior to travel and not expired)
        val rabiesRecord = vaccinations.find { it.vaccine == VaccineType.RABIES }
        val rabiesValid = if (rabiesRecord != null) {
            val daysSinceVaccine = ChronoUnit.DAYS.between(rabiesRecord.administeredDate, travelDate)
            val notExpired = !travelDate.isAfter(rabiesRecord.nextDueDate)
            if (daysSinceVaccine < 21) {
                remarks.add("Rabies vaccine must be administered at least 21 days before travel date.")
                false
            } else if (!notExpired) {
                remarks.add("Rabies vaccine will expire prior to travel date.")
                false
            } else {
                true
            }
        } else {
            remarks.add("Missing mandatory Rabies vaccination record.")
            false
        }

        // 3. Deworming check (Echinococcus treatment required within 5 days of travel)
        val dewormingRecord = vaccinations.find { it.vaccine == VaccineType.DEWORMING }
        val dewormingValid = dewormingRecord != null && !travelDate.isAfter(dewormingRecord.nextDueDate)
        if (!dewormingValid) {
            remarks.add("Echinococcus internal parasite treatment must be current within 90 days.")
        }

        val allReady = microchipValid && rabiesValid && dewormingValid
        val daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), travelDate).coerceAtLeast(0)

        return TravelReadinessAudit(
            isReadyForTravel = allReady,
            microchipVerified = microchipValid,
            rabiesValid = rabiesValid,
            dewormingValid = dewormingValid,
            daysUntilNextAction = daysUntil,
            remarksAr = remarks
        )
    }

    /**
     * Identifies vaccines due within the next specified number of days.
     */
    fun getUpcomingVaccinations(vaccinations: List<VaccinationRecord>, windowDays: Long = 30): List<VaccinationRecord> {
        val cutoff = LocalDate.now().plusDays(windowDays)
        return vaccinations.filter { record ->
            record.nextDueDate.isBefore(cutoff) || record.isOverdue
        }
    }
}
