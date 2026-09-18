package id.com.flare.common.utilities;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import id.com.flare.common.utilities.country.AseanCountry;
import id.com.flare.common.utilities.phone.PhoneNumberValidator;

class CountryAndPhoneNumberTests {

	private static final PhoneNumberUtil PHONE_NUMBER_UTIL = PhoneNumberUtil.getInstance();

	private static final Map<AseanCountry, Integer> EXPECTED_CALLING_CODES = Map.of(AseanCountry.INDONESIA, 62,
			AseanCountry.MALAYSIA, 60, AseanCountry.SINGAPORE, 65, AseanCountry.THAILAND, 66, AseanCountry.PHILIPPINES,
			63, AseanCountry.VIETNAM, 84, AseanCountry.BRUNEI, 673, AseanCountry.CAMBODIA, 855, AseanCountry.LAOS, 856,
			AseanCountry.MYANMAR, 95);

	@Test
	void findsSupportedCountryByEitherIsoCode() {
		assertThat(AseanCountry.getByAlpha2("id")).contains(AseanCountry.INDONESIA);
		assertThat(AseanCountry.getByAlpha3("MYS")).contains(AseanCountry.MALAYSIA);
		assertThat(AseanCountry.isSupported("sgp")).isTrue();
		assertThat(AseanCountry.isSupported("XX")).isFalse();
		assertThat(AseanCountry.getByAlpha2(null)).isEmpty();
	}

	@Test
	void rejectsInvalidAndUnsafePhoneNumberInput() {
		String validIndonesiaE164 = PHONE_NUMBER_UTIL.format(exampleMobileNumber(AseanCountry.INDONESIA),
				PhoneNumberUtil.PhoneNumberFormat.E164);

		assertThat(PhoneNumberValidator.isValid("not-a-phone-number", AseanCountry.INDONESIA)).isFalse();
		assertThat(PhoneNumberValidator.isValid("123", AseanCountry.INDONESIA)).isFalse();
		assertThat(PhoneNumberValidator.isValid(null, AseanCountry.INDONESIA)).isFalse();
		assertThat(PhoneNumberValidator.isValid("", AseanCountry.INDONESIA)).isFalse();
		assertThat(PhoneNumberValidator.isValid(" \t\n", AseanCountry.INDONESIA)).isFalse();
		assertThat(PhoneNumberValidator.isValid("123", null)).isFalse();
		assertThat(PhoneNumberValidator.isValid(validIndonesiaE164, null)).isFalse();
		assertThat(PhoneNumberValidator.normalize("", AseanCountry.INDONESIA)).isEmpty();
		assertThat(PhoneNumberValidator.normalize(null, AseanCountry.INDONESIA)).isEmpty();
		assertThat(PhoneNumberValidator.normalize(" \t\n", AseanCountry.INDONESIA)).isEmpty();
		assertThat(PhoneNumberValidator.normalize("not-a-phone-number", AseanCountry.INDONESIA)).isEmpty();
		assertThat(PhoneNumberValidator.normalize("123", null)).isEmpty();
		assertThat(PhoneNumberValidator.normalize(validIndonesiaE164, null)).isEmpty();
	}

