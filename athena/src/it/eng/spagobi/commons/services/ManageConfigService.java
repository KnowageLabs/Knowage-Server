/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

/**
 * Object name
 * 
 * ManageConfig
 * 
 * 
 * Public Properties
 * 
 * [list]
 * 
 * 
 * Public Methods
 * 
 * [list]
 * 
 * 
 * Public Events
 * 
 * [list]
 * 
 * Authors
 * 
 * Monia Spinelli (monia.spinelli@eng.it)
 */
package it.eng.spagobi.commons.services;

import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;

import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.DomainDAOHibImpl;
import it.eng.spagobi.commons.dao.IConfigDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONAcknowledge;
import it.eng.spagobi.utilities.service.JSONSuccess;

public class ManageConfigService extends AbstractSpagoBIAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// logger component
	private static Logger logger = Logger.getLogger(ManageDomainService.class);

	// Service parameter
	private final String MESSAGE_DET = "MESSAGE_DET";

	private static final String CONFIG_LIST = "CONFIG_LIST";
	private static final String CONFIG_DELETE = "CONFIG_DELETE";
	private static final String CONFIG_SAVE = "CONFIG_SAVE";

	private IEngUserProfile profile = null;
	IConfigDAO configDao = null;

	@Override
	public void doService() {
		logger.debug("IN");
		String serviceType = null;
		profile = getUserProfile();

		try {
			configDao = DAOFactory.getSbiConfigDAO();
			configDao.setUserProfile(profile);
		} catch (EMFUserError e1) {
			logger.error(e1.getMessage(), e1);
			throw new SpagoBIServiceException(SERVICE_NAME, "Error occurred");
		}

		serviceType = this.getAttributeAsString(MESSAGE_DET);
		logger.debug("Parameter [" + MESSAGE_DET + "] is equal to ["
				+ serviceType + "]");

		if (serviceType != null) {
			if (serviceType.equalsIgnoreCase(CONFIG_LIST)) {
				doConfigList();
			} else if (serviceType.equalsIgnoreCase(CONFIG_DELETE)) {
				doDelete();
			} else if (serviceType.equalsIgnoreCase(CONFIG_SAVE)) {
				doSave();
			} else {
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Unable to execute service [" + serviceType + "]");
			}
		} else {
			logger.warn("The service type is missing");
		}

		logger.debug("OUT");

	}

	public void doSave() {

		logger.debug("IN");
		Config config = null;
		Config configTemp = null;

		try {
			config = readConfig();
			configTemp = configDao.loadConfigParametersByLabel(config
					.getLabel());
		} catch (Throwable e) {
			logger.error("Exception occurred while saving config data", e);
			throw new SpagoBIServiceException(SERVICE_NAME,
					"Impossible to save config", e);
		}
		if (configTemp != null && !configTemp.getId().equals(config.getId())) {
			throw new SpagoBIServiceException(SERVICE_NAME,
					"Label already in use");
		}
		try {
			configDao.saveConfig(config);
			JSONObject response = new JSONObject();
			response.put("ID", config.getId());
			writeBackToClient(new JSONSuccess(response));
		} catch (Throwable e) {
			logger.error("Exception occurred while saving config data", e);
			throw new SpagoBIServiceException(SERVICE_NAME,
					"Impossible to save config", e);
		} finally {
			logger.debug("OUT");
		}
	}

	public void doDelete() {
		logger.debug("IN");
		try {
			logger.debug("Delete config");
			Integer id = this.getAttributeAsInteger("ID");
			configDao.delete(id);
			JSONObject response = new JSONObject();
			response.put("ID", id);
			writeBackToClient(new JSONSuccess(response));

		} catch (Throwable e) {
			logger.error("Exception occurred while retrieving config data", e);
			throw new SpagoBIServiceException(SERVICE_NAME,
					"Impossible to delete config", e);
		} finally {
			logger.debug("OUT");
		}
	}

	public void doConfigList() {
		logger.debug("IN");
		try {
			logger.debug("Loaded config list");

			List<Config> configList = DAOFactory.getSbiConfigDAO()
					.loadAllConfigParameters();

			JSONArray configListJSON = (JSONArray) SerializerFactory
					.getSerializer("application/json").serialize(configList,
							this.getLocale());
			JSONObject response = new JSONObject();
			response.put("response", configListJSON);
			writeBackToClient(new JSONSuccess(response));

		} catch (Throwable e) {
			logger.error("Exception occurred while retrieving config data", e);
			throw new SpagoBIServiceException(SERVICE_NAME,
					"Exception occurred while retrieving config data", e);
		} finally {
			logger.debug("OUT");
		}
	}

	private Config readConfig() throws EMFUserError {
		logger.debug("IN");
		Config config = new Config();
		if (this.requestContainsAttribute("ID")) {
			config.setId(this.getAttributeAsInteger("ID"));
		}
		config.setLabel(this.getAttributeAsString("LABEL"));
		config.setName(this.getAttributeAsString("NAME"));
		config.setDescription(this.getAttributeAsString("DESCRIPTION"));
		config.setActive(this.getAttributeAsBoolean("IS_ACTIVE"));
		config.setValueCheck(this.getAttributeAsString("VALUE_CHECK"));
		config.setCategory(this.getAttributeAsString("CATEGORY"));
		if (this.requestContainsAttribute("VALUE_TYPE")) {
			DomainDAOHibImpl domain = new DomainDAOHibImpl();
			Domain dom = domain.loadDomainByCodeAndValue("PAR_TYPE", this
					.getAttributeAsString("VALUE_TYPE"));
			config.setValueTypeId(dom.getValueId());
		}

		logger.debug("OUT");
		return config;

	}

}