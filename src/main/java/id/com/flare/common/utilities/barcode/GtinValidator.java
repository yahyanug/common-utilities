package id.com.flare.common.utilities.barcode;

import java.util.Optional;

/**
 * GS1 Global Trade Item Number (GTIN) validation, check-digit calculation, type
 * detection, and canonical 14-digit normalization.
 *
 * <p>
 * GTIN-8, GTIN-12, GTIN-13, and GTIN-14 contain respectively 8, 12, 13, and 14 ASCII
 * digits, including a GS1 modulo-10 check digit. GTIN-8 is equivalent to EAN-8, GTIN-12
 * to UPC-A, and GTIN-13 to EAN-13. Whitespace, separators, and non-ASCII digits are
 * rejected. A valid result does not verify GS1 registration, barcode assignment,
 * manufacturer validity, or product existence. Validation level: STRUCTURAL_AND_CHECKSUM.
 * </p>
 *
 * @see <a href="https://www.gs1.org/services/how-calculate-check-digit-manually">GS1
 * check-digit calculation</a>
 */
public final class GtinValidator {

	private GtinValidator() {
	}

	/**
	 * Returns whether the input is a valid GTIN-8, GTIN-12, GTIN-13, or GTIN-14.
	 */
	public static boolean isValid(String value) {
		return detectType(value).filter(type -> BarcodeValidator.isValidGs1(value, type.getLength())).isPresent();
	}

	/**
	 * Returns whether the input is a valid GTIN-8.
	 */
	public static boolean isValidGtin8(String value) {
		return BarcodeValidator.isValidGs1(value, GtinType.GTIN_8.getLength());
	}

	/**
	 * Returns whether the input is a valid GTIN-12.
	 */
	public static boolean isValidGtin12(String value) {
		return BarcodeValidator.isValidGs1(value, GtinType.GTIN_12.getLength());
	}

	/**
	 * Returns whether the input is a valid GTIN-13.
	 */
	public static boolean isValidGtin13(String value) {
		return BarcodeValidator.isValidGs1(value, GtinType.GTIN_13.getLength());
	}

	/**
	 * Returns whether the input is a valid GTIN-14.
	 */
	public static boolean isValidGtin14(String value) {
		return BarcodeValidator.isValidGs1(value, GtinType.GTIN_14.getLength());
	}

	/**
	 * Returns the GTIN representation identified by the input's ASCII-digit length. This
	 * method does not validate the check digit; use {@link #isValid(String)} when
	 * checksum validation is required.
	 */
	public static Optional<GtinType> detectType(String value) {
		if (value == null) {
			return Optional.empty();
		}
		return typeForLength(value.length()).filter(type -> BarcodeValidator.hasAsciiDigits(value, type.getLength()));
	}

	/**
	 * Calculates a GS1 modulo-10 check digit for a 7-, 11-, 12-, or 13-digit GTIN
	 * payload.
	 * @throws IllegalArgumentException if the payload has an unsupported length or does
	 * not contain only ASCII digits
	 */
	public static int calculateCheckDigit(String payload) {
		if (payload == null) {
			throw new IllegalArgumentException("GTIN payload must contain 7, 11, 12, or 13 ASCII digits");
		}
		return typeForLength(payload.length() + 1)
				.map(type -> BarcodeValidator.calculateGs1CheckDigit(payload, type.getLength() - 1)).orElseThrow(
						() -> new IllegalArgumentException("GTIN payload must contain 7, 11, 12, or 13 ASCII digits"));
	}

	/**
	 * Returns a valid GTIN right-justified and zero-padded to GS1's canonical
	 * fourteen-digit storage representation. Invalid input returns empty and is never
	 * normalized.
	 */
	public static Optional<String> normalizeToGtin14(String value) {
		return detectType(value).filter(type -> BarcodeValidator.isValidGs1(value, type.getLength()))
				.map(type -> "0".repeat(GtinType.GTIN_14.getLength() - type.getLength()) + value);
	}

	private static Optional<GtinType> typeForLength(int length) {
		return switch (length) {
			case 8 -> Optional.of(GtinType.GTIN_8);
			case 12 -> Optional.of(GtinType.GTIN_12);
			case 13 -> Optional.of(GtinType.GTIN_13);
			case 14 -> Optional.of(GtinType.GTIN_14);
			default -> Optional.empty();
		};
	}

}
