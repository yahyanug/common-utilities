package id.com.flare.common.utilities;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.zip.CRC32;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import id.com.flare.common.utilities.file.FileMimeTypeDetector;
import id.com.flare.common.utilities.file.FileNameSanitizer;
import id.com.flare.common.utilities.file.FileExtensionInspector;
import id.com.flare.common.utilities.file.FileSizeValidator;
import id.com.flare.common.utilities.file.ImageDimensionInspector;
import id.com.flare.common.utilities.file.ZipArchiveInspector;

class FileSecurityTests {

	@Test
	void detectsDoubleExtensionsOnHiddenFiles() {
		assertThat(FileExtensionInspector.extractExtension(".hidden.php.jpg")).contains("jpg");
		assertThat(FileExtensionInspector.hasMultipleExtensions(".hidden.php.jpg")).isTrue();
		assertThat(FileExtensionInspector.hasSuspiciousDoubleExtension(".hidden.php.jpg", Set.of("php"))).isTrue();
		assertThat(FileExtensionInspector.hasMultipleExtensions(".hidden.jpg")).isFalse();
	}

	@Test
	void differentiatesUnsupportedAndTruncatedImageHeaders() throws IOException {
		byte[] truncated = { (byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10 };
		assertThat(ImageDimensionInspector.inspect(truncated)).isEmpty();
		org.assertj.core.api.Assertions
				.assertThatThrownBy(() -> ImageDimensionInspector.inspect(new ByteArrayInputStream(truncated)))
				.isInstanceOf(IOException.class);
		assertThat(ImageDimensionInspector.inspect(new ByteArrayInputStream(new byte[] { 1, 2, 3 }))).isEmpty();
	}

	@Test
	void rejectsPortableArchivePathAliasesInsteadOfSanitizingThem() {
		for (String path : new String[] { "dir/file:stream", "CON.txt", "dir/.. /escape.txt", "dir//", "dir/a?b" }) {
			assertThat(ZipArchiveInspector.isSafeEntryPath(path)).as(path).isFalse();
			assertThat(ZipArchiveInspector.resolveEntryInsideBaseDirectory(Path.of("upload"), path)).isEmpty();
		}
		assertThat(ZipArchiveInspector.isSafeEntryPath("dir/")).isTrue();
	}

	@Test
	void rejectsTruncatedZipSignaturesAndReleasesButDoesNotCloseInput() throws IOException {
		for (byte[] bytes : new byte[][] { {}, { 'P', 'K', 3, 4 }, { 'P', 'K', 5, 6 }, { 'P', 'K', 7, 8 } })
			assertThat(ZipArchiveInspector.hasAcceptableEntries(new ByteArrayInputStream(bytes),
					new ZipArchiveInspector.ZipArchiveLimits(1, 10))).isFalse();
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try (ZipOutputStream zip = new ZipOutputStream(output)) {
		}
		TrackingInputStream input = new TrackingInputStream(output.toByteArray());
		assertThat(ZipArchiveInspector.hasAcceptableEntries(input, new ZipArchiveInspector.ZipArchiveLimits(0, 0)))
				.isTrue();
		assertThat(input.closed).isFalse();
	}

	@Test
	void rejectsExpansionDuringDecompressionBeforeReadingTheWholeEntry() throws IOException {
		byte[] compressed = zipOf("large.txt", new byte[2_000_000]);
		TrackingInputStream input = new TrackingInputStream(compressed);
		assertThat(ZipArchiveInspector.hasAcceptableEntries(input, new ZipArchiveInspector.ZipArchiveLimits(1, 2)))
				.isFalse();
		assertThat(input.available()).isGreaterThan(compressed.length / 2);
		assertThat(input.closed).isFalse();
	}

	@Test
	void readsOnlyImageHeadersAndLeavesCallerStreamOpen() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ImageIO.write(new BufferedImage(2, 3, BufferedImage.TYPE_INT_RGB), "png", output);
		TrackingInputStream input = new TrackingInputStream(output.toByteArray());
		assertThat(ImageDimensionInspector.inspect(input)).contains(new ImageDimensionInspector.ImageDimensions(2, 3));
		assertThat(input.closed).isFalse();
		assertThat(ImageDimensionInspector.inspect((java.io.InputStream) null)).isEmpty();
	}

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

