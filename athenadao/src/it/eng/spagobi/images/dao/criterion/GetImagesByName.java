package it.eng.spagobi.images.dao.criterion;

import it.eng.spagobi.commons.dao.ICriterion;
import it.eng.spagobi.images.metadata.SbiImages;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class GetImagesByName implements ICriterion<SbiImages> {

	private final String name;

	public GetImagesByName(String name) {
		this.name = name;
	}

	@Override
	public Criteria evaluate(Session session) {
		Criteria c = session.createCriteria(SbiImages.class);
		c.add(Restrictions.eq("name", name));
		return c;
	}

}
