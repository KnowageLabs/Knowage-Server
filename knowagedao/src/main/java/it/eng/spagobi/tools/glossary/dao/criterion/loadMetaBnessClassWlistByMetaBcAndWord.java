package it.eng.spagobi.tools.glossary.dao.criterion;

import it.eng.spagobi.commons.dao.ICriterion;
import it.eng.spagobi.tools.glossary.metadata.SbiGlBnessClsWlist;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class loadMetaBnessClassWlistByMetaBcAndWord implements ICriterion<SbiGlBnessClsWlist> {

	private final Integer metaBcId;
	private final Integer word_id;

	private final String column;

	public loadMetaBnessClassWlistByMetaBcAndWord(Integer metaBcId, Integer word_id, String column) {
		this.word_id = word_id;
		this.metaBcId = metaBcId;
		this.column = column;
	}

	@Override
	public Criteria evaluate(Session session) {
		Criteria c = session.createCriteria(SbiGlBnessClsWlist.class);
		c.add(Restrictions.eq("id.bcId", metaBcId));
		c.add(Restrictions.eq("id.wordId", word_id));
		c.add(Restrictions.eq("id.column_name", column));
		return c;
	}
}
