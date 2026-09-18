package id.com.flare.common.utilities.postal.philippines;

/**
 * Format validator for Philippine four-digit postal codes.
 *
 * <p>
 * Validation Level: FORMAT_ONLY. Accepted values contain exactly four ASCII digits.
 * Leading zeros are significant and accepted. No normalization is performed;
 * {@code null}, empty, whitespace, punctuation, and non-ASCII digits are rejected. A
 * valid result verifies neither postal-code existence nor address assignment.
 * </p>
 */
public final class PhilippinesPostalCodeValidator {

	private PhilippinesPostalCodeValidator() {
	}

	/**
	 * Returns whether the input has the Philippine four-digit postal-code format.
	 *
	 * @see <a href=
	 * "https://www.upu.int/UPU/media/upu/PostalEntitiesFiles/addressingUnit/phlEn.pdf">UPU
	 * Philippine postal-code reference</a>
	 */
	public static boolean isValid(String postalCode) {
		return postalCode != null && postalCode.length() == 4
				&& postalCode.chars().allMatch(PhilippinesPostalCodeValidator::isAsciiDigit);
	}

	private static boolean isAsciiDigit(int character) {
		return character >= '0' && character <= '9';
	}

}
