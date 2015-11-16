package it.eng.spagobi.tools.glossary.dao.criterion;

import it.eng.spagobi.commons.dao.ICriterion;
import it.eng.spagobi.tools.glossary.metadata.SbiGlGlossary;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;

public class SearchGlossaryByName implements ICriterion<SbiGlGlossary> {

	private final String gloss;

	public SearchGlossaryByName(String gloss) {
		this.gloss = gloss;
	}

	@Override
	public Criteria evaluate(Session session) {
		Criteria c = session.createCriteria(SbiGlGlossary.class);
		c.setProjection(Projections.projectionList().add(Projections.property("glossaryId"), "glossaryId").add(Projections.property("glossaryNm"), "glossaryNm"))
				.setResultTransformer(Transformers.aliasToBean(SbiGlGlossary.class));
		if (gloss != null && !gloss.isEmpty()) {
			c.add(Restrictions.eq("glossaryNm", gloss).ignoreCase());
		}
		return c;
	}

}
