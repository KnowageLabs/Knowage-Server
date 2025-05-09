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
package it.eng.spagobi.commons.utilities;

import static it.eng.spagobi.commons.constants.ConfigurationConstants.SPAGOBI_SPAGOBI_SERVICE_JNDI;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.engines.drivers.IEngineDriver;
import it.eng.spagobi.engines.drivers.geo.GeoDriver;
import it.eng.spagobi.monitoring.dao.AuditManager;
import it.eng.spagobi.services.common.SsoServiceInterface;
import it.eng.spagobi.utilities.assertion.Assert;

public class ExecutionProxy {

	private static final Logger LOGGER = LogManager.getLogger(ExecutionProxy.class);

	private static final String BACK_END_EXTENSION = "BackEnd";

	public static final String SEND_MAIL_MODALITY = "SEND_MAIL";
	public static final String EXPORT_MODALITY = "EXPORT";
	public static final String MASSIVE_EXPORT_MODALITY = SpagoBIConstants.MASSIVE_EXPORT_MODALITY;

	private BIObject biObject = null;

	private String returnedContentType = null;
	private String outputTypeTrigger = null;
	String mimeType = null;

	/**
	 * Gets the bi object.
	 *
	 * @return the bi object
	 */
	public BIObject getBiObject() {
		return biObject;
	}

	/**
	 * Sets the bi object.
	 *
	 * @param biObject the new bi object
	 */
	public void setBiObject(BIObject biObject) {
		this.biObject = biObject;
	}

