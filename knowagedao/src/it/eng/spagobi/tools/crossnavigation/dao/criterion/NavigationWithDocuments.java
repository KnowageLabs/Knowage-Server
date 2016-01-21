package it.eng.spagobi.tools.crossnavigation.dao.criterion;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;

import it.eng.spagobi.commons.dao.ICriterion;
import it.eng.spagobi.tools.crossnavigation.bo.SimpleNavigation;
import it.eng.spagobi.tools.crossnavigation.metadata.SbiCrossNavigation;

public class NavigationWithDocuments implements ICriterion<SimpleNavigation>{

	@Override
	public Criteria evaluate(Session session) {
		
		return null;
	}

}
