package it.eng.spagobi.commons.initializers.metadata;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.json.JSONObject;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.ITenantsDAO;
import it.eng.spagobi.commons.metadata.SbiCommonInfo;
import it.eng.spagobi.commons.metadata.SbiDashboardTheme;
import it.eng.spagobi.commons.metadata.SbiTenant;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class DashboardThemesInitializer extends SpagoBIInitializer {

	private static Logger logger = Logger.getLogger(DashboardThemesInitializer.class);
	private final List<SbiTenant> tenants = new ArrayList<>();

	public DashboardThemesInitializer() {
		targetComponentName = "DASHBOARDTHEMES";
		configurationFileName = "it/eng/spagobi/commons/initializers/metadata/config/dashboardThemes.xml";
	}

	/**
	 * @return the tenants
	 */
	public List<SbiTenant> getTenants() {
		return tenants;
	}

	@Override
	public void init(SourceBean config, Session hibernateSession) {
		if (tenants.isEmpty()) {
			ITenantsDAO tenantsDAO = DAOFactory.getTenantsDAO();
			tenants.addAll(tenantsDAO.loadAllTenants());
		}

		for (SbiTenant sbiTenant : tenants) {
			initDashboardTheme(hibernateSession, sbiTenant);
		}

	}

	public void initDashboardTheme(Session hibernateSession, SbiTenant sbiTenant) {
		logger.debug("IN");
		try {
			String hql = "from SbiDashboardTheme l where l.commonInfo.organization = :organization and l.isDefault= :isDefault";
			Query hqlQuery = hibernateSession.createQuery(hql);
			hqlQuery.setString("organization", sbiTenant.getName());
			hqlQuery.setBoolean("isDefault", true);
			List dashboardThemes = hqlQuery.list();
			if (dashboardThemes.isEmpty()) {
				logger.info("DashboardTheme table is empty. Starting populating DashboardThemas...");
				writeDefaultDashboardTheme(hibernateSession, sbiTenant.getName());
			} else {
				logger.debug("DashboardTheme table is already populated. No operations needed.");
			}
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("An unexpected error occured while initializing DashboardTheme", t);
		} finally {
			logger.debug("OUT");
		}

	}

	private void writeDefaultDashboardTheme(Session hibernateSession, String tenant) throws Exception {
		logger.debug("IN");
		SourceBean dashboardThemesSB = getConfiguration();
		if (dashboardThemesSB == null) {
			logger.info("Configuration file for predefined DashboardThemes not found");
			return;
		}
		List dashboardThemeList = dashboardThemesSB.getAttributeAsList("THEME");
		if (dashboardThemeList == null || dashboardThemeList.isEmpty()) {
			logger.info("No predefined dashboardThemes available from configuration file");
			return;
		}
		Iterator it = dashboardThemeList.iterator();
		while (it.hasNext()) {

				SourceBean aDashboardThemeSB = (SourceBean) it.next();
				SbiDashboardTheme aSbiDashboardTheme = new SbiDashboardTheme();
				aSbiDashboardTheme.setThemeName((String) aDashboardThemeSB.getAttribute("name"));
				aSbiDashboardTheme.setIsDefault(true);

				String defaultThema = (String) aDashboardThemeSB.getAttribute("theme");
				JSONObject defaultThemaDecode = new JSONObject(new String(Base64.getDecoder().decode(defaultThema)));
				aSbiDashboardTheme.setConfig(defaultThemaDecode);

				SbiCommonInfo sbiCommonInfo = new SbiCommonInfo();
				sbiCommonInfo.setOrganization(tenant);
				aSbiDashboardTheme.setCommonInfo(sbiCommonInfo);

				logger.debug("Inserting DashboardTheme with name = [" + aDashboardThemeSB.getAttribute("name") + "] ...");

				hibernateSession.save(aSbiDashboardTheme);


		}
		logger.debug("OUT");

	}

}
