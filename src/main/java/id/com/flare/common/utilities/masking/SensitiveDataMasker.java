package id.com.flare.common.utilities.masking;

/**
 * Stateless functions for reducing accidental sensitive-data exposure in displays and
 * logs. Masking is not encryption or irreversible anonymization, and this class never
 * logs or retains supplied values.
 */
public final class SensitiveDataMasker {

	private SensitiveDataMasker() {
	}

	/**
	 * Masks a value with {@code *}, retaining the requested prefix and suffix only when
	 * they do not overlap.
	 *
	 * <p>
	 * Prefix and suffix lengths are Unicode code-point counts, so surrogate pairs are
	 * never split. If the requested visible portions overlap, or exactly cover the input,
	 * every code point is masked. Null returns null and an empty value returns empty.
	 * Negative lengths are invalid configuration and cause an
	 * {@link IllegalArgumentException} without including the input in its message.
	 * </p>
	 */
	public static String mask(String value, int visiblePrefix, int visibleSuffix) {
		if (value == null)
			return null;
		if (visiblePrefix < 0 || visibleSuffix < 0)
			throw new IllegalArgumentException("Visible lengths must not be negative");
		int codePointCount = value.codePointCount(0, value.length());
		if (visiblePrefix + visibleSuffix >= codePointCount)
			return "*".repeat(codePointCount);
		int prefixEnd = value.offsetByCodePoints(0, visiblePrefix);
		int suffixStart = value.offsetByCodePoints(0, codePointCount - visibleSuffix);
		return value.substring(0, prefixEnd) + "*".repeat(codePointCount - visiblePrefix - visibleSuffix)
				+ value.substring(suffixStart);
	}

	/**
	 * Masks an email local part while retaining its first code point and leaving the
	 * domain visible. A one-code-point local part and malformed values are fully masked;
	 * null returns null and empty returns empty. This method does not validate email
	 * syntax.
	 */
	public static String maskEmail(String email) {
		if (email == null)
			return null;
		int at = email.lastIndexOf('@');
		return at > 0 && at < email.length() - 1 && email.indexOf('@') == at
				? mask(email.substring(0, at), 1, 0) + email.substring(at) : mask(email, 0, 0);
	}

	/**
	 * Masks a supplied phone representation without validating it, retaining four leading
	 * and two trailing code points. Inputs of six code points or fewer are fully masked;
	 * null returns null and empty returns empty.
	 */
	public static String maskPhoneNumber(String phoneNumber) {
		return mask(phoneNumber, 4, 2);
	}

	/**
	 * Masks a supplied Indonesian NIK representation without validating it, retaining the
	 * first two and final four code points. Inputs of six code points or fewer are fully
	 * masked; null returns null and empty returns empty.
	 */
	public static String maskNik(String nik) {
		return mask(nik, 2, 4);
	}

	/**
	 * Masks a supplied Malaysian NRIC representation without validating or normalizing
	 * it, retaining only its final four code points. Formatted separators before those
	 * four code points are masked with the rest of the representation. Inputs of four
	 * code points or fewer are fully masked; null returns null and empty returns empty.
	 */
	public static String maskNric(String nric) {
		return mask(nric, 0, 4);
	}

	/**
	 * Masks a payment-card representation without validation, retaining at most its final
	 * four code points. Inputs of four code points or fewer are fully masked; null
	 * returns null and empty returns empty. It does not process payments, handle CVV
	 * values, or perform card validation.
	 */
	public static String maskCreditCard(String cardNumber) {
		return mask(cardNumber, 0, 4);
	}

	/**
	 * Masks a country-neutral bank-account representation without validation, retaining
	 * only its final four code points. Inputs of four code points or fewer are fully
	 * masked; null returns null and empty returns empty.
	 */
	public static String maskBankAccount(String accountNumber) {
		return mask(accountNumber, 0, 4);
	}

	/**
	 * Masks a token or secret without parsing it, retaining only its final four code
	 * points. Inputs of four code points or fewer are fully masked; null returns null and
	 * empty returns empty.
	 */
	public static String maskToken(String token) {
		return mask(token, 0, 4);
	}

}
