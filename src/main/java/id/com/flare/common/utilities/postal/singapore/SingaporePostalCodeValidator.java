package id.com.flare.common.utilities.postal.singapore;

/**
 * Format validator for Singapore's six-digit postal codes.
 *
 * <p>
 * Validation Level: FORMAT_ONLY. Accepted values contain exactly six ASCII digits.
 * Leading zeros are significant and accepted. No normalization is performed;
 * {@code null}, empty, whitespace, punctuation, and non-ASCII digits are rejected. A
 * valid result verifies neither postal-code existence nor address assignment.
 * </p>
 */
public final class SingaporePostalCodeValidator {

	private SingaporePostalCodeValidator() {
	}

	/**
	 * Returns whether the input has Singapore's documented six-digit postal-code format.
	 *
	 * @see <a href=
	 * "https://www.upu.int/UPU/media/upu/PostalEntitiesFiles/addressingUnit/sgpEn.pdf">UPU
	 * Singapore postal-code reference</a>
	 */
	public static boolean isValid(String postalCode) {
		return postalCode != null && postalCode.length() == 6
				&& postalCode.chars().allMatch(SingaporePostalCodeValidator::isAsciiDigit);
	}

	private static boolean isAsciiDigit(int character) {
		return character >= '0' && character <= '9';
	}

}
