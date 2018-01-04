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
package it.eng.spagobi.tools.dataset.dao;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.SpagoBIDAOException;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSet;
import it.eng.spagobi.utilities.assertion.Assert;

public class SbiDataSetDAOImpl extends AbstractHibernateDAO implements ISbiDataSetDAO {

	static private Logger logger = Logger.getLogger(SbiDataSetDAOImpl.class);

	@Override
	public SbiDataSet loadSbiDataSetByLabel(String label) {
		Session session;
		Transaction transaction;

		logger.debug("IN");

		session = null;
		transaction = null;
		try {
			if (label == null) {
				throw new IllegalArgumentException("Input parameter [label] cannot be null");
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Throwable t) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", t);
			}
			Query hibQuery = session.createQuery("from SbiDataSet h where h.active = ? and h.label = ? ");
			hibQuery.setBoolean(0, true);
			hibQuery.setString(1, label);
			SbiDataSet sbiDataSet = (SbiDataSet) hibQuery.uniqueResult();

			transaction.commit();

			return sbiDataSet;
		} catch (Throwable t) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("An unexpected error occured while loading dataset whose label is equal to [" + label + "]", t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
	}

	@Override
	public List<SbiDataSet> loadSbiDataSets() {
		return loadDataSets(null, null, null, null, null, null, null, true);
	}

	@Override
	public SbiDataSet loadSbiDataSetByIdAndOrganiz(Integer id, String organiz) {
		Session session;
		List<SbiDataSet> list = null;
		SbiDataSet sbiDataSet = null;
		try {
			session = getSession();
			Criteria c = session.createCriteria(SbiDataSet.class);
			c.addOrder(Order.asc("label"));
			c.add(Restrictions.eq("id.dsId", id));
			if (organiz != null) {
				c.add(Restrictions.eq("id.organization", organiz));
			}
			c.add(Restrictions.eq("active", true));

			sbiDataSet = (SbiDataSet) c.uniqueResult();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return sbiDataSet;
	}

	@Override
	public List<SbiDataSet> loadPaginatedSearchSbiDataSet(String search, Integer page, Integer item_per_page, IEngUserProfile finalUserProfile,
			Boolean seeTechnical) {
		return loadPaginatedSearchSbiDataSet(search, page, item_per_page, finalUserProfile, seeTechnical, null);
	}

	@Override
	public List<SbiDataSet> loadPaginatedSearchSbiDataSet(String search, Integer page, Integer item_per_page, IEngUserProfile finalUserProfile,
			Boolean seeTechnical, Integer[] ids) {
		Session session;
		List<SbiDataSet> list = null;

		try {
			session = getSession();
			Criteria c = session.createCriteria(SbiDataSet.class);
			c.addOrder(Order.asc("label"));

			if (page != null && item_per_page != null) {
				c.setFirstResult((page - 1) * item_per_page);
				c.setMaxResults(item_per_page);
			}

			c.add(Restrictions.like("label", search == null ? "" : search, MatchMode.ANYWHERE).ignoreCase());
			c.add(Restrictions.eq("active", true));

			if (ids != null && ids.length > 0) {
				c.add(Restrictions.in("id.dsId", ids));
			}

			if (finalUserProfile != null) {

				logger.debug("For final user take only owned, enterprise and shared");

				SbiDomains scopeUserDomain = DAOFactory.getDomainDAO().loadSbiDomainByCodeAndValue("DS_SCOPE", SpagoBIConstants.DS_SCOPE_USER);
				SbiDomains scopeEnterpriseDomain = DAOFactory.getDomainDAO().loadSbiDomainByCodeAndValue("DS_SCOPE", SpagoBIConstants.DS_SCOPE_ENTERPRISE);
				SbiDomains scopeTechnicalDomain = DAOFactory.getDomainDAO().loadSbiDomainByCodeAndValue("DS_SCOPE", SpagoBIConstants.DS_SCOPE_TECHNICAL);

				Disjunction or = Restrictions.disjunction();

				// OWNER OR

				// take owned datasets
				or.add(Restrictions.eq("owner", ((UserProfile) finalUserProfile).getUserId().toString()));

				// get categories
				Set<Domain> categoryList = UserUtilities.getDataSetCategoriesByUser(finalUserProfile);

				if (categoryList != null) {
					if (categoryList.size() > 0) {
						SbiDomains[] categoryArray = new SbiDomains[categoryList.size()];
						int i = 0;
						for (Iterator iterator = categoryList.iterator(); iterator.hasNext();) {
							Domain domain = (Domain) iterator.next();
							String domainCd = domain.getDomainCode();
							String valueCd = domain.getValueCd();
							SbiDomains sbiDomain = DAOFactory.getDomainDAO().loadSbiDomainByCodeAndValue(domainCd, valueCd);
							categoryArray[i] = sbiDomain;
							i++;
						}
						// (IN CATEGORY AND (SCOPE=USER OR SCOPE=ENTERPRISE)) OR SCOPE=TECHNICAL
						Conjunction andCategories = Restrictions.conjunction();
						andCategories.add(Restrictions.in("category", categoryArray));

						Disjunction orScope = Restrictions.disjunction();
						orScope.add(Restrictions.eq("scope", scopeUserDomain));
						orScope.add(Restrictions.eq("scope", scopeEnterpriseDomain));

						andCategories.add(orScope);

						if (seeTechnical != null && seeTechnical) {
							Disjunction orTechnical = Restrictions.disjunction();
							orTechnical.add(andCategories);
							orTechnical.add(Restrictions.eq("scope", scopeTechnicalDomain));
							or.add(orTechnical);
						} else {
							or.add(andCategories);
						}

					}
				} else {
					// if no categoryList take also all USER and ENTERPRISE dataset
					// SCOPE=USER OR SCOPE=ENTERPRISE)
					or.add(Restrictions.eq("scope", scopeUserDomain));
					or.add(Restrictions.eq("scope", scopeEnterpriseDomain));
				}

				c.add(or);
			}

			list = c.list();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@Override
	public List<SbiDataSet> loadDataSets(String owner, Boolean includeOwned, Boolean includePublic, String scope, String type, String category,
			String implementation, Boolean showDerivedDatasets) {

		Session session = getSession();

		logger.debug("IN");

		session = null;
		try {
			// open session
			session = getSession();

			// create statement
			String statement = "from SbiDataSet h where h.active = ?";
			if (owner != null) {
				String ownedCondition = includeOwned ? "h.owner = ?" : "h.owner != ?";
				statement += " and " + ownedCondition + " ";
			}
			if (type != null)
				statement += " and h.scope.valueCd = ? ";
			if (category != null)
				statement += " and h.category.valueCd = ? ";
			if (implementation != null)
				statement += " and h.type = ? ";
			if (showDerivedDatasets == null || showDerivedDatasets.equals(false))
				statement += " and h.federation is null ";

			// inject parameters
			int paramIndex = 0;
			Query query = session.createQuery(statement);
			query.setBoolean(paramIndex++, true);
			if (owner != null) {
				query.setString(paramIndex++, owner);
			}
			if (type != null)
				query.setString(paramIndex++, type);
			if (category != null)
				query.setString(paramIndex++, category);
			if (implementation != null)
				query.setString(paramIndex++, implementation);

			return executeQuery(query, session);
		} catch (Throwable t) {
			throw new SpagoBIDAOException("An unexpected error occured while loading dataset whose owner is equal to [" + owner + "]", t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
	}

	private List<SbiDataSet> executeQuery(Query query, Session session) {
		List<SbiDataSet> sbiDataSetList;
		Transaction transaction;

		logger.debug("IN");

		transaction = null;
		try {
			transaction = beginTransaction(session);
			sbiDataSetList = query.list();

			transaction.commit();
		} catch (Throwable t) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("An unexpected error occured while loading dataset", t);
		} finally {
			logger.debug("OUT");
		}

		return sbiDataSetList;
	}

	private Transaction beginTransaction(Session session) {
		Transaction transaction = null;
		try {
			Assert.assertNotNull(session, "session cannot be null");
			transaction = session.beginTransaction();
			Assert.assertNotNull(transaction, "transaction cannot be null");
		} catch (Throwable t) {
			throw new SpagoBIDAOException("An error occured while creating the new transaction", t);
		}

		return transaction;
	}

	@Override
	public Integer countSbiDataSet(String search) throws EMFUserError {
		return countSbiDataSet(search, null);
	}

	@Override
	public Integer countSbiDataSet(String search, Integer[] ids) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		Integer resultNumber;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			String hql = "select count(*) from SbiDataSet where active=true and label like '%" + search + "%'" + getIdsWhereClause(ids);
			Query hqlQuery = aSession.createQuery(hql);
			Long temp = (Long) hqlQuery.uniqueResult();
			resultNumber = new Integer(temp.intValue());

		} catch (HibernateException he) {
			logger.error("Error while loading the list of SbiDataSet", he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 9104);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}
		return resultNumber;
	}

	private String getIdsWhereClause(Integer[] ids) {
		String result = "";
		if (ids != null && ids.length > 0) {
			StringBuilder sb = new StringBuilder();
			for (Integer id : ids) {
				sb.append(",");
				sb.append(id);
			}
			sb.deleteCharAt(0);
			sb.insert(0, " and id.dsId in (");
			sb.append(")");
			result = sb.toString();
		}
		return result;
	}

}
