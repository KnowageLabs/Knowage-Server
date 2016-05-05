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
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.services.common.EnginConf;
import it.eng.spagobi.services.content.bo.Content;
import it.eng.spagobi.services.proxy.ContentServiceProxy;
import it.eng.spagobi.utilities.DynamicClassLoader;
import it.eng.spagobi.utilities.ParametersDecoder;
import it.eng.spagobi.utilities.ResourceClassLoader;
import it.eng.spagobi.utilities.SpagoBIAccessUtils;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.util.ArrayList;
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

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JExcelApiExporter;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRGraphics2DExporter;
import net.sf.jasperreports.engine.export.JRGraphics2DExporterParameter;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.JRTextExporter;
import net.sf.jasperreports.engine.export.JRTextExporterParameter;
import net.sf.jasperreports.engine.export.JRXmlExporter;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;
import net.sf.jasperreports.engine.fill.JRGzipVirtualizer;
import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer;
import net.sf.jasperreports.engine.util.JRSwapFile;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.apache.log4j.Logger;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;
import org.xml.sax.InputSource;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
//import com.sun.image.codec.jpeg.JPEGCodec;
//import com.sun.image.codec.jpeg.JPEGEncodeParam;
//import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * Jasper Report implementation built to provide all methods to run a report inside SpagoBI. It is the jasper report Engine implementation for SpagoBI.
 * 
 * @author Gioia * @deprecated
 */
@Deprecated
public class JasperReportRunner {

	private static transient Logger logger = Logger.getLogger(JasperReportRunner.class);
	public static final String JS_FILE_ZIP = "JS_File";
	public static final String JS_DIR = "JS_dir";
	public static final String JS_EXT_ZIP = ".zip";

	private String documentId = null;
	private String userId = null;
	private String userUniqueIdentifier = null;

	/**
	 * Class Constructor.
	 * 
	 * @param session
	 *            the session
	 */
	public JasperReportRunner(HttpSession session) {
		super();
	}

