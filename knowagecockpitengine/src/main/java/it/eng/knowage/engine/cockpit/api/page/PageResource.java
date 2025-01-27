/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.knowage.engine.cockpit.api.page;

import static it.eng.knowage.commons.security.KnowageSystemConfiguration.getKnowageVueContext;
import static it.eng.spagobi.commons.constants.ConfigurationConstants.SPAGOBI_SPAGOBI_SERVICE_JNDI;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import it.eng.spagobi.utilities.mime.MimeUtils;
import it.eng.spagobi.utilities.rest.RestUtilities;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
//import org.apache.jena.ext.com.google.common.collect.Iterables;
import com.google.common.collect.Iterables;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.resteasy.plugins.providers.html.View;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.knowage.engine.cockpit.CockpitEngine;
import it.eng.knowage.engine.cockpit.CockpitEngineInstance;
import it.eng.knowage.engine.cockpit.api.AbstractCockpitEngineResource;
import it.eng.knowage.engine.cockpit.api.export.excel.ExcelExporter;
import it.eng.knowage.engine.cockpit.api.export.pdf.nodejs.PdfExporterV2;
import it.eng.knowage.engine.cockpit.api.export.png.PngExporter;
import it.eng.knowage.export.wrapper.beans.RenderOptions;
import it.eng.knowage.export.wrapper.beans.ViewportDimensions;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.EngineStartServletIOManager;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @authors Andrea Gioia (andrea.gioia@eng.it)
 *
 */

@Path("/1.0/pages")
public class PageResource extends AbstractCockpitEngineResource {

	private static final Logger LOGGER = LogManager.getLogger(PageResource.class);
	private static final String OUTPUT_TYPE = "outputType";
	private static final String PDF_FRONT_PAGE = "pdfFrontPage";
	private static final String PDF_PAGE_ORIENTATION = "pdfPageOrientation";
	private static final String PDF_BACK_PAGE = "pdfBackPage";
	private static final String PDF_WIDTH = "pdfWidth";
	private static final String PDF_HEIGHT = "pdfHeight";
	private static final String PDF_DEVICE_SCALE_FACTOR = "pdfDeviceScaleFactor";
	private static final String PDF_WAIT_TIME = "pdfWaitTime";
	private static final String IS_MULTI_SHEET = "isMultiSheet";
	private static final String USER_ID = "user_id";
	private static final String TOKEN_HEADER = "x-kn-authorization";
	private static final String DOCUMENT_ID = "document";
	private static final String DOCUMENT_LABEL = "DOCUMENT_LABEL";
	private static final String DOCUMENT_NAME = "DOCUMENT_NAME";


	private static Map<String, JSONObject> pages;

	private final Encoder base64Encoder = Base64.getEncoder().withoutPadding();

	static {
		pages = new HashMap<>();

		try {
			pages.put("edit",
					new JSONObject("{name: 'execute', description: 'the cockpit edit page', parameters: []}"));
			pages.put("execute", new JSONObject(
					"{name: 'execute', description: 'the cockpit execution page', parameters: ['template']}"));
			pages.put("test",
					new JSONObject("{name: 'test', description: 'the cockpit test page', parameters: ['template']}"));
		} catch (JSONException t) {
			LOGGER.error(t);
		}
	}

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public String getDataSets() {
		try {
			JSONArray resultsJSON = new JSONArray();
			Iterator<String> it = pages.keySet().iterator();
			while (it.hasNext()) {
				String pageName = it.next();
				resultsJSON.put(pages.get(pageName));
			}

			return resultsJSON.toString();
		} catch (Exception e) {
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException("", getEngineInstance(), e);
		} finally {
			LOGGER.debug("OUT");
		}
	}

	@GET
	@Path("/{pagename}")
	public Object openPageGet(@PathParam("pagename") String pageName) {
		return openPageInternal(pageName);
	}

	@POST
	@Path("/{pagename}")
	public Object openPagePost(@PathParam("pagename") String pageName) {
		return openPageInternal(pageName);
	}

