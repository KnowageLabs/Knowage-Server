package it.eng.spagobi.commons.initializers.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.ICategoryDAO;
import it.eng.spagobi.commons.dao.ITenantsDAO;
import it.eng.spagobi.commons.dao.dto.SbiCategory;
import it.eng.spagobi.commons.metadata.SbiTenant;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class CategoriesInitializer extends SpagoBIInitializer {

	private static final Logger logger = LogManager.getLogger(CategoriesInitializer.class);

	private SourceBean configuration = null;

	private final List<SbiTenant> tenants = new ArrayList<>();

	private final Map<String, List<Map<String, String>>> configurationAsMap = new LinkedHashMap<>();

	public CategoriesInitializer() {
		targetComponentName = "Categories";
		configurationFileName = "it/eng/spagobi/commons/initializers/metadata/config/categories.xml";
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

		try {
			readConfiguration();

			for (SbiTenant sbiTenant : tenants) {
				initTenant(sbiTenant);
			}

		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("Ab unexpected error occured while initializeng Domains", t);
		} finally {
			logger.debug("OUT");
		}

	}

	private void readConfiguration() throws Exception {
		configuration = getConfiguration();

		List<SourceBean> categories = configuration.getAttributeAsList("CATEGORY");

		for (SourceBean category : categories) {

			String code = (String) category.getAttribute("code");
			String name = (String) category.getAttribute("name");
			String type = (String) category.getAttribute("type");

			Map<String, String> element = new HashMap<>();

			element.put("code", code);
			element.put("name", name);

			configurationAsMap.putIfAbsent(type, new ArrayList<Map<String, String>>());

			configurationAsMap.get(type).add(element);
		}
	}

	private void initTenant(SbiTenant sbiTenant) throws EMFUserError {

		String tenantName = sbiTenant.getName();
		Tenant tenant = new Tenant(tenantName);

		TenantManager.setTenant(tenant);

		ICategoryDAO categoryDAO = DAOFactory.getCategoryDAO();

		initBusinessModelCategory(categoryDAO);
		initDataSetCategory(categoryDAO);
		initKpiCategory(categoryDAO);
		initKpiTargetCategory(categoryDAO);
		initKpiMeasureCategory(categoryDAO);
		initGeoCategory(categoryDAO);

		TenantManager.unset();
	}

	private void initBusinessModelCategory(ICategoryDAO categoryDAO) throws EMFUserError {
		List<SbiCategory> categoriesForBusinessModel = categoryDAO.getCategoriesForBusinessModel();

		if (categoriesForBusinessModel.isEmpty()) {
			initPerType(categoryDAO, ICategoryDAO.BUSINESS_MODEL_CATEGORY);
		}
	}

	private void initDataSetCategory(ICategoryDAO categoryDAO) throws EMFUserError {
		List<SbiCategory> categoriesForBusinessModel = categoryDAO.getCategoriesForDataset();

		if (categoriesForBusinessModel.isEmpty()) {
			initPerType(categoryDAO, ICategoryDAO.DATASET_CATEGORY);
		}
	}

	private void initKpiCategory(ICategoryDAO categoryDAO) throws EMFUserError {
		List<SbiCategory> categoriesForBusinessModel = categoryDAO.getCategoriesForKpi();

		if (categoriesForBusinessModel.isEmpty()) {
			initPerType(categoryDAO, ICategoryDAO.KPI_CATEGORY);
		}
	}

	private void initKpiTargetCategory(ICategoryDAO categoryDAO) throws EMFUserError {
		List<SbiCategory> categoriesForBusinessModel = categoryDAO.getCategoriesForKpiTarget();

		if (categoriesForBusinessModel.isEmpty()) {
			initPerType(categoryDAO, ICategoryDAO.KPI_TARGET_CATEGORY);
		}
	}

	private void initKpiMeasureCategory(ICategoryDAO categoryDAO) throws EMFUserError {
		List<SbiCategory> categoriesForBusinessModel = categoryDAO.getCategoriesForKpiMeasure();

		if (categoriesForBusinessModel.isEmpty()) {
			initPerType(categoryDAO, ICategoryDAO.KPI_MEASURE_CATEGORY);
		}
	}

	private void initGeoCategory(ICategoryDAO categoryDAO) throws EMFUserError {
		List<SbiCategory> categoriesForBusinessModel = categoryDAO.getCategoriesForGeoReport();

		if (categoriesForBusinessModel.isEmpty()) {
			initPerType(categoryDAO, ICategoryDAO.GEO_CATEGORY);
		}
	}

	private void initPerType(ICategoryDAO categoryDAO, String type) throws EMFUserError {
		List<Map<String, String>> businessModelCategoryList = configurationAsMap.getOrDefault(type, Collections.EMPTY_LIST);

		for (Map<String, String> map : businessModelCategoryList) {

			String code = map.get("code");
			String name = map.get("name");

			categoryDAO.create(code, name, type);
		}
	}

}
