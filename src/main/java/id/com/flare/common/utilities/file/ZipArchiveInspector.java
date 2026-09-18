package id.com.flare.common.utilities.file;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

/**
 * Scans ZIP local entries without extraction or formula/content evaluation. No malware
 * guarantee is provided. Central-directory integrity, symlinks, nested archives and
 * eventual extraction are outside this stream scanner's scope.
 */
public final class ZipArchiveInspector {

	private static final int BUFFER_SIZE = 8192;

	private ZipArchiveInspector() {
	}

	/**
	 * Recognizes Tika's ZIP, TAR, gzip, 7z and RAR MIME types. Null/empty returns false.
	 * Recognition does not establish integrity; content may be an archive-based document.
	 */
	public static boolean isArchive(byte[] content) {
		return FileMimeTypeDetector.detectMimeType(content).map(ZipArchiveInspector::isArchiveMimeType).orElse(false);
	}

	/**
	 * Checks a relative slash-separated entry path; one final slash is allowed for a
	 * directory. Every component must survive FileNameSanitizer unchanged. Rejects
	 * traversal, backslashes, absolute/drive paths, empty components, Windows devices,
	 * alternate streams and ambiguous trailing spaces/periods. Null/blank returns false.
	 */
	public static boolean isSafeEntryPath(String entryPath) {
		if (entryPath == null || entryPath.isBlank() || entryPath.indexOf('\\') >= 0 || entryPath.startsWith("/"))
			return false;
		String path = entryPath.endsWith("/") ? entryPath.substring(0, entryPath.length() - 1) : entryPath;
		for (String component : path.split("/", -1)) {
			if (FileNameSanitizer.sanitizeFileName(component).filter(component::equals).isEmpty())
				return false;
		}
		return true;
	}

	/**
	 * Resolves a validated entry lexically below the absolute normalized base, without
	 * writes. Null/unsafe/unrepresentable paths return empty. Existing symlinks and
	 * filesystem races still require extraction-time controls.
	 */
	public static Optional<Path> resolveEntryInsideBaseDirectory(Path baseDirectory, String entryPath) {
		if (baseDirectory == null || !isSafeEntryPath(entryPath))
			return Optional.empty();
		try {
			Path normalizedBase = baseDirectory.toAbsolutePath().normalize();
			Path candidate = normalizedBase.resolve(entryPath).normalize();
			return candidate.startsWith(normalizedBase) && !candidate.equals(normalizedBase) ? Optional.of(candidate)
					: Optional.empty();
		}
		catch (InvalidPathException exception) {
			return Optional.empty();
		}
	}

	/**
	 * Checks safe local entry names, inclusive entry count (directories included), and
	 * running aggregate decoded/compressed payload ratio after each buffer. Stops on
	 * excess without draining the entry; releases the inflater but never closes the
	 * caller's stream. Null, invalid configuration object, malformed local entries or
	 * truncated initial headers return false. Empty ZIPs require a complete empty end
	 * record. This does not validate the central directory of non-empty archives. Callers
	 * must independently bound input size/time; no absolute decoded-byte cap or recursive
	 * archive scan is implied.
	 * @throws IOException on underlying I/O failures (truncated ZIP EOF returns false)
	 */
	public static boolean hasAcceptableEntries(InputStream content, ZipArchiveLimits limits) throws IOException {
		if (content == null || limits == null)
			return false;
		try {
			return inspectEntries(content, limits);
		}
		catch (ZipException | EOFException | IllegalArgumentException | ArithmeticException exception) {
			return false;
		}
	}

	private static boolean inspectEntries(InputStream content, ZipArchiveLimits limits) throws IOException {
		PushbackInputStream input = new PushbackInputStream(content, 4);
		byte[] header = input.readNBytes(4);
		if (header.length != 4 || header[0] != 'P' || header[1] != 'K')
			return false;
		if (header[2] == 5 && header[3] == 6)
			return isCompleteEmptyEndRecord(input);
		if (header[2] != 3 || header[3] != 4)
			return false;
		input.unread(header);
		long entryCount = 0;
		long decoded = 0;
		long compressed = 0;
		byte[] buffer = new byte[BUFFER_SIZE];
		try (MeasuredZipInputStream zip = new MeasuredZipInputStream(input)) {
			for (ZipEntry entry; (entry = zip.getNextEntry()) != null;) {
				if (!isSafeEntryPath(entry.getName()) || entryCount == limits.maximumEntryCount())
					return false;
				entryCount++;
				long entryDecoded = 0;
				for (int count; (count = zip.read(buffer)) != -1;) {
					decoded = Math.addExact(decoded, count);
					entryDecoded = Math.addExact(entryDecoded, count);
					long currentCompressed = entry.getMethod() == ZipEntry.STORED ? entryDecoded : zip.compressedRead();
					if (!withinRatio(decoded, Math.addExact(compressed, currentCompressed),
							limits.maximumExpansionRatio()))
						return false;
				}
				long size = entry.getCompressedSize();
				if (size < 0)
					return false;
				compressed = Math.addExact(compressed, size);
				if (!withinRatio(decoded, compressed, limits.maximumExpansionRatio()))
					return false;
			}
		}
		return entryCount > 0;
	}

	private static boolean isCompleteEmptyEndRecord(InputStream input) throws IOException {
		byte[] rest = input.readNBytes(18);
		if (rest.length != 18)
			return false;
		for (int index = 0; index < 16; index++) {
			if (rest[index] != 0)
				return false;
		}
		int commentLength = Byte.toUnsignedInt(rest[16]) | (Byte.toUnsignedInt(rest[17]) << 8);
		return input.readNBytes(commentLength).length == commentLength;
	}

	private static boolean withinRatio(long decoded, long compressed, double maximumRatio) {
		return decoded == 0 || (compressed > 0 && (double) decoded / compressed <= maximumRatio);
	}

	private static boolean isArchiveMimeType(String mimeType) {
		return "application/zip".equalsIgnoreCase(mimeType) || "application/x-tar".equalsIgnoreCase(mimeType)
				|| "application/gzip".equalsIgnoreCase(mimeType)
				|| "application/x-7z-compressed".equalsIgnoreCase(mimeType)
				|| "application/x-rar-compressed".equalsIgnoreCase(mimeType);
	}

	/**
	 * Inclusive caller limits. Negative counts, negative ratios, NaN and infinity throw
	 * IllegalArgumentException. Zero permits only zero entries or zero decoded bytes.
	 */
	public record ZipArchiveLimits(long maximumEntryCount, double maximumExpansionRatio) {

		public ZipArchiveLimits {
			if (maximumEntryCount < 0 || !Double.isFinite(maximumExpansionRatio) || maximumExpansionRatio < 0)
				throw new IllegalArgumentException("ZIP limits must be finite and non-negative");
		}

	}

	private static final class MeasuredZipInputStream extends ZipInputStream {

		private MeasuredZipInputStream(InputStream input) {
			super(new FilterInputStream(input) {
				@Override
				public void close() {
					// Caller owns the input; ZipInputStream still releases its inflater.
				}
			});
		}

		private long compressedRead() {
			return this.inf.getBytesRead();
		}

	}

}
