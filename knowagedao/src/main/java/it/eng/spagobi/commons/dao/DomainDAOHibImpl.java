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
package it.eng.spagobi.commons.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.commons.metadata.SbiProductType;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * Defines the Hibernate implementations for all DAO methods, for a domain.
 *
 * @author zoppello e Monia Spinelli
 */
public class DomainDAOHibImpl extends AbstractHibernateDAO implements IDomainDAO {

	// logger component
	private static Logger logger = Logger.getLogger(DomainDAOHibImpl.class);

	@Override
	public List<Integer> loadListMetaModelDomainsByRole(Integer roleId) throws SpagoBIRuntimeException {

		Session aSession = null;
		Transaction tx = null;

		List realResult = new ArrayList();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SQLQuery query = aSession.createSQLQuery("select category_id from SBI_EXT_ROLES_CATEGORY where ext_role_id=" + roleId);
			List hibList = query.list();

			Iterator it = hibList.iterator();

			while (it.hasNext()) {
				Integer categoryId = Integer.getInteger(it.next().toString());
				realResult.add(categoryId);
			}
			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new SpagoBIRuntimeException(he.getMessage());

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}

		return realResult;

	}

	/**
	 * Load list domains by type.
	 *
	 * @param domainType
	 *            the domain type
	 *
	 * @return the list
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.commons.dao.IDomainDAO#loadListDomainsByType(java.lang.String)
	 */
	@Override
	public List<Domain> loadListDomainsByType(String domainType) throws EMFUserError {
		/*
		 * <STATEMENT name="SELECT_LIST_DOMAINS" query="SELECT T.VALUE_NM AS VALUE_NAME, T.VALUE_ID AS VALUE_ID, T.VALUE_CD AS VALUE_CD FROM SBI_DOMAINS T WHERE
		 * DOMAIN_CD = ? "/>
		 */
		Session aSession = null;
		Transaction tx = null;

		List<Domain> realResult = new ArrayList<>();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Criterion domainCdCriterrion = Expression.eq("domainCd", domainType);
			Order valueIdOrder = Order.asc("valueId");
			Criteria criteria = aSession.createCriteria(SbiDomains.class);
			criteria.add(domainCdCriterrion);
			criteria.addOrder(valueIdOrder);

			List<SbiDomains> hibList = criteria.list();

			Iterator<SbiDomains> it = hibList.iterator();

			while (it.hasNext()) {
				realResult.add(toDomain(it.next()));
			}
			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}

		return realResult;

	}

	@Override
	public List loadListDomainsByTypeAndTenant(String domainType) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;

		List<Domain> realResult = new ArrayList<Domain>();
		Set<String> alreadyAdded = new HashSet<String>();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Criterion domainCdCriterrion = Expression.eq("domainCd", domainType);
			Criteria criteria = aSession.createCriteria(SbiDomains.class);
			criteria.add(domainCdCriterrion);

			List hibList = criteria.list();
			Iterator domainIt = hibList.iterator();

			String tenant = getTenant();

			while (domainIt.hasNext()) {

				SbiDomains domain = (SbiDomains) domainIt.next();

				Query hibQueryProd = aSession
						.createQuery("select opt.sbiProductType from SbiOrganizationProductType opt " + "where opt.sbiOrganizations.name = :tenant ");
				hibQueryProd.setString("tenant", tenant);

				List hibListProd = hibQueryProd.list();
				Iterator productIt = hibListProd.iterator();

				while (productIt.hasNext()) {
					SbiProductType productType = (SbiProductType) productIt.next();

					Query hibQueryEng = aSession.createQuery("from SbiProductTypeEngine pte " + "where pte.sbiProductType.label = :productType "
							+ "and pte.sbiEngines.biobjType.valueCd = :valueCd");

					hibQueryEng.setString("productType", productType.getLabel());
					hibQueryEng.setString("valueCd", domain.getValueCd());

					List hibListEng = hibQueryEng.list();
					Domain aDomain = toDomain(domain);
					if (!hibListEng.isEmpty() && alreadyAdded.add(aDomain.getValueName())) {
						realResult.add(aDomain);
					}
				}

			}
			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}

		return realResult;

	}

	/**
	 * Load domain by code and value.
	 *
	 * @param codeDomain
	 *            the code domain
	 * @param codeValue
	 *            the code value
	 *
	 * @return the domain
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.commons.dao.IDomainDAO#loadDomainByCodeAndValue(java.lang.String, java.lang.String)
	 */
	@Override
	public Domain loadDomainByCodeAndValue(String codeDomain, String codeValue) throws EMFUserError {
		/*
		 * <STATEMENT name="SELECT_DOMAIN_FROM_CODE_VALUE" query="SELECT D.VALUE_NM AS VALUE_NAME, D.VALUE_ID AS VALUE_ID, D.VALUE_CD AS VALUE_CD FROM
		 * SBI_DOMAINS D WHERE DOMAIN_CD = ? AND VALUE_CD = ? "/>
		 */
		Domain aDomain = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			aDomain = loadDomainByCodeAndValue(codeDomain, codeValue, aSession);

			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}

		return aDomain;
	}

	/**
	 * Same as loadDomainByCodeAndValue but with (optional) external session
	 *
	 * @param codeDomain
	 * @param codeValue
	 * @param aSession
	 * @return
	 * @throws EMFUserError
	 */
	@Override
	public Domain loadDomainByCodeAndValue(String codeDomain, String codeValue, Session aSession) throws EMFUserError {
		if (aSession == null) {
			return loadDomainByCodeAndValue(codeDomain, codeValue);
		} else {
			Criterion restrictionOnDomainCd = Restrictions.eq("domainCd", codeDomain);
			Criterion restructionOnValueCd = Restrictions.eq("valueCd", codeValue);
			Criterion aCriterion = Restrictions.and(restrictionOnDomainCd, restructionOnValueCd);
			Criteria criteria = aSession.createCriteria(SbiDomains.class);
			criteria.add(aCriterion);

			SbiDomains aSbiDomains = (SbiDomains) criteria.uniqueResult();
			if (aSbiDomains == null) {
				return null;
			}

			return toDomain(aSbiDomains);
		}
	}

	/**
	 * Load domain by code and value.
	 *
	 * @param codeDomain
	 *            the code domain
	 * @param codeValue
	 *            the code value
	 *
	 * @return the domain
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.commons.dao.IDomainDAO#loadDomainByCodeAndValue(java.lang.String, java.lang.String)
	 */
	@Override
	public SbiDomains loadSbiDomainByCodeAndValue(String codeDomain, String codeValue) throws EMFUserError {
		/*
		 * <STATEMENT name="SELECT_DOMAIN_FROM_CODE_VALUE" query="SELECT D.VALUE_NM AS VALUE_NAME, D.VALUE_ID AS VALUE_ID, D.VALUE_CD AS VALUE_CD FROM
		 * SBI_DOMAINS D WHERE DOMAIN_CD = ? AND VALUE_CD = ? "/>
		 */
		SbiDomains aSbiDomains = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Criterion aCriterion = Expression.and(Expression.eq("domainCd", codeDomain), Expression.eq("valueCd", codeValue));
			Criteria criteria = aSession.createCriteria(SbiDomains.class);
			criteria.add(aCriterion);

			aSbiDomains = (SbiDomains) criteria.uniqueResult();
			if (aSbiDomains == null)
				return null;

			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}

		return aSbiDomains;
	}

	/**
	 * From the hibernate domain object at input, gives the corrispondent <code>Domain</code> object.
	 *
	 * @param hibDomain
	 *            The hybernate Domain object
	 *
	 * @return The corrispondent <code>Domain</code>
	 */
	public Domain toDomain(SbiDomains hibDomain) {
		Domain aDomain = new Domain();
		aDomain.setValueCd(hibDomain.getValueCd());
		aDomain.setValueId(hibDomain.getValueId());
		aDomain.setValueName(hibDomain.getValueNm());
		aDomain.setDomainCode(hibDomain.getDomainCd());
		aDomain.setDomainName(hibDomain.getDomainNm());
		aDomain.setValueDescription(hibDomain.getValueDs());
		return aDomain;
	}

	/**
	 * Load domain by id.
	 *
	 * @param id
	 *            the id
	 *
	 * @return the domain
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.commons.dao.IDomainDAO#loadDomainById(java.lang.Integer)
	 */
	@Override
	public Domain loadDomainById(Integer id) throws EMFUserError {

		Domain toReturn = null;
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiDomains hibDomain = (SbiDomains) aSession.load(SbiDomains.class, id);

			toReturn = toDomain(hibDomain);
			tx.commit();

		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}

		return toReturn;
	}

	/**
	 * Load domain by id.
	 *
	 * @param id
	 *            the id
	 *
	 * @return the domain
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.commons.dao.IDomainDAO#loadSbiDomainById(java.lang.Integer)
	 */
	@Override
	public SbiDomains loadSbiDomainById(Integer id) throws EMFUserError {

		SbiDomains toReturn = null;
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiDomains hibDomain = (SbiDomains) aSession.load(SbiDomains.class, id);

			toReturn = hibDomain;
			tx.commit();

		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}

		return toReturn;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.commons.dao.IDomainDAO#loadListDomains()
	 */
	@Override
	public List loadListDomains() throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		List domains = new ArrayList();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Query hibQuery = aSession.createQuery(" from SbiDomains");
			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();
			while (it.hasNext()) {
				Domain dom = toDomain((SbiDomains) it.next());
				domains.add(dom);
			}
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		return domains;
	}

	/**
	 * to the hibernate domain object at input, from the corrispondent <code>Domain</code> object.
	 *
	 * @param Domain
	 *            object
	 *
	 * @return The corrispondent <code>SbiDomain</code>
	 */
	public SbiDomains fromDomain(Domain Domain) {
		SbiDomains hibDomain = new SbiDomains();
		hibDomain.setValueCd(Domain.getValueCd());
		hibDomain.setValueId(Domain.getValueId());
		hibDomain.setValueNm(Domain.getValueName());
		hibDomain.setDomainCd(Domain.getDomainCode());
		hibDomain.setDomainNm(Domain.getDomainName());
		hibDomain.setValueDs(Domain.getValueDescription());
		return hibDomain;
	}

	/**
	 * Save domain by id.
	 *
	 * @param id
	 *            the id
	 *
	 * @return void
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 */
	@Override
	public void saveDomain(Domain domain) throws EMFUserError {

		Domain toSave = null;
		Session aSession = null;
		Transaction tx = null;

		logger.debug("IN");
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Integer newId = saveDomain(domain, aSession);

			tx.commit();

			domain.setValueId(newId);

		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
			logger.debug("OUT");
		}

	}

	/**
	 * @param domain
	 * @param aSession
	 * @return
	 */
	@Override
	public Integer saveDomain(Domain domain, Session aSession) {
		SbiDomains hibDomains = null;
		Integer id = domain.getValueId();

		if (id != null) {
			// modification
			logger.debug("Update Domain");
			hibDomains = (SbiDomains) aSession.load(SbiDomains.class, id);
			updateSbiCommonInfo4Update(hibDomains);
			hibDomains.setDomainCd(domain.getDomainCode());
			hibDomains.setDomainNm(domain.getDomainName());
			hibDomains.setValueCd(domain.getValueCd());
			hibDomains.setValueDs(domain.getValueDescription());
			hibDomains.setValueId(domain.getValueId());
			hibDomains.setValueNm(domain.getValueName());
		} else {
			// insertion
			logger.debug("Insert new Domain");
			hibDomains = fromDomain(domain);
			updateSbiCommonInfo4Insert(hibDomains);
			hibDomains.setValueId(domain.getValueId());
		}

		Integer newId = (Integer) aSession.save(hibDomains);
		return newId;
	}

	/**
	 * Delete domain by id.
	 *
	 * @param id
	 *            the id
	 *
	 * @return void
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 */
	@Override
	public void delete(Integer idDomain) throws EMFUserError {
		logger.debug("IN");
		Session sess = null;
		Transaction tx = null;

		try {
			sess = getSession();
			tx = sess.beginTransaction();

			Criterion aCriterion = Expression.eq("valueId", idDomain);
			Criteria criteria = sess.createCriteria(SbiDomains.class);
			criteria.add(aCriterion);
			SbiDomains aSbiDomains = (SbiDomains) criteria.uniqueResult();
			if (aSbiDomains != null)
				sess.delete(aSbiDomains);
			tx.commit();

		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();
			throw new RuntimeException("Impossible to delete domain [" + idDomain + "]", he);

		} finally {
			if (sess != null) {
				if (sess.isOpen())
					sess.close();
			}
		}
	}

}
