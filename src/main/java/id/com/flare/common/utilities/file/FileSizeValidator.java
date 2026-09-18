package id.com.flare.common.utilities.file;

/**
 * Validates a supplied upload size against a caller-defined byte limit. This class does
 * not read content, infer a policy, or accept an unknown (negative) size.
 */
public final class FileSizeValidator {

	private FileSizeValidator() {
	}

	/**
	 * Returns whether {@code sizeInBytes} is between zero and the supplied inclusive
	 * {@code maximumSizeInBytes}. Negative values, including an unknown content length,
	 * and a negative limit return {@code false}.
	 */
	public static boolean isWithinMaximumSize(long sizeInBytes, long maximumSizeInBytes) {
		return sizeInBytes >= 0 && maximumSizeInBytes >= 0 && sizeInBytes <= maximumSizeInBytes;
	}

}
