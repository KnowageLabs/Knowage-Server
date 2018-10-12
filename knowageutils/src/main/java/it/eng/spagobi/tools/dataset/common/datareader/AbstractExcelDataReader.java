package it.eng.spagobi.tools.dataset.common.datareader;

public abstract class AbstractExcelDataReader extends AbstractDataReader {

	public static final String EXCEL_FILE_SKIP_ROWS = "skipRows";
	public static final String EXCEL_FILE_LIMIT_ROWS = "limitRows";
	public static final String EXCEL_FILE_SHEET_NUMBER = "xslSheetNumber";

	private String skipRows;
	private String limitRows;
	private String xslSheetNumber;
	private int numberOfColumns = 0;

	public AbstractExcelDataReader() {
		super();
	}

	public String getSkipRows() {
		return skipRows;
	}

	public void setSkipRows(String skipRows) {
		this.skipRows = skipRows;
	}

	public String getLimitRows() {
		return limitRows;
	}

	public void setLimitRows(String limitRows) {
		this.limitRows = limitRows;
	}

	public String getXslSheetNumber() {
		return xslSheetNumber;
	}

	public void setXslSheetNumber(String xslSheetNumber) {
		this.xslSheetNumber = xslSheetNumber;
	}

	public int getNumberOfColumns() {
		return numberOfColumns;
	}

	public void setNumberOfColumns(int numberOfColumns) {
		this.numberOfColumns = numberOfColumns;
	}

}
