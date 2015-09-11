package it.eng.spagobi.tools.glossary.dao.criterion;

import it.eng.spagobi.commons.dao.ICriterion;
import it.eng.spagobi.tools.glossary.metadata.SbiGlDataSetWlist;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class SearchListDataSetWlist implements ICriterion<SbiGlDataSetWlist> {

	private final Integer iddoc;
	private final String  organiz;

	public SearchListDataSetWlist(Integer iddoc,String organiz) {
		this.iddoc = iddoc;
		this.organiz=organiz;
	}

	@Override
	public Criteria evaluate(Session session) {
		Criteria c = session.createCriteria(SbiGlDataSetWlist.class, "dwlist");
		if (iddoc != null) {
			c.createAlias("dwlist.word", "wordWl");
			c.add(Restrictions.eq("dwlist.id.datasetId", iddoc));
			if(organiz!=null){
			c.add(Restrictions.eq("dwlist.id.organization", organiz));
			}
		}
		// c.addOrder(Order.asc("word"));
		return c;
	}

}
