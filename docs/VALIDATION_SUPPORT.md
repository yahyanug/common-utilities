# Validation Support

This document describes the consolidated V1/V2 source. Local validation checks the stated
format, structure, or checksum; it never establishes government registration, issuance,
ownership, current status, or database existence.

`VERIFIED` means the implementation's stated rules have an authoritative supporting
reference in the affected validator's JavaDoc. `PARTIALLY_VERIFIED` means the documented
format is implemented but a complete authoritative rule is unavailable, so the validator
deliberately checks only the supported subset. These retained confidence labels concern
the documented identifier rules, not a guarantee of current issuance policy. No additional
country rules or confidence upgrades were introduced during consolidation.

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

NIK has no normalizer or full-date extraction. NPWP normalization trims surrounding
Java `trim()` whitespace, accepts 15/16 digits or exactly `NN.NNN.NNN.N-NNN.NNN`, and
formats only legacy 15-digit values. Malaysian NRIC normalization checks representation
only; `isValid` additionally checks month/day (including February 29, without resolving
the century). Singapore accepts S/T/F/G/M prefixes and case-normalizes validated values.
Brunei normalization returns the hyphenated display form. Vietnam tax normalization trims
surrounding whitespace; citizen-ID year extraction spans 1900-2399 without rejecting
future years. Laos has validation only. Invalid/null extraction inputs return empty.

## Postal codes and country metadata

All postal validators are FORMAT_ONLY and reference UPU documentation in JavaDoc. They
perform no normalization, checksum, existence check or address lookup. Null, whitespace,
wrong length and non-ASCII digits fail; leading zeroes are accepted.

| Country | Accepted format |
|---|---|
| Indonesia | 5 ASCII digits |
| Malaysia | 5 ASCII digits |
| Singapore | 6 ASCII digits |
| Thailand | 5 ASCII digits |
| Philippines | 4 ASCII digits |
| Vietnam | 5 ASCII digits |
| Brunei | Uppercase B/K/P/T, uppercase ASCII letter, 4 ASCII digits |
| Cambodia | 6 ASCII digits |
| Laos | 5 ASCII digits |
| Myanmar | 7 ASCII digits |

`AseanCountry` provides ISO code metadata for all ten ASEAN countries in scope. It does
not imply identity-validation support. Postal-code validators are implemented for every
country in that enum and validate documented local formats only. Country lookups accept
alpha-2/alpha-3 case-insensitively, without trimming; null/unknown codes return empty.
The enum is a fixed supported set, not a live membership registry.

## Upload security

`FileSizeValidator` compares a known byte size with an inclusive caller-supplied limit;
zero size is accepted, negative size/limit is false. It does not measure content or
verify a claimed Content-Length. `FileExtensionInspector` returns a
lowercase extension without its leading period and can identify multiple extensions. A
double extension is only suspicious when an earlier extension matches the
caller-supplied set, because compound names such as `archive.tar.gz` are legitimate.
Both slash styles select the final filename component. `.profile` has no extension;
`.profile.php.jpg` has two. Normalization trims, lowercases and removes one leading dot;
it does not validate content or provide a complete filename character policy.

`FileMimeTypeDetector` uses Tika without filename hints. Byte-array null/empty input returns
empty; the stream method returns empty only for null and otherwise returns Tika's result
(including its empty-stream type). It may consume bytes, never closes caller streams,
and propagates I/O errors. MIME allow-lists are case-insensitive; generic octet-stream
and unknown extensions fail extension-consistency checks. PDF/PNG/JPEG/WebP helpers are
MIME recognition, not complete signature/format validation or malware detection.

`FileNameSanitizer` reduces input to one component, replaces prohibited characters,
trims and removes trailing dots/spaces, and prefixes the implemented Windows device names.
`resolveInsideBaseDirectory` uses that sanitized name; it intentionally discards parent
components rather than rejecting them. Both null/unusable inputs return empty. This is
lexical containment only, with no protection against existing links or filesystem races.

