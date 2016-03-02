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

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.dispatching.service.DefaultRequestContext;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.engines.InternalEngineIFace;
import it.eng.spagobi.engines.chart.SpagoBIChartInternalEngine;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.engines.drivers.IEngineDriver;
import it.eng.spagobi.engines.drivers.geo.GeoDriver;
import it.eng.spagobi.engines.drivers.worksheet.WorksheetDriver;
import it.eng.spagobi.monitoring.dao.AuditManager;
import it.eng.spagobi.services.common.SsoServiceInterface;
import it.eng.spagobi.utilities.assertion.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

public class ExecutionProxy {

	static private Logger logger = Logger.getLogger(ExecutionProxy.class);
	static private String backEndExtension = "BackEnd";
	static public String SEND_MAIL_MODALITY = "SEND_MAIL";
	static public String EXPORT_MODALITY = "EXPORT";
	static public String MASSIVE_EXPORT_MODALITY = SpagoBIConstants.MASSIVE_EXPORT_MODALITY;

	private BIObject biObject = null;

	private String returnedContentType = null;
	String mimeType = null;

	// used for worksheetExecution
	private boolean splittingFilter = false;

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
	 * @param biObject
	 *            the new bi object
	 */
	public void setBiObject(BIObject biObject) {
		this.biObject = biObject;
	}

