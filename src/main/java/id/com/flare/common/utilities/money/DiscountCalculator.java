package id.com.flare.common.utilities.money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

/**
 * Calculates a discount amount from a base amount and decimal rate.
 *
 * <p>
 * The rate must be between {@code 0} and {@code 1} inclusive. The result is rounded to
 * the supplied currency's default minor-unit scale using the caller-supplied rounding
 * mode. This class applies no promotion eligibility, stacking, cap, or final-price
 * business rules.
 * </p>
 */
public final class DiscountCalculator {

	private DiscountCalculator() {
	}

	/**
	 * Returns the rounded discount amount, or empty for invalid input or unsupported
	 * currency minor-unit data.
	 */
	public static Optional<BigDecimal> calculateDiscount(BigDecimal baseAmount, BigDecimal rate, String currencyCode,
			RoundingMode roundingMode) {
		return RateAmountCalculator.calculate(baseAmount, rate, currencyCode, roundingMode);
	}

}
