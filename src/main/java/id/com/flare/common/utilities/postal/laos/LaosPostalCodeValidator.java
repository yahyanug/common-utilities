package id.com.flare.common.utilities.postal.laos;

/**
 * Format validator for Lao PDR five-digit postal codes.
 *
 * <p>
 * Validation Level: FORMAT_ONLY. Accepted values contain exactly five ASCII digits.
 * Leading zeros are significant and accepted. No normalization is performed;
 * {@code null}, empty, whitespace, punctuation, and non-ASCII digits are rejected. A
 * valid result verifies neither postal-code existence nor address assignment.
 * </p>
 */
public final class LaosPostalCodeValidator {

	private LaosPostalCodeValidator() {
	}

	/**
	 * Returns whether the input has Lao PDR's documented five-digit postal-code format.
	 *
	 * @see <a href=
	 * "https://www.upu.int/UPU/media/upu/PostalEntitiesFiles/addressingUnit/laoEn.pdf">UPU
	 * Lao PDR postal-code reference</a>
	 */
	public static boolean isValid(String postalCode) {
		return postalCode != null && postalCode.length() == 5
				&& postalCode.chars().allMatch(LaosPostalCodeValidator::isAsciiDigit);
	}

	private static boolean isAsciiDigit(int character) {
		return character >= '0' && character <= '9';
	}

}