	@Test
	void validatesCallerDefinedUploadSizeAndInspectsExtensions() {
		assertThat(FileSizeValidator.isWithinMaximumSize(10, 10)).isTrue();
		assertThat(FileSizeValidator.isWithinMaximumSize(11, 10)).isFalse();
		assertThat(FileSizeValidator.isWithinMaximumSize(-1, 10)).isFalse();
		assertThat(FileSizeValidator.isWithinMaximumSize(0, -1)).isFalse();

		assertThat(FileExtensionInspector.extractExtension("receipt.PDF")).contains("pdf");
		assertThat(FileExtensionInspector.extractExtension(".profile")).isEmpty();
		assertThat(FileExtensionInspector.extractExtension("invoice.")).isEmpty();
		assertThat(FileExtensionInspector.extractExtension(null)).isEmpty();
		assertThat(FileExtensionInspector.normalizeExtension(" .JpG ")).contains("jpg");
		assertThat(FileExtensionInspector.normalizeExtension("tar.gz")).isEmpty();
		assertThat(FileExtensionInspector.normalizeExtension("")).isEmpty();
		assertThat(FileExtensionInspector.hasMultipleExtensions("invoice.pdf.exe")).isTrue();
		assertThat(FileExtensionInspector.hasMultipleExtensions("archive.tar.gz")).isTrue();
		assertThat(FileExtensionInspector.hasSuspiciousDoubleExtension("invoice.pdf.exe", Set.of("PDF"))).isTrue();
		assertThat(FileExtensionInspector.hasSuspiciousDoubleExtension("archive.tar.gz", Set.of("pdf"))).isFalse();
		assertThat(FileExtensionInspector.hasSuspiciousDoubleExtension(null, Set.of("pdf"))).isFalse();
	}

	@Test
	void inspectsImageHeadersWithoutDecodingTheRaster() throws IOException {
		BufferedImage image = new BufferedImage(4, 3, BufferedImage.TYPE_INT_RGB);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		assertThat(ImageIO.write(image, "png", output)).isTrue();

		ImageDimensionInspector.ImageDimensions dimensions = ImageDimensionInspector.inspect(output.toByteArray())
				.orElseThrow();
		assertThat(dimensions.width()).isEqualTo(4);
		assertThat(dimensions.height()).isEqualTo(3);
		assertThat(dimensions.pixelCount()).isEqualTo(12);
		assertThat(ImageDimensionInspector.isWithinMaximumDimensions(dimensions, 4, 3)).isTrue();
		assertThat(ImageDimensionInspector.isWithinMaximumDimensions(dimensions, 3, 3)).isFalse();
		assertThat(ImageDimensionInspector.isWithinMaximumPixelCount(dimensions, 12)).isTrue();
		assertThat(ImageDimensionInspector.isWithinMaximumPixelCount(dimensions, 11)).isFalse();
		assertThat(ImageDimensionInspector.inspect(new byte[] { (byte) 0x89, 'P', 'N', 'G' })).isEmpty();
		assertThat(ImageDimensionInspector.inspect(new byte[0])).isEmpty();
		assertThat(ImageDimensionInspector.inspect((byte[]) null)).isEmpty();
		assertThat(ImageDimensionInspector.isWithinMaximumDimensions(null, 1, 1)).isFalse();
		assertThat(ImageDimensionInspector.isWithinMaximumPixelCount(null, 1)).isFalse();
	}

