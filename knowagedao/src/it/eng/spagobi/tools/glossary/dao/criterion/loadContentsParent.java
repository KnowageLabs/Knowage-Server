package it.eng.spagobi.tools.glossary.dao.criterion;

import it.eng.spagobi.commons.dao.ICriterion;
import it.eng.spagobi.tools.glossary.metadata.SbiGlContents;
import it.eng.spagobi.tools.glossary.metadata.SbiGlWlist;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;

public class loadContentsParent implements ICriterion<SbiGlContents> {

	private final Integer cont_id;

	public loadContentsParent(Integer cont_id) {
		this.cont_id = cont_id;
	}

	@Override
	public Criteria evaluate(Session session) {
		Criteria c = session.createCriteria(SbiGlContents.class,"cont");
//		Criteria c = session.createCriteria(SbiGlWlist.class,"wlist");
		c.createAlias("cont.parent", "par"); 
		c.add(Restrictions.eq("cont.contentId", cont_id));
	
		return c;
	}

}
