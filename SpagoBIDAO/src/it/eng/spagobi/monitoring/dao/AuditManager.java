/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.monitoring.dao;

import it.eng.spago.base.SourceBean;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.SubObject;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.behaviouralmodel.lov.bo.ModalitiesValue;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
//import it.eng.spagobi.commons.utilities.ParameterValuesEncoder;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.monitoring.metadata.SbiAudit;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

public class AuditManager {

	static private Logger logger = Logger.getLogger(AuditManager.class);

	public static final String MODULE_NAME = "AuditService";

	public static final String AUDIT_ID = "SPAGOBI_AUDIT_ID";
	public static final String EXECUTION_START = "SPAGOBI_AUDIT_EXECUTION_START";
	public static final String EXECUTION_END = "SPAGOBI_AUDIT_EXECUTION_END";
	public static final String EXECUTION_STATE = "SPAGOBI_AUDIT_EXECUTION_STATE";
	public static final String ERROR_MESSAGE = "SPAGOBI_AUDIT_ERROR_MESSAGE";
	public static final String ERROR_CODE = "SPAGOBI_AUDIT_ERROR_CODE";

	private static AuditManager _instance = null;

	private static boolean _disabled = false;
	private static String _documentState = "ALL";
	private static IAuditDAO _auditDAO = null;
	
    private String separator;
    private String openBlockMarker;
    private String closeBlockMarker;

	private AuditManager() {
		logger.debug("Begin istantiation of AuditManager");
		SourceBean config = (SourceBean) ConfigSingleton.getInstance().getAttribute("AUDIT.CONFIG");
		logger.debug("Audit configuration found: \n" + config.toString());
		String disable = (String) config.getAttribute("disable");
		if (disable != null && disable.toLowerCase().trim().equals("true")) {
			_disabled = true;
		}

		if (!_disabled) {
			/*
			 * loads the document state and try to find it in the SBI_DOMAINS
			 * table; if it does not exist, the default value is considered
			 */
			String documentState = (String) config.getAttribute("document_state");
			logger.debug("document_state="+documentState);
			if (documentState != null) {
				documentState = documentState.toUpperCase().trim();
				if (!documentState.toUpperCase().trim().equals("ALL")) {
					List availableStates = new ArrayList();
					try {
						availableStates = DAOFactory.getDomainDAO().loadListDomainsByType("STATE");
					} catch (EMFUserError e) {
						logger.error("Error while getting available document states from db", e);
					}
					boolean stateFound = false;
					Iterator it = availableStates.iterator();
					while (it.hasNext()) {
						Domain aDomain = (Domain) it.next();
						if (aDomain.getValueCd().equalsIgnoreCase(documentState)) {
							stateFound = true;
							break;
						}
					}
					if (stateFound) {
						_documentState = documentState;
					}
				}
			}

			/*
			 * instantiates the persistence class; if some errors occur, the
			 * audit log is disabled
			 */
			String persistenceClassName = (String) config.getAttribute("persistenceClass");
			try {
				Class persistenceClass = Class.forName(persistenceClassName);
				_auditDAO = (IAuditDAO) persistenceClass.newInstance();
				
			} catch (Exception e) {
				logger.error("Error while instantiating persistence class. Audit log will be disabled", e);
				_disabled = true;
			}
		}
		logger.debug("AuditManager instatiation end");
	}

	/**
	 * Gets the single instance of AuditManager.
	 * 
	 * @return single instance of AuditManager
	 */
	public static AuditManager getInstance() {
		if (_instance == null) {
			_instance = new AuditManager();
		}
		return _instance;
	}

