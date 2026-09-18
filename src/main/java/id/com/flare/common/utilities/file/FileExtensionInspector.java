package id.com.flare.common.utilities.file;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Inspects filename extensions without treating them as evidence of file content or
 * safety. Returned extensions are lowercase and do not include a leading period.
 */
public final class FileExtensionInspector {

	private FileExtensionInspector() {
	}

	/**
	 * Extracts the final extension from the final filename component. Null, blank names,
	 * names without an extension, and single-component dot files (such as .profile)
	 * return empty. A dot file with a suffix (such as .profile.txt) returns that suffix.
	 */
	public static Optional<String> extractExtension(String fileName) {
		if (fileName == null || fileName.isBlank())
			return Optional.empty();
		String baseName = fileName.replace('\\', '/');
		baseName = baseName.substring(baseName.lastIndexOf('/') + 1);
		int extensionStart = baseName.lastIndexOf('.');
		return extensionStart > 0 && extensionStart < baseName.length() - 1
				? normalizeExtension(baseName.substring(extensionStart + 1)) : Optional.empty();
	}

	/**
	 * Normalizes a standalone extension by removing one optional leading period, trimming
	 * surrounding whitespace, and converting it to lowercase. Path separators, further
	 * periods, and empty values return empty.
	 */
	public static Optional<String> normalizeExtension(String extension) {
		if (extension == null)
			return Optional.empty();
		String normalized = extension.trim();
		if (normalized.startsWith("."))
			normalized = normalized.substring(1);
		return normalized.isEmpty() || normalized.indexOf('.') >= 0 || normalized.indexOf('/') >= 0
				|| normalized.indexOf('\\') >= 0 ? Optional.empty() : Optional.of(normalized.toLowerCase(Locale.ROOT));
	}

	/**
	 * Returns whether the final filename component has two or more non-empty extensions.
	 * This is descriptive only: compound extensions such as {@code .tar.gz} are not
	 * inherently unsafe.
	 */
	public static boolean hasMultipleExtensions(String fileName) {
		return extensionSegments(fileName).length > 1;
	}

	/**
	 * Returns whether a non-final extension is in the caller-provided
	 * suspicious-extension set. Values in that set are normalized case-insensitively; a
	 * caller defines the policy because no extension is universally suspicious.
	 */
	public static boolean hasSuspiciousDoubleExtension(String fileName, Set<String> suspiciousExtensions) {
		if (suspiciousExtensions == null || suspiciousExtensions.isEmpty())
			return false;
		Set<String> normalizedSuspicious = suspiciousExtensions.stream().map(FileExtensionInspector::normalizeExtension)
				.flatMap(Optional::stream).collect(java.util.stream.Collectors.toUnmodifiableSet());
		String[] extensions = extensionSegments(fileName);
		return extensions.length > 1
				&& Arrays.stream(extensions, 0, extensions.length - 1).anyMatch(normalizedSuspicious::contains);
	}

	private static String[] extensionSegments(String fileName) {
		if (fileName == null || fileName.isBlank())
			return new String[0];
		String baseName = fileName.replace('\\', '/');
		baseName = baseName.substring(baseName.lastIndexOf('/') + 1);
		int firstPeriod = baseName.indexOf('.', baseName.startsWith(".") ? 1 : 0);
		if (firstPeriod <= 0 || firstPeriod == baseName.length() - 1)
			return new String[0];
		return Arrays.stream(baseName.substring(firstPeriod + 1).split("\\."))
				.map(FileExtensionInspector::normalizeExtension).flatMap(Optional::stream).toArray(String[]::new);
	}

}
