package id.com.flare.common.utilities.country;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/** The ASEAN countries supported by this library, identified using ISO 3166-1 codes. */
public enum AseanCountry {

	INDONESIA("ID", "IDN"), MALAYSIA("MY", "MYS"), SINGAPORE("SG", "SGP"), THAILAND("TH", "THA"),
	PHILIPPINES("PH", "PHL"), VIETNAM("VN", "VNM"), BRUNEI("BN", "BRN"), CAMBODIA("KH", "KHM"), LAOS("LA", "LAO"),
	MYANMAR("MM", "MMR");

	private static final Map<String, AseanCountry> BY_ALPHA_2 = indexBy(AseanCountry::getAlpha2);

	private static final Map<String, AseanCountry> BY_ALPHA_3 = indexBy(AseanCountry::getAlpha3);

	private final String alpha2;

	private final String alpha3;

	AseanCountry(String alpha2, String alpha3) {
		this.alpha2 = alpha2;
		this.alpha3 = alpha3;
	}

	public String getAlpha2() {
		return this.alpha2;
	}

	public String getAlpha3() {
		return this.alpha3;
	}

	/** Returns the supported country for an ISO alpha-2 code, case-insensitively. */
	public static Optional<AseanCountry> getByAlpha2(String alpha2) {
		return get(BY_ALPHA_2, alpha2);
	}

	/** Returns the supported country for an ISO alpha-3 code, case-insensitively. */
	public static Optional<AseanCountry> getByAlpha3(String alpha3) {
		return get(BY_ALPHA_3, alpha3);
	}

	/** Returns whether an ISO alpha-2 or alpha-3 code is in this supported set. */
	public static boolean isSupported(String code) {
		return getByAlpha2(code).isPresent() || getByAlpha3(code).isPresent();
	}

	private static Map<String, AseanCountry> indexBy(Function<AseanCountry, String> key) {
		return Arrays.stream(values()).collect(Collectors.toUnmodifiableMap(key, Function.identity()));
	}

	private static Optional<AseanCountry> get(Map<String, AseanCountry> countries, String code) {
		return code == null ? Optional.empty() : Optional.ofNullable(countries.get(code.toUpperCase(Locale.ROOT)));
	}

}
