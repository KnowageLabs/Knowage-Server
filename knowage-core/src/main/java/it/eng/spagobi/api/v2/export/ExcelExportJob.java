// File: `src/main/java/it/eng/spagobi/api/v2/export/ExcelExportJob.java`
package it.eng.spagobi.api.v2.export;

import it.eng.knowage.commons.multitenant.OrganizationImageManager;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.iterator.DataIterator;
import it.eng.spagobi.tools.dataset.common.iterator.ResultSetIterator;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import org.apache.log4j.Logger;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.util.*;

public class ExcelExportJob extends AbstractExportJob {
	private static final Logger LOGGER = Logger.getLogger(ExcelExportJob.class);

	private static final String DATE_FORMAT = "dd/MM/yyyy";
	private static final String TIMESTAMP_FORMAT = "dd/MM/yyyy HH:mm:ss.SSS";

	private String imageB64 = "";
	private String documentName = "";

	@Override
	protected void export(JobExecutionContext context) throws JobExecutionException {
		OutputStream exportFileOS = getDataOutputStream();
		Path spillFile = null;

		try {
			IDataSet dataSet = getDataSet();
			IMetaData dataSetMetadata = dataSet.getMetadata();

			spillFile = Files.createTempFile("excel-export-", ".tsv");

			// Phase 1: DB read -> TSV spill (holds pool connection only here)
			List<IFieldMetaData> filteredMetadata;
			filteredMetadata = spillToTsvAndCloseIterator(dataSet, dataSetMetadata, spillFile);

			// Phase 2: TSV -> XLSX (no DB connection held)
			writeXlsxFromTsv(exportFileOS, dataSet.getLabel(), filteredMetadata, spillFile);

			exportFileOS.flush();
		} catch (Exception e) {
			String msg = String.format("Error writing data file `%s`\\!", getDataFile());
			LOGGER.error(msg, e);
			throw new JobExecutionException(msg, e);
		} finally {
			try {
				if (exportFileOS != null) exportFileOS.close();
			} catch (IOException ignored) {}

			if (spillFile != null) {
				try {
					Files.deleteIfExists(spillFile);
				} catch (IOException ignored) {}
			}
		}
	}

	private List<IFieldMetaData> spillToTsvAndCloseIterator(IDataSet dataSet, IMetaData dataSetMetadata, Path spillFile) throws Exception {
		DataIterator iterator = null;
		try {
			iterator = dataSet.iterator();

			// Determine SQL column order once
			ResultSetMetaData rsmd = ((ResultSetIterator) iterator).getRs().getMetaData();
			List<IFieldMetaData> filteredMetadata = new ArrayList<>();
			for (int c = 1; c <= rsmd.getColumnCount(); c++) {
				String columnName = rsmd.getColumnName(c);
				for (IFieldMetaData m : dataSetMetadata.getFieldsMeta()) {
					if (m.getName() != null && m.getName().equalsIgnoreCase(columnName)) {
						filteredMetadata.add(m);
						break;
					}
				}
			}

			// Spill rows quickly
			try (BufferedWriter w = Files.newBufferedWriter(spillFile, StandardCharsets.UTF_8)) {
				while (iterator.hasNext()) {
					IRecord record = iterator.next();
					int cols = Math.min(record.getFields().size(), filteredMetadata.size());

					for (int k = 0; k < cols; k++) {
						Object v = record.getFieldAt(k).getValue();
						if (k > 0) w.write('\t');
						if (v != null) {
							// Encode tabs/newlines safely
							String s = v.toString();
							s = s.replace("\\r", " ").replace("\\n", " ").replace("\\t", " ");
							w.write(s);
						}
					}
					w.write('\n');
				}
			}

			return filteredMetadata;
		} finally {
			// Critical: release pool connection/cursor as soon as spill is done
			closeQuietly(iterator);
		}
	}

