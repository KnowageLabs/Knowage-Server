package it.eng.spagobi.tools.glossary.dao.criterion;

import it.eng.spagobi.commons.dao.Criterion;
import it.eng.spagobi.tools.glossary.metadata.SbiGlAttribute;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;

public class SearchAttributeByName implements Criterion<SbiGlAttribute> {

	private final String word;

	public SearchAttributeByName(String name) {
		this.word = name;
	}

	@Override
	public Criteria evaluate(Session session) {
		Criteria c = session.createCriteria(SbiGlAttribute.class);
		c.setProjection(Projections.projectionList().add(Projections.property("attributeId"), "attributeId").add(Projections.property("attributeNm"), "attributeNm"))
				.setResultTransformer(Transformers.aliasToBean(SbiGlAttribute.class));
		if (word != null && !word.isEmpty()) {
			c.add(Restrictions.like("attributeNm", word, MatchMode.ANYWHERE).ignoreCase());
		}
		c.addOrder(Order.asc("attributeNm"));
		return c;
	}

}
