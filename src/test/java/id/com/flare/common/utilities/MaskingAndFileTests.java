package id.com.flare.common.utilities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.junit.jupiter.api.Test;

import id.com.flare.common.utilities.file.FileMimeTypeDetector;
import id.com.flare.common.utilities.file.FileNameSanitizer;
import id.com.flare.common.utilities.masking.SensitiveDataMasker;

class MaskingAndFileTests {

	@Test
	void masksFullyWhenVisibleLengthSumWouldOverflow() {
		assertThat(SensitiveDataMasker.mask("synthetic", Integer.MAX_VALUE, 1)).isEqualTo("*********");
		assertThat(SensitiveDataMasker.mask("", Integer.MAX_VALUE, Integer.MAX_VALUE)).isEmpty();
	}

	@Test
	void masksGenericValuesWithoutLeakingOverlappingOrUnicodeValues() {
		assertThat(SensitiveDataMasker.mask("1234567890", 3, 2)).isEqualTo("123*****90");
		assertThat(SensitiveDataMasker.mask("123456", 2, 0)).isEqualTo("12****");
		assertThat(SensitiveDataMasker.mask("123456", 0, 2)).isEqualTo("****56");
		assertThat(SensitiveDataMasker.mask("123456", 0, 0)).isEqualTo("******");
		assertThat(SensitiveDataMasker.mask("abc", 2, 2)).isEqualTo("***");
		assertThat(SensitiveDataMasker.mask("x", 0, 0)).isEqualTo("*");
		assertThat(SensitiveDataMasker.mask("", 0, 0)).isEmpty();
		assertThat(SensitiveDataMasker.mask("A\uD83D\uDE00BC", 1, 1)).isEqualTo("A**C");
		assertThat(SensitiveDataMasker.mask("\uD83D\uDE00abc", 1, 1)).isEqualTo("\uD83D\uDE00**c");
		assertThat(SensitiveDataMasker.mask(null, 1, 1)).isNull();
		assertThatThrownBy(() -> SensitiveDataMasker.mask("x", -1, 0)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> SensitiveDataMasker.mask("x", 0, -1)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void appliesDomainSpecificMaskingPolicies() {
		assertThat(SensitiveDataMasker.maskEmail("alice@example.test")).isEqualTo("a****@example.test");
		assertThat(SensitiveDataMasker.maskEmail("ab@example.test")).isEqualTo("a*@example.test");
		assertThat(SensitiveDataMasker.maskEmail("a@example.test")).isEqualTo("*@example.test");
		assertThat(SensitiveDataMasker.maskEmail("invalid@@example.test")).isEqualTo("*********************");
		assertThat(SensitiveDataMasker.maskEmail("")).isEmpty();
		assertThat(SensitiveDataMasker.maskEmail(null)).isNull();

		assertThat(SensitiveDataMasker.maskPhoneNumber("081234567890")).isEqualTo("0812******90");
		assertThat(SensitiveDataMasker.maskPhoneNumber("+6281234567890")).isEqualTo("+628********90");
		assertThat(SensitiveDataMasker.maskPhoneNumber("123")).isEqualTo("***");
		assertThat(SensitiveDataMasker.maskPhoneNumber("")).isEmpty();
		assertThat(SensitiveDataMasker.maskPhoneNumber(null)).isNull();

		assertThat(SensitiveDataMasker.maskNik("1234560101000001")).isEqualTo("12**********0001");
		assertThat(SensitiveDataMasker.maskNik("123")).isEqualTo("***");
		assertThat(SensitiveDataMasker.maskNik("")).isEmpty();
		assertThat(SensitiveDataMasker.maskNik(null)).isNull();

		assertThat(SensitiveDataMasker.maskNric("000229101234")).isEqualTo("********1234");
		assertThat(SensitiveDataMasker.maskNric("000229-10-1234")).isEqualTo("**********1234");
		assertThat(SensitiveDataMasker.maskNric("123")).isEqualTo("***");
		assertThat(SensitiveDataMasker.maskNric("")).isEmpty();
		assertThat(SensitiveDataMasker.maskNric(null)).isNull();

		assertThat(SensitiveDataMasker.maskCreditCard("1234567890123456")).isEqualTo("************3456");
		assertThat(SensitiveDataMasker.maskCreditCard("12345678")).isEqualTo("****5678");
		assertThat(SensitiveDataMasker.maskCreditCard("123")).isEqualTo("***");
		assertThat(SensitiveDataMasker.maskCreditCard("")).isEmpty();
		assertThat(SensitiveDataMasker.maskCreditCard(null)).isNull();

		assertThat(SensitiveDataMasker.maskBankAccount("1234567890")).isEqualTo("******7890");
		assertThat(SensitiveDataMasker.maskBankAccount("123")).isEqualTo("***");
		assertThat(SensitiveDataMasker.maskBankAccount("")).isEmpty();
		assertThat(SensitiveDataMasker.maskBankAccount(null)).isNull();
		assertThat(SensitiveDataMasker.maskToken("synthetic-secret-token")).isEqualTo("******************oken");
		assertThat(SensitiveDataMasker.maskToken("abc")).isEqualTo("***");
		assertThat(SensitiveDataMasker.maskToken("")).isEmpty();
		assertThat(SensitiveDataMasker.maskToken(null)).isNull();
	}

	@Test
	void detectsContentAndSanitizesUntrustedFileNames() {
		byte[] pdf = "%PDF-1.7\n".getBytes(StandardCharsets.US_ASCII);
		assertThat(FileMimeTypeDetector.detectMimeType(pdf)).contains("application/pdf");
		assertThat(FileMimeTypeDetector.isAllowedMimeType(pdf, Set.of("application/pdf"))).isTrue();
		assertThat(FileMimeTypeDetector.isAllowedMimeType(pdf, Set.of("image/png"))).isFalse();
		assertThat(FileMimeTypeDetector.detectMimeType(new byte[0])).isEmpty();
		assertThat(FileMimeTypeDetector.detectMimeType(null)).isEmpty();
		assertThat(FileNameSanitizer.sanitizeFileName("../unsafe\\invoice.pdf")).contains("invoice.pdf");
		assertThat(FileNameSanitizer.sanitizeFileName("CON.txt")).contains("_CON.txt");
		assertThat(FileNameSanitizer.sanitizeFileName("report\u0000.pdf")).contains("report_.pdf");
		assertThat(FileNameSanitizer.sanitizeFileName(".. ")).isEmpty();
		assertThat(FileNameSanitizer.sanitizeFileName(null)).isEmpty();
	}

}
