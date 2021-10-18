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
package it.eng.spagobi.engines.birt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.report.IBirtConstants;
import org.eclipse.birt.report.engine.api.EXCELRenderOption;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.HTMLActionHandler;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.HTMLServerImageHandler;
import org.eclipse.birt.report.engine.api.IDataExtractionTask;
import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IImage;
import org.eclipse.birt.report.engine.api.IPageHandler;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.IRenderTask;
import org.eclipse.birt.report.engine.api.IReportDocument;
import org.eclipse.birt.report.engine.api.IReportDocumentInfo;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IResultSetItem;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.IRunTask;
import org.eclipse.birt.report.engine.api.IScalarParameterDefn;
import org.eclipse.birt.report.engine.api.PDFRenderOption;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.eclipse.birt.report.engine.dataextraction.CSVDataExtractionOption;
import org.eclipse.birt.report.engine.dataextraction.ICSVDataExtractionOption;
import org.eclipse.birt.report.utility.BirtUtility;
import org.eclipse.birt.report.utility.DataExtractionParameterUtil;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.engines.birt.exceptions.ConnectionDefinitionException;
import it.eng.spagobi.engines.birt.exceptions.ConnectionParameterNotValidException;
import it.eng.spagobi.engines.birt.utilities.ParameterConverter;
import it.eng.spagobi.engines.birt.utilities.Utils;
import it.eng.spagobi.services.common.EnginConf;
import it.eng.spagobi.services.common.SsoServiceFactory;
import it.eng.spagobi.services.common.SsoServiceInterface;
import it.eng.spagobi.services.content.bo.Content;
import it.eng.spagobi.services.proxy.ContentServiceProxy;
import it.eng.spagobi.services.proxy.DataSourceServiceProxy;
import it.eng.spagobi.tools.dataset.common.behaviour.UserProfileUtils;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.DynamicClassLoader;
import it.eng.spagobi.utilities.ParametersDecoder;
import it.eng.spagobi.utilities.SpagoBIAccessUtils;
import it.eng.spagobi.utilities.callbacks.audit.AuditAccessUtils;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;


/**
 * @author Zerbetto (davide.zerbetto@eng.it)
 *
 *         DATE CONTRIBUTOR/DEVELOPER NOTE 02-10-2008 Zerbetto Davide/Julien Decreuse (Smile) Upgrade to Birt 2.3.0 API
 **/
public class BirtReportServlet extends HttpServlet {

	private IReportEngine birtReportEngine = null;
	protected static Logger logger = Logger.getLogger(BirtReportServlet.class);
	private static final String CONNECTION_NAME = "connectionName";
	public static final String JS_EXT_ZIP = ".zip";
	public static final String JS_FILE_ZIP = "JS_File";
	public static final String RTF_FORMAT = "RTF";
	public static final String predefinedGroovyScriptFileName = "predefinedGroovyScript.groovy";
	public static final String predefinedJsScriptFileName = "predefinedJavascriptScript.js";

	private static final String FLAGGED_CSV_EXPORT_RESULT_SET_NAME = "exportdata";

	public static final String OUTPUT_FOLDER = System.getProperty("java.io.tmpdir") != null && System.getProperty("java.io.tmpdir").endsWith(File.separator)
			? System.getProperty("java.io.tmpdir") + "birt" + File.separator
			: System.getProperty("java.io.tmpdir") + File.separator + "birt" + File.separator;

	public static final String PAGE_FILE_NAME = "page";
	public static final String REPORT_EXECUTION_ID = "REPORT_EXECUTION_ID";

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
	 */
	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
		logger.debug("Initializing SpagoBI BirtReport Engine...");
		BirtEngine.initBirtConfig();
		logger.debug(":init:Inizialization of SpagoBI BirtReport Engine ended succesfully");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.servlet.GenericServlet#destroy()
	 */
	@Override
	public void destroy() {
		super.destroy();
		BirtEngine.destroyBirtEngine();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest , javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

		logger.debug("Start processing a new request...");

		// USER PROFILE
		HttpSession session = request.getSession();
		IEngUserProfile profile = (IEngUserProfile) session.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		String documentId = request.getParameter("document");
		// String userId = (String) ((UserProfile)profile).getUserId();
		String userId = (String) profile.getUserUniqueIdentifier();

		logger.debug("userId=" + userId);
		logger.debug("documentId=" + documentId);

		// AUDIT UPDATE
		String auditId = request.getParameter("SPAGOBI_AUDIT_ID");
		logger.debug("auditId=" + auditId);
		AuditAccessUtils auditAccessUtils = (AuditAccessUtils) request.getSession().getAttribute("SPAGOBI_AUDIT_UTILS");
		if (auditId != null) {
			if (auditAccessUtils != null)
				auditAccessUtils.updateAudit(session, userId, auditId, new Long(System.currentTimeMillis()), null, "EXECUTION_STARTED", null, null);
		}
		try {
			runReport(request, response);
			// AUDIT UPDATE
			if (auditId != null) {
				if (auditAccessUtils != null)
					auditAccessUtils.updateAudit(session, userId, auditId, null, new Long(System.currentTimeMillis()), "EXECUTION_PERFORMED", null, null);
			}
		} catch (ConnectionDefinitionException e) {
			logger.error("Error during report production \n\n " + e);
			PrintWriter writer = response.getWriter();
			String resp = "<html><body><center>" + e.getDescription() + "</center></body></html>";
			writer.write(resp);
			writer.flush();
			writer.close();
			// AUDIT UPDATE
			if (auditId != null) {
				if (auditAccessUtils != null)
					auditAccessUtils.updateAudit(session, userId, auditId, null, new Long(System.currentTimeMillis()), "EXECUTION_FAILED", e.getDescription(),
							null);
			}
		} catch (Exception e) {
			logger.error("Error during report production \n\n ", e);
			// AUDIT UPDATE
			if (auditId != null) {
				if (auditAccessUtils != null)
					auditAccessUtils.updateAudit(session, userId, auditId, null, new Long(System.currentTimeMillis()), "EXECUTION_FAILED", e.getMessage(),
							null);
			}
		}

		logger.info(" Request processed");

	}

