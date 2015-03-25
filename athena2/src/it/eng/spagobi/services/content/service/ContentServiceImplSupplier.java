/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.services.content.service;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.analiticalmodel.document.dao.IObjTemplateDAO;
import it.eng.spagobi.analiticalmodel.document.dao.ISubreportDAO;
import it.eng.spagobi.analiticalmodel.document.handlers.ExecutionInstance;
import it.eng.spagobi.commons.bo.Subreport;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.engines.drivers.IEngineDriver;
import it.eng.spagobi.services.content.bo.Content;
import it.eng.spagobi.services.security.exceptions.SecurityException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;

import sun.misc.BASE64Encoder;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class ContentServiceImplSupplier {
	static private Logger logger = Logger
			.getLogger(ContentServiceImplSupplier.class);

	/**
	 * Read template.
	 * 
	 * @param user
	 *            the user
	 * @param document
	 *            the document
	 * 
	 * @return the content
	 * 
	 * @throws SecurityException
	 *             the security exception
	 * @throws EMFUserError
	 *             the EMF user error
	 * @throws EMFInternalError
	 *             the EMF internal error
	 */
	public Content readTemplate(String user, String document, HashMap parameters)
			throws SecurityException, EMFUserError, EMFInternalError {
		Content content;
		BIObject biobj;

		logger.debug("IN");

		logger.debug("user: [" + user + "]");
		logger.debug("document: [" + document + "]");

		if (parameters == null) {
			logger.debug("Input parameters map is null. It will be considered as an empty map");
			parameters = new HashMap();
		}

		content = new Content();
		try {
			Integer id = new Integer(document);
			biobj = DAOFactory.getBIObjectDAO().loadBIObjectById(id);
			// only if the user is not Scheduler or Workflow system user or it
			// is a call to retrieve a subreport,
			// check visibility on document and parameter values
			boolean checkNeeded = true;
			boolean modContained = parameters
					.containsKey("SBI_READ_ONLY_TEMPLATE");
			if (modContained) {
				boolean onlytemplate = parameters.containsValue("true");
				if (onlytemplate) {
					checkNeeded = false;
				}
			}

			if (checkNeeded && !UserProfile.isSchedulerUser(user)
					&& !UserProfile.isWorkflowUser(user)
					&& !isSubReportCall(biobj, parameters)) {
				checkRequestCorrectness(user, biobj, parameters);
			}

			IObjTemplateDAO tempdao = DAOFactory.getObjTemplateDAO();
			ObjTemplate temp = tempdao.getBIObjectActiveTemplate(biobj.getId());
			if (temp == null) {
				logger.warn("The template dor document [" + id + "] is NULL");
				throw new SecurityException("The template dor document [" + id
						+ "] is NULL");
			}
			byte[] template = temp.getContent();
			if (biobj.getEngine().getUrl() != null && !"".equals(biobj.getEngine().getUrl())){
				//only for external engine calls the elaborateTemplate method (ie. to internationalize template)
				try{
					String driverClassName = biobj.getEngine().getDriverName();
					logger.warn("The driver used is [" + driverClassName + "]");
					IEngineDriver aEngineDriver = (IEngineDriver)Class.forName(driverClassName).newInstance();
					String language = (String) parameters.get(SpagoBIConstants.SBI_LANGUAGE);
					String country = (String) parameters.get(SpagoBIConstants.SBI_COUNTRY);
					if (language == null || country == null){
						logger.debug("Not locale informations found in parameters... Not setted it at this time.");
					}else{
						logger.debug("Language retrieved: [" + language + "]; country retrieved: [" + country + "]");
						Locale locale =  new Locale(language, country);
						aEngineDriver.applyLocale(locale);
					}
					logger.warn("Calling elaborateTemplate method defined into the driver ... ");
					byte[] elabTemplate = aEngineDriver.ElaborateTemplate(template);
					logger.warn("Finished elaborateTemplate method defined into the driver. ");
					template = elabTemplate;
				}catch(Exception ex){
					logger.error("Error while getting template: " + ex);
					return null;
				}
			}

			BASE64Encoder bASE64Encoder = new BASE64Encoder();
			content.setContent(bASE64Encoder.encode(template));
			logger.debug("template read");
			content.setFileName(temp.getName());
		} catch (NumberFormatException e) {
			logger.error("NumberFormatException", e);
			throw e;
		} catch (EMFUserError e) {
			logger.error("EMFUserError", e);
			throw e;
		} catch (EMFInternalError e) {
			logger.error("EMFUserError", e);
			throw e;
		} finally {
			logger.debug("OUT");
		}

		return content;
	}

	/**
	 * Read template by label.
	 * 
	 * @param user
	 *            the user
	 * @param document
	 *            the document
	 * 
	 * @return the content
	 * 
	 * @throws SecurityException
	 *             the security exception
	 * @throws EMFUserError
	 *             the EMF user error
	 * @throws EMFInternalError
	 *             the EMF internal error
	 */
	public Content readTemplateByLabel(String user, String label,
			HashMap parameters) throws SecurityException, EMFUserError,
			EMFInternalError {
		Content content;
		BIObject biobj;

		logger.debug("IN");

		logger.debug("user: [" + user + "]");
		logger.debug("document: [" + label + "]");

		content = new Content();
		try {
			biobj = DAOFactory.getBIObjectDAO().loadBIObjectByLabel(label);
			// only if the user is not Scheduler or Workflow system user or it
			// is a call to retrieve a subreport,
			// check visibility on document and parameter values
			if (!UserProfile.isSchedulerUser(user)
					&& !UserProfile.isWorkflowUser(user)
					&& !isSubReportCall(biobj, parameters)) {
				checkRequestCorrectness(user, biobj, parameters);
			}

			IObjTemplateDAO tempdao = DAOFactory.getObjTemplateDAO();
			ObjTemplate temp = tempdao.getBIObjectActiveTemplate(biobj.getId());
			if (temp == null) {
				logger.warn("The template dor document [" + label + "] is NULL");
				throw new SecurityException("The template dor document ["
						+ label + "] is NULL");
			}
			byte[] template = temp.getContent();

			if (biobj.getEngine().getUrl() != null && !"".equals(biobj.getEngine().getUrl())){
				//only for external engine calls the elaborateTemplate method (ie. to internationalize template)
				try{
					String driverClassName = biobj.getEngine().getDriverName();
					logger.warn("The driver used is [" + driverClassName + "]");
					IEngineDriver aEngineDriver = (IEngineDriver)Class.forName(driverClassName).newInstance();
					String language = (String) parameters.get(SpagoBIConstants.SBI_LANGUAGE);
					String country = (String) parameters.get(SpagoBIConstants.SBI_COUNTRY);
					if (language == null || country == null){
						logger.debug("Not locale informations found in parameters... Not setted it at this time.");
					}else{
						logger.debug("Language retrieved: [" + language + "]; country retrieved: [" + country + "]");
						Locale locale =  new Locale(language, country);
						aEngineDriver.applyLocale(locale);
					}
					logger.warn("Calling elaborateTemplate method defined into the driver ... ");
					byte[] elabTemplate = aEngineDriver.ElaborateTemplate(template);
					logger.warn("Finished elaborateTemplate method defined into the driver. ");
					template = elabTemplate;
				}catch(Exception ex){
					logger.error("Error while getting template: " + ex);
					return null;
				}
			}
			
			BASE64Encoder bASE64Encoder = new BASE64Encoder();
			content.setContent(bASE64Encoder.encode(template));
			logger.debug("template read");
			content.setFileName(temp.getName());
		} catch (NumberFormatException e) {
			logger.error("NumberFormatException", e);
			throw e;
		} catch (EMFUserError e) {
			logger.error("EMFUserError", e);
			throw e;
		} catch (EMFInternalError e) {
			logger.error("EMFUserError", e);
			throw e;
		} finally {
			logger.debug("OUT");
		}

		return content;
	}

	/**
	 * Since SpagoBIJasperReportEngine invokes the readTemplate method also for
	 * subreports, subreport parameters are managed by Jasper. In order to
	 * understand if the required document is a valid subreport, take a look at
	 * execution parameters: the parameter document should be the document id of
	 * the master document and the required document should be a subreport of
	 * its.
	 * 
	 * @param biobj
	 *            The required biobject
	 * @param parameters
	 *            The execution parameters.
	 * @return true if it is a call to retrieve a subreport
	 */
	private boolean isSubReportCall(BIObject biobj, HashMap parameters) {
		logger.debug("IN");
		try {
			Engine engine = biobj.getEngine();
			if (engine
					.getDriverName()
					.equals("it.eng.spagobi.engines.drivers.jasperreport.JasperReportDriver")) {
				String masterReportIdStr = (String) parameters.get("document");
				Integer masterReportId = new Integer(masterReportIdStr);
				if (biobj.getId().equals(masterReportId)) {
					// the required master document is exactly the current
					// document
					return false;
				}
				logger.debug("Jasper master report id: " + masterReportIdStr
						+ ". Looking for subreports...");
				ISubreportDAO subrptdao = DAOFactory.getSubreportDAO();
				List subreportList = subrptdao
						.loadSubreportsByMasterRptId(masterReportId);
				boolean subreportFound = false;
				for (int i = 0; i < subreportList.size(); i++) {
					Subreport subreport = (Subreport) subreportList.get(i);
					if (subreport.getSub_rpt_id().equals(biobj.getId())) {
						subreportFound = true;
						break;
					}
				}
				if (subreportFound) {
					logger.debug("Current required biobject is a subreport, ok.");
				} else {
					logger.error("Subreport not found!!!!");
				}
				return subreportFound;
			} else {
				// other engines than Jasper do not support subreports in this
				// way
				return false;
			}
		} catch (Exception e) {
			logger.error(e);
			return false;
		} finally {
			logger.debug("OUT");
		}
	}

	private boolean checkParametersErrors(IEngUserProfile profile,
			Integer biobjectId, String roleName, Map parameters) {
		logger.debug("IN: user = [" + profile.getUserUniqueIdentifier()
				+ "], biobjectid = [" + biobjectId + "], " + "roleName = ["
				+ roleName + "], parameters = [" + parameters + "]");
		Monitor monitor = MonitorFactory
				.start("spagobi.service.ContentSupplier.checkParametersErrors");
		try {

			String modality = (String) parameters
					.get(SpagoBIConstants.EXECUTION_MODALITY);
			logger.debug("Execution modality retrieved : " + modality);
			ExecutionInstance instance = new ExecutionInstance(profile, "", "",
					biobjectId, roleName, modality, true, true, null);
			
			instance.refreshParametersValues(parameters, true);
			List errors = instance.getParametersErrors();
			if (errors != null && errors.size() > 0) {
				return false;
			} else {
				return true;
			}
		} catch (Exception e) {
			logger.error(e);
			return false;
		} finally {
			logger.debug("OUT");
			monitor.stop();
		}
	}

	/**
	 * Check the correctness of the request, i.e.: 1. if the user is able to see
	 * the required document 2. if there is a correct role for execution (if a
	 * role is specified on the parameters map, it must be a valid role) 3. if
	 * parameters are correct for the user In case the request is not valid (the
	 * previuos conditions are not satisfied), an exception is thrown.
	 * 
	 * @param user
	 *            The user identifier
	 * @param biobj
	 *            The document
	 * @param parameters
	 *            The document parameters
	 * @throws SecurityException
	 * @throws EMFInternalError
	 * @throws EMFUserError
	 */
	private void checkRequestCorrectness(String user, BIObject biobj,
			HashMap parameters) throws SecurityException, EMFInternalError,
			EMFUserError {
		logger.debug("IN: user = [" + user + "], biobjectid = [" + biobj
				+ "], parameters = [" + parameters + "]");
		Monitor monitor = MonitorFactory
				.start("spagobi.service.ContentSupplier.checkRequestCorrectness");
		try {
			if (biobj == null) {
				logger.error("No document specified");
				return;
			}
			logger.debug("Input document: id=[" + biobj.getId() + "], name=["
					+ biobj.getName() + "], label=[" + biobj.getLabel() + "]");
			// creates the user profile
			IEngUserProfile profile = null;
			try {
				profile = UserUtilities.getUserProfile(user);
			} catch (Exception e) {
				logger.error("An error occurred while creating the profile of user ["
						+ user + "]");
				throw new SecurityException(
						"An error occurred while creating the profile of user ["
								+ user + "]", e);
			}

			// Check if the user can execute the document
			boolean canExec = ObjectsAccessVerifier.canExec(biobj, profile);
			if (!canExec) {
				logger.error("Current user cannot execute the required document");
				throw new SecurityException(
						"Current user cannot execute the required document");
			}
			Integer id = biobj.getId();
			// get the correct roles for execution
			List correctRoles = null;
			if (profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV)
					|| profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_USER)
					|| profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN))
				correctRoles = DAOFactory.getBIObjectDAO()
						.getCorrectRolesForExecution(id, profile);
			else
				correctRoles = DAOFactory.getBIObjectDAO()
						.getCorrectRolesForExecution(id);
			logger.debug("correct roles for execution retrived " + correctRoles);
			// at this point correctRoles must contains at least one role, since the user can execute the document

			if (parameters == null) {
				logger.debug("Input parameters map is null. It will be considered as an empty map");
				parameters = new HashMap();
			}

			boolean parametersAreCorrect = false;

			String roleName = (String) parameters.get("SBI_EXECUTION_ROLE");
			if (roleName != null) {
				// if a role is specified, check if it is a valid role for
				// execution
				logger.debug("Execution role specified: " + roleName);
				if (!correctRoles.contains(roleName)) {
					if (correctRoles == null || correctRoles.size() == 0) {
						logger.error("Role [] is not a valid role for the execution of document with id = ["
								+ biobj.getId()
								+ "], label = ["
								+ biobj.getLabel() + "]");
						throw new SecurityException(
								"Role [] is not a valid role for the execution of document with id = ["
										+ biobj.getId() + "], label = ["
										+ biobj.getLabel() + "]");
					}
				}
				// check if parameter values are correct for the role
				parametersAreCorrect = checkParametersErrors(profile,
						biobj.getId(), roleName, parameters);
			} else {
				// if a role is not specified, iterate on valid roles
				logger.debug("Execution role not specified: iterating on all available roles...");
				Iterator it = correctRoles.iterator();
				while (it.hasNext()) {
					roleName = it.next().toString();
					// check if parameter values are correct for the role
					parametersAreCorrect = checkParametersErrors(profile,
							biobj.getId(), roleName, parameters);
					if (parametersAreCorrect) {
						break;
					} else {
						logger.debug("Role " + roleName
								+ " is NOT compatible with input parameters");
					}
				}

			}

			if (!parametersAreCorrect) {
				logger.error("Document cannot be executed by the user with the input parameters.");
				throw new SecurityException(
						"Document cannot be executed by the user with the input parameters.");
			} else {
				logger.debug("Role " + roleName
						+ " is compatible with input parameters");
			}
		} finally {
			monitor.stop();
			logger.debug("OUT");
		}
	}

}
