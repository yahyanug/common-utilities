package id.com.flare.common.utilities;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

import org.junit.jupiter.api.Test;

import id.com.flare.common.utilities.money.CurrencyValidator;
import id.com.flare.common.utilities.money.DiscountCalculator;
import id.com.flare.common.utilities.money.MoneyAmount;
import id.com.flare.common.utilities.money.PercentageValidator;
import id.com.flare.common.utilities.money.TaxCalculator;

class MoneyUtilitiesTests {

	@Test
	void validatesIsoCurrencyCodesAndLooksUpMinorUnitDigits() {
		assertThat(CurrencyValidator.isValid("USD")).isTrue();
		assertThat(CurrencyValidator.isValid("JPY")).isTrue();
		assertThat(CurrencyValidator.isValid("BHD")).isTrue();
		assertThat(CurrencyValidator.getDefaultFractionDigits("USD")).hasValue(2);
		assertThat(CurrencyValidator.getDefaultFractionDigits("JPY")).hasValue(0);
		assertThat(CurrencyValidator.getDefaultFractionDigits("BHD")).hasValue(3);

		assertThat(CurrencyValidator.isValid("usd")).isFalse();
		assertThat(CurrencyValidator.isValid(" USD")).isFalse();
		assertThat(CurrencyValidator.isValid("ZZZ")).isFalse();
		assertThat(CurrencyValidator.isValid(null)).isFalse();
		assertThat(CurrencyValidator.getDefaultFractionDigits("ZZZ")).isEmpty();
	}

	@Test
	void convertsMajorAndMinorUnitsWithoutLossOrTruncation() {
		assertThat(MoneyAmount.toMinorUnits(new BigDecimal("12.34"), "USD")).contains(BigInteger.valueOf(1234));
		assertThat(MoneyAmount.toMajorUnits(BigInteger.valueOf(1234), "USD")).contains(new BigDecimal("12.34"));
		assertThat(MoneyAmount.toMinorUnits(new BigDecimal("-12.34"), "USD")).contains(BigInteger.valueOf(-1234));
		assertThat(MoneyAmount.toMajorUnits(BigInteger.valueOf(-1234), "USD")).contains(new BigDecimal("-12.34"));
		assertThat(MoneyAmount.toMinorUnits(new BigDecimal("100"), "JPY")).contains(BigInteger.valueOf(100));
		assertThat(MoneyAmount.toMajorUnits(BigInteger.valueOf(100), "JPY")).contains(new BigDecimal("100"));
		assertThat(MoneyAmount.toMinorUnits(new BigDecimal("1.234"), "BHD")).contains(BigInteger.valueOf(1234));

		assertThat(MoneyAmount.toMinorUnits(new BigDecimal("12.345"), "USD")).isEmpty();
		assertThat(MoneyAmount.toMinorUnits(new BigDecimal("1.01"), "JPY")).isEmpty();
		assertThat(MoneyAmount.toMinorUnits(new BigDecimal("1.230"), "USD")).isEmpty();
		assertThat(MoneyAmount.toMinorUnits(null, "USD")).isEmpty();
		assertThat(MoneyAmount.toMinorUnits(BigDecimal.ONE, "ZZZ")).isEmpty();
		assertThat(MoneyAmount.toMajorUnits(null, "USD")).isEmpty();
		assertThat(MoneyAmount.toMajorUnits(BigInteger.ONE, "XXX")).isEmpty();
	}

