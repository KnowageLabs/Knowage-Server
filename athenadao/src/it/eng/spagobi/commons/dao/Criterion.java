package it.eng.spagobi.commons.dao;

import org.hibernate.Criteria;
import org.hibernate.Session;

public interface Criterion<T> {

	public Criteria evaluate(Session session);

}