	/**
	 * This method, known all input information, runs a report with JasperReport inside SpagoBI. iIt is the Jasper Report Engine's core method.
	 * 
	 * @param parameters
	 *            The input parameters map
	 * @param servletContext
	 *            The java servlet context object
	 * @param servletResponse
	 *            The java http servlet response object
	 * @param conn
	 *            the conn
	 * @param out
	 *            the out
	 * @param servletRequest
	 *            the servlet request
	 * @throws Exception
	 *             If any Exception occurred
	 */
	public void runReport(Connection conn, Map parameters, OutputStream out, ServletContext servletContext, HttpServletResponse servletResponse,
			HttpServletRequest servletRequest) throws Exception {
		logger.debug("IN");
		Monitor monitor = MonitorFactory.start("JasperReportRunner.service");
		documentId = servletRequest.getParameter("document");

		HttpSession session = servletRequest.getSession();
		IEngUserProfile profile = (IEngUserProfile) session.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		userId = (String) ((UserProfile) profile).getUserId();
		userUniqueIdentifier = (String) profile.getUserUniqueIdentifier();

		logger.debug("Read user data from the request. userId=" + userId + ". DocumentId=" + documentId);

		String resourcePath = EnginConf.getInstance().getResourcePath() + "/img/";
		String entity = (String) parameters.get(SpagoBIConstants.SBI_ENTITY);
		// IF exist an ENTITY parameter concat to resourcePath
		if (entity != null && entity.length() > 0) {
			resourcePath = resourcePath.concat(entity + "/");
		}
		logger.debug("SetUp resourcePath:" + resourcePath);

		String prefixDirTemplate = (String) parameters.get("prefixName");
		logger.debug("prefixDirTemplate:" + prefixDirTemplate);

		parameters.put("SBI_USERID", userUniqueIdentifier);
		parameters.put("SBI_HTTP_SESSION", session);
		parameters.put("SBI_RESOURCE_PATH", resourcePath);

		/*
		 * TODO Since this is the back-end (logic) part of the JasperEngine the direct use of HttpServletResponse, HttpServletRequest and ServletContext objects
		 * shuold be pushed back to JasperReportServlet that is the front-end (control) part of the engine
		 */
		File[] compiledSubreports = null;
		SpagoBIAccessUtils util = new SpagoBIAccessUtils();
		// identity string for object execution
		UUIDGenerator uuidGen = UUIDGenerator.getInstance();
		UUID uuid_local = uuidGen.generateTimeBasedUUID();
		// String executionId = uuid_local.toString();
		// executionId = executionId.replaceAll("-", "");
		String flgTemplateStandard = "true";

		// ContentServiceProxy contentProxy=new ContentServiceProxy(userId,session);
		ContentServiceProxy contentProxy = new ContentServiceProxy(userUniqueIdentifier, session);

		try {
			String tmpDirectory = System.getProperty("java.io.tmpdir");

			// all jar needed by JR to succesfully compile a report should be on this path
			// (by default is WEB_INF/lib)
			setJRClasspath(getJRLibDir(servletContext));

			HashMap requestParameters = ParametersDecoder.getDecodedRequestParameters(servletRequest);
			Content template = contentProxy.readTemplate(documentId, requestParameters);
			if (template == null) {
				logger.error("The document haven't the template.documentId=" + documentId + " userUniqueIdentifier=" + userUniqueIdentifier);
				return;
			}
			logger.debug("Read the template." + template.getFileName());
			InputStream is = null;
			BASE64Decoder bASE64Decoder = new BASE64Decoder();
			byte[] templateContent = bASE64Decoder.decodeBuffer(template.getContent());
			is = new java.io.ByteArrayInputStream(templateContent);

			if (template.getFileName().indexOf(".zip") > -1) {
				flgTemplateStandard = "false";
			}

			/*
			 * Dynamic template management: if the template is a zip file it is opened and every class are added to the classpath
			 */
			boolean propertiesLoaded = false;
			if (flgTemplateStandard.equalsIgnoreCase("false")) {
				logger.debug("The template is a .ZIP file");
				File fileZip = new File(getJRTempDir(servletContext, prefixDirTemplate), JS_FILE_ZIP + JS_EXT_ZIP);
				FileOutputStream foZip = new FileOutputStream(fileZip);
				foZip.write(templateContent);
				foZip.close();
				util.unzip(fileZip, getJRTempDir(servletContext, prefixDirTemplate));
				JarFile zipFile = new JarFile(fileZip);
				Enumeration totalZipEntries = zipFile.entries();
				File jarFile = null;
				while (totalZipEntries.hasMoreElements()) {
					ZipEntry entry = (ZipEntry) totalZipEntries.nextElement();
					if (entry.getName().endsWith(".jar")) {
						jarFile = new File(getJRTempDirName(servletContext, prefixDirTemplate) + entry.getName());
						// set classloader with jar
						ClassLoader previous = Thread.currentThread().getContextClassLoader();
						DynamicClassLoader dcl = new DynamicClassLoader(jarFile, previous);
						Thread.currentThread().setContextClassLoader(dcl);
					} else if (entry.getName().endsWith(".jrxml")) {
						// set InputStream with jrxml
						File jrxmlFile = new File(getJRTempDirName(servletContext, prefixDirTemplate) + entry.getName());
						InputStream isJrxml = new FileInputStream(jrxmlFile);
						byte[] templateJrxml = new byte[0];
						templateJrxml = util.getByteArrayFromInputStream(isJrxml);
						is = new java.io.ByteArrayInputStream(templateJrxml);
					}
					if (entry.getName().endsWith(".properties")) {

						propertiesLoaded = true;
					}
				}
			}

			// Set the temporary location for the files generated on-the-fly by JR
			// (by default is the current user tmp-dir)
			setJRTempDir(tmpDirectory);

			logger.debug("Compiling template file ...");
			Monitor monitorCompileTemplate = MonitorFactory.start("JasperReportRunner.compileTemplate");
			JasperReport report = JasperCompileManager.compileReport(is);
			monitorCompileTemplate.stop();
			logger.debug("Template file compiled  succesfully");

			parameters = adaptReportParams(parameters, report);

			// Add locale
			if (parameters.get("SBI_LANGUAGE") != null && parameters.get("SBI_COUNTRY") != null) {
				Locale locale = null;
				String language = (String) parameters.get("SBI_LANGUAGE");
				String country = (String) parameters.get("SBI_COUNTRY");
				logger.debug("Internazionalization in " + language);
				locale = new Locale(language, country, "");

				parameters.put("REPORT_LOCALE", locale);

				ResourceBundle rs = null;

				if (propertiesLoaded == false) {

					// if properties file are not loaded by template load them from resources
					SourceBean config = null;
					if (getClass().getResource("/engine-config.xml") != null) {
						InputSource source = new InputSource(getClass().getResourceAsStream("/engine-config.xml"));
						config = SourceBean.fromXMLStream(source);
					}
					SourceBean sb = (SourceBean) config.getAttribute("RESOURCE_PATH_JNDI_NAME");
					String path = sb.getCharacters();
					String resPath = SpagoBIUtilities.readJndiResource(path);
					resPath += "/jasper_messages/";

					ClassLoader previous = Thread.currentThread().getContextClassLoader();
					ResourceClassLoader dcl = new ResourceClassLoader(resPath, previous);
					// Thread.currentThread().setContextClassLoader(dcl);
					try {
						// rs=PropertyResourceBundle.getBundle("messages",locale, Thread.currentThread().getContextClassLoader());
						rs = PropertyResourceBundle.getBundle("messages", locale, dcl);
					} catch (Exception e) {
						logger.error("could not find properties message");
					}
					parameters.put("REPORT_RESOURCE_BUNDLE", rs);
				}
			}

			Monitor monitorSubReport = MonitorFactory.start("JasperReportRunner.compileSubReport");
			// compile subreports
			// compiledSubreports = compileSubreports(parameters, getJRCompilationDir(servletContext, executionId),contentProxy, requestParameters);
			compiledSubreports = compileSubreports(parameters, servletContext, contentProxy, requestParameters);
			monitorSubReport.stop();
			// set classloader
			ClassLoader previous = Thread.currentThread().getContextClassLoader();
			ClassLoader current = URLClassLoader.newInstance(new URL[] { getJRCompilationDir(servletContext, prefixDirTemplate).toURL() }, previous);
			Thread.currentThread().setContextClassLoader(current);

			// Create the virtualizer
			if (isVirtualizationActive()) {
				logger.debug("Virtualization of fill process is active");
				// parameters.put(JRParameter.REPORT_VIRTUALIZER, getVirtualizer(tmpDirectory, servletContext));
				parameters.put(JRParameter.REPORT_VIRTUALIZER, getSwapVirtualizer(tmpDirectory, servletContext));
				// parameters.put(JRParameter.REPORT_VIRTUALIZER, getGzipVirtualizer());
			}

			logger.debug("Filling report ...");
			Monitor monitorFillingReport = MonitorFactory.start("JasperReportRunner.FillingReport");
			JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, conn);
			monitorFillingReport.stop();
			logger.debug("Report filled succesfully");

			logger.debug("Exporting report ...");
			String outputType = (String) parameters.get("outputType");
			if (outputType == null) {
				logger.debug("Output type is not specified. Default type will be used");
				outputType = "html";
				// outputType = ExporterFactory.getDefaultType();
			}
			logger.debug("Output format is [" + outputType + "]");
			Monitor monitorExportReport = MonitorFactory.start("JasperReportRunner.ExportReport");
			JRExporter exporter = ExporterFactory.getExporter(outputType);
			String mimeType = ExporterFactory.getMIMEType(outputType);

			if (exporter != null)
				logger.debug("Configured exporter class [" + exporter.getClass().getName() + "]");
			else
				logger.debug("Exporter class [null]");
			logger.debug("Configured MIME type [" + mimeType + "]");

			// for base types use default exporter, mimeType and parameters if these
			// are not specified by configuration file
			if (outputType.equalsIgnoreCase("csv")) {
				if (mimeType == null)
					mimeType = "text/plain";
				servletResponse.setContentType(mimeType);
				if (exporter == null)
					exporter = new JRCsvExporter();
			} else if (outputType.equalsIgnoreCase("html")) {
				if (mimeType == null)
					mimeType = "text/html";
				servletResponse.setContentType(mimeType);
				if (exporter == null)
					exporter = new JRHtmlExporter();
				exporter.setParameter(JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN, Boolean.FALSE);
				exporter.setParameter(JRHtmlExporterParameter.BETWEEN_PAGES_HTML, "");
				// define the map structure for report images
				HashMap m_imagesMap = new HashMap();
				String mapName = uuid_local.toString();
				servletRequest.getSession().setAttribute(mapName, m_imagesMap);
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_MAP, m_imagesMap);
				// exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI, "image.jsp?mapname="+mapName+"&image=");
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI, "JRImageServlet?mapname=" + mapName + "&image=");

