package id.com.flare.common.utilities;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import id.com.flare.common.utilities.identity.laos.LaosTaxIdValidator;

class LaosTaxIdValidatorTests {

	@Test
	void validatesDocumentedTinStructureForVatAndNonVatTaxpayers() {
		assertThat(LaosTaxIdValidator.isValid("123456789900")).isTrue();
		assertThat(LaosTaxIdValidator.isValid("123456789000")).isTrue();
		assertThat(LaosTaxIdValidator.isValid("123456789-900")).isTrue();
	}

	@Test
	void rejectsInvalidLaosTinValues() {
		assertThat(LaosTaxIdValidator.isValid(null)).isFalse();
		assertThat(LaosTaxIdValidator.isValid("")).isFalse();
		assertThat(LaosTaxIdValidator.isValid("12345678900")).isFalse();
		assertThat(LaosTaxIdValidator.isValid("1234567899000")).isFalse();
		assertThat(LaosTaxIdValidator.isValid("12345678-9900")).isFalse();
		assertThat(LaosTaxIdValidator.isValid("123456789800")).isFalse();
		assertThat(LaosTaxIdValidator.isValid("1234567899A0")).isFalse();
		assertThat(LaosTaxIdValidator.isValid("\uFF11".repeat(9) + "900")).isFalse();
		assertThat(LaosTaxIdValidator.isValid(" 123456789900")).isFalse();
	}

}
