package id.com.flare.common.utilities.identity.singapore;

import java.util.Optional;

/**
 * Structural and checksum validator for Singapore NRIC and FIN numbers.
 *
 * <p>
 * An NRIC or FIN has one prefix letter, seven digits, and a final checksum letter. This
 * class applies the published weighted Mod-11 algorithm, including prefix-specific
 * offsets and checksum-letter tables, and normalizes letters to upper case. It does not
 * verify issuance or a holder's identity. Validation level: STRUCTURAL_AND_CHECKSUM.
 * </p>
 *
 * @see <a href=
 * "https://userapps.support.sap.com/sap/support/knowledge/en/2572734">NRIC/FIN checksum
 * algorithm reference</a>
 * @see <a href=
 * "https://www.ica.gov.sg/news-and-publications/newsroom/media-release/new-m-fin-series-to-be-introduced-from-1-january-2022">ICA
 * M FIN format reference</a>
 */
public final class SingaporeIdentityNumberValidator {

	private static final int IDENTITY_NUMBER_LENGTH = 9;

	private static final int[] WEIGHTS = { 2, 7, 6, 5, 4, 3, 2 };

	private static final String NRIC_CHECK_LETTERS = "JZIHGFEDCBA";

	private static final String FIN_CHECK_LETTERS = "XWUTRQPNMLK";

	private static final String M_FIN_CHECK_LETTERS = "XWUTRQPNJLK";

	private SingaporeIdentityNumberValidator() {
	}

	/**
	 * Returns whether the input has the documented NRIC or FIN structure and check
	 * letter.
	 */
	public static boolean isValid(String identityNumber) {
		return normalize(identityNumber).isPresent();
	}

	/**
	 * Returns the upper-case canonical identity number only when its check letter is
	 * valid.
	 */
	public static Optional<String> normalize(String identityNumber) {
		if (identityNumber == null || identityNumber.length() != IDENTITY_NUMBER_LENGTH) {
			return Optional.empty();
		}
		String canonical = identityNumber.toUpperCase(java.util.Locale.ROOT);
		char prefix = canonical.charAt(0);
		if (!isSupportedPrefix(prefix) || !hasDigitsAndCheckLetter(canonical) || !hasValidCheckLetter(canonical)) {
			return Optional.empty();
		}
		return Optional.of(canonical);
	}

	private static boolean isSupportedPrefix(char prefix) {
		return prefix == 'S' || prefix == 'T' || prefix == 'F' || prefix == 'G' || prefix == 'M';
	}

	private static boolean hasDigitsAndCheckLetter(String identityNumber) {
		return identityNumber.substring(1, 8).chars().allMatch(SingaporeIdentityNumberValidator::isAsciiDigit)
				&& identityNumber.charAt(8) >= 'A' && identityNumber.charAt(8) <= 'Z';
	}

	private static boolean isAsciiDigit(int character) {
		return character >= '0' && character <= '9';
	}

	private static boolean hasValidCheckLetter(String identityNumber) {
		int sum = identityNumber.charAt(0) == 'T' || identityNumber.charAt(0) == 'G' ? 4
				: identityNumber.charAt(0) == 'M' ? 3 : 0;
		for (int index = 0; index < WEIGHTS.length; index++) {
			sum += (identityNumber.charAt(index + 1) - '0') * WEIGHTS[index];
		}
		return checkLetters(identityNumber.charAt(0)).charAt(sum % 11) == identityNumber.charAt(8);
	}

	private static String checkLetters(char prefix) {
		if (prefix == 'S' || prefix == 'T') {
			return NRIC_CHECK_LETTERS;
		}
		return prefix == 'M' ? M_FIN_CHECK_LETTERS : FIN_CHECK_LETTERS;
	}

}
