/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.sdk.domains.impl;

import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.sdk.AbstractSDKService;
import it.eng.spagobi.sdk.documents.bo.SDKDocument;
import it.eng.spagobi.sdk.domains.DomainsService;
import it.eng.spagobi.sdk.domains.bo.SDKDomain;
import it.eng.spagobi.sdk.engines.EnginesService;
import it.eng.spagobi.sdk.engines.bo.SDKEngine;
import it.eng.spagobi.sdk.exceptions.NotAllowedOperationException;
import it.eng.spagobi.sdk.exceptions.SDKException;
import it.eng.spagobi.sdk.utilities.SDKObjectsConverter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

public class DomainsServiceImpl extends AbstractSDKService implements DomainsService {

	static private Logger logger = Logger.getLogger(DomainsServiceImpl.class);

	public boolean insertDomain(SDKDomain sdkDomain) throws SDKException {
		logger.error("Service not implemented");
		return false;
	}

	public boolean updateDomain(SDKDomain sdkDomain) throws SDKException {
		logger.error("Service not implemented");
		return false;
	}

	public SDKDomain getDomainById(Integer valueId) throws SDKException {
		logger.error("Service not implemented");
		return null;

	}

	public SDKDomain getDomainByDomainAndValueCd(String domainCd, String valueCd)
			throws SDKException {
		logger.error("Service not implemented");
		return null;
	}

	public SDKDomain[] getAllDomains() throws SDKException {
		logger.error("Service not implemented");
		return null;

	}

	public SDKDomain[] getDomainsListByDomainCd(String domainCd)
			throws SDKException {
		SDKDomain[] toReturn = null;
		logger.debug("IN: domainCd to retrieve = " + domainCd);

		this.setTenant();

		try {
			if (domainCd == null) {
				logger.warn("DomainCd in input is null!");
				return null;
			}
			 List domains = DAOFactory.getDomainDAO().loadListDomainsByType(domainCd);
			if (domains == null) {
				logger.warn("SbiDomain with domainCd [" + domainCd + "] not existing.");
				return null;
			}
			
			toReturn= new SDKDomain[domains.size()];
			int i = 0;
			for (Iterator iterator = domains.iterator(); iterator.hasNext();) {
				Domain domain = (Domain) iterator.next();
				
				SDKDomain sdkDomain = new SDKObjectsConverter().fromDomainToSDKDomain(domain);
				toReturn[i] = sdkDomain;
				i++;
			}
			
			
		} catch (Exception e) {
			logger.error("Error while retrieving document", e);
		} finally {
			this.unsetTenant();
			logger.debug("OUT");
		}
		return toReturn;
	}
	
}