	/**
	 * Load audit.
	 * 
	 * @param id the id
	 * 
	 * @return the sbi audit
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public SbiAudit loadAudit(Integer id) throws EMFUserError {
		SbiAudit aSbiAudit = _auditDAO.loadAuditByID(id);
		return aSbiAudit;
	}

	/**
	 * Insert audit.
	 * 
	 * @param aSbiAudit the a sbi audit
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	private void insertAudit(SbiAudit aSbiAudit) throws EMFUserError {
		if (canBeRegistered(aSbiAudit))
			_auditDAO.insertAudit(aSbiAudit);
	}

	/**
	 * Modify audit.
	 * 
	 * @param aSbiAudit the a sbi audit
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	private void modifyAudit(SbiAudit aSbiAudit) throws EMFUserError {
		if (canBeRegistered(aSbiAudit))
			_auditDAO.modifyAudit(aSbiAudit);
	}

	private boolean canBeRegistered(SbiAudit aSbiAudit) {
		if (!_disabled) {
			if (_documentState.equalsIgnoreCase("ALL") || _documentState.equalsIgnoreCase(aSbiAudit.getDocumentState())) {
				return true;
			} else {
				logger.debug("AuditManager is disabled for documents with state " + aSbiAudit.getDocumentState());
				return false;
			}
		} else {
			logger.debug("AuditManager is disabled, so no records can be modified");
			return false;
		}
	}

	/**
	 * Inserts a record on the audit log.
	 * 
	 * @param obj The BIObject being executed
	 * @param profile The user profile
	 * @param role The execution role
	 * @param modality The execution modality
	 * @param subObj the sub obj
	 * 
	 * @return The Integer representing the execution id
	 */
	public Integer insertAudit(BIObject obj, SubObject subObj, IEngUserProfile profile, String role, String modality) {
		logger.debug("IN");
		_auditDAO.setUserProfile(profile);
		SbiAudit audit = new SbiAudit();
		logger.debug("userID for audit"+ ((UserProfile)profile).getUserId().toString());
		audit.setUserName(((UserProfile)profile).getUserId().toString());

		audit.setUserGroup(role);
		audit.setDocumentId(obj.getId());
		audit.setDocumentLabel(obj.getLabel());
		audit.setDocumentName(obj.getName());
		audit.setDocumentType(obj.getBiObjectTypeCode());
		audit.setDocumentState(obj.getStateCode());

		String documentParameters = "";
		List parameters = obj.getBiObjectParameters();
		if (parameters != null && parameters.size() > 0) {
			for (int i = 0; i < parameters.size(); i++) {
				BIObjectParameter parameter = (BIObjectParameter) parameters.get(i);
				documentParameters += parameter.getParameterUrlName() + "=";
				if (parameter.getParameterValues() != null) {
					String value = encode(parameter);
					documentParameters += value;
				} else
					documentParameters += "NULL";
				if (i < parameters.size() - 1)
					documentParameters += "&";
			}
		}
		audit.setDocumentParameters(documentParameters);
		if (subObj != null) {
			audit.setSubObjId(subObj.getId());
			audit.setSubObjName(subObj.getName());
			audit.setSubObjOwner(subObj.getOwner());
			audit.setSubObjIsPublic(subObj.getIsPublic().booleanValue() ? new Short((short) 1) : new Short((short) 0));
		}
		Engine engine = obj.getEngine();
		audit.setEngineId(engine.getId());
		audit.setEngineLabel(engine.getLabel());
		audit.setEngineName(engine.getName());
		Domain engineType = null;
		try {
			engineType = DAOFactory.getDomainDAO().loadDomainById(engine.getEngineTypeId());
		} catch (EMFUserError e) {
			logger.error("Error retrieving document's engine information", e);
		}
		audit.setEngineType(engineType != null ? engineType.getValueCd() : null);
		if (engineType != null) {
			if ("EXT".equalsIgnoreCase(engineType.getValueCd())) {
				audit.setEngineUrl(engine.getUrl());
				audit.setEngineDriver(engine.getDriverName());
			} else {
				audit.setEngineClass(engine.getClassName());
			}
		}
		audit.setRequestTime(new Timestamp(System.currentTimeMillis()));
		audit.setExecutionModality(modality);
		audit.setExecutionState("EXECUTION_REQUESTED");

		try {
			insertAudit(audit);
		} catch (EMFUserError e) {
			logger.error("Error doing audit insertion", e);
			return null;
		}
		logger.debug("OUT");
		return audit.getId();
	}

