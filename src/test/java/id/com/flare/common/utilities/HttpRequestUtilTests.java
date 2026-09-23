package id.com.flare.common.utilities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Proxy;
import java.util.Map;

import org.junit.jupiter.api.Test;

import id.com.flare.common.utilities.network.http.HttpRequestDetails;
import id.com.flare.common.utilities.network.http.HttpRequestUtil;
import jakarta.servlet.http.HttpServletRequest;

class HttpRequestUtilTests {

	@Test
	void returnsRealIpWhenPresent() {
		assertThat(HttpRequestUtil.getClientIpAddress(request(Map.of("X-Real-IP", "203.0.113.10"), "192.0.2.10")))
				.isEqualTo("203.0.113.10");
	}

	@Test
	void usesForwardedForWhenRealIpIsAbsent() {
		assertThat(HttpRequestUtil.getClientIpAddress(request(Map.of("X-Forwarded-For", "203.0.113.11"), "192.0.2.10")))
				.isEqualTo("203.0.113.11");
	}

	@Test
	void returnsFirstUsableForwardedForValue() {
		assertThat(HttpRequestUtil
				.getClientIpAddress(request(Map.of("X-Forwarded-For", "203.0.113.12, 198.51.100.12"), "192.0.2.10")))
						.isEqualTo("203.0.113.12");
	}

	@Test
	void ignoresUnknownAndBlankForwardedIpValues() {
		assertThat(HttpRequestUtil
				.getClientIpAddress(request(Map.of("X-Real-IP", " unknown ", "X-Forwarded-For", " "), "192.0.2.10")))
						.isEqualTo("192.0.2.10");
		assertThat(HttpRequestUtil
				.getClientIpAddress(request(Map.of("X-Forwarded-For", "unknown, 203.0.113.13"), "192.0.2.10")))
						.isEqualTo("203.0.113.13");
	}

	@Test
	void usesRemoteAddressAsClientIpFallback() {
		assertThat(HttpRequestUtil.getClientIpAddress(request(Map.of(), "192.0.2.10"))).isEqualTo("192.0.2.10");
	}

	@Test
	void returnsUserAgentAndOrigin() {
		HttpServletRequest request = request(Map.of("User-Agent", "test-agent", "Origin", "https://app.example.test"),
				"192.0.2.10");

		assertThat(HttpRequestUtil.getUserAgent(request)).isEqualTo("test-agent");
		assertThat(HttpRequestUtil.getOrigin(request)).isEqualTo("https://app.example.test");
	}

	@Test
	void prefersFirstForwardedHostValue() {
		assertThat(HttpRequestUtil.getHost(
				request(Map.of("X-Forwarded-Host", "public.example.test, internal.example.test"), "192.0.2.10")))
						.isEqualTo("public.example.test");
	}

	@Test
	void usesServerNameWhenForwardedHostIsAbsent() {
		assertThat(HttpRequestUtil.getHost(request(Map.of(), "192.0.2.10"))).isEqualTo("service.example.test");
	}

	@Test
	void mapsRequestDetails() {
		HttpServletRequest request = request(Map.of("X-Real-IP", "203.0.113.14", "User-Agent", "test-agent"),
				"192.0.2.10");

		assertThat(HttpRequestUtil.getDetails(request))
				.isEqualTo(new HttpRequestDetails("POST", "203.0.113.14", "/orders/42", "test-agent"));
	}

	@Test
	void rejectsNullRequests() {
		assertThatThrownBy(() -> HttpRequestUtil.getClientIpAddress(null)).isInstanceOf(NullPointerException.class);
		assertThatThrownBy(() -> HttpRequestUtil.getUserAgent(null)).isInstanceOf(NullPointerException.class);
		assertThatThrownBy(() -> HttpRequestUtil.getOrigin(null)).isInstanceOf(NullPointerException.class);
		assertThatThrownBy(() -> HttpRequestUtil.getHost(null)).isInstanceOf(NullPointerException.class);
		assertThatThrownBy(() -> HttpRequestUtil.getDetails(null)).isInstanceOf(NullPointerException.class);
	}

	private static HttpServletRequest request(Map<String, String> headers, String remoteAddress) {
		return (HttpServletRequest) Proxy.newProxyInstance(HttpRequestUtilTests.class.getClassLoader(),
				new Class<?>[] { HttpServletRequest.class }, (proxy, method, arguments) -> switch (method.getName()) {
					case "getHeader" -> headers.get(arguments[0]);
					case "getRemoteAddr" -> remoteAddress;
					case "getServerName" -> "service.example.test";
					case "getMethod" -> "POST";
					case "getRequestURI" -> "/orders/42";
					default -> null;
				});
	}

}