	/**
	 * Executes a document in background with the given profile.
	 *
	 * @param profile
	 *            The user profile
	 * @param modality
	 *            The execution modality (for auditing)
	 * @param defaultOutputFormat
	 *            The default output format (optional) , considered if the document has no output format parameter set
	 *
	 * @return the byte[]
	 */
	public byte[] exec(IEngUserProfile profile, String modality, String defaultOutputFormat) {
		logger.debug("IN");
		byte[] response = new byte[0];
		try {
			if (biObject == null)
				return response;
			// get the engine of the biobject
			Engine eng = biObject.getEngine();
			// if engine is not an external it's not possible to call it using
			// url
			if (!EngineUtilities.isExternal(eng))
				if (eng.getClassName().equals("it.eng.spagobi.engines.kpi.SpagoBIKpiInternalEngine")) {
					SourceBean request = null;
					SourceBean resp = null;
					EMFErrorHandler errorHandler = null;

					try {
						request = new SourceBean("");
						resp = new SourceBean("");
					} catch (SourceBeanException e1) {
						e1.printStackTrace();
					}
					RequestContainer reqContainer = new RequestContainer();
					ResponseContainer resContainer = new ResponseContainer();
					reqContainer.setServiceRequest(request);
					resContainer.setServiceResponse(resp);
					DefaultRequestContext defaultRequestContext = new DefaultRequestContext(reqContainer, resContainer);
					resContainer.setErrorHandler(new EMFErrorHandler());
					RequestContainer.setRequestContainer(reqContainer);
					ResponseContainer.setResponseContainer(resContainer);
					Locale locale = new Locale("it", "IT", "");
					SessionContainer session = new SessionContainer(true);
					reqContainer.setSessionContainer(session);
					SessionContainer permSession = session.getPermanentContainer();
					// IEngUserProfile profile = new AnonymousCMSUserProfile();
					permSession.setAttribute(IEngUserProfile.ENG_USER_PROFILE, profile);
					errorHandler = defaultRequestContext.getErrorHandler();

					String className = eng.getClassName();
					logger.debug("Try instantiating class " + className + " for internal engine " + eng.getName() + "...");
					InternalEngineIFace internalEngine = null;
					// tries to instantiate the class for the internal engine
					try {
						if (className == null && className.trim().equals(""))
							throw new ClassNotFoundException();
						internalEngine = (InternalEngineIFace) Class.forName(className).newInstance();
					} catch (ClassNotFoundException cnfe) {
						logger.error("The class ['" + className + "'] for internal engine " + eng.getName() + " was not found.", cnfe);
						Vector params = new Vector();
						params.add(className);
						params.add(eng.getName());
						errorHandler.addError(new EMFUserError(EMFErrorSeverity.ERROR, 2001, params));
						return response;
					} catch (Exception e) {
						logger.error("Error while instantiating class " + className, e);
						errorHandler.addError(new EMFUserError(EMFErrorSeverity.ERROR, 100));
						return response;
					}
					try {
						reqContainer.setAttribute("scheduledExecution", "true");
						internalEngine.execute(reqContainer, biObject, resp);
					} catch (EMFUserError e) {
						logger.error("Error during engine execution", e);
						errorHandler.addError(e);
					} catch (Exception e) {
						logger.error("Error while engine execution", e);
						errorHandler.addError(new EMFUserError(EMFErrorSeverity.ERROR, 100));
					}
					return response;
				} else if (eng.getClassName().equals("it.eng.spagobi.engines.chart.SpagoBIChartInternalEngine")) {
					SourceBean request = null;
					EMFErrorHandler errorHandler = null;
					try {
						request = new SourceBean("");
					} catch (SourceBeanException e1) {
						e1.printStackTrace();
					}
					RequestContainer reqContainer = new RequestContainer();
					SpagoBIChartInternalEngine sbcie = new SpagoBIChartInternalEngine();

					// Call chart engine
					File file = sbcie.executeChartCode(reqContainer, biObject, null, profile);

					// read input from file
					InputStream is = new FileInputStream(file);

					// Get the size of the file
					long length = file.length();

					if (length > Integer.MAX_VALUE) {
						logger.error("file too large");
						return null;
					}

					// Create the byte array to hold the data
					byte[] bytes = new byte[(int) length];

					// Read in the bytes
					int offset = 0;
					int numRead = 0;
					while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
						offset += numRead;
					}

					// Ensure all the bytes have been read in
					if (offset < bytes.length) {
						logger.warn("Could not read all the file");
					}
					// Close the input stream and return bytes
					is.close();
					return bytes;
				} // end chart case
				else {
					return response;
				}
			// get driver class
			String driverClassName = eng.getDriverName();

			// build an instance of the driver
			IEngineDriver aEngineDriver = (IEngineDriver) Class.forName(driverClassName).newInstance();

			// get the map of parameter to send to the engine
			Map mapPars = aEngineDriver.getParameterMap(biObject, profile, "");
			if (defaultOutputFormat != null && !defaultOutputFormat.trim().equals("")) {
				List params = biObject.getBiObjectParameters();
				Iterator iterParams = params.iterator();
				boolean findOutPar = false;
				while (iterParams.hasNext()) {
					BIObjectParameter par = (BIObjectParameter) iterParams.next();
					String parUrlName = par.getParameterUrlName();
					List values = par.getParameterValues();
					logger.debug("processing biparameter with url name " + parUrlName);
					if (parUrlName.equalsIgnoreCase("outputType") && values != null && values.size() > 0) {
						findOutPar = true;
						break;
					}
				}
				if (!findOutPar) {
					mapPars.put("outputType", defaultOutputFormat);
				}
			}

			adjustParametersForExecutionProxy(aEngineDriver, mapPars, modality);

			// pass ticket ...
			String pass = SingletonConfig.getInstance().getConfigValue("SPAGOBI_SSO.PASS");
			if (pass == null)
				logger.warn("Pass Ticket is null");
			mapPars.put(SpagoBIConstants.PASS_TICKET, pass);

			// TODO merge with ExecutionInstance.addSystemParametersForExternalEngines for SBI_CONTEXT, locale parameters, etc...

			// set spagobi context
			if (!mapPars.containsKey(SpagoBIConstants.SBI_CONTEXT)) {
				String sbicontext = GeneralUtilities.getSpagoBiContext();
				if (sbicontext != null) {
					mapPars.put(SpagoBIConstants.SBI_CONTEXT, sbicontext);
				}
			}

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

			// set userId in particular cases (backend operations)
			if (SEND_MAIL_MODALITY.equals(modality) || EXPORT_MODALITY.equals(modality) || SpagoBIConstants.MASSIVE_EXPORT_MODALITY.equals(modality)) {
				mapPars.put(SsoServiceInterface.USER_ID, ((UserProfile) profile).getUserUniqueIdentifier());
			}

			// adding SBI_EXECUTION_ID parameter
			if (!mapPars.containsKey("SBI_EXECUTION_ID")) {
				UUIDGenerator uuidGen = UUIDGenerator.getInstance();
				UUID uuidObj = uuidGen.generateTimeBasedUUID();
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

			// built the request to sent to the engine
			Iterator iterMapPar = mapPars.keySet().iterator();
			HttpClient client = new HttpClient();
			// get the url of the engine
			String urlEngine = getExternalEngineUrl(eng);
			PostMethod httppost = new PostMethod(urlEngine);
			while (iterMapPar.hasNext()) {
				String parurlname = (String) iterMapPar.next();
				String parvalue = "";
				if (mapPars.get(parurlname) != null)
					parvalue = mapPars.get(parurlname).toString();
				httppost.addParameter(parurlname, parvalue);
			}
			// sent request to the engine
			int statusCode = client.executeMethod(httppost);
			logger.debug("statusCode=" + statusCode);
			response = httppost.getResponseBody();
			logger.debug("response=" + response.toString());
			Header headContetType = httppost.getResponseHeader("Content-Type");
			if (headContetType != null) {
				returnedContentType = headContetType.getValue();
			} else {
				returnedContentType = "application/octet-stream";
			}

			auditManager.updateAudit(auditId, null, new Long(GregorianCalendar.getInstance().getTimeInMillis()), "EXECUTION_PERFORMED", null, null);
			httppost.releaseConnection();
		} catch (Exception e) {
			logger.error("Error while executing object ", e);
		}
		logger.debug("OUT");
		return response;
	}

