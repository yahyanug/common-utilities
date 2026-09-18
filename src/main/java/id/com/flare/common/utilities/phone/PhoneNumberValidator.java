package id.com.flare.common.utilities.phone;

import java.util.Optional;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import id.com.flare.common.utilities.country.AseanCountry;

/**
 * Validates and normalizes telephone numbers using Google's libphonenumber metadata.
 *
 * <p>
 * A supplied {@link AseanCountry} is both the default parsing region for national input
 * and the required region for the parsed number. Consequently, a number with an
 * international prefix (including {@code +}) must still belong to that country. This
 * class accepts the national and international representations supported by
 * libphonenumber. The {@code 00} international prefix is interpreted only where the
 * supplied region's metadata defines it; it is not treated as a universal prefix.
 * </p>
 *
 * <p>
 * Validation uses {@link PhoneNumberUtil#isValidNumberForRegion(PhoneNumber, String)},
 * rather than {@code isPossibleNumber}, because possible numbers may have a plausible
 * length without matching an assigned numbering-plan range. It also deliberately does not
 * use {@code isValidNumber} alone: that method would accept a valid number from a
 * different region. This class does not verify number assignment, ownership, SIM
 * existence, reachability, or an active subscription. Validation level: STRUCTURAL, using
 * the bundled libphonenumber numbering-plan metadata.
 * </p>
 */
public final class PhoneNumberValidator {

	private static final PhoneNumberUtil PHONE_NUMBER_UTIL = PhoneNumberUtil.getInstance();

	private PhoneNumberValidator() {
	}

	/**
	 * Returns whether {@code value} is a valid phone number for {@code country}.
	 *
	 * <p>
	 * National input is interpreted in the supplied country's region. International input
	 * is accepted when libphonenumber can parse it, including numbers that begin with
	 * {@code +}. Null, blank, malformed, invalid, and wrong-country values return
	 * {@code false}.
	 * </p>
	 */
	public static boolean isValid(String value, AseanCountry country) {
		return parseValid(value, country).isPresent();
	}

	/**
	 * Normalizes a valid phone number to its E.164 representation, such as
	 * {@code +6281234567890}.
	 *
	 * <p>
	 * The country supplies the default parsing region for national input and must match
	 * the parsed number's region. The result is empty for null, blank, malformed,
	 * invalid, or wrong-country input; invalid input is never converted into an
	 * E.164-looking value. E.164 represents the phone number only and does not include an
	 * extension.
	 * </p>
	 * @return the E.164 phone number, or empty when validation fails
	 */
	public static Optional<String> normalize(String value, AseanCountry country) {
		return parseValid(value, country)
				.map(number -> PHONE_NUMBER_UTIL.format(number, PhoneNumberUtil.PhoneNumberFormat.E164));
	}

	private static Optional<PhoneNumber> parseValid(String value, AseanCountry country) {
		if (value == null || value.isBlank() || country == null)
			return Optional.empty();
		try {
			PhoneNumber parsed = PHONE_NUMBER_UTIL.parse(value, country.getAlpha2());
			return PHONE_NUMBER_UTIL.isValidNumberForRegion(parsed, country.getAlpha2()) ? Optional.of(parsed)
					: Optional.empty();
		}
		catch (NumberParseException exception) {
			return Optional.empty();
		}
	}

}
