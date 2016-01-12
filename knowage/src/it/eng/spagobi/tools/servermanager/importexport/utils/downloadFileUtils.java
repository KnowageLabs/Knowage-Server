package it.eng.spagobi.tools.servermanager.importexport.utils;

import it.eng.spago.validation.EMFValidationError;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.tools.importexport.ExportUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class downloadFileUtils {

	static protected Logger logger = Logger.getLogger(downloadFileUtils.class);

	/**
	 * Handle a download request of an eported file. Reads the file, sends it as an http response attachment and in the end deletes the file.
	 */
	public static byte[] manageDownloadExportFile(String exportFileName, HttpServletResponse response) {
		logger.debug("IN");
		try {
			String folderPath = ExportUtilities.getExportTempFolderPath();
			String fileExtension = "zip";
			return manageDownload(exportFileName, fileExtension, folderPath, response, true);
		} catch (Exception e) {
			logger.error("Error while downloading export file", e);
		} finally {
			logger.debug("OUT");
		}
		return null;
	}

	/**
	 * Handle a download request of an eported file. Reads the file, sends it as an http response attachment and in the end deletes the file.
	 *
	 * @throws EMFValidationError
	 */
	private static byte[] manageDownload(String fileName, String fileExtension, String folderPath, HttpServletResponse response, boolean deleteFile)
			throws EMFValidationError {
		logger.debug("IN");
		byte[] exportContent = "".getBytes();
		try {
			File exportedFile = new File(folderPath + "/" + fileName + "." + fileExtension);
			if (!exportedFile.exists()) {
				logger.error("File not found");
				return null;
			}
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(exportedFile);
				exportContent = GeneralUtilities.getByteArrayFromInputStream(fis);
			} catch (IOException ioe) {
				logger.error("Cannot get bytes of the exported file", ioe);
			}
			response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "." + fileExtension + "\";");
			response.setContentLength(exportContent.length);
			// response.getOutputStream().write(exportContent);
			// response.getOutputStream().flush();
			if (fis != null)
				fis.close();
			if (deleteFile) {
				exportedFile.delete();
			}
		} catch (IOException ioe) {
			logger.error("Cannot flush response", ioe);
			return null;
		} finally {
			logger.debug("OUT");
		}
		return exportContent;
	}
}
