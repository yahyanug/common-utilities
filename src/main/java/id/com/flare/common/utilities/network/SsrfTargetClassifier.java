package id.com.flare.common.utilities.network;

import java.net.URI;
import java.util.Set;

/** Classifies URL targets without DNS or network access. */
public final class SsrfTargetClassifier {

	private SsrfTargetClassifier() {
	}

	/**
	 * HOSTNAME is unresolved; PUBLIC_IP only means outside the implemented non-public
	 * ranges.
	 */
	public enum TargetType {

		INVALID, HOSTNAME, LOCAL_HOSTNAME, PUBLIC_IP, PRIVATE_IP, LOOPBACK_IP, LINK_LOCAL_IP, NON_PUBLIC_IP

	}

	/**
	 * Classifies URL host syntax locally, independent of scheme policy. Null/invalid
	 * input is INVALID.
	 */
	public static TargetType classify(String url) {
		URI uri = UrlValidator.parseAbsoluteUrl(url).orElse(null);
		if (uri == null)
			return TargetType.INVALID;
		String host = UrlValidator.normalizeHost(uri.getHost()).orElseThrow();
		if ("localhost".equalsIgnoreCase(host) || host.toLowerCase(java.util.Locale.ROOT).endsWith(".localhost"))
			return TargetType.LOCAL_HOSTNAME;
		if (IpAddressValidator.isLoopback(host))
			return TargetType.LOOPBACK_IP;
		if (IpAddressValidator.isLinkLocal(host))
			return TargetType.LINK_LOCAL_IP;
		if (IpAddressValidator.isPrivate(host))
			return TargetType.PRIVATE_IP;
		if (IpAddressValidator.parseLiteral(host).isPresent())
			return IpAddressValidator.isNonPublic(host) ? TargetType.NON_PUBLIC_IP : TargetType.PUBLIC_IP;
		return TargetType.HOSTNAME;
	}

	/**
	 * Flags invalid URLs, non-HTTP(S) schemes, credentials and known local/non-public
	 * targets. False does not mean safe: hostname DNS results and additional special-use
	 * ranges are unknown.
	 */
	public static boolean isObviouslyUnsafe(String url) {
		TargetType type = classify(url);
		return !UrlValidator.isHttpOrHttps(url)
				|| UrlValidator.parseAbsoluteUrl(url).map(uri -> uri.getRawUserInfo() != null).orElse(true)
				|| (type != TargetType.HOSTNAME && type != TargetType.PUBLIC_IP);
	}

	/**
	 * Checks an absolute HTTP(S), credential-free redirect against exact allowed
	 * hostnames. Relative URLs, localhost, IP literals and null/invalid inputs fail.
	 * Allowed hosts authorize any valid port. The caller must trust the allowed
	 * destinations and validate every subsequent redirect; this is not DNS validation or
	 * a same-origin check.
	 */
	public static boolean isSafeRedirectUrl(String url, Set<String> allowedHosts) {
		return UrlValidator.isHttpOrHttps(url) && UrlValidator.hasAllowedHost(url, allowedHosts)
				&& classify(url) == TargetType.HOSTNAME
				&& UrlValidator.parseAbsoluteUrl(url).map(uri -> uri.getUserInfo() == null).orElse(false);
	}

}
