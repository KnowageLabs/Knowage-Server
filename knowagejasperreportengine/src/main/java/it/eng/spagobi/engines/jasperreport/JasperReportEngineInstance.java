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

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.engines.jasperreport.datasource.JRSpagoBIDataStoreDataSource;
import it.eng.spagobi.engines.jasperreport.exporters.JRImageExporterParameter;
import it.eng.spagobi.services.common.EnginConf;
import it.eng.spagobi.services.content.bo.Content;
import it.eng.spagobi.services.proxy.ContentServiceProxy;
import it.eng.spagobi.services.proxy.DataSetServiceProxy;
import it.eng.spagobi.services.proxy.EventServiceProxy;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.DynamicClassLoader;
import it.eng.spagobi.utilities.ParametersDecoder;
import it.eng.spagobi.utilities.ResourceClassLoader;
import it.eng.spagobi.utilities.SpagoBIAccessUtils;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.AbstractEngineInstance;
import it.eng.spagobi.utilities.engines.AuditServiceProxy;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.IEngineAnalysisState;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.servlet.http.HttpServletRequest;

import net.sf.jasperreports.engine.JRDataset;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRVirtualizer;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import net.sf.jasperreports.engine.export.JRTextExporterParameter;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.apache.log4j.Logger;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

import sun.misc.BASE64Decoder;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * @authors Andrea Gioia (andrea.gioia@eng.it) Davide Zerbetto (davide.zerbetto@eng.it)
 */
public class JasperReportEngineInstance extends AbstractEngineInstance {

	JasperReportEngineTemplate template;
	String outputType;
	JRExporter exporter;
	boolean virtualizationEnabled;
	JRVirtualizer virtualizer;
	File libDir;
	File workingDir;

	private DataSetServiceProxy dsProxy;
	public static final String JS_FILE_ZIP = "JS_File";
	public static final String JS_DIR = "JS_dir";
	public static final String JS_EXT_ZIP = ".zip";

	private static transient Logger logger = Logger.getLogger(JasperReportEngineInstance.class);

	public JasperReportEngineInstance(JasperReportEngineTemplate template, Map env, DataSetServiceProxy dsProxy) {
		super(env);
		Assert.assertNotNull(env, "[env] parameter cannot be null in order to properly initialize a new JasperReportEngineInstance");
		this.template = template;
		Assert.assertNotNull(env, "[template] parameter cannot be null in order to properly initialize a new JasperReportEngineInstance");
		this.dsProxy = dsProxy;
	}

	public void runReport(File file, HttpServletRequest httpServletRequest) {
		logger.debug("IN");
		OutputStream out;

		out = null;
		try {
			out = new FileOutputStream(file);
			runReport(out, httpServletRequest);
		} catch (Throwable t1) {
			throw new JasperReportEngineRuntimeException("Impossible to run report", t1);
		} finally {
			logger.debug("OUT");
			try {
				if (out != null) {
					out.flush();
					out.close();
				}
			} catch (Throwable t2) {
				throw new JasperReportEngineRuntimeException("Impossible to close output stream", t2);
			}
		}
	}

