package id.com.flare.common.utilities;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import id.com.flare.common.utilities.identity.brunei.BruneiIdentityCardType;
import id.com.flare.common.utilities.identity.brunei.BruneiIdentityCardValidator;
import id.com.flare.common.utilities.identity.philippines.PhilSysCardNumberValidator;
import id.com.flare.common.utilities.identity.singapore.SingaporeIdentityNumberValidator;
import id.com.flare.common.utilities.identity.thailand.ThailandNationalIdValidator;

class AdditionalSeaIdentityValidatorTests {

	@Test
	void normalizesBruneiIdentityCardsAndExtractsResidentCategory() {
		assertThat(BruneiIdentityCardValidator.normalize("01000001")).contains("01-000001");
		assertThat(BruneiIdentityCardValidator.normalize("31-000001")).contains("31-000001");
		assertThat(BruneiIdentityCardValidator.getType("01-000001")).contains(BruneiIdentityCardType.CITIZEN);
		assertThat(BruneiIdentityCardValidator.getType("31-000001"))
				.contains(BruneiIdentityCardType.PERMANENT_RESIDENT);
		assertThat(BruneiIdentityCardValidator.getType("51-000001"))
				.contains(BruneiIdentityCardType.TEMPORARY_RESIDENT);
		assertThat(BruneiIdentityCardValidator.isValid("00-000000")).isFalse();
		assertThat(BruneiIdentityCardValidator.isValid("00-080001")).isFalse();
		assertThat(BruneiIdentityCardValidator.isValid("30-080000")).isFalse();
		assertThat(BruneiIdentityCardValidator.isValid("30-080001")).isTrue();
		assertThat(BruneiIdentityCardValidator.isValid("50-130000")).isFalse();
		assertThat(BruneiIdentityCardValidator.isValid("50-130001")).isTrue();
		assertThat(BruneiIdentityCardValidator.isValid("01--000001")).isFalse();
		assertThat(BruneiIdentityCardValidator.isValid("\uFF10\uFF11\uFF10\uFF10\uFF10\uFF10\uFF10\uFF11")).isFalse();
		assertThat(BruneiIdentityCardValidator.normalize(null)).isEmpty();
	}

	@Test
	void validatesSingaporeNricAndFinStructureAndChecksum() {
		assertThat(SingaporeIdentityNumberValidator.normalize("s1234567d")).contains("S1234567D");
		assertThat(SingaporeIdentityNumberValidator.isValid("F1234567N")).isTrue();
		assertThat(SingaporeIdentityNumberValidator.isValid("G1234567X")).isTrue();
		assertThat(SingaporeIdentityNumberValidator.isValid("M1234567K")).isTrue();
		assertThat(SingaporeIdentityNumberValidator.isValid("G5872776N")).isTrue();
		assertThat(SingaporeIdentityNumberValidator.isValid("S1234567A")).isFalse();
		assertThat(SingaporeIdentityNumberValidator.isValid("X1A2B3C4D")).isFalse();
		assertThat(SingaporeIdentityNumberValidator.isValid("S1A2B3C4")).isFalse();
		assertThat(SingaporeIdentityNumberValidator.isValid("S1A2B3C4-")).isFalse();
		assertThat(SingaporeIdentityNumberValidator.isValid("S1A2B3C4D")).isFalse();
		assertThat(SingaporeIdentityNumberValidator.isValid("S\uFF11\uFF12\uFF13\uFF14\uFF15\uFF16\uFF17D")).isFalse();
		assertThat(SingaporeIdentityNumberValidator.isValid(" S1234567D")).isFalse();
		assertThat(SingaporeIdentityNumberValidator.normalize(null)).isEmpty();
	}

	@Test
	void validatesThaiNationalIdStructureAndChecksum() {
		assertThat(ThailandNationalIdValidator.isValid("1234567890121")).isTrue();
		assertThat(ThailandNationalIdValidator.isValid("0000000000000")).isFalse();
		assertThat(ThailandNationalIdValidator.isValid("9000000000004")).isFalse();
		assertThat(ThailandNationalIdValidator.isValid("1234567890123")).isFalse();
		assertThat(ThailandNationalIdValidator.isValid("123456789012")).isFalse();
		assertThat(ThailandNationalIdValidator.isValid("12345678901234")).isFalse();
		assertThat(ThailandNationalIdValidator.isValid("123456789012A")).isFalse();
		assertThat(ThailandNationalIdValidator
				.isValid("\uFF11\uFF12\uFF13\uFF14\uFF15\uFF16\uFF17\uFF18\uFF19\uFF10\uFF11\uFF12\uFF11")).isFalse();
		assertThat(ThailandNationalIdValidator.isValid(" 1234567890123")).isFalse();
		assertThat(ThailandNationalIdValidator.isValid(null)).isFalse();
	}

	@Test
	void validatesPublicPhilSysCardNumberFormatOnly() {
		assertThat(PhilSysCardNumberValidator.isValid("1234567890123456")).isTrue();
		assertThat(PhilSysCardNumberValidator.isValid("123456789012")).isFalse();
		assertThat(PhilSysCardNumberValidator.isValid("123456789012345")).isFalse();
		assertThat(PhilSysCardNumberValidator.isValid("123456789012345A")).isFalse();
		assertThat(PhilSysCardNumberValidator.isValid("\uFF11".repeat(16))).isFalse();
		assertThat(PhilSysCardNumberValidator.isValid("")).isFalse();
		assertThat(PhilSysCardNumberValidator.isValid(null)).isFalse();
	}

}
