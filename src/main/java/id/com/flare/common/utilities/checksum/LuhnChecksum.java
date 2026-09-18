package id.com.flare.common.utilities.checksum;

/**
 * Implementation of the Luhn (modulo-10) check-digit algorithm.
 *
 * <p>
 * This is a generic checksum utility. Validation accepts one or more ASCII decimal digits
 * only; spaces, hyphens, and every other non-digit character are rejected rather than
 * removed. {@link #isValid(String)} returns {@code false} for null or empty input. A
 * valid checksum does not establish that an identifier was issued, assigned, or is
 * otherwise usable. Validation level: CHECKSUM.
 * </p>
 */
public final class LuhnChecksum {

	private LuhnChecksum() {
	}

	/**
	 * Returns whether a complete ASCII-digit value has a valid Luhn check digit.
	 * @return {@code false} for null, empty, or non-ASCII-digit input
	 */
	public static boolean isValid(String value) {
		if (isAsciiDigits(value))
			return false;
		int sum = 0;
		boolean doubleDigit = false;
		for (int index = value.length() - 1; index >= 0; index--) {
			int digit = value.charAt(index) - '0';
			if (doubleDigit && (digit *= 2) > 9)
				digit -= 9;
			sum += digit;
			doubleDigit = !doubleDigit;
		}
		return sum % 10 == 0;
	}

	/**
	 * Calculates the Luhn check digit for a non-empty ASCII-digit payload.
	 * @throws IllegalArgumentException if the payload is null, empty, or contains a
	 * non-ASCII-digit character
	 */
	public static int calculateCheckDigit(String payload) {
		if (isAsciiDigits(payload))
			throw new IllegalArgumentException("Payload must contain one or more ASCII digits");
		int sum = 0;
		boolean doubleDigit = true;
		for (int index = payload.length() - 1; index >= 0; index--) {
			int digit = payload.charAt(index) - '0';
			if (doubleDigit && (digit *= 2) > 9)
				digit -= 9;
			sum += digit;
			doubleDigit = !doubleDigit;
		}
		return (10 - sum % 10) % 10;
	}

	private static boolean isAsciiDigits(String value) {
		return value == null || value.isEmpty()
				|| !value.chars().allMatch(character -> character >= '0' && character <= '9');
	}

}
