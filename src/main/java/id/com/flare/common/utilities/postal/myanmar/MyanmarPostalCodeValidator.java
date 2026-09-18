package id.com.flare.common.utilities.postal.myanmar;

/**
 * Format validator for Myanmar's seven-digit postal codes.
 *
 * <p>
 * Validation Level: FORMAT_ONLY. Accepted values contain exactly seven ASCII digits.
 * Leading zeros are significant and accepted. No normalization is performed;
 * {@code null}, empty, whitespace, punctuation, and non-ASCII digits are rejected. A
 * valid result verifies neither postal-code existence nor address assignment.
 * </p>
 */
public final class MyanmarPostalCodeValidator {

	private MyanmarPostalCodeValidator() {
	}

	/**
	 * Returns whether the input has Myanmar's documented seven-digit postal-code format.
	 *
	 * @see <a href=
	 * "https://www.upu.int/UPU/media/upu/PostalEntitiesFiles/addressingUnit/mmrEn.pdf">UPU
	 * Myanmar postal-code reference</a>
	 */
	public static boolean isValid(String postalCode) {
		return postalCode != null && postalCode.length() == 7
				&& postalCode.chars().allMatch(MyanmarPostalCodeValidator::isAsciiDigit);
	}

	private static boolean isAsciiDigit(int character) {
		return character >= '0' && character <= '9';
	}

}
