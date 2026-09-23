package id.com.flare.common.utilities.network.http;

/**
 * Generic request metadata extracted from an HTTP servlet request.
 *
 * <p>
 * Forwarded values are metadata supplied by the request and require a correctly
 * configured trusted proxy or gateway before use in security decisions.
 * </p>
 */
public record HttpRequestDetails(String method, String ipAddress, String uri, String userAgent) {
}
