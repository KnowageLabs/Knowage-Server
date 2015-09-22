package it.eng.spagobi.tools.events.dao;

import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.tools.events.metadata.SbiEventJob;

import java.util.List;

public interface IEventJobDAO extends ISpagoBIDao {

	public Integer addEvent(SbiEventJob event);

	public void updateEvent(SbiEventJob event);

	public void deleteEvent(Integer event);

	public List<SbiEventJob> listEvent();

	public SbiEventJob loadEvent(Integer id);
}
