package it.eng.spagobi.images.dao.criterion;

import it.eng.spagobi.commons.dao.ICriterion;
import it.eng.spagobi.images.metadata.SbiImages;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

public class SearchImages implements ICriterion<SbiImages> {

	private final String name;
	private final String description;

	public SearchImages(String name, String description) {
		this.name = name;
		this.description = description;
	}

	@Override
	public Criteria evaluate(Session session) {
		Criteria c = session.createCriteria(SbiImages.class);
		if (name != null && !name.isEmpty()) {
			c.add(Restrictions.like("name", name, MatchMode.ANYWHERE).ignoreCase());
		}
		if (description != null && !description.isEmpty()) {
			c.add(Restrictions.like("description", description, MatchMode.ANYWHERE).ignoreCase());
		}
		return c;
	}

}
