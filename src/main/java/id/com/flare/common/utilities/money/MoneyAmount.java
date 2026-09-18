package id.com.flare.common.utilities.money;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Currency-aware amount conversion, scale validation, and explicit rounding.
 *
 * <p>
 * Major-unit and minor-unit conversions use {@link BigDecimal} and {@link BigInteger} to
 * preserve precision and sign without floating-point conversion. A major amount is
 * convertible only when its declared scale does not exceed the currency's default
 * minor-unit scale; excess fractional digits return empty rather than being truncated.
 * Rounding always requires a caller-supplied {@link RoundingMode}; this class has no
 * default financial rounding policy.
 * </p>
 */
public final class MoneyAmount {

	private MoneyAmount() {
	}

	/**
	 * Converts a major-unit amount to exact minor units. Invalid currency input,
	 * currencies without a defined minor-unit scale, null input, and excessive scale
	 * return empty.
	 */
	public static Optional<BigInteger> toMinorUnits(BigDecimal majorAmount, String currencyCode) {
		return minorUnitDigits(currencyCode).filter(scale -> majorAmount != null && majorAmount.scale() <= scale)
				.map(scale -> majorAmount.movePointRight(scale).toBigIntegerExact());
	}

	/**
	 * Converts an exact minor-unit amount to a major-unit {@link BigDecimal} at the
	 * currency's default minor-unit scale. Invalid currency input, currencies without a
	 * defined minor-unit scale, and null input return empty.
	 */
	public static Optional<BigDecimal> toMajorUnits(BigInteger minorAmount, String currencyCode) {
		return minorUnitDigits(currencyCode).filter(scale -> minorAmount != null)
				.map(scale -> new BigDecimal(minorAmount, scale));
	}

	/**
	 * Returns whether the amount's declared scale is at most the currency's default
	 * minor-unit scale. Null and currencies without a defined minor-unit scale return
	 * false.
	 */
	public static boolean hasValidScale(BigDecimal amount, String currencyCode) {
		return minorUnitDigits(currencyCode).filter(scale -> amount != null && amount.scale() <= scale).isPresent();
	}

	/**
	 * Rounds an amount to the currency's default minor-unit scale using the supplied
	 * rounding mode. Null input, null rounding mode, invalid currency input, and
	 * currencies without a defined minor-unit scale return empty. A result that cannot be
	 * represented with {@link RoundingMode#UNNECESSARY} also returns empty.
	 */
	public static Optional<BigDecimal> round(BigDecimal amount, String currencyCode, RoundingMode roundingMode) {
		Optional<Integer> scale = minorUnitDigits(currencyCode);
		if (amount == null || roundingMode == null || scale.isEmpty()) {
			return Optional.empty();
		}
		try {
			return Optional.of(amount.setScale(scale.get(), roundingMode));
		}
		catch (ArithmeticException exception) {
			return Optional.empty();
		}
	}

	static Optional<Integer> minorUnitDigits(String currencyCode) {
		OptionalInt fractionDigits = CurrencyValidator.getDefaultFractionDigits(currencyCode);
		return fractionDigits.isPresent() && fractionDigits.getAsInt() >= 0 ? Optional.of(fractionDigits.getAsInt())
				: Optional.empty();
	}

}
