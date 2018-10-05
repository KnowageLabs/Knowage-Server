/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.engines.birt.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import it.eng.spagobi.engines.birt.BirtReportServlet;

public class Utils {

	protected static Logger logger = Logger.getLogger(Utils.class);

	/**
	 * Resolve system properties.
	 *
	 * @param logDir
	 *            the log dir
	 *
	 * @return the string
	 */
	public static String resolveSystemProperties(String logDir) {
		if (logDir == null)
			return null;
		int startIndex = logDir.indexOf("${");
		if (startIndex == -1)
			return logDir;
		else
			return resolveSystemProperties(logDir, startIndex);
	}

	/**
	 * Resolve system properties.
	 *
	 * @param logDir
	 *            the log dir
	 * @param startIndex
	 *            the start index
	 *
	 * @return the string
	 */
	public static String resolveSystemProperties(String logDir, int startIndex) {
		if (logDir == null)
			return logDir;
		int endIndex = -1;
		if (logDir.indexOf("${", startIndex) != -1) {
			int beginIndex = logDir.indexOf("${", startIndex);
			endIndex = logDir.indexOf("}", beginIndex);
			if (endIndex != -1) {
				String sysPropertyName = logDir.substring(beginIndex + 2, endIndex);
				String sysPropertyValue = System.getProperty(sysPropertyName);
				if (sysPropertyValue != null) {
					logDir = logDir.replace("${" + sysPropertyName + "}", sysPropertyValue);
				}
			}
		}
		if (endIndex != -1) {
			if (logDir.indexOf("${", endIndex) != -1) {
				logDir = resolveSystemProperties(logDir, endIndex);
			}
		}
		return logDir;
	}

	public static void sendPage(HttpServletResponse response, int pageNumber, String reportExecutionId) {
		ServletOutputStream ouputStream = null;
		InputStream fis = null;
		File htmlFile = null;
		String completeImageFileName = null;
		String mimeType = "text/html";

		htmlFile = new File(BirtReportServlet.OUTPUT_FOLDER + File.separator + reportExecutionId, BirtReportServlet.PAGE_FILE_NAME + pageNumber + ".html");

		// file path traversal security check
		if (!((htmlFile.getParentFile().getParent() + File.separator).equals(BirtReportServlet.OUTPUT_FOLDER))) {
			logger.error("Security exception: parent folder " + htmlFile.getParent() + " is not equal to expected folder " + BirtReportServlet.OUTPUT_FOLDER);
			throw new RuntimeException(
					"Security exception: parent folder " + htmlFile.getParent() + " is not equal to expected folder " + BirtReportServlet.OUTPUT_FOLDER);
		}

		try {
			fis = new FileInputStream(htmlFile);
		} catch (FileNotFoundException e) {
			logger.warn(completeImageFileName + " file not found, probably end of the file");
			return;
			// throw new RuntimeException("File [" + completeImageFileName + "] not found.", e);
		}

		try {

			ouputStream = response.getOutputStream();

			response.setContentType(mimeType);
			response.setHeader("Content-Type", mimeType);

			byte[] buffer = new byte[1024];
			int len;
			while ((len = fis.read(buffer)) >= 0)
				ouputStream.write(buffer, 0, len);

		} catch (Exception e) {
			logger.error("Error writing image into servlet output stream", e);
		} finally {
			if (fis != null)
				try {
					fis.close();
				} catch (IOException e) {
					logger.error("Error while closing FileInputStream on file " + completeImageFileName, e);
				}
			if (ouputStream != null) {
				try {
					ouputStream.flush();
					ouputStream.close();
				} catch (IOException e) {
					logger.error("Error flushing servlet output stream", e);
				}
			}
			// if (imageFile != null && imageFile.exists() && imageFile.isFile()) {
			// imageFile.delete();
			// }
		}

	}
}
