package id.com.flare.common.utilities.network;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Validates absolute URLs locally. Validation does not establish host existence or
 * reachability.
 */
public final class UrlValidator {

	private static final Pattern NUMERIC_HOST = Pattern
			.compile("(?:[0-9]+|0[xX][0-9a-fA-F]+)(?:\\.(?:[0-9]+|0[xX][0-9a-fA-F]+))*\\.?");

	private UrlValidator() {
	}

	/**
	 * Parses an absolute server URL or returns empty. Requires an ASCII/ACE hostname or
	 * strict IP literal and an absent port or port 1-65535. Rejects empty ports, scoped
	 * IPv6 and ambiguous numeric hosts (integer, octal, hex, abbreviated IPv4). User info
	 * and non-HTTP schemes remain syntax-valid; apply SSRF policy separately.
	 */
	public static Optional<URI> parseAbsoluteUrl(String value) {
		if (value == null || value.isBlank())
			return Optional.empty();
		try {
			URI uri = new URI(value);
			return uri.isAbsolute() && uri.getHost() != null && normalizeHost(uri.getHost()).isPresent()
					&& !uri.getRawAuthority().endsWith(":") && isValidPort(uri.getPort()) ? Optional.of(uri)
							: Optional.empty();
		}
		catch (URISyntaxException exception) {
			return Optional.empty();
		}
	}

	/**
	 * Returns whether the value meets {@link #parseAbsoluteUrl(String)}; null is false.
	 */
	public static boolean isValidUrl(String value) {
		return parseAbsoluteUrl(value).isPresent();
	}

	/**
	 * Accepts ports 1-65535 and the URI sentinel -1 for an omitted port; zero is invalid.
	 */
	public static boolean isValidPort(int port) {
		return port == -1 || port >= 1 && port <= 65535;
	}

	/** Checks URL syntax and an HTTP or HTTPS scheme; does not check target safety. */
	public static boolean isHttpOrHttps(String value) {
		return parseAbsoluteUrl(value).map(uri -> isHttpOrHttpsScheme(uri.getScheme())).orElse(false);
	}

	/** Returns the lowercase scheme of a valid URL, or empty for null/invalid input. */
	public static Optional<String> extractScheme(String value) {
		return parseAbsoluteUrl(value).map(URI::getScheme).map(scheme -> scheme.toLowerCase(Locale.ROOT));
	}

	/**
	 * Returns the host as supplied, without IPv6 brackets; null/invalid URLs return
	 * empty.
	 */
	public static Optional<String> extractHost(String value) {
		return parseAbsoluteUrl(value).map(URI::getHost).map(UrlValidator::unbracket);
	}

	/**
	 * Matches valid URL schemes case-insensitively; null sets are false and null entries
	 * ignored.
	 */
	public static boolean hasAllowedScheme(String value, Set<String> allowedSchemes) {
		return allowedSchemes != null && parseAbsoluteUrl(value).map(URI::getScheme)
				.map(scheme -> allowedSchemes.stream().filter(candidate -> candidate != null)
						.anyMatch(candidate -> scheme.equalsIgnoreCase(candidate)))
				.orElse(false);
	}

	/**
	 * Matches normalized hosts exactly, without wildcard/subdomain matching or port
	 * restrictions. Null sets return false; null/invalid entries are ignored. IPv6
	 * comparison retains spelling.
	 */
	public static boolean hasAllowedHost(String value, Set<String> allowedHosts) {
		if (allowedHosts == null)
			return false;
		return extractHost(value).flatMap(UrlValidator::normalizeHost).map(host -> allowedHosts.stream()
				.map(UrlValidator::normalizeHost).flatMap(Optional::stream).anyMatch(host::equals)).orElse(false);
	}

	/**
	 * Lowercases scheme/host, removes a hostname's root dot and applies URI path
	 * normalization. Preserves raw escapes, user info, port, query and fragment; never
	 * decodes reserved delimiters. Null/invalid input returns empty. Normalization is not
	 * an SSRF or authorization check.
	 */
	public static Optional<String> normalizeUrl(String value) {
		return parseAbsoluteUrl(value).flatMap(uri -> normalizeHost(unbracket(uri.getHost())).flatMap(host -> {
			try {
				String authorityHost = host.indexOf(':') >= 0 ? "[" + host + "]" : host;
				String authority = (uri.getRawUserInfo() == null ? "" : uri.getRawUserInfo() + "@") + authorityHost
						+ (uri.getPort() == -1 ? "" : ":" + uri.getPort());
				String raw = uri.getScheme().toLowerCase(Locale.ROOT) + "://" + authority + uri.getRawPath()
						+ (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery())
						+ (uri.getRawFragment() == null ? "" : "#" + uri.getRawFragment());
				return Optional.of(new URI(raw).normalize().toASCIIString());
			}
			catch (URISyntaxException exception) {
				return Optional.empty();
			}
		}));
	}

	static Optional<String> normalizeHost(String host) {
		if (host == null)
			return Optional.empty();
		String literal = unbracket(host);
		if (IpAddressValidator.parseLiteral(literal).isPresent())
			return Optional.of(literal.toLowerCase(Locale.ROOT));
		return NUMERIC_HOST.matcher(literal).matches() ? Optional.empty()
				: HostnameValidator.normalizeHostname(literal);
	}

	static boolean isHttpOrHttpsScheme(String scheme) {
		return "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
	}

	static String unbracket(String host) {
		return host != null && host.startsWith("[") && host.endsWith("]") ? host.substring(1, host.length() - 1) : host;
	}

}
