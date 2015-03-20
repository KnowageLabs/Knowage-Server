/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.functionalitytree.init;

import it.eng.spago.base.SourceBean;
import it.eng.spago.init.InitializerIFace;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.metadata.SbiTenant;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * 
 * This class initialize the functionalities tree using some configuration
 * parameters. The class is called from Spago Framework during the Application
 * Start-Up.
 * 
 */

public class TreeInitializer implements InitializerIFace {

	static private Logger logger = Logger.getLogger(TreeInitializer.class);

	private SourceBean _config;

	/**
	 * Create and initialize all the repositories defined in the configuration
	 * SourceBean, the method is called automatically from Spago Framework at
	 * application start up if the Spago initializers.xml file is configured
	 * 
	 * @param config
	 *            the config
	 */
	public void init(SourceBean config) {
		logger.debug("IN");
		_config = config;

		initialize();
		logger.debug("OUT");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.spago.init.InitializerIFace#getConfig()
	 */
	public SourceBean getConfig() {
		return _config;
	}

	private void initialize() {
		logger.debug("IN");
		try {
			List<SbiTenant> tenants = DAOFactory.getTenantsDAO().loadAllTenants();
			for (SbiTenant tenant : tenants) {
				initialize(tenant, _config);
			}
		} catch (Exception e) {
			logger.error("Error while initializing tree structure", e);
			throw new SpagoBIRuntimeException("Error while initializing tree structure", e);
		}
		logger.debug("OUT");
	}

	public void initialize(SbiTenant tenant, SourceBean config) {
		Assert.assertNotNull(tenant, "Tenant in input cannot be null");
		try {
			ILowFunctionalityDAO functionalityDAO = DAOFactory
					.getLowFunctionalityDAO();
			functionalityDAO.setTenant(tenant.getName());
			List functions = functionalityDAO.loadAllLowFunctionalities(false);
			if (functions != null && functions.size() > 0) {
				logger.debug("Tree already initialized");
			} else {
				List nodes = config
						.getAttributeAsList("TREE_INITIAL_STRUCTURE.NODE");
				Iterator it = nodes.iterator();
				while (it.hasNext()) {
					SourceBean node = (SourceBean) it.next();
					String code = (String) node.getAttribute("code");
					String name = (String) node.getAttribute("name");
					String description = (String) node
							.getAttribute("description");
					String codeType = (String) node.getAttribute("codeType");
					String parentPath = (String) node
							.getAttribute("parentPath");
					LowFunctionality functionality = new LowFunctionality();
					functionality.setCode(code);
					functionality.setName(name);
					functionality.setDescription(description);
					functionality.setCodType(codeType);
					functionality.setPath(parentPath + "/" + code);
					if (parentPath != null && !parentPath.trim().equals("")) {
						// if it is not the root load the id of the parent path
						LowFunctionality parentFunctionality = functionalityDAO
								.loadLowFunctionalityByPath(parentPath, false);
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
					functionalityDAO
							.insertLowFunctionality(functionality, null);
				}
			}
		} catch (Exception e) {
			logger.error("Error while initializing tree structure in tenant " + tenant.getName(), e);
			throw new SpagoBIRuntimeException("Error while initializing tree structure in tenant " + tenant.getName(), e);
		}

	}

}
