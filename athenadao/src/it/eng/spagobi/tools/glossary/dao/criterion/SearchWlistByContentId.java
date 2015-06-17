package it.eng.spagobi.tools.glossary.dao.criterion;

import it.eng.spagobi.commons.dao.ICriterion;
import it.eng.spagobi.tools.glossary.metadata.SbiGlWlist;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class SearchWlistByContentId implements ICriterion<SbiGlWlist> {

	private final Integer contentId;

	public SearchWlistByContentId(Integer contentId) {
		this.contentId = contentId;
	}

	@Override
	public Criteria evaluate(Session session) {
		Criteria c = session.createCriteria(SbiGlWlist.class);
		c.add(Restrictions.eq("content.contentId", contentId));
		return c;
	}

}
