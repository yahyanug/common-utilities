package id.com.flare.common.utilities.money;

import java.math.BigDecimal;

/**
 * Validation for non-negative percentage values and decimal rates.
 *
 * <p>
 * A percentage is expressed from {@code 0} through {@code 100}; a rate is its decimal
 * equivalent from {@code 0} through {@code 1}. Both endpoints are accepted. This class
 * does not impose an application-specific scale or business limit beyond those ranges.
 * </p>
 */
public final class PercentageValidator {

	private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

	private PercentageValidator() {
	}

	/** Returns whether the input is a percentage from {@code 0} through {@code 100}. */
	public static boolean isValidPercentage(BigDecimal percentage) {
		return isWithinInclusiveRange(percentage, ONE_HUNDRED);
	}

	/** Returns whether the input is a decimal rate from {@code 0} through {@code 1}. */
	public static boolean isValidRate(BigDecimal rate) {
		return isWithinInclusiveRange(rate, BigDecimal.ONE);
	}

	private static boolean isWithinInclusiveRange(BigDecimal value, BigDecimal upperBound) {
		return value != null && value.signum() >= 0 && value.compareTo(upperBound) <= 0;
	}

}
