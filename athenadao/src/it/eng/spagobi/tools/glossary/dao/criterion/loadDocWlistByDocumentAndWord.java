package it.eng.spagobi.tools.glossary.dao.criterion;

import it.eng.spagobi.commons.dao.ICriterion;
import it.eng.spagobi.tools.glossary.metadata.SbiGlDocWlist;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;

public class loadDocWlistByDocumentAndWord implements ICriterion<SbiGlDocWlist> {

	private final Integer doc_id;
	private final Integer word_id;

	public loadDocWlistByDocumentAndWord(Integer doc_id,Integer word_id) {
		this.word_id = word_id;
		this.doc_id = doc_id;
	}

	@Override
	public Criteria evaluate(Session session) {
		Criteria c = session.createCriteria(SbiGlDocWlist.class);
		c.add(Restrictions.eq("id.documentId", doc_id));
		c.add(Restrictions.eq("id.wordId", word_id));
		return c;
	}

}