	private String decodeParameter(Object paramaterValue) {
		if (paramaterValue == null)
			return null;
		else {
			String paramaterValueStr = paramaterValue.toString();
			String toReturn = "";
			ParametersDecoder decoder = new ParametersDecoder();
			if (decoder.isMultiValues(paramaterValueStr)) {
				List values = decoder.decode(paramaterValueStr);
				// toReturn = (String) values.get(0);
				for (int i = 0; i < values.size(); i++) {
					toReturn += (i > 0 ? "," : "");
					toReturn += values.get(i);
				}
			} else {
				toReturn = paramaterValueStr;
			}
			return toReturn;
		}
	}

	protected HTMLRenderOption prepareHtmlRenderOption(HttpServletRequest servletRequest) throws Exception {
		boolean isBackEndRequest = isBackEndRequest(servletRequest);
		if (isBackEndRequest) {
			return prerareBackEndHtmlRenderOption();
		} else {
			return prepareFrontEndHtmlRenderOption();
		}
	}

	private boolean isBackEndRequest(HttpServletRequest servletRequest) {
		logger.debug("IN");
		String requestURL = servletRequest.getRequestURL().toString();
		logger.debug("Request URL : " + requestURL);
		boolean toReturn = requestURL.endsWith("BackEnd");
		logger.debug("OUT : " + toReturn);
		return toReturn;
	}

	private HTMLRenderOption prepareFrontEndHtmlRenderOption() {
		logger.debug("IN");
		String tmpDir = System.getProperty("java.io.tmpdir");
		String imageDirectory = tmpDir.endsWith(File.separator) ? tmpDir + "birt" : tmpDir + File.separator + "birt";
		String imageBaseUrl = "BirtImageServlet?imageID=";

		// Register new image handler
		HTMLRenderOption renderOption = new HTMLRenderOption();
		renderOption.setActionHandler(new HTMLActionHandler());
		HTMLServerImageHandler imageHandler = new HTMLServerImageHandler();
		renderOption.setImageHandler(imageHandler);
		renderOption.setImageDirectory(imageDirectory);
		renderOption.setBaseImageURL(imageBaseUrl);
		renderOption.setEmbeddable(false);
		logger.debug("OUT");
		return renderOption;
	}

	private HTMLRenderOption prerareBackEndHtmlRenderOption() {
		logger.debug("IN");
		HTMLRenderOption renderOption = new HTMLRenderOption();
		renderOption.setOutputFormat(HTMLRenderOption.HTML);
		renderOption.setSupportedImageFormats("PNG;GIF;JPG;BMP");
		renderOption.setEmbeddable(false);
		renderOption.setImageHandler(new HTMLServerImageHandler() {
			@Override
			protected String handleImage(IImage image, Object context, String prefix, boolean needMap) {
				byte[] encodedBytes = Base64.getEncoder().encode(image.getImageData());
				String embeddedImage = new String(encodedBytes);
				String extension = image.getExtension().substring(1).toLowerCase(); // it starts with "."
				return "data:image/" + extension + ";base64," + embeddedImage;
			}
		});
		logger.debug("OUT");
		return renderOption;
	}

	private InputStream getTemplateContent(HttpServletRequest servletRequest) throws IOException {
		logger.debug("IN");
		HttpSession session = servletRequest.getSession();
		IEngUserProfile profile = (IEngUserProfile) session.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		String documentId = servletRequest.getParameter("document");
		String userId = (String) profile.getUserUniqueIdentifier();
		// String userId = (String)((UserProfile) profile).getUserId();
		logger.debug("userId=" + userId);
		logger.debug("documentId=" + documentId);

		ContentServiceProxy contentProxy = new ContentServiceProxy(userId, servletRequest.getSession());

		HashMap requestParameters = ParametersDecoder.getDecodedRequestParameters(servletRequest);
		Content template = contentProxy.readTemplate(documentId, requestParameters);
		logger.debug("Read the template=" + template.getFileName());

		InputStream is = null;
		byte[] templateContent = null;
		try {
			Base64.Decoder bASE64Decoder = Base64.getDecoder();
			templateContent = bASE64Decoder.decode(template.getContent());
			is = new java.io.ByteArrayInputStream(templateContent);
		} catch (Throwable t) {
			logger.warn("Error on decompile", t);
		}

		String flgTemplateStandard = "true";
		if (template.getFileName().indexOf(".zip") > -1) {
			flgTemplateStandard = "false";
		} else {
			flgTemplateStandard = "true";
		}

		SpagoBIAccessUtils util = new SpagoBIAccessUtils();
		UUIDGenerator uuidGen = UUIDGenerator.getInstance();
		UUID uuid_local = uuidGen.generateTimeBasedUUID();
		String executionId = uuid_local.toString();
		executionId = executionId.replaceAll("-", "");
		boolean propertiesLoaded = false;
		if (flgTemplateStandard.equalsIgnoreCase("false")) {
			logger.debug("The template is a .ZIP file");
			File fileZip = new File(getBirtExecutionTempDir(executionId), JS_FILE_ZIP + JS_EXT_ZIP);
			FileOutputStream foZip = new FileOutputStream(fileZip);
			foZip.write(templateContent);
			foZip.close();
			util.unzip(fileZip, getBirtExecutionTempDir(executionId));
			JarFile zipFile = new JarFile(fileZip);
			Enumeration totalZipEntries = zipFile.entries();
			File jarFile = null;
			while (totalZipEntries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) totalZipEntries.nextElement();
				if (entry.getName().endsWith(".jar")) {
					jarFile = new File(getBirtExecutionTempDirName(executionId) + entry.getName());
					// set classloader with jar
					ClassLoader previous = Thread.currentThread().getContextClassLoader();
					DynamicClassLoader dcl = new DynamicClassLoader(jarFile, previous);
					Thread.currentThread().setContextClassLoader(dcl);
				} else if (entry.getName().endsWith(".rptdesign")) {
					// set InputStream with report
					File birtFile = new File(getBirtExecutionTempDirName(executionId) + entry.getName());
					InputStream isBirt = new FileInputStream(birtFile);
					byte[] templateRptDesign = new byte[0];
					templateRptDesign = util.getByteArrayFromInputStream(isBirt);
					is = new java.io.ByteArrayInputStream(templateRptDesign);
				}
				if (entry.getName().endsWith(".properties")) {
					propertiesLoaded = true;
				}
			}
			String resourcePath = getBirtExecutionTempDirName(executionId);
			if (resourcePath != null) {
				this.birtReportEngine.getConfig().setResourcePath(resourcePath);
			}

		} else {
			String resPath = BirtEngineConfig.getEngineResourcePath();
			if (resPath != null) {
				logger.debug("Resource path is [" + resPath + "]");
				this.birtReportEngine.getConfig().setResourcePath(resPath);
			} else {
				logger.debug("Resource path is null");
				// TODO: should I throw an exception here?
			}
		}

