# Validation Support

This matrix describes V1 implementations only. Local validation checks the stated
format, structure, or checksum; it never establishes government registration, issuance,
ownership, current status, or database existence.

`VERIFIED` means the implementation's stated rules have an authoritative supporting
reference in the affected validator's JavaDoc. `PARTIALLY_VERIFIED` means the documented
format is implemented but a complete authoritative rule is unavailable, so the validator
deliberately checks only the supported subset. `CORRECTED` and `UNVERIFIED` have no V1
entries.

| Country | Identifier | Validation level | Audit status | Format | Structure | Checksum | Extraction | Limitations |
|---|---|---|---|---|---|---|---|---|
| Indonesia | NIK | STRUCTURAL | VERIFIED | 16 ASCII digits | Calendar day/month, documented gender-day encoding, and final sequence from `0001` | No | Gender; province, city/regency, district digits | Region codes and full birth year are not verified or derived |
| Indonesia | NPWP | FORMAT_ONLY | VERIFIED | 15 or 16 ASCII digits; legacy display accepted | Length only | No | No | Registration, NIK linkage, and taxpayer type are not verified |
| Malaysia | NRIC | STRUCTURAL | VERIFIED | 12 ASCII digits; display hyphens accepted | Calendar month/day | No | No | Birthplace and issuance are not verified; year is ambiguous |
| Singapore | NRIC / FIN | STRUCTURAL_AND_CHECKSUM | PARTIALLY_VERIFIED | Prefix, 7 ASCII digits, final letter | Supported NRIC/FIN prefixes | Published Mod-11 variant | No | Issuance and holder identity are not verified; checksum support is documented by the linked technical reference |
| Thailand | National ID | STRUCTURAL_AND_CHECKSUM | VERIFIED | 13 ASCII digits | Person-category digit `1-8` | Published weighted Mod-11 calculation | No | Issuance and holder are not verified |
| Philippines | PhilSys Card Number | FORMAT_ONLY | VERIFIED | Public 16 ASCII-digit PCN | Length only | No | No | Does not accept confidential PSN or verify card/token validity |
| Vietnam | Citizen identity number | FORMAT_ONLY | VERIFIED | 12 ASCII digits | Length only | No | Gender; birth year | Location codes and issuance are not verified |
| Vietnam | Tax ID | STRUCTURAL | VERIFIED | 10 ASCII digits or 10 digits plus `-` and `001-999` | Published base/dependent layout | No | No | Registration and base-ID check digit are not verified |
| Brunei | Identity Card | STRUCTURAL | PARTIALLY_VERIFIED | 8 ASCII digits or `NN-NNNNNN` | Published block and serial ranges | No | Resident category | Issuance and current status are not verified; published rule support is indirect |
| Laos | Tax ID | STRUCTURAL | PARTIALLY_VERIFIED | 12 ASCII digits or 9 ASCII digits, `-`, 3 ASCII digits | VAT-status position | No | No | Monitoring digit is not calculated because no authoritative local algorithm is documented; registration is not verified |
| Indonesia | Postal code | FORMAT_ONLY | 5 ASCII digits | Length only | No | No | Existence and address assignment are not verified |
| Malaysia | Postal code | FORMAT_ONLY | 5 ASCII digits | Length only | No | No | Existence and address assignment are not verified |
| Singapore | Postal code | FORMAT_ONLY | 6 ASCII digits | Length only | No | No | Existence and address assignment are not verified |
| Thailand | Postal code | FORMAT_ONLY | 5 ASCII digits | Length only | No | No | Existence and address assignment are not verified |
| Philippines | Postal code | FORMAT_ONLY | 4 ASCII digits | Length only | No | No | Existence and address assignment are not verified |
| Vietnam | Postal code | FORMAT_ONLY | 5 ASCII digits | Length only | No | No | Existence and address assignment are not verified |
| Brunei | Postal code | FORMAT_ONLY | District letter (`B`, `K`, `P`, or `T`), ASCII letter, 4 ASCII digits | Documented district-letter set | No | No | Existence and address assignment are not verified |
| Cambodia | Postal code | FORMAT_ONLY | 6 ASCII digits | Length only | No | No | Existence and address assignment are not verified |
| Laos | Postal code | FORMAT_ONLY | 5 ASCII digits | Length only | No | No | Existence and address assignment are not verified |
| Myanmar | Postal code | FORMAT_ONLY | 7 ASCII digits | Length only | No | No | Existence and address assignment are not verified |

`AseanCountry` provides ISO code metadata for all ten ASEAN countries in scope. It does
not imply identity-validation support. Postal-code validators are implemented for every
country in that enum and validate documented local formats only.

## Barcode and GS1

| Identifier | Validation level | Format | Checksum | Limitations |
|---|---|---|---|---|
| GTIN-8 | STRUCTURAL_AND_CHECKSUM | 8 ASCII digits | GS1 modulo-10 | Does not verify GS1 registration, barcode assignment, manufacturer validity, or product existence |
| GTIN-12 | STRUCTURAL_AND_CHECKSUM | 12 ASCII digits | GS1 modulo-10 | Does not verify GS1 registration, barcode assignment, manufacturer validity, or product existence |
| GTIN-13 | STRUCTURAL_AND_CHECKSUM | 13 ASCII digits | GS1 modulo-10 | Does not verify GS1 registration, barcode assignment, manufacturer validity, or product existence |
| GTIN-14 | STRUCTURAL_AND_CHECKSUM | 14 ASCII digits | GS1 modulo-10 | Does not verify GS1 registration, barcode assignment, manufacturer validity, or product existence |

GTIN-8, GTIN-12, and GTIN-13 are validated consistently with EAN-8, UPC-A, and EAN-13.
`GtinValidator.normalizeToGtin14` returns a right-justified, zero-padded 14-digit GTIN
only when the supplied GTIN passes validation.

## Currency and money

| Feature | Behavior | Limitations |
|---|---|---|
| Currency code | `CurrencyValidator` accepts canonical ISO 4217 codes known to `java.util.Currency` and exposes Java's default fraction digits | Currency support does not establish merchant, provider, or country acceptance; currencies with Java minor-unit value `-1` cannot be used for money amounts |
| Major/minor conversion | `MoneyAmount` converts through `BigDecimal` and `BigInteger` only when the amount's declared scale is no greater than the currency's minor-unit scale | No floating-point conversion, implicit rounding, or truncation |
| Scale and rounding | Scale validation is strict; rounding requires a caller-supplied `RoundingMode` | No default financial rounding policy |
| Percentage, discount, and tax | Percentage is `0-100`; decimal rate is `0-1`; discount and tax calculators return the rounded component amount | No promotion, eligibility, tax-jurisdiction, exemption, inclusivity, FX, or exchange-rate policy |

## Phone numbers

`PhoneNumberValidator` supports every `AseanCountry` region through the bundled
libphonenumber metadata. `isValid(value, country)` parses national input with that
country as its default region, then requires `isValidNumberForRegion`. Therefore an
international input, including one beginning with `+`, is accepted only when it belongs
to the supplied country. `normalize(value, country)` returns E.164 only after that same
validation succeeds.

The `00` international prefix is region-specific metadata, not a prefix the utility
adds or recognizes globally. The validator accepts libphonenumber-supported national,
international, and punctuation-bearing representations. Its result establishes only
consistency with the bundled numbering-plan metadata; it does not establish assignment,
ownership, SIM existence, reachability, or active subscription status.
