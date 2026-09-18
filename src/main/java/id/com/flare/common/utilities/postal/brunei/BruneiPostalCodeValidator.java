package id.com.flare.common.utilities.postal.brunei;

/**
 * Format validator for Brunei Darussalam's six-character postal codes.
 *
 * <p>
 * Validation Level: FORMAT_ONLY. Accepted values have an uppercase district letter
 * ({@code B}, {@code K}, {@code P}, or {@code T}), an uppercase ASCII letter, and four
 * ASCII digits. A leading zero in the numeric suffix is significant and accepted. No
 * normalization is performed. {@code null}, empty, whitespace, punctuation, lowercase
 * letters, and non-ASCII digits are rejected. A valid result verifies neither postal-code
 * existence nor address assignment.
 * </p>
 */
public final class BruneiPostalCodeValidator {

	private BruneiPostalCodeValidator() {
	}

	/**
	 * Returns whether the input has Brunei Darussalam's documented postal-code format.
	 *
	 * @see <a href=
	 * "https://www.upu.int/UPU/media/upu/PostalEntitiesFiles/addressingUnit/brnEn.pdf">UPU
	 * Brunei Darussalam postal-code reference</a>
	 */
	public static boolean isValid(String postalCode) {
		return postalCode != null && postalCode.length() == 6 && isDistrictCode(postalCode.charAt(0))
				&& isUppercaseAsciiLetter(postalCode.charAt(1)) && isAsciiDigit(postalCode.charAt(2))
				&& isAsciiDigit(postalCode.charAt(3)) && isAsciiDigit(postalCode.charAt(4))
				&& isAsciiDigit(postalCode.charAt(5));
	}

	private static boolean isDistrictCode(char character) {
		return character == 'B' || character == 'K' || character == 'P' || character == 'T';
	}

	private static boolean isUppercaseAsciiLetter(char character) {
		return character >= 'A' && character <= 'Z';
	}

	private static boolean isAsciiDigit(char character) {
		return character >= '0' && character <= '9';
	}

}