	@Test
	void validatesZipEntryPathsAndCallerDefinedLimitsWithoutExtracting() throws IOException {
		byte[] safeZip = zipOf("images/logo.png", new byte[20]);
		assertThat(ZipArchiveInspector.isArchive(safeZip)).isTrue();
		assertThat(ZipArchiveInspector.isArchive(null)).isFalse();
		assertThat(ZipArchiveInspector.isSafeEntryPath("images/logo.png")).isTrue();
		assertThat(ZipArchiveInspector.isSafeEntryPath("../escape.txt")).isFalse();
		assertThat(ZipArchiveInspector.isSafeEntryPath("..\\escape.txt")).isFalse();
		assertThat(ZipArchiveInspector.isSafeEntryPath("/absolute.txt")).isFalse();
		assertThat(ZipArchiveInspector.isSafeEntryPath("C:/absolute.txt")).isFalse();
		assertThat(ZipArchiveInspector.isSafeEntryPath(null)).isFalse();
		assertThat(ZipArchiveInspector.resolveEntryInsideBaseDirectory(Path.of("safe-upload-root"), "images/logo.png"))
				.hasValueSatisfying(
						path -> assertThat(path.startsWith(Path.of("safe-upload-root").toAbsolutePath().normalize()))
								.isTrue());
		assertThat(ZipArchiveInspector.resolveEntryInsideBaseDirectory(Path.of("safe-upload-root"), "../escape.txt"))
				.isEmpty();

		assertThat(ZipArchiveInspector.hasAcceptableEntries(new ByteArrayInputStream(safeZip),
				new ZipArchiveInspector.ZipArchiveLimits(1, 100))).isTrue();
		assertThat(ZipArchiveInspector.hasAcceptableEntries(new ByteArrayInputStream(safeZip),
				new ZipArchiveInspector.ZipArchiveLimits(0, 100))).isFalse();
		assertThat(ZipArchiveInspector.hasAcceptableEntries(new ByteArrayInputStream(safeZip),
				new ZipArchiveInspector.ZipArchiveLimits(1, 0))).isFalse();
		byte[] storedZip = storedZipOf("images/logo.png", new byte[] { 1, 2, 3 });
		assertThat(ZipArchiveInspector.hasAcceptableEntries(new ByteArrayInputStream(storedZip),
				new ZipArchiveInspector.ZipArchiveLimits(1, 1))).isTrue();
		assertThat(ZipArchiveInspector.hasAcceptableEntries(new ByteArrayInputStream(storedZip),
				new ZipArchiveInspector.ZipArchiveLimits(1, 0.99))).isFalse();
		assertThat(
				ZipArchiveInspector.hasAcceptableEntries(new ByteArrayInputStream(zipOf("../escape.txt", new byte[1])),
						new ZipArchiveInspector.ZipArchiveLimits(1, 100))).isFalse();
		assertThat(
				ZipArchiveInspector.hasAcceptableEntries(new ByteArrayInputStream(zipOf("..\\escape.txt", new byte[1])),
						new ZipArchiveInspector.ZipArchiveLimits(1, 100))).isFalse();
		assertThat(
				ZipArchiveInspector.hasAcceptableEntries(new ByteArrayInputStream(zipOf("/absolute.txt", new byte[1])),
						new ZipArchiveInspector.ZipArchiveLimits(1, 100))).isFalse();
		assertThat(ZipArchiveInspector.hasAcceptableEntries(null, new ZipArchiveInspector.ZipArchiveLimits(1, 1)))
				.isFalse();
		assertThat(ZipArchiveInspector.hasAcceptableEntries(new ByteArrayInputStream(new byte[] { 1, 2, 3 }),
				new ZipArchiveInspector.ZipArchiveLimits(1, 1))).isFalse();
	}

	private static byte[] zipOf(String entryName, byte[] contents) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try (ZipOutputStream zip = new ZipOutputStream(output)) {
			zip.putNextEntry(new ZipEntry(entryName));
			zip.write(contents);
			zip.closeEntry();
		}
		return output.toByteArray();
	}

	private static byte[] storedZipOf(String entryName, byte[] contents) throws IOException {
		CRC32 checksum = new CRC32();
		checksum.update(contents);
		ZipEntry entry = new ZipEntry(entryName);
		entry.setMethod(ZipEntry.STORED);
		entry.setSize(contents.length);
		entry.setCompressedSize(contents.length);
		entry.setCrc(checksum.getValue());
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try (ZipOutputStream zip = new ZipOutputStream(output)) {
			zip.putNextEntry(entry);
			zip.write(contents);
			zip.closeEntry();
		}
		return output.toByteArray();
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