	private void writeXlsxFromTsv(OutputStream out, String sheetName, List<IFieldMetaData> meta, Path spillFile) throws Exception {
		try (SXSSFWorkbook wb = new SXSSFWorkbook(100)) {
			wb.setCompressTempFiles(true);

			Sheet sheet = wb.createSheet(sheetName);
			if (sheet instanceof SXSSFSheet) ((SXSSFSheet) sheet).setRandomAccessWindowSize(100);

			CreationHelper createHelper = wb.getCreationHelper();

			CellStyle tsCellStyle = wb.createCellStyle();
			tsCellStyle.setDataFormat(createHelper.createDataFormat().getFormat(TIMESTAMP_FORMAT));

			CellStyle dateCellStyle = wb.createCellStyle();
			dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat(DATE_FORMAT));

			CellStyle intCellStyle = wb.createCellStyle();
			intCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("0"));

			CellStyle decimalCellStyle = wb.createCellStyle();
			decimalCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("#,##0.00"));

			this.imageB64 = OrganizationImageManager.getOrganizationB64ImageWide(TenantManager.getTenant().getName());

			int headerIndex = createBrandedHeaderSheet(
					sheet,
					this.imageB64,
					0,
					35,
					2,
					0,
					25,
					2,
					10,
					10,
					this.documentName,
					sheet.getSheetName()
			);

			// Header
			Row header = sheet.createRow(headerIndex + 1);
			for (int c = 0; c < meta.size(); c++) {
				Cell cell = header.createCell(c);
				cell.setCellValue(meta.get(c).getAlias());
			}

			final int recordLimit = SpreadsheetVersion.EXCEL2007.getLastRowIndex();
			int outRow = headerIndex + 2;
			int flushEvery = 500;

			try (BufferedReader r = Files.newBufferedReader(spillFile, StandardCharsets.UTF_8)) {
				String line;
				while ((line = r.readLine()) != null && outRow < recordLimit) {
					Row row = sheet.createRow(outRow);
					String[] parts = line.split("\\t", -1);

					int cols = Math.min(parts.length, meta.size());
					for (int k = 0; k < cols; k++) {
						IFieldMetaData fmd = meta.get(k);
						Class<?> clazz = fmd.getType();
						String raw = parts[k];

						Cell cell = row.createCell(k);
						if (raw == null || raw.isEmpty()) continue;

						// Best-effort typing; avoids DB connection entirely
						if (Timestamp.class.isAssignableFrom(clazz)) {
							// Leave as text unless you have an agreed serialization format in spill
							cell.setCellValue(raw);
							cell.setCellStyle(tsCellStyle);
						} else if (Date.class.isAssignableFrom(clazz)) {
							cell.setCellValue(raw);
							cell.setCellStyle(dateCellStyle);
						} else if (Integer.class.isAssignableFrom(clazz) || Long.class.isAssignableFrom(clazz)) {
							try {
								cell.setCellValue(Long.parseLong(raw));
								cell.setCellStyle(intCellStyle);
							} catch (NumberFormatException nfe) {
								cell.setCellValue(raw);
							}
						} else if (Double.class.isAssignableFrom(clazz) || Float.class.isAssignableFrom(clazz) || BigDecimal.class.isAssignableFrom(clazz)) {
							try {
								cell.setCellValue(Double.parseDouble(raw));
								cell.setCellStyle(decimalCellStyle);
							} catch (NumberFormatException nfe) {
								cell.setCellValue(raw);
							}
						} else {
							cell.setCellValue(raw);
						}
					}

					if (sheet instanceof SXSSFSheet && (outRow % flushEvery == 0)) {
						((SXSSFSheet) sheet).flushRows(flushEvery);
					}
					outRow++;
				}
			}

			wb.write(out);
		}
	}

	private void closeQuietly(Object it) {
		if (it == null) return;
		try {
			if (it instanceof AutoCloseable) ((AutoCloseable) it).close();
		} catch (Exception ex) {
			LOGGER.warn("Error while closing connection", ex);
		}
	}

	@Override
	protected String extension() { return "xlsx"; }

	@Override
	protected String mime() { return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"; }
}
