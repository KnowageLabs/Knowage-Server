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
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spagobi.commons.metadata.SbiCommonInfo;
import it.eng.spagobi.commons.metadata.SbiOrganizationProductType;
import it.eng.spagobi.commons.metadata.SbiOrganizationProductTypeId;
import it.eng.spagobi.commons.metadata.SbiProductType;
import it.eng.spagobi.commons.metadata.SbiTenant;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class TenantsInitializer extends SpagoBIInitializer {

	private static String TENANTS_CONFIG_TAG_NAME = "TENANTS";
	private static String TENANT_CONFIG_TAG_NAME = "TENANT";
	private static String TENANT_CONFIG_NAME_ATTRIBUTE = "name";

	@Override
	SourceBean getConfiguration() throws Exception {
		SourceBean config = (SourceBean) ConfigSingleton.getInstance().getAttribute(TENANTS_CONFIG_TAG_NAME);
		if (config == null) {
			throw new Exception("Tenants configuration not found!!!");
		}
		return config;
	}

	static private Logger logger = Logger.getLogger(TenantsInitializer.class);

	public TenantsInitializer() {
		targetComponentName = "Tenants";
	}

	@Override
	public void init(SourceBean config, Session hibernateSession) {
		logger.debug("IN");
		try {
			String hql = "from SbiTenant";
			Query hqlQuery = hibernateSession.createQuery(hql);
			List<SbiTenant> existingTenants = hqlQuery.list();
			List<String> configuredTenants = getConfiguredTenants();
			for (String aConfiguredTenant : configuredTenants) {
				if (exists(aConfiguredTenant, existingTenants)) {
					LogMF.debug(logger, "Tenant {0} already exists", aConfiguredTenant);
					writeMissingTenantProductType(aConfiguredTenant, hibernateSession);
				} else {
					LogMF.info(logger, "Tenant {0} does not exist. It will be inserted", aConfiguredTenant);
					writeTenant(aConfiguredTenant, hibernateSession);
					writeTenantProductType(aConfiguredTenant, hibernateSession);
					LogMF.debug(logger, "Tenant {0} was inserted", aConfiguredTenant);
				}
			}
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("Ab unexpected error occured while initializeng Tenants", t);
		} finally {
			logger.debug("OUT");
		}
	}

	private boolean exists(String aConfiguredTenant, List<SbiTenant> existingTenants) {
		for (SbiTenant aTenant : existingTenants) {
			if (aTenant.getName().equals(aConfiguredTenant)) {
				return true;
			}
		}
		return false;
	}

	private List<String> getConfiguredTenants() throws Exception {
		logger.debug("IN");
		List<String> toReturn = new ArrayList<String>();
		SourceBean configuration = this.getConfiguration();
		List tenantsSB = configuration.getAttributeAsList(TENANT_CONFIG_TAG_NAME);
		if (tenantsSB == null || tenantsSB.isEmpty()) {
			throw new Exception("No configured tenants found!!!");
		}
		Iterator it = tenantsSB.iterator();
		while (it.hasNext()) {
			SourceBean aTenantSB = (SourceBean) it.next();
			String name = (String) aTenantSB.getAttribute(TENANT_CONFIG_NAME_ATTRIBUTE);
			LogMF.debug(logger, "Found configured tenant: [{0}]", name);
			toReturn.add(name);
		}
		logger.debug("OUT");
		return toReturn;
	}

	private void writeTenant(String tenantName, Session hibernateSession) throws Exception {
		logger.debug("IN");
		SbiTenant aTenant = new SbiTenant();
		aTenant.setName(tenantName);
		logger.debug("Inserting tenant with name = [" + tenantName + "]...");
		hibernateSession.save(aTenant);
		logger.debug("OUT");
	}

	private void writeTenantProductType(String tenantName, Session hibernateSession) throws Exception {
		SourceBean configuration = this.getConfiguration();
		Object tenantObject = configuration.getFilteredSourceBeanAttribute(TENANT_CONFIG_TAG_NAME, "name", tenantName);

		if (tenantObject == null) {
			throw new Exception("Tenant [" + tenantName + "] configuration not found!!!");
		} else {
			SourceBean tenantObjectSB = (SourceBean) tenantObject;
			List tenantProducts = tenantObjectSB.getAttributeAsList("PRODUCT");
			Iterator it = tenantProducts.iterator();
			while (it.hasNext()) {
				SourceBean aTenantProductSB = (SourceBean) it.next();
				String isActive = (String) aTenantProductSB.getAttribute("active");
				if (isActive != null && isActive.equalsIgnoreCase("true")) {
					String productTypeName = (String) aTenantProductSB.getAttribute("name");
					LogMF.debug(logger, "Found configured tenant product: [{0}]", productTypeName);

					// /create association tenant to product type
					SbiTenant aTenant = findTenant(hibernateSession, tenantName);
					SbiProductType aProductType = findProductType(hibernateSession, productTypeName);
					if (aProductType != null) {
						SbiOrganizationProductType association = new SbiOrganizationProductType();
						association.setSbiProductType(aProductType);
						association.setSbiOrganizations(aTenant);
						SbiCommonInfo commonInfo = new SbiCommonInfo();
						commonInfo.setUserIn("server");
						commonInfo.setTimeIn(new Date());
						commonInfo.setOrganization(tenantName);

						association.setCommonInfo(commonInfo);

						SbiOrganizationProductTypeId id = new SbiOrganizationProductTypeId();
						id.setProductTypeId(aProductType.getProductTypeId());
						id.setOrganizationId(aTenant.getId());
						association.setId(id);

						hibernateSession.save(association);
					}
				}
			}
		}
	}

	private void writeMissingTenantProductType(String tenantName, Session hibernateSession) throws Exception {
		SourceBean configuration = this.getConfiguration();
		Object tenantObject = configuration.getFilteredSourceBeanAttribute(TENANT_CONFIG_TAG_NAME, "name", tenantName);

		if (tenantObject == null) {
			throw new Exception("Tenant [" + tenantName + "] configuration not found!!!");
		} else {
			SourceBean tenantObjectSB = (SourceBean) tenantObject;
			List tenantProducts = tenantObjectSB.getAttributeAsList("PRODUCT");
			Iterator it = tenantProducts.iterator();
			while (it.hasNext()) {
				SourceBean aTenantProductSB = (SourceBean) it.next();
				String isActive = (String) aTenantProductSB.getAttribute("active");
				if (isActive != null && isActive.equalsIgnoreCase("true")) {
					String productTypeName = (String) aTenantProductSB.getAttribute("name");
					LogMF.debug(logger, "Found configured tenant product: [{0}]", productTypeName);

					// /create association tenant to product type
					SbiTenant aTenant = findTenant(hibernateSession, tenantName);
					SbiProductType aProductType = findProductType(hibernateSession, productTypeName);
					if (aProductType != null) {
						SbiOrganizationProductType association = findOrganizationProductType(hibernateSession, tenantName, productTypeName);
						if (association == null) {
							association = new SbiOrganizationProductType();
							association.setSbiProductType(aProductType);
							association.setSbiOrganizations(aTenant);
							SbiCommonInfo commonInfo = new SbiCommonInfo();
							commonInfo.setUserIn("server");
							commonInfo.setTimeIn(new Date());
							commonInfo.setOrganization(tenantName);

							association.setCommonInfo(commonInfo);

							SbiOrganizationProductTypeId id = new SbiOrganizationProductTypeId();
							id.setProductTypeId(aProductType.getProductTypeId());
							id.setOrganizationId(aTenant.getId());
							association.setId(id);

							hibernateSession.save(association);
						}
					}
				}
			}
		}
	}

}
