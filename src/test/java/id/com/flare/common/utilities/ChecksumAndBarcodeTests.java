package id.com.flare.common.utilities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import id.com.flare.common.utilities.barcode.BarcodeValidator;
import id.com.flare.common.utilities.barcode.GtinType;
import id.com.flare.common.utilities.barcode.GtinValidator;
import id.com.flare.common.utilities.checksum.LuhnChecksum;

class ChecksumAndBarcodeTests {

	@Test
	void validatesAndCalculatesLuhn() {
		assertThat(LuhnChecksum.isValid("79927398713")).isTrue();
		assertThat(LuhnChecksum.isValid("0")).isTrue();
		assertThat(LuhnChecksum.isValid("1")).isFalse();
		assertThat(LuhnChecksum.calculateCheckDigit("7992739871")).isEqualTo(3);
		assertThat(LuhnChecksum.isValid("79927398714")).isFalse();
		assertThat(LuhnChecksum.isValid("7992-7398713")).isFalse();
		assertThat(LuhnChecksum.isValid("\uFF17\uFF19\uFF19\uFF12\uFF17\uFF13\uFF19\uFF18\uFF17\uFF11\uFF13"))
				.isFalse();
		assertThat(LuhnChecksum.isValid(null)).isFalse();
		assertThat(LuhnChecksum.isValid("")).isFalse();
		assertThat(LuhnChecksum.isValid(" ")).isFalse();
		assertThatThrownBy(() -> LuhnChecksum.calculateCheckDigit("12A")).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> LuhnChecksum.calculateCheckDigit("")).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> LuhnChecksum.calculateCheckDigit(null)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void validatesEan8ChecksumsAndPayloads() {
		assertThat(BarcodeValidator.isValidEan8("96385074")).isTrue();
		assertThat(BarcodeValidator.isValidEan8("00000000")).isTrue();
		assertThat(BarcodeValidator.calculateEan8CheckDigit("9638507")).isEqualTo(4);
		assertThat(BarcodeValidator.calculateEan8CheckDigit("0000000")).isZero();
		assertBarcodeInvalid(BarcodeValidator.isValidEan8("96385075"), BarcodeValidator.isValidEan8("9638507"),
				BarcodeValidator.isValidEan8("963850740"), BarcodeValidator.isValidEan8("9638507A"),
				BarcodeValidator.isValidEan8("\uFF19\uFF16\uFF13\uFF18\uFF15\uFF10\uFF17\uFF14"),
				BarcodeValidator.isValidEan8(null), BarcodeValidator.isValidEan8(""));
		assertThatThrownBy(() -> BarcodeValidator.calculateEan8CheckDigit("963850"))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> BarcodeValidator.calculateEan8CheckDigit("963850A"))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void validatesEan13ChecksumsAndPayloads() {
		assertThat(BarcodeValidator.isValidEan13("4006381333931")).isTrue();
		assertThat(BarcodeValidator.isValidEan13("0000000000000")).isTrue();
		assertThat(BarcodeValidator.calculateEan13CheckDigit("400638133393")).isEqualTo(1);
		assertThat(BarcodeValidator.calculateEan13CheckDigit("000000000000")).isZero();
		assertBarcodeInvalid(BarcodeValidator.isValidEan13("4006381333932"),
				BarcodeValidator.isValidEan13("400638133393"), BarcodeValidator.isValidEan13("40063813339310"),
				BarcodeValidator.isValidEan13("400638133393A"), BarcodeValidator.isValidEan13(null),
				BarcodeValidator.isValidEan13(""));
		assertThatThrownBy(() -> BarcodeValidator.calculateEan13CheckDigit("40063813339"))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> BarcodeValidator.calculateEan13CheckDigit("40063813339A"))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void validatesUpcAChecksumsAndPayloads() {
		assertThat(BarcodeValidator.isValidUpcA("036000291452")).isTrue();
		assertThat(BarcodeValidator.isValidUpcA("000000000000")).isTrue();
		assertThat(BarcodeValidator.calculateUpcACheckDigit("03600029145")).isEqualTo(2);
		assertThat(BarcodeValidator.calculateUpcACheckDigit("00000000000")).isZero();
		assertBarcodeInvalid(BarcodeValidator.isValidUpcA("036000291453"), BarcodeValidator.isValidUpcA("03600029145"),
				BarcodeValidator.isValidUpcA("0360002914520"), BarcodeValidator.isValidUpcA("03600029145A"),
				BarcodeValidator.isValidUpcA(null), BarcodeValidator.isValidUpcA(""));
		assertThatThrownBy(() -> BarcodeValidator.calculateUpcACheckDigit("0360002914"))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> BarcodeValidator.calculateUpcACheckDigit("0360002914A"))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void validatesGtinTypesAndReusesExistingEanAndUpcValidation() {
		assertThat(GtinValidator.isValidGtin8("96385074")).isTrue();
		assertThat(GtinValidator.isValidGtin12("036000291452")).isTrue();
		assertThat(GtinValidator.isValidGtin13("4006381333931")).isTrue();
		assertThat(GtinValidator.isValidGtin14("10012345678902")).isTrue();
		assertThat(GtinValidator.isValid("00000000")).isTrue();
		assertThat(GtinValidator.isValid("00000000000000")).isTrue();

		assertThat(GtinValidator.isValidGtin8("96385074")).isEqualTo(BarcodeValidator.isValidEan8("96385074"));
		assertThat(GtinValidator.isValidGtin12("036000291452")).isEqualTo(BarcodeValidator.isValidUpcA("036000291452"));
		assertThat(GtinValidator.isValidGtin13("4006381333931"))
				.isEqualTo(BarcodeValidator.isValidEan13("4006381333931"));
		assertBarcodeInvalid(GtinValidator.isValidGtin8("96385075"), GtinValidator.isValidGtin12("036000291453"),
				GtinValidator.isValidGtin13("4006381333932"), GtinValidator.isValidGtin14("10012345678903"));

		assertThat(GtinValidator.detectType("96385074")).contains(GtinType.GTIN_8);
		assertThat(GtinValidator.detectType("036000291452")).contains(GtinType.GTIN_12);
		assertThat(GtinValidator.detectType("4006381333931")).contains(GtinType.GTIN_13);
		assertThat(GtinValidator.detectType("10012345678902")).contains(GtinType.GTIN_14);
		assertThat(GtinValidator.detectType("10012345678903")).contains(GtinType.GTIN_14);
	}

