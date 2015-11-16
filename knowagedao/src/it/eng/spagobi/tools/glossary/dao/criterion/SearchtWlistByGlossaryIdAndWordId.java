package it.eng.spagobi.tools.glossary.dao.criterion;

import it.eng.spagobi.commons.dao.ICriterion;
import it.eng.spagobi.tools.glossary.metadata.SbiGlWlist;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class SearchtWlistByGlossaryIdAndWordId implements ICriterion<SbiGlWlist> {
	private final Integer glossaryId;
	private final Integer wordId;

	static protected Logger logger = Logger.getLogger(SearchtWlistByGlossaryIdAndWordId.class);

	public SearchtWlistByGlossaryIdAndWordId(Integer glossaryId, Integer wordId) {
		this.glossaryId = glossaryId;
		this.wordId = wordId;
	}

	@Override
	public Criteria evaluate(Session session) {

		if (glossaryId == null || wordId == null) {
			logger.debug("SearchtWlistByGlossaryIdAndWordId, glossaryId or wordId =null");
			return null;
		}

		Criteria c = session.createCriteria(SbiGlWlist.class, "wlist");
		c.createAlias("wlist.content", "contentWl");
		c.createAlias("contentWl.glossary", "glossaryWl");
		c.createAlias("word", "wordWl");
		c.add(Restrictions.eq("glossaryWl.glossaryId", glossaryId));
		c.add(Restrictions.eq("wordWl.wordId", wordId));

		return c;
	}

}