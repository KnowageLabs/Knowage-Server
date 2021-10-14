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

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;

import it.eng.spagobi.commons.dao.ICriterion;
import it.eng.spagobi.tools.glossary.metadata.SbiGlContents;

public class SearchContentsByNameAndID implements ICriterion<SbiGlContents> {

	private final String cont;

	private final Integer id;

	public SearchContentsByNameAndID(String cont, Integer id) {
		this.cont = cont;
		this.id = id;
	}

	@Override
	public Criteria evaluate(Session session) {
		Criteria c = session.createCriteria(SbiGlContents.class);
		c.setProjection(Projections.projectionList().add(Projections.property("contentId"), "contentId").add(Projections.property("contentNm"), "contentNm")
				.add(Projections.property("glossaryId"), "glossaryId")).setResultTransformer(Transformers.aliasToBean(SbiGlContents.class));
		if (cont != null && !cont.isEmpty()) {
			c.add(Restrictions.eq("contentNm", cont).ignoreCase());
		}
		if (id != null) {
			c.add(Restrictions.eq("glossaryId", id));
		}
		return c;
	}

}