	@Test
	void calculatesAndNormalizesGtinsWithoutAcceptingInvalidInput() {
		assertThat(GtinValidator.calculateCheckDigit("9638507")).isEqualTo(4);
		assertThat(GtinValidator.calculateCheckDigit("03600029145")).isEqualTo(2);
		assertThat(GtinValidator.calculateCheckDigit("400638133393")).isEqualTo(1);
		assertThat(GtinValidator.calculateCheckDigit("1001234567890")).isEqualTo(2);
		assertThat(GtinValidator.normalizeToGtin14("96385074")).contains("00000096385074");
		assertThat(GtinValidator.normalizeToGtin14("036000291452")).contains("00036000291452");
		assertThat(GtinValidator.normalizeToGtin14("4006381333931")).contains("04006381333931");
		assertThat(GtinValidator.normalizeToGtin14("10012345678902")).contains("10012345678902");

		assertBarcodeInvalid(GtinValidator.isValid("10012345678903"), GtinValidator.isValid("1001234567890"),
				GtinValidator.isValid("100123456789020"), GtinValidator.isValid("1001234567890A"),
				GtinValidator.isValid("1001234567890-"), GtinValidator.isValid("1001234567890 "),
				GtinValidator.isValid("\uFF11".repeat(14)), GtinValidator.isValid(null), GtinValidator.isValid(""));
		assertThat(GtinValidator.detectType("1001234567890A")).isEmpty();
		assertThat(GtinValidator.detectType(null)).isEmpty();
		assertThat(GtinValidator.normalizeToGtin14("10012345678903")).isEmpty();
		assertThat(GtinValidator.normalizeToGtin14(null)).isEmpty();
		assertThatThrownBy(() -> GtinValidator.calculateCheckDigit("100123"))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> GtinValidator.calculateCheckDigit("100123A"))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> GtinValidator.calculateCheckDigit(null)).isInstanceOf(IllegalArgumentException.class);
	}

	private void assertBarcodeInvalid(boolean... values) {
		assertThat(values).containsOnly(false);
	}

}
