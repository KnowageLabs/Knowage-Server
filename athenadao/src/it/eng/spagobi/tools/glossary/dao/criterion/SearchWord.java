package it.eng.spagobi.tools.glossary.dao.criterion;

import it.eng.spagobi.commons.dao.ICriterion;
import it.eng.spagobi.tools.glossary.metadata.SbiGlWord;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.transform.Transformers;

public class SearchWord implements ICriterion<SbiGlWord> {
	
	private final Integer page;
	private final Integer item_per_page;

	public SearchWord(Integer page,Integer item_per_page) {
		this.page = page;
		this.item_per_page = item_per_page;
			}
	
	
	@Override
	public Criteria evaluate(Session session) {
		Criteria c = session.createCriteria(SbiGlWord.class);
		c.setProjection(Projections.projectionList().add(Projections.property("wordId"), "wordId").add(Projections.property("word"), "word"))
				.setResultTransformer(Transformers.aliasToBean(SbiGlWord.class));
		
		if(page!=null && item_per_page!=null ){
		c.setFirstResult((page - 1) * item_per_page);
	     c.setMaxResults(item_per_page);
		}
		
		return c;
	}

}
