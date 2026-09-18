package id.com.flare.common.utilities.file;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Sanitizes untrusted file names into a single safe filename component. It does not
 * authorize storage, inspect content, or establish that a resulting path is safe from
 * filesystem links.
 */
public final class FileNameSanitizer {

	private static final Set<String> WINDOWS_RESERVED = Set.of("CON", "PRN", "AUX", "NUL", "COM1", "COM2", "COM3",
			"COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7",
			"LPT8", "LPT9");

	private FileNameSanitizer() {
	}

	/**
	 * Returns a filename without caller-controlled path components. Both slash styles,
	 * control characters, null bytes, and Windows-reserved names are handled; leading and
	 * trailing whitespace and trailing periods are removed. Null or an unusable name,
	 * including {@code .} and {@code ..}, returns empty. This method performs no content
	 * validation or authorization.
	 */
	public static Optional<String> sanitizeFileName(String fileName) {
		if (fileName == null)
			return Optional.empty();
		String baseName = fileName.replace('\\', '/');
		baseName = baseName.substring(baseName.lastIndexOf('/') + 1).replaceAll("[\\p{Cntrl}<>:/\\\\|?*]", "_")
				.replace('"', '_').trim();
		while (baseName.endsWith(".") || baseName.endsWith(" "))
			baseName = baseName.substring(0, baseName.length() - 1);
		if (baseName.isEmpty() || ".".equals(baseName) || "..".equals(baseName))
			return Optional.empty();
		String stem = baseName.contains(".") ? baseName.substring(0, baseName.indexOf('.')) : baseName;
		String windowsStem = stem.replaceAll("[. ]+$", "");
		return Optional.of(WINDOWS_RESERVED.contains(windowsStem.toUpperCase(Locale.ROOT)) ? "_" + baseName : baseName);
	}

	/**
	 * Resolves a sanitized untrusted filename under {@code baseDirectory} without writing
	 * to the filesystem. The result is normalized and verified to start with the
	 * normalized absolute base directory; null inputs or unusable names return empty.
	 * This lexical check does not authorize access or protect against symlinks created
	 * after resolution.
	 */
	public static Optional<Path> resolveInsideBaseDirectory(Path baseDirectory, String untrustedFileName) {
		if (baseDirectory == null)
			return Optional.empty();
		Path normalizedBase = baseDirectory.toAbsolutePath().normalize();
		return sanitizeFileName(untrustedFileName).map(normalizedBase::resolve).map(Path::normalize)
				.filter(candidate -> candidate.startsWith(normalizedBase));
	}

}