	/**
	 * Executes a document in background with the given profile.
	 *
	 * @param profile             The user profile
	 * @param modality            The execution modality (for auditing)
	 * @param defaultOutputFormat The default output format (optional) , considered if the document has no output format parameter set
	 *
	 * @return the byte[]
	 */
	public byte[] exec(IEngUserProfile profile, String modality, String defaultOutputFormat) {
		LOGGER.debug("Executing document with profile {}, modality {}, defaultOutputFormat {}", profile, modality, defaultOutputFormat);
		byte[] response = new byte[0];
		try {
			if (biObject == null) {
				return response;
			}
			// get the engine of the biobject
			Engine eng = biObject.getEngine();
			// if engine is not an external it's not possible to call it using
			// url
			if (!EngineUtilities.isExternal(eng)) {
				return response;
			}
			// get driver class
			String driverClassName = eng.getDriverName();

			// build an instance of the driver
			IEngineDriver aEngineDriver = (IEngineDriver) Class.forName(driverClassName).newInstance();

			// get the map of parameter to send to the engine
			Map mapPars = aEngineDriver.getParameterMap(biObject, profile, "");
			if (defaultOutputFormat != null && !defaultOutputFormat.trim().equals("")) {
				List<BIObjectParameter> params = biObject.getDrivers();
				Iterator<BIObjectParameter> iterParams = params.iterator();
				boolean findOutPar = false;
				while (iterParams.hasNext()) {
					BIObjectParameter par = iterParams.next();
					String parUrlName = par.getParameterUrlName();
					List values = par.getParameterValues();
					LOGGER.debug("Processing BIObjectParameter with url name {}", parUrlName);
					if (parUrlName.equalsIgnoreCase("outputType") && values != null && !values.isEmpty()) {
						findOutPar = true;
						break;
					}
				}
				if (!findOutPar) {
					mapPars.put("outputType", defaultOutputFormat);
				}
			}

			adjustParametersForExecutionProxy(aEngineDriver, mapPars, modality);

			// TODO merge with ExecutionInstance.addSystemParametersForExternalEngines for SBI_CONTEXT, locale parameters, etc...

			// set country and language (locale)
			Locale locale = GeneralUtilities.getDefaultLocale();
			if (!mapPars.containsKey(SpagoBIConstants.SBI_COUNTRY)) {
				String country = locale.getCountry();
				mapPars.put(SpagoBIConstants.SBI_COUNTRY, country);
			}
			if (!mapPars.containsKey(SpagoBIConstants.SBI_LANGUAGE)) {
				String language = locale.getLanguage();
				mapPars.put(SpagoBIConstants.SBI_LANGUAGE, language);
			}

			if (!mapPars.containsKey(SpagoBIConstants.SBI_SCRIPT)) {
				String script = locale.getScript();
				mapPars.put(SpagoBIConstants.SBI_SCRIPT, script);
			}

			// set userId in particular cases (backend operations)
			if (SEND_MAIL_MODALITY.equals(modality) || EXPORT_MODALITY.equals(modality) || SpagoBIConstants.MASSIVE_EXPORT_MODALITY.equals(modality)) {
				mapPars.put(SsoServiceInterface.USER_ID, ((UserProfile) profile).getUserUniqueIdentifier());
			}

			// adding SBI_EXECUTION_ID parameter
			if (!mapPars.containsKey("SBI_EXECUTION_ID")) {
				UUID uuidObj = UUID.randomUUID();
				String executionId = uuidObj.toString();
				executionId = executionId.replaceAll("-", "");
				mapPars.put("SBI_EXECUTION_ID", executionId);
			}

			// AUDIT
			AuditManager auditManager = AuditManager.getInstance();
			Integer auditId = auditManager.insertAudit(biObject, null, profile, "", modality != null ? modality : "");
			// adding parameters for AUDIT updating
			if (auditId != null) {
				mapPars.put(AuditManager.AUDIT_ID, auditId.toString());
			}

			// add outputType from trigger
			adjustOutputParameterForSchedulerTrigger(mapPars);

			// get the url of the engine
			String urlEngine = getExternalEngineUrl(eng);

			LOGGER.debug("The URL is {}", urlEngine);

			// built the request to sent to the engine
			HttpMethod httpMethod;
			if (!EngineUtilities.hasBackEndService(eng)) {
				GetMethod getMethod = new GetMethod(urlEngine);
				List<NameValuePair> nameValuePairs = new ArrayList<>();
				Iterator iterMapPar = mapPars.keySet().iterator();
				while (iterMapPar.hasNext()) {
					String parurlname = (String) iterMapPar.next();
					String parvalue = "";
					if (mapPars.get(parurlname) != null) {
						parvalue = mapPars.get(parurlname).toString();
					}
					nameValuePairs.add(new NameValuePair(parurlname, parvalue));
				}
				getMethod.setQueryString(nameValuePairs.toArray(new NameValuePair[0]));
				httpMethod = getMethod;
			} else {
				PostMethod postMethod = new PostMethod(urlEngine);
				Iterator iterMapPar = mapPars.keySet().iterator();
				while (iterMapPar.hasNext()) {
					String parurlname = (String) iterMapPar.next();
					String parvalue = "";
					if (mapPars.get(parurlname) != null) {
						parvalue = mapPars.get(parurlname).toString();
					}
					postMethod.addParameter(parurlname, parvalue);
				}
				httpMethod = postMethod;
			}
			// String userId = (String) UserProfile.createSchedulerUserProfileWithRole(null).getUserUniqueIdentifier();
			UserProfile userProfile = (UserProfile) profile;
			String encodedUserId = Base64.getEncoder().encodeToString(((String) userProfile.getUserId()).getBytes(UTF_8));
			httpMethod.addRequestHeader("Authorization", "Direct " + encodedUserId);

			// sent request to the engine
			LOGGER.debug("Calling {} with parameters {} and headers {}", httpMethod.getURI(), httpMethod.getParams(), httpMethod.getRequestHeaders());
			HttpClient client = new HttpClient();
			int statusCode = client.executeMethod(httpMethod);
			LOGGER.debug("Response status code {}", statusCode);
			response = httpMethod.getResponseBody();

			Header headContetType = httpMethod.getResponseHeader("Content-Type");
			if (headContetType != null) {
				returnedContentType = headContetType.getValue();
			} else {
				returnedContentType = "application/octet-stream";
			}

			auditManager.updateAudit(auditId, null, Calendar.getInstance().getTimeInMillis(), "EXECUTION_PERFORMED", null, null);
			httpMethod.releaseConnection();
		} catch (Exception e) {
			LOGGER.error("Error while executing object ", e);
		}
		LOGGER.debug("OUT");
		return response;
	}


