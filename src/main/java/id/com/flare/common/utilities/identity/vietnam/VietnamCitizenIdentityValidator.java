package id.com.flare.common.utilities.identity.vietnam;

import java.util.Optional;
import id.com.flare.common.utilities.identity.Gender;

/**
 * Format-level validator for Vietnam's 12-digit citizen identity number.
 *
 * <p>
 * The first three digits identify the place or country of birth, the fourth encodes sex
 * and birth century, the next two encode the birth year, and the final six are a
 * sequence. This validator verifies only the documented numeric structure; it does not
 * maintain a potentially changing list of location codes or verify issuance by a
 * government authority. Validation level: FORMAT_ONLY.
 * </p>
 *
 * @see <a href=
 * "https://thanhphohaiphong.gov.vn/12-so-tren-the-can-cuoc-co-y-nghia-gi.html">Vietnam
 * citizen identity number reference</a>
 */
public final class VietnamCitizenIdentityValidator {

	private static final int IDENTITY_NUMBER_LENGTH = 12;

	private VietnamCitizenIdentityValidator() {
	}

	/** Returns whether the value has the documented twelve-digit structural format. */
	public static boolean isValid(String identityNumber) {
		return identityNumber != null && identityNumber.length() == IDENTITY_NUMBER_LENGTH
				&& identityNumber.chars().allMatch(VietnamCitizenIdentityValidator::isAsciiDigit);
	}

	/** Returns the gender encoded by a structurally valid citizen identity number. */
	public static Optional<Gender> getGender(String identityNumber) {
		if (!isValid(identityNumber)) {
			return Optional.empty();
		}
		return Optional.of((identityNumber.charAt(3) - '0') % 2 == 0 ? Gender.MALE : Gender.FEMALE);
	}

	/**
	 * Returns the four-digit birth year encoded by a structurally valid citizen identity
	 * number.
	 */
	public static Optional<Integer> getBirthYear(String identityNumber) {
		if (!isValid(identityNumber)) {
			return Optional.empty();
		}
		int genderAndCenturyCode = identityNumber.charAt(3) - '0';
		int centuryStart = 1900 + (genderAndCenturyCode / 2) * 100;
		return Optional.of(centuryStart + Integer.parseInt(identityNumber.substring(4, 6)));
	}

	private static boolean isAsciiDigit(int character) {
		return character >= '0' && character <= '9';
	}

}
