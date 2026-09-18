# common-utilities

A lightweight Java 17 library of deterministic validation, normalization, masking, and
file-inspection utilities for backend applications. V1 focuses on reusable ASEAN-aware
metadata and documented identifier formats, without external verification.

## Requirements

Java 17 and the included Gradle wrapper.

## Installation

For local development, publish the project to Maven Local:

```bat
gradlew.bat clean build publishToMavenLocal
```

Then consume the locally published artifact from another Gradle project:

```gradle
repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation 'id.com.flare:common-utilities:0.0.1'
}
```

The project does not currently configure a remote publishing repository.

## Available Utilities

- ASEAN ISO 3166-1 alpha-2 and alpha-3 metadata for all ten V1 countries.
- Identity validators and normalizers for the documented Indonesia, Malaysia, Singapore,
  Thailand, Philippines, Vietnam, Brunei, and Laos identifiers. Their validation levels,
  audit status, and limitations are listed in [Validation Support](docs/VALIDATION_SUPPORT.md).
- FORMAT_ONLY postal-code validators for all ten ASEAN countries.
- Country-aware phone validation for all ten ASEAN countries and E.164 normalization
  through libphonenumber.
- Generic Luhn, EAN-8, EAN-13, UPC-A, and GTIN-8/12/13/14 GS1 check-digit
  validation, type detection, and validated GTIN-14 normalization.
- ISO 4217 currency validation, minor-unit lookup, exact major/minor conversion,
  currency-scale validation and explicit rounding, percentage validation, and generic
  discount/tax amount calculation.
- Sensitive-data masking for generic values, emails, phones, identifiers, cards, bank
  accounts, and tokens.
- Apache Tika content-type detection, caller-defined MIME allow-lists, extension/content
  checks, filename sanitization, and lexical base-directory resolution.

## Quick Examples

```java
import country.id.com.flare.common.utilities.AseanCountry;
import masking.id.com.flare.common.utilities.SensitiveDataMasker;
import money.id.com.flare.common.utilities.MoneyAmount;
import phone.id.com.flare.common.utilities.PhoneNumberValidator;

import java.math.BigDecimal;
import java.math.RoundingMode;

boolean validPhone = PhoneNumberValidator.isValid("0812 3456 7890", AseanCountry.INDONESIA);
String e164 = PhoneNumberValidator.normalize("0812 3456 7890", AseanCountry.INDONESIA)
        .orElseThrow();
String masked = SensitiveDataMasker.maskCreditCard("1234567890123456");
BigDecimal roundedTax = MoneyAmount.round(new BigDecimal("7.505"), "USD", RoundingMode.HALF_UP)
        .orElseThrow();
```

## Validation Semantics

- **FORMAT_ONLY** checks representation rules such as ASCII characters, length, and
  documented separators.
- **STRUCTURAL** additionally checks reliable internal structure such as date fields or
  published serial ranges.
- **CHECKSUM** checks a documented mathematical check digit.
- **STRUCTURAL_AND_CHECKSUM** checks both structure and a documented check digit.

Local validation never proves government registration, issuance, ownership, current
status, or database existence.

Phone validation similarly checks only the bundled libphonenumber numbering-plan
metadata. It does not establish assignment, ownership, SIM existence, reachability, or
an active subscription. Phone input is country-bound: the supplied `AseanCountry` is
the parsing region for national input and must match international input before E.164
normalization succeeds.

Money utilities use Java's ISO 4217 currency metadata and never use `double` or `float`.
Major-to-minor conversion rejects amounts whose declared scale exceeds the currency's
default minor-unit scale. Rounding and tax/discount amount calculation require the caller
to select a `RoundingMode`; they do not impose a financial rounding, tax, discount, or
foreign-exchange policy.

## Security Limitations

MIME detection is not malware detection. Masking is not encryption or irreversible
anonymization. Filename sanitization and lexical base-directory resolution are not
authorization and cannot protect against filesystem-link races. Applications remain
responsible for authorization, storage controls, and malware scanning.

## Build

```bat
gradlew.bat format
gradlew.bat clean build
```
