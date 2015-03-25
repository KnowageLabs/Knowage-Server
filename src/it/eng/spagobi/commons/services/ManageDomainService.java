/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

/**
 * Object name
 * 
 * ManageDomains
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
import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IDomainDAO;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONAcknowledge;
import it.eng.spagobi.utilities.service.JSONSuccess;

public class ManageDomainService extends AbstractSpagoBIAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// logger component
	private static Logger logger = Logger.getLogger(ManageDomainService.class);

	// Service parameter
	private final String MESSAGE_DET = "MESSAGE_DET";

	private static final String DOMAIN_LIST = "DOMAIN_LIST";
	private static final String DOMAIN_DELETE = "DOMAIN_DELETE";
	private static final String DOMAIN_SAVE = "DOMAIN_SAVE";
	private static final String DOMAINS_FILTER = "DOMAINS_FILTER";


	private IEngUserProfile profile = null;
	private IDomainDAO domainDao=null;
	@Override
	public void doService() {
		
		String serviceType=null;
		profile=getUserProfile();

		logger.debug("IN");

		try {
			domainDao = DAOFactory.getDomainDAO();
			domainDao.setUserProfile(profile);
		} catch (EMFUserError e1) {
			logger.error(e1.getMessage(), e1);
			throw new SpagoBIServiceException(SERVICE_NAME, "Error occurred");
		}

		serviceType = this.getAttributeAsString(MESSAGE_DET);
		logger.debug("Parameter [" + MESSAGE_DET + "] is equal to ["
				+ serviceType + "]");

		if (serviceType != null) {
			if (serviceType.equalsIgnoreCase(DOMAIN_LIST)) {
				doDomainList();
			} else if (serviceType.equalsIgnoreCase(DOMAINS_FILTER)) {
				if(this.requestContainsAttribute("DOMAIN_TYPE")){
					String domainType = this.getAttributeAsString("DOMAIN_TYPE");
					doDomainList(domainType);
				}
			} else if (serviceType.equalsIgnoreCase(DOMAIN_DELETE)) {
				doDelete();
			} else if (serviceType.equalsIgnoreCase(DOMAIN_SAVE)) {
				doSave();
			} else {
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Unable to execute service [" + serviceType + "]");
			}
		}

		logger.debug("OUT");

	}

	public void doSave() {

		logger.debug("IN");
		Domain domain = null;
		Domain domainTemp = null;
		try {
			domain = this.setDomain();
			domainTemp = domainDao.loadDomainByCodeAndValue(domain.getDomainCode(), domain.getValueCd());
		} catch (Throwable e) {
			logger.error("Exception occurred while saving config data", e);
			throw new SpagoBIServiceException(SERVICE_NAME,
					"Impossible to save domain", e);
		}
		if (domainTemp != null && !domainTemp.getValueId().equals(domain.getValueId())) {
			throw new SpagoBIServiceException(SERVICE_NAME,
					"Domain Code and Value Code already in use");
		}
		try {
			logger.debug("Save domain");
			
			domainDao.saveDomain(domain);
			JSONObject response = new JSONObject();
			response.put("VALUE_ID", domain.getValueId());
			writeBackToClient(new JSONSuccess(response));
			
		} catch (Throwable e) {
			logger.error("Exception occurred while saving config data", e);
			throw new SpagoBIServiceException(SERVICE_NAME,
					"Impossible to save domain", e);
		} finally {
			logger.debug("OUT");
		}
	}

	public void doDelete() {
		try {
			logger.debug("Delete domain");
			Integer valueId = this.getAttributeAsInteger("VALUE_ID");
			domainDao.delete(valueId);
			JSONObject response = new JSONObject();
			response.put("VALUE_ID", valueId);
			writeBackToClient(new JSONSuccess(response));

		} catch (Throwable e) {
			logger.error("Exception occurred while retrieving domain data", e);
			throw new SpagoBIServiceException(SERVICE_NAME,
					"Impossible to delete domain", e);
		}
	}

	public void doDomainList() {
		try {
			logger.debug("Loaded domain list");

			List<Domain> domainList = domainDao.loadListDomains();

			JSONArray domainListJSON = (JSONArray) SerializerFactory
					.getSerializer("application/json").serialize(domainList,
							this.getLocale());
			JSONObject response = new JSONObject();
			response.put("response", domainListJSON);

			writeBackToClient(new JSONSuccess(response));

		} catch (Throwable e) {
			logger.error("Exception occurred while retrieving domain data", e);
			throw new SpagoBIServiceException(SERVICE_NAME,
					"Exception occurred while retrieving domain data", e);
		}
	}

	public void doDomainList(String domainType) {
		try {
			logger.debug("Loaded domain list");

			List<Domain> domainList = domainDao.loadListDomainsByType(domainType);	
			
			JSONArray domainListJSON = (JSONArray) SerializerFactory
					.getSerializer("application/json").serialize(domainList,
							this.getLocale());
			JSONObject response = new JSONObject();
			response.put("domains", domainListJSON);

			writeBackToClient(new JSONSuccess(response));

		} catch (Throwable e) {
			logger.error("Exception occurred while retrieving domain data", e);
			throw new SpagoBIServiceException(SERVICE_NAME,
					"Exception occurred while retrieving domain data", e);
		}
	}


	public Domain setDomain() {
		Domain domain = new Domain();
		if(this.requestContainsAttribute("VALUE_ID")){
			domain.setValueId(this.getAttributeAsInteger("VALUE_ID"));
		}
		domain.setValueCd(this.getAttributeAsString("VALUE_CD"));
		domain.setValueName(this.getAttributeAsString("VALUE_NM"));
		domain.setDomainCode(this.getAttributeAsString("DOMAIN_CD"));
		domain.setDomainName(this.getAttributeAsString("DOMAIN_NM"));
		domain.setValueDescription(this.getAttributeAsString("VALUE_DS"));

		return domain;

	}

}