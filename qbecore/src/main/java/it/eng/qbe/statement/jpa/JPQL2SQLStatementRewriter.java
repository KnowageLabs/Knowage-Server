/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.qbe.statement.jpa;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.eclipse.persistence.internal.jpa.EJBQueryImpl;
import org.eclipse.persistence.jpa.JpaEntityManager;
import org.eclipse.persistence.queries.DatabaseQuery;
import org.eclipse.persistence.sessions.DatabaseRecord;
import org.eclipse.persistence.sessions.Session;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.Oracle8iDialect;
import org.hibernate.dialect.Oracle9iDialect;
import org.hibernate.engine.query.HQLQueryPlan;
import org.hibernate.hql.QueryTranslator;
import org.hibernate.hql.ast.QueryTranslatorImpl;
import org.hibernate.impl.SessionFactoryImpl;
import org.hibernate.impl.SessionImpl;
import org.hibernate.param.DynamicFilterParameterSpecification;

import it.eng.qbe.statement.hibernate.HQL2SQLStatementRewriter;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * The Class HqlToSqlQueryRewriter.
 *
 * @author Giachino, Alberto Ghedin (alberto.ghedin@eng.it)
 */
public class JPQL2SQLStatementRewriter {

	/** The entity manager. */
	private EntityManager entityManager;

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(JPQL2SQLStatementRewriter.class);

	/**
	 * Instantiates a new hql to sql query rewriter.
	 *
	 * @param session
	 *            the session
	 */
	public JPQL2SQLStatementRewriter(EntityManager em) {
		this.entityManager = em;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.qbe.export.IQueryRewriter#rewrite(java.lang.String)
	 */
	public String rewrite(String query) {
		if (this.entityManager instanceof org.eclipse.persistence.jpa.JpaEntityManager) {
			return rewriteEclipseLink(query);
		} else {
			return rewriteHibernate(query);
		}
	}

	public String rewrite(Query query) {
		if (this.entityManager instanceof org.eclipse.persistence.jpa.JpaEntityManager) {
			return rewriteEclipseLink(query);
		} else {
			return rewriteHibernate(query);
		}
	}

	/**
	 * Rewrite the JPQL query string in a SQL String (The persistence provider implementation in use is EclipseLink)
	 *
	 * @param query
	 *            The String of the JPQL query
	 * @return the string of the JPQL query translated in SQL
	 */
	private String rewriteEclipseLink(String query) {
		EJBQueryImpl qi = (EJBQueryImpl) this.entityManager.createQuery(query);
		return rewriteEclipseLink(qi);
	}

	/**
	 * Rewrite the JPQL query in a SQL String (The persistence provider implementation in use is EclipseLink)
	 *
	 * @param query
	 *            The JPQL query
	 * @return the string of the JPQL query translated in SQL
	 */
	private String rewriteEclipseLink(Query query) {
		EJBQueryImpl qi = (EJBQueryImpl) query;
		Session session = this.entityManager.unwrap(JpaEntityManager.class).getActiveSession();
		DatabaseQuery databaseQuery = (qi).getDatabaseQuery();
		databaseQuery.prepareCall(session, new DatabaseRecord());
		String sqlString = databaseQuery.getTranslatedSQLString(session, new DatabaseRecord());

		// ADD THE ALIAS in the select statement (necessary for the temporary table construction..)
		int fromPosition = sqlString.indexOf("FROM");
		StringBuffer sqlQuery2 = new StringBuffer();
		String SelectStatement = sqlString.substring(0, fromPosition - 1);
		StringTokenizer SelectStatementStk = new StringTokenizer(SelectStatement, ",");
		int i = 0;
		while (SelectStatementStk.hasMoreTokens()) {
			sqlQuery2.append(SelectStatementStk.nextToken());
			sqlQuery2.append(" as alias");
			sqlQuery2.append(i);
			sqlQuery2.append(",");
			i++;
		}
		sqlQuery2.delete(sqlQuery2.length() - 1, sqlQuery2.length());
		sqlQuery2.append(sqlString.substring(fromPosition - 1));

		logger.debug("JPQL QUERY: " + sqlQuery2);

		return sqlQuery2.toString();
	}

	/**
	 * Rewrite the JPQL query string in a SQL String (The persistence provider implementation in use is Hibernate)
	 *
	 * @param query
	 *            The String of the JPQL query
	 * @return the string of the JPQL query translated in SQL
	 */
	private String rewriteHibernate(String query) {

		/*
		 * Extract generated query from em with filters enabled.
		 */
		SessionImpl session = entityManager.unwrap(SessionImpl.class);
		Map<Object, Object> enabledFilters = session.getEnabledFilters();
		SessionFactoryImpl sessionFactory = (SessionFactoryImpl) session.getSessionFactory();
		HQLQueryPlan hqlQueryPlan = sessionFactory.getQueryPlanCache().getHQLQueryPlan(query, false, enabledFilters);
		QueryTranslator[] queryTranslators = hqlQueryPlan.getTranslators();
		QueryTranslatorImpl queryTranslator = (QueryTranslatorImpl) queryTranslators[0];
		List<?> collectedParameterSpecifications = queryTranslator.getCollectedParameterSpecifications();
		String sqlString = queryTranslator.getSQLString();
		StringBuilder finalSqlStringBuilder = new StringBuilder(sqlString);

		/*
		 * Replace parameters in query
		 */
		List<Object> values = new ArrayList<Object>();
		getFiltersValues(session, collectedParameterSpecifications, values);
		fixQueryParameters(sessionFactory, values);
		replaceQueryParameters(finalSqlStringBuilder, values);

		return finalSqlStringBuilder.toString();
	}

	private void replaceQueryParameters(StringBuilder finalSqlStringBuilder, List<Object> values) {
		int indexOf;
		Iterator<Object> iterator = values.iterator();
		while ((indexOf = finalSqlStringBuilder.indexOf("?")) != -1) {
			Object value = iterator.next();
			finalSqlStringBuilder.replace(indexOf, indexOf+1, value.toString());
		}
	}

	private void getFiltersValues(SessionImpl session, List<?> collectedParameterSpecifications, List<Object> values) {
		/*
		 * Get access to private fields of DynamicFilterParameterSpecification
		 */
		Field filterNameField = null;
		Field parameterNameField = null;
		try {
			filterNameField = DynamicFilterParameterSpecification.class.getDeclaredField("filterName");
			parameterNameField = DynamicFilterParameterSpecification.class.getDeclaredField("parameterName");
		} catch (Exception e) {
			throw new SpagoBIRuntimeException(e);
		}
		filterNameField.setAccessible(true);
		parameterNameField.setAccessible(true);

		/*
		 * Get values of parameters managing multivalues value
		 */
		for (Object currParameter : collectedParameterSpecifications) {
			try {
				String filterName = (String) filterNameField.get(currParameter);
				String parameterName = (String) parameterNameField.get(currParameter);
				Object value = session.getLoadQueryInfluencers().getFilterParameterValue( filterName + '.' + parameterName );

				if (value instanceof Collection) {
					Collection<?> coll = (Collection) value;
					for (Object obj : coll) {
						values.add(fix(obj));
					}
				} else {
					values.add(fix(value));
				}
			} catch (Exception e) {
				throw new SpagoBIRuntimeException(e);
			}
		}
	}

	private void fixQueryParameters(SessionFactoryImpl sessionFactory, List<Object> values) {
		Dialect dialect = sessionFactory.getDialect();
		boolean isOracle = isOracle(dialect);

		for (int i = 0; i < values.size(); i++) {
			Object value = values.get(i);
			if (value instanceof Date) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				sdf.setTimeZone(TimeZone.getDefault());
				Date valueAsDate = (Date) value;
				String valueAsString = sdf.format(valueAsDate);
				if (isOracle) {
					value = String.format("TO_DATE('%s', 'YYYY-MM-DD')", valueAsString);
				} else {
					value = String.format("CAST('%s' AS DATE)", valueAsString);
				}
			} else if (value instanceof String) {

				value = String.format("'%s'", value);
			} else if (value instanceof BigDecimal) {
				value = ((BigDecimal) value).toPlainString();
			} else {
				value = value.toString();
			}
			values.set(i, value);
		}
	}