	/**
	 * Update audit.
	 * 
	 * @param auditId the audit id
	 * @param startTime the start time
	 * @param endTime the end time
	 * @param executionState the execution state
	 * @param errorMessage the error message
	 * @param errorCode the error code
	 */
	public void updateAudit(Integer auditId, Long startTime, Long endTime, String executionState, String errorMessage,
			String errorCode) {
		logger.debug("IN");
		if (auditId == null) {
			logger.warn("Audit record id not specified, no updating is possible.");
			return;
		}

		SbiAudit audit=null;
		try {
			audit = loadAudit(auditId);
			if (audit==null) {
				logger.error("audit==null ");
				return;
			}
		} catch (EMFUserError e) {
			logger.error("Error loading audit record with id = [" + auditId.toString() + "]", e);
			logger.debug("OUT");
			return;
		}

		if (audit.getExecutionStartTime() != null && audit.getExecutionEndTime() != null) {
			logger.warn("Audit record with id = [" + auditId.toString() + "] has already a start time and an "
					+ "end time. This record will not be modified.");
			logger.debug("OUT");
			return;
		}

		if (startTime != null) {
			Date executionStartTime = new Date(startTime.longValue());
			audit.setExecutionStartTime(executionStartTime);
		}
		if (endTime != null) {
			Date executionEndTime = new Date(endTime.longValue());
			audit.setExecutionEndTime(executionEndTime);
			Date executionStartTime = audit.getExecutionStartTime();
			if (executionStartTime != null) {
				// calculates exectuion time as a difference in ms
				long executionTimeLongMSec = endTime.longValue() - executionStartTime.getTime();
				// calculates exectuion time as a difference in s
				int executionTimeIntSec = Math.round(executionTimeLongMSec / 1000);
				Integer executionTime = new Integer(executionTimeIntSec);
				audit.setExecutionTime(executionTime);
			}
		}
		if (executionState != null && !executionState.trim().equals("")) {
			audit.setExecutionState(executionState);
		}
		if (errorMessage != null && !errorMessage.trim().equals("")) {
			audit.setErrorMessage(errorMessage);
			audit.setError(new Short((short) 1));
		} else {
			audit.setError(new Short((short) 0));
		}
		if (errorCode != null && !errorCode.trim().equals("")) {
			audit.setErrorCode(errorCode);
		}

		try {
			modifyAudit(audit);
		} catch (EMFUserError e) {
			logger.error("Error updating audit record with id = [" + auditId.toString() + "]", e);
			logger.debug("OUT");
			return;
		}
		logger.debug("OUT");
	}

	/**
	 * Gets the most popular.
	 * 
	 * @param profile the profile
	 * @param limit the limit
	 * 
	 * @return the most popular
	 */
	public List getMostPopular(IEngUserProfile profile, int limit) {
		logger.debug("IN");
		List toReturn = new ArrayList();
		try {
			Collection roles = null;
			roles = ((UserProfile)profile).getRolesForUse();
			toReturn = _auditDAO.getMostPopular(roles, limit);
		} catch (Exception e) {
			logger
			.error("Error while loading most popular for user " + ((UserProfile)profile).getUserId().toString(),
					e);
		} finally {
			logger.debug("OUT");
		}
		return toReturn;
	}

	/**
	 * Gets the my recently used.
	 * 
	 * @param profile the profile
	 * @param limit the limit
	 * 
	 * @return the my recently used
	 */
	public List getMyRecentlyUsed(IEngUserProfile profile, int limit) {
		logger.debug("IN");
		List toReturn = new ArrayList();
		try {
			toReturn = _auditDAO.getMyRecentlyUsed(((UserProfile)profile).getUserId().toString(), limit);
		} catch (Exception e) {
			logger.error("Error while loading my recently used for user "
					+ ((UserProfile)profile).getUserId().toString(), e);
		} finally {
			logger.debug("OUT");
		}
		return toReturn;
	}

	/**
	 * Gets the last execution.
	 * 
	 * @param objId the obj id
	 * 
	 * @return the last execution
	 */
	public SbiAudit getLastExecution(Integer objId) {
		logger.debug("IN");
		SbiAudit toReturn = new SbiAudit();
		try {
			toReturn = _auditDAO.getLastExecution(objId);
		} catch (Exception e) {
			logger.error("Error while loading my last execution for document " + objId, e);
		} finally {
			logger.debug("OUT");
		}
		return toReturn;
	}

