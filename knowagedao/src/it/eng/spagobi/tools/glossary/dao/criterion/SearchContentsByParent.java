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
package it.eng.spagobi.tools.glossary.dao.criterion;

import it.eng.spagobi.commons.dao.ICriterion;
import it.eng.spagobi.tools.glossary.metadata.SbiGlContents;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class SearchContentsByParent implements ICriterion<SbiGlContents> {
	private final Integer glossaryId;
	private final Integer parentId;

	public SearchContentsByParent(Integer glossaryId, Integer parentId) {
		this.glossaryId = glossaryId;
		this.parentId = parentId;
	}

	@Override
	public Criteria evaluate(Session session) {
		Criteria c = session.createCriteria(SbiGlContents.class);
		if (glossaryId != null)
			c.add(Restrictions.eq("glossary.glossaryId", glossaryId));
		if (parentId != null){
			c.add(Restrictions.eq("parent.contentId", parentId));
		}else{
			c.add(Restrictions.isNull("parent.contentId"));
		}
		c.addOrder((Order.asc("contentNm")));
		return c;
	}

}