	private void runReport(OutputStream out, HttpServletRequest httpServletRequest) {
		logger.debug("IN");
		Monitor monitor = MonitorFactory.start("JasperReportRunner.service");
		String prefixDirTemplate = null;
		Connection connection = null;
		try {
			Assert.assertNotNull(exporter, "exporter cannot be null");

			setJRProperties();
			setJRBuiltinParameters();

			prefixDirTemplate = (String) getEnv().get("prefixName");
			logger.debug("prefixDirTemplate:" + prefixDirTemplate);
			InputStream is = template.open(getCacheDir(prefixDirTemplate));

			logger.debug("Getting Jasper Design from template file ...");
			JasperDesign jasperDesign = JRXmlLoader.load(is);

			// get datasets
			List<JRDataset> datasets = jasperDesign.getDatasetsList();

			for (int h = 0; h < datasets.size(); h++) {
				JRDataset designDataset = datasets.get(h);
				String datasetName = designDataset.getName();
				// get document's dataset
				IDataSet dataset = this.dsProxy.getDataSetByLabel(datasetName);

				if (dataset != null) {
					logger.debug("Found SpagoBI dataset " + datasetName);
					// get parameter of type JRDataSource
					for (int y = 0; y < jasperDesign.getParametersList().size(); y++) {
						JRParameter parameter = jasperDesign.getParametersList().get(y);
						String paramName = parameter.getName();
						if (parameter.getValueClassName().equals("net.sf.jasperreports.engine.JRDataSource") && paramName.equals(datasetName)) {// &&
																																				// parameter.getName().equals(dataset.getLabel()
							// set dataset query value
							dataset.loadData();
							IDataStore dstore = dataset.getDataStore();
							JRSpagoBIDataStoreDataSource dataSource = new JRSpagoBIDataStoreDataSource(dstore);

							getEnv().put(paramName, dataSource);
							logger.debug("set parameter" + paramName + " value");
						}
					}
				}
			}

			logger.debug("Compiling template file ...");
			Monitor monitorCompileTemplate = MonitorFactory.start("JasperReportRunner.compileTemplate");
			JasperReport report = JasperCompileManager.compileReport(jasperDesign);

			monitorCompileTemplate.stop();
			logger.debug("Template file compiled succesfully");

			adaptReportParams(report);
			setupLocale();

			Monitor monitorSubReport = MonitorFactory.start("JasperReportRunner.compileSubReport");
			File[] compiledSubreports = compileSubreports();
			monitorSubReport.stop();
			ClassLoader previous = Thread.currentThread().getContextClassLoader();
			ClassLoader current = URLClassLoader.newInstance(new URL[] { getCacheDir(prefixDirTemplate).toURL() }, previous);
			Thread.currentThread().setContextClassLoader(current);

			logger.debug("Filling report ...");
			Monitor monitorFillingReport = MonitorFactory.start("JasperReportRunner.FillingReport");
			JasperPrint jasperPrint = null;
			if (getDataSet() != null) {
				logger.debug("... using dataset [" + getDataSet().getName() + "]");
				getDataSet().setParamsMap(getEnv());
				getDataSet().loadData();
				for (int i = 0; i < getDataSet().getDataStore().getMetaData().getFieldCount(); i++) {
					logger.debug("Dataset column [" + (i + 1) + "] name is equal to [" + getDataSet().getDataStore().getMetaData().getFieldAlias(i) + "]");
				}

				JRSpagoBIDataStoreDataSource dataSource = new JRSpagoBIDataStoreDataSource(getDataSet().getDataStore());
				jasperPrint = JasperFillManager.fillReport(report, getEnv(), dataSource);
			} else {
				logger.debug("... using datasource [" + getDataSource().getLabel() + "]");
				connection = getConnection();
				jasperPrint = JasperFillManager.fillReport(report, getEnv(), connection);
			}

			monitorFillingReport.stop();
			logger.debug("Report filled succesfully");

			logger.debug("Exporting report ...");
			Monitor monitorExportReport = MonitorFactory.start("JasperReportRunner.ExportReport");

			if (outputType.equalsIgnoreCase("html")) {
				exporter.setParameter(JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN, Boolean.FALSE);
				exporter.setParameter(JRHtmlExporterParameter.BETWEEN_PAGES_HTML, "");
				HashMap m_imagesMap = new HashMap();
				UUIDGenerator uuidGen = UUIDGenerator.getInstance();
				UUID uuid_local = uuidGen.generateTimeBasedUUID();
				String mapName = uuid_local.toString();
				httpServletRequest.getSession().setAttribute(mapName, m_imagesMap);
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_MAP, m_imagesMap);
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI, "JRImageServlet?mapname=" + mapName + "&image=");
			} else if (outputType.equalsIgnoreCase("txt")) {
				exporter.setParameter(JRTextExporterParameter.PAGE_HEIGHT, new Integer(100));
				exporter.setParameter(JRTextExporterParameter.PAGE_WIDTH, new Integer(100));
			} else if (outputType.equalsIgnoreCase("JPG")) {
				exporter.setParameter(JRImageExporterParameter.JASPER_REPORT, report);
			} else if (outputType.equalsIgnoreCase("JPGBASE64")) {
				exporter.setParameter(JRImageExporterParameter.JASPER_REPORT, report);
			}

			exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
			exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
			exporter.exportReport();

			monitorExportReport.stop();
			logger.debug("Report exported succesfully");

		} catch (Throwable e) {
			throw new JasperReportEngineRuntimeException("Impossible to run report", e);
		} finally {
			try {
				if (connection != null && !connection.isClosed()) {
					connection.close();
				}
			} catch (SQLException sqle) {
				logger.error("Error closing connection", sqle);
			}
			try {
				File tmpDir = getCacheDir(prefixDirTemplate);
				String[] files = tmpDir.list();
				if (files.length == 0) {
					SpagoBIAccessUtils util = new SpagoBIAccessUtils();
					util.deleteDirectory(tmpDir);
					logger.debug("Delating temporary directory: " + tmpDir);
				}
			} catch (Throwable t) {
				logger.error("Error while deleting cache dir content", t);
			}
			monitor.stop();
			logger.debug("OUT");

		}

	}

	private static final String JR_PROPERTY_COMPILE_DIR = "jasper.reports.compile.temp";
	private static final String JR_PROPERTY_CLASSPATH = "jasper.reports.compile.class.path";

	private void setJRProperties() {
		System.setProperty(JR_PROPERTY_COMPILE_DIR, getWorkingDir().getAbsolutePath());
		logger.debug("Set [" + JR_PROPERTY_COMPILE_DIR + "] property to value [" + System.getProperty(JR_PROPERTY_COMPILE_DIR) + "]");

		System.setProperty(JR_PROPERTY_CLASSPATH, buildJRClasspathValue());
		logger.debug("Set [" + JR_PROPERTY_CLASSPATH + "] property to value [" + System.getProperty(JR_PROPERTY_CLASSPATH) + "]");
	}

	public void setJRBuiltinParameters() {
		String resourcePath;
		String entity;

		resourcePath = EnginConf.getInstance().getResourcePath() + "/img/";
		entity = (String) getEnv().get(SpagoBIConstants.SBI_ENTITY);
		if (entity != null && entity.length() > 0) {
			resourcePath = resourcePath.concat(entity + "/");
		}
		getEnv().put("SBI_RESOURCE_PATH", resourcePath);

		// Create the virtualizer
		if (isVirtualizationEnabled()) {
			logger.debug("Virtualization of fill process is active");
			getEnv().put(JRParameter.REPORT_VIRTUALIZER, getVirtualizer());
		}
	}

	private File getCacheDir(String prefixTemplate) {
		File jrTempDir;

		jrTempDir = new File(getWorkingDir(), "reports");
		jrTempDir = new File(jrTempDir, JS_DIR + "__" + prefixTemplate);
		jrTempDir.mkdirs();

		return jrTempDir;
	}

	// ///////////////////////////////////////
	// UTILITY METHODS
	// ///////////////////////////////////////

	/**
	 * Build a classpath variable appending all the jar files founded into the specified directory.
	 *
	 * @param libDir
	 *            JR lib-dir to scan for find jar files to include into the classpath variable
	 * @return the classpath used by JasperReprorts Engine (by default equals to WEB-INF/lib)
	 */
	private String buildJRClasspathValue() {
		logger.debug("IN");
		String getJRClasspathValue = null;

		logger.debug("Reading jar files from lib-dir...");
		StringBuffer jasperReportClassPathStringBuffer = new StringBuffer();

		String fileToAppend = null;
		if (getLibDir().isDirectory()) {
			String[] jarFiles = getLibDir().list();
			for (int i = 0; i < jarFiles.length; i++) {
				String namefile = jarFiles[i];
				if (!namefile.endsWith("jar"))
					continue; // the inclusion of txt files causes problems
				fileToAppend = libDir + System.getProperty("file.separator") + jarFiles[i];
				logger.debug("Appending jar file [" + fileToAppend + "] to JasperReports classpath");
				jasperReportClassPathStringBuffer.append(fileToAppend);
				jasperReportClassPathStringBuffer.append(System.getProperty("path.separator"));
			}
		}

		getJRClasspathValue = jasperReportClassPathStringBuffer.toString();
		getJRClasspathValue = getJRClasspathValue.substring(0, getJRClasspathValue.length() - 1);
		logger.debug("OUT");
		return getJRClasspathValue;
	}

	void setupLocale() {
		logger.debug("IN");
		Locale locale;
		String language;
		String country;
		ResourceBundle resourceBoundle;

		language = (String) getEnv().get("SBI_LANGUAGE");
		country = (String) getEnv().get("SBI_COUNTRY");

		if (language != null && country != null) {

			logger.debug("Internazionalization in " + language);
			locale = new Locale(language, country, "");
			getEnv().put("REPORT_LOCALE", locale);

			if (!template.isPropertiesLoaded()) {
				logger.debug("Properties are loaded");
				File resourceDir = JasperReportEngine.getConfig().getEngineResourceDir();

				logger.debug("Root dir of ResourceClassLoader has been set to [" + resourceDir.getAbsolutePath() + "]");

				ClassLoader previous = Thread.currentThread().getContextClassLoader();
				ResourceClassLoader dcl = new ResourceClassLoader(resourceDir.getAbsolutePath(), previous);
				try {
					resourceBoundle = PropertyResourceBundle.getBundle("messages", locale, dcl);
					getEnv().put("REPORT_RESOURCE_BUNDLE", resourceBoundle);
				} catch (Exception e) {
					logger.warn("could not find properties message", e);
				}
			}
		}
		logger.debug("OUT");
	}

	void storeLocale(File pathMasterID) {
		logger.debug("IN");
		Locale locale;
		String language;
		String country;
		ResourceBundle resourceBoundle;

		language = (String) getEnv().get("SBI_LANGUAGE");
		country = (String) getEnv().get("SBI_COUNTRY");

		if (language != null && country != null) {

			logger.debug("Internazionalization in " + language);
			locale = new Locale(language, country, "");

			File resourceDir = JasperReportEngine.getConfig().getEngineResourceDir();
			File source = new File(resourceDir + System.getProperty("file.separator") + "messages_" + language + "_" + country + ".properties");
			if (source.exists() && !source.isDirectory() && source.length() != 0) {
				File target = new File(pathMasterID + System.getProperty("file.separator") + "messages_" + language + "_" + country + ".properties");
				try {
					Files.copy(source.toPath(), target.toPath(), REPLACE_EXISTING);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			logger.debug("Properties are copied to [" + pathMasterID + "]");
		}
	}

	Map adaptReportParams(JasperReport report) {
		logger.debug("IN");

		String dateformat = (String) getEnv().get("dateformat");
		if (dateformat != null) {
			dateformat = dateformat.replaceAll("D", "d");
			dateformat = dateformat.replaceAll("m", "M");
			dateformat = dateformat.replaceAll("Y", "y");
		}

		JRParameter[] reportParameters = report.getParameters();
		ParametersDecoder decoder = new ParametersDecoder();
		for (int i = 0; i < reportParameters.length; i++) {
			JRParameter parameter = reportParameters[i];

			logger.debug("Examining parameter with name [" + parameter.getName() + "] ...");

			String paramValueString = null;

			if (getEnv().get(parameter.getName()) instanceof String) {
				paramValueString = (String) getEnv().get(parameter.getName());
			}

			if (paramValueString == null) {
				logger.debug("No value found for parameter with name [" + parameter.getName() + "]");
			} else {
				logger.debug("Value found for parameter with name [" + parameter.getName() + "] is [" + paramValueString + "]");
				/*
				 * The ParameterConverter converts a single value. Multi-value parameters are assumed to contains values that are String type. If they are not
				 * Strings (list of dates, list of numbers, ...) the converter will not work.
				 */
				if (decoder.isMultiValues(paramValueString)) {
					logger.debug("Value found for parameter with name [" + parameter.getName() + "] is [" + paramValueString + "] and it is multivalue. "
							+ "Cannot adapt parameter nature");
					continue;
				}
				Class aReportParameterClass = parameter.getValueClass();
				Object newValue = ParameterConverter.convertParameter(aReportParameterClass, paramValueString, dateformat);
				if (newValue == null)
					newValue = paramValueString;

				if (!(newValue instanceof String)) {
					logger.debug("Updating parameter with name [" + parameter.getName() + "] to a " + newValue.getClass().getName() + ".");
					getEnv().put(parameter.getName(), newValue);
				}
			}
		}
		logger.debug("OUT");
		return getEnv();
	}

	// =========================================================================================================================

	private static class SubreportMeta {
		private String documentId; // 1, 2, ..., n
		private String templateType; // file | archive
		private String templateName;
		private String templateFingerprint; // documentId + templateId

		public SubreportMeta(String documentId) {
			setDocumentId(documentId);
		}

		public String getDocumentId() {
			return documentId;
		}

		public void setDocumentId(String documentId) {
			this.documentId = documentId;
		}

		public String getTemplateFingerprint() {
			return templateFingerprint;
		}

		public void setTemplateFingerprint(String templateFingerprint) {
			this.templateFingerprint = templateFingerprint;
		}

		public String getTemplateType() {
			return templateType;
		}

		public void setTemplateType(String templateType) {
			this.templateType = templateType;
		}

		public String getTemplateName() {
			return templateName;
		}

		public void setTemplateName(String templateName) {
			this.templateName = templateName;
		}
	}

	private Map<String, SubreportMeta> getSubreportsMeta() {
		Map<String, SubreportMeta> subreportsMeta;

		logger.debug("IN");
		subreportsMeta = new HashMap<String, SubreportMeta>();

		try {

			Iterator it = getEnv().keySet().iterator();
			while (it.hasNext()) {
				String parName = (String) it.next();
				if (parName.startsWith("sr") && parName.endsWith("ids")) {
					int start = parName.indexOf('.') + 1;
					int end = parName.indexOf('.', start);
					String subreportKey = parName.substring(start, end);
					String subreportIds = (String) getEnv().get(parName);
					String[] ids = subreportIds.split("_");
					SubreportMeta subreportMeta = new SubreportMeta(ids[0]);
					// subreportMeta.setTemplateName( (String)params.get("subrpt." + subreportKey + ".tempName") );
					subreportMeta.setTemplateFingerprint(subreportIds);
					// subreportMeta.setTemplateType( (String)params.get("subrpt." + subreportKey + ".flgTempStd") );
					subreportsMeta.put(subreportKey, subreportMeta);
					logger.debug("JasperReports subreport id : " + getEnv().get(parName));
				}
			}

		} catch (Throwable t) {
			logger.error("Error while extracting subreports meta", t);
		} finally {
			logger.debug("OUT");
		}

		return subreportsMeta;

	}

	private File[] compileSubreports() {

		File[] files = null;

		logger.debug("IN");
		try {
			Map<String, SubreportMeta> subreportsMeta = getSubreportsMeta();
			int subreportNum = subreportsMeta.keySet().size();

			files = new File[subreportNum];
			logger.debug("Subreports number is equal to [" + subreportNum + "]");

			Iterator it = subreportsMeta.keySet().iterator();
			int i = 0;
			while (it.hasNext()) {
				SubreportMeta subreportMeta = subreportsMeta.get(it.next());
				String masterIds = (String) getEnv().get("prefixName");

				// check if the subreport is cached into file system
				File subreportCacheDir = getCacheDir(masterIds + System.getProperty("file.separator") + subreportMeta.getTemplateFingerprint());
				logger.debug("dirTemplate is equal to [" + subreportCacheDir + "]");

				File[] compiledJRFiles = subreportCacheDir.listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						logger.debug("scan dir [" + name + "]");
						return name.endsWith(".jasper");
					}
				});
				logger.debug("found [" + compiledJRFiles.length + "] compiled files");
				if (compiledJRFiles.length > 1) {
					throw new RuntimeException("More then one compiled file found in directory [" + subreportCacheDir + "]");
				}

				if (compiledJRFiles.length == 1) {
					logger.debug("template [" + subreportMeta.getTemplateFingerprint() + "] alredy exists");
					files[i] = compiledJRFiles[0];
				} else {
					logger.debug("template [" + subreportMeta.getTemplateFingerprint() + "] does not exists yet");

					File destDir = getCacheDir(masterIds + System.getProperty("file.separator") + subreportMeta.getTemplateFingerprint());
					// File destDir = getCacheDir(masterIds);
					storeLocale(getCacheDir(masterIds));

					logger.debug("destDir number is equal to [" + destDir + "]");

					// File or directory does not exist, create a new file compiled!
					// put "true" to the parameter that not permits the validation on parameters of the subreport.
					ContentServiceProxy contentServiceProxy = (ContentServiceProxy) getEnv().get(EngineConstants.ENV_CONTENT_SERVICE_PROXY);
					HashMap requestParameters = new HashMap();
					requestParameters.put("SBI_READ_ONLY_TEMPLATE", "true");
					Content template = contentServiceProxy.readTemplate(subreportMeta.getDocumentId(), requestParameters);
					template.getFileName();
					logger.debug("Read the template.(subreport)" + template.getFileName());
					InputStream is = null;
					BASE64Decoder bASE64Decoder = new BASE64Decoder();
					byte[] templateContent = bASE64Decoder.decodeBuffer(template.getContent());
					is = new java.io.ByteArrayInputStream(templateContent);
					String str = new String(templateContent);

					SpagoBIAccessUtils util = new SpagoBIAccessUtils();

					/*
					 * Dynamic template management: if the template is a zip file it is opened and every class are added to the classpath
					 */
					String flgTemplateStandard = "true"; // = subreportMeta.getTemplateType();
					if (template.getFileName().indexOf(".zip") > -1) {
						flgTemplateStandard = "false";
					}

					if (flgTemplateStandard.equalsIgnoreCase("false")) {
						File fileZip = new File(destDir, this.JS_FILE_ZIP + i + JS_EXT_ZIP);
						FileOutputStream foZip = new FileOutputStream(fileZip);
						foZip.write(templateContent);
						foZip.close();
						util.unzip(fileZip, destDir);
						JarFile zipFile = new JarFile(fileZip);
						Enumeration totalZipEntries = zipFile.entries();
						File jarFile = null;
						while (totalZipEntries.hasMoreElements()) {
							ZipEntry entry = (ZipEntry) totalZipEntries.nextElement();
							if (entry.getName().endsWith(".jar")) {
								// set classloader with jar
								jarFile = new File(destDir + entry.getName());
								ClassLoader previous = Thread.currentThread().getContextClassLoader();
								DynamicClassLoader dcl = new DynamicClassLoader(jarFile, previous);
								// ClassLoader current = URLClassLoader.newInstance(new URL[]{jarFile.toURL()}, previous);
								Thread.currentThread().setContextClassLoader(dcl);
							}
							if (entry.getName().endsWith(".jrxml")) {
								// set InputStream with jrxml
								File jrxmlFile = new File(destDir + System.getProperty("file.separator") + entry.getName());
								InputStream isJrxml = new FileInputStream(jrxmlFile);
								templateContent = util.getByteArrayFromInputStream(isJrxml);
								is = new java.io.ByteArrayInputStream(templateContent);
							}
						}
					}

					JasperDesign jasperDesign = JRXmlLoader.load(is);

					// the following instruction is necessary because the above instruction cleans variable 'is'
					is = new java.io.ByteArrayInputStream(templateContent);

					int indPoint = template.getFileName().indexOf('.');
					files[i] = new File(destDir, template.getFileName().substring(0, indPoint) + ".jasper");
					// files[i] = new File(destDir, jasperDesign.getName() + ".jasper");
					logger.debug("Compiling template file: " + files[i]);

					FileOutputStream fos = null;
					try {
						fos = new FileOutputStream(files[i]);
					} catch (FileNotFoundException e) {
						logger.error("Internal error in compiling subreport method", e);
					}
					JasperCompileManager.compileReportToStream(is, fos);
					logger.debug("Template file compiled  succesfully");
				}
				i++;
			}

			URL[] urls = new URL[files.length];
			for (int j = 0; j < files.length; j++) {
				// adds the subreport's folder to the classpath
				urls[j] = files[j].getParentFile().toURL();
				logger.debug("Added url [" + files[j].getParentFile().toURL() + "] to classloader");
			}
			ClassLoader previous = Thread.currentThread().getContextClassLoader();
			ClassLoader current = URLClassLoader.newInstance(urls, previous);
			Thread.currentThread().setContextClassLoader(current);
		} catch (Throwable t) {
			logger.error("Error while ccompiling subreports", t);
		} finally {
			logger.debug("OUT");
		}

		return files;
	}

	public boolean isVirtualizationEnabled() {
		return virtualizationEnabled;
	}

	public void setVirtualizationEnabled(boolean virtualizationEnabled) {
		this.virtualizationEnabled = virtualizationEnabled;
	}

	public JRVirtualizer getVirtualizer() {
		return virtualizer;
	}

	public void setVirtualizer(JRVirtualizer virtualizer) {
		this.virtualizer = virtualizer;
	}

	public File getLibDir() {
		return libDir;
	}

	public void setLibDir(File libDir) {
		this.libDir = libDir;
	}

	public File getWorkingDir() {
		return workingDir;
	}

	public void setWorkingDir(File workingDir) {
		this.workingDir = workingDir;
	}

	public JRExporter getExporter() {
		return exporter;
	}

	public void setExporter(JRExporter exporter) {
		this.exporter = exporter;
	}

	public String getOutputType() {
		return outputType;
	}

	public void setOutputType(String outputType) {
		this.outputType = outputType;
	}

	// -- accessor methods ------------------------------------------------------------

	public IDataSource getDataSource() {
		return (IDataSource) this.getEnv().get(EngineConstants.ENV_DATASOURCE);
	}

	public Connection getConnection() {
		Connection conn = null;
		String schema = null;
		try {
			if (getDataSource().checkIsMultiSchema()) {
				String attrname = getDataSource().getSchemaAttribute();
				UserProfile userProfile = (UserProfile) getEnv().get(EngineConstants.ENV_USER_PROFILE);
				if (attrname != null)
					schema = (String) userProfile.getUserAttribute(attrname);
			}
		} catch (Throwable t1) {
			logger.error("Impossible to manage properly multiSchema attribute", t1);
		}

		try {
			conn = getDataSource().getConnection(schema);
		} catch (Throwable t2) {
			logger.error("Cannot retrieve connection for schema [" + schema + "]", t2);
		}

		return conn;
	}

	public IDataSet getDataSet() {
		return (IDataSet) this.getEnv().get(EngineConstants.ENV_DATASET);
	}

	public Locale getLocale() {
		return (Locale) this.getEnv().get(EngineConstants.ENV_LOCALE);
	}

	public AuditServiceProxy getAuditServiceProxy() {
		return (AuditServiceProxy) this.getEnv().get(EngineConstants.ENV_AUDIT_SERVICE_PROXY);
	}

	public EventServiceProxy getEventServiceProxy() {
		return (EventServiceProxy) this.getEnv().get(EngineConstants.ENV_EVENT_SERVICE_PROXY);
	}

	// -- unimplemented methods ------------------------------------------------------------

	public IEngineAnalysisState getAnalysisState() {
		throw new JasperReportEngineRuntimeException("Unsupported method [getAnalysisState]");
	}

	public void setAnalysisState(IEngineAnalysisState analysisState) {
		throw new JasperReportEngineRuntimeException("Unsupported method [setAnalysisState]");
	}

	public void validate() throws SpagoBIEngineException {
		throw new JasperReportEngineRuntimeException("Unsupported method [validate]");
	}

}
