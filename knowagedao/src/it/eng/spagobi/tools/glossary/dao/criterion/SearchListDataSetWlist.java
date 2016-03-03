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
import it.eng.spagobi.tools.glossary.metadata.SbiGlDataSetWlist;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class SearchListDataSetWlist implements ICriterion<SbiGlDataSetWlist> {

	private final Integer iddoc;
	private final String  organiz;

	public SearchListDataSetWlist(Integer iddoc,String organiz) {
		this.iddoc = iddoc;
		this.organiz=organiz;
	}

	@Override
	public Criteria evaluate(Session session) {
		Criteria c = session.createCriteria(SbiGlDataSetWlist.class, "dwlist");
		if (iddoc != null) {
			c.createAlias("dwlist.word", "wordWl");
			c.add(Restrictions.eq("dwlist.id.datasetId", iddoc));
			if(organiz!=null){
			c.add(Restrictions.eq("dwlist.id.organization", organiz));
			}
		}
		// c.addOrder(Order.asc("word"));
		return c;
	}

}
