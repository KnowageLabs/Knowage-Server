package it.eng.spagobi.tools.glossary.dao.criterion;

import it.eng.spagobi.commons.dao.Criterion;
import it.eng.spagobi.tools.glossary.metadata.SbiGlWord;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.transform.Transformers;

public class SearchWord implements Criterion<SbiGlWord> {

	@Override
	public Criteria evaluate(Session session) {
		Criteria c = session.createCriteria(SbiGlWord.class);
		c.setProjection(Projections.projectionList().add(Projections.property("wordId"), "wordId").add(Projections.property("word"), "word"))
				.setResultTransformer(Transformers.aliasToBean(SbiGlWord.class));
		return c;
	}

}
