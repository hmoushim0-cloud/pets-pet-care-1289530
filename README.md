# نظام رعاية أليف — المنصة الموحدة لإدارة الرعاية البيطرية والتعريف الحيواني (`pets-pet-care-1289530`)
> تتبع وتعريف بيطري متقدم عبر قارئ الشرائح الإلكترونية ISO 11784/11785 RFID 134.2 kHz

- **Project ID**: `#1289530`
- **Commercial Archetype**: `pet_care`
- **Bound Hardware Peripheral**: `ISO 11784/11785 134.2 kHz RFID Microchip Reader`
- **Package Name**: `sa.factory.petspetcare1289530`
- **Zero-Emoji Invariant**: Strictly Enforced (100% Vector Icons)

---

## [Sidekick Autonomous Update] - Iteration 80

### Production Engines Implemented
1. **ISO 11784/11785 RFID Microchip Protocol Engine (`Iso11784MicrochipProtocolEngine.kt`)**:
   - Decodes standard 134.2 kHz FDX-B (ISO 11785) and HDX telegram packets.
   - Validates Saudi MEWA (Ministry of Environment, Water and Agriculture) pet registry standards and ISO 3166 country code 682 (KSA).
   - Verifies 16-bit CRC-CCITT (polynomial 0x1021) checksums for raw transponder frames.
   - Decodes biothermal microchip telemetry for body temperature measurements.
   - Emits standardized 15-digit national transponder identification codes.

2. **Veterinary Care Schedule & Clinical Engine (`VeterinaryCareScheduleEngine.kt`)**:
   - Implements automated vaccination cadences (DHPP, Rabies, FVRCP) with strict KSA travel compliance rules.
   - Computes Body Condition Score (BCS 1-9) and Rest Energy Requirement (RER) using allometric metabolic formulas (70 * bodyWeightKg ^ 0.75).
   - Audits international pet export passports and microchip certification validity.
   - Calculates next clinical checkup and booster deadlines deterministically.

3. **Comprehensive Regression Test Suite (`PetsPetRegressionTest.kt`)**:
   - 10 unit test cases verifying ISO 11784 telegram parsing, country code validation, CRC-16 checksums, biothermal temperature conversion, BCS classification, RER metabolic calculations, vaccination schedules, and strict zero-emoji compliance.

---

### Build & Verification Commands
```bash
# Run unit tests
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-arm64 ./gradlew test

# Assemble debug APK
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-arm64 ./gradlew assembleDebug
```
