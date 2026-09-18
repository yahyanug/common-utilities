package id.com.flare.common.utilities;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.Set;
import org.junit.jupiter.api.Test;
import id.com.flare.common.utilities.network.HostnameValidator;
import id.com.flare.common.utilities.network.IpAddressValidator;
import id.com.flare.common.utilities.network.SsrfTargetClassifier;
import id.com.flare.common.utilities.network.UrlValidator;

class NetworkSecurityTests {

	@Test
	void normalizationPreservesEscapedDelimitersAndIsIdempotent() {
		String input = "HTTPS://Example.COM/a%2Fb/%2e%2e/c?x=a%26b%3Dc%23d#x%2Fy";
		String expected = "https://example.com/a%2Fb/%2e%2e/c?x=a%26b%3Dc%23d#x%2Fy";
		assertThat(UrlValidator.normalizeUrl(input)).contains(expected);
		assertThat(UrlValidator.normalizeUrl(expected)).contains(expected);
	}

	@Test
	void rejectsIpv4BeforeIpv6CompressionAndNonPublicRangeFalsePositives() {
		assertThat(IpAddressValidator.isValidIpv6("192.0.2.1::")).isFalse();
		assertThat(IpAddressValidator.isValidIpv6("1:192.0.2.1::2")).isFalse();
		for (String value : new String[] { "192.0.2.1", "198.51.100.1", "203.0.113.1", "fec0::1" })
			assertThat(IpAddressValidator.isNonPublic(value)).as(value).isTrue();
		for (String value : new String[] { "192.0.3.1", "192.2.0.1", "198.51.99.1", "203.0.114.1" })
			assertThat(IpAddressValidator.isNonPublic(value)).as(value).isFalse();
	}

	@Test
	void preventsLocalHostAndNumericHostSpellingBypasses() {
		for (String value : new String[] { "http://localhost.", "http://API.LOCALHOST.", "http://2130706433",
				"http://0177.0.0.1", "http://0x7f000001", "http://127.1", "http://[fe80::1%25eth0]" }) {
			assertThat(SsrfTargetClassifier.isObviouslyUnsafe(value)).as(value).isTrue();
			assertThat(SsrfTargetClassifier.isSafeRedirectUrl(value,
					Set.of("localhost", "api.localhost", "2130706433", "0177.0.0.1", "0x7f000001", "127.1"))).isFalse();
		}
		assertThat(HostnameValidator.normalizeHostname("127.0.0.1.")).isEmpty();
		assertThat(HostnameValidator.normalizeHostname("\uFF11\uFF12\uFF17.0.0.1")).isEmpty();
	}

	@Test
	void rejectsMalformedAuthoritiesAndKeepsSyntaxSeparateFromSsrfPolicy() {
		for (String value : new String[] { "", " ", "//example.com", "https://example.com:", "http://[::1]:0",
				"http://example.com:65536", "http://example.com\\@localhost", "http://%31%32%37.0.0.1" })
			assertThat(UrlValidator.isValidUrl(value)).as(value).isFalse();
		assertThat(UrlValidator.isValidUrl("ftp://example.com/file")).isTrue();
		assertThat(SsrfTargetClassifier.isObviouslyUnsafe("ftp://example.com/file")).isTrue();
		assertThat(SsrfTargetClassifier.isObviouslyUnsafe("http://user@example.com")).isTrue();
		assertThat(UrlValidator.hasAllowedHost("https://example.com.evil.test", Set.of("example.com"))).isFalse();
		assertThat(UrlValidator.hasAllowedScheme("https://example.com", null)).isFalse();
		assertThat(UrlValidator.hasAllowedHost("https://example.com", null)).isFalse();
	}

