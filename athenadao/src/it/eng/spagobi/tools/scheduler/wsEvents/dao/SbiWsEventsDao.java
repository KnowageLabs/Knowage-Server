package it.eng.spagobi.tools.scheduler.wsEvents.dao;

import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.commons.metadata.SbiHibernateModel;
import it.eng.spagobi.tools.scheduler.wsEvents.SbiWsEvent;

import java.util.List;

public interface SbiWsEventsDao extends ISpagoBIDao {

	public List<SbiWsEvent> getWsEventList();

	public SbiWsEvent loadSbiWsEvent(Integer id);

	public SbiWsEvent loadSbiWsEvent(String eventName);

	public Integer triggerEvent(SbiHibernateModel sbiWsEvent);

	public void updateEvent(SbiHibernateModel sbiWsEvent);

}
