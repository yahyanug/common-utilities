package id.com.flare.common.utilities.money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

/**
 * Calculates a tax amount from a base amount and decimal rate.
 *
 * <p>
 * The rate must be between {@code 0} and {@code 1} inclusive. The result is rounded to
 * the supplied currency's default minor-unit scale using the caller-supplied rounding
 * mode. This class applies no jurisdictional tax rules, exemptions, thresholds,
 * inclusivity, or filing policy.
 * </p>
 */
public final class TaxCalculator {

	private TaxCalculator() {
	}

	/**
	 * Returns the rounded tax amount, or empty for invalid input or unsupported currency
	 * minor-unit data.
	 */
	public static Optional<BigDecimal> calculateTax(BigDecimal baseAmount, BigDecimal rate, String currencyCode,
			RoundingMode roundingMode) {
		return RateAmountCalculator.calculate(baseAmount, rate, currencyCode, roundingMode);
	}

}
