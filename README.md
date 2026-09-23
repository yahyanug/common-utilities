# common-utilities

A Java 17 library of local validation, normalization, masking, and security inspection
utilities for backend applications. The consolidated V1/V2 API includes country-specific
formats and caller-configured upload, spreadsheet, and network checks. It performs no
government verification, DNS resolution, HTTP requests, or archive extraction.

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

- ISO 3166-1 alpha-2 and alpha-3 metadata for the ten countries in `AseanCountry`.
- Identity validators and normalizers for the documented Indonesia, Malaysia, Singapore,
  Thailand, Philippines, Vietnam, Brunei, and Laos identifiers. Their validation levels,
  audit status, and limitations are listed in [Validation Support](docs/VALIDATION_SUPPORT.md).
- FORMAT_ONLY postal-code validators for all ten supported countries.
- Country-aware phone validation for those countries and E.164 normalization
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
- Upload-security inspection: caller-defined file-size limits; normalized extension and
  caller-policy suspicious-double-extension checks; Image I/O header-only dimensions and
  pixel-count limits; and Tika archive recognition plus ZIP local entry-path,
  entry-count, and running expansion-ratio checks without extraction.
- CSV/XLSX import-export security: caller-defined normalized required-header checks,
  duplicate and blank-header detection, spreadsheet formula-like text neutralization,
  RFC 4180 CSV field escaping, and Apache POI formula-cell detection without evaluation.
- Network and SSRF inspection: local URL/hostname/IP syntax checks, URI normalization and
  host/scheme extraction, port and caller allow-list checks, safe redirect validation,
  and obvious private, loopback, link-local, and non-public literal target classification.
- HTTP request metadata: servlet request client-IP, host, origin, user-agent, and request
  detail extraction. Forwarded headers require a correctly configured trusted proxy or
  gateway before they can inform security decisions.

## Quick Examples

```java
import id.com.flare.common.utilities.country.AseanCountry;
import id.com.flare.common.utilities.masking.SensitiveDataMasker;
import id.com.flare.common.utilities.money.MoneyAmount;
import id.com.flare.common.utilities.phone.PhoneNumberValidator;
import id.com.flare.common.utilities.spreadsheet.CsvExportSanitizer;
import id.com.flare.common.utilities.spreadsheet.TabularHeaderValidator;
import id.com.flare.common.utilities.network.SsrfTargetClassifier;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

boolean validPhone = PhoneNumberValidator.isValid("0812 3456 7890", AseanCountry.INDONESIA);
String e164 = PhoneNumberValidator.normalize("0812 3456 7890", AseanCountry.INDONESIA)
        .orElseThrow();
String masked = SensitiveDataMasker.maskCreditCard("1234567890123456");
BigDecimal roundedTax = MoneyAmount.round(new BigDecimal("7.505"), "USD", RoundingMode.HALF_UP)
        .orElseThrow();
boolean headersMatch = TabularHeaderValidator.hasRequiredColumns(
        List.of(" Item Code ", "Quantity"), List.of("item code", "quantity"));
String csvField = CsvExportSanitizer.escapeCell("=1+1"); // apostrophe-prefixed text
var target = SsrfTargetClassifier.classify("https://example.com/import");
// HOSTNAME means unresolved, not an approval to connect.
```

## Dependencies and integration

| Dependency | Scope | Purpose |
|---|---|---|
| libphonenumber 8.13.40 | implementation | Regional phone parsing/validation |
| Tika core 2.9.0 | implementation | Content-based MIME detection and extension metadata |
| Commons CSV 1.14.1 | implementation | CSV quoting/escaping |
| POI core 5.5.1 | api | The existing Excel detector accepts POI `Cell` |
| Jakarta Servlet API 6.0.0 | compileOnly/testImplementation | Servlet request metadata extraction |
| POI OOXML 5.5.1 | testImplementation | XLSX test fixtures only |

Versions are pinned in `gradle.properties`. Dependencies bring their own transitive
libraries; core utilities do not require Spring. Applications opening/writing XLSX must
add `org.apache.poi:poi-ooxml` themselves. The library only inspects supplied cells and
text; it does not load or write workbooks. CSV import parsing should use a mature parser
directly. All utility classes are stateless; callers own streams, mutable cells and
collections and must prevent concurrent mutation during inspection.

Public signatures are preserved, including existing convenience entry points. Most
validators return false for null and normalizers return empty; masking/export text
preserves null (CSV renders it as an empty field). Check-digit calculation and invalid
limit constructors throw `IllegalArgumentException`; stream inspection may throw
`IOException`. Consult each method's JavaDoc for exceptions to these conventions.

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

MIME detection and archive/image inspection are not malware detection. Masking is not
encryption or irreversible anonymization. Filename sanitization, lexical
base-directory resolution, and ZIP entry resolution are not authorization and cannot
protect against filesystem-link races. Applications remain responsible for
authorization, storage controls, malware scanning, and safe extraction writes.

Image inspection reads only first-frame headers; readable dimensions do not establish
complete image integrity. ZIP checks cover local entries, not central-directory integrity,
links or nested archives. Ratio checks stop during decompression but do not impose an
absolute decoded-byte or time limit. Bound input resources in the consuming application.

Spreadsheet export prefixes formula-like text with an apostrophe when its first
non-whitespace, control, or format character is `=`, `+`, `-`, or `@`, including signed
numbers supplied as strings. This is a mitigation whose behavior depends on the spreadsheet
product and import/re-save flow. For XLSX, explicitly write STRING cells and never pass
untrusted text to a formula setter. It does not make an untrusted workbook safe to open.

Network helpers make no DNS or network calls. URL and hostname validity therefore does
not establish DNS existence, resolved-address safety, reachability, ownership, or HTTP
response behavior. Applications must resolve and re-check destination addresses at the
network boundary to defend against DNS rebinding.
`PUBLIC_IP` only means outside the implemented non-public ranges; that list is deliberately
documented as incomplete. Redirect checks require absolute HTTP(S), no credentials and an
exact allowed hostname; they permit all valid ports for that host and are not a same-origin
check. See [Validation Support](docs/VALIDATION_SUPPORT.md) for exact behavior and limitations.

## Build

```bat
gradlew.bat format
gradlew.bat clean build
```