	private boolean isOracle(Dialect dialect) {
		if (dialect instanceof Oracle8iDialect) {
			return true;
		} else if (dialect instanceof Oracle9iDialect) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Rewrite the JPQL query in a SQL String (The persistence provider implementation in use is Hibernate)
	 *
	 * @param query
	 *            The JPQL query
	 * @return the string of the JPQL query translated in SQL
	 */
	private String rewriteHibernate(Query query) {
		org.hibernate.ejb.HibernateEntityManager em = (org.hibernate.ejb.HibernateEntityManager) this.entityManager;
		org.hibernate.ejb.HibernateQuery qi = (org.hibernate.ejb.HibernateQuery) (query);
		org.hibernate.Session session = em.getSession();
		HQL2SQLStatementRewriter queryRewriter = new HQL2SQLStatementRewriter(session);
		String sqlQueryString = queryRewriter.rewrite(qi.getHibernateQuery().getQueryString());
		return sqlQueryString;
	}

	/**
	 * Fixes SQL values.
	 *
	 * WORKAROUND This code is present because of the way {@link it.eng.qbe.statement.jpa.JPQLDataSet} does the count
	 * TODO Remove this and fix the count in {@link it.eng.qbe.statement.jpa.JPQLDataSet}6
	 *
	 * @param value Value to fix
	 * @return Fixed value
	 */
	private Object fix(Object value) {
		if (value instanceof String) {
			String _value = (String) value;
			value = escapeString(_value);
		}
		return value;
	}

	/**
	 * WORKAROUND This code is present because of the way {@link it.eng.qbe.statement.jpa.JPQLDataSet} does the count
	 * TODO Remove this and fix the count in {@link it.eng.qbe.statement.jpa.JPQLDataSet}6
	 */
	private String escapeString(String value) {
		return value.toString().replace("'", "''");
	}

}
