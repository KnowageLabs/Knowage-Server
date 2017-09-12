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
import it.eng.spagobi.tools.alert.metadata.SbiAlertListener;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;

/**
 * @author Salvo Lupo (salvatore.lupo@eng.it)
 * 
 */
public class AlertListenerInitializer extends SpagoBIInitializer {

	private static final String TAG_NAME = "LISTENER";
	static private Logger logger = Logger.getLogger(AlertListenerInitializer.class);

	public AlertListenerInitializer() {
		targetComponentName = "AlertListener";
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
			List<SbiAlertListener> lst = list(SbiAlertListener.class);
			if (lst == null || lst.isEmpty()) {
				logger.info("AlertListener table is empty. Starting populating listeners...");
				writeListeners(session);
			} else {
				logger.debug("AlertListener table is already populated, only missing listeners will be populated");
				addMissingListener(session, lst, loadConfiguration());
			}
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("Ab unexpected error occured while initializeng AlertListener", t);
		} finally {
			logger.debug("OUT");
		}
	}

	private void writeListeners(Session aSession) throws Exception {
		logger.debug("IN");
		List listenerList = loadConfiguration();
		Iterator it = listenerList.iterator();
		while (it.hasNext()) {
			SourceBean aListenerSB = (SourceBean) it.next();
			SbiAlertListener aListener = from(aListenerSB);
			logger.debug("Inserting AlertListener with name = [" + aListenerSB.getAttribute("name") + "], ...");
			insert(aListener);
		}
		logger.debug("OUT");
	}

	private SbiAlertListener from(SourceBean aListenerSB) {
		SbiAlertListener aListener = new SbiAlertListener();
		aListener.setName((String) aListenerSB.getAttribute("name"));
		aListener.setClassName((String) aListenerSB.getAttribute("className"));
		aListener.setTemplate((String) aListenerSB.getAttribute("template"));
		return aListener;
	}

	private List loadConfiguration() throws Exception {
		SourceBean bean = getConfiguration();
		if (bean == null) {
			throw new Exception("AlertListener configuration file not found!!!");
		}
		List listenerList = bean.getAttributeAsList(TAG_NAME);
		if (listenerList == null || listenerList.isEmpty()) {
			throw new Exception("No predefined listeners found!!!");
		}
		return listenerList;
	}

	private void addMissingListener(Session session, List<SbiAlertListener> dbListeners, List xmlListeners) {
		logger.debug("IN");

		Iterator it2 = xmlListeners.iterator();
		while (it2.hasNext()) {
			boolean existsInDb = false;
			SourceBean aListenerSB = (SourceBean) it2.next();
			String nameXml = (String) aListenerSB.getAttribute("name");

			Iterator it = dbListeners.iterator();
			while (it.hasNext()) {
				SbiAlertListener d = (SbiAlertListener) it.next();
				String name = d.getName();
				if (nameXml.equalsIgnoreCase(name)) {
					existsInDb = true;
					break;
				}
			}
			if (!existsInDb) {
				SbiAlertListener aListener = from(aListenerSB);
				insert(aListener);
			}
		}
		logger.debug("OUT");
	}

}
