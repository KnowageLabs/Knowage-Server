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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Criterion;
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
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSet;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSetFilter;
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
			initialize(sbiDataSet);

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
	public SbiDataSet loadSbiDataSetByIdAndOrganiz(Integer id, String organiz, Session session) {
		SbiDataSet sbiDataSet = null;
		try {
			Criteria c = session.createCriteria(SbiDataSet.class);
			c.addOrder(Order.asc("label"));
			c.add(Restrictions.eq("id.dsId", id));
			if (organiz != null) {
				c.add(Restrictions.eq("id.organization", organiz));
			}
			onlyActive(c);

			sbiDataSet = (SbiDataSet) c.uniqueResult();
			initialize(sbiDataSet);

		} catch (Exception e) {
			throw new SpagoBIDAOException("An unexpected error occured while loading datasets", e);
		} finally {
			logger.debug("OUT");
		}
		return sbiDataSet;
	}

	@Override
	public SbiDataSet loadSbiDataSetByIdAndOrganiz(Integer id, String organiz) {
		Session session = null;
		SbiDataSet sbiDataSet = null;
		try {
			session = getSession();
			sbiDataSet = loadSbiDataSetByIdAndOrganiz(id, organiz, session);
		} catch (Exception e) {
			throw new SpagoBIDAOException("An unexpected error occured while loading datasets", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
		return sbiDataSet;
	}

	@Override
	public List<SbiDataSet> loadPaginatedSearchSbiDataSet(String search, Integer page, Integer item_per_page, IEngUserProfile finalUserProfile,
			Boolean seeTechnical, Integer[] ids, boolean spatialOnly) {
		Session session = null;
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
			onlyActive(c);

			if (ids != null && ids.length > 0) {
				c.add(Restrictions.in("id.dsId", ids));
			}

			if (spatialOnly) {
				c.add(Restrictions.like("dsMetadata", IFieldMetaData.FieldType.SPATIAL_ATTRIBUTE.toString(), MatchMode.ANYWHERE));
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
			initialize(list);

		} catch (Exception e) {
			throw new SpagoBIDAOException("An unexpected error occured while loading datasets", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
		return list;
	}

	@Override
	public List<SbiDataSet> loadDataSets(String owner, Boolean includeOwned, Boolean includePublic, String scope, String type, String category,
			String implementation, Boolean showDerivedDatasets) {

		Session session = null;

		logger.debug("IN");

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

			List<SbiDataSet> datasets = executeQuery(query, session);
			initialize(datasets);
			return datasets;
		} catch (Exception e) {
			throw new SpagoBIDAOException("An unexpected error occured while loading dataset whose owner is equal to [" + owner + "]", e);
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
			transaction = session.beginTransaction();
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

	private static void initialize(SbiDataSet dataset) {
		Hibernate.initialize(dataset.getId());
		Hibernate.initialize(dataset.getCategory());
		Hibernate.initialize(dataset.getTransformer());
		Hibernate.initialize(dataset.getScope());
		Hibernate.initialize(dataset.getFederation());
	}

	private static void initialize(List<SbiDataSet> datasets) {
		for (SbiDataSet dataset : datasets) {
			initialize(dataset);
		}
	}

	@Override
	public List<SbiDataSet> list(int offset, int fetchSize, String owner, String sortByColumn, boolean reverse, List<Integer> tagIds, List<SbiDataSetFilter> filter) {

		Session session = null;
		List<SbiDataSet> ret = Collections.emptyList();

		try {
			session = getSession();

			Criteria cr = session.createCriteria(SbiDataSet.class);

			onlyActive(cr);
			fromOffset(cr, offset);
			withFetchSize(cr, fetchSize);
			ownedBy(cr, owner);
			sortedBy(cr, sortByColumn, reverse);
			withTags(cr, tagIds);
			filterOn(cr, filter);

			ret = cr.list();

		} catch(Exception ex) {
			LogMF.error(logger, "Error getting list of dataset with offset {0}, limit {1}, owner {2}, sorting column {3}, reverse {4} and tags {5}", new Object[] { offset, fetchSize, owner, sortByColumn, reverse, tagIds });
		}finally {
			if (session != null) {
				session.close();
			}
		}

		return ret;
	}

	@Override
	public List<SbiDataSet> workspaceList(int offset, int fetchSize, String owner, boolean includeOwned, boolean includePublic, String scope, String type, Set<Domain> categoryList, String implementation, boolean showDerivedDatasets) {

		List<SbiDataSet> results = Collections.emptyList();
		Session session = null;

		try {
			// open session
			session = getSession();
			Criteria cr = session.createCriteria(SbiDataSet.class);

			onlyActive(cr);
			fromOffset(cr, offset);
			withFetchSize(cr, fetchSize);

			if (StringUtils.isNotEmpty(owner)) {
				if (includeOwned) {
					ownedBy(cr, owner);
				} else {
					notOwnedBy(cr, owner);
				}
			}

			withDsTypeCd(cr, type);

			if (categoryList != null) {
				logger.debug("We'll take in consideration categories");
				if (!categoryList.isEmpty()) {
					logger.debug("User has one or more categories");
					if (owner != null && includeOwned) {
						logger.debug("The owner can see all it's datasets");
					} else {
						List<String> collect = categoryList.stream().map(e -> e.getValueCd()).collect(Collectors.toList());
						cr.createAlias("category", "c");
						cr.add(Restrictions.in("c.valueCd", collect));
					}
				} else {
					logger.debug("No categories for the user so we take just it's own datasets");
					if (owner == null || !includeOwned) {
						logger.debug("Owner is not specified on the service so we should return no datasets");
						return Collections.emptyList();
					}
				}

			}

			withImplementation(cr, implementation);
			withDerived(cr, showDerivedDatasets);

			results = cr.list();

		} catch (Throwable t) {
			throw new SpagoBIDAOException("An unexpected error occured while loading dataset whose owner is equal to [" + owner + "]", t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}

		return results;
	}

	@Override
	public List<SbiDataSet> loadMyDataSets(int offset, int fetchSize, UserProfile userProfile) {
		logger.debug("IN");
		List<SbiDataSet> results = new ArrayList<>();
		Session session = null;

		try {
			StringBuilder statement = new StringBuilder();
			Set<Domain> categoryList = null;
			List<Integer> categoryIds = null;

			statement.append("from SbiDataSet ds where ds.active = :active and (ds.owner = :owner or (");
			session = getSession();
			categoryList = UserUtilities.getDataSetCategoriesByUser(userProfile);
			if (categoryList.isEmpty()) {
				statement.append("ds.category.valueId is null ");
			} else {
				categoryIds = extractCategoryIds(categoryList);
				statement.append("(ds.category.valueId is null or ds.category.valueId in (:categories)) ");
			}

			statement.append(
					"and ds.scope.valueId in (select dom.valueId from SbiDomains dom where dom.valueCd in ('USER', 'ENTERPRISE') and dom.domainCd = 'DS_SCOPE')))");

			Query query = session.createQuery(statement.toString());
			query.setBoolean("active", true);
			query.setString("owner", userProfile.getUserId().toString());

			if (categoryIds != null && !categoryIds.isEmpty())
				query.setParameterList("categories", categoryIds);

			results = query.list();

		} catch (Exception e) {
			throw new SpagoBIDAOException("An unexpected error occured while loading all datasets for final user", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}

		return results;
	}

	private List<Integer> extractCategoryIds(Set<Domain> categoryList) {
		List<Integer> toReturn = new ArrayList<>();
		Iterator<Domain> it = categoryList.iterator();
		while (it.hasNext()) {
			Domain category = it.next();
			toReturn.add(category.getValueId());
		}
		return toReturn;
	}


	private void withImplementation(Criteria cr, String implementation) {
		if (StringUtils.isNotEmpty(implementation)) {
			cr.add(Restrictions.eq("type", implementation));
		}
	}

	private void withDerived(Criteria cr, Boolean showDerivedDatasets) {
		if (showDerivedDatasets == false) {
			cr.add(Restrictions.isNull("federation"));
		}
	}

	private void withDsTypeCd(Criteria cr, String type) {
		if (StringUtils.isNotEmpty(type)) {
			cr.createAlias("scope", "s");
			cr.add(Restrictions.eq("s.valueCd", type));
		}
	}

	private void notOwnedBy(Criteria cr, String owner) {
		if (StringUtils.isNotEmpty(owner)) {
			cr.add(notOwnedRestriction(owner));
		}
	}

	private void withTags(Criteria cr, List<Integer> tagIds) {
		if (!tagIds.isEmpty()) {
			cr.createAlias("tag.dsTagId", "tag");
			cr.add(Restrictions.in("tag.tagId", tagIds));
		}
	}

	private void sortedBy(Criteria cr, String sortByColumn, boolean reverse) {
		if (StringUtils.isNotEmpty(sortByColumn)) {
			Order orderBy = null;

			if (!reverse) {
				orderBy = Order.asc(sortByColumn);
			} else {
				orderBy = Order.desc(sortByColumn);
			}
			cr.addOrder(orderBy);
		}
	}

	private void ownedBy(Criteria cr, String owner) {
		if (StringUtils.isNotEmpty(owner)) {
			cr.add(ownedByRestriction(owner));
		}
	}

	private void ownedByCurrentUSer(Criteria cr) {
		IEngUserProfile userProfile = getUserProfile();
		ownedBy(cr, userProfile);
	}

	private void ownedBy(Criteria cr, IEngUserProfile userProfile) {
		String owner = userProfile.getUserUniqueIdentifier().toString();
		if (StringUtils.isNotEmpty(owner)) {
			cr.add(ownedByRestriction(owner));
		}
	}

	private void withFetchSize(Criteria cr, int fetchSize) {
		if (fetchSize != -1) {
			cr.setFetchSize(fetchSize);
		}
	}

	private void fromOffset(Criteria cr, int offset) {
		if (offset != -1) {
			cr.setFirstResult(offset);
		}
	}

	private void onlyActive(Criteria cr) {
		cr.add(onlyActiveRestriction());
	}

	private void filterOn(Criteria cr, List<SbiDataSetFilter> filters) {
		if (filters != null && !filters.isEmpty()) {

			Disjunction disjunction = Restrictions.disjunction();

			filters.forEach(filter -> {
				String typeFilter = filter.getType();
				String columnFilter = filter.getColumn();
				String valueFilter = filter.getValue();

				switch (typeFilter) {
				case "=":
					disjunction.add(Restrictions.eq(columnFilter, valueFilter));
					break;
				case "like":
					disjunction.add(Restrictions.like(columnFilter, "%" + valueFilter + "%").ignoreCase());
					break;
				default:
					logger.warn("Invalid filter type: " + typeFilter);
					break;
				}

			});

			cr.add(disjunction);

		}
	}



	private Criterion onlyActiveRestriction() {
		return Restrictions.eq("active", true);
	}

	private Criterion notOwnedRestriction(String owner) {
		return Restrictions.ne("owner", owner);
	}

	private Criterion ownedByRestriction(String owner) {
		return Restrictions.eq("owner", owner);
	}

	private Criterion ownedByRestriction(IEngUserProfile userProfile) {
		String owner = userProfile.getUserUniqueIdentifier().toString();
		return Restrictions.eq("owner", owner);
	}


}