`ImageDimensionInspector` uses Java Image I/O readers to obtain width and height without
decoding a full image raster. Its dimension and pixel-count checks are inclusive and
require caller-supplied limits. Only frame zero is inspected; image data, CRCs and later
frames are not validated. Reader availability depends on the installed Image I/O providers
(for example, WebP MIME recognition does not imply a WebP reader). The byte-array overload
returns empty on IOException; the stream overload propagates malformed-header/I/O errors,
returns empty for null/unsupported input, and leaves the stream open. It uses memory caching
without temporary files; callers should bound input. Dimensions are non-negative ints
(negative construction throws); pixel multiplication uses long, and null dimensions or
negative limits fail. A readable header does not prove a complete image.

`ZipArchiveInspector` uses Tika only to identify common archive MIME types, and uses
Java `ZipInputStream` to scan ZIP local entries without extraction. Recognition covers
ZIP, TAR, gzip, 7z and RAR MIME types; only ZIP entries can be scanned. Paths must use
relative forward slashes and each component must survive `FileNameSanitizer` unchanged:
traversal, absolute/drive paths, backslashes, empty components, implemented Windows devices,
alternate streams and ambiguous trailing spaces/dots fail. One final slash is allowed
for a directory. Invalid/native-unrepresentable resolved paths return empty.

Limits are inclusive: count includes directory entries; the running aggregate ratio is
decoded payload bytes / compressed payload bytes (excluding ZIP headers). Ratio checks
run after each 8192-byte output buffer and can reject an archive whose final ratio might
later decrease. Zero counts/ratios are allowed; negative/non-finite configuration throws
IllegalArgumentException. Null content/limits, bad local entries, bad CRCs and truncated
initial headers fail. Empty ZIPs require a complete empty end record. Other I/O errors
propagate. The inflater is released on all exits; caller streams remain open and rejection
does not drain the remainder.

This is not whole-archive integrity validation: central-directory consistency, missing
central directories on non-empty streams, trailing content, symlink metadata, duplicate
entry collisions and nested archives are not checked. No absolute decoded-byte/time cap
is imposed. Bound input size, processing time and extraction resources separately. No
malware protection or protection against filesystem-link races is provided.

## CSV and spreadsheet security

`TabularHeaderValidator` applies Unicode NFKC normalization, collapses whitespace, and
uses lowercase `Locale.ROOT` names before identifying missing caller-required columns
or duplicates. It has no built-in Product, Stock, Customer, or other business schema.
Blank, null, or duplicate headers are rejected by `hasRequiredColumns` because they
make import mapping ambiguous.
Empty required sets impose no schema (two empty lists succeed). Missing-column diagnostics
ignore blank required declarations, while the boolean validation rejects them. Null
collections have documented diagnostic-specific results. Returned duplicate/missing sets
are immutable and retain encounter order; input collections must not be mutated concurrently.

`SpreadsheetFormulaGuard` treats a value as formula-like when its first non-whitespace,
control, or Unicode-format code point is `=`, `+`, `-`, or `@`. It prefixes such export
values with an apostrophe. `CsvExportSanitizer` then delegates RFC 4180 quoting for one
field to Apache Commons CSV, including commas, quotes, and line breaks. This is a
defensive export convention, not formula evaluation, input authorization, or a
guarantee for every spreadsheet product.
Null formula text is false and sanitizes to null; CSV null is an empty field. Empty text
and ordinary Unicode text remain unchanged. Negative/signed numbers supplied as text
are deliberately guarded. CSV output represents one field, not an entire record; callers
must use a consistent dialect and never double-escape or bypass escaping for other fields.

`ExcelFormulaCellDetector` only reads Apache POI's declared `CellType.FORMULA`; it does
not evaluate a formula or inspect cached results. Consumers parsing workbooks remain
responsible for resource limits, external-link handling, macro policy, and untrusted
workbook safety.
For XLSX exports, consumers must write explicit STRING cells, never formulas. Apostrophe
prefixing is a text mitigation, not XLSX XML escaping or workbook generation. POI handles
actual workbook serialization. Callers own the mutable cell/workbook and its synchronization.

## Network and SSRF security

