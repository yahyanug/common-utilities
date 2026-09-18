package id.com.flare.common.utilities;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import id.com.flare.common.utilities.identity.Gender;
import id.com.flare.common.utilities.identity.indonesia.NikValidator;
import id.com.flare.common.utilities.identity.indonesia.NpwpValidator;
import id.com.flare.common.utilities.identity.malaysia.NricValidator;

class IdentityValidatorTests {

	private static final String SYNTHETIC_MALE_NIK = "1234560101000001";

	private static final String SYNTHETIC_FEMALE_NIK = "1234564101000001";

	@Test
	void validatesNikStructureAndExtractsReliableComponents() {
		assertThat(NikValidator.isValid(SYNTHETIC_MALE_NIK)).isTrue();
		assertThat(NikValidator.isValid(SYNTHETIC_FEMALE_NIK)).isTrue();
		assertThat(NikValidator.getGender(SYNTHETIC_MALE_NIK)).contains(Gender.MALE);
		assertThat(NikValidator.getGender(SYNTHETIC_FEMALE_NIK)).contains(Gender.FEMALE);
		assertThat(NikValidator.getProvinceCode(SYNTHETIC_MALE_NIK)).contains("12");
		assertThat(NikValidator.getCityCode(SYNTHETIC_MALE_NIK)).contains("34");
		assertThat(NikValidator.getDistrictCode(SYNTHETIC_MALE_NIK)).contains("56");
	}

	@Test
	void rejectsInvalidNikValues() {
		assertThat(NikValidator.isValid(null)).isFalse();
		assertThat(NikValidator.isValid("")).isFalse();
		assertThat(NikValidator.isValid("1234563201000001")).isFalse();
		assertThat(NikValidator.isValid("1234560100000001")).isFalse();
		assertThat(NikValidator.isValid("1234560101000000")).isFalse();
		assertThat(NikValidator.isValid("123456010100000")).isFalse();
		assertThat(NikValidator.isValid("123456010100000A")).isFalse();
		assertThat(NikValidator.isValid("\uFF11".repeat(16))).isFalse();
		assertThat(NikValidator.getGender("123")).isEmpty();
	}

	@Test
	void normalizesAndValidatesNricWithoutAssumingCenturyOrBirthplace() {
		assertThat(NricValidator.normalize(" 000229-10-1234 ")).contains("000229101234");
		assertThat(NricValidator.isValid("000229-10-1234")).isTrue();
		assertThat(NricValidator.isValid("000230101234")).isFalse();
		assertThat(NricValidator.normalize("000229--10-1234")).isEmpty();
		assertThat(NricValidator.normalize("0002291-01234")).isEmpty();
		assertThat(NricValidator.normalize("000229-10-123A")).isEmpty();
		assertThat(NricValidator.isValid("\uFF11".repeat(12))).isFalse();
		assertThat(NricValidator.normalize("")).isEmpty();
		assertThat(NricValidator.normalize(null)).isEmpty();
	}

	@Test
	void normalizesAndFormatsLegacyAndSixteenDigitNpwp() {
		assertThat(NpwpValidator.normalize("12.345.678.9-012.345")).contains("123456789012345");
		assertThat(NpwpValidator.format("123456789012345")).contains("12.345.678.9-012.345");
		assertThat(NpwpValidator.isValid("0123456789012345")).isTrue();
		assertThat(NpwpValidator.format("0123456789012345")).isEmpty();
		assertThat(NpwpValidator.isValid("12345678901234")).isFalse();
		assertThat(NpwpValidator.isValid("12345678901234A")).isFalse();
		assertThat(NpwpValidator.normalize("12.345.6789012.345")).isEmpty();
		assertThat(NpwpValidator.normalize("12 345 678 901 2345")).isEmpty();
		assertThat(NpwpValidator.isValid("\uFF11".repeat(15))).isFalse();
		assertThat(NpwpValidator.normalize(null)).isEmpty();
	}

}
