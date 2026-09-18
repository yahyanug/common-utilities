package id.com.flare.common.utilities.postal.cambodia;

/**
 * Format validator for Cambodia's six-digit postal codes.
 *
 * <p>
 * Validation Level: FORMAT_ONLY. Accepted values contain exactly six ASCII digits.
 * Leading zeros are significant and accepted. No normalization is performed;
 * {@code null}, empty, whitespace, punctuation, and non-ASCII digits are rejected. A
 * valid result verifies neither postal-code existence nor address assignment.
 * </p>
 */
public final class CambodiaPostalCodeValidator {

	private CambodiaPostalCodeValidator() {
	}

	/**
	 * Returns whether the input has Cambodia's documented six-digit postal-code format.
	 *
	 * @see <a href=
	 * "https://www.upu.int/UPU/media/upu/PostalEntitiesFiles/addressingUnit/khmEn.pdf">UPU
	 * Cambodia postal-code reference</a>
	 */
	public static boolean isValid(String postalCode) {
		return postalCode != null && postalCode.length() == 6
				&& postalCode.chars().allMatch(CambodiaPostalCodeValidator::isAsciiDigit);
	}

	private static boolean isAsciiDigit(int character) {
		return character >= '0' && character <= '9';
	}

}
