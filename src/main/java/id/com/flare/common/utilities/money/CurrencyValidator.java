package id.com.flare.common.utilities.money;

import java.util.Currency;
import java.util.OptionalInt;

/**
 * ISO 4217 currency-code validation and default minor-unit lookup backed by
 * {@link java.util.Currency}.
 *
 * <p>
 * Codes must use Java's canonical upper-case representation; input is not trimmed or
 * normalized. A valid code does not imply that a payment provider, merchant, or country
 * accepts that currency. A result of {@code -1} for default fraction digits means Java
 * has no defined minor-unit scale for that currency, so it cannot be used by the money
 * amount operations in this package.
 * </p>
 */
public final class CurrencyValidator {

	private CurrencyValidator() {
	}

	/** Returns whether the input is a canonical ISO 4217 currency code known to Java. */
	public static boolean isValid(String currencyCode) {
		return currency(currencyCode) != null;
	}

	/**
	 * Returns Java's default number of minor-unit digits for a canonical ISO 4217
	 * currency code. Invalid input returns empty; a present value of {@code -1} has the
	 * meaning defined by {@link Currency#getDefaultFractionDigits()}.
	 */
	public static OptionalInt getDefaultFractionDigits(String currencyCode) {
		Currency currency = currency(currencyCode);
		return currency == null ? OptionalInt.empty() : OptionalInt.of(currency.getDefaultFractionDigits());
	}

	private static Currency currency(String currencyCode) {
		if (currencyCode == null) {
			return null;
		}
		try {
			Currency currency = Currency.getInstance(currencyCode);
			return currency.getCurrencyCode().equals(currencyCode) ? currency : null;
		}
		catch (IllegalArgumentException exception) {
			return null;
		}
	}

}
