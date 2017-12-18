package it.eng.spagobi.tools.glossary.dao.criterion;

import it.eng.spagobi.commons.dao.ICriterion;
import it.eng.spagobi.tools.glossary.metadata.SbiGlBnessClsWlist;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class SearchListMetaBnessClsWlist implements ICriterion<SbiGlBnessClsWlist> {

	private final Integer metaBnessClsId;

	public SearchListMetaBnessClsWlist(Integer metaBnessClsId) {
		this.metaBnessClsId = metaBnessClsId;
	}

	@Override
	public Criteria evaluate(Session session) {
		Criteria c = session.createCriteria(SbiGlBnessClsWlist.class, "dwlist");
		if (metaBnessClsId != null) {
			c.createAlias("dwlist.word", "wordWl");
			c.add(Restrictions.eq("dwlist.id.bcId", metaBnessClsId));

		}
		// c.addOrder(Order.asc("word"));
		return c;
	}

}
