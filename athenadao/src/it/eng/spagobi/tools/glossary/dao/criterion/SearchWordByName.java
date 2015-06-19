package it.eng.spagobi.tools.glossary.dao.criterion;

import it.eng.spagobi.commons.dao.ICriterion;
import it.eng.spagobi.tools.glossary.metadata.SbiGlWord;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;

public class SearchWordByName implements ICriterion<SbiGlWord> {

	private final String word;

	public SearchWordByName(String word) {
		this.word = word;
	}

	@Override
	public Criteria evaluate(Session session) {
		Criteria c = session.createCriteria(SbiGlWord.class);
		c.setProjection(Projections.projectionList().add(Projections.property("wordId"), "wordId").add(Projections.property("word"), "word"))
				.setResultTransformer(Transformers.aliasToBean(SbiGlWord.class));
		if (word != null && !word.isEmpty()) {
			c.add(Restrictions.eq("word", word).ignoreCase());
		}
		return c;
	}

}
