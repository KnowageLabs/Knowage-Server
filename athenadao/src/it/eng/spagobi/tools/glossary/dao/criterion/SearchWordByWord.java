package it.eng.spagobi.tools.glossary.dao.criterion;

import it.eng.spagobi.commons.dao.ICriterion;
import it.eng.spagobi.tools.glossary.metadata.SbiGlWord;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;

public class SearchWordByWord implements ICriterion<SbiGlWord> {

	private final String word;
	private final Integer page;
	private final Integer item_per_page;

	public SearchWordByWord(String word,Integer page,Integer item_per_page) {
		this.word = word;
		this.page = page;
		this.item_per_page = item_per_page;
	}

	@Override
	public Criteria evaluate(Session session) {
		Criteria c = session.createCriteria(SbiGlWord.class);
		c.setProjection(Projections.projectionList().add(Projections.property("wordId"), "wordId").add(Projections.property("word"), "word"))
				.setResultTransformer(Transformers.aliasToBean(SbiGlWord.class));
		if (word != null && !word.isEmpty()) {
			c.add(Restrictions.like("word", word, MatchMode.ANYWHERE).ignoreCase());
		}
		if(page!=null && item_per_page!=null ){
			c.setFirstResult((page - 1) * item_per_page);
		     c.setMaxResults(item_per_page);
			}
		c.addOrder(Order.asc("word"));
		return c;
	}

}
