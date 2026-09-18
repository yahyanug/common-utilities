package id.com.flare.common.utilities;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Function;

import org.junit.jupiter.api.Test;

import id.com.flare.common.utilities.postal.brunei.BruneiPostalCodeValidator;
import id.com.flare.common.utilities.postal.cambodia.CambodiaPostalCodeValidator;
import id.com.flare.common.utilities.postal.indonesia.IndonesiaPostalCodeValidator;
import id.com.flare.common.utilities.postal.laos.LaosPostalCodeValidator;
import id.com.flare.common.utilities.postal.malaysia.MalaysiaPostalCodeValidator;
import id.com.flare.common.utilities.postal.myanmar.MyanmarPostalCodeValidator;
import id.com.flare.common.utilities.postal.philippines.PhilippinesPostalCodeValidator;
import id.com.flare.common.utilities.postal.singapore.SingaporePostalCodeValidator;
import id.com.flare.common.utilities.postal.thailand.ThailandPostalCodeValidator;
import id.com.flare.common.utilities.postal.vietnam.VietnamPostalCodeValidator;

class PostalCodeValidatorTests {

	@Test
	void validatesIndonesiaFiveDigitPostalCodeStructure() {
		assertNumericPostalCodeValidator(IndonesiaPostalCodeValidator::isValid, "40198", "01234");
	}

	@Test
	void validatesMalaysiaFiveDigitPostalCodeStructure() {
		assertNumericPostalCodeValidator(MalaysiaPostalCodeValidator::isValid, "43000", "01234");
	}

	@Test
	void validatesSingaporeSixDigitPostalCodeStructure() {
		assertNumericPostalCodeValidator(SingaporePostalCodeValidator::isValid, "408600", "012345");
	}

	@Test
	void validatesThailandFiveDigitPostalCodeStructure() {
		assertNumericPostalCodeValidator(ThailandPostalCodeValidator::isValid, "10501", "01234");
	}

	@Test
	void validatesPhilippineFourDigitPostalCodeStructure() {
		assertNumericPostalCodeValidator(PhilippinesPostalCodeValidator::isValid, "1000", "0123");
	}

	@Test
	void validatesVietnamFiveDigitPostalCodeStructure() {
		assertNumericPostalCodeValidator(VietnamPostalCodeValidator::isValid, "10000", "01234");
	}

	@Test
	void validatesLaoFiveDigitPostalCodeStructure() {
		assertNumericPostalCodeValidator(LaosPostalCodeValidator::isValid, "01160", "01234");
	}

	@Test
	void validatesCambodiaSixDigitPostalCodeStructure() {
		assertNumericPostalCodeValidator(CambodiaPostalCodeValidator::isValid, "120209", "012345");
	}

	@Test
	void validatesMyanmarSevenDigitPostalCodeStructure() {
		assertNumericPostalCodeValidator(MyanmarPostalCodeValidator::isValid, "0505001", "0123456");
	}

	@Test
	void validatesBruneiAlphanumericPostalCodeStructure() {
		assertThat(BruneiPostalCodeValidator.isValid("BT2328")).isTrue();
		assertThat(BruneiPostalCodeValidator.isValid("BA1511")).isTrue();
		assertThat(BruneiPostalCodeValidator.isValid("BT0001")).isTrue();
		assertInvalid(BruneiPostalCodeValidator.isValid("BT232"), BruneiPostalCodeValidator.isValid("BT23280"),
				BruneiPostalCodeValidator.isValid("B12328"), BruneiPostalCodeValidator.isValid("AT2328"),
				BruneiPostalCodeValidator.isValid("BT23-8"), BruneiPostalCodeValidator.isValid("BT232 "),
				BruneiPostalCodeValidator.isValid("bt2328"),
				BruneiPostalCodeValidator.isValid("BT\uFF12\uFF13\uFF12\uFF18"), BruneiPostalCodeValidator.isValid(""),
				BruneiPostalCodeValidator.isValid(null));
	}

	private void assertNumericPostalCodeValidator(Function<String, Boolean> validator, String documentedExample,
			String leadingZeroExample) {
		assertThat(validator.apply(documentedExample)).isTrue();
		assertThat(validator.apply(leadingZeroExample)).isTrue();

		int length = documentedExample.length();
		assertInvalid(validator.apply(documentedExample.substring(0, length - 1)),
				validator.apply(documentedExample + "0"),
				validator.apply(documentedExample.substring(0, length - 1) + "A"),
				validator.apply(documentedExample.substring(0, length - 1) + "-"),
				validator.apply(documentedExample.substring(0, length - 1) + " "),
				validator.apply("\uFF10".repeat(length)), validator.apply(""), validator.apply(null));
	}

	private void assertInvalid(boolean... results) {
		assertThat(results).containsOnly(false);
	}

}
