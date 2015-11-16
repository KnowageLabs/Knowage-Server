package it.eng.spagobi.tools.glossary.dao.criterion;

import it.eng.spagobi.commons.dao.ICriterion;
import it.eng.spagobi.tools.glossary.metadata.SbiGlBnessClsWlist;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class loadBnessClsWlistFromArray implements ICriterion<SbiGlBnessClsWlist> {

	private final Integer id;
	private final String column;

	public loadBnessClsWlistFromArray(Integer id, String column) {
		this.id = id;
		this.column = column;
	}

	@Override
	public Criteria evaluate(Session session) {
		Criteria c = session.createCriteria(SbiGlBnessClsWlist.class, "bnwl");
		c.createAlias("bnwl.word", "worddass");
		// c.createAlias("bnwl.bness_cls", "bness_clsass");
		c.add(Restrictions.eq("bnwl.bness_cls.bcId", id));
		if (column != null && column.compareTo("null") != 0) {
			c.add(Restrictions.eq("bnwl.column_name", column));
		}
		return c;
	}

}
