package id.com.flare.common.utilities.spreadsheet;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;

/**
 * Identifies Apache POI Excel formula cells without reading their result or evaluating
 * their expression. The caller owns the mutable cell/workbook and must prevent concurrent
 * mutation.
 */
public final class ExcelFormulaCellDetector {

	private ExcelFormulaCellDetector() {
	}

	/**
	 * Returns whether a POI cell is explicitly stored as a formula cell. Null is false.
	 */
	public static boolean isFormulaCell(Cell cell) {
		return cell != null && cell.getCellType() == CellType.FORMULA;
	}

}
