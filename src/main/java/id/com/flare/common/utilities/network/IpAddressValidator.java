package id.com.flare.common.utilities.network;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

/**
 * Parses literal IPv4 and IPv6 addresses locally. It never invokes name resolution.
 */
public final class IpAddressValidator {

	private IpAddressValidator() {
	}

	/** Returns whether input is a canonical dotted-decimal IPv4 literal. */
	public static boolean isValidIpv4(String value) {
		return parseIpv4(value) != null;
	}

	/**
	 * Returns whether input is an unbracketed, unscoped IPv6 literal, including a final
	 * embedded IPv4 address.
	 */
	public static boolean isValidIpv6(String value) {
		return parseIpv6(value) != null;
	}

	/** Returns a literal address without DNS lookup, or empty for invalid input. */
	public static Optional<InetAddress> parseLiteral(String value) {
		byte[] bytes = parseIpv4(value);
		if (bytes == null)
			bytes = parseIpv6(value);
		if (bytes == null)
			return Optional.empty();
		try {
			return Optional.of(InetAddress.getByAddress(bytes));
		}
		catch (UnknownHostException exception) {
			return Optional.empty();
		}
	}

	/**
	 * Checks the documented non-public ranges, including IPv4-mapped IPv6. Invalid input
	 * returns false. This is a fixed subset of special-use addresses, not a routability
	 * guarantee; see docs/VALIDATION_SUPPORT.md for the exact ranges.
	 * @see <a href="https://www.iana.org/assignments/iana-ipv4-special-registry/">IANA
	 * IPv4 special-use registry</a>
	 */
	public static boolean isNonPublic(String value) {
		byte[] bytes = literalBytes(value);
		return bytes != null && isNonPublic(bytes);
	}

	/** Checks RFC 1918 IPv4 and IPv6 unique-local addresses; invalid input is false. */
	public static boolean isPrivate(String value) {
		byte[] bytes = literalBytes(value);
		return bytes != null && isPrivate(bytes);
	}

	/** Checks IPv4 127/8 and IPv6 ::1, including mapped IPv4; invalid input is false. */
	public static boolean isLoopback(String value) {
		byte[] bytes = literalBytes(value);
		return bytes != null && isLoopback(bytes);
	}

	/**
	 * Checks IPv4 169.254/16 and IPv6 fe80::/10, including mapped IPv4; invalid input is
	 * false.
	 */
	public static boolean isLinkLocal(String value) {
		byte[] bytes = literalBytes(value);
		return bytes != null && isLinkLocal(bytes);
	}

	static byte[] literalBytes(String value) {
		byte[] bytes = parseIpv4(value);
		return bytes != null ? bytes : parseIpv6(value);
	}

	static boolean isNonPublic(byte[] bytes) {
		bytes = mappedIpv4(bytes);
		if (bytes.length == 4) {
			int first = unsigned(bytes[0]);
			int second = unsigned(bytes[1]);
			int third = unsigned(bytes[2]);
			return isPrivate(bytes) || isLoopback(bytes) || isLinkLocal(bytes) || first == 0 || first >= 224
					|| (first == 100 && second >= 64 && second <= 127) || (first == 192 && second == 0 && third == 0)
					|| (first == 192 && second == 0 && third == 2)
					|| (first == 198 && (second == 18 || second == 19 || (second == 51 && third == 100)))
					|| (first == 203 && second == 0 && third == 113);
		}
		return isPrivate(bytes) || isLoopback(bytes) || isLinkLocal(bytes) || isAllZero(bytes)
				|| (unsigned(bytes[0]) == 0xfe && (unsigned(bytes[1]) & 0xc0) == 0xc0) || unsigned(bytes[0]) == 0xff
				|| (unsigned(bytes[0]) == 0x20 && unsigned(bytes[1]) == 0x01 && unsigned(bytes[2]) == 0x0d
						&& unsigned(bytes[3]) == 0xb8);
	}

	static boolean isPrivate(byte[] bytes) {
		bytes = mappedIpv4(bytes);
		return bytes.length == 4
				? unsigned(bytes[0]) == 10
						|| (unsigned(bytes[0]) == 172 && unsigned(bytes[1]) >= 16 && unsigned(bytes[1]) <= 31)
						|| (unsigned(bytes[0]) == 192 && unsigned(bytes[1]) == 168)
				: (unsigned(bytes[0]) & 0xfe) == 0xfc;
	}

	static boolean isLoopback(byte[] bytes) {
		bytes = mappedIpv4(bytes);
		return bytes.length == 4 ? unsigned(bytes[0]) == 127 : isAllZero(bytes, 0, 15) && unsigned(bytes[15]) == 1;
	}

