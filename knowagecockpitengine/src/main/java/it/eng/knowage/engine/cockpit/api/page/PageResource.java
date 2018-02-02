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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
import it.eng.knowage.slimerjs.wrapper.beans.CustomHeaders;
import it.eng.knowage.slimerjs.wrapper.beans.RenderOptions;
import it.eng.knowage.slimerjs.wrapper.beans.ViewportDimensions;
import it.eng.knowage.slimerjs.wrapper.enums.RenderFormat;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
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
	public void openPage(@PathParam("pagename") String pageName, @QueryParam("extjs") @DefaultValue("4") String extjs) {
		CockpitEngineInstance engineInstance;
		String dispatchUrl = null;

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
				if ("xls".equals(outputType) || "xlsx".equals(outputType)) {
					request.setAttribute("template", getIOManager().getTemplateAsString());
					dispatchUrl = "/WEB-INF/jsp/ngCockpitExportExcel.jsp";
					response.setContentType(MediaType.APPLICATION_OCTET_STREAM);
				} else if ("pdf".equals(outputType)) {
					String requestURL = getRequestUrlForPdfExport(request);
					request.setAttribute("requestURL", requestURL);

					RenderOptions renderOptions = getRenderOptionsForPdfExporter(request);
					request.setAttribute("renderOptions", renderOptions);

					dispatchUrl = "/WEB-INF/jsp/ngCockpitExportPdf.jsp";
					response.setContentType(MediaType.APPLICATION_OCTET_STREAM);
				} else if ("JPG".equals(outputType)) {
					String requestURL = getRequestUrlForJpgExport(request);
					request.setAttribute("requestURL", requestURL);

					RenderOptions renderOptions = getRenderOptionsForJpgExporter(request);
					request.setAttribute("renderOptions", renderOptions);

					dispatchUrl = "/WEB-INF/jsp/ngCockpitExportJpg.jsp";
					response.setContentType(MediaType.APPLICATION_OCTET_STREAM);
				} else {
					engineInstance = CockpitEngine.createInstance(getIOManager().getTemplateAsString(), getIOManager().getEnv());
					getIOManager().getHttpSession().setAttribute(EngineConstants.ENGINE_INSTANCE, engineInstance);
					// getExecutionSession().setAttributeInSession(EngineConstants.ENGINE_INSTANCE, engineInstance);
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

			/**
			 * Setting the encoding type to the response object, so the Cockpit engine when calling the rendering of the chart (chart.jsp) can display the real
			 * content of the chart template. If this is not set, specific Italian letters, such as ù and à are going to be displayed as black squared question
			 * marks - they will not be displayed as they are specified by the user.
			 *
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			response.setContentType("text/html");
			response.setCharacterEncoding("UTF-8");

			request.getRequestDispatcher(dispatchUrl).forward(request, response);
		} catch (Exception e) {
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException("", getEngineInstance(), e);
		} finally {
			logger.debug("OUT");
		}
	}

	private String getRequestUrlForJpgExport(HttpServletRequest request) {
		StringBuilder sb = new StringBuilder(request.getRequestURL().toString());
		String sep = "?";
		Map<String, String[]> parameterMap = request.getParameterMap();
		for (String parameter : parameterMap.keySet()) {
			if (!JPG_PARAMETERS.contains(parameter)) {
				String[] values = parameterMap.get(parameter);
				if (values != null && values.length > 0) {
					sb.append(sep);
					sb.append(parameter);
					sb.append("=");
					sb.append(values[0]);
					sep = "&";
				}
			}
		}
		sb.append("&export=true");
		return sb.toString();
	}

	private RenderOptions getRenderOptionsForJpgExporter(HttpServletRequest request) throws UnsupportedEncodingException {
		String userId = (String) getUserProfile().getUserUniqueIdentifier();
		String encodedUserId = Base64.encode(userId.getBytes("UTF-8"));
		Map<String, String> headers = new HashMap<String, String>(1);
		headers.put("Authorization", "Direct " + encodedUserId);
		CustomHeaders customHeaders = new CustomHeaders(headers);
		RenderOptions renderOptions = RenderOptions.DEFAULT.withCustomHeaders(customHeaders).withRenderFormat(RenderFormat.PNG);
		return renderOptions;
	}

	private RenderOptions getRenderOptionsForPdfExporter(HttpServletRequest request) throws UnsupportedEncodingException {
		String userId = (String) getUserProfile().getUserUniqueIdentifier();
		String encodedUserId = Base64.encode(userId.getBytes("UTF-8"));
		Map<String, String> headers = new HashMap<String, String>(1);
		headers.put("Authorization", "Direct " + encodedUserId);
		CustomHeaders customHeaders = new CustomHeaders(headers);

		int pdfWidth = Integer.valueOf(request.getParameter(PDF_WIDTH));
		int pdfHeight = Integer.valueOf(request.getParameter(PDF_HEIGHT));
		ViewportDimensions dimensions = new ViewportDimensions(pdfWidth, pdfHeight);

		long pdfRenderingWaitTime = 1000 * Long.valueOf(request.getParameter(PDF_WAIT_TIME));

		RenderOptions renderOptions = RenderOptions.DEFAULT.withCustomHeaders(customHeaders).withDimensions(dimensions)
				.withJavaScriptExecutionDetails(pdfRenderingWaitTime, 5000L);
		return renderOptions;
	}

	private String getRequestUrlForPdfExport(HttpServletRequest request) {
		String requestURL = request.getRequestURL().toString();
		String hostURL = GeneralUtilities.getSpagoBiHost();
		String serviceURL = getServiceHostUrl();

		StringBuilder sb = new StringBuilder(requestURL.replace(hostURL, serviceURL));
		String sep = "?";
		Map<String, String[]> parameterMap = request.getParameterMap();
		for (String parameter : parameterMap.keySet()) {
			if (!PDF_PARAMETERS.contains(parameter)) {
				String[] values = parameterMap.get(parameter);
				if (values != null && values.length > 0) {
					sb.append(sep);
					sb.append(parameter);
					sb.append("=");
					if (parameter.equals(SpagoBIConstants.SBI_HOST)) {
						sb.append(getServiceHostUrl());
					} else {
						sb.append(values[0]);
					}
					sep = "&";
				}
			}
		}
		sb.append("&export=true");
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
