package id.com.flare.common.utilities;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Set;

import org.junit.jupiter.api.Test;

import id.com.flare.common.utilities.file.FileMimeTypeDetector;
import id.com.flare.common.utilities.file.FileNameSanitizer;

class FileSecurityTests {

	private static final byte[] PDF = "%PDF-1.7\n".getBytes(StandardCharsets.US_ASCII);

	private static final byte[] PNG = { (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A };

	private static final byte[] JPEG = { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10, 'J', 'F', 'I',
			'F', 0x00 };

	private static final byte[] WEBP = { 'R', 'I', 'F', 'F', 0x1A, 0x00, 0x00, 0x00, 'W', 'E', 'B', 'P', 'V', 'P', '8',
			' ' };

	@Test
	void detectsContentWithoutTrustingNamesAndDoesNotCloseStreams() throws IOException {
		assertThat(FileMimeTypeDetector.detectMimeType(PDF)).contains("application/pdf");
		assertThat(FileMimeTypeDetector.detectMimeType(PNG)).contains("image/png");
		assertThat(FileMimeTypeDetector.detectMimeType(JPEG)).contains("image/jpeg");
		assertThat(FileMimeTypeDetector.detectMimeType(WEBP)).contains("image/webp");
		assertThat(FileMimeTypeDetector.detectMimeType("plain text".getBytes(StandardCharsets.UTF_8)))
				.contains("text/plain");
		assertThat(FileMimeTypeDetector.detectMimeType(new byte[0])).isEmpty();
		assertThat(FileMimeTypeDetector.detectMimeType((byte[]) null)).isEmpty();
		assertThat(FileMimeTypeDetector.detectMimeType(new byte[] { 0, (byte) 0xFF, 0, (byte) 0x80 }))
				.contains("application/octet-stream");

		TrackingInputStream input = new TrackingInputStream(PDF);
		assertThat(FileMimeTypeDetector.detectMimeTypeFromStream(input)).contains("application/pdf");
		assertThat(input.closed).isFalse();
		input.close();
		assertThat(input.closed).isTrue();
		assertThat(FileMimeTypeDetector.detectMimeTypeFromStream((ByteArrayInputStream) null)).isEmpty();
	}

	@Test
	void providesAllowListSignatureAndExtensionChecks() {
		assertThat(FileMimeTypeDetector.isAllowedMimeType(PDF, Set.of("APPLICATION/PDF"))).isTrue();
		assertThat(FileMimeTypeDetector.isAllowedMimeType(PDF, Set.of("image/png"))).isFalse();
		assertThat(FileMimeTypeDetector.isAllowedMimeType(PDF, Set.of())).isFalse();
		assertThat(FileMimeTypeDetector.isAllowedMimeType(PDF, null)).isFalse();
		assertThat(FileMimeTypeDetector.isAllowedMimeType(null, Set.of("application/pdf"))).isFalse();
		assertThat(FileMimeTypeDetector.isPdf(PDF)).isTrue();
		assertThat(FileMimeTypeDetector.isPng(PNG)).isTrue();
		assertThat(FileMimeTypeDetector.isJpeg(JPEG)).isTrue();
		assertThat(FileMimeTypeDetector.isWebp(WEBP)).isTrue();
		assertThat(FileMimeTypeDetector.isPdf(PNG)).isFalse();

		assertThat(FileMimeTypeDetector.hasConsistentExtension(PDF, "invoice.PDF")).isTrue();
		assertThat(FileMimeTypeDetector.hasConsistentExtension(PDF, "invoice.jpg")).isFalse();
		assertThat(FileMimeTypeDetector.hasConsistentExtension(PNG, "invoice.pdf")).isFalse();
		assertThat(FileMimeTypeDetector.hasConsistentExtension(PDF, "invoice.unknown")).isFalse();
		assertThat(FileMimeTypeDetector.hasConsistentExtension(PDF, "invoice")).isFalse();
		assertThat(FileMimeTypeDetector.hasConsistentExtension(new byte[] { 0, (byte) 0xFF }, "file.bin")).isFalse();
	}

	@Test
	void sanitizesUntrustedFileNamesAndResolvesOnlyWithinTheBaseDirectory() {
		assertThat(FileNameSanitizer.sanitizeFileName("report.pdf")).contains("report.pdf");
		assertThat(FileNameSanitizer.sanitizeFileName("  report final .pdf  ")).contains("report final .pdf");
		assertThat(FileNameSanitizer.sanitizeFileName("archive.tar.gz")).contains("archive.tar.gz");
		assertThat(FileNameSanitizer.sanitizeFileName("../unsafe\\invoice.pdf")).contains("invoice.pdf");
		assertThat(FileNameSanitizer.sanitizeFileName("/var/private/invoice.pdf")).contains("invoice.pdf");
		assertThat(FileNameSanitizer.sanitizeFileName("C:\\private\\invoice.pdf")).contains("invoice.pdf");
		assertThat(FileNameSanitizer.sanitizeFileName("report\u0000.pdf")).contains("report_.pdf");
		assertThat(FileNameSanitizer.sanitizeFileName("report\u0001.pdf")).contains("report_.pdf");
		assertThat(FileNameSanitizer.sanitizeFileName(".")).isEmpty();
		assertThat(FileNameSanitizer.sanitizeFileName("..")).isEmpty();
		assertThat(FileNameSanitizer.sanitizeFileName("CON")).contains("_CON");
		assertThat(FileNameSanitizer.sanitizeFileName("PRN.txt")).contains("_PRN.txt");
		assertThat(FileNameSanitizer.sanitizeFileName("COM1 .txt")).contains("_COM1 .txt");
		assertThat(FileNameSanitizer.sanitizeFileName("LPT9.")).contains("_LPT9");
		assertThat(FileNameSanitizer.sanitizeFileName("a")).contains("a");
		assertThat(FileNameSanitizer.sanitizeFileName("r\u00E9sum\u00E9.pdf")).contains("r\u00E9sum\u00E9.pdf");
		assertThat(FileNameSanitizer.sanitizeFileName(null)).isEmpty();

		Path baseDirectory = Path.of("safe-upload-root");
		assertThat(FileNameSanitizer.resolveInsideBaseDirectory(baseDirectory, "../escape.txt"))
				.hasValueSatisfying(path -> {
					assertThat(path.getFileName()).hasToString("escape.txt");
					assertThat(path.startsWith(baseDirectory.toAbsolutePath().normalize())).isTrue();
				});
		assertThat(FileNameSanitizer.resolveInsideBaseDirectory(baseDirectory, "..")).isEmpty();
		assertThat(FileNameSanitizer.resolveInsideBaseDirectory(null, "invoice.pdf")).isEmpty();
	}

	private static final class TrackingInputStream extends ByteArrayInputStream {

		private boolean closed;

		private TrackingInputStream(byte[] content) {
			super(content);
		}

		@Override
		public void close() throws IOException {
			this.closed = true;
			super.close();
		}

	}

}
