package it.eng.spagobi.tools.glossary.dao.criterion;

import it.eng.spagobi.commons.dao.ICriterion;
import it.eng.spagobi.tools.glossary.metadata.SbiGlDataSetWlist;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;

public class loadDataSetWlistByDatasetAndWordAndColumn implements ICriterion<SbiGlDataSetWlist> {

	private final Integer datasetId;
	private final Integer word_id;
	private final String column;

	public loadDataSetWlistByDatasetAndWordAndColumn(Integer datasetId,Integer word_id,String column) {
		this.word_id = word_id;
		this.datasetId =datasetId;
		this.column = column;
	}

	@Override
	public Criteria evaluate(Session session) {
		Criteria c = session.createCriteria(SbiGlDataSetWlist.class);
		c.add(Restrictions.eq("id.datasetId", datasetId));
		c.add(Restrictions.eq("id.wordId", word_id));
		c.add(Restrictions.eq("id.column_name", column));
		return c;
	}

}