	private String getExternalEngineUrl(Engine eng) {
		logger.debug("IN");
		// in case there is a Secondary URL, use it
		String urlEngine = eng.getSecondaryUrl();
		if (urlEngine == null || urlEngine.trim().equals("")) {
			logger.debug("Secondary url is not defined for engine " + eng.getLabel() + "; main url will be used.");
			// in case there is not a Secondary URL, use the main url
			urlEngine = eng.getUrl();
		}
		logger.debug("Engine url is " + urlEngine);
		Assert.assertTrue(urlEngine != null && !urlEngine.trim().equals(""), "External engine url is not defined!!");
		urlEngine = resolveRelativeUrls(urlEngine);
		// ADD this extension because this is a BackEnd engine invocation
		urlEngine = urlEngine + backEndExtension;
		logger.debug("OUT: returning " + urlEngine);
		return urlEngine;
	}

	private String resolveRelativeUrls(String url) {
		logger.debug("IN: url = " + url);
		if (url.startsWith("/")) {
			logger.debug("Url is relative");
			String domain = GeneralUtilities.getSpagoBiHost();
			logger.debug("SpagoBI domain is " + domain);
			url = domain + url;
			logger.debug("Absolute url is " + url);
		}
		logger.debug("OUT: returning " + url);
		return url;
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
	 * @param returnedContentType
	 *            the new returned content type
	 */
	public void setReturnedContentType(String returnedContentType) {
		this.returnedContentType = returnedContentType;
	}

	/**
	 * Gets the file extension from cont type.
	 *
	 * @param contentType
	 *            the content type
	 *
	 * @return the file extension from cont type
	 */
	public String getFileExtensionFromContType(String contentType) {
		logger.debug("IN: contentType = " + contentType);
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
		logger.debug("OUT");
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
		} else if (driver instanceof WorksheetDriver && modality.equals(SpagoBIConstants.MASSIVE_EXPORT_MODALITY)) {
			mapPars.remove("ACTION_NAME");
			mapPars.put("ACTION_NAME", WorksheetDriver.MASSIVE_EXPORT_PARAM_ACTION_NAME);
			mapPars.remove("MIME_TYPE");
			mapPars.put("MIME_TYPE", mimeType);
			mapPars.remove("SPLITTING_FILTER");
			mapPars.put("SPLITTING_FILTER", splittingFilter);
			mapPars.put(SpagoBIConstants.EXECUTION_MODALITY, SpagoBIConstants.MASSIVE_EXPORT_MODALITY);
			mapPars.put(SpagoBIConstants.TEMPORARY_TABLE_ROOT_NAME, this.getTemporaryTableName());
			mapPars.put(SpagoBIConstants.DROP_TEMPORARY_TABLE_ON_EXIT, "TRUE");
		}
	}

	private String getTemporaryTableName() {
		UUIDGenerator uuidGen = UUIDGenerator.getInstance();
		UUID uuidObj = uuidGen.generateTimeBasedUUID();
		String executionId = uuidObj.toString();
		return executionId.replaceAll("-", "");
	}

	public boolean isSplittingFilter() {
		return splittingFilter;
	}

	public void setSplittingFilter(boolean splittingFilter) {
		this.splittingFilter = splittingFilter;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String outputType) {
		this.mimeType = outputType;
	}
}