	static boolean isLinkLocal(byte[] bytes) {
		bytes = mappedIpv4(bytes);
		return bytes.length == 4 ? unsigned(bytes[0]) == 169 && unsigned(bytes[1]) == 254
				: unsigned(bytes[0]) == 0xfe && (unsigned(bytes[1]) & 0xc0) == 0x80;
	}

	private static byte[] parseIpv4(String value) {
		if (value == null || value.isEmpty())
			return null;
		String[] parts = value.split("\\.", -1);
		if (parts.length != 4)
			return null;
		byte[] bytes = new byte[4];
		for (int index = 0; index < parts.length; index++) {
			String part = parts[index];
			if (part.isEmpty() || part.length() > 3 || (part.length() > 1 && part.charAt(0) == '0'))
				return null;
			int number = 0;
			for (int characterIndex = 0; characterIndex < part.length(); characterIndex++) {
				char character = part.charAt(characterIndex);
				if (character < '0' || character > '9')
					return null;
				number = number * 10 + character - '0';
			}
			if (number > 255)
				return null;
			bytes[index] = (byte) number;
		}
		return bytes;
	}

	private static byte[] parseIpv6(String value) {
		if (value == null || value.isEmpty() || value.indexOf('%') >= 0)
			return null;
		int compressed = value.indexOf("::");
		if (compressed >= 0 && value.indexOf("::", compressed + 2) >= 0)
			return null;
		String[] halves = compressed >= 0
				? new String[] { value.substring(0, compressed), value.substring(compressed + 2) }
				: new String[] { value };
		int[] groups = new int[8];
		// An embedded IPv4 address occupies the final 32 bits, never before ::.
		if (compressed >= 0 && halves[0].indexOf('.') >= 0)
			return null;
		int leftCount = appendGroups(halves[0], groups, 0, compressed >= 0);
		if (leftCount < 0)
			return null;
		int[] rightGroups = new int[8];
		int rightCount = compressed >= 0 ? appendGroups(halves[1], rightGroups, 0, true) : 0;
		if (rightCount < 0 || (compressed < 0 && leftCount != 8) || (compressed >= 0 && leftCount + rightCount >= 8))
			return null;
		for (int index = 0; index < rightCount; index++)
			groups[8 - rightCount + index] = rightGroups[index];
		byte[] bytes = new byte[16];
		for (int index = 0; index < 8; index++) {
			bytes[index * 2] = (byte) (groups[index] >>> 8);
			bytes[index * 2 + 1] = (byte) groups[index];
		}
		return bytes;
	}

	private static int appendGroups(String section, int[] groups, int offset, boolean emptyAllowed) {
		if (section.isEmpty())
			return emptyAllowed ? 0 : -1;
		String[] tokens = section.split(":", -1);
		int groupIndex = offset;
		for (int tokenIndex = 0; tokenIndex < tokens.length; tokenIndex++) {
			String token = tokens[tokenIndex];
			if (token.isEmpty() || groupIndex >= groups.length)
				return -1;
			if (token.indexOf('.') >= 0) {
				if (tokenIndex != tokens.length - 1 || groupIndex > groups.length - 2)
					return -1;
				byte[] ipv4 = parseIpv4(token);
				if (ipv4 == null)
					return -1;
				groups[groupIndex++] = unsigned(ipv4[0]) << 8 | unsigned(ipv4[1]);
				groups[groupIndex++] = unsigned(ipv4[2]) << 8 | unsigned(ipv4[3]);
			}
			else {
				if (token.length() > 4)
					return -1;
				int group = 0;
				for (int index = 0; index < token.length(); index++) {
					int digit = Character.digit(token.charAt(index), 16);
					if (digit < 0 || token.charAt(index) > 127)
						return -1;
					group = group * 16 + digit;
				}
				groups[groupIndex++] = group;
			}
		}
		return groupIndex - offset;
	}

	private static byte[] mappedIpv4(byte[] bytes) {
		if (bytes.length == 16 && isAllZero(bytes, 0, 10) && unsigned(bytes[10]) == 0xff && unsigned(bytes[11]) == 0xff)
			return new byte[] { bytes[12], bytes[13], bytes[14], bytes[15] };
		return bytes;
	}

	private static boolean isAllZero(byte[] bytes) {
		return isAllZero(bytes, 0, bytes.length);
	}

	private static boolean isAllZero(byte[] bytes, int start, int end) {
		for (int i = start; i < end; i++)
			if (bytes[i] != 0)
				return false;
		return true;
	}

	private static int unsigned(byte value) {
		return Byte.toUnsignedInt(value);
	}

}
