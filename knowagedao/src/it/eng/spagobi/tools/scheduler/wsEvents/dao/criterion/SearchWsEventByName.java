package it.eng.spagobi.tools.scheduler.wsEvents.dao.criterion;

import it.eng.spagobi.commons.dao.ICriterion;
import it.eng.spagobi.tools.scheduler.wsEvents.SbiWsEvent;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class SearchWsEventByName implements ICriterion<SbiWsEvent> {

	private final String eventName;

	public SearchWsEventByName(String eventName) {
		this.eventName = eventName;
	}

	@Override
	public Criteria evaluate(Session session) {
		Criteria c = session.createCriteria(SbiWsEvent.class);

		if (eventName != null && !eventName.isEmpty()) {
			c.add(Restrictions.eq("eventName", eventName));
		}
		return c;
	}

}
