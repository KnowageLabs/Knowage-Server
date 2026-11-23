package it.eng.spagobi.api.v2.export;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import it.eng.spagobi.tools.dataset.bo.IDataSet;

public class ExcelExportJobTest {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Test
	public void sanitizesCustomNamesForDownloadsAndWorksheets() {
		assertEquals("Sales_2026__North_",
				AbstractExportJob.normalizeFileName(" Sales/2026:[North]. "));
		assertEquals("Sales__2026", AbstractExportJob.normalizeFileName("Sales\r\n2026"));
		assertEquals("_Sales_", AbstractExportJob.normalizeFileName("'Sales'"));
		assertEquals("_CON", AbstractExportJob.normalizeFileName("CON"));
		assertNull(AbstractExportJob.normalizeFileName("  "));
		assertNull(AbstractExportJob.normalizeFileName("..."));
	}

	@Test
	public void usesTheResolvedNameForTheDownloadWorksheetAndA1WithOrWithoutBranding() throws Exception {
		String name = "Sales 2026 EMEA detailed dataset export";
		for (String image : new String[] { "", "branding" }) {
			TestExportJob job = exportJob(name, image);

			try (XSSFWorkbook workbook = writeWorkbook(job)) {
				Sheet sheet = workbook.getSheetAt(0);
				assertEquals(name + ".xlsx", job.getExportFileName() + "." + job.extension());
				assertEquals(name.substring(0, 31), sheet.getSheetName());
				assertEquals(name, sheet.getRow(0).getCell(0).getStringCellValue());
				assertEquals(image.isEmpty() ? -1 : 1, job.brandingStartRow);
			}
		}
	}

	@Test
	public void keepsDatasetNamesAndTheExistingBrandedHeaderWhenNoNameIsConfigured() throws Exception {
		TestExportJob job = exportJob(null, "branding");

		try (XSSFWorkbook workbook = writeWorkbook(job)) {
			assertEquals("Dataset name.xlsx", job.getExportFileName() + "." + job.extension());
			assertEquals("Dataset label", workbook.getSheetName(0));
			assertEquals("Dataset label", workbook.getSheetAt(0).getRow(2).getCell(0).getStringCellValue());
			assertEquals(0, job.brandingStartRow);
		}
	}

	private TestExportJob exportJob(String name, String image) throws Exception {
		TestExportJob job = new TestExportJob();
		IDataSet dataset = (IDataSet) Proxy.newProxyInstance(IDataSet.class.getClassLoader(),
				new Class<?>[] { IDataSet.class }, (proxy, method, args) -> {
					if ("getName".equals(method.getName())) {
						return "Dataset name";
					}
					if ("getLabel".equals(method.getName())) {
						return "Dataset label";
					}
					throw new AssertionError("Unexpected dataset call: " + method.getName());
				});
		setField(AbstractExportJob.class, job, "dataSet", dataset);
		setField(AbstractExportJob.class, job, "exportFileName", AbstractExportJob.normalizeFileName(name));
		setField(ExcelExportJob.class, job, "imageB64", image);
		return job;
	}

	private XSSFWorkbook writeWorkbook(TestExportJob job) throws Exception {
		Path spillFile = temporaryFolder.newFile().toPath();
		Method write = ExcelExportJob.class.getDeclaredMethod("writeXlsxFromTsv",
				java.io.OutputStream.class, String.class, List.class, Path.class);
		write.setAccessible(true);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		write.invoke(job, output, job.getExportTitle(), Collections.emptyList(), spillFile);
		return new XSSFWorkbook(new ByteArrayInputStream(output.toByteArray()));
	}

	private void setField(Class<?> type, Object target, String name, Object value) throws Exception {
		Field field = type.getDeclaredField(name);
		field.setAccessible(true);
		field.set(target, value);
	}

	private static class TestExportJob extends ExcelExportJob {
		private int brandingStartRow = -1;

		@Override
		public void drawBrandendHeaderImage(Sheet sheet, String imageB64, int pictureType, int startCol,
				int startRow, int colspan, int rowspan) {
			brandingStartRow = startRow;
		}
	}
}
