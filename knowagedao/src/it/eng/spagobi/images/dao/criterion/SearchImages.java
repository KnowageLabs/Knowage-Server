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
