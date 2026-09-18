package id.com.flare.common.utilities.money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

final class RateAmountCalculator {

	private RateAmountCalculator() {
	}

	static Optional<BigDecimal> calculate(BigDecimal baseAmount, BigDecimal rate, String currencyCode,
			RoundingMode roundingMode) {
		if (baseAmount == null || !PercentageValidator.isValidRate(rate)) {
			return Optional.empty();
		}
		return MoneyAmount.round(baseAmount.multiply(rate), currencyCode, roundingMode);
	}

}