		logger.debug("OUT");
		return is;
	}

	protected Map findReportParams(HttpServletRequest request, IReportRunnable design) throws ConnectionDefinitionException {
		logger.debug("IN");
		String dateformat = request.getParameter("dateformat");
		if (dateformat != null) {
			dateformat = dateformat.replaceAll("D", "d");
			dateformat = dateformat.replaceAll("m", "M");
			dateformat = dateformat.replaceAll("Y", "y");
		}

		HashMap toReturn = new HashMap();
		IGetParameterDefinitionTask task = birtReportEngine.createGetParameterDefinitionTask(design);
		Collection paramsColl = task.getParameterDefns(false);
		Iterator it = paramsColl.iterator();
		while (it.hasNext()) {
			IScalarParameterDefn param = (IScalarParameterDefn) it.next();
			String paramName = param.getName();
			String paramValueString = request.getParameter(paramName);
			paramValueString = decodeParameter(paramValueString);

			if (paramValueString == null || paramValueString.trim().equals("")) {
				logger.debug(this.getClass().getName() + "findReportParams() The report parameter " + paramName + " has no values set.");
				continue;
				// logger.debug(this.getClass().getName() +
				// "findReportParams() The report parameter " + paramName
				// + " has no values set. Gets default value.");
				// paramValueString = param.getDefaultValue();
			}

			int paramType = param.getDataType();
			/*
			 * The ParameterConverter converts a single value. Multi-value parameters are assumed to contains values that are String type. If they are not
			 * Strings (list of dates, list of numbers, ...) the converter will not work.
			 */
			Object paramValue = ParameterConverter.convertParameter(paramType, paramValueString, dateformat);
			if (paramValue == null)
				paramValue = paramValueString;

			toReturn.put(paramName, paramValue);
			logger.debug("PUT " + paramName + "/" + paramValueString);

		}
		logger.debug("OUT");
		return toReturn;
	}

	private String getBirtExecutionTempDirName(String executionId) {
		logger.debug("IN");
		String jrTempDir = OUTPUT_FOLDER + "reports" + File.separator + executionId + File.separator;
		logger.debug("OUT");
		return jrTempDir;
	}

	private File getBirtExecutionTempDir(String executionId) {
		logger.debug("IN");
		File jrTempDir = null;
		String jrTempDirStr = getBirtExecutionTempDirName(executionId);
		jrTempDir = new File(jrTempDirStr.substring(0, jrTempDirStr.length() - 1));
		jrTempDir.mkdirs();
		logger.debug("OUT");
		return jrTempDir;
	}

	/**
	 * @param params
	 * @param parName
	 * @param parValue
	 */
	private void addParToParMap(Map params, String parName, String parValue) {
		logger.debug("IN.parName:" + parName + " /parValue:" + parValue);
		String newParValue;

		ParametersDecoder decoder = new ParametersDecoder();
		if (decoder.isMultiValues(parValue)) {
			List values = decoder.decode(parValue);
			newParValue = "";
			for (int i = 0; i < values.size(); i++) {
				newParValue += (i > 0 ? "," : "");
				newParValue += values.get(i);
			}

		} else {
			newParValue = parValue;
		}

		params.put(parName, newParValue);
		logger.debug("OUT");
	}

	/**
	 *
	 * @param documentId
	 * @return jndi connection
	 * @throws ConnectionDefinitionException
	 */
	private IDataSource findDataSource(HttpSession session, String userId, String documentId, String requestConnectionName)
			throws ConnectionDefinitionException {
		logger.debug("IN");
		if (documentId == null) {
			logger.error("Document identifier NOT found. Returning null.");
			throw new ConnectionParameterNotValidException("No default connection defined in " + "engine-config.xml file.");
		}
		DataSourceServiceProxy proxyDS = new DataSourceServiceProxy(userId, session);
		IDataSource ds = null;
		if (requestConnectionName != null) {
			ds = proxyDS.getDataSourceByLabel(requestConnectionName);
		} else {
			ds = proxyDS.getDataSource(documentId);
		}
		if (ds == null) {
			logger.warn("Data Source IS NULL. There are problems reading DataSource informations");
			return null;
		}
		logger.debug("OUT");
		return ds;
	}

	protected void runReport(HttpServletRequest request, HttpServletResponse response) throws Exception {
		logger.debug("IN");
		HttpSession session = request.getSession();
		IEngUserProfile profile = (IEngUserProfile) session.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		String userId = (String) profile.getUserUniqueIdentifier();
		logger.debug("userId=" + userId);
		String documentId = request.getParameter("document");
		logger.debug("documentId=" + documentId);

		ServletContext servletContext = getServletContext();
		this.birtReportEngine = BirtEngine.getBirtEngine(request, servletContext);
		IReportRunnable design = null;

		// retrieve once in order to convert into string and check if contains javascript for progressive view
		InputStream isToString = getTemplateContent(request);
		int n = isToString.available();
		byte[] bytes = new byte[n];
		isToString.read(bytes, 0, n);
		String result = new String(bytes, StandardCharsets.UTF_8);
		isToString.close();

		// reds again in order to make template

		InputStream is = getTemplateContent(request);
		logger.debug("runReport(): template document retrieved.");
		// Open the report design
		design = birtReportEngine.openReportDesign(is);

		Map params = new HashMap();
		Enumeration enumer = request.getParameterNames();
		String parName = null;
		String parValue = null;
		logger.debug("Reading request parameters...");
		while (enumer.hasMoreElements()) {
			parName = (String) enumer.nextElement();
			parValue = request.getParameter(parName);
			addParToParMap(params, parName, parValue);
			logger.debug("Read parameter [" + parName + "] with value [" + parValue + "] from request");
		}
		logger.debug("Request parameters read sucesfully" + params);

		SsoServiceInterface proxyService = SsoServiceFactory.createProxyService();
		String token = proxyService.readTicket(session);

		String kpiUrl = EnginConf.getInstance().getSpagoBiServerUrl() + "/publicjsp/kpiValueXml.jsp?SECURITY_TOKEN=" + token + "&USERID=" + userId;
		// String kpiUrl =
		// EnginConf.getInstance().getSpagoBiServerUrl()+"/testXml.jsp?"+"USERID="+userId;

		Locale locale = null;

		String language = null;
		String country = null;

		String languageOverride = request.getParameter("LanguageOverride");
		if (languageOverride != null) {
			language = languageOverride;
		} else {
			language = request.getParameter("SBI_LANGUAGE");
		}
		String countryOverride = request.getParameter("CountryOverride");
		if (countryOverride != null) {
			country = countryOverride;
		} else {
			country = request.getParameter("SBI_COUNTRY");
		}

		if (language != null && country != null) {
			locale = new Locale(language, country, "");
		} else {
			locale = Locale.ENGLISH;
		}
		String outputFormat = request.getParameter("outputType");
		logger.debug("outputType -- [" + outputFormat + "]");
		String outputType = getOutputType(outputFormat);

		boolean progressiveViewing = false;
		boolean isHTML = outputType.equalsIgnoreCase("text/html") ? true : false;
		boolean containsJQuery = false;

		// check if there is script for progressive pagination inside the report
		if (result.contains("<script src=\"./js/lib/jquery")) {
			containsJQuery = true;
			logger.debug("JQuery to enable progressive viewing is contained");
		}
		if (isHTML && containsJQuery) {
			logger.debug("Enabling progressive viewing");
			progressiveViewing = true;
		}

		logger.debug("outputFormat -- [" + outputFormat + "]");
		logger.debug("outputType -- [" + outputType + "]");
		logger.debug("Run birt with progressive viewing -- [" + progressiveViewing + "]");

		logger.debug("runReport(): report design opened successfully.");

		// task.setParameterValues(reportParams); NO PATCH
		// task.validateParameters();
		// Map userProfileAttrs = UserProfileUtils.getProfileAttributes(profile); // NO PATCH
		// Map context = getTaskContext(userId, params, request, resPathJNDI, userProfileAttrs); // NO PATCH
		// task.setAppContext(context); NO PATCH
		// renderOption.setOutputStream(response.getOutputStream());
		// task.setRenderOption(renderOption);
		// Create task to run and render the report,
		// IRunAndRenderTask task = birtReportEngine.createRunAndRenderTask(design); // NO PATCH
		// task.setLocale(locale);
		logger.debug("runReport(): RunAndRenderTask created successfully.");

		// Set parameters for the report
		Map reportParams = findReportParams(request, design);

		String requestConnectionName = request.getParameter(CONNECTION_NAME);
		logger.debug("requestConnectionName:" + requestConnectionName);
		IDataSource ds = findDataSource(request.getSession(), userId, documentId, requestConnectionName);
		if (ds != null) {
			logger.debug("DataSource founded.");

			if (ds.checkIsJndi()) {

				if (ds.checkIsMultiSchema()) {
					String schema = null;
					try {
						String attrname = ds.getSchemaAttribute();
						if (attrname != null)
							schema = (String) profile.getUserAttribute(attrname);
					} catch (EMFInternalError e) {
						logger.error("Cannot retrive ENTE", e);
					}
					reportParams.put("connectionName", ds.getJndi() + schema);
				} else {
					reportParams.put("connectionName", ds.getJndi());
				}

			} else {
				reportParams.put("driver", ds.getDriver());
				reportParams.put("url", ds.getUrlConnection());
				reportParams.put("user", ds.getUser());
				reportParams.put("pwd", (ds.getPwd().equals("")) ? " " : ds.getPwd());

			}
		}

		reportParams.put("KpiDSXmlUrl", kpiUrl);

		// gets static resources with SBI_RESOURCE_PATH system's parameter
		String resPathJNDI = EnginConf.getInstance().getResourcePath();
		String resourcePath = resPathJNDI + File.separatorChar + "img" + File.separatorChar;
		String entity = (String) reportParams.get(SpagoBIConstants.SBI_ENTITY);
		// IF exist an ENTITY parameter concat to resourcePath
		if (entity != null && entity.length() > 0) {
			resourcePath = resourcePath.concat(entity + File.separatorChar);
		}
		logger.debug("SetUp resourcePath:" + resourcePath);
		reportParams.put("SBI_RESOURCE_PATH", resourcePath);

		Map userProfileAttrs = UserProfileUtils.getProfileAttributes(profile);
		Map context = getTaskContext(userId, params, request, resPathJNDI, userProfileAttrs);

		String templateFileName = request.getParameter("template_file_name");
		logger.debug("templateFileName -- [" + templateFileName + "]");
		if (templateFileName == null || templateFileName.trim().equals(""))
			templateFileName = "report";
		IRenderOption renderOption = null;

		if (outputFormat != null && outputFormat.equalsIgnoreCase(IBirtConstants.PDF_RENDER_FORMAT)) {
			renderOption = new PDFRenderOption();
			renderOption.setOutputFormat(IBirtConstants.PDF_RENDER_FORMAT);
			// renderOption.setSupportedImageFormats("JPG;jpg;PNG;png;BMP;bmp;SVG;svg;GIF;gif");
			response.setContentType("application/pdf");
			response.setHeader("Content-disposition", "inline; filename=" + templateFileName + ".pdf");
		} else if (outputFormat != null && outputFormat.equalsIgnoreCase(IBirtConstants.HTML_RENDER_FORMAT)) {
			renderOption = prepareHtmlRenderOption(request);
			renderOption.setOutputFormat(IBirtConstants.HTML_RENDER_FORMAT);
			response.setHeader("Content-Type", "text/html");
			response.setContentType("text/html");
		} else if (outputFormat != null && outputFormat.equalsIgnoreCase(IBirtConstants.DOC_RENDER_FORMAT)) {
			renderOption = new RenderOption();
			setMSOfficeEmitterId("doc", renderOption);
			response.setContentType("application/msword");
			response.setHeader("Content-disposition", "inline; filename=" + templateFileName + ".doc");
		} else if (outputFormat != null && outputFormat.equalsIgnoreCase("docx")) {
			renderOption = new RenderOption();
			setMSOfficeEmitterId("docx", renderOption);
			response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
			response.setHeader("Content-disposition", "inline; filename=" + templateFileName + ".docx");
		} else if (outputFormat != null && outputFormat.equalsIgnoreCase(RTF_FORMAT)) {
			renderOption = prepareHtmlRenderOption(request);
			renderOption.setOutputFormat(RTF_FORMAT);
			response.setContentType("application/rtf");
			response.setHeader("Content-disposition", "inline; filename=" + templateFileName + ".rtf");
		} else if (outputFormat != null && outputFormat.equalsIgnoreCase(IBirtConstants.EXCEL_RENDER_FORMAT)) {
			renderOption = new EXCELRenderOption();
			setMSOfficeEmitterId("xls", renderOption);
			response.setContentType("application/vnd.ms-excel");
			response.setHeader("Content-disposition", "inline; filename=" + templateFileName + ".xls");
		} else if (outputFormat != null && outputFormat.equalsIgnoreCase("xlsx")) {
			renderOption = new EXCELRenderOption();
			renderOption.setOption("excel_native_charts", false);
			setMSOfficeEmitterId("xlsx", renderOption);
			response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			response.setHeader("Content-disposition", "inline; filename=" + templateFileName + ".xlsx");
		} else if (outputFormat != null && outputFormat.equalsIgnoreCase("ppt")) {
			renderOption = new RenderOption();
			setMSOfficeEmitterId("ppt", renderOption);
			response.setContentType("application/vnd.ms-powerpoint");
			response.setHeader("Content-disposition", "inline; filename=" + templateFileName + ".ppt");
		} else if (outputFormat != null && outputFormat.equalsIgnoreCase("pptx")) {
			renderOption = new RenderOption();
			setMSOfficeEmitterId("pptx", renderOption);
			response.setContentType("application/vnd.openxmlformats-officedocument.presentationml.presentation");
			response.setHeader("Content-disposition", "inline; filename=" + templateFileName + ".pptx");
		} else if (outputFormat != null && outputFormat.equalsIgnoreCase(IBirtConstants.POSTSCRIPT_RENDER_FORMAT)) {
			renderOption = new PDFRenderOption();
			renderOption.setOutputFormat(IBirtConstants.POSTSCRIPT_RENDER_FORMAT);
			// renderOption.setOutputFileName(templateFileName + ".ps");
			response.setHeader("Content-disposition", "inline; filename=" + templateFileName + ".ps");
		} else if (outputFormat != null && outputFormat.equalsIgnoreCase(DataExtractionParameterUtil.EXTRACTION_FORMAT_CSV)) {
			logger.debug(" Output format parameter is CSV. Create document obj .");
			prepareCSVRender(reportParams, request, design, userId, documentId, profile, kpiUrl, response, context);
			return;

		} else {
			logger.debug(" Output format parameter not set or not valid. Using default output format: HTML.");
			outputFormat = IBirtConstants.HTML_RENDER_FORMAT;
			renderOption = prepareHtmlRenderOption(request);
			renderOption.setOutputFormat(IBirtConstants.HTML_RENDER_FORMAT);
			response.setContentType("text/html");
			response.setHeader("Content-Type", "text/html");
		}

		IRunAndRenderTask runAndRenderTask = null;
		IRunTask runTask = null;

		if (progressiveViewing == false) { // Traditional run and rendering
			// Create task to run and render the report,
			runAndRenderTask = birtReportEngine.createRunAndRenderTask(design);
			runAndRenderTask.setLocale(locale);
			runAndRenderTask.setParameterValues(reportParams);
			runAndRenderTask.validateParameters();
			runAndRenderTask.setAppContext(context);
			renderOption.setOutputStream(response.getOutputStream());
			runAndRenderTask.setRenderOption(renderOption);
			logger.debug("runReport(): RunAndRenderTask created successfully.");
		} else { // progressive run
			ProgressiveCustomPageHandler myPageHandler = new ProgressiveCustomPageHandler((String) context.get(REPORT_EXECUTION_ID));
			runTask = birtReportEngine.createRunTask(design);
			runTask.setLocale(locale);
			runTask.setPageHandler(myPageHandler);
			runTask.setParameterValues(reportParams);
			runTask.validateParameters();
			runTask.setAppContext(context);
			renderOption.setOutputStream(response.getOutputStream());
		}

		String reportExecutionId = (String) context.get(REPORT_EXECUTION_ID);
		String pattern = "^[a-zA-Z0-9]*$";
		if (!reportExecutionId.matches(pattern)) {
			throw new RuntimeException("Security Exception: Report Executio Id [" + reportExecutionId + "] is not alfanumeric.");
		}

		logger.debug("Execution id is : " + reportExecutionId);

		try {
			if (progressiveViewing == false) {
				runAndRenderTask.run();
			} else {
				String file = OUTPUT_FOLDER + reportExecutionId + ".rptdocument";
				logger.debug("output file path is  " + file);

				runTask.run(file);

				Utils.sendPage(response, 1, (String) context.get(REPORT_EXECUTION_ID));

			}
		} catch (Exception e) {
			throw e; // we throw the exception as it is, since this is caught in
		} finally {
			if (runAndRenderTask != null)
				runAndRenderTask.close();
			if (runTask != null)
				runTask.close();
			if (is != null)
				is.close();
		}

		// NO PATCH
		// task.setParameterValues(reportParams); NO PATCH
		// task.validateParameters();
		// task.setAppContext(context); NO PATCH
		// renderOption.setOutputStream(response.getOutputStream());
		// task.setRenderOption(renderOption);
		// Create task to run and render the report,
		// IRunAndRenderTask task = birtReportEngine.createRunAndRenderTask(design); // NO PATCH
		// task.setLocale(locale);

		// setting HTML header if output format is HTML: this is necessary in
		// order to inject the document.domain directive
		// commented by Davide Zerbetto on 12/10/2009: there are problems with
		// MIF (Ext ManagedIFrame library) library
		/*
		 * if (outputFormat.equalsIgnoreCase(IBirtConstants.HTML_RENDER_FORMAT)) { ((HTMLRenderOption) renderOption).setEmbeddable(true);
		 * injectHTMLHeader(response); }
		 */
		// try {
		// task.run();
		// } catch (Exception e) {
		// logger.error("Error while running the report: " + e);
		// e.printStackTrace();
		// }
		// task.close();
		// commented by Davide Zerbetto on 12/10/2009: there are problems with
		// MIF (Ext ManagedIFrame library) library
		/*
		 * if (outputFormat.equalsIgnoreCase(IBirtConstants.HTML_RENDER_FORMAT)) { injectHTMLFooter(response); }
		 */

		logger.debug("OUT");

	}

	public static String createNewExecutionId() {
		String executionId = null;
		UUIDGenerator uuidGen = UUIDGenerator.getInstance();
		UUID uuidObj = uuidGen.generateTimeBasedUUID();
		executionId = uuidObj.toString();
		executionId = executionId.replaceAll("-", "");
		return executionId;
	}

	private void setMSOfficeEmitterId(String output, IRenderOption renderOption) {
		// change emitter according to engine-config.xml
		SourceBean engineConfig = EnginConf.getInstance().getConfig();
		if (engineConfig == null) {
			renderOption.setOutputFormat(output);
			logger.warn("Could not find engine config file");
			return;
		}
		// Assert.assertNotNull(engineConfig, "Could not find engine configuration file");
		String attributeName = output.toUpperCase() + "_EMITTER";
		SourceBean emitterConf = (SourceBean) engineConfig.getAttribute(attributeName);
		if (emitterConf == null) {
			renderOption.setOutputFormat(output);
			logger.debug("Could not find emitter configuration for output = [" + output + "]. Using default emitter.");
			return;
		}
		// Assert.assertNotNull(emitterConf, "Could not find Excel emitter configuration");
		String emitterId = (String) emitterConf.getAttribute("id");
		String outputFormat = (String) emitterConf.getAttribute("output_format");
		logger.debug("Using emitter [" + emitterId + "] with output format [" + outputFormat + "] ...");
		renderOption.setOption(IRenderOption.EMITTER_ID, emitterId);
		renderOption.setOutputFormat(outputFormat);
	}

	private Map getTaskContext(String userId, Map reportParams, HttpServletRequest request, String resourcePath, Map userProfileAttrs) throws IOException {
		Map context = BirtUtility.getAppContext(request);

		String spagoBiServerURL = EnginConf.getInstance().getSpagoBiServerUrl();
		HttpSession session = request.getSession();
		String secureAttributes = (String) session.getAttribute("isBackend");
		String serviceUrlStr = null;
		SourceBean engineConfig = EnginConf.getInstance().getConfig();
		if (engineConfig != null) {
			SourceBean sourceBeanConf = (SourceBean) engineConfig.getAttribute("DataSetServiceProxy_URL");
			serviceUrlStr = sourceBeanConf.getCharacters();
		}

		SsoServiceInterface proxyService = SsoServiceFactory.createProxyService();
		String token = proxyService.readTicket(session);

		context.put(REPORT_EXECUTION_ID, createNewExecutionId());
		context.put("RESOURCE_PATH_JNDI_NAME", resourcePath);
		context.put("SBI_BIRT_RUNTIME_IS_RUNTIME", "true");
		context.put("SBI_BIRT_RUNTIME_USER_ID", userId);
		context.put("SBI_BIRT_RUNTIME_SECURE_ATTRS", secureAttributes);
		context.put("SBI_BIRT_RUNTIME_SERVICE_URL", serviceUrlStr);
		context.put("SBI_BIRT_RUNTIME_SERVER_URL", spagoBiServerURL);
		context.put("SBI_BIRT_RUNTIME_TOKEN", token);
		context.put("SBI_BIRT_RUNTIME_PARS_MAP", reportParams);
		context.put("SBI_BIRT_RUNTIME_PROFILE_USER_ATTRS", userProfileAttrs);
		context.put("SBI_BIRT_RUNTIME_GROOVY_SCRIPT_FILE_NAME", predefinedGroovyScriptFileName);
		context.put("SBI_BIRT_RUNTIME_JS_SCRIPT_FILE_NAME", predefinedJsScriptFileName);
		context.put("SESSION", session);

		return context;
	}

	/**
	 * This method injects the HTML header into the report HTML output. This is necessary in order to inject the document.domain javascript directive
	 */
	/*
	 * commented by Davide Zerbetto on 12/10/2009: there are problems with MIF (Ext ManagedIFrame library) library protected void
	 * injectHTMLHeader(HttpServletResponse response) throws IOException { logger.debug("IN"); String header = null; try { SourceBean config =
	 * EnginConf.getInstance().getConfig(); SourceBean htmlHeaderSb = (SourceBean) config.getAttribute("HTML_HEADER"); header = htmlHeaderSb.getCharacters(); if
	 * (header == null || header.trim().equals("")) { throw new Exception("HTML_HEADER not configured"); } header = header.replaceAll("\\$\\{SBI_DOMAIN\\}",
	 * EnginConf.getInstance().getSpagoBiDomain()); } catch (Exception e) { logger .error("Error while retrieving HTML_HEADER from engine configuration.", e);
	 * logger.info("Using default HTML header", e); StringBuffer buffer = new StringBuffer(); buffer.append(
	 * "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">" ); buffer.append(
	 * "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"></meta>" ); buffer.append("  <script type=\"text/javascript\">");
	 * buffer.append("    document.domain='" + EnginConf.getInstance().getSpagoBiDomain() + "';"); buffer.append("  </script>"); buffer.append("</head><body>");
	 * header = buffer.toString(); } response.getOutputStream().write(header.getBytes()); logger.debug("OUT"); }
	 */

	/**
	 * This method injects the HTML footer into the report HTML output. See injectHTMLHeader method
	 *
	 * @param context
	 */
	/*
	 * commented by Davide Zerbetto on 12/10/2009: there are problems with MIF (Ext ManagedIFrame library) library protected void
	 * injectHTMLFooter(HttpServletResponse response) throws IOException { logger.debug("IN"); String footer = null; try { SourceBean config =
	 * EnginConf.getInstance().getConfig(); SourceBean htmlHeaderSb = (SourceBean) config.getAttribute("HTML_FOOTER"); footer = htmlHeaderSb.getCharacters(); if
	 * (footer == null || footer.trim().equals("")) { throw new Exception("HTML_FOOTER not configured"); } } catch (Exception e) { logger.
	 * error("Error while retrieving HTML_FOOTER from engine configuration.", e); logger.info("Using default HTML footer", e); StringBuffer buffer = new
	 * StringBuffer(); buffer.append("</body></html>"); footer = buffer.toString(); } response.getOutputStream().write(footer.getBytes()); logger.debug("OUT");
	 * }
	 */

	private void prepareCSVRender(Map reportParams, HttpServletRequest request, IReportRunnable design, String userId, String documentId,
			IEngUserProfile profile, String kpiUrl, HttpServletResponse response, Map context) throws Exception {

		logger.debug("IN");

		IReportDocument reportDocument = null;

		try {

			reportDocument = getReportDocument(design, reportParams, context);

			logger.debug("Report document obtained for report " + design.getReportName() + "; pages count = " + reportDocument.getPageCount());

			IDataExtractionTask dataExtractionTask = null;

			try {
				// *** Create the data extraction task ****
				dataExtractionTask = birtReportEngine.createDataExtractionTask(reportDocument);
				List resultSetList = dataExtractionTask.getResultSetList();
				if (resultSetList.size() == 0) {
					throw new SpagoBIRuntimeException("No result sets found!!");
				}

				ICSVDataExtractionOption extractionOptions = new CSVDataExtractionOption();

				String encoding = getCSVEncoding();
				extractionOptions.setEncoding(encoding);

				OutputStream responseOut = response.getOutputStream();

				extractionOptions.setOutputFormat("csv");
				extractionOptions.setSeparator(";");

				// find if there is a resultset named FLAGGED_CSV_EXPORT_RESULT_SET_NAME
				// @formatter:off
				IResultSetItem flaggedExportResultSet = (IResultSetItem) resultSetList.stream()
						.filter(item -> FLAGGED_CSV_EXPORT_RESULT_SET_NAME.equalsIgnoreCase(((IResultSetItem) item).getResultSetName()))
						.findAny()
						.orElse(null);
				// @formatter:on

				if (flaggedExportResultSet != null) {
					logger.debug("Found ExportData Element in report; extracting it...");
					extractSingleResultSet(response, dataExtractionTask, extractionOptions, responseOut, flaggedExportResultSet);
					logger.debug("Result set " + flaggedExportResultSet.getResultSetName() + " extracted succesfully");
				} else {
					// ExtractData element not found, search all element to export
					// check if there is only a result set and generate one CSV file
					if (resultSetList.size() == 1) {
						logger.debug("Found only one result set; extracting it...");
						IResultSetItem resultSet = (IResultSetItem) resultSetList.get(0);
						extractSingleResultSet(response, dataExtractionTask, extractionOptions, responseOut, resultSet);
						logger.debug("Result set " + resultSet.getResultSetName() + " extracted succesfully");
					} else {
						// with more resultSets generate a zip file containing more CSV files
						logger.debug("Found more than one result sets; extracting all of them...");
						extractAllResultSets(response, dataExtractionTask, resultSetList, extractionOptions, responseOut);
						logger.debug("All result sets extracted succesfully");
					}
				}
			} catch (Exception e) {
				throw new SpagoBIRuntimeException("Error while extracting data from report " + design.getReportName(), e);
			} finally {
				if (dataExtractionTask != null) {
					// close the extraction task
					dataExtractionTask.close();
				}
			}

		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while getting report document for report " + design.getReportName(), e);
		} finally {
			if (reportDocument != null) {
				// close the report document
				reportDocument.close();
			}
		}

		logger.debug("OUT");
	}

	protected void extractAllResultSets(HttpServletResponse response, IDataExtractionTask dataExtractionTask, List resultSetList,
			ICSVDataExtractionOption extractionOptions, OutputStream responseOut) throws BirtException {
		try {
			// Set the HTTP response
			response.setContentType("application/zip");
			response.setHeader("Content-disposition", "attachment; filename=reportcsv.zip");

			// ZipOutputStream directly on the response OutputStream
			ZipOutputStream outZip = new ZipOutputStream(responseOut);

			// temporary output buffer that contain a single csv
			OutputStream tempOut = new ByteArrayOutputStream();

			// temporary input buffer
			InputStream tempIn;

			// Create a buffer for reading the files
			byte[] buf = new byte[1024];

			// extracted csv is writed on the temp buffer
			extractionOptions.setOutputStream(tempOut);

			// iterate the resultSetList
			for (int i = 0; i < resultSetList.size(); i++) {

				// get an item
				IResultSetItem resultItem = (IResultSetItem) resultSetList.get(i);

				// Set the name of the element you want to retrieve.
				String dispName = resultItem.getResultSetName();
				dataExtractionTask.selectResultSet(dispName);
				dataExtractionTask.extract(extractionOptions);
				logger.debug("Extraction successfull " + dispName);

				// Add ZIP entry to ZIP output stream
				outZip.putNextEntry(new ZipEntry("reportcsv" + i + ".csv"));

				// convert temp outputStream to InputStream
				tempIn = new ByteArrayInputStream(((ByteArrayOutputStream) tempOut).toByteArray());

				// Transfer bytes from the temp buffer to the ZIP file
				int len;
				while ((len = tempIn.read(buf)) > 0) {
					outZip.write(buf, 0, len);
				}

				// Complete the entry
				outZip.closeEntry();
				tempIn.close();

				// reset the temp output buffer
				((ByteArrayOutputStream) tempOut).reset();
			}
			// **************************

			// Complete the ZIP file
			outZip.close();

		} catch (IOException e) {
			logger.error("Error while generating csv zip file", e);
		}
	}

	protected void extractSingleResultSet(HttpServletResponse response, IDataExtractionTask dataExtractionTask, ICSVDataExtractionOption extractionOptions,
			OutputStream responseOut, IResultSetItem resultSet) throws BirtException {
		// output directly on the response OutputStream
		extractionOptions.setOutputStream(responseOut);

		// Set the HTTP response
		response.setContentType("text/csv");
		response.setHeader("Content-disposition", "inline; filename=reportcsv.csv");
		dataExtractionTask.selectResultSet(resultSet.getResultSetName());
		dataExtractionTask.extract(extractionOptions);
		logger.debug("Extraction successfull " + resultSet.getResultSetName());
	}

	protected String getCSVEncoding() {
		String encoding = null;
		// get CSV encoding according to engine-config.xml
		SourceBean engineConfig = EnginConf.getInstance().getConfig();
		if (engineConfig != null) {
			SourceBean sourceBeanConf = (SourceBean) engineConfig.getAttribute("CSV_EMITTER_ENCODING");
			if (sourceBeanConf != null && !sourceBeanConf.getCharacters().trim().equalsIgnoreCase("")) {
				encoding = sourceBeanConf.getCharacters();
			}
		}
		if (encoding == null) {
			logger.debug("No configuration found for CSV encoding. Considering UTF-8 as default...");
			encoding = StandardCharsets.UTF_8.name();
		} else {
			logger.debug("CSV encoding found from configuration: [" + encoding + "]");
		}
		return encoding;
	}

	protected IReportDocument getReportDocument(IReportRunnable design, Map reportParams, Map context) throws EngineException {
		logger.debug("IN");
		IReportDocument toReturn = null;
		IRunTask runTask = null;

		try {
			// Create task to run the report
			runTask = birtReportEngine.createRunTask(design);

			// **** Set parameters for the report ****
			runTask.setParameterValues(reportParams);
			runTask.validateParameters();
			// ************************************

			String executionId = generateExecutionId();

			String nameFile = getBirtExecutionTempDirName(executionId) + "csvreport.rptdocument";

			runTask.setAppContext(context);

			// Run the report and create the rptdocument
			runTask.run(nameFile);

			// Open the rptdocument
			toReturn = birtReportEngine.openReportDocument(nameFile);

			logger.debug("Report document successfully retrieved for report " + design.getReportName());

		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while getting report document for report " + design.getReportName(), e);
		} finally {
			if (runTask != null) {
				// close the task
				runTask.close();
			}
		}
		logger.debug("OUT");
		return toReturn;
	}

	protected String generateExecutionId() {
		UUIDGenerator uuidGen = UUIDGenerator.getInstance();
		UUID uuid_local = uuidGen.generateTimeBasedUUID();
		String executionId = uuid_local.toString();
		executionId = executionId.replaceAll("-", "");
		return executionId;
	}

	private String getOutputType(String outputFormat) {
		String toReturn = null;
		if (outputFormat != null && outputFormat.equalsIgnoreCase(IBirtConstants.PDF_RENDER_FORMAT)) {
			toReturn = "application/pdf";
		} else if (outputFormat != null && outputFormat.equalsIgnoreCase(IBirtConstants.HTML_RENDER_FORMAT)) {
			toReturn = "text/html";
		} else if (outputFormat != null && outputFormat.equalsIgnoreCase(IBirtConstants.DOC_RENDER_FORMAT)) {
			toReturn = "application/msword";
		} else if (outputFormat != null && outputFormat.equalsIgnoreCase(RTF_FORMAT)) {
			toReturn = "application/rtf";
		} else if (outputFormat != null && outputFormat.equalsIgnoreCase(IBirtConstants.EXCEL_RENDER_FORMAT)) {
			toReturn = "application/vnd.ms-excel";
		} else if (outputFormat != null && outputFormat.equalsIgnoreCase("xlsx")) {
			toReturn = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
		} else if (outputFormat != null && outputFormat.equalsIgnoreCase("ppt")) {
			toReturn = "application/vnd.ms-powerpoint";
		} else if (outputFormat != null && outputFormat.equalsIgnoreCase(IBirtConstants.POSTSCRIPT_RENDER_FORMAT)) {
			toReturn = "IBirtConstants.POSTSCRIPT_RENDER_FORMAT";
		} else if (outputFormat != null && outputFormat.equalsIgnoreCase(DataExtractionParameterUtil.EXTRACTION_FORMAT_CSV)) {
			toReturn = "CSV";
		} else {
			toReturn = "text/html";
		}
		return toReturn;
	}

	public class ProgressiveCustomPageHandler implements IPageHandler {

		// Define local variables for the callback class
		private int lastCheckpoint = 0;
		private String reportExecutionId = null;

		public ProgressiveCustomPageHandler(String reportExecutionId) {
			this.reportExecutionId = reportExecutionId;
		}

		// @Override
		@Override
		public void onPage(int pageNumber, boolean readyForViewing, IReportDocumentInfo reportDocument) {
			// we only want to do something if this is a checkpoint event
			if (readyForViewing) {
				// Just let the user know that the next page ranges are ready,
				// then set the last check point to the
				// current page
				logger.debug("Pages " + lastCheckpoint + " through " + pageNumber + " are ready for viewing");

				int pageStart;
				int pageEnd;

				if (lastCheckpoint == 0) {
					// pageRange = 1 + "-" + pageNumber;
					pageStart = 1;
					pageEnd = pageNumber;
				} else {
					// pageRange = (lastCheckpoint + 1) + "-" + pageNumber;
					pageStart = lastCheckpoint + 1;
					pageEnd = pageNumber;

				}

				lastCheckpoint = pageNumber;

				IRenderTask task = null;
				IReportDocument iReportDocument = null;
				try {
					// open the report document then create the render task from it
					iReportDocument = reportDocument.openReportDocument();
					task = birtReportEngine.createRenderTask(iReportDocument);

					IRenderOption renderOption = prepareFrontEndHtmlRenderOption();
					renderOption.setOutputFormat(IBirtConstants.HTML_RENDER_FORMAT);

					for (int i = pageStart; i <= pageEnd; i++) {
						renderOption.setOutputFileName(
								BirtReportServlet.OUTPUT_FOLDER + reportExecutionId + File.separator + BirtReportServlet.PAGE_FILE_NAME + i + ".html");

						task.setRenderOption(renderOption);
						logger.debug("Page number " + i + " is ready for viewing");
						task.setPageNumber(i);
						task.render();

					}

				} catch (Exception e) {
					logger.error("Exception while rendering page " + pageNumber, e);
				} finally {
					if (iReportDocument != null)
						iReportDocument.close();
					if (task != null)
						task.close();
				}
			}
		}

	}
}