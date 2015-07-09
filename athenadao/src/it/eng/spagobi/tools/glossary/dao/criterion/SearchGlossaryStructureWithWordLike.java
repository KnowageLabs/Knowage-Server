package it.eng.spagobi.tools.glossary.dao.criterion;

import it.eng.spagobi.commons.dao.ICriterion;
import it.eng.spagobi.tools.glossary.metadata.SbiGlWlist;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

public class SearchGlossaryStructureWithWordLike implements ICriterion<SbiGlWlist> {
	private final String glossaryId;
	private final String word;

	public SearchGlossaryStructureWithWordLike(String glossaryId, String word) {
		this.glossaryId = glossaryId;
		this.word = word;
	}

	@Override
	public Criteria evaluate(Session session) {
		
		if(glossaryId ==null || word==null ){
			System.out.println("SearchGlossaryStructureWithWordLike, glossaryId or word =null");
		return null;
		}
		
		Criteria c = session.createCriteria(SbiGlWlist.class,"wlist");
		c.createAlias("wlist.content", "contentWl"); 
		c.createAlias("contentWl.glossary", "glossaryWl"); 
		c.createAlias("word", "wordWl");
//		c.createAlias("contentWl.parent", "parent"); // get parent info
		c.add(Restrictions.eq("glossaryWl.glossaryId", Integer.parseInt(glossaryId)));
		c.add(Restrictions.like("wordWl.word",  word, MatchMode.ANYWHERE).ignoreCase());
		
		return c;
	}

}