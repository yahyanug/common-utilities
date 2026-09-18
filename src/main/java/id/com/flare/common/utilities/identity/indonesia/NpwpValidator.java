package id.com.flare.common.utilities.identity.indonesia;

import java.util.Optional;

/**
 * Format validation and normalization for Indonesian NPWP identifiers. DJP supports
 * legacy 15-digit NPWP values and 16-digit NPWP/NIK values. This class only checks
 * numeric length; it cannot verify registration, a NIK match, or taxpayer type. The
 * conventional display format is defined only for legacy 15-digit values. Validation
 * level: FORMAT_ONLY.
 *
 * @see <a href="https://pajak.go.id/en/node/107835">DJP NPWP format reference</a>
 */
public final class NpwpValidator {

	private NpwpValidator() {
	}

	/**
	 * Returns whether the input has a valid normalized NPWP numeric length (15 or 16
	 * digits).
	 */
	public static boolean isValid(String npwp) {
		return normalize(npwp).isPresent();
	}

	/**
	 * Removes conventional spaces, periods, and hyphens and returns a canonical 15- or
	 * 16-digit NPWP.
	 */
	public static Optional<String> normalize(String npwp) {
		if (npwp == null)
			return Optional.empty();
		String candidate = npwp.trim();
		if (isDigits(candidate, 15) || isDigits(candidate, 16)) {
			return Optional.of(candidate);
		}
		if (candidate.length() == 20 && candidate.charAt(2) == '.' && candidate.charAt(6) == '.'
				&& candidate.charAt(10) == '.' && candidate.charAt(12) == '-' && candidate.charAt(16) == '.'
				&& isDigits(candidate.substring(0, 2) + candidate.substring(3, 6) + candidate.substring(7, 10)
						+ candidate.charAt(11) + candidate.substring(13, 16) + candidate.substring(17), 15)) {
			return Optional.of(candidate.substring(0, 2) + candidate.substring(3, 6) + candidate.substring(7, 10)
					+ candidate.charAt(11) + candidate.substring(13, 16) + candidate.substring(17));
		}
		return Optional.empty();
	}

	/**
	 * Formats legacy NPWP as {@code 12.345.678.9-012.345}; 16-digit values return empty.
	 */
	public static Optional<String> format(String npwp) {
		return normalize(npwp).filter(value -> value.length() == 15)
				.map(value -> value.substring(0, 2) + "." + value.substring(2, 5) + "." + value.substring(5, 8) + "."
						+ value.charAt(8) + "-" + value.substring(9, 12) + "." + value.substring(12, 15));
	}

	private static boolean isDigits(String value, int length) {
		return value.length() == length && value.chars().allMatch(NpwpValidator::isAsciiDigit);
	}

	private static boolean isAsciiDigit(int character) {
		return character >= '0' && character <= '9';
	}

}
