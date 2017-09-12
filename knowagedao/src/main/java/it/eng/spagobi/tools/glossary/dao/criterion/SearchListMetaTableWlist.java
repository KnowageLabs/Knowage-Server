package it.eng.spagobi.tools.glossary.dao.criterion;

import it.eng.spagobi.commons.dao.ICriterion;
import it.eng.spagobi.tools.glossary.metadata.SbiGlTableWlist;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class SearchListMetaTableWlist implements ICriterion<SbiGlTableWlist> {

	private final Integer metaTableId;

	public SearchListMetaTableWlist(Integer metaTableId) {
		this.metaTableId = metaTableId;
	}

	@Override
	public Criteria evaluate(Session session) {
		Criteria c = session.createCriteria(SbiGlTableWlist.class, "dwlist");
		if (metaTableId != null) {
			c.createAlias("dwlist.word", "wordWl");
			c.add(Restrictions.eq("dwlist.id.tableId", metaTableId));

		}
		// c.addOrder(Order.asc("word"));
		return c;
	}

}