	@GET
	@Path("/{pagename}/pdf")
	public Response openPageGetPdf(@PathParam("pagename") String pageName)
			throws EMFUserError, IOException, InterruptedException, JSONException {
		return openPagePdfInternal(pageName);
	}

	@POST
	@Path("/{pagename}/pdf")
	public Response openPagePostPdf(@PathParam("pagename") String pageName)
			throws EMFUserError, IOException, InterruptedException, JSONException {
		return openPagePdfInternal(pageName);
	}

	@GET
	@Path("/{pagename}/spreadsheet")
	public Response openPageGetSpreadsheet(@PathParam("pagename") String pageName)
			throws IOException, InterruptedException, JSONException {
		return openPageSpreadsheetInternal(pageName);
	}

	@POST
	@Path("/{pagename}/spreadsheet")
	public void openPagePostSpreadsheet(@Context HttpServletRequest req)
			throws IOException, InterruptedException, JSONException {
		logger.debug("IN");
		response.setCharacterEncoding(UTF_8.name());
		try {
			JSONObject body = new JSONObject();
			String token = request.getHeader(TOKEN_HEADER);
			//USER ID IS THAT TOKEN WITHOUR THE BEARER
			String userId = token.substring(7);
			String template = getIOManager().getTemplateAsString();
			body.put("template", template);
//			String outputType = body.getString(OUTPUT_TYPE);
			ExcelExporter excelExporter = new ExcelExporter(userId, body);
			String mimeType = excelExporter.getMimeType();

			if (!MimeUtils.isValidMimeType(mimeType))
				throw new SpagoBIRuntimeException("Invalid mime type: " + mimeType);

			if (mimeType != null) {
				Integer documentId = Integer.valueOf(req.getParameter(DOCUMENT_ID));
				String documentLabel = req.getParameter(DOCUMENT_LABEL);
				String documentName = req.getParameter(DOCUMENT_NAME);
				String options = body.optString("options");
				byte[] data;
				data = excelExporter.getDashboardBinaryData(documentId, documentLabel, documentName, template, options);
				//convert to base64
				String base64 = Base64.getEncoder().encodeToString(data);

				response.setContentType(MediaType.APPLICATION_OCTET_STREAM);
				response.setHeader("Content-length", Integer.toString(data.length));
				response.setHeader("Content-Type", mimeType);
				response.setHeader("Content-Disposition", "attachment; fileName=" + documentLabel + "." + "xlsx");

				response.getOutputStream().write(data, 0, data.length);
				response.getOutputStream().flush();
				response.getOutputStream().close();
			}
		} catch (Exception e) {
			logger.error("Cannot export to Excel", e);
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException("", getEngineInstance(), e);
		} finally {
			logger.debug("OUT");
		}
	}

	@GET
	@Path("/{pagename}/png")
	public Response openPageGetPng(@PathParam("pagename") String pageName)
			throws EMFUserError, IOException, InterruptedException, JSONException {
		return openPagePngInternal(pageName);
	}

	@POST
	@Path("/{pagename}/png")
	public Response openPagePostPng(@PathParam("pagename") String pageName)
			throws EMFUserError, IOException, InterruptedException, JSONException {
		return openPagePngInternal(pageName);
	}

	/**
	 * @return Could be either a {@link View}, for HTML output type and error, or a {@link Response}, for PDF, PNG and Excel.
	 */
	private Object openPageInternal(String pageName) {
		CockpitEngineInstance engineInstance;
		String dispatchUrl = null;
		EngineStartServletIOManager ioManager = getIOManager();
		Map env = ioManager.getEnv();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Opening page {}", pageName);
			Enumeration<String> parameterNames = request.getParameterNames();
			List<String> parameterNamesAsList = Collections.list(parameterNames);
			Collections.sort(parameterNamesAsList);

			LOGGER.debug("Parameters are: ");
			parameterNamesAsList.forEach(name -> {
				String[] parameterValues = request.getParameterValues(name);
				LOGGER.debug("{} = {}", name, Arrays.asList(parameterValues));
			});

			LOGGER.warn("Environment vars are:");
			env.forEach((key, value) -> LOGGER.warn("{} - {}", key, value));
		}

