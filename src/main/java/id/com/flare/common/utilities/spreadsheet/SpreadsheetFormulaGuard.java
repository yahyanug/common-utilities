package id.com.flare.common.utilities.spreadsheet;

/**
 * Detects spreadsheet formula-like text and neutralizes it for export. It does not
 * evaluate formulas or determine whether a spreadsheet application will execute a value.
 */
public final class SpreadsheetFormulaGuard {

	private SpreadsheetFormulaGuard() {
	}

	/**
	 * Returns whether the first non-whitespace, control, or format code point is one of
	 * {@code =}, {@code +}, {@code -}, or {@code @}. This includes common leading-space,
	 * tab, newline, null-byte, and zero-width-character bypass attempts.
	 */
	public static boolean isFormulaLike(String value) {
		if (value == null || value.isEmpty())
			return false;
		for (int offset = 0; offset < value.length();) {
			int codePoint = value.codePointAt(offset);
			if (!isIgnorablePrefix(codePoint))
				return codePoint == '=' || codePoint == '+' || codePoint == '-' || codePoint == '@';
			offset += Character.charCount(codePoint);
		}
		return false;
	}

	/**
	 * Prefixes formula-like text with an apostrophe as an export mitigation. Spreadsheet
	 * products and import/re-save flows differ; this is not a universal execution-safety
	 * guarantee. Apply CSV quoting separately. For XLSX, write an explicit string cell,
	 * never a formula cell. Null remains null and ordinary text is unchanged; negative
	 * numbers supplied as strings are deliberately treated as formula-like.
	 */
	public static String escapeForSpreadsheetExport(String value) {
		return isFormulaLike(value) ? "'" + value : value;
	}

	private static boolean isIgnorablePrefix(int codePoint) {
		int type = Character.getType(codePoint);
		return Character.isWhitespace(codePoint) || Character.isSpaceChar(codePoint)
				|| Character.isISOControl(codePoint) || type == Character.FORMAT;
	}

}
