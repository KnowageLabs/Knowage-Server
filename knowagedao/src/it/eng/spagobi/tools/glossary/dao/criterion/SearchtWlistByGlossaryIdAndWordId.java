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
import it.eng.spagobi.tools.glossary.metadata.SbiGlWlist;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class SearchtWlistByGlossaryIdAndWordId implements ICriterion<SbiGlWlist> {
	private final Integer glossaryId;
	private final Integer wordId;

	static protected Logger logger = Logger.getLogger(SearchtWlistByGlossaryIdAndWordId.class);

	public SearchtWlistByGlossaryIdAndWordId(Integer glossaryId, Integer wordId) {
		this.glossaryId = glossaryId;
		this.wordId = wordId;
	}

	@Override
	public Criteria evaluate(Session session) {

		if (glossaryId == null || wordId == null) {
			logger.debug("SearchtWlistByGlossaryIdAndWordId, glossaryId or wordId =null");
			return null;
		}

		Criteria c = session.createCriteria(SbiGlWlist.class, "wlist");
		c.createAlias("wlist.content", "contentWl");
		c.createAlias("contentWl.glossary", "glossaryWl");
		c.createAlias("word", "wordWl");
		c.add(Restrictions.eq("glossaryWl.glossaryId", glossaryId));
		c.add(Restrictions.eq("wordWl.wordId", wordId));

		return c;
	}

}