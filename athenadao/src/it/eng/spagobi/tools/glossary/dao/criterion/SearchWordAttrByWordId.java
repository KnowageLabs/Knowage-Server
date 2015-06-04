package it.eng.spagobi.tools.glossary.dao.criterion;

import it.eng.spagobi.commons.dao.Criterion;
import it.eng.spagobi.tools.glossary.metadata.SbiGlWordAttr;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class SearchWordAttrByWordId implements Criterion<SbiGlWordAttr> {

	private final Integer wordId;

	public SearchWordAttrByWordId(Integer wordId) {
		this.wordId = wordId;
	}

	@Override
	public Criteria evaluete(Session session) {
		Criteria c = session.createCriteria(SbiGlWordAttr.class);
		c.add(Restrictions.eq("word.wordId", wordId));
		return c;
	}

}
