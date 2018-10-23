package it.eng.spagobi.tools.dataset.common.datareader;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.EnumMap;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelDataReaderFactory {

	private static transient Logger logger = Logger.getLogger(ExcelDataReaderFactory.class);

	private EnumMap<ExcelFileTypes, Class<? extends Workbook>> mapping = new EnumMap<>(ExcelFileTypes.class);

	public ExcelDataReaderFactory() {
		mapping.put(ExcelFileTypes.XLS, HSSFWorkbook.class);
		mapping.put(ExcelFileTypes.XLSX, XSSFWorkbook.class);
	}

	public Workbook getWorkookInstance(String fileType, InputStream inputDataStream)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<? extends Workbook> definition = mapping.get(ExcelFileTypes.valueOf(fileType));
		Constructor<? extends Workbook> constructor = null;
		try {
			constructor = definition.getConstructor(new Class[] { InputStream.class });
		} catch (IllegalArgumentException ex) {
			logger.error("Can not create object for file type: " + fileType);
			throw ex;
		} catch (Exception e) {
			logger.error("Something goes wrong with instantiating POI library classes for file extension: " + fileType);
		}
		return constructor.newInstance(inputDataStream);
	}

}