				/*
				 * commented by Davide Zerbetto on 12/10/2009: there are problems with MIF (Ext ManagedIFrame library) library // setting HTML header: this is
				 * necessary in order to inject the document.domain directive String head = getHTMLHeader();
				 * exporter.setParameter(JRHtmlExporterParameter.HTML_HEADER, head);
				 */
			} else if (outputType.equalsIgnoreCase("xls")) {
				if (mimeType == null)
					mimeType = "application/vnd.ms-excel";
				servletResponse.setContentType(mimeType);
				// if(exporter == null) exporter = new JRXlsExporter();
				if (exporter == null)
					exporter = new JExcelApiExporter();
			} else if (outputType.equalsIgnoreCase("rtf")) {
				if (mimeType == null)
					mimeType = "application/rtf";
				servletResponse.setContentType(mimeType);
				if (exporter == null)
					exporter = new JRRtfExporter();
			} else if (outputType.equalsIgnoreCase("xml")) {
				if (mimeType == null)
					mimeType = "text/xml";
				servletResponse.setContentType(mimeType);
				if (exporter == null)
					exporter = new JRXmlExporter();
			} else if (outputType.equalsIgnoreCase("txt")) {
				if (mimeType == null)
					mimeType = "text/plain";
				servletResponse.setContentType(mimeType);
				if (exporter == null)
					exporter = new JRTextExporter();
				exporter.setParameter(JRTextExporterParameter.PAGE_HEIGHT, new Integer(100));
				exporter.setParameter(JRTextExporterParameter.PAGE_WIDTH, new Integer(100));
			} else if (outputType.equalsIgnoreCase("pdf")) {
				if (mimeType == null)
					mimeType = "application/pdf";
				servletResponse.setContentType(mimeType);
				if (exporter == null)
					exporter = new JRPdfExporter();
			} else if (outputType.equalsIgnoreCase("JPG")) {
				byte[] bytes = getImageBytes(report, jasperPrint);
				if (mimeType == null)
					mimeType = "application/jpeg";
				out.write(bytes);
				return;
			} else if (outputType.equalsIgnoreCase("JPGBASE64")) {
				byte[] bytes = getImagesBase64Bytes(report, jasperPrint);
				if (mimeType == null)
					mimeType = "text/plain";
				out.write(bytes);
				return;
			} else {
				if (mimeType != null && exporter != null)
					servletResponse.setContentType(mimeType);
				else {
					logger.warn("Impossible to load exporter for type " + outputType);
					logger.warn("Pdf exporter will be used");
					servletResponse.setContentType("application/pdf");
					exporter = new JRPdfExporter();
				}
			}

			logger.debug("MIME type of response is [" + servletResponse.getContentType() + "]");
			logger.debug("Exporter class used is  [" + exporter.getClass().getName() + "]");

			exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
			exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
			exporter.exportReport();
			monitorExportReport.stop();
			logger.debug("Report exported succesfully");

		} catch (Throwable e) {
			logger.error("An exception has occured", e);
			throw new Exception(e);
		} finally {
			// delete tmp dir for dynamic template management only if it's empty (without subreport files)
			// File tmpDir = getJRTempDir(servletContext, executionId, prefixDirTemplate).getParentFile();
			File tmpDir = getJRTempDir(servletContext, prefixDirTemplate);
			String[] files = tmpDir.list();
			if (files.length == 0) {
				util.deleteDirectory(tmpDir);
				logger.debug("Delating temporary directory: " + tmpDir);
			}
			monitor.stop();
			logger.debug("OUT");
		}

	}

	/**
	 * This method builds the html header string to be injected on report HTML output. This is necessary in order to inject the document.domain javascript
	 * directive
	 * 
	 * @return the HTML head tag as a string
	 */
	/*
	 * commented by Davide Zerbetto: there are problems with MIF (Ext ManagedIFrame library) library protected String getHTMLHeader() { logger.debug("IN");
	 * String header = null; try { SourceBean config = EnginConf.getInstance().getConfig(); SourceBean htmlHeaderSb = (SourceBean)
	 * config.getAttribute("HTML_HEADER"); header = htmlHeaderSb.getCharacters(); if (header == null || header.trim().equals("")) { throw new
	 * Exception("HTML_HEADER not configured"); } header = header.replaceAll("\\$\\{SBI_DOMAIN\\}", EnginConf.getInstance().getSpagoBiDomain()); } catch
	 * (Exception e) { logger.error("Error while retrieving HTML_HEADER from engine configuration.", e); logger.info("Using default HTML header", e);
	 * StringBuffer buffer = new StringBuffer(); buffer.append("<html>"); buffer.append("<head>"); buffer.append("<title></title>");
	 * buffer.append("  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>"); buffer.append("  <style type=\"text/css\">");
	 * buffer.append("    a {text-decoration: none}"); buffer.append("  </style>"); buffer.append("  <script type=\"text/javascript\">");
	 * buffer.append("    document.domain='" + EnginConf.getInstance().getSpagoBiDomain() + "';"); buffer.append("  </script>"); buffer.append("</head>");
	 * buffer.append("<body text=\"#000000\" link=\"#000000\" alink=\"#000000\" vlink=\"#000000\">");
	 * buffer.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">");
	 * buffer.append("<tr><td width=\"50%\">&nbsp;</td><td align=\"center\">"); header = buffer.toString(); } logger.debug("OUT"); return header; }
	 */

	// ///////////////////////////////////////
	// UTILITY METHODS
	// ///////////////////////////////////////

	private void setJRClasspath(String jrLibDir) {
		logger.debug("JasperReports lib-dir is [" + this.getClass().getName() + "]");
		System.setProperty("jasper.reports.compile.class.path", buildJRClasspathValue(jrLibDir));
		logger.debug("Set [jasper.reports.compile.class.path properties] to value [" + System.getProperty("jasper.reports.compile.class.path") + "]");

	}

	/**
	 * Build a classpath variable appending all the jar files founded into the specified directory.
	 * 
	 * @param libDir
	 *            JR lib-dir to scan for find jar files to include into the classpath variable
	 * @return the classpath used by JasperReprorts Engine (by default equals to WEB-INF/lib)
	 */
	private String buildJRClasspathValue(String libDir) {
		logger.debug("IN");
		String getJRClasspathValue = null;

		logger.debug("Reading jar files from lib-dir...");
		StringBuffer jasperReportClassPathStringBuffer = new StringBuffer();
		File f = new File(libDir);
		String fileToAppend = null;
		if (f.isDirectory()) {
			String[] jarFiles = f.list();
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

	private boolean isVirtualizationActive() {
		logger.debug("IN");
		boolean isVirtualizationActive = false;
		SourceBean config = EnginConf.getInstance().getConfig();
		String active = (String) config.getAttribute("VIRTUALIZER.active");
		if (active != null)
			isVirtualizationActive = active.equalsIgnoreCase("true");
		logger.debug("OUT");
		return isVirtualizationActive;
	}

	/**
	 * Gets the virtualizer. (the slowest)
	 * 
	 * @param tmpDirectory
	 *            the tmp directory
	 * @param servletContext
	 *            the servlet context
	 * @return the virtualizer
	 */
	public JRFileVirtualizer getVirtualizer(String tmpDirectory, ServletContext servletContext) {
		logger.debug("IN");
		JRFileVirtualizer virtualizer = null;

		SourceBean config = EnginConf.getInstance().getConfig();
		String maxSizeStr = (String) config.getAttribute("VIRTUALIZER.maxSize");
		int maxSize = 2;
		if (maxSizeStr != null)
			maxSize = Integer.parseInt(maxSizeStr);
		String dir = (String) config.getAttribute("VIRTUALIZER.dir");
		if (dir == null) {
			dir = tmpDirectory;
		} else {
			if (!dir.startsWith("/")) {
				String contRealPath = servletContext.getRealPath("/");
				if (contRealPath.endsWith("\\") || contRealPath.endsWith("/")) {
					contRealPath = contRealPath.substring(0, contRealPath.length() - 1);
				}
				dir = contRealPath + "/" + dir;
			}
		}
		dir = dir + System.getProperty("file.separator") + "jrcache";
		File file = new File(dir);
		file.mkdirs();
		logger.debug("Max page cached during virtualization process: " + maxSize);
		logger.debug("Dir used as storing area during virtualization: " + dir);
		virtualizer = new JRFileVirtualizer(maxSize, dir);
		virtualizer.setReadOnly(false);
		logger.debug("OUT");
		return virtualizer;
	}

	/**
	 * Gets the swap virtualizer. (the fastest)
	 * 
	 * @param tmpDirectory
	 *            the tmp directory
	 * @param servletContext
	 *            the servlet context
	 * @return the virtualizer
	 */
	public JRSwapFileVirtualizer getSwapVirtualizer(String tmpDirectory, ServletContext servletContext) {
		logger.debug("IN");
		JRSwapFileVirtualizer virtualizer = null;

		SourceBean config = EnginConf.getInstance().getConfig();
		String maxSizeStr = (String) config.getAttribute("VIRTUALIZER.maxSize");
		int maxSize = 2;
		if (maxSizeStr != null)
			maxSize = Integer.parseInt(maxSizeStr);
		String dir = (String) config.getAttribute("VIRTUALIZER.dir");
		if (dir == null) {
			dir = tmpDirectory;
		} else {
			if (!dir.startsWith("/")) {
				String contRealPath = servletContext.getRealPath("/");
				if (contRealPath.endsWith("\\") || contRealPath.endsWith("/")) {
					contRealPath = contRealPath.substring(0, contRealPath.length() - 1);
				}
				dir = contRealPath + "/" + dir;
			}
		}

		dir = dir + System.getProperty("file.separator") + "jrcache";
		File file = new File(dir);
		file.mkdirs();
		logger.debug("Max page cached during virtualization process: " + maxSize);
		logger.debug("Dir used as storing area during virtualization: " + dir);
		JRSwapFile swapFile = new JRSwapFile(dir, maxSize, maxSize);
		virtualizer = new JRSwapFileVirtualizer(maxSize, swapFile);
		virtualizer.setReadOnly(false);
		logger.debug("OUT");
		return virtualizer;
	}

	/**
	 * Gets the gZip virtualizer (it works in memory: slower).
	 * 
	 * @param tmpDirectory
	 *            the tmp directory
	 * @param servletContext
	 *            the servlet context
	 * @return the virtualizer
	 */
	public JRGzipVirtualizer getGzipVirtualizer() {
		logger.debug("IN");
		JRGzipVirtualizer virtualizer = null;

		SourceBean config = EnginConf.getInstance().getConfig();
		String maxSizeStr = (String) config.getAttribute("VIRTUALIZER.maxSize");
		int maxSize = 2;
		if (maxSizeStr != null)
			maxSize = Integer.parseInt(maxSizeStr);

		logger.debug("Max page cached during virtualization process: " + maxSize);

		virtualizer = new JRGzipVirtualizer(maxSize);
		virtualizer.setReadOnly(false);
		logger.debug("OUT");
		return virtualizer;
	}

	private byte[] getImagesBase64Bytes(JasperReport report, JasperPrint jasperPrint) {
		logger.debug("IN");
		byte[] bytes = new byte[0];
		try {
			String message = "<IMAGES>";
			List bufferedImages = generateReportImages(report, jasperPrint);
			Iterator iterImgs = bufferedImages.iterator();
			int count = 1;
			while (iterImgs.hasNext()) {
				message += "<IMAGE page=\"" + count + "\">";
				BufferedImage image = (BufferedImage) iterImgs.next();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();

				ImageWriter imageWriter = ImageIO.getImageWritersBySuffix("jpeg").next();
				ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
				imageWriter.setOutput(ios);
				IIOMetadata imageMetaData = imageWriter.getDefaultImageMetadata(new ImageTypeSpecifier(image), null);
				ImageWriteParam par = imageWriter.getDefaultWriteParam();
				par.setCompressionMode(JPEGImageWriteParam.MODE_EXPLICIT);
				par.setCompressionQuality(1.0f);
				imageWriter.write(imageMetaData, new IIOImage(image, null, null), par);

				// JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(baos);
				// JPEGEncodeParam encodeParam = encoder.getDefaultJPEGEncodeParam(image);
				// encodeParam.setQuality(1.0f, true);
				// encoder.setJPEGEncodeParam(encodeParam);
				// encoder.encode(image);

				byte[] byteImg = baos.toByteArray();
				baos.close();
				BASE64Encoder encoder64 = new BASE64Encoder();
				String encodedImage = encoder64.encode(byteImg);
				message += encodedImage;
				message += "</IMAGE>";
				count++;
			}
			message += "</IMAGES>";
			bytes = message.getBytes();
		} catch (Exception e) {
			logger.error("Error while producing byte64 encoding of the report images", e);
		}
		logger.debug("OUT");
		return bytes;
	}

	private byte[] getImageBytes(JasperReport report, JasperPrint jasperPrint) {
		logger.debug("IN");
		byte[] bytes = new byte[0];
		try {
			List bufferedImages = generateReportImages(report, jasperPrint);
			// calculate dimension of the final page
			Iterator iterImgs = bufferedImages.iterator();
			int totalHeight = 0;
			int totalWidth = 0;
			while (iterImgs.hasNext()) {
				BufferedImage image = (BufferedImage) iterImgs.next();
				int hei = image.getHeight();
				int wid = image.getWidth();
				totalHeight += hei;
				totalWidth = wid;
			}
			// create an unique buffer image
			BufferedImage finalImage = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_RGB);
			Graphics2D finalGr2 = finalImage.createGraphics();
			// append all images to the final
			iterImgs = bufferedImages.iterator();
			int y = 0;
			int x = 0;
			while (iterImgs.hasNext()) {
				BufferedImage image = (BufferedImage) iterImgs.next();
				int hei = image.getHeight();
				finalGr2.drawImage(image, new AffineTransform(1f, 0f, 0f, 1f, x, y), null);
				y += hei;
			}
			// gets byte of the jpeg image
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			ImageWriter imageWriter = ImageIO.getImageWritersBySuffix("jpeg").next();
			ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
			imageWriter.setOutput(ios);
			IIOMetadata imageMetaData = imageWriter.getDefaultImageMetadata(new ImageTypeSpecifier(finalImage), null);
			ImageWriteParam par = imageWriter.getDefaultWriteParam();
			par.setCompressionMode(JPEGImageWriteParam.MODE_EXPLICIT);
			par.setCompressionQuality(1.0f);
			imageWriter.write(imageMetaData, new IIOImage(finalImage, null, null), par);

			// JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(baos);
			// JPEGEncodeParam encodeParam = encoder.getDefaultJPEGEncodeParam(finalImage);
			// encodeParam.setQuality(1.0f, true);
			// encoder.setJPEGEncodeParam(encodeParam);
			// encoder.encode(finalImage);

			bytes = baos.toByteArray();
			baos.close();

		} catch (Exception e) {
			logger.error("Error while producing jpg image of the report", e);
		}
		logger.debug("OUT");
		return bytes;
	}

	private List generateReportImages(JasperReport report, JasperPrint jasperPrint) {
		logger.debug("IN");
		List bufferedImages = new ArrayList();
		try {
			int height = report.getPageHeight();
			int width = report.getPageWidth();
			boolean export = true;
			int index = 0;
			while (export == true) {
				BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
				Graphics2D gr2 = image.createGraphics();
				JRExporter exporter = new JRGraphics2DExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
				exporter.setParameter(JRGraphics2DExporterParameter.GRAPHICS_2D, gr2);
				exporter.setParameter(JRGraphics2DExporterParameter.PAGE_INDEX, new Integer(index));
				try {
					exporter.exportReport();
				} catch (Exception e) {
					export = false;
					continue;
				}
				index++;
				bufferedImages.add(image);
			}
		} catch (Exception e) {
			logger.error("Error while producing jpg images of the report", e);
		}
		logger.debug("OUT");
		return bufferedImages;
	}

	/**
	 * @return the classpath used by JasperReprorts Engine (by default equals to WEB-INF/lib) TODO convert this to a File returning method
	 */
	private String getJRLibDir(ServletContext servletContext) {
		logger.debug("IN");
		String jrLibDir = null;
		jrLibDir = servletContext.getRealPath("WEB-INF") + System.getProperty("file.separator") + "lib";
		logger.debug("OUT");
		return jrLibDir;
	}

	private void setJRTempDir(String jrTmpDir) {
		System.setProperty("jasper.reports.compile.temp", jrTmpDir);
		logger.debug("Set [jasper.reports.compile.temp] to value [" + System.getProperty("jasper.reports.compile.temp") + "]");
	}

	private File getJRCompilationDir(ServletContext servletContext, String prefixTemplate) {
		logger.debug("IN");
		File jrCompilationDir = null;
		jrCompilationDir = getJRTempDir(servletContext, prefixTemplate);
		logger.debug("OUT");
		return jrCompilationDir;
	}

	private String getJRTempDirName(ServletContext servletContext, String prefixTemplate) {
		logger.debug("IN");
		String jrTempDir = servletContext.getRealPath("tmpdir") + System.getProperty("file.separator") + "reports" + System.getProperty("file.separator")
				+ JS_DIR + "__" + prefixTemplate + System.getProperty("file.separator");
		logger.debug("OUT");
		return jrTempDir;
	}

	private File getJRTempDir(ServletContext servletContext, String prefixTemplate) {
		logger.debug("IN");
		File jrTempDir = null;

		String jrTempDirStr = getJRTempDirName(servletContext, prefixTemplate);
		jrTempDir = new File(jrTempDirStr.substring(0, jrTempDirStr.length() - 1));
		jrTempDir.mkdirs();
		logger.debug("OUT");
		return jrTempDir;
	}

	protected Map adaptReportParams(Map parameters, JasperReport report) {
		logger.debug("IN");
		String dateformat = (String) parameters.get("dateformat");
		if (dateformat != null) {
			dateformat = dateformat.replaceAll("D", "d");
			dateformat = dateformat.replaceAll("m", "M");
			dateformat = dateformat.replaceAll("Y", "y");
		}
		JRParameter[] reportParameters = report.getParameters();
		ParametersDecoder decoder = new ParametersDecoder();
		for (int i = 0; i < reportParameters.length; i++) {
			JRParameter aReportParameter = reportParameters[i];
			String paramName = aReportParameter.getName();
			logger.debug("Examining parameter with name [" + paramName + "] ...");

			String paramValueString = null;

			if (parameters.get(paramName) instanceof String) {
				paramValueString = (String) parameters.get(paramName);
			}
			if (paramValueString == null) {
				logger.debug("No value found for parameter with name [" + paramName + "]");
				continue;
			}
			if (paramValueString != null) {
				logger.debug("Value found for parameter with name [" + paramName + "] is [" + paramValueString + "]");
				/*
				 * The ParameterConverter converts a single value. Multi-value parameters are assumed to contains values that are String type. If they are not
				 * Strings (list of dates, list of numbers, ...) the converter will not work.
				 */
				if (decoder.isMultiValues(paramValueString)) {
					logger.debug("Value found for parameter with name [" + paramName + "] is [" + paramValueString + "] and it is multivalue. "
							+ "Cannot adapt parameter nature");
					continue;
				}
				Class aReportParameterClass = aReportParameter.getValueClass();
				Object newValue = ParameterConverter.convertParameter(aReportParameterClass, paramValueString, dateformat);
				if (newValue == null)
					newValue = paramValueString;

				if (!(newValue instanceof String)) {
					logger.debug("Updating parameter with name [" + paramName + "] to a " + newValue.getClass().getName() + ".");
					parameters.put(paramName, newValue);
				}
			}
		}
		logger.debug("OUT");
		return parameters;
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

	private Map<String, SubreportMeta> getSubreportsMeta(Map params) {
		Map<String, SubreportMeta> subreportsMeta;

		logger.debug("IN");
		subreportsMeta = new HashMap<String, SubreportMeta>();

		try {
			// String subrptnumStr = (params.get("srptnum")==null)?"0":(String)params.get("srptnum");
			// int subrptnum = Integer.parseInt(subrptnumStr);

			/*
			 * Iterator it = params.keySet().iterator(); while(it.hasNext()){ String parName = (String)it.next(); if(parName.startsWith("subrpt") &&
			 * parName.endsWith("id")) { int start = parName.indexOf('.') + 1; int end = parName.indexOf('.', start); String subreportKey =
			 * parName.substring(start, end); String subreportId = (String)params.get(parName); SubreportMeta subreportMeta = new SubreportMeta( subreportId );
			 * subreportMeta.setTemplateName( (String)params.get("subrpt." + subreportKey + ".tempName") ); subreportMeta.setTemplateFingerprint(
			 * (String)params.get("subrpt." + subreportKey + ".prefixName") ); subreportMeta.setTemplateType( (String)params.get("subrpt." + subreportKey +
			 * ".flgTempStd") ); subreportsMeta.put(subreportKey, subreportMeta); logger.debug("JasperReports subreport id : " + params.get(parName)); } }
			 */
			Iterator it = params.keySet().iterator();
			while (it.hasNext()) {
				String parName = (String) it.next();
				if (parName.startsWith("sr") && parName.endsWith("ids")) {
					int start = parName.indexOf('.') + 1;
					int end = parName.indexOf('.', start);
					String subreportKey = parName.substring(start, end);
					String subreportIds = (String) params.get(parName);
					String[] ids = subreportIds.split("_");
					SubreportMeta subreportMeta = new SubreportMeta(ids[0]);
					// subreportMeta.setTemplateName( (String)params.get("subrpt." + subreportKey + ".tempName") );
					subreportMeta.setTemplateFingerprint(subreportIds);
					// subreportMeta.setTemplateType( (String)params.get("subrpt." + subreportKey + ".flgTempStd") );
					subreportsMeta.put(subreportKey, subreportMeta);
					logger.debug("JasperReports subreport id : " + params.get(parName));
				}
			}

		} catch (Throwable t) {
			logger.error("Error while extracting subreports meta", t);
		} finally {
			logger.debug("OUT");
		}

		return subreportsMeta;

	}

	private File[] compileSubreports(Map params, ServletContext servletContext, ContentServiceProxy contentProxy, HashMap requestParameters) {

		File[] files = null;

		logger.debug("IN");
		try {
			/*
			 * String subrptnumStr = (params.get("srptnum")==null)?"0":(String)params.get("srptnum"); int subrptnum = Integer.parseInt(subrptnumStr); String[]
			 * subreports = new String[subrptnum]; String[] subreportsType = new String[subrptnum];
			 */

			/*
			 * Iterator it = params.keySet().iterator(); while(it.hasNext()){ String parName = (String)it.next(); if(parName.startsWith("subrpt") &&
			 * parName.endsWith("id")) { int start = parName.indexOf('.') + 1; int end = parName.indexOf('.', start); String numberStr =
			 * parName.substring(start, end); int number = Integer.parseInt(numberStr) - 1; subreports[number] = (String)params.get(parName);
			 * logger.debug("JasperReports subreport id : " + params.get(parName)); } else if(parName.startsWith("subrpt") && parName.endsWith("flgTempStd")) {
			 * int start = parName.indexOf('.') + 1; int end = parName.indexOf('.', start); String numberStr = parName.substring(start, end); int number =
			 * Integer.parseInt(numberStr) - 1; subreportsType[number] = (String)params.get(parName); } }
			 */
			Map<String, SubreportMeta> subreportsMeta = getSubreportsMeta(params);
			int subreportNum = subreportsMeta.keySet().size();

			files = new File[subreportNum];
			logger.debug("Subreports number is equal to [" + subreportNum + "]");

			Iterator it = subreportsMeta.keySet().iterator();
			int i = 0;
			while (it.hasNext()) {
				SubreportMeta subreportMeta = subreportsMeta.get(it.next());
				String masterIds = (String) params.get("prefixName");

				// check if the subreport is cached into file system
				String dirTemplate = getJRTempDirName(servletContext, masterIds + System.getProperty("file.separator") + subreportMeta.getTemplateFingerprint());
				logger.debug("dirTemplate is equal to [" + dirTemplate + "]");

				// boolean exists = (new File(dirTemplate + subreportMeta.getTemplateName() + ".jasper")).exists();
				File subreportCacheDir = new File(dirTemplate);
				if (subreportCacheDir.exists()) {
					logger.debug("template [" + subreportMeta.getTemplateFingerprint() + "] alredy exists");

					// File already exists
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
					// files[i] = new File(dirTemplate, subreportMeta.getTemplateName() + ".jasper");
					files[i] = compiledJRFiles[0];
				} else {
					logger.debug("template [" + subreportMeta.getTemplateFingerprint() + "] does not exists yet");

					File destDir = getJRCompilationDir(servletContext,
							masterIds + System.getProperty("file.separator") + subreportMeta.getTemplateFingerprint());

					logger.debug("destDir number is equal to [" + destDir + "]");

					// File or directory does not exist, create a new file compiled!
					// put "true" to the parameter that not permits the validation on parameters of the subreport.
					requestParameters.put("SBI_READ_ONLY_TEMPLATE", "true");
					Content template = contentProxy.readTemplate(subreportMeta.getDocumentId(), requestParameters);
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

					files[i] = new File(destDir, jasperDesign.getName() + ".jasper");
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

				// adds the subreport's folder to the classpath
				/*
				 * ClassLoader previous = Thread.currentThread().getContextClassLoader(); ClassLoader current = URLClassLoader.newInstance( new URL[]{
				 * getJRCompilationDir(servletContext, masterIds + System.getProperty("file.separator") + subreportMeta.getTemplateFingerprint()).toURL() },
				 * previous);
				 */
				// Thread.currentThread().setContextClassLoader(current);

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

}
