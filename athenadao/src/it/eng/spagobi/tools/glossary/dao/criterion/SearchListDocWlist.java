package it.eng.spagobi.tools.glossary.dao.criterion;

import it.eng.spagobi.commons.dao.ICriterion;
import it.eng.spagobi.tools.glossary.metadata.SbiGlDocWlist;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class SearchListDocWlist implements ICriterion<SbiGlDocWlist> {

	private final Integer iddoc;

	public SearchListDocWlist(Integer iddoc) {
		this.iddoc = iddoc;
	}

	@Override
	public Criteria evaluate(Session session) {
		Criteria c = session.createCriteria(SbiGlDocWlist.class,"dwlist");
		if (iddoc != null) {
			c.createAlias("dwlist.word", "wordWl");
			c.add(Restrictions.eq("id.documentId", iddoc));
		}
//		c.addOrder(Order.asc("word"));
		return c;
	}

}
