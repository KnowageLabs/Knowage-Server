/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2020 Engineering Ingegneria Informatica S.p.A.
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

/**
 *
 */
package it.eng.spagobi.commons.initializers.metadata;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.analiticalmodel.document.utils.CockpitStatisticsTablesUtils;
import it.eng.spagobi.commons.metadata.SbiCockpitWidget;

/**
 * @author albnale
 *
 */
public class CockpitStatisticsInitializer extends SpagoBIInitializer {
	static private Logger logger = Logger.getLogger(CockpitStatisticsInitializer.class);

	public CockpitStatisticsInitializer() {
		targetComponentName = "CockpitStatistics";
		configurationFileName = null;
	}

	@Override
	public void init(SourceBean config, Session hibernateSession) {

		List sbiCockpitWidgetsList = hibernateSession.createCriteria(SbiCockpitWidget.class).list();
		if (sbiCockpitWidgetsList == null || (sbiCockpitWidgetsList != null && sbiCockpitWidgetsList.isEmpty())) {
			StringBuilder sb = new StringBuilder();
			sb.append("select so from SbiObjects so, SbiEngines se ");
			sb.append(" where so.sbiEngines.engineId = se.engineId");
			sb.append(" and se.name = 'Cockpit Engine'");
			Query c = hibernateSession.createQuery(sb.toString());
			List templates = c.list();
			for (Object objTemplate : templates) {
				SbiObjects sbiObjects = (SbiObjects) objTemplate;

				CockpitStatisticsTablesUtils.parseTemplate(sbiObjects, hibernateSession, true);

			}

		} else {
			logger.info("Table SBI_COCKPIT_WIDGETS is already populated. Initialization is skipped.");
		}

	}

}
