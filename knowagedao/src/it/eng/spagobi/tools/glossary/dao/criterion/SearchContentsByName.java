package it.eng.spagobi.tools.glossary.dao.criterion;

import it.eng.spagobi.commons.dao.ICriterion;
import it.eng.spagobi.tools.glossary.metadata.SbiGlContents;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;

public class SearchContentsByName implements ICriterion<SbiGlContents> {

	private final String cont;

	public SearchContentsByName(String cont) {
		this.cont = cont;
	}

	@Override
	public Criteria evaluate(Session session) {
		Criteria c = session.createCriteria(SbiGlContents.class);
		c.setProjection(Projections.projectionList().add(Projections.property("contentId"), "contentId").add(Projections.property("contentNm"), "contentNm"))
				.setResultTransformer(Transformers.aliasToBean(SbiGlContents.class));
		if (cont != null && !cont.isEmpty()) {
			c.add(Restrictions.eq("contentNm", cont).ignoreCase());
		}
		return c;
	}

}
