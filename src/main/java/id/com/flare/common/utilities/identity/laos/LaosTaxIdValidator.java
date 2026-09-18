package id.com.flare.common.utilities.identity.laos;

/**
 * Structural validator for Lao taxpayer identification numbers (TINs).
 *
 * <p>
 * The documented structure comprises an eight-digit taxpayer sequence, one
 * monitoring/check digit, a VAT-status digit ({@code 9} for VAT or {@code 0} for
 * non-VAT), and a two-digit branch number. It accepts the canonical twelve digits and the
 * documented display form with a hyphen after the monitoring digit. The published
 * material describes the monitoring digit's purpose but not a local calculation
 * algorithm, so this class validates the documented length and VAT-status position only.
 * It does not verify taxpayer registration or calculate the monitoring digit. Validation
 * level: STRUCTURAL.
 * </p>
 *
 * @see <a href="https://www.laotradeportal.gov.la/en-gb/site/display/1735">Lao PDR
 * taxpayer identification number decree</a>
 */
public final class LaosTaxIdValidator {

	private static final int TAX_ID_LENGTH = 12;

	private static final int VAT_STATUS_INDEX = 9;

	private LaosTaxIdValidator() {
	}

	/**
	 * Returns whether the input has the documented Lao TIN structural format.
	 *
	 * <p>
	 * This is structural validation only and does not validate the monitoring/check
	 * digit's calculation.
	 * </p>
	 */
	public static boolean isValid(String taxId) {
		String canonical = canonicalize(taxId);
		return canonical != null && canonical.length() == TAX_ID_LENGTH
				&& canonical.chars().allMatch(LaosTaxIdValidator::isAsciiDigit)
				&& (canonical.charAt(VAT_STATUS_INDEX) == '0' || canonical.charAt(VAT_STATUS_INDEX) == '9');
	}

	private static String canonicalize(String taxId) {
		if (taxId == null) {
			return null;
		}
		if (taxId.length() == TAX_ID_LENGTH + 1 && taxId.charAt(VAT_STATUS_INDEX) == '-') {
			return taxId.substring(0, VAT_STATUS_INDEX) + taxId.substring(VAT_STATUS_INDEX + 1);
		}
		return taxId;
	}

	private static boolean isAsciiDigit(int character) {
		return character >= '0' && character <= '9';
	}

}
