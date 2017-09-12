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
package it.eng.spagobi.engines.jasperreport;

import it.eng.spago.base.SourceBean;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spagobi.engines.jasperreport.exporters.JRImageBase64Exporter;
import it.eng.spagobi.engines.jasperreport.exporters.JRJpegExporter;
import it.eng.spagobi.services.common.EnginConf;
import it.eng.spagobi.utilities.assertion.Assert;

import java.io.File;

import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRVirtualizer;
import net.sf.jasperreports.engine.export.JExcelApiExporter;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.JRTextExporter;
import net.sf.jasperreports.engine.export.JRXmlExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer;
import net.sf.jasperreports.engine.util.JRSwapFile;

import org.apache.log4j.Logger;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class JasperReportEngineConfig {

	private EnginConf engineConfig;

	private static transient Logger logger = Logger.getLogger(JasperReportEngineConfig.class);

	// -- singleton pattern --------------------------------------------
	private static JasperReportEngineConfig instance;

	public static JasperReportEngineConfig getInstance() {
		if (instance == null) {
			instance = new JasperReportEngineConfig();
		}
		return instance;
	}

	private JasperReportEngineConfig() {
		setEngineConfig(EnginConf.getInstance());
	}

	// -- singleton pattern --------------------------------------------

	// -- JasperReportEngine Conf Access methods ------------------------

	private File getwebappRootDir() {
		File webinfDir = new File(ConfigSingleton.getRootPath());
		return webinfDir.getParentFile();
	}

	public File getLibDir() {
		File libDir;

		libDir = new File(getwebappRootDir(), "WEB-INF");
		libDir = new File(libDir, "lib");

		return libDir;
	}

	public File getEngineResourceDir() {
		File resourceDir;

		resourceDir = null;
		if (getEngineConfig().getResourcePath() != null) {
			resourceDir = new File(getEngineConfig().getResourcePath(), "jasper_messages");
		} else {
			resourceDir = new File(getwebappRootDir(), "resources");
			resourceDir = new File(resourceDir, "jasper_messages");
		}
		resourceDir.mkdirs();

		return resourceDir;
	}

	public File getTempDir() {

		File tempDir;

		String configuredTempDir = (String) getConfigSourceBean().getAttribute("GENERALSETTINGS.tmpdir");
		logger.debug("Configured temp dir is [" + configuredTempDir + "]");
		File configuredTempDirFile = new File(configuredTempDir);

		if (configuredTempDirFile.isAbsolute()) {
			tempDir = configuredTempDirFile;
		} else {
			logger.debug("Configured temp dir is recognized as relative");
			String javaIoTmpDir = System.getProperty("java.io.tmpdir");
			logger.debug("java.io.tmpdir is [" + javaIoTmpDir + "]");
			tempDir = new File(javaIoTmpDir, configuredTempDir);
		}

		logger.debug("Temporary directory is [" + tempDir.getAbsolutePath() + "]");

		tempDir.mkdirs();

		return tempDir;
	}

	public File getReportOutputDir() {
		File reportOutputDir;

		reportOutputDir = new File(getTempDir(), "reports");
		reportOutputDir.mkdirs();
		reportOutputDir.mkdirs();

		return reportOutputDir;
	}

	public boolean isVirtualizationEbabled() {
		boolean isVirtualizationActive = false;
		String active = (String) getConfigSourceBean().getAttribute("VIRTUALIZER.active");
		if (active != null)
			isVirtualizationActive = active.equalsIgnoreCase("true");
		return isVirtualizationActive;
	}

	public JRVirtualizer getVirtualizer() {

		JRSwapFileVirtualizer virtualizer;
		int maxSize = 2;

		String maxSizeStr = (String) getConfigSourceBean().getAttribute("VIRTUALIZER.maxSize");
		if (maxSizeStr != null) {
			maxSize = Integer.parseInt(maxSizeStr);
		}

		File virtualizationDir;
		String virtualizationDirPath = (String) getConfigSourceBean().getAttribute("VIRTUALIZER.dir");
		virtualizationDir = null;

		if (virtualizationDirPath == null) {
			virtualizationDir = new File(getTempDir(), "virtualization");
		} else {
			if (!virtualizationDirPath.startsWith("/")) {
				virtualizationDir = new File(getTempDir(), virtualizationDirPath);
			} else {
				virtualizationDir = new File(virtualizationDirPath);
			}
		}

		virtualizationDir = new File(virtualizationDir, "jrcache");
		virtualizationDir.mkdirs();

		logger.debug("Max page cached during virtualization process: " + maxSize);
		logger.debug("Dir used as storing area during virtualization: " + virtualizationDir);

		JRSwapFile swapFile = new JRSwapFile(virtualizationDir.getAbsolutePath(), maxSize, maxSize);
		virtualizer = new JRSwapFileVirtualizer(maxSize, swapFile);
		virtualizer.setReadOnly(false);

		return virtualizer;
	}

	public JRExporter getExporter(String format) {
		JRExporter exporter = null;

		Assert.assertNotNull(format, "Input parameter [format] cennot be null");

		SourceBean exporterConfig = (SourceBean) getConfigSourceBean().getFilteredSourceBeanAttribute("EXPORTERS.EXPORTER", "format", format);
		if (exporterConfig != null) {
			String exporterClassName = (String) exporterConfig.getAttribute("class");
			if (exporterClassName != null) {

				try {
					exporter = (JRExporter) Class.forName(exporterClassName).newInstance();
				} catch (Throwable t) {
					throw new JasperReportEngineRuntimeException("Impossible to instatiate exporter", t);
				}
			}
		}

		if (exporter == null) {
			if (format.equalsIgnoreCase("csv"))
				exporter = new JRCsvExporter();
			else if (format.equalsIgnoreCase("html"))
				exporter = new JRHtmlExporter();
			else if (format.equalsIgnoreCase("xls"))
				exporter = new JExcelApiExporter(); // exporter = new JRXlsExporter();
			else if (format.equalsIgnoreCase("xlsx"))
				exporter = new JRXlsxExporter();
			else if (format.equalsIgnoreCase("rtf"))
				exporter = new JRRtfExporter();
			else if (format.equalsIgnoreCase("xml"))
				exporter = new JRXmlExporter();
			else if (format.equalsIgnoreCase("txt"))
				exporter = new JRTextExporter();
			else if (format.equalsIgnoreCase("pdf"))
				exporter = new JRPdfExporter();
			else if (format.equalsIgnoreCase("JPG"))
				exporter = new JRJpegExporter();
			else if (format.equalsIgnoreCase("JPGBASE64"))
				exporter = new JRImageBase64Exporter();
			else
				exporter = new JRHtmlExporter();
		}

		return exporter;
	}

	public String getMIMEType(String format) {
		String mimeType = null;
		SourceBean exporterConfig = (SourceBean) getConfigSourceBean().getFilteredSourceBeanAttribute("EXPORTERS.EXPORTER", "format", format);
		if (exporterConfig != null) {
			mimeType = (String) exporterConfig.getAttribute("mime");
		}

		if (mimeType == null) {
			if (format.equalsIgnoreCase("csv"))
				mimeType = "text/plain";
			else if (format.equalsIgnoreCase("html"))
				mimeType = "text/html";
			else if (format.equalsIgnoreCase("xls"))
				mimeType = "application/vnd.ms-excel";
			else if (format.equalsIgnoreCase("xlsx"))
				mimeType = "xlsx=application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
			else if (format.equalsIgnoreCase("rtf"))
				mimeType = "application/rtf";
			else if (format.equalsIgnoreCase("xml"))
				mimeType = "text/xml";
			else if (format.equalsIgnoreCase("txt"))
				mimeType = "text/plain";
			else if (format.equalsIgnoreCase("pdf"))
				mimeType = "application/pdf";
			else if (format.equalsIgnoreCase("JPG"))
				mimeType = "application/jpeg";
			else if (format.equalsIgnoreCase("JPGBASE64"))
				mimeType = "text/plain";
			else
				mimeType = "text/html";
		}

		return mimeType;
	}

	public String getDefaultOutputType() {
		String defaultType = null;
		defaultType = (String) getConfigSourceBean().getAttribute("EXPORTERS.default");
		if (defaultType == null)
			defaultType = "html";
		return defaultType;
	}

	// -- ACCESS Methods -----------------------------------------------
	public EnginConf getEngineConfig() {
		return engineConfig;
	}

	private void setEngineConfig(EnginConf engineConfig) {
		this.engineConfig = engineConfig;
	}

	public SourceBean getConfigSourceBean() {
		return getEngineConfig().getConfig();
	}
}
