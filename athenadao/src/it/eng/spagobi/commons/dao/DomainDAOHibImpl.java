/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/*
 * Created on 20-giu-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package it.eng.spagobi.commons.dao;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.metadata.SbiDomains;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Expression;

/**
 * Defines the Hibernate implementations for all DAO methods, for a domain.
 * 
 * @author zoppello e Monia Spinelli
 */
public class DomainDAOHibImpl extends AbstractHibernateDAO implements
		IDomainDAO {

	// logger component
	private static Logger logger = Logger.getLogger(DomainDAOHibImpl.class);
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
	public List loadListDomainsByType(String domainType) throws EMFUserError {
		/*
		 * <STATEMENT name="SELECT_LIST_DOMAINS" query="SELECT T.VALUE_NM AS
		 * VALUE_NAME, T.VALUE_ID AS VALUE_ID, T.VALUE_CD AS VALUE_CD FROM
		 * SBI_DOMAINS T WHERE DOMAIN_CD = ? "/>
		 */
		Session aSession = null;
		Transaction tx = null;

		List realResult = new ArrayList();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Criterion domainCdCriterrion = Expression
					.eq("domainCd", domainType);
			Criteria criteria = aSession.createCriteria(SbiDomains.class);
			criteria.add(domainCdCriterrion);

			List hibList = criteria.list();

			Iterator it = hibList.iterator();

			while (it.hasNext()) {
				realResult.add(toDomain((SbiDomains) it.next()));
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

	public List loadListDomainsByTypeAndTenant(String domainType)
			throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;

		List realResult = new ArrayList();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			

			
			Criterion domainCdCriterrion = Expression
					.eq("domainCd", domainType);
			Criteria criteria = aSession.createCriteria(SbiDomains.class);
			criteria.add(domainCdCriterrion);

			List hibList = criteria.list();
			
			String tenant = getTenant();


			Iterator it = hibList.iterator();

			while (it.hasNext()) {
				Query hibQueryEng = aSession.createQuery("from SbiOrganizationEngine oe "
						+ "where oe.sbiOrganizations.name = :tenant "
						+ "and oe.sbiEngines.biobjType.valueCd = :valueCd"
						);

				hibQueryEng.setString("tenant", tenant);				
				SbiDomains domain = (SbiDomains) it.next();
				hibQueryEng.setString("valueCd", domain.getValueCd());
				
				List hibListEng  =hibQueryEng.list();
				if(!hibListEng.isEmpty()){
					realResult.add(toDomain(domain));
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
	 * @see it.eng.spagobi.commons.dao.IDomainDAO#loadDomainByCodeAndValue(java.lang.String,
	 *      java.lang.String)
	 */
	public Domain loadDomainByCodeAndValue(String codeDomain, String codeValue)
			throws EMFUserError {
		/*
		 * <STATEMENT name="SELECT_DOMAIN_FROM_CODE_VALUE" query="SELECT
		 * D.VALUE_NM AS VALUE_NAME, D.VALUE_ID AS VALUE_ID, D.VALUE_CD AS
		 * VALUE_CD FROM SBI_DOMAINS D WHERE DOMAIN_CD = ? AND VALUE_CD = ? "/>
		 */
		Domain aDomain = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Criterion aCriterion = Expression.and(Expression.eq("domainCd",
					codeDomain), Expression.eq("valueCd", codeValue));
			Criteria criteria = aSession.createCriteria(SbiDomains.class);
			criteria.add(aCriterion);

			SbiDomains aSbiDomains = (SbiDomains) criteria.uniqueResult();
			if (aSbiDomains == null)
				return null;

			aDomain = toDomain(aSbiDomains);

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
	 * @see it.eng.spagobi.commons.dao.IDomainDAO#loadDomainByCodeAndValue(java.lang.String,
	 *      java.lang.String)
	 */
	public SbiDomains loadSbiDomainByCodeAndValue(String codeDomain,
			String codeValue) throws EMFUserError {
		/*
		 * <STATEMENT name="SELECT_DOMAIN_FROM_CODE_VALUE" query="SELECT
		 * D.VALUE_NM AS VALUE_NAME, D.VALUE_ID AS VALUE_ID, D.VALUE_CD AS
		 * VALUE_CD FROM SBI_DOMAINS D WHERE DOMAIN_CD = ? AND VALUE_CD = ? "/>
		 */
		SbiDomains aSbiDomains = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Criterion aCriterion = Expression.and(Expression.eq("domainCd",
					codeDomain), Expression.eq("valueCd", codeValue));
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
	 * From the hibernate domain object at input, gives the corrispondent
	 * <code>Domain</code> object.
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
	public Domain loadDomainById(Integer id) throws EMFUserError {

		Domain toReturn = null;
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiDomains hibDomain = (SbiDomains) aSession.load(SbiDomains.class,
					id);

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.spagobi.commons.dao.IDomainDAO#loadListDomains()
	 */
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
	 * to the hibernate domain object at input, from the corrispondent
	 * <code>Domain</code> object.
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
	public void saveDomain(Domain domain) throws EMFUserError {
		
		Domain toSave = null;
		Session aSession = null;
		Transaction tx = null;

		logger.debug("IN");
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			
			SbiDomains hibDomains = null;
			Integer id = domain.getValueId();
			
			if(id!=null){
				//modification
				logger.debug("Update Domain");
				hibDomains = (SbiDomains) aSession.load(SbiDomains.class, id);
				updateSbiCommonInfo4Update(hibDomains);
				hibDomains.setDomainCd(domain.getDomainCode());
				hibDomains.setDomainNm(domain.getDomainName());
				hibDomains.setValueCd(domain.getValueCd());
				hibDomains.setValueDs(domain.getValueDescription());
				hibDomains.setValueId(domain.getValueId());
				hibDomains.setValueNm(domain.getValueName());
			}
			else{
				//insertion
				logger.debug("Insert new Domain");
				hibDomains = fromDomain(domain);
				updateSbiCommonInfo4Insert(hibDomains);
				hibDomains.setValueId(domain.getValueId());
			}
			
			Integer newId = (Integer) aSession.save(hibDomains);
				
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
	public void delete(Integer idDomain) throws EMFUserError {
		logger.debug("IN");
		Session sess = null;
		Transaction tx = null;

		try {
			sess = getSession();
			tx = sess.beginTransaction();

			Criterion aCriterion = Expression.eq("valueId",idDomain);
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
