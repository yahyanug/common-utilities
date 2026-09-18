package id.com.flare.common.utilities.identity.malaysia;

import java.time.DateTimeException;
import java.time.MonthDay;
import java.util.Optional;

/**
 * Structural validator for Malaysian NRIC numbers. Malaysia's tax authority specifies the
 * twelve-digit display form {@code YYMMDD-PB-###G}. This validates digits and a possible
 * calendar month/day, but not birthplace codes or issuance by JPN. The two-digit year is
 * ambiguous, so no full birth-date extraction is provided. Validation level: STRUCTURAL.
 *
 * @see <a href=
 * "https://phl.hasil.gov.my/pdf/pdfam/MALAYSIA_TIN_NUMBER_AND_TIN_REGISTRATION_02122020.pdf">IRBM
 * NRIC reference</a>
 */
public final class NricValidator {

	private static final int NRIC_LENGTH = 12;

	private NricValidator() {
	}

	/** Returns whether input conforms to the implemented NRIC structural rules. */
	public static boolean isValid(String nric) {
		return normalize(nric).filter(NricValidator::hasPossibleBirthDay).isPresent();
	}

	/**
	 * Returns the canonical twelve-digit value, accepting display hyphens and surrounding
	 * whitespace.
	 */
	public static Optional<String> normalize(String nric) {
		if (nric == null)
			return Optional.empty();
		String candidate = nric.trim();
		String canonical = candidate.length() == NRIC_LENGTH ? candidate
				: candidate.length() == NRIC_LENGTH + 2 && candidate.charAt(6) == '-' && candidate.charAt(9) == '-'
						? candidate.substring(0, 6) + candidate.substring(7, 9) + candidate.substring(10) : "";
		return canonical.length() == NRIC_LENGTH && canonical.chars().allMatch(NricValidator::isAsciiDigit)
				? Optional.of(canonical) : Optional.empty();
	}

	private static boolean hasPossibleBirthDay(String nric) {
		try {
			MonthDay.of(Integer.parseInt(nric.substring(2, 4)), Integer.parseInt(nric.substring(4, 6)));
			return true;
		}
		catch (DateTimeException exception) {
			return false;
		}
	}

	private static boolean isAsciiDigit(int character) {
		return character >= '0' && character <= '9';
	}

}
