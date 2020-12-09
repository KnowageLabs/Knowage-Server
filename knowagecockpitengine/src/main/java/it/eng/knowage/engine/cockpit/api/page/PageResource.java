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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.axis.encoding.Base64;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.emory.mathcs.backport.java.util.Arrays;
import it.eng.knowage.engine.cockpit.CockpitEngine;
import it.eng.knowage.engine.cockpit.CockpitEngineInstance;
import it.eng.knowage.engine.cockpit.api.AbstractCockpitEngineResource;
import it.eng.knowage.export.wrapper.beans.RenderOptions;
import it.eng.knowage.export.wrapper.beans.ViewportDimensions;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @authors Andrea Gioia (andrea.gioia@eng.it)
 *
 */

@Path("/1.0/pages")
public class PageResource extends AbstractCockpitEngineResource {

	private static final String OUTPUT_TYPE = "outputType";
	private static final String PDF_PAGE_ORIENTATION = "pdfPageOrientation";
	private static final String PDF_ZOOM = "pdfZoom";
	private static final String PDF_WIDTH = "pdfWidth";
	private static final String PDF_HEIGHT = "pdfHeight";
	private static final String PDF_DEVICE_SCALE_FACTOR = "pdfDeviceScaleFactor";
	private static final String PDF_WAIT_TIME = "pdfWaitTime";
	static private final List<String> PDF_PARAMETERS = Arrays
			.asList(new String[] { OUTPUT_TYPE, PDF_WIDTH, PDF_HEIGHT, PDF_WAIT_TIME, PDF_ZOOM, PDF_PAGE_ORIENTATION });
	static private final List<String> JPG_PARAMETERS = Arrays.asList(new String[] { OUTPUT_TYPE });

	static private Map<String, JSONObject> pages;
	static private Map<String, String> urls;

	static private Logger logger = Logger.getLogger(PageResource.class);

	static {
		pages = new HashMap<String, JSONObject>();
		urls = new HashMap<String, String>();

		try {
			pages.put("edit", new JSONObject("{name: 'execute', description: 'the cockpit edit page', parameters: []}"));
			urls.put("edit", "/WEB-INF/jsp/ngCockpit.jsp");
			pages.put("execute", new JSONObject("{name: 'execute', description: 'the cockpit execution page', parameters: ['template']}"));
			urls.put("execute", "/WEB-INF/jsp/ngCockpit.jsp");
			pages.put("test", new JSONObject("{name: 'test', description: 'the cockpit test page', parameters: ['template']}"));
			urls.put("execute", "/WEB-INF/jsp/test4.jsp");
		} catch (JSONException t) {
			logger.error(t);
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
			logger.debug("OUT");
		}
	}

	@GET
	@Path("/{pagename}")
	public void openPageGet(@PathParam("pagename") String pageName) {
		openPageInternal(pageName);
	}

	@POST
	@Path("/{pagename}")
	public void openPagePost(@PathParam("pagename") String pageName) {
		openPageInternal(pageName);
	}

