package id.com.flare.common.utilities.postal.thailand;

/**
 * Format validator for Thailand's five-digit postal codes.
 *
 * <p>
 * Validation Level: FORMAT_ONLY. Accepted values contain exactly five ASCII digits.
 * Leading zeros are significant and accepted. No normalization is performed;
 * {@code null}, empty, whitespace, punctuation, and non-ASCII digits are rejected. A
 * valid result verifies neither postal-code existence nor address assignment.
 * </p>
 */
public final class ThailandPostalCodeValidator {

	private ThailandPostalCodeValidator() {
	}

	/**
	 * Returns whether the input has Thailand's documented five-digit postal-code format.
	 *
	 * @see <a href=
	 * "https://www.upu.int/UPU/media/upu/PostalEntitiesFiles/addressingUnit/thaEn.pdf">UPU
	 * Thailand postal-code reference</a>
	 */
	public static boolean isValid(String postalCode) {
		return postalCode != null && postalCode.length() == 5
				&& postalCode.chars().allMatch(ThailandPostalCodeValidator::isAsciiDigit);
	}

	private static boolean isAsciiDigit(int character) {
		return character >= '0' && character <= '9';
	}

}
