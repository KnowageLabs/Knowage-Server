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
	public SbiWsEvent loadSbiWsEvent(String eventName) {
		return list(new SearchWsEventByName(eventName)).get(0);
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