	/**
	 * Gets the medium exec time.
	 * 
	 * @param objId the obj id
	 * 
	 * @return the medium exec time
	 */
	public Double getMediumExecTime(Integer objId) {
		logger.debug("IN");
		Double toReturn = new Double(0);
		try {
			toReturn = _auditDAO.getMediumExecTime(objId);
		} catch (Exception e) {
			logger.error("Error while calculating the medium execution time for document " + objId, e);
		} finally {
			logger.debug("OUT");
		}
		return toReturn;
	}
	
	/*
	 * Methods copied from ParameterValuesEncoder for DAO Refactoring
	 */
	
	/**
	 * Encode.
	 * 
	 * @param biobjPar the biobj par
	 * 
	 * @return the string
	 */
	public String encode(BIObjectParameter biobjPar) {
		logger.debug("IN");
		if (biobjPar.getParameterValues() == null) {
			logger.debug("biobjPar.getParameterValues() == null");
			return null;
		}



		Parameter parameter = biobjPar.getParameter();
		if (parameter != null) {

			String type = parameter.getType();
			ModalitiesValue modValue = parameter.getModalityValue();
			if (modValue != null) {

				boolean multivalue =  biobjPar.isMultivalue();

				String typeCode = biobjPar.getParameter().getModalityValue().getITypeCd();
				logger.debug("typeCode="+typeCode);
				if (typeCode.equalsIgnoreCase(SpagoBIConstants.INPUT_TYPE_MAN_IN_CODE)) {
					multivalue = false;
				}

				if (!multivalue) {
					return (String) biobjPar.getParameterValues().get(0);
				} else {
					return encodeMultivaluesParam(biobjPar.getParameterValues(), type);
				}
			} else {
				List values = biobjPar.getParameterValues();
				if (values != null && values.size() > 0) {
					if (values.size() == 1)
						return (String) biobjPar.getParameterValues().get(0);
					else
						return encodeMultivaluesParam(biobjPar.getParameterValues(), type);
				} else
					return "";
			}
		} else {
			Integer parId = biobjPar.getParID();
			String type = null;
			if (parId == null) {
				logger.warn("Parameter object nor parameter id are set into BiObjectPrameter with label = "
						+ biobjPar.getLabel() + " of document with id = " + biobjPar.getBiObjectID());
			} else {
				try {
					Parameter aParameter = DAOFactory.getParameterDAO().loadForDetailByParameterID(parId);
					type = aParameter.getType();
				} catch (EMFUserError e) {
					logger.warn("Error loading parameter with id = " + biobjPar.getParID());
				}
			}
			List values = biobjPar.getParameterValues();
			if (values != null && values.size() > 0) {
				if (values.size() == 1)
					return (String) biobjPar.getParameterValues().get(0);
				else
					return encodeMultivaluesParam(biobjPar.getParameterValues(), type);
			} else
				return "";
		}

	}
	
	/**
	 * Multi values parameters are encoded in the following way:
	 * openBlockMarker + separator + openBlockMarker + [values separated by the separator] + closeBlockMarker + parameterType + closeBlockMarker
	 * Examples:
	 * {,{string1,string2,string3}STRING}
	 * {,{number1,number1,number1}NUM}
	 * 
	 * parameterType: the type of the parameter (NUM/STRING/DATE)
	 */
	private String encodeMultivaluesParam(List values, String parameterType) {
		logger.debug("IN");
		String value = "";

		if (values == null || values.size() == 0)
			return value;

		value += openBlockMarker;
		value += separator;
		value += openBlockMarker;
		for (int i = 0; i < values.size(); i++) {
			String valueToBeAppended = (values.get(i) == null) ? "" : (String) values.get(i);
			value += (i > 0) ? separator : "";
			value += valueToBeAppended;
		}
		value += closeBlockMarker;
		value += parameterType;
		value += closeBlockMarker;
		logger.debug("IN.value=" + value);
		return value;
	}
	
	//----------------------------------------------------------------
}
