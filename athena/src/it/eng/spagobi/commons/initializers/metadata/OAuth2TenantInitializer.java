package it.eng.spagobi.commons.initializers.metadata;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.metadata.SbiTenant;
//import it.eng.spagobi.security.OAuth2SecurityInfoProvider;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

public class OAuth2TenantInitializer extends SpagoBIInitializer {
	static private Logger logger = Logger.getLogger(OAuth2TenantInitializer.class);

	// It retrieves organizations associated with fi-ware application. If they are not inside the database, it stores them in it
	@Override
	public void init(SourceBean config, Session hibernateSession) {
		logger.debug("IN");
		try {
			String hql = "from SbiTenant";
			Query hqlQuery = hibernateSession.createQuery(hql);
			List<SbiTenant> existingTenants = hqlQuery.list();
			/*
			 * List<String> configuredTenants = OAuth2SecurityInfoProvider.getTenants(); for (String aConfiguredTenant : configuredTenants) { if
			 * (exists(aConfiguredTenant, existingTenants)) { LogMF.debug(logger, "Tenant {0} already exists", aConfiguredTenant); } else { LogMF.info(logger,
			 * "Tenant {0} does not exist. It will be inserted", aConfiguredTenant); writeTenant(aConfiguredTenant, hibernateSession); LogMF.debug(logger,
			 * "Tenant {0} was inserted", aConfiguredTenant); } }
			 */
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
			throw new SpagoBIRuntimeException("An unexpected error occured while initializing Tenants", t);
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

	private void writeTenant(String tenantName, Session hibernateSession) throws Exception {
		logger.debug("IN");
		SbiTenant aTenant = new SbiTenant();
		aTenant.setName(tenantName);
		logger.debug("Inserting tenant with name = [" + tenantName + "]...");
		hibernateSession.save(aTenant);
		logger.debug("OUT");
	}
}
