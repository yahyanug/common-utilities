package id.com.flare.common.utilities.barcode;

/**
 * Structural checksum validators and check-digit calculation for EAN-8, EAN-13, and UPC-A
 * barcodes.
 *
 * <p>
 * Validation requires the standard's exact ASCII-digit length and a correct GS1 modulo-10
 * check digit. Whitespace, separators, and non-ASCII numerals are rejected; input is
 * never normalized. Checksum validation does not establish GS1 registration, barcode
 * assignment, manufacturer validity, or product existence. UPC-E is not supported because
 * its compressed representation requires separate expansion rules outside this focused
 * utility's current scope. Validation level: STRUCTURAL_AND_CHECKSUM.
 * </p>
 */
public final class BarcodeValidator {

	private BarcodeValidator() {
	}

	/**
	 * Returns whether an eight-ASCII-digit EAN-8 has a correct GS1 modulo-10 check digit.
	 * This is structural and checksum validation only.
	 */
	public static boolean isValidEan8(String value) {
		return isValidGs1(value, 8);
	}

	/**
	 * Returns whether a thirteen-ASCII-digit EAN-13 has a correct GS1 modulo-10 check
	 * digit. This is structural and checksum validation only.
	 */
	public static boolean isValidEan13(String value) {
		return isValidGs1(value, 13);
	}

	/**
	 * Returns whether a twelve-ASCII-digit UPC-A has a correct GS1 modulo-10 check digit.
	 * This is structural and checksum validation only.
	 */
	public static boolean isValidUpcA(String value) {
		return isValidGs1(value, 12);
	}

	/**
	 * Calculates the EAN-8 check digit from its seven-ASCII-digit payload.
	 * @throws IllegalArgumentException if the payload does not contain exactly seven
	 * ASCII digits
	 */
	public static int calculateEan8CheckDigit(String payload) {
		return calculateGs1CheckDigit(payload, 7);
	}

	/**
	 * Calculates the EAN-13 check digit from its twelve-ASCII-digit payload.
	 * @throws IllegalArgumentException if the payload does not contain exactly twelve
	 * ASCII digits
	 */
	public static int calculateEan13CheckDigit(String payload) {
		return calculateGs1CheckDigit(payload, 12);
	}

	/**
	 * Calculates the UPC-A check digit from its eleven-ASCII-digit payload.
	 * @throws IllegalArgumentException if the payload does not contain exactly eleven
	 * ASCII digits
	 */
	public static int calculateUpcACheckDigit(String payload) {
		return calculateGs1CheckDigit(payload, 11);
	}

	static boolean isValidGs1(String value, int length) {
		return hasAsciiDigits(value, length)
				&& calculate(value.substring(0, length - 1)) == value.charAt(length - 1) - '0';
	}

	static int calculateGs1CheckDigit(String payload, int length) {
		if (!hasAsciiDigits(payload, length))
			throw new IllegalArgumentException("Payload must contain exactly " + length + " ASCII digits");
		return calculate(payload);
	}

	static boolean hasAsciiDigits(String value, int length) {
		return value != null && value.length() == length
				&& value.chars().allMatch(character -> character >= '0' && character <= '9');
	}

	private static int calculate(String payload) {
		int sum = 0;
		for (int index = payload.length() - 1, position = 0; index >= 0; index--, position++)
			sum += (payload.charAt(index) - '0') * (position % 2 == 0 ? 3 : 1);
		return (10 - sum % 10) % 10;
	}

}
