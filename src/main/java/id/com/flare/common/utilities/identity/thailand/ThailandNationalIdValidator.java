package id.com.flare.common.utilities.identity.thailand;

/**
 * Structural and checksum validator for Thailand's thirteen-digit national ID number.
 *
 * <p>
 * Thailand's population registration authority documents a thirteen-digit number whose
 * first digit represents one of eight person categories and whose final digit is a check
 * digit. This class accepts categories {@code 1} through {@code 8} and applies the
 * documented weighted Mod-11 check-digit calculation to the first twelve digits. It does
 * not verify an ID's issuance or holder. Validation level: STRUCTURAL_AND_CHECKSUM.
 * </p>
 *
 * @see <a href=
 * "https://www.moe.go.th/%E0%B9%84%E0%B8%82%E0%B8%9B%E0%B8%A3%E0%B8%B4%E0%B8%A8%E0%B8%99%E0%B8%B2%E0%B9%80%E0%B8%A5%E0%B8%82%E0%B8%9A%E0%B8%B1%E0%B8%95%E0%B8%A3%E0%B8%9B%E0%B8%A3%E0%B8%B0%E0%B8%8A%E0%B8%B2%E0%B8%8A%E0%B8%99/">Thai
 * Ministry of Education national ID reference</a>
 */
public final class ThailandNationalIdValidator {

	private static final int NATIONAL_ID_LENGTH = 13;

	private ThailandNationalIdValidator() {
	}

	/**
	 * Returns whether the value has a valid Thai national-ID structure and check digit.
	 */
	public static boolean isValid(String nationalId) {
		if (nationalId == null || nationalId.length() != NATIONAL_ID_LENGTH
				|| !nationalId.chars().allMatch(ThailandNationalIdValidator::isAsciiDigit) || nationalId.charAt(0) < '1'
				|| nationalId.charAt(0) > '8') {
			return false;
		}
		int sum = 0;
		for (int index = 0; index < NATIONAL_ID_LENGTH - 1; index++) {
			sum += (nationalId.charAt(index) - '0') * (NATIONAL_ID_LENGTH - index);
		}
		int expectedCheckDigit = (11 - sum % 11) % 10;
		return expectedCheckDigit == nationalId.charAt(NATIONAL_ID_LENGTH - 1) - '0';
	}

	private static boolean isAsciiDigit(int character) {
		return character >= '0' && character <= '9';
	}

}