	@Test
	void validatesCurrencyScaleAndRoundsOnlyWithCallerSpecifiedMode() {
		assertThat(MoneyAmount.hasValidScale(new BigDecimal("1.23"), "USD")).isTrue();
		assertThat(MoneyAmount.hasValidScale(new BigDecimal("1.230"), "USD")).isFalse();
		assertThat(MoneyAmount.hasValidScale(new BigDecimal("1.234"), "USD")).isFalse();
		assertThat(MoneyAmount.hasValidScale(new BigDecimal("100"), "JPY")).isTrue();
		assertThat(MoneyAmount.hasValidScale(new BigDecimal("100.0"), "JPY")).isFalse();
		assertThat(MoneyAmount.hasValidScale(new BigDecimal("1E+3"), "JPY")).isTrue();
		assertThat(MoneyAmount.hasValidScale(null, "USD")).isFalse();

		assertThat(MoneyAmount.round(new BigDecimal("1.235"), "USD", RoundingMode.HALF_UP))
				.contains(new BigDecimal("1.24"));
		assertThat(MoneyAmount.round(new BigDecimal("1.225"), "USD", RoundingMode.HALF_EVEN))
				.contains(new BigDecimal("1.22"));
		assertThat(MoneyAmount.round(new BigDecimal("1.5"), "JPY", RoundingMode.HALF_UP)).contains(new BigDecimal("2"));
		assertThat(MoneyAmount.round(new BigDecimal("1.2345"), "BHD", RoundingMode.DOWN))
				.contains(new BigDecimal("1.234"));
		assertThat(MoneyAmount.round(new BigDecimal("1.235"), "USD", RoundingMode.UNNECESSARY)).isEmpty();
		assertThat(MoneyAmount.round(null, "USD", RoundingMode.HALF_UP)).isEmpty();
		assertThat(MoneyAmount.round(BigDecimal.ONE, "USD", null)).isEmpty();
		assertThat(MoneyAmount.round(BigDecimal.ONE, "ZZZ", RoundingMode.HALF_UP)).isEmpty();
	}

	@Test
	void validatesPercentagesAndCalculatesDiscountAndTaxAmounts() {
		assertThat(PercentageValidator.isValidPercentage(BigDecimal.ZERO)).isTrue();
		assertThat(PercentageValidator.isValidPercentage(new BigDecimal("100"))).isTrue();
		assertThat(PercentageValidator.isValidPercentage(new BigDecimal("100.01"))).isFalse();
		assertThat(PercentageValidator.isValidPercentage(new BigDecimal("-0.01"))).isFalse();
		assertThat(PercentageValidator.isValidPercentage(null)).isFalse();
		assertThat(PercentageValidator.isValidRate(BigDecimal.ZERO)).isTrue();
		assertThat(PercentageValidator.isValidRate(BigDecimal.ONE)).isTrue();
		assertThat(PercentageValidator.isValidRate(new BigDecimal("1.001"))).isFalse();
		assertThat(PercentageValidator.isValidRate(new BigDecimal("-0.01"))).isFalse();
		assertThat(PercentageValidator.isValidRate(null)).isFalse();

		assertThat(DiscountCalculator.calculateDiscount(new BigDecimal("10.05"), new BigDecimal("0.10"), "USD",
				RoundingMode.HALF_UP)).contains(new BigDecimal("1.01"));
		assertThat(DiscountCalculator.calculateDiscount(new BigDecimal("10.05"), new BigDecimal("0.10"), "USD",
				RoundingMode.HALF_EVEN)).contains(new BigDecimal("1.00"));
		assertThat(TaxCalculator.calculateTax(new BigDecimal("100.00"), new BigDecimal("0.075"), "USD",
				RoundingMode.HALF_UP)).contains(new BigDecimal("7.50"));
		assertThat(TaxCalculator.calculateTax(new BigDecimal("-10.00"), new BigDecimal("0.10"), "USD",
				RoundingMode.HALF_UP)).contains(new BigDecimal("-1.00"));
		assertThat(TaxCalculator.calculateTax(BigDecimal.ZERO, new BigDecimal("0.10"), "JPY", RoundingMode.HALF_UP))
				.contains(BigDecimal.ZERO);

		assertThat(DiscountCalculator.calculateDiscount(BigDecimal.ONE, new BigDecimal("1.01"), "USD",
				RoundingMode.HALF_UP)).isEmpty();
		assertThat(TaxCalculator.calculateTax(BigDecimal.ONE, null, "USD", RoundingMode.HALF_UP)).isEmpty();
		assertThat(TaxCalculator.calculateTax(null, BigDecimal.ONE, "USD", RoundingMode.HALF_UP)).isEmpty();
		assertThat(TaxCalculator.calculateTax(BigDecimal.ONE, BigDecimal.ONE, "ZZZ", RoundingMode.HALF_UP)).isEmpty();
		assertThat(TaxCalculator.calculateTax(BigDecimal.ONE, BigDecimal.ONE, "USD", null)).isEmpty();
	}

}
