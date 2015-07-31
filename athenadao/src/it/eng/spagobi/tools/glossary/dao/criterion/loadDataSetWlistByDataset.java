package it.eng.spagobi.tools.glossary.dao.criterion;

import it.eng.spagobi.commons.dao.ICriterion;
import it.eng.spagobi.tools.glossary.metadata.SbiGlDataSetWlist;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;

public class loadDataSetWlistByDataset implements ICriterion<SbiGlDataSetWlist> {

	private final Integer datasetId;
	private final String organization;

	public loadDataSetWlistByDataset(Integer datasetId,String organization) {
		this.datasetId =datasetId;
		this.organization = organization;
	}

	@Override
	public Criteria evaluate(Session session) {
		Criteria c = session.createCriteria(SbiGlDataSetWlist.class);
		c.add(Restrictions.eq("id.datasetId", datasetId));
		c.add(Restrictions.eq("id.organization", organization));
		return c;
	}

}
