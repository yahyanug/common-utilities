package id.com.flare.common.utilities;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import id.com.flare.common.utilities.spreadsheet.CsvExportSanitizer;
import id.com.flare.common.utilities.spreadsheet.ExcelFormulaCellDetector;
import id.com.flare.common.utilities.spreadsheet.SpreadsheetFormulaGuard;
import id.com.flare.common.utilities.spreadsheet.TabularHeaderValidator;

class SpreadsheetSecurityTests {

	@Test
	void csvRoundTripsAsOneFieldAndNeutralizationIsIdempotent() throws IOException {
		for (String input : new String[] { "", "\u00E9\uD83D\uDE00", "\"\n=1,2", "\t\uFEFF=1,2\r\n\"x\"", "-12" }) {
			String escaped = CsvExportSanitizer.escapeCell(input);
			try (var parser = org.apache.commons.csv.CSVParser.parse(escaped + "\r\n",
					org.apache.commons.csv.CSVFormat.RFC4180)) {
				var records = parser.getRecords();
				assertThat(records).hasSize(1);
				assertThat(records.get(0).size()).isEqualTo(1);
				assertThat(records.get(0).get(0)).isEqualTo(SpreadsheetFormulaGuard.escapeForSpreadsheetExport(input));
			}
			String sanitized = SpreadsheetFormulaGuard.escapeForSpreadsheetExport(input);
			assertThat(SpreadsheetFormulaGuard.escapeForSpreadsheetExport(sanitized)).isEqualTo(sanitized);
		}
	}

	@Test
	void normalizesAndValidatesCallerDefinedHeaders() {
		assertThat(TabularHeaderValidator.normalizeHeader("  Product\t Name  ")).contains("product name");
		assertThat(TabularHeaderValidator.normalizeHeader("\uFF23\uFF41\uFF46\uFF45")).contains("cafe");
		assertThat(TabularHeaderValidator.normalizeHeader("  ")).isEmpty();
		assertThat(TabularHeaderValidator.normalizeHeader(null)).isEmpty();

		List<String> headers = List.of(" SKU ", "product name", "Quantity");
		assertThat(TabularHeaderValidator.hasRequiredColumns(headers, List.of("sku", " PRODUCT NAME "))).isTrue();
		assertThat(TabularHeaderValidator.findMissingRequiredColumns(headers, List.of("sku", "price")))
				.containsExactly("price");
		assertThat(TabularHeaderValidator.hasRequiredColumns(headers, List.of("sku", "price"))).isFalse();
		assertThat(TabularHeaderValidator.findDuplicateHeaders(List.of("SKU", " sku ", "Name"))).containsExactly("sku");
		assertThat(TabularHeaderValidator.hasRequiredColumns(List.of("SKU", " sku "), List.of("sku"))).isFalse();
		assertThat(TabularHeaderValidator.hasBlankHeaders(List.of("SKU", " "))).isTrue();
		assertThat(TabularHeaderValidator.hasBlankHeaders(null)).isTrue();
		assertThat(TabularHeaderValidator.findDuplicateHeaders(null)).isEmpty();
		assertThat(TabularHeaderValidator.findMissingRequiredColumns(null, List.of("sku"))).containsExactly("sku");
		assertThat(TabularHeaderValidator.hasRequiredColumns(List.of("sku"), null)).isFalse();
		assertThat(TabularHeaderValidator.findMissingRequiredColumns(List.of("sku"), null)).isEqualTo(Set.of());
	}

	@Test
	void detectsAndNeutralizesFormulaLikeValuesBeforeCsvEscaping() {
		assertThat(SpreadsheetFormulaGuard.isFormulaLike("ordinary café")).isFalse();
		assertThat(SpreadsheetFormulaGuard.isFormulaLike("=SUM(A1:A2)")).isTrue();
		assertThat(SpreadsheetFormulaGuard.isFormulaLike("+1")).isTrue();
		assertThat(SpreadsheetFormulaGuard.isFormulaLike("-1")).isTrue();
		assertThat(SpreadsheetFormulaGuard.isFormulaLike("@cmd")).isTrue();
		assertThat(SpreadsheetFormulaGuard.isFormulaLike(" \t\n=SUM(A1:A2)")).isTrue();
		assertThat(SpreadsheetFormulaGuard.isFormulaLike("\u0000\u200B@cmd")).isTrue();
		assertThat(SpreadsheetFormulaGuard.isFormulaLike("\t")).isFalse();
		assertThat(SpreadsheetFormulaGuard.isFormulaLike(null)).isFalse();
		assertThat(SpreadsheetFormulaGuard.escapeForSpreadsheetExport("=1+1")).isEqualTo("'=1+1");
		assertThat(SpreadsheetFormulaGuard.escapeForSpreadsheetExport("normal")).isEqualTo("normal");
		assertThat(SpreadsheetFormulaGuard.escapeForSpreadsheetExport(null)).isNull();

		assertThat(CsvExportSanitizer.escapeCell("normal, \"unicode café\"\ntext"))
				.isEqualTo("\"normal, \"\"unicode café\"\"\ntext\"");
		assertThat(CsvExportSanitizer.escapeCell("=1+1")).isEqualTo("'=1+1");
		assertThat(CsvExportSanitizer.escapeCell(null)).isEmpty();
	}

	@Test
	void detectsExcelFormulaCellsWithoutEvaluatingThem() throws IOException {
		try (XSSFWorkbook workbook = new XSSFWorkbook()) {
			var row = workbook.createSheet().createRow(0);
			var formulaCell = row.createCell(0);
			formulaCell.setCellFormula("1+1");
			var textCell = row.createCell(1);
			textCell.setCellValue("=1+1");

			assertThat(ExcelFormulaCellDetector.isFormulaCell(formulaCell)).isTrue();
			assertThat(ExcelFormulaCellDetector.isFormulaCell(textCell)).isFalse();
			assertThat(ExcelFormulaCellDetector.isFormulaCell(null)).isFalse();
		}
	}

}
