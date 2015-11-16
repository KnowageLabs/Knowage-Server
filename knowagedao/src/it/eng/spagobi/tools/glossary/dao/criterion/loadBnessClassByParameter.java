package it.eng.spagobi.tools.glossary.dao.criterion;

import it.eng.spagobi.commons.dao.ICriterion;
import it.eng.spagobi.tools.glossary.metadata.SbiGlBnessCls;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class loadBnessClassByParameter implements ICriterion<SbiGlBnessCls> {

	private final String datamart;
	private final String bnesscls;

	public loadBnessClassByParameter(String datamart, String bnesscls) {
		this.datamart = datamart;
		this.bnesscls = bnesscls;
	}

	@Override
	public Criteria evaluate(Session session) {
		Criteria c = session.createCriteria(SbiGlBnessCls.class);
		c.add(Restrictions.eq("datamart", datamart));
		c.add(Restrictions.eq("unique_identifier", bnesscls));
		return c;
	}

}
