/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.initializers.metadata;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import it.eng.spago.base.SourceBean;
import it.eng.spago.init.InitializerIFace;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.commons.metadata.SbiProductType;
import it.eng.spagobi.commons.metadata.SbiUserFunctionality;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class FunctionalitiesInitializer extends SpagoBIInitializer {

	static private Logger logger = Logger.getLogger(FunctionalitiesInitializer.class);


	public FunctionalitiesInitializer() {
		targetComponentName = "Functionalities";
		configurationFileName = "it/eng/spagobi/commons/initializers/metadata/config/userFunctionalities.xml";
	}
	
	
	public void init(SourceBean config, Session hibernateSession) {
		logger.debug("IN");
		try {
//			String hql = "from SbiUserFunctionality";
//			Query hqlQuery = hibernateSession.createQuery(hql);
//			List userFunctionalities = hqlQuery.list();
//			if (userFunctionalities.isEmpty()) {
//				logger.info("User functionality table is empty. Starting populating predefined User functionalities...");
//				writeUserFunctionalities(hibernateSession);
//			} else {
//				logger.debug("User functionality table is already populated");
//			}
			writeUserFunctionalities(hibernateSession);
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("Ab unexpected error occured while initializeng Functionalities", t);
		} finally {
			logger.debug("OUT");
		}
	}
	
	private void writeUserFunctionalities(Session aSession) throws Exception {
		logger.debug("IN");
		SourceBean userFunctionalitiesSB = getConfiguration();
		
		configurationFileName = "it/eng/spagobi/commons/initializers/metadata/config/roleTypeUserFunctionalities.xml";
		SourceBean roleTypeUserFunctionalitiesSB = getConfiguration();
		
		if (userFunctionalitiesSB == null) {
			throw new Exception("User functionalities configuration file not found!!!");
		}
		if (roleTypeUserFunctionalitiesSB == null) {
			throw new Exception("Role type user functionalities configuration file not found!!!");
		}
		List userFunctionalitiesList = userFunctionalitiesSB.getAttributeAsList("USER_FUNCTIONALITY");
		if (userFunctionalitiesList == null || userFunctionalitiesList.isEmpty()) {
			throw new Exception("No predefined user functionalities found!!!");
		}
		Iterator it = userFunctionalitiesList.iterator();
		while (it.hasNext()) {
			SourceBean aUSerFunctionalitySB = (SourceBean) it.next();
			
			String userFunctionalityName = (String) aUSerFunctionalitySB.getAttribute("name");
			String userFunctionalityProductType = (String) aUSerFunctionalitySB.getAttribute("productType");
			//retrieve productType for his id 
			SbiProductType productType = findProductType(aSession,userFunctionalityProductType);

			
			String hql = "from SbiUserFunctionality f where f.name=? and f.productType = ?";
			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setParameter(0, userFunctionalityName);
			hqlQuery.setParameter(1, productType.getProductTypeId(),Hibernate.INTEGER);
			SbiUserFunctionality aUserFunctionality = (SbiUserFunctionality)hqlQuery.uniqueResult();
			if(aUserFunctionality == null) {
				aUserFunctionality = new SbiUserFunctionality();
				aUserFunctionality.setName(userFunctionalityName);
				aUserFunctionality.setProductType(productType);
				aUserFunctionality.setDescription((String) aUSerFunctionalitySB.getAttribute("description"));
			}
			
			Object roleTypesObject = roleTypeUserFunctionalitiesSB.getFilteredSourceBeanAttribute("ROLE_TYPE_USER_FUNCTIONALITY", "userFunctionality", userFunctionalityName);
			if (roleTypesObject == null) {
				throw new Exception("No role type found for user functionality [" + userFunctionalityName + "] in product type ["+productType+"]!!!");
			}
			StringBuffer roleTypesStrBuffer = new StringBuffer();
			Set roleTypes = new HashSet();
			if(aUserFunctionality.getRoleType() != null) {
				roleTypes.addAll(aUserFunctionality.getRoleType());
			}
			if (roleTypesObject instanceof SourceBean) {
				SourceBean roleTypeSB = (SourceBean) roleTypesObject;
				String roleTypeCd = (String) roleTypeSB.getAttribute("roleType");
				roleTypesStrBuffer.append(roleTypeCd);
				SbiDomains domainRoleType = findDomain(aSession, roleTypeCd, "ROLE_TYPE");
				roleTypes.add(domainRoleType);
			} else if (roleTypesObject instanceof List) {
				List roleTypesSB = (List) roleTypesObject;
				Iterator roleTypesIt = roleTypesSB.iterator();
				while (roleTypesIt.hasNext()) {
					SourceBean roleTypeSB = (SourceBean) roleTypesIt.next();
					String roleTypeCd = (String) roleTypeSB.getAttribute("roleType");
					roleTypesStrBuffer.append(roleTypeCd);
					if (roleTypesIt.hasNext()) {
						roleTypesStrBuffer.append(";");
					}
					SbiDomains domainRoleType = findDomain(aSession, roleTypeCd, "ROLE_TYPE");
					roleTypes.add(domainRoleType);
				}
			}
			aUserFunctionality.setRoleType(roleTypes);

			logger.debug("Inserting UserFunctionality with name = [" + aUSerFunctionalitySB.getAttribute("name") + "] associated to role types [" + roleTypesStrBuffer.toString() + "]...");

			aSession.save(aUserFunctionality);
		}
		logger.debug("OUT");
	}

}
