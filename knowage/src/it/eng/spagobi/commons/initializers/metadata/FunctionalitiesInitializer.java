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
package it.eng.spagobi.commons.initializers.metadata;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.commons.metadata.SbiProductType;
import it.eng.spagobi.commons.metadata.SbiUserFunctionality;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;

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

	@Override
	public void init(SourceBean config, Session hibernateSession) {
		logger.debug("IN");
		try {
			// String hql = "from SbiUserFunctionality";
			// Query hqlQuery = hibernateSession.createQuery(hql);
			// List userFunctionalities = hqlQuery.list();
			// if (userFunctionalities.isEmpty()) {
			// logger.info("User functionality table is empty. Starting populating predefined User functionalities...");
			// writeUserFunctionalities(hibernateSession);
			// } else {
			// logger.debug("User functionality table is already populated");
			// }
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
			// retrieve productType for his id
			SbiProductType productType = findProductType(aSession, userFunctionalityProductType);

			String hql = "from SbiUserFunctionality f where f.name=? and f.productType = ?";
			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setParameter(0, userFunctionalityName);
			hqlQuery.setParameter(1, productType.getProductTypeId(), Hibernate.INTEGER);
			SbiUserFunctionality aUserFunctionality = (SbiUserFunctionality) hqlQuery.uniqueResult();
			if (aUserFunctionality == null) {
				aUserFunctionality = new SbiUserFunctionality();
				aUserFunctionality.setName(userFunctionalityName);
				aUserFunctionality.setProductType(productType);
				aUserFunctionality.setDescription((String) aUSerFunctionalitySB.getAttribute("description"));
			}

			Object roleTypesObject = roleTypeUserFunctionalitiesSB.getFilteredSourceBeanAttribute("ROLE_TYPE_USER_FUNCTIONALITY", "userFunctionality",
					userFunctionalityName);
			if (roleTypesObject == null) {
				throw new Exception("No role type found for user functionality [" + userFunctionalityName + "] in product type [" + productType.getLabel()
						+ "]!!!");
			}
			StringBuffer roleTypesStrBuffer = new StringBuffer();
			Set roleTypes = new HashSet();
			if (aUserFunctionality.getRoleType() != null) {
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

			logger.debug("Inserting UserFunctionality with name = [" + aUSerFunctionalitySB.getAttribute("name") + "] associated to role types ["
					+ roleTypesStrBuffer.toString() + "]...");

			aSession.save(aUserFunctionality);
		}
		logger.debug("OUT");
	}

}
