package it.eng.spagobi.commons.bo;

import java.io.Serializable;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

public class CriteriaParameter implements Serializable {

	/**
	 * equal, not equal, like, like ignore case
	 */
	public enum Match {
		EQ, NOT_EQ, LIKE, ILIKE
	};

	private Match match;
	private String name;
	private Object value;

	public CriteriaParameter(String name, Object value) {
		this(name, value, Match.EQ);
	}

	public CriteriaParameter(String name, Object value, Match match) {
		this.name = name;
		this.value = value;
		this.match = match;
	}

	public Criterion toHibernateCriterion() {
		Criterion restriction = null;
		switch (getMatch()) {
		case LIKE:
			restriction = Restrictions.like(getName(), (String) getValue(), MatchMode.ANYWHERE);
			break;
		case ILIKE:
			restriction = Restrictions.like(getName(), (String) getValue(), MatchMode.ANYWHERE).ignoreCase();
			break;
		case NOT_EQ:
			restriction = Restrictions.ne(getName(), getValue());
			break;
		default:
			restriction = Restrictions.eq(getName(), getValue());
			break;
		}
		return restriction;
	}

	/**
	 * @return the match
	 */
	public Match getMatch() {
		return match != null ? match : Match.EQ;
	}

	/**
	 * @param match
	 *            the match to set
	 */
	public void setMatch(Match match) {
		this.match = match;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(Object value) {
		this.value = value;
	}
}
