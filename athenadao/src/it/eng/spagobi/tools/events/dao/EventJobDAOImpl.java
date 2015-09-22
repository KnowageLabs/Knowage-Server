package it.eng.spagobi.tools.events.dao;

import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.tools.events.metadata.SbiEventJob;

import java.util.List;

public class EventJobDAOImpl extends AbstractHibernateDAO implements IEventJobDAO {

	@Override
	public Integer addEvent(SbiEventJob event) {
		return (Integer) insert(event);
	}

	@Override
	public void updateEvent(SbiEventJob event) {
		update(event);
	}

	@Override
	public List<SbiEventJob> listEvent() {
		return list(SbiEventJob.class);
	}

	@Override
	public SbiEventJob loadEvent(Integer eventId) {
		return load(SbiEventJob.class, eventId);
	}

	@Override
	public void deleteEvent(Integer id) {
		delete(SbiEventJob.class, id);
	}

}
