package id.com.flare.common.utilities.file;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;

/**
 * Content-based MIME-type detection backed by Apache Tika. A detected MIME type reports
 * what content appears to be; it does not establish that a file is safe, non-malicious,
 * authorized, or suitable for storage.
 */
public final class FileMimeTypeDetector {

	private static final Tika TIKA = new Tika();

	private static final MimeTypes MIME_TYPES = MimeTypes.getDefaultMimeTypes();

	private FileMimeTypeDetector() {
	}

	/**
	 * Detects a MIME type from file content, without using a supplied filename or
	 * extension. Null or empty content has no result.
	 */
	public static Optional<String> detectMimeType(byte[] content) {
		return content == null || content.length == 0 ? Optional.empty() : Optional.of(TIKA.detect(content));
	}

	/**
	 * Detects a MIME type from a caller-owned stream. This method consumes content as
	 * needed for detection but never closes the stream; callers remain responsible for
	 * closing it. Null has no result.
	 * @throws IOException if reading the stream fails
	 */
	public static Optional<String> detectMimeTypeFromStream(InputStream content) throws IOException {
		return content == null ? Optional.empty() : Optional.of(TIKA.detect(content));
	}

	/**
	 * Returns whether content's detected MIME type is in the caller-provided allow-list,
	 * case-insensitively. Null or empty allow-lists, null content, and undetectable
	 * content return {@code false}; this method establishes no global safe-type policy.
	 */
	public static boolean isAllowedMimeType(byte[] content, Set<String> allowedMimeTypes) {
		if (allowedMimeTypes == null || allowedMimeTypes.isEmpty())
			return false;
		return detectMimeType(content)
				.map(type -> allowedMimeTypes.stream().filter(value -> value != null)
						.map(value -> value.toLowerCase(Locale.ROOT)).anyMatch(type.toLowerCase(Locale.ROOT)::equals))
				.orElse(false);
	}

	/** Returns whether content is detected as a PDF. This is not a safety guarantee. */
	public static boolean isPdf(byte[] content) {
		return hasMimeType(content, "application/pdf");
	}

	/**
	 * Returns whether content is detected as a PNG image. This is not a safety guarantee.
	 */
	public static boolean isPng(byte[] content) {
		return hasMimeType(content, "image/png");
	}

	/**
	 * Returns whether content is detected as a JPEG image. This is not a safety
	 * guarantee.
	 */
	public static boolean isJpeg(byte[] content) {
		return hasMimeType(content, "image/jpeg");
	}

	/**
	 * Returns whether content is detected as a WebP image. This is not a safety
	 * guarantee.
	 */
	public static boolean isWebp(byte[] content) {
		return hasMimeType(content, "image/webp");
	}

	/**
	 * Returns whether a filename extension is one of Apache Tika's known extensions for
	 * the detected MIME type. Filename paths are reduced to their final component for
	 * inspection only; callers must sanitize names separately. Unknown extensions, no
	 * extension, and generic {@code application/octet-stream} detections return
	 * {@code false}. This check detects some extension spoofing but is not malware
	 * protection.
	 */
	public static boolean hasConsistentExtension(byte[] content, String fileName) {
		return detectMimeType(content).filter(type -> !"application/octet-stream".equalsIgnoreCase(type))
				.flatMap(FileMimeTypeDetector::extensionsFor).flatMap(extensions -> FileExtensionInspector
						.extractExtension(fileName).map(extension -> "." + extension).filter(extensions::contains))
				.isPresent();
	}

	private static boolean hasMimeType(byte[] content, String expectedMimeType) {
		return detectMimeType(content).map(expectedMimeType::equalsIgnoreCase).orElse(false);
	}

	private static Optional<Set<String>> extensionsFor(String mimeType) {
		try {
			MimeType type = MIME_TYPES.forName(mimeType);
			return Optional.of(type.getExtensions().stream().map(extension -> extension.toLowerCase(Locale.ROOT))
					.collect(java.util.stream.Collectors.toUnmodifiableSet()));
		}
		catch (MimeTypeException exception) {
			return Optional.empty();
		}
	}

}
