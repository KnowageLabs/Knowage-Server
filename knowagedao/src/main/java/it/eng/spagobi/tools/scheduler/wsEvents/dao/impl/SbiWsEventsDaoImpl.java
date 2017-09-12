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
package it.eng.spagobi.tools.scheduler.wsEvents.dao.impl;

import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.metadata.SbiHibernateModel;
import it.eng.spagobi.tools.scheduler.wsEvents.SbiWsEvent;
import it.eng.spagobi.tools.scheduler.wsEvents.dao.SbiWsEventsDao;
import it.eng.spagobi.tools.scheduler.wsEvents.dao.criterion.SearchWsEventByName;

import java.util.List;

import org.apache.log4j.Logger;

public class SbiWsEventsDaoImpl extends AbstractHibernateDAO implements SbiWsEventsDao {
	static private Logger logger = Logger.getLogger(SbiWsEventsDaoImpl.class);

	@Override
	public List<SbiWsEvent> getWsEventList() {
		return list(SbiWsEvent.class);
	}

	@Override
	public SbiWsEvent loadSbiWsEvent(Integer id) {
		return load(SbiWsEvent.class, id);
	}

	@Override
	public List<SbiWsEvent> loadSbiWsEvents(String eventName) {
		return list(new SearchWsEventByName(eventName));
	}

	@Override
	public Integer triggerEvent(SbiHibernateModel sbiWsEvent) {
		return (Integer) insert(sbiWsEvent);
	}

	@Override
	public void updateEvent(SbiHibernateModel sbiWsEvent) {
		update(sbiWsEvent);
	}

}