	@ParameterizedTest
	@EnumSource(AseanCountry.class)
	void validatesAndNormalizesLibphonenumberExampleMobileNumbersForEverySupportedCountry(AseanCountry country) {
		PhoneNumber example = exampleMobileNumber(country);
		String national = PHONE_NUMBER_UTIL.format(example, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
		String e164 = PHONE_NUMBER_UTIL.format(example, PhoneNumberUtil.PhoneNumberFormat.E164);
		String international = PHONE_NUMBER_UTIL.format(example, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
		String rfc3966 = PHONE_NUMBER_UTIL.format(example, PhoneNumberUtil.PhoneNumberFormat.RFC3966);

		assertThat(PHONE_NUMBER_UTIL.getCountryCodeForRegion(country.getAlpha2()))
				.as("libphonenumber calling code for %s", country).isEqualTo(EXPECTED_CALLING_CODES.get(country));
		assertThat(PHONE_NUMBER_UTIL.isPossibleNumber(example)).isTrue();
		assertThat(PHONE_NUMBER_UTIL.isValidNumber(example)).isTrue();
		assertThat(PHONE_NUMBER_UTIL.isValidNumberForRegion(example, country.getAlpha2())).isTrue();
		assertThat(PhoneNumberValidator.isValid(national, country)).isTrue();
		assertThat(PhoneNumberValidator.normalize(national, country)).contains(e164);
		assertThat(PhoneNumberValidator.isValid(e164, country)).isTrue();
		assertThat(PhoneNumberValidator.normalize(e164, country)).contains(e164);
		assertThat(PhoneNumberValidator.isValid(international, country)).isTrue();
		assertThat(PhoneNumberValidator.normalize(international, country)).contains(e164);
		assertThat(PhoneNumberValidator.isValid(rfc3966, country)).isTrue();
		assertThat(PhoneNumberValidator.normalize(rfc3966, country)).contains(e164);
		assertThat(PhoneNumberValidator.isValid("1", country)).isFalse();
		assertThat(PhoneNumberValidator.normalize("1", country)).isEmpty();
	}

	@Test
	void acceptsRegionSpecificInternationalPrefixAndRequiresTheExplicitCountryToMatch() {
		PhoneNumber malaysiaExample = exampleMobileNumber(AseanCountry.MALAYSIA);
		String malaysiaE164 = PHONE_NUMBER_UTIL.format(malaysiaExample, PhoneNumberUtil.PhoneNumberFormat.E164);
		String malaysiaInternationalPrefix = "00" + malaysiaE164.substring(1);

		assertThat(PhoneNumberValidator.isValid(malaysiaInternationalPrefix, AseanCountry.MALAYSIA)).isTrue();
		assertThat(PhoneNumberValidator.normalize(malaysiaInternationalPrefix, AseanCountry.MALAYSIA))
				.contains(malaysiaE164);
		assertThat(PhoneNumberValidator.isValid(malaysiaE164, AseanCountry.INDONESIA)).isFalse();
		assertThat(PhoneNumberValidator.normalize(malaysiaE164, AseanCountry.INDONESIA)).isEmpty();
	}

	@Test
	void rejectsMalformedPrefixesAndHandlesUnicodeDigitsAsLibphonenumberDoes() {
		PhoneNumber indonesiaExample = exampleMobileNumber(AseanCountry.INDONESIA);
		String indonesiaE164 = PHONE_NUMBER_UTIL.format(indonesiaExample, PhoneNumberUtil.PhoneNumberFormat.E164);

		assertThat(PhoneNumberValidator.isValid("+", AseanCountry.INDONESIA)).isFalse();
		assertThat(PhoneNumberValidator.normalize("+", AseanCountry.INDONESIA)).isEmpty();
		assertThat(PhoneNumberValidator.isValid("00", AseanCountry.MALAYSIA)).isFalse();
		assertThat(PhoneNumberValidator.normalize("00", AseanCountry.MALAYSIA)).isEmpty();
		assertThat(PhoneNumberValidator.isValid(toArabicIndicDigits(indonesiaE164), AseanCountry.INDONESIA)).isTrue();
		assertThat(PhoneNumberValidator.normalize(toArabicIndicDigits(indonesiaE164), AseanCountry.INDONESIA))
				.contains(indonesiaE164);
	}

	@ParameterizedTest
	@EnumSource(AseanCountry.class)
	void rejectsValidNumbersFromTheWrongRegion(AseanCountry country) {
		AseanCountry foreignCountry = AseanCountry.values()[(country.ordinal() + 1) % AseanCountry.values().length];
		PhoneNumber foreignExample = exampleMobileNumber(foreignCountry);
		String foreignE164 = PHONE_NUMBER_UTIL.format(foreignExample, PhoneNumberUtil.PhoneNumberFormat.E164);

		assertThat(PHONE_NUMBER_UTIL.isValidNumber(foreignExample)).isTrue();
		assertThat(PHONE_NUMBER_UTIL.isValidNumberForRegion(foreignExample, country.getAlpha2())).isFalse();
		assertThat(PhoneNumberValidator.isValid(foreignE164, country)).isFalse();
		assertThat(PhoneNumberValidator.normalize(foreignE164, country)).isEmpty();
	}

	private PhoneNumber exampleMobileNumber(AseanCountry country) {
		PhoneNumber example = PHONE_NUMBER_UTIL.getExampleNumberForType(country.getAlpha2(),
				PhoneNumberUtil.PhoneNumberType.MOBILE);
		assertThat(example).as("libphonenumber mobile example for %s", country).isNotNull();
		return example;
	}

	private String toArabicIndicDigits(String value) {
		StringBuilder converted = new StringBuilder(value.length());
		for (char character : value.toCharArray()) {
			converted.append(Character.isDigit(character) ? (char) ('\u0660' + character - '0') : character);
		}
		return converted.toString();
	}

}
