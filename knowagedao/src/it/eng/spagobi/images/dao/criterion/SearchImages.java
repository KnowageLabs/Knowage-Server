package it.eng.spagobi.images.dao.criterion;

import it.eng.spagobi.commons.dao.ICriterion;
import it.eng.spagobi.images.dao.IImagesDAO.Direction;
import it.eng.spagobi.images.dao.IImagesDAO.OrderBy;
import it.eng.spagobi.images.metadata.SbiImages;

import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class SearchImages implements ICriterion<SbiImages> {

	private final String name;
	private final String description;
	private final Map<OrderBy, Direction> sort;

	public SearchImages(String name, String description, Map<OrderBy, Direction> sort) {
		this.name = name;
		this.description = description;
		this.sort = sort;
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
		if (sort != null) {
			for (Entry<OrderBy, Direction> entry : sort.entrySet()) {
				String orderField = "";
				switch (entry.getKey()) {
				case name:
					orderField = "name";
					break;
				case timeIn:
					orderField = "commonInfo.timeIn";
					break;
				}
				c.addOrder(Direction.asc.equals(entry.getValue()) ? Order.asc(orderField) : Order.desc(orderField));
			}
		}
		return c;
	}

}
