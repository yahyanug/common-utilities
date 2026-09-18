package id.com.flare.common.utilities.identity.vietnam;

import java.util.Optional;

/**
 * Structural validator for Vietnamese tax identification numbers.
 *
 * <p>
 * A base tax ID has ten digits. A dependent-unit or business-location tax ID has ten
 * digits, a hyphen, and a branch sequence from {@code 001} through {@code 999}. This
 * class validates and normalizes only that published structure; it does not verify
 * taxpayer registration or calculate the base tax ID's check digit. Validation level:
 * STRUCTURAL.
 * </p>
 *
 * @see <a href=
 * "https://www.gdt.gov.vn/wps/portal/%21ut/p/z0/nY5NC4IwAIb_Sh08yqab2o5FXxNJMgrbRWbTuZItaUQ_v1mXzl1eeODh4QUMlIBp_lSSW2U07x2fWVxBTGg2Ox3yIkJrSI-7Lcn3WQCXGKSA_QqLFU2csCkKmmZBSIKxoK7DwOaAXYy2zcuCUgo7-YC2HnzwyrEHO6O07JQwynaGq7Ese1N_T_yV8GAIA-IW16hpW-4nmAvfAfFJhBqf1ChuRAhFksTgfmN11MvpGyqICm0%21/">Vietnam
 * tax ID reference</a>
 */
public final class VietnamTaxIdValidator {

	private static final int BASE_TAX_ID_LENGTH = 10;

	private static final int DEPENDENT_TAX_ID_LENGTH = 14;

	private VietnamTaxIdValidator() {
	}

	/**
	 * Returns whether the value matches a supported Vietnamese tax-ID structural format.
	 */
	public static boolean isValid(String taxId) {
		return normalize(taxId).isPresent();
	}

	/**
	 * Returns the canonical ten-digit or {@code 1234567890-123} representation.
	 * Surrounding whitespace is accepted; internal whitespace and any non-documented
	 * separator are rejected.
	 */
	public static Optional<String> normalize(String taxId) {
		if (taxId == null) {
			return Optional.empty();
		}
		String canonical = taxId.trim();
		if (isDigits(canonical, BASE_TAX_ID_LENGTH)) {
			return Optional.of(canonical);
		}
		if (canonical.length() == DEPENDENT_TAX_ID_LENGTH && canonical.charAt(BASE_TAX_ID_LENGTH) == '-'
				&& isDigits(canonical.substring(0, BASE_TAX_ID_LENGTH), BASE_TAX_ID_LENGTH)
				&& isDependentSequence(canonical.substring(BASE_TAX_ID_LENGTH + 1))) {
			return Optional.of(canonical);
		}
		return Optional.empty();
	}

	private static boolean isDigits(String value, int length) {
		return value.length() == length && value.chars().allMatch(VietnamTaxIdValidator::isAsciiDigit);
	}

	private static boolean isDependentSequence(String value) {
		return isDigits(value, 3) && !value.equals("000");
	}

	private static boolean isAsciiDigit(int character) {
		return character >= '0' && character <= '9';
	}

}
