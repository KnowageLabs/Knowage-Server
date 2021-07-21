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
package it.eng.spagobi.services.content.service;

import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale.Builder;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.GenericValidator;
import org.apache.log4j.Logger;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import it.eng.knowage.security.ProductProfiler;
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
import it.eng.spagobi.engines.drivers.chart.ChartDriver;
import it.eng.spagobi.engines.drivers.kpi.KpiDriver;
import it.eng.spagobi.services.content.bo.Content;
import it.eng.spagobi.services.security.exceptions.SecurityException;
import it.eng.spagobi.utilities.engines.AbstractEngineStartAction;
import it.eng.spagobi.utilities.engines.EngineStartServletIOManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class ContentServiceImplSupplier {
	static private Logger logger = Logger.getLogger(ContentServiceImplSupplier.class);

	/**
	 * Read template.
	 *
	 * @param user     the user
	 * @param document the document
	 *
	 * @return the content
	 *
	 * @throws SecurityException the security exception
	 * @throws EMFUserError      the EMF user error
	 * @throws EMFInternalError  the EMF internal error
	 */
	public Content readTemplate(String user, String document, Map<String, ?> parameters) throws SecurityException, EMFUserError, EMFInternalError {
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
			// check if document can be executed accordingly to EE product mappings
			if (!ProductProfiler.canExecuteDocument(biobj)) {
				throw new SpagoBIRuntimeException("This document cannot be executed within the current product!");
			}
			// only if the user is not Scheduler or Workflow system user or it
			// is a call to retrieve a subreport,
			// check visibility on document and parameter values
			boolean checkNeeded = true;
			boolean modContained = parameters.containsKey("SBI_READ_ONLY_TEMPLATE");
			if (modContained) {
				boolean onlytemplate = parameters.containsValue("true");
				if (onlytemplate) {
					checkNeeded = false;
				}
			}

			if (checkNeeded && !UserProfile.isSchedulerUser(user) && !isSubReportCall(biobj, parameters)) {
				checkRequestCorrectness(user, biobj, parameters);
			}

			IObjTemplateDAO tempdao = DAOFactory.getObjTemplateDAO();
			ObjTemplate temp = tempdao.getBIObjectActiveTemplate(biobj.getId());
			if (temp == null) {
				logger.warn("The template dor document [" + id + "] is NULL");
				if (biobj.getEngine().getDriverName().equals(ChartDriver.class.getName())
						|| biobj.getEngine().getDriverName().equals(KpiDriver.class.getName())) {
					temp = new ObjTemplate();
				} else {
					throw new SecurityException("The template for document [" + id + "] is NULL");
				}
			}
			byte[] template = temp.getContent();
			if (biobj.getEngine().getUrl() != null && !"".equals(biobj.getEngine().getUrl())) {
				// only for external engine calls the elaborateTemplate method (ie. to internationalize template)
				try {
					String driverClassName = biobj.getEngine().getDriverName();
					logger.warn("The driver used is [" + driverClassName + "]");
					IEngineDriver aEngineDriver = (IEngineDriver) Class.forName(driverClassName).newInstance();
					String language = (String) parameters.get(SpagoBIConstants.SBI_LANGUAGE);
					String country = (String) parameters.get(SpagoBIConstants.SBI_COUNTRY);
					if (language == null || country == null) {
						logger.debug("Not locale informations found in parameters... Not setted it at this time.");
					} else {
						logger.debug("Language retrieved: [" + language + "]; country retrieved: [" + country + "]");
						Builder builder = new Builder().setLanguage(language).setRegion(country);
						String script = (String) parameters.get(SpagoBIConstants.SBI_SCRIPT);
						if (StringUtils.isNotBlank(script)) {
							builder.setScript(script);
						}
						aEngineDriver.applyLocale(builder.build());
					}
					logger.warn("Calling elaborateTemplate method defined into the driver ... ");

					if (biobj.getEngine().getDriverName().equals(ChartDriver.class.getName()) && template == null) {
						String emptyString = "";
						template = emptyString.getBytes();
					}

					byte[] elabTemplate = aEngineDriver.ElaborateTemplate(template);
					logger.warn("Finished elaborateTemplate method defined into the driver. ");
					template = elabTemplate;
				} catch (Exception ex) {
					logger.error("Error while getting template: " + ex);
					return null;
				}
			}

			Base64.Encoder bASE64Encoder = Base64.getEncoder();
			if (template != null) {
				content.setContent(bASE64Encoder.encodeToString(template));
			} else {
				content.setContent("");
			}

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
		} catch (Exception e) {
			logger.error("Generic error", e);
			throw e;
		} finally {
			logger.debug("OUT");
		}

		return content;
	}

	/**
	 * Read template by label.
	 *
	 * @param user     the user
	 * @param document the document
	 *
	 * @return the content
	 *
	 * @throws SecurityException the security exception
	 * @throws EMFUserError      the EMF user error
	 * @throws EMFInternalError  the EMF internal error
	 */
	public Content readTemplateByLabel(String user, String label, Map<String, ?> parameters) throws SecurityException, EMFUserError, EMFInternalError {
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
			if (!UserProfile.isSchedulerUser(user) && !isSubReportCall(biobj, parameters)) {
				checkRequestCorrectness(user, biobj, parameters);
			}

			IObjTemplateDAO tempdao = DAOFactory.getObjTemplateDAO();
			ObjTemplate temp = tempdao.getBIObjectActiveTemplate(biobj.getId());
			if (temp == null) {
				logger.warn("The template dor document [" + label + "] is NULL");
				throw new SecurityException("The template dor document [" + label + "] is NULL");
			}
			byte[] template = temp.getContent();

			if (biobj.getEngine().getUrl() != null && !"".equals(biobj.getEngine().getUrl())) {
				// only for external engine calls the elaborateTemplate method (ie. to internationalize template)
				try {
					String driverClassName = biobj.getEngine().getDriverName();
					logger.warn("The driver used is [" + driverClassName + "]");
					IEngineDriver aEngineDriver = (IEngineDriver) Class.forName(driverClassName).newInstance();
					String language = (String) parameters.get(SpagoBIConstants.SBI_LANGUAGE);
					String country = (String) parameters.get(SpagoBIConstants.SBI_COUNTRY);
					if (language == null || country == null) {
						logger.debug("Not locale informations found in parameters... Not setted it at this time.");
					} else {
						logger.debug("Language retrieved: [" + language + "]; country retrieved: [" + country + "]");
						Builder builder = new Builder().setLanguage(language).setRegion(country);
						String script = (String) parameters.get(SpagoBIConstants.SBI_SCRIPT);
						if (StringUtils.isNotBlank(script)) {
							builder.setScript(script);
						}
						aEngineDriver.applyLocale(builder.build());
					}
					logger.warn("Calling elaborateTemplate method defined into the driver ... ");
					byte[] elabTemplate = aEngineDriver.ElaborateTemplate(template);
					logger.warn("Finished elaborateTemplate method defined into the driver. ");
					template = elabTemplate;
				} catch (Exception ex) {
					logger.error("Error while getting template: " + ex);
					return null;
				}
			}

			Base64.Encoder bASE64Encoder = Base64.getEncoder();
			content.setContent(bASE64Encoder.encodeToString(template));
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
	 * Since SpagoBIJasperReportEngine invokes the readTemplate method also for subreports, subreport parameters are managed by Jasper. In order to understand
	 * if the required document is a valid subreport, take a look at execution parameters: the parameter document should be the document id of the master
	 * document and the required document should be a subreport of its.
	 *
	 * @param biobj      The required biobject
	 * @param parameters The execution parameters.
	 * @return true if it is a call to retrieve a subreport
	 */
	private boolean isSubReportCall(BIObject biobj, Map<String, ?> parameters) {
		logger.debug("IN");
		try {
			Engine engine = biobj.getEngine();
			if (engine.getDriverName().equals("it.eng.spagobi.engines.drivers.jasperreport.JasperReportDriver")) {
				String masterReportIdStr = (String) parameters.get("document");
				Integer masterReportId = new Integer(masterReportIdStr);
				if (biobj.getId().equals(masterReportId)) {
					// the required master document is exactly the current
					// document
					return false;
				}
				logger.debug("Jasper master report id: " + masterReportIdStr + ". Looking for subreports...");
				ISubreportDAO subrptdao = DAOFactory.getSubreportDAO();
				List subreportList = subrptdao.loadSubreportsByMasterRptId(masterReportId);
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

	/**
	 * TODO
	 *
	 * @param profile    TODO
	 * @param biobjectId TODO
	 * @param roleName   TODO
	 * @param parameters TODO
	 * @throws SecurityException Map parameters is not compatible with the document in relation to profile e role
	 */
	private void checkParametersCorrectness(IEngUserProfile profile, Integer biobjectId, String roleName, Map parameters) throws SecurityException {
		logger.debug("IN: user id = [" + ((UserProfile) profile).getUserId() + "], biobjectid = [" + biobjectId + "], " + "roleName = [" + roleName
				+ "], parameters = [" + parameters + "]");
		Monitor monitor = MonitorFactory.start("spagobi.service.ContentSupplier.checkParametersErrors");
		try {

			String modality = (String) parameters.get(SpagoBIConstants.EXECUTION_MODALITY);
			logger.debug("Execution modality retrieved : " + modality);
			ExecutionInstance instance = new ExecutionInstance(profile, "", "", biobjectId, roleName, modality, true, true, null);

			instance.refreshParametersValues(parameters, true);
			boolean onEditMode = (parameters.get(EngineStartServletIOManager.ON_EDIT_MODE) != null);
			List errors = instance.getParametersErrors(onEditMode);
			if (errors != null && errors.size() > 0) {
				String msg = String.format(
						"Document with id %s cannot be executed by the user [%s] with role [%s] with the input parameters [%s]. With following errors: [%s]",
						biobjectId, profile, roleName, parameters, errors);
				logger.error(msg);
				throw new SecurityException(msg);
			}
		} catch (Exception e) {
			String msg = String.format("Error during execution of document with id %s by the user [%s] with role [%s] with the input parameters [%s]",
					biobjectId, profile, roleName, parameters);
			logger.error(msg, e);
			throw new SecurityException(msg, e);
		} finally {
			logger.debug("OUT");
			monitor.stop();
		}
	}

	/**
	 * Check the correctness of the request, i.e.: 1. if the user is able to see the required document 2. if there is a correct role for execution (if a role is
	 * specified on the parameters map, it must be a valid role) 3. if parameters are correct for the user In case the request is not valid (the previuos
	 * conditions are not satisfied), an exception is thrown.
	 *
	 * @param user       The user identifier
	 * @param biobj      The document
	 * @param parameters The document parameters
	 * @throws SecurityException
	 * @throws EMFInternalError
	 * @throws EMFUserError
	 */
	private void checkRequestCorrectness(String user, BIObject biobj, Map<String, ?> parameters) throws SecurityException, EMFInternalError, EMFUserError {
		logger.debug("IN: user = [" + user + "], biobjectid = [" + biobj + "], parameters = [" + parameters + "]");
		Monitor monitor = MonitorFactory.start("spagobi.service.ContentSupplier.checkRequestCorrectness");
		try {
			if (biobj == null) {
				logger.error("No document specified");
				return;
			}
			logger.debug("Input document: id=[" + biobj.getId() + "], name=[" + biobj.getName() + "], label=[" + biobj.getLabel() + "]");
			// creates the user profile
			IEngUserProfile profile = null;
			try {
				profile = UserUtilities.getUserProfile(user);
			} catch (Exception e) {
				logger.error("An error occurred while creating the profile of user [" + user + "]");
				throw new SecurityException("An error occurred while creating the profile of user [" + user + "]", e);
			}

			// Check if the user can execute the document
			boolean canExec = ObjectsAccessVerifier.canExec(biobj, profile);
			if (!canExec) {
				logger.error("Current user cannot execute the required document");
				throw new SecurityException("Current user cannot execute the required document");
			}

			Integer id = biobj.getId();
			// get the correct roles for execution
			List correctRoles = null;
			if (profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV)
					|| profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_USER)
					|| profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN))
				correctRoles = DAOFactory.getBIObjectDAO().getCorrectRolesForExecution(id, profile);
			else
				correctRoles = DAOFactory.getBIObjectDAO().getCorrectRolesForExecution(id);
			logger.debug("correct roles for execution retrived " + correctRoles);
			// at this point correctRoles must contains at least one role, since the user can execute the document

			if (parameters == null) {
				logger.debug("Input parameters map is null. It will be considered as an empty map");
				parameters = new HashMap();
			}

			if (isOLAPSubObjectExecution(parameters)) {
				// in case of the execution of an OLAP subobject, we skip the validations on drivers for now
				// TODO implement validation also for OLAP subobjects
				logger.debug("Current request is for OLAP subobject, skipping drivers validation");
				return;
			}

			String roleName = (String) parameters.get("SBI_EXECUTION_ROLE");
			if (roleName != null) {
				// if a role is specified, check if it is a valid role for
				// execution
				logger.debug("Execution role specified: " + roleName);
				if (!correctRoles.contains(roleName)) {
					if (correctRoles == null || correctRoles.size() == 0) {
						logger.error("Role [] is not a valid role for the execution of document with id = [" + biobj.getId() + "], label = [" + biobj.getLabel()
								+ "]");
						throw new SecurityException("Role [] is not a valid role for the execution of document with id = [" + biobj.getId() + "], label = ["
								+ biobj.getLabel() + "]");
					}
				}
				// check if parameter values are correct for the role
				checkParametersCorrectness(profile, biobj.getId(), roleName, parameters);
			} else {
				// if a role is not specified, iterate on valid roles
				logger.debug("Execution role not specified: iterating on all available roles...");
				Iterator it = correctRoles.iterator();
				while (it.hasNext()) {
					roleName = it.next().toString();
					// check if parameter values are correct for the role
					checkParametersCorrectness(profile, biobj.getId(), roleName, parameters);
				}

			}

			logger.debug("Role " + roleName + " is compatible with input parameters");
		} finally {
			monitor.stop();
			logger.debug("OUT");
		}
	}

	private boolean isOLAPSubObjectExecution(Map<String, ?> parameters) {
		Object subObjectId = parameters.get(AbstractEngineStartAction.SUBOBJ_ID);
		// in case subobject id is there and it is an integer, then it is an OLAP subobject execution request
		boolean toReturn = subObjectId != null && GenericValidator.isInt(subObjectId.toString());
		logger.debug("Current request is for OLAP subobject? " + toReturn);
		return toReturn;
	}

}
