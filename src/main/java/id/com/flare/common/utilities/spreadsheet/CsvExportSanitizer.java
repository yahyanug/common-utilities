package id.com.flare.common.utilities.spreadsheet;

import org.apache.commons.csv.CSVFormat;

/**
 * Produces one RFC 4180-compatible CSV field after applying the spreadsheet formula
 * export policy. CSV quoting and escaping are delegated to Apache Commons CSV.
 */
public final class CsvExportSanitizer {

	private CsvExportSanitizer() {
	}

	/**
	 * Escapes one CSV field using RFC 4180 rules after neutralizing formula-like content.
	 * Null is represented as an empty CSV field.
	 */
	public static String escapeCell(String value) {
		return CSVFormat.RFC4180.format(SpreadsheetFormulaGuard.escapeForSpreadsheetExport(value));
	}

}