	private void openPageInternal(String pageName) {
		CockpitEngineInstance engineInstance;
		String dispatchUrl = null;

		if (logger.isDebugEnabled()) {
			Enumeration<String> parameterNames = request.getParameterNames();
			while (parameterNames.hasMoreElements()) {
				String name = parameterNames.nextElement();
				String[] parameterValues = request.getParameterValues(name);
				logger.debug(name + " = " + Arrays.asList(parameterValues));
			}
		}

		try {
			// To deploy into JBOSSEAP64 is needed a StandardWrapper, instead of RestEasy Wrapper
			// HttpServletRequest request = ResteasyProviderFactory.getContextData(HttpServletRequest.class);
			// HttpServletResponse response = ResteasyProviderFactory.getContextData(HttpServletResponse.class);

			/**
			 * Setting the encoding type to the response object, so the Cockpit engine when calling the rendering of the chart (chart.jsp) can display the real
			 * content of the chart template. If this is not set, specific Italian letters, such as ù and à are going to be displayed as black squared question
			 * marks - they will not be displayed as they are specified by the user.
			 *
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			response.setContentType(MediaType.TEXT_HTML);
			response.setCharacterEncoding("UTF-8");

			if ("execute".equals(pageName)) {
				String outputType = request.getParameter(OUTPUT_TYPE);
				if ("xls".equalsIgnoreCase(outputType) || "xlsx".equalsIgnoreCase(outputType)) {
					request.setAttribute("template", getIOManager().getTemplateAsString());
					String requestURL = getRequestUrlForExcelExport(request);
					request.setAttribute("requestURL", requestURL);
					dispatchUrl = "/WEB-INF/jsp/ngCockpitExportExcel.jsp";
					response.setContentType(MediaType.APPLICATION_OCTET_STREAM);
				} else if ("pdf".equalsIgnoreCase(outputType)) {
					String requestURL = getRequestUrlForPdfExport(request);
					request.setAttribute("requestURL", requestURL);

					RenderOptions renderOptions = getRenderOptionsForPdfExporter(request);
					request.setAttribute("renderOptions", renderOptions);

					dispatchUrl = "/WEB-INF/jsp/ngCockpitExportPdf.jsp";
					response.setContentType(MediaType.APPLICATION_OCTET_STREAM);
				} else if ("JPG".equalsIgnoreCase(outputType)) {
					throw new UnsupportedOperationException("This method is not implemented anymore");
				} else if ("PNG".equalsIgnoreCase(outputType)) {
					// TO CHANGE
					String requestURL = getRequestUrlForPdfExport(request);
					request.setAttribute("requestURL", requestURL);

					RenderOptions renderOptions = getRenderOptionsForPdfExporter(request);
					request.setAttribute("renderOptions", renderOptions);

					dispatchUrl = "/WEB-INF/jsp/ngCockpitExportPng.jsp";
					response.setContentType(MediaType.APPLICATION_OCTET_STREAM);
				} else {
					engineInstance = CockpitEngine.createInstance(getIOManager().getTemplateAsString(), getIOManager().getEnv());
					getIOManager().getHttpSession().setAttribute(EngineConstants.ENGINE_INSTANCE, engineInstance);

					String editMode = request.getParameter("documentMode");
					if (editMode != null) {
						editMode = editMode.equals("null") ? null : request.getParameter("documentMode");
					}

					if (editMode != null && editMode.equals("EDIT")) {
						String documentLabel = request.getParameter("DOCUMENT_LABEL");
						BIObject obj = DAOFactory.getBIObjectDAO().loadBIObjectByLabel(documentLabel);
						if (!ObjectsAccessVerifier.canEdit(obj, getUserProfile())) {
							String message = String.format("User [%s] cannot edit this document", (String) getUserProfile().getUserId());
							logger.error(message);
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
						getIOManager().getEnvForWidget());
				getIOManager().getHttpSession().setAttribute(EngineConstants.ENGINE_INSTANCE, engineInstance);
				// getExecutionSession().setAttributeInSession(EngineConstants.ENGINE_INSTANCE, engineInstance);
				dispatchUrl = "/WEB-INF/jsp/ngCockpit.jsp";
			} else if ("test".equals(pageName)) {
				dispatchUrl = "/WEB-INF/jsp/test4.jsp";
			} else {
				// error
				dispatchUrl = "/WEB-INF/jsp/error.jsp";
			}

			request.getRequestDispatcher(dispatchUrl).forward(request, response);
		} catch (Exception e) {
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException("", getEngineInstance(), e);
		} finally {
			logger.debug("OUT");
		}
	}

	private RenderOptions getRenderOptionsForPdfExporter(HttpServletRequest request) throws UnsupportedEncodingException {
		String userId = (String) getUserProfile().getUserUniqueIdentifier();
		String encodedUserId = Base64.encode(userId.getBytes("UTF-8"));
		Map<String, String> headers = new HashMap<String, String>(1);
		headers.put("Authorization", "Direct " + encodedUserId);

		RenderOptions defaultRenderOptions = RenderOptions.defaultOptions();
		ViewportDimensions defaultDimensions = defaultRenderOptions.getDimensions();
		long defaultJsRenderingWait = defaultRenderOptions.getJsRenderingWait();
		int pdfWidth = Integer.valueOf(defaultDimensions.getWidth());
		int pdfHeight = Integer.valueOf(defaultDimensions.getHeight());
		double pdfDeviceScaleFactor = Double.valueOf(defaultDimensions.getDeviceScaleFactor());
		long pdfRenderingWaitTime = defaultJsRenderingWait;

		String widthParameterVal = request.getParameter(PDF_WIDTH);
		String heightParameterVal = request.getParameter(PDF_HEIGHT);
		String deviceScaleFactorVal = request.getParameter(PDF_DEVICE_SCALE_FACTOR);
		String jsRenderingWaitParameterVal = request.getParameter(PDF_WAIT_TIME);

		if (widthParameterVal != null) {
			pdfWidth = Integer.valueOf(widthParameterVal);
		}
		if (heightParameterVal != null) {
			pdfHeight = Integer.valueOf(heightParameterVal);
		}
		if (jsRenderingWaitParameterVal != null) {
			pdfRenderingWaitTime = 1000 * Long.valueOf(jsRenderingWaitParameterVal);
		}

		if (deviceScaleFactorVal != null) {
			pdfDeviceScaleFactor = Double.valueOf(deviceScaleFactorVal);
		}

		ViewportDimensions dimensions = ViewportDimensions.builder().withWidth(pdfWidth).withHeight(pdfHeight).withDeviceScaleFactor(pdfDeviceScaleFactor)
				.build();
		RenderOptions renderOptions = defaultRenderOptions.withDimensions(dimensions).withJavaScriptExecutionDetails(pdfRenderingWaitTime, 5000L);
		return renderOptions;
	}

	private String getRequestUrlForPdfExport(HttpServletRequest request) throws UnsupportedEncodingException {

		String documentLabel = request.getParameter("DOCUMENT_LABEL");
		BIObject biObject = null;
		try {
			biObject = DAOFactory.getBIObjectDAO().loadBIObjectByLabel(documentLabel);
		} catch (EMFUserError e) {
			throw new SpagoBIRuntimeException("Error retrieving document with label " + documentLabel, e);
		}
		Engine eng = biObject.getEngine();
		String externalUrl = GeneralUtilities.getExternalEngineUrl(eng);

		StringBuilder sb = new StringBuilder(externalUrl);
		String sep = "?";
		Map<String, String[]> parameterMap = request.getParameterMap();
		for (String parameter : parameterMap.keySet()) {
			if (!PDF_PARAMETERS.contains(parameter)) {
				String[] values = parameterMap.get(parameter);
				if (values != null && values.length > 0) {
					sb.append(sep);
					sb.append(URLEncoder.encode(parameter, "UTF-8"));
					sb.append("=");
					sb.append(URLEncoder.encode(values[0], "UTF-8"));
					sep = "&";
				}
			}
		}
		sb.append("&export=true");
		return sb.toString();
	}

	private String getRequestUrlForExcelExport(HttpServletRequest request) throws UnsupportedEncodingException {

		String documentLabel = request.getParameter("DOCUMENT_LABEL");
		BIObject biObject = null;
		try {
			biObject = DAOFactory.getBIObjectDAO().loadBIObjectByLabel(documentLabel);
		} catch (EMFUserError e) {
			throw new SpagoBIRuntimeException("Error retrieving document with label " + documentLabel, e);
		}
		Engine eng = biObject.getEngine();
		String externalUrl = GeneralUtilities.getExternalEngineUrl(eng);

		StringBuilder sb = new StringBuilder(externalUrl);
		String sep = "?";
		Map<String, String[]> parameterMap = request.getParameterMap();
		for (String parameter : parameterMap.keySet()) {
			String[] values = parameterMap.get(parameter);
			if (values != null && values.length > 0) {
				sb.append(sep);
				sb.append(URLEncoder.encode(parameter, "UTF-8"));
				sb.append("=");
				sb.append(URLEncoder.encode(values[0], "UTF-8"));
				sep = "&";
			}
		}
		sb.append("&scheduledexport=true");
		return sb.toString();
	}

	public String getServiceHostUrl() {
		String serviceURL = SpagoBIUtilities.readJndiResource(SingletonConfig.getInstance().getConfigValue("SPAGOBI.SPAGOBI_SERVICE_JNDI"));
		serviceURL = serviceURL.substring(0, serviceURL.lastIndexOf('/'));
		return serviceURL;
	}

	@GET
	@Path("/executeTest")
	@Produces(MediaType.APPLICATION_JSON)
	public String testAction(@Context HttpServletResponse response) {

		logger.debug("IN");

		try {
			JSONObject obj = new JSONObject();
			try {
				obj.put("result", "ok");
			} catch (JSONException e) {
				logger.error("Error building the success string");
				throw new SpagoBIRuntimeException("Error building the success string");
			}
			String successString = obj.toString();
			return successString;
		} finally {
			logger.debug("OUT");
		}
	}

	private JSONObject buildBaseTemplate() {
		JSONObject template;

		logger.debug("IN");
		template = new JSONObject();
		logger.debug("OUT");

		return template;
	}
}
