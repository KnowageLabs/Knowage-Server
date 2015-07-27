package it.eng.spagobi.tools.glossary.dao.criterion;

import it.eng.spagobi.commons.dao.ICriterion;
import it.eng.spagobi.tools.udp.metadata.SbiUdpValue;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class SearchWordAttrByWordId implements ICriterion<SbiUdpValue> {

	private final Integer wordId;

	public SearchWordAttrByWordId(Integer wordId) {
		this.wordId = wordId;
	}

	@Override
	public Criteria evaluate(Session session) {
		Criteria c = session.createCriteria(SbiUdpValue.class);
		c.add(Restrictions.eq("referenceId", wordId));
		return c;
	}

}
