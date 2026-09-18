package id.com.flare.common.utilities.identity.philippines;

/**
 * Format-level validator for the public 16-digit Philippine PhilSys Card Number (PCN).
 *
 * <p>
 * PSA identifies the PCN as the public, replaceable 16-digit token on a PhilID. This
 * validator intentionally does not accept the confidential 12-digit PhilSys Number (PSN),
 * and does not verify token issuance or card validity. Validation level: FORMAT_ONLY.
 * </p>
 *
 * @see <a href="https://psa.gov.ph/content/advisory-1">PSA PhilID advisory</a>
 */
public final class PhilSysCardNumberValidator {

	private static final int PCN_LENGTH = 16;

	private PhilSysCardNumberValidator() {
	}

	/** Returns whether the input has the public PhilSys Card Number's 16-digit format. */
	public static boolean isValid(String cardNumber) {
		return cardNumber != null && cardNumber.length() == PCN_LENGTH
				&& cardNumber.chars().allMatch(PhilSysCardNumberValidator::isAsciiDigit);
	}

	private static boolean isAsciiDigit(int character) {
		return character >= '0' && character <= '9';
	}

}