	private String getExternalEngineUrl(Engine eng) {
		LOGGER.debug("IN");
		// in case there is a Secondary URL, use it
		String urlEngine = eng.getSecondaryUrl();
		if (urlEngine == null || urlEngine.trim().equals("")) {
			LOGGER.debug("Secondary url is not defined for engine {}; main url will be used.", eng.getLabel());
			// in case there is not a Secondary URL, use the main url
			urlEngine = eng.getUrl();
		}
		LOGGER.debug("Engine url is {}", urlEngine);
		Assert.assertTrue(urlEngine != null && !urlEngine.trim().equals(""), "External engine url is not defined!!");
		urlEngine = resolveRelativeUrls(urlEngine);

		String driverName = eng.getDriverName();
		if (EngineUtilities.hasBackEndService(eng)) {
			// ADD this extension because this is a BackEnd engine invocation
			urlEngine = urlEngine + BACK_END_EXTENSION;
		}
		LOGGER.debug("Returned URL is {}", urlEngine);
		return urlEngine;
	}

	private String resolveRelativeUrls(String url) {
		LOGGER.debug("Resolving relative ULR {}", url);
		if (url.startsWith("/")) {
			LOGGER.debug("Url is relative");
			String domain = getServiceHostUrl();
			LOGGER.debug("SpagoBI domain is {}", domain);
			url = domain + url;
			LOGGER.debug("Absolute url is {}", url);
		}
		LOGGER.debug("Returning {}", url);
		return url;
	}

	public String getServiceHostUrl() {
		String serviceURL = SpagoBIUtilities.readJndiResource(SingletonConfig.getInstance().getConfigValue(SPAGOBI_SPAGOBI_SERVICE_JNDI));
		serviceURL = serviceURL.substring(0, serviceURL.lastIndexOf('/'));
		return serviceURL;
	}

	/**
	 * Gets the returned content type.
	 *
	 * @return the returned content type
	 */
	public String getReturnedContentType() {
		return returnedContentType;
	}

	/**
	 * Sets the returned content type.
	 *
	 * @param returnedContentType the new returned content type
	 */
	public void setReturnedContentType(String returnedContentType) {
		this.returnedContentType = returnedContentType;
	}

	/**
	 * Gets the file extension from cont type.
	 *
	 * @param contentType the content type
	 *
	 * @return the file extension from cont type
	 */
	public String getFileExtensionFromContType(String contentType) {
		LOGGER.debug("Getting file extension from content type {}", contentType);
		String extension = "";
		if (contentType != null) {
			if (contentType.equalsIgnoreCase("text/html")) {
				extension = ".html";
			} else if (contentType.equalsIgnoreCase("text/xml")) {
				extension = ".xml";
			} else if (contentType.equalsIgnoreCase("text/plain")) {
				extension = ".txt";
			} else if (contentType.equalsIgnoreCase("text/csv")) {
				extension = ".csv";
			} else if (contentType.equalsIgnoreCase("application/pdf")) {
				extension = ".pdf";
			} else if (contentType.equalsIgnoreCase("application/rtf")) {
				extension = ".pdf";
			} else if (contentType.equalsIgnoreCase("application/vnd.ms-excel")) {
				extension = ".xls";
			} else if (contentType.equalsIgnoreCase("application/msword")) {
				extension = ".word";
			} else if (contentType.equalsIgnoreCase("image/jpeg")) {
				extension = ".jpg";
			} else if (contentType.equalsIgnoreCase("application/powerpoint")) {
				extension = ".ppt";
			} else if (contentType.equalsIgnoreCase("application/vnd.ms-powerpoint")) {
				extension = ".ppt";
			} else if (contentType.equalsIgnoreCase("application/x-mspowerpoint")) {
				extension = ".ppt";
			} else if (contentType.equalsIgnoreCase("image/svg+xml")) {
				extension = ".svg";
			}
		}
		LOGGER.debug("OUT");
		return extension;
	}

	/**
	 * Adjust paramters set by driver for use by Execution proxy
	 *
	 */

	public void adjustParametersForExecutionProxy(IEngineDriver driver, Map mapPars, String modality) {
		if (driver instanceof GeoDriver) {
			mapPars.remove("ACTION_NAME");
			mapPars.put("ACTION_NAME", "EXECUTION_PROXY_GEO_ACTION");
			mapPars.remove("outputType");
			mapPars.put("outputType", "JPEG");
		}
	}

	private void adjustOutputParameterForSchedulerTrigger(Map mapPars) {
		if (this.outputTypeTrigger != null) {
			mapPars.put("outputType", this.outputTypeTrigger);
		}
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String outputType) {
		this.mimeType = outputType;
	}

	public void setOutputTypeTrigger(String outputTypeTrigger) {
		this.outputTypeTrigger = outputTypeTrigger;
	}

}
