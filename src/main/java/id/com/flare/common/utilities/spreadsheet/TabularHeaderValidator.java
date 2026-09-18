package id.com.flare.common.utilities.spreadsheet;

import java.text.Normalizer;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Normalizes and validates CSV or spreadsheet headers without imposing an import schema.
 * Required columns are always supplied by the caller.
 */
public final class TabularHeaderValidator {

	private static final Pattern WHITESPACE = Pattern.compile("[\\p{javaWhitespace}\\p{Zs}]+");

	private TabularHeaderValidator() {
	}

	/**
	 * Returns a canonical header: Unicode NFKC, trimmed and collapsed whitespace, and
	 * lowercase using {@link Locale#ROOT}. Null or blank headers return empty.
	 */
	public static Optional<String> normalizeHeader(String header) {
		if (header == null)
			return Optional.empty();
		String normalized = WHITESPACE.matcher(Normalizer.normalize(header, Normalizer.Form.NFKC)).replaceAll(" ")
				.strip().toLowerCase(Locale.ROOT);
		return normalized.isEmpty() ? Optional.empty() : Optional.of(normalized);
	}

	/**
	 * Returns true for a null collection or any null/blank header; an empty collection is
	 * false.
	 */
	public static boolean hasBlankHeaders(Collection<String> headers) {
		return headers == null || headers.stream().anyMatch(header -> normalizeHeader(header).isEmpty());
	}

	/**
	 * Returns canonical header names that occur more than once, preserving their first
	 * duplicate encounter order. Blank headers are reported separately by
	 * {@link #hasBlankHeaders(Collection)}.
	 */
	public static Set<String> findDuplicateHeaders(Collection<String> headers) {
		if (headers == null)
			return Set.of();
		Set<String> seen = new LinkedHashSet<>();
		Set<String> duplicates = new LinkedHashSet<>();
		for (String header : headers) {
			normalizeHeader(header).ifPresent(normalized -> {
				if (!seen.add(normalized))
					duplicates.add(normalized);
			});
		}
		return Collections.unmodifiableSet(duplicates);
	}

	/**
	 * Returns normalized caller-required columns absent from the supplied headers. Null
	 * or blank required column declarations are ignored by this diagnostic; callers can
	 * use {@link #hasRequiredColumns(Collection, Collection)} to reject invalid
	 * declarations.
	 */
	public static Set<String> findMissingRequiredColumns(Collection<String> headers,
			Collection<String> requiredColumns) {
		if (requiredColumns == null)
			return Set.of();
		Set<String> available = new LinkedHashSet<>();
		if (headers != null)
			headers.forEach(header -> normalizeHeader(header).ifPresent(available::add));
		Set<String> missing = new LinkedHashSet<>();
		for (String requiredColumn : requiredColumns)
			normalizeHeader(requiredColumn).filter(normalized -> !available.contains(normalized))
					.ifPresent(missing::add);
		return Collections.unmodifiableSet(missing);
	}

	/**
	 * Returns whether all caller-required columns are present after normalization. Null
	 * or blank required declarations, null headers, blank headers, or duplicate headers
	 * are rejected to avoid ambiguous import mappings. An empty required list imposes no
	 * required names; two empty collections return true.
	 */
	public static boolean hasRequiredColumns(Collection<String> headers, Collection<String> requiredColumns) {
		return headers != null && requiredColumns != null && !hasBlankHeaders(headers)
				&& findDuplicateHeaders(headers).isEmpty()
				&& requiredColumns.stream().allMatch(requiredColumn -> normalizeHeader(requiredColumn).isPresent())
				&& findMissingRequiredColumns(headers, requiredColumns).isEmpty();
	}

}
