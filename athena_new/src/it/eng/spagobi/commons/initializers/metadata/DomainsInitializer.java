/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.initializers.metadata;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class DomainsInitializer extends SpagoBIInitializer {

	static private Logger logger = Logger.getLogger(DomainsInitializer.class);

	public DomainsInitializer() {
		targetComponentName = "Domains";
		configurationFileName = "it/eng/spagobi/commons/initializers/metadata/config/domains.xml";
	}
	
	public void init(SourceBean config, Session hibernateSession) {
		logger.debug("IN");
		try {
			String hql = "from SbiDomains";
			Query hqlQuery = hibernateSession.createQuery(hql);
			List domains = hqlQuery.list();
			if (domains.isEmpty()) {
				logger.info("Domains table is empty. Starting populating domains...");
				writeDomains(hibernateSession);
			} else {
				logger.debug("Domains table is already populated, only missing domains will be populated");
				writeMissingDomains(hibernateSession);				
			}
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("Ab unexpected error occured while initializeng Domains", t);
		} finally {
			logger.debug("OUT");
		}
	}
	
	private void writeDomains(Session aSession) throws Exception {
		logger.debug("IN");
		SourceBean domainsSB = getConfiguration();
		if (domainsSB == null) {
			throw new Exception("Domains configuration file not found!!!");
		}
		List domainsList = domainsSB.getAttributeAsList("DOMAIN");
		if (domainsList == null || domainsList.isEmpty()) {
			throw new Exception("No predefined domains found!!!");
		}
		Iterator it = domainsList.iterator();
		while (it.hasNext()) {
			SourceBean aDomainSB = (SourceBean) it.next();
			SbiDomains aDomain = new SbiDomains();
			aDomain.setDomainCd((String) aDomainSB.getAttribute("domainCd"));
			aDomain.setDomainNm((String) aDomainSB.getAttribute("domainNm"));
			aDomain.setValueCd((String) aDomainSB.getAttribute("valueCd"));
			aDomain.setValueNm((String) aDomainSB.getAttribute("valueNm"));
			aDomain.setValueDs((String) aDomainSB.getAttribute("valueDs"));
			logger.debug("Inserting Domain with valueCd = [" + aDomainSB.getAttribute("valueCd") + "], domainCd = [" + aDomainSB.getAttribute("domainCd") + "] ...");
			aSession.save(aDomain);
		}
		logger.debug("OUT");
	}
	
	private void writeMissingDomains(Session aSession) throws Exception {
		logger.debug("IN");
		SourceBean domainsSB = getConfiguration();
		if (domainsSB == null) {
			throw new Exception("Domains configuration file not found!!!");
		}
		List domainsList = domainsSB.getAttributeAsList("DOMAIN");
		if (domainsList == null || domainsList.isEmpty()) {
			throw new Exception("No predefined domains found!!!");
		}
		
		List alreadyExamined = new ArrayList();
		Iterator it = domainsList.iterator();
		while (it.hasNext()) {
			SourceBean aDomainSB = (SourceBean) it.next();
			if(!alreadyExamined.contains(aDomainSB)){
				
			String domainCd = (String) aDomainSB.getAttribute("domainCd");
			if (domainCd == null || domainCd.equals("")) {
				logger.error("No predefined domains code found!!!");
				throw new Exception("No predefined domains code found!!!");
			}
			//Retrieving all the domains in the DB with the specified domain Code
			logger.debug("Retrieving all the domains in the DB with the specified domain Code");
			String hql = "from SbiDomains where domainCd = '"+domainCd+"'";
			Query hqlQuery = aSession.createQuery(hql);
			List result = hqlQuery.list();
			
			logger.debug("Retrieving all the domains in the XML file with the specified domain Code");
			//Retrieving all the domains in the XML file with the specified domain Code
			List domainsXmlList = domainsSB.getFilteredSourceBeanAttributeAsList("DOMAIN", "domainCd", domainCd);
			
			logger.debug("Retrieving all the domains in the XML file with the specified domain Code");
			//Checking if the domains in the DB are less than the ones in the xml file
			if(result.size() < domainsXmlList.size()){
				//Less domains in the DB than in the XML file, will add new ones
				logger.debug("Less domains in the DB than in the XML file, will add new ones");
				addMissingDomains(aSession,result,domainsXmlList);
			}
			//Removing form the list of XML domains the ones already checked
			logger.debug("Adding to the list of XML domains already checked");
			alreadyExamined.addAll(domainsXmlList);
			}
		}
		logger.debug("OUT");
	}
	
	private void addMissingDomains(Session aSession, List dbDomains, List xmlDomains){
		logger.debug("IN");
		
		Iterator it2 = xmlDomains.iterator();
		while(it2.hasNext()){
			boolean existsInDb = false;
			SourceBean aDomainSB = (SourceBean) it2.next();
			String valueCdXml = (String) aDomainSB.getAttribute("valueCd");
			logger.debug("Retrieved valueCd of XML Domain: "+valueCdXml);
			
			Iterator it = dbDomains.iterator();
			while (it.hasNext()) {
				SbiDomains d = (SbiDomains)it.next();
				String valueCd = d.getValueCd();
				logger.debug("Retrieved valueCd of DB Domain: "+valueCd);
				
				if(valueCdXml.equalsIgnoreCase(valueCd)){
					existsInDb = true;
					logger.debug("Domain already exists in the DB");
					break;
				}				
			}	
			if(!existsInDb){
				logger.debug("Domain doesn't exist in the DB");
				SbiDomains aDomain = new SbiDomains();
				aDomain.setDomainCd((String) aDomainSB.getAttribute("domainCd"));
				aDomain.setDomainNm((String) aDomainSB.getAttribute("domainNm"));
				aDomain.setValueCd((String) aDomainSB.getAttribute("valueCd"));
				aDomain.setValueNm((String) aDomainSB.getAttribute("valueNm"));
				aDomain.setValueDs((String) aDomainSB.getAttribute("valueDs"));
				logger.debug("New Domain ready to be iserted in the DB");
				logger.debug("Inserting Domain with valueCd = [" + aDomainSB.getAttribute("valueCd") + "], domainCd = [" + aDomainSB.getAttribute("domainCd") + "] ...");
				aSession.save(aDomain);
				logger.debug("New Domain iserted in the DB");
			}
		}		
		logger.debug("OUT");
	}

}
