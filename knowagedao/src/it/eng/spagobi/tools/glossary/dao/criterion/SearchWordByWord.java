package it.eng.spagobi.tools.glossary.dao.criterion;

import it.eng.spagobi.commons.dao.ICriterion;
import it.eng.spagobi.tools.glossary.metadata.SbiGlWlist;
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
	private final Integer glossary_id;

	public SearchWordByWord(String word,Integer page,Integer item_per_page,Integer glossary_id) {
		this.word = word;
		this.page = page;
		this.item_per_page = item_per_page;
		this.glossary_id=glossary_id;
	}

	@Override
	public Criteria evaluate(Session session) {
		Criteria c;
		
		if(glossary_id==null){
			c = session.createCriteria(SbiGlWord.class,"gl_word");
		}else{
			//filter by glossary
			c = session.createCriteria(SbiGlWlist.class,"wlist");
			c.createAlias("wlist.word", "gl_word");
			c.createAlias("wlist.content", "gl_cont");
			c.add(Restrictions.eq("gl_cont.glossaryId", glossary_id));
		}
		
		
		c.setProjection(Projections.projectionList().add(Projections.property("gl_word.wordId"), "wordId").add(Projections.property("gl_word.word"), "word"))
				.setResultTransformer(Transformers.aliasToBean(SbiGlWord.class));
		if (word != null && !word.isEmpty()) {
			c.add(Restrictions.like("gl_word.word", word, MatchMode.ANYWHERE).ignoreCase());
		}
		if(page!=null && item_per_page!=null ){
			c.setFirstResult((page - 1) * item_per_page);
		     c.setMaxResults(item_per_page);
			}
		c.addOrder(Order.asc("gl_word.word"));
		return c;
	}

}
