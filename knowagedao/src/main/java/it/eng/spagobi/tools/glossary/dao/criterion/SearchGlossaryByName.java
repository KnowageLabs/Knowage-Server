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
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;

import it.eng.spagobi.commons.dao.ICriterion;
import it.eng.spagobi.tools.glossary.metadata.SbiGlGlossary;

public class SearchGlossaryByName implements ICriterion<SbiGlGlossary> {

	private String glossary;
	private Integer page;
	private Integer itemsPerPage;

	public SearchGlossaryByName(String glossary) {
		this.glossary = glossary;
	}

	public SearchGlossaryByName(Integer page, Integer itemsPerPage, String glossary) {
		this.page = page;
		this.itemsPerPage = itemsPerPage;
		this.glossary = glossary;
	}

	@Override
	public Criteria evaluate(Session session) {
		Criteria criteria = session.createCriteria(SbiGlGlossary.class);
		criteria.setProjection(
				Projections.projectionList().add(Projections.property("glossaryId"), "glossaryId").add(Projections.property("glossaryNm"), "glossaryNm"))
				.setResultTransformer(Transformers.aliasToBean(SbiGlGlossary.class));
		if (glossary != null && !glossary.isEmpty()) {
			criteria.add(Restrictions.like("glossaryNm", glossary, MatchMode.ANYWHERE).ignoreCase());
		}

		if (page != null && itemsPerPage != null) {
			criteria.setFirstResult((page - 1) * itemsPerPage);
			criteria.setMaxResults(itemsPerPage);
		}

		return criteria;
	}

}
