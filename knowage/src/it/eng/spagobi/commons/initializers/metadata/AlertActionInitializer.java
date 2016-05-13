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
import it.eng.spagobi.commons.metadata.SbiTenant;
import it.eng.spagobi.tools.alert.metadata.SbiAlertAction;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;

/**
 * @author Salvo Lupo (salvatore.lupo@eng.it)
 * 
 */
public class AlertActionInitializer extends SpagoBIInitializer {

	private static final String TAG_NAME = "ACTION";
	static private Logger logger = Logger.getLogger(AlertActionInitializer.class);

	public AlertActionInitializer() {
		targetComponentName = "AlertAction";
		configurationFileName = "it/eng/spagobi/commons/initializers/metadata/config/alert.xml";
	}

	@SuppressWarnings("unchecked")
	@Override
	public void init(SourceBean config, Session session) {
		List<SbiTenant> tenants = session.createCriteria(SbiTenant.class).list();
		for (SbiTenant tenant : tenants) {
			setTenant(tenant.getName());
			init(session);
		}
	}

	@SuppressWarnings("unchecked")
	public void init(Session session) {
		logger.debug("IN");
		try {
			List<SbiAlertAction> lst = list(SbiAlertAction.class);
			if (lst == null || lst.isEmpty()) {
				logger.info("AlertAction table is empty. Starting populating actions...");
				writeActions(session);
			} else {
				logger.debug("AlertAction table is already populated, only missing actions will be populated");
				addMissingAction(session, lst, loadConfiguration());
			}
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("Ab unexpected error occured while initializeng AlertAction", t);
		} finally {
			logger.debug("OUT");
		}
	}

	private void writeActions(Session aSession) throws Exception {
		logger.debug("IN");
		List actionList = loadConfiguration();
		Iterator it = actionList.iterator();
		while (it.hasNext()) {
			SourceBean aActionSB = (SourceBean) it.next();
			SbiAlertAction aAction = from(aActionSB);
			logger.debug("Inserting AlertAction with name = [" + aActionSB.getAttribute("name") + "], ...");
			insert(aAction);
		}
		logger.debug("OUT");
	}

	private SbiAlertAction from(SourceBean aActionSB) {
		SbiAlertAction aAction = new SbiAlertAction();
		aAction.setName((String) aActionSB.getAttribute("name"));
		aAction.setClassName((String) aActionSB.getAttribute("className"));
		aAction.setTemplate((String) aActionSB.getAttribute("template"));
		return aAction;
	}

	private List loadConfiguration() throws Exception {
		SourceBean bean = getConfiguration();
		if (bean == null) {
			throw new Exception("AlertAction configuration file not found!!!");
		}
		List actionList = bean.getAttributeAsList(TAG_NAME);
		if (actionList == null || actionList.isEmpty()) {
			throw new Exception("No predefined actions found!!!");
		}
		return actionList;
	}

	private void addMissingAction(Session session, List<SbiAlertAction> dbActions, List xmlActions) {
		logger.debug("IN");

		Iterator it2 = xmlActions.iterator();
		while (it2.hasNext()) {
			boolean existsInDb = false;
			SourceBean aActionSB = (SourceBean) it2.next();
			String nameXml = (String) aActionSB.getAttribute("name");

			Iterator it = dbActions.iterator();
			while (it.hasNext()) {
				SbiAlertAction d = (SbiAlertAction) it.next();
				String name = d.getName();
				if (nameXml.equalsIgnoreCase(name)) {
					existsInDb = true;
					break;
				}
			}
			if (!existsInDb) {
				SbiAlertAction aAction = from(aActionSB);
				insert(aAction);
			}
		}
		logger.debug("OUT");
	}

}