		try {

			/**
			 * Setting the encoding type to the response object, so the Cockpit engine when calling the rendering of the chart (chart.jsp) can display the real content of
			 * the chart template. If this is not set, specific Italian letters, such as ù and à are going to be displayed as black squared question marks - they will not
			 * be displayed as they are specified by the user.
			 *
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			response.setContentType(MediaType.TEXT_HTML);
			response.setCharacterEncoding(UTF_8.name());

			if ("execute".equals(pageName)) {
				String outputType = request.getParameter(OUTPUT_TYPE);
				if ("xls".equalsIgnoreCase(outputType) || "xlsx".equalsIgnoreCase(outputType)) {
					return createRedirect("/spreadsheet");
				} else if ("pdf".equalsIgnoreCase(outputType)) {
					return createRedirect("/pdf");
				} else if ("JPG".equalsIgnoreCase(outputType)) {
					throw new UnsupportedOperationException("This method is not implemented anymore");
				} else if ("PNG".equalsIgnoreCase(outputType)) {
					return createRedirect("/png");
				} else {
					String templateAsString = ioManager.getTemplateAsString();
					engineInstance = CockpitEngine.createInstance(templateAsString, env);
					ioManager.getHttpSession().setAttribute(EngineConstants.ENGINE_INSTANCE, engineInstance);

					String editMode = request.getParameter("documentMode");
					if (editMode != null) {
						editMode = editMode.equals("null") ? null : request.getParameter("documentMode");
					}

					if (editMode != null && editMode.equals("EDIT")) {
						String documentLabel = request.getParameter("DOCUMENT_LABEL");
						BIObject obj = DAOFactory.getBIObjectDAO().loadBIObjectByLabel(documentLabel);
						if (!ObjectsAccessVerifier.canEdit(obj, getUserProfile())) {
							String message = String.format("User [%s] cannot edit this document",
									(String) getUserProfile().getUserId());
							LOGGER.error(message);
							throw new Exception(message);

						}
					}

					dispatchUrl = "/WEB-INF/jsp/ngCockpit.jsp";
				}
			} else if ("edit".equals(pageName)) {
				JSONObject template = null;
				template = buildBaseTemplate();
				// create a new engine instance
				engineInstance = CockpitEngine.createInstance(template.toString(), // servletIOManager.getTemplateAsString(),
						ioManager.getEnvForWidget());
				ioManager.getHttpSession().setAttribute(EngineConstants.ENGINE_INSTANCE, engineInstance);
				dispatchUrl = "/WEB-INF/jsp/ngCockpit.jsp";
			} else {
				// error
				dispatchUrl = "/WEB-INF/jsp/error.jsp";
			}

			return new View(dispatchUrl);
		} catch (Exception e) {
			LOGGER.error("Error opening page", e);
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException("", getEngineInstance(), e);
		} finally {
			LOGGER.debug("OUT");
		}
	}

	private Response createRedirect(String suffix) throws URISyntaxException {
		URI newLocation = createNewLocation(suffix);

		return Response.status(307).header("Location", newLocation).build();
	}

	private URI createNewLocation(String suffix) throws URISyntaxException {
		String requestURL = request.getRequestURI();
		String queryString = request.getQueryString();

		StringBuilder sb = new StringBuilder(requestURL);
		sb.append(suffix);
		if (Objects.nonNull(queryString)) {
			sb.append("?");
			sb.append(queryString);
		}

		return new URI(sb.toString());
	}

	private Response openPagePdfInternal(String pageName)
			throws EMFUserError, IOException, InterruptedException, JSONException {
		String requestURL = getRequestUrlForPdfExport(request);
		RenderOptions renderOptions = getRenderOptionsForPdfExporter(request);

		int documentId = Integer.parseInt(request.getParameter("document"));
		String userId = request.getParameter("user_id");
		String pdfPageOrientation = request.getParameter(PDF_PAGE_ORIENTATION);
		boolean pdfFrontPage = Boolean.parseBoolean(request.getParameter(PDF_FRONT_PAGE));
		boolean pdfBackPage = Boolean.parseBoolean(request.getParameter(PDF_BACK_PAGE));

		PdfExporterV2 pdfExporter = new PdfExporterV2(documentId, userId, requestURL, renderOptions, pdfPageOrientation,
				pdfFrontPage, pdfBackPage);
		byte[] data = pdfExporter.getBinaryData();

		return Response.ok(data, "application/pdf").header("Content-Length", Integer.toString(data.length))
				.header("Content-Disposition",
						"attachment; fileName=" + request.getParameter("DOCUMENT_LABEL") + ".pdf")
				.build();
	}

	private Response openPageSpreadsheetInternal(String pageName)
			throws IOException, InterruptedException, JSONException {
		String requestURL = getRequestUrlForExcelExport(request);

		request.setAttribute("template", getIOManager().getTemplateAsString());

		String userId = request.getParameter("user_id");
		Map<String, String[]> parameterMap = request.getParameterMap();

		String documentLabel = request.getParameter("DOCUMENT_LABEL");

		ExcelExporter excelExporter = new ExcelExporter(userId, parameterMap, requestURL);
		String mimeType = excelExporter.getMimeType();
		byte[] data = excelExporter.getBinaryData(documentLabel);

		return Response.ok(data, mimeType).header("Content-length", Integer.toString(data.length))
				.header("Content-Disposition", "attachment; fileName=" + documentLabel + ".xlsx").build();
	}

	private Response openPagePngInternal(String pageName)
			throws EMFUserError, IOException, InterruptedException, JSONException {
		String requestURL = null;
		String documentLabel = request.getParameter("DOCUMENT_LABEL");
		String viewName = request.getParameter("viewName");
		String viewId = request.getParameter("viewId");
		if (viewName != null && viewId != null) {
			requestURL = getRequestUrlWithViewHandling(documentLabel);
		} else {
			requestURL = getRequestUrlForPdfExport(request);
		}
		RenderOptions renderOptions = getRenderOptionsForPdfExporter(request);

		int documentId = Integer.parseInt(request.getParameter("document"));
		String userId = request.getParameter("user_id");
		String pdfPageOrientation = request.getParameter(PDF_PAGE_ORIENTATION);
		boolean pdfFrontPage = request.getParameter(PDF_FRONT_PAGE) != null
				&& Boolean.parseBoolean(request.getParameter(PDF_FRONT_PAGE));
		boolean pdfBackPage = request.getParameter(PDF_BACK_PAGE) != null
				&& Boolean.parseBoolean(request.getParameter(PDF_BACK_PAGE));

		PngExporter pngExporter = new PngExporter(documentId, userId, requestURL, renderOptions, pdfPageOrientation,
				pdfFrontPage, pdfBackPage);
		byte[] data = pngExporter.getBinaryData();

		boolean isZipped = false;
		
		// TODO non-regression-test 
		int thresholdEntries = 10000;
		int thresholdSize = 1000000000;
		double thresholdRatio = 10;
		int totalSizeArchive = 0;
		int totalEntryArchive = 0;
		
		ZipInputStream zippedInputStream = new ZipInputStream(new ByteArrayInputStream(data));
		ZipEntry zipEntry = null;
		
		while((zipEntry = zippedInputStream.getNextEntry()) != null) {
			
			totalEntryArchive ++;
			
			int nBytes = -1;
			byte[] buffer = new byte[2048];
			int totalSizeEntry = 0;

			while((nBytes = new ZipInputStream(new ByteArrayInputStream(data)).read(buffer)) > 0) {
			      //out.write(buffer, 0, nBytes);
			      totalSizeEntry += nBytes;
			      totalSizeArchive += nBytes;

			      double compressionRatio = (double) totalSizeEntry / zipEntry.getCompressedSize();
			      if(compressionRatio > thresholdRatio) {
			        // ratio between compressed and uncompressed data is highly suspicious, looks like a Zip Bomb Attack
			    	logger.error("Error while unzip file. Invalid archive file");
					throw new SpagoBIRuntimeException("Error while unzip file. Invalid archive file");
			      }
			 } 
			
			if(totalSizeArchive > thresholdSize) {
				// the uncompressed data size is too much for the application resource capacity
				logger.error("Error while unzip file. Invalid archive file");
				throw new SpagoBIRuntimeException("Error while unzip file. Invalid archive file");
			}

			if(totalEntryArchive > thresholdEntries) {
				// too much entries in this archive, can lead to inodes exhaustion of the system
				logger.error("Error while unzip file. Invalid archive file");
				throw new SpagoBIRuntimeException("Error while unzip file. Invalid archive file");
			}
			
			isZipped = new ZipInputStream(new ByteArrayInputStream(data)).getNextEntry() != null;			  
		}

		String mimeType = null;
		String contentDisposition = null;

		if (!isZipped) {
			mimeType = "image/png";
			contentDisposition = "attachment; fileName=" + documentLabel + ".png";
		} else {
			mimeType = "application/zip";
			contentDisposition = "attachment; fileName=" + documentLabel + ".zip";
		}

		return Response.ok(data, mimeType).header("Content-length", Integer.toString(data.length))
				.header("Content-Disposition", contentDisposition).build();
	}

	/**
	 * @param documentLabel
	 * @param viewName
	 * @param viewId
	 * @return
	 */
	private String getRequestUrlWithViewHandling(String documentLabel) {
		URIBuilder externalUrl = getExternalUrl(documentLabel);

		externalUrl.setPath(getKnowageVueContext() + "/workspace/dashboard-view/" + documentLabel);

		request.getParameterMap().forEach((k, v) -> {

			List<String> asList = Arrays.asList(v);
			String collect = asList.stream().collect(Collectors.joining(","));

			externalUrl.setParameter(k, collect);
		});

		addParametersToHideToolbarAndMenuInVue(externalUrl);

		return externalUrl.toString();
	}

	/**
	 * @param documentLabel
	 * @return
	 */
	private URIBuilder getExternalUrl(String documentLabel) {
		BIObject biObject = null;
		try {
			biObject = DAOFactory.getBIObjectDAO().loadBIObjectByLabel(documentLabel);
		} catch (EMFUserError e) {
			throw new SpagoBIRuntimeException("Error retrieving document with label " + documentLabel, e);
		}
		Engine eng = biObject.getEngine();
		return GeneralUtilities.getBE2BEEngineUrl(eng);
	}

	private RenderOptions getRenderOptionsForPdfExporter(HttpServletRequest request) {
		String userId = (String) getUserProfile().getUserUniqueIdentifier();
		String encodedUserId = new String(base64Encoder.encode(userId.getBytes(UTF_8)));
		Map<String, String> headers = new HashMap<>(1);
		headers.put("Authorization", "Direct " + encodedUserId);

		RenderOptions defaultRenderOptions = RenderOptions.defaultOptions();
		ViewportDimensions defaultDimensions = defaultRenderOptions.getDimensions();
		long defaultJsRenderingWait = defaultRenderOptions.getJsRenderingWait();
		int pdfWidth = Integer.parseInt(defaultDimensions.getWidth());
		int pdfHeight = Integer.parseInt(defaultDimensions.getHeight());
		double pdfDeviceScaleFactor = Double.parseDouble(defaultDimensions.getDeviceScaleFactor());
		long pdfRenderingWaitTime = defaultJsRenderingWait;

		String widthParameterVal = request.getParameter(PDF_WIDTH);
		String heightParameterVal = request.getParameter(PDF_HEIGHT);
		String deviceScaleFactorVal = request.getParameter(PDF_DEVICE_SCALE_FACTOR);
		String jsRenderingWaitParameterVal = request.getParameter(PDF_WAIT_TIME);

		Boolean isMultiSheet = Boolean.parseBoolean(request.getParameter(IS_MULTI_SHEET));

		if (widthParameterVal != null) {
			pdfWidth = Integer.parseInt(widthParameterVal);
		}
		if (heightParameterVal != null) {
			pdfHeight = Integer.parseInt(heightParameterVal);
		}
		if (jsRenderingWaitParameterVal != null) {
			pdfRenderingWaitTime = 1000 * Long.parseLong(jsRenderingWaitParameterVal);
		}

		if (deviceScaleFactorVal != null) {
			pdfDeviceScaleFactor = Double.parseDouble(deviceScaleFactorVal);
		}

		ViewportDimensions dimensions = ViewportDimensions.builder().withWidth(pdfWidth).withHeight(pdfHeight)
				.withDeviceScaleFactor(pdfDeviceScaleFactor).withIsMultiSheet(isMultiSheet).build();
		return defaultRenderOptions.withDimensions(dimensions).withJavaScriptExecutionDetails(pdfRenderingWaitTime,
				5000L);
	}

	private String getRequestUrlForPdfExport(HttpServletRequest request) throws JSONException {

		String documentLabel = request.getParameter("DOCUMENT_LABEL");
		BIObject biObject = null;
		try {
			biObject = DAOFactory.getBIObjectDAO().loadBIObjectByLabel(documentLabel);
		} catch (EMFUserError e) {
			throw new SpagoBIRuntimeException("Error retrieving document with label " + documentLabel, e);
		}
		Engine eng = biObject.getEngine();
		URIBuilder externalUrl = GeneralUtilities.getBE2BEEngineUrl(eng);

		if (isDashboard(eng)) {
			manageParametersForDashboards(biObject, documentLabel, externalUrl);
		} else {
			manageParametersForEverythingElse(externalUrl);
		}
		externalUrl.setParameter("export", "true");
		return externalUrl.toString();
	}

	private String getRequestUrlForExcelExport(HttpServletRequest request) throws JSONException {

		String documentLabel = request.getParameter("DOCUMENT_LABEL");
		BIObject biObject = null;
		try {
			biObject = DAOFactory.getBIObjectDAO().loadBIObjectByLabel(documentLabel);
		} catch (EMFUserError e) {
			throw new SpagoBIRuntimeException("Error retrieving document with label " + documentLabel, e);
		}
		Engine eng = biObject.getEngine();
		URIBuilder externalUrl = GeneralUtilities.getBE2BEEngineUrl(eng);

		if (isDashboard(eng)) {
			manageParametersForDashboards(biObject, documentLabel, externalUrl);
		} else {
			manageParametersForEverythingElse(externalUrl);
		}
		externalUrl.setParameter("scheduledexport", "true");
		return externalUrl.toString();
	}

	public String getServiceHostUrl() {
		String serviceURL = SpagoBIUtilities
				.readJndiResource(SingletonConfig.getInstance().getConfigValue(SPAGOBI_SPAGOBI_SERVICE_JNDI));
		serviceURL = serviceURL.substring(0, serviceURL.lastIndexOf('/'));
		return serviceURL;
	}

	@GET
	@Path("/executeTest")
	@Produces(MediaType.APPLICATION_JSON)
	public String testAction(@Context HttpServletResponse response) {

		LOGGER.debug("IN");

		try {
			JSONObject obj = new JSONObject();
			try {
				obj.put("result", "ok");
			} catch (JSONException e) {
				LOGGER.error("Error building the success string");
				throw new SpagoBIRuntimeException("Error building the success string");
			}
			return obj.toString();
		} finally {
			LOGGER.debug("OUT");
		}
	}

	private JSONObject buildBaseTemplate() {
		JSONObject template;

		LOGGER.debug("IN");
		template = new JSONObject();
		LOGGER.debug("OUT");

		return template;
	}

	private void manageParametersForDashboards(BIObject biObject, String documentLabel, URIBuilder uriBuilder)
			throws JSONException {

		uriBuilder.setPath(getKnowageVueContext() + "/dashboard/" + documentLabel);
		uriBuilder.setParameter("params", createJsonFromParemeters(biObject));
		uriBuilder.setParameter("role", getExecutionRoleForDashboard());
		addParametersToHideToolbarAndMenuInVue(uriBuilder);
	}

	private void manageParametersForEverythingElse(URIBuilder uriBuilder) {
		Map<String, String[]> parameterMap = request.getParameterMap();
		for (Entry<String, String[]> parameter : parameterMap.entrySet()) {
			String key = parameter.getKey();
			String[] value = parameter.getValue();
			if (value != null && value.length > 0) {
				uriBuilder.setParameter(key, value[0]);
			}
		}
	}

	private String getExecutionRoleForDashboard() {
		Map<String, String[]> parameterMap = request.getParameterMap();
		String role = Optional.ofNullable(parameterMap.get("SBI_EXECUTION_ROLE")).map(e -> e[0]).orElse("");
		if (StringUtils.isEmpty(role)) {
			role = Optional.ofNullable(parameterMap.get("role")).map(e -> e[0]).orElse("");
		}
		return role;
	}

	private String createJsonFromParemeters(BIObject biObject) throws JSONException {
		List<BIObjectParameter> drivers = biObject.getDrivers();
		// We wrap parameters map because it could be updated here below
		Map<String, String[]> parameterMap = new HashMap<>(request.getParameterMap());
		JSONArray parametersAsJson = new JSONArray();

		reconcileParametersWithParamsV2FromUrl(parameterMap);

		for (BIObjectParameter driver : drivers) {
			String urlName = driver.getParameterUrlName();

			boolean isMultivalue = driver.isMultivalue();

			List<String> values = Optional.ofNullable(parameterMap.get(urlName)).map(Arrays::asList)
					.orElse(Collections.emptyList());
			List<String> descriptions = Optional.ofNullable(parameterMap.get(urlName + "_description"))
					.map(Arrays::asList).orElse(Collections.emptyList());

			if (OUTPUT_TYPE.equals(urlName)) {
				LOGGER.debug("Forcing outputType to HTML");
				values = Arrays.asList("HTML");
				descriptions = Arrays.asList("HTML");
			}

			JSONObject currentDriverJson = new JSONObject();

			JSONArray valuesAsJSONArray = new JSONArray();

			for (int i = 0; i < Math.max(values.size(), descriptions.size()); i++) {
				Object value = Iterables.get(values, i, "");
				Object description = Iterables.get(descriptions, i, "");

				JSONObject currValue = new JSONObject();

				currValue.put("value", value);
				currValue.put("description", description);

				valuesAsJSONArray.put(currValue);
			}

			currentDriverJson.put("value", valuesAsJSONArray);
			currentDriverJson.put("urlName", urlName);
			currentDriverJson.put("multivalue", isMultivalue);

			parametersAsJson.put(currentDriverJson);
		}

		String parametersAsString = parametersAsJson.toString();
		return java.util.Base64.getEncoder().withoutPadding().encodeToString(parametersAsString.getBytes());
	}

	private boolean isDashboard(Engine eng) {
		return "knowagedashboardengine".equals(eng.getLabel());
	}

	private void addParametersToHideToolbarAndMenuInVue(URIBuilder uriBuilder) {
		uriBuilder.setParameter("toolbar", "false");
		uriBuilder.setParameter("menu", "false");
		uriBuilder.setParameter("finalUser", "true");
	}

	private void reconcileParametersWithParamsV2FromUrl(Map<String, String[]> parameterMap) throws JSONException {
		// Manage new parameters format in Base64
		String parametersV2FromUrl = Optional.ofNullable(request.getParameter("params"))
				.map(e -> new String(java.util.Base64.getDecoder().decode(e))).orElse("[]");
		JSONArray parametersV2FromUrlAsJSONArray = new JSONArray(parametersV2FromUrl);
		for (int i = 0; i < parametersV2FromUrlAsJSONArray.length(); i++) {
			JSONObject currParameterFromParametersV2 = (JSONObject) parametersV2FromUrlAsJSONArray.get(i);

			String urlName = currParameterFromParametersV2.getString("urlName");

			if (!parameterMap.containsKey(urlName)) {
				List<String> values = new ArrayList<>();
				List<String> descriptions = new ArrayList<>();

				JSONArray value = currParameterFromParametersV2.getJSONArray("value");

				for (int k = 0; k < value.length(); k++) {
					JSONObject currentParameterValue = (JSONObject) value.get(k);

					String cValue = currentParameterValue.getString("value");
					String cDesc = currentParameterValue.getString("description");

					values.add(cValue);
					descriptions.add(cDesc);
				}

				parameterMap.put(urlName, values.toArray(new String[0]));
				parameterMap.put(urlName + "_description", descriptions.toArray(new String[0]));
			}
		}
	}

}