`UrlValidator` uses Java `URI` parsing for absolute URL syntax, extracts schemes and
hosts, normalizes accepted URLs, and applies caller-supplied allow-lists. Validation is
FORMAT_ONLY. URLs must be absolute server URIs with an ASCII/ACE hostname or strict literal;
Unicode hostnames must first be converted using `HostnameValidator`. Empty ports, scoped
IPv6, and ambiguous integer/octal/hex/abbreviated numeric hosts are rejected. Public
`isValidPort` accepts `1-65535` and the existing `-1` omitted-port sentinel. URL syntax
alone permits credentials and non-HTTP schemes. Normalization lowercases scheme/host,
removes hostname root dots and applies Java URI path normalization while preserving
raw escapes, user info, port, query and fragment. Host extraction preserves spelling
except IPv6 brackets; scheme extraction lowercases.

Host allow-lists compare exact normalized names (IDN/case/root-dot normalization), not
wildcards or subdomains; IPv6 literals retain their textual spelling. Schemes compare
case-insensitively. Null sets fail and null/invalid entries are ignored. Ports are not
part of host allow-lists. `HostnameValidator` accepts single labels, uses Java IDN STD3
conversion and label/total length checks, and rejects literals after normalization. It
does not check TLD registration or existence.

`IpAddressValidator` is a literal-only local parser: four decimal octets without leading
zeroes, or unbracketed IPv6 with optional final embedded IPv4. Zones/CIDR/whitespace fail.
Null/invalid classification inputs return false, so check syntax separately. `parseLiteral`
uses `InetAddress.getByAddress` only; mapped IPv6 can return Java's IPv4 representation.

The implemented non-public classification covers the following fixed subset of the
[IANA IPv4](https://www.iana.org/assignments/iana-ipv4-special-registry/) and
[IANA IPv6](https://www.iana.org/assignments/iana-ipv6-special-registry/) special-use spaces:

| Classification | IPv4 | IPv6 |
|---|---|---|
| Private | 10/8, 172.16/12, 192.168/16 | fc00::/7 |
| Loopback | 127/8 | ::1 |
| Link-local | 169.254/16 | fe80::/10 |
| Other non-public | 0/8, 100.64/10, 192.0.0/24, 192.0.2/24, 198.18/15, 198.51.100/24, 203.0.113/24, 224/4 and 240/4 | ::, fec0::/10, ff00::/8, 2001:db8::/32 |

IPv4-mapped IPv6 uses the embedded IPv4 classification. The whole 192.0.0/24 block is
conservatively flagged, including globally reachable exceptions. This is not a complete
or automatically updated special-use registry: translation/tunnel addresses and other
reserved IPv6 spaces are not comprehensively classified. `PUBLIC_IP` means only that
none of the listed rules matched, not proof of public reachability.

`SsrfTargetClassifier` identifies malformed URLs, obvious local hostnames, and unsafe
literal addresses. Trailing-root-dot localhost and its subdomains are classified locally.
`classify` reports host category independent of scheme; `isObviouslyUnsafe` also rejects
non-HTTP(S) schemes and credentials. False means no obvious match, not permission to connect.
`isSafeRedirectUrl` requires HTTP/HTTPS, an exact caller
allow-listed hostname, no user info, and a hostname target. These checks cannot classify
a hostname's eventual DNS result or stop DNS rebinding, proxies, redirects followed by
an HTTP client, or application-specific authorization; callers must resolve and re-check
the final connection address at the network boundary.
Relative/scheme-relative redirects and all literal-IP redirects fail. Any valid port is
allowed for a listed hostname, so this is not same-origin validation. DNS classification,
every followed redirect, and HTTP-client interpretation remain the application's responsibility.

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
`detectType` checks length/digits only, not checksum. UPC-E is unsupported. Luhn is CHECKSUM
validation over non-empty ASCII digits (including the single digit `0`); it does not
identify issuers or validate a card account. All checksum validators reject null; check-digit
calculators throw IllegalArgumentException for null or malformed payloads.

## Masking

Masking counts Unicode code points, preserves null, and uses `*`. Generic prefix/suffix
lengths that cover or overlap the value mask it fully; non-null input with negative
lengths throws. Phone masking exposes four leading/two trailing characters, NIK two/four,
and NRIC/card/account/token masking only four trailing characters, unless that would
expose the whole value. Email masking retains one local-part character and the domain
when there is exactly one non-edge @; a one-character local part is fully masked and
otherwise malformed values are masked in full. These are display conventions without
identifier validation, encryption, irreversible anonymization or sensitive JSON traversal.

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
