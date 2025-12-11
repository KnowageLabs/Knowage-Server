package it.eng.spagobi.commons.initializers.metadata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.ITenantsDAO;
import it.eng.spagobi.commons.metadata.SbiTenant;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class TreeInitializer extends SpagoBIInitializer {

	private static final Logger logger = LogManager.getLogger(TreeInitializer.class);

	private final List<SbiTenant> tenants = new ArrayList<>();

	public TreeInitializer() {
		targetComponentName = "Tree";
		configurationFileName = "it/eng/spagobi/commons/initializers/metadata/config/tree.xml";
	}

	public List<SbiTenant> getTenants() {
		return tenants;
	}

	@Override
	public void init(SourceBean config, Session hibernateSession) {
		logger.debug("IN");
		try {
			if (tenants.isEmpty()) {
				ITenantsDAO tenantsDAO = DAOFactory.getTenantsDAO();
				tenants.addAll(tenantsDAO.loadAllTenants());
			}

			for (SbiTenant tenant : tenants) {
				initialize(tenant);
			}
		} catch (Exception e) {
			logger.error("Error while initializing tree structure", e);
			throw new SpagoBIRuntimeException("Error while initializing tree structure", e);
		}
		logger.debug("OUT");

	}

	private void initialize(SbiTenant tenant) {
		Assert.assertNotNull(tenant, "Tenant in input cannot be null");
		try {
			ILowFunctionalityDAO functionalityDAO = DAOFactory.getLowFunctionalityDAO();
			functionalityDAO.setTenant(tenant.getName());
			List functions = functionalityDAO.loadAllLowFunctionalities(false);
			if (functions != null && functions.size() > 0) {
				logger.debug("Tree already initialized");
			} else {
				SourceBean configuration = getConfiguration();
				List nodes = configuration.getAttributeAsList("TREE_INITIAL_STRUCTURE.NODE");
				Iterator it = nodes.iterator();
				while (it.hasNext()) {
					SourceBean node = (SourceBean) it.next();
					String code = (String) node.getAttribute("code");
					String name = (String) node.getAttribute("name");
					String description = (String) node.getAttribute("description");
					String codeType = (String) node.getAttribute("codeType");
					String parentPath = (String) node.getAttribute("parentPath");
					LowFunctionality functionality = new LowFunctionality();
					functionality.setCode(code);
					functionality.setName(name);
					functionality.setDescription(description);
					functionality.setCodType(codeType);
					functionality.setPath(parentPath + "/" + code);
					if (parentPath != null && !parentPath.trim().equals("")) {
						// if it is not the root load the id of the parent path
						LowFunctionality parentFunctionality = functionalityDAO.loadLowFunctionalityByPath(parentPath, false);
						functionality.setParentId(parentFunctionality.getId());
					} else {
						// if it is the root the parent path id is set to null
						functionality.setParentId(null);
					}
					// sets no permissions
					functionality.setDevRoles(new Role[0]);
					functionality.setExecRoles(new Role[0]);
					functionality.setTestRoles(new Role[0]);
					functionality.setCreateRoles(new Role[0]);
					functionalityDAO.insertLowFunctionality(functionality, null);
				}
			}
		} catch (Exception e) {
			logger.error("Error while initializing tree structure in tenant " + tenant.getName(), e);
			throw new SpagoBIRuntimeException("Error while initializing tree structure in tenant " + tenant.getName(), e);
		}

	}

}
