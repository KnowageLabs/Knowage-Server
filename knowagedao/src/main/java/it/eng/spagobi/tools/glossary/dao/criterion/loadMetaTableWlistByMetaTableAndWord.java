package it.eng.spagobi.tools.glossary.dao.criterion;

import it.eng.spagobi.commons.dao.ICriterion;
import it.eng.spagobi.tools.glossary.metadata.SbiGlTableWlist;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class loadMetaTableWlistByMetaTableAndWord implements ICriterion<SbiGlTableWlist> {

	private final Integer metaTableId;
	private final Integer word_id;

	private final String column;

	public loadMetaTableWlistByMetaTableAndWord(Integer metaTableId, Integer word_id, String column) {
		this.word_id = word_id;
		this.metaTableId = metaTableId;
		this.column = column;
	}

	@Override
	public Criteria evaluate(Session session) {
		Criteria c = session.createCriteria(SbiGlTableWlist.class);
		c.add(Restrictions.eq("id.tableId", metaTableId));
		c.add(Restrictions.eq("id.wordId", word_id));
		c.add(Restrictions.eq("id.column_name", column));
		return c;
	}
}
