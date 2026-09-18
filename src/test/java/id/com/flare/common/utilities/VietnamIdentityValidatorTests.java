package id.com.flare.common.utilities;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import id.com.flare.common.utilities.identity.Gender;
import id.com.flare.common.utilities.identity.vietnam.VietnamCitizenIdentityValidator;
import id.com.flare.common.utilities.identity.vietnam.VietnamTaxIdValidator;

class VietnamIdentityValidatorTests {

	@Test
	void validatesVietnamCitizenIdentityAndExtractsDocumentedFields() {
		String syntheticMaleIdentity = "001200000001";
		String syntheticFemaleIdentity = "001399000001";

		assertThat(VietnamCitizenIdentityValidator.isValid(syntheticMaleIdentity)).isTrue();
		assertThat(VietnamCitizenIdentityValidator.getGender(syntheticMaleIdentity)).contains(Gender.MALE);
		assertThat(VietnamCitizenIdentityValidator.getBirthYear(syntheticMaleIdentity)).contains(2000);
		assertThat(VietnamCitizenIdentityValidator.getGender(syntheticFemaleIdentity)).contains(Gender.FEMALE);
		assertThat(VietnamCitizenIdentityValidator.getBirthYear(syntheticFemaleIdentity)).contains(2099);
	}

	@Test
	void rejectsInvalidVietnamCitizenIdentityValues() {
		assertThat(VietnamCitizenIdentityValidator.isValid(null)).isFalse();
		assertThat(VietnamCitizenIdentityValidator.isValid("")).isFalse();
		assertThat(VietnamCitizenIdentityValidator.isValid("00120000000")).isFalse();
		assertThat(VietnamCitizenIdentityValidator.isValid("0012000000012")).isFalse();
		assertThat(VietnamCitizenIdentityValidator.isValid("00120000000A")).isFalse();
		assertThat(VietnamCitizenIdentityValidator.isValid("\uFF11".repeat(12))).isFalse();
		assertThat(VietnamCitizenIdentityValidator.isValid(" 001200000001")).isFalse();
		assertThat(VietnamCitizenIdentityValidator.getBirthYear("invalid")).isEmpty();
	}

	@Test
	void normalizesVietnamTaxIdsWithoutChangingTheirMeaning() {
		assertThat(VietnamTaxIdValidator.normalize(" 1234567890 ")).contains("1234567890");
		assertThat(VietnamTaxIdValidator.normalize("1234567890-001")).contains("1234567890-001");
		assertThat(VietnamTaxIdValidator.isValid("1234567890")).isTrue();
		assertThat(VietnamTaxIdValidator.isValid("1234567890-001")).isTrue();
		assertThat(VietnamTaxIdValidator.isValid("1234567890-000")).isFalse();
		assertThat(VietnamTaxIdValidator.isValid("1234567890001")).isFalse();
		assertThat(VietnamTaxIdValidator.isValid("1234567890-01A")).isFalse();
		assertThat(VietnamTaxIdValidator.isValid("1234567890 -001")).isFalse();
		assertThat(VietnamTaxIdValidator.isValid("\uFF11".repeat(10))).isFalse();
		assertThat(VietnamTaxIdValidator.normalize(null)).isEmpty();
	}

}
