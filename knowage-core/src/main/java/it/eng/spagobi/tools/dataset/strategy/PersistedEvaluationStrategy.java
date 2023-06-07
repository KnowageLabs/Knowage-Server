/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2018 Engineering Ingegneria Informatica S.p.A.
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
 *
 */

package it.eng.spagobi.tools.dataset.strategy;

import java.util.Date;

import org.apache.log4j.Logger;

import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.tools.scheduler.bo.Trigger;

class PersistedEvaluationStrategy extends AbstractJdbcEvaluationStrategy {

	private static final Logger LOGGER = Logger.getLogger(PersistedEvaluationStrategy.class);

	public PersistedEvaluationStrategy(IDataSet dataSet) {
		super(dataSet);
	}

	@Override
	protected String getTableName() {
		return dataSet.getPersistTableName();
	}

	@Override
	protected IDataSource getDataSource() {
		return dataSet.getDataSourceForWriting();
	}

	@Override
	protected Date getDate() {
		Date toReturn = null;

		Trigger trigger = loadTrigger();
		Date previousFireTime = null;
		if (trigger != null) { // dataset is scheduled
			previousFireTime = trigger.getPreviousFireTime();
		}
		if (previousFireTime != null) {
			toReturn = previousFireTime;
		} else { // dataset is not scheduled or no previous fire time available
			toReturn = dataSet.getDateIn();
		}

		return toReturn;
	}

	private Trigger loadTrigger() {
		String triggerGroupName = "DEFAULT";
		String triggerName = "persist_" + dataSet.getName();

		LOGGER.debug("Loading trigger with name [" + triggerName + "] from group [" + triggerGroupName + "]");
		return DAOFactory.getSchedulerDAO().loadTrigger(triggerGroupName, triggerName);
	}
}
