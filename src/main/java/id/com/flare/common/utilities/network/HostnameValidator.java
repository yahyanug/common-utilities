package id.com.flare.common.utilities.network;

import java.net.IDN;
import java.util.Locale;
import java.util.Optional;

/**
 * Validates and normalizes hostname syntax without DNS resolution or reachability checks.
 */
public final class HostnameValidator {

	private HostnameValidator() {
	}

	/**
	 * Returns a lowercase ASCII hostname using IDN conversion. Labels may contain ASCII
	 * letters, digits, and interior hyphens. Null, blank, IP literals, and malformed
	 * names return empty. A final root period is removed.
	 */
	public static Optional<String> normalizeHostname(String host) {
		if (host == null || host.isBlank() || IpAddressValidator.parseLiteral(host).isPresent())
			return Optional.empty();
		try {
			String ascii = IDN.toASCII(host, IDN.USE_STD3_ASCII_RULES).toLowerCase(Locale.ROOT);
			if (ascii.endsWith("."))
				ascii = ascii.substring(0, ascii.length() - 1);
			if (ascii.isEmpty() || ascii.length() > 253 || IpAddressValidator.parseLiteral(ascii).isPresent())
				return Optional.empty();
			for (String label : ascii.split("\\.", -1)) {
				if (label.isEmpty() || label.length() > 63 || label.startsWith("-") || label.endsWith("-"))
					return Optional.empty();
			}
			return Optional.of(ascii);
		}
		catch (IllegalArgumentException exception) {
			return Optional.empty();
		}
	}

	/**
	 * Returns whether a value has valid hostname syntax. It does not resolve the name.
	 */
	public static boolean isValidHostname(String host) {
		return normalizeHostname(host).isPresent();
	}

}
