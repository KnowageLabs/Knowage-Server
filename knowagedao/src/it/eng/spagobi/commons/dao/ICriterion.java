package it.eng.spagobi.commons.dao;

import org.hibernate.Criteria;
import org.hibernate.Session;

public interface ICriterion<T> {

	public Criteria evaluate(Session session);

}
