package id.com.flare.common.utilities.network.http;

import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Extracts generic metadata from servlet requests.
 *
 * <p>
 * Forwarded headers are trusted only when the application is behind a correctly
 * configured trusted proxy or gateway.
 * </p>
 */
public final class HttpRequestUtil {

	private static final String UNKNOWN = "unknown";

	private HttpRequestUtil() {
	}

	/**
	 * Returns the client IP metadata, preferring X-Real-IP, X-Forwarded-For, and then the
	 * servlet remote address. Blank and {@code unknown} forwarded values are ignored.
	 * @throws NullPointerException when {@code request} is null
	 */
	public static String getClientIpAddress(HttpServletRequest request) {
		Objects.requireNonNull(request, "request must not be null");
		String realIp = usableValue(request.getHeader(HttpHeaderNames.X_REAL_IP));
		if (realIp != null)
			return realIp;
		String forwardedFor = firstUsableValue(request.getHeader(HttpHeaderNames.X_FORWARDED_FOR));
		return forwardedFor != null ? forwardedFor : request.getRemoteAddr();
	}

	/**
	 * Returns the User-Agent header.
	 * @throws NullPointerException when {@code request} is null
	 */
	public static String getUserAgent(HttpServletRequest request) {
		Objects.requireNonNull(request, "request must not be null");
		return request.getHeader(HttpHeaderNames.USER_AGENT);
	}

	/**
	 * Returns the Origin header.
	 * @throws NullPointerException when {@code request} is null
	 */
	public static String getOrigin(HttpServletRequest request) {
		Objects.requireNonNull(request, "request must not be null");
		return request.getHeader(HttpHeaderNames.ORIGIN);
	}

	/**
	 * Returns the forwarded host metadata when present, otherwise the servlet server
	 * name.
	 * @throws NullPointerException when {@code request} is null
	 */
	public static String getHost(HttpServletRequest request) {
		Objects.requireNonNull(request, "request must not be null");
		String forwardedHost = firstUsableValue(request.getHeader(HttpHeaderNames.X_FORWARDED_HOST));
		return forwardedHost != null ? forwardedHost : request.getServerName();
	}

	/**
	 * Returns the request method, selected client IP address, URI, and user agent.
	 * @throws NullPointerException when {@code request} is null
	 */
	public static HttpRequestDetails getDetails(HttpServletRequest request) {
		Objects.requireNonNull(request, "request must not be null");
		return new HttpRequestDetails(request.getMethod(), getClientIpAddress(request), request.getRequestURI(),
				getUserAgent(request));
	}

	private static String firstUsableValue(String value) {
		if (value == null)
			return null;
		for (String candidate : value.split(",")) {
			String usableValue = usableValue(candidate);
			if (usableValue != null)
				return usableValue;
		}
		return null;
	}

	private static String usableValue(String value) {
		if (value == null)
			return null;
		String trimmed = value.trim();
		return trimmed.isEmpty() || UNKNOWN.equalsIgnoreCase(trimmed) ? null : trimmed;
	}

}