	@Test
	void validatesHostsIpsAndPorts() {
		assertThat(HostnameValidator.normalizeHostname("B\u00FCcher.example.")).contains("xn--bcher-kva.example");
		assertThat(HostnameValidator.isValidHostname("api.example.com")).isTrue();
		assertThat(HostnameValidator.isValidHostname("bad_host")).isFalse();
		assertThat(IpAddressValidator.isValidIpv4("192.168.1.1")).isTrue();
		assertThat(IpAddressValidator.isValidIpv4("256.1.1.1")).isFalse();
		assertThat(IpAddressValidator.isValidIpv4("01.2.3.4")).isFalse();
		assertThat(IpAddressValidator.isValidIpv6("2001:db8::1")).isTrue();
		assertThat(IpAddressValidator.isValidIpv6("::ffff:127.0.0.1")).isTrue();
		assertThat(IpAddressValidator.isValidIpv6("2001:::1")).isFalse();
		assertThat(IpAddressValidator.isPrivate("10.0.0.1")).isTrue();
		assertThat(IpAddressValidator.isLoopback("127.0.0.1")).isTrue();
		assertThat(IpAddressValidator.isLinkLocal("169.254.1.1")).isTrue();
		assertThat(IpAddressValidator.isNonPublic("::1")).isTrue();
		assertThat(IpAddressValidator.isNonPublic("8.8.8.8")).isFalse();
		assertThat(UrlValidator.isValidPort(1)).isTrue();
		assertThat(UrlValidator.isValidPort(65535)).isTrue();
		assertThat(UrlValidator.isValidPort(0)).isFalse();
		assertThat(UrlValidator.isValidPort(65536)).isFalse();
	}

	@Test
	void validatesAndRestrictsUrls() {
		assertThat(UrlValidator.normalizeUrl("HTTP://Example.COM/a/../b?x=1")).contains("http://example.com/b?x=1");
		assertThat(UrlValidator.extractHost("https://example.com:8443/x")).contains("example.com");
		assertThat(UrlValidator.extractScheme("https://example.com")).contains("https");
		assertThat(UrlValidator.isHttpOrHttps("https://example.com")).isTrue();
		assertThat(UrlValidator.isHttpOrHttps("ftp://example.com")).isFalse();
		assertThat(UrlValidator.isValidUrl("http://")).isFalse();
		assertThat(UrlValidator.isValidUrl(null)).isFalse();
		assertThat(UrlValidator.hasAllowedScheme("https://example.com", Set.of("HTTPS"))).isTrue();
		assertThat(UrlValidator.hasAllowedHost("https://EXAMPLE.com", Set.of("example.com"))).isTrue();
		assertThat(UrlValidator.hasAllowedHost("https://sub.example.com", Set.of("example.com"))).isFalse();
	}

	@Test
	void classifiesSsrfTargetsAndSafeRedirects() {
		assertThat(SsrfTargetClassifier.classify("http://127.0.0.1"))
				.isEqualTo(SsrfTargetClassifier.TargetType.LOOPBACK_IP);
		assertThat(SsrfTargetClassifier.classify("http://[::1]/"))
				.isEqualTo(SsrfTargetClassifier.TargetType.LOOPBACK_IP);
		assertThat(SsrfTargetClassifier.classify("http://169.254.169.254"))
				.isEqualTo(SsrfTargetClassifier.TargetType.LINK_LOCAL_IP);
		assertThat(SsrfTargetClassifier.classify("http://localhost"))
				.isEqualTo(SsrfTargetClassifier.TargetType.LOCAL_HOSTNAME);
		assertThat(SsrfTargetClassifier.isObviouslyUnsafe("http://10.0.0.1")).isTrue();
		assertThat(SsrfTargetClassifier.isObviouslyUnsafe("https://example.com")).isFalse();
		assertThat(SsrfTargetClassifier.isSafeRedirectUrl("https://example.com/callback", Set.of("example.com")))
				.isTrue();
		assertThat(SsrfTargetClassifier.isSafeRedirectUrl("http://127.0.0.1", Set.of("127.0.0.1"))).isFalse();
		assertThat(SsrfTargetClassifier.isSafeRedirectUrl("https://user@example.com", Set.of("example.com"))).isFalse();
	}

}
