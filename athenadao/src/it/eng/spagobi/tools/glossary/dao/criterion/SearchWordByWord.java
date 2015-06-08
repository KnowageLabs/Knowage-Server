package it.eng.spagobi.tools.glossary.dao.criterion;

import it.eng.spagobi.commons.dao.Criterion;
import it.eng.spagobi.tools.glossary.metadata.SbiGlWord;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class SearchWordByWord implements Criterion<SbiGlWord> {

	private final String word;

	public SearchWordByWord(String word) {
		this.word = word;
	}

	@Override
	public Criteria evaluate(Session session) {
		Criteria c = session.createCriteria(SbiGlWord.class);
		if (word != null) {
			c.add(Restrictions.like("word", word, MatchMode.ANYWHERE).ignoreCase());
		}
		c.addOrder(Order.asc("word"));
		return c;
	}

}
