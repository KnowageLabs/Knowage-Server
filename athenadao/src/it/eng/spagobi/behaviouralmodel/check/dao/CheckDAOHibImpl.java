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
package it.eng.spagobi.behaviouralmodel.check.dao;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.behaviouralmodel.check.bo.Check;
import it.eng.spagobi.behaviouralmodel.check.metadata.SbiChecks;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.commons.utilities.SpagoBITracer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Expression;

/**
 * Defines the Hibernate implementations for all DAO methods,
 * for a value constraint.  
 * 
 * @author Zoppello
 */
public class CheckDAOHibImpl extends AbstractHibernateDAO implements ICheckDAO {

	

	/**
	 * Load all checks.
	 * 
	 * @return the list
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.behaviouralmodel.check.dao.ICheckDAO#loadAllChecks()
	 */
	public List loadAllChecks() throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;

		List realResult = new ArrayList();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = aSession.createQuery(" from SbiChecks");
			List hibList = hibQuery.list();

			tx.commit();
			

			Iterator it = hibList.iterator();

			while (it.hasNext()) {
				realResult.add(toCheck((SbiChecks) it.next()));
			}
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}
		return realResult;
	}

	/**
	 * Load check by id.
	 * 
	 * @param id the id
	 * 
	 * @return the check
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.behaviouralmodel.check.dao.ICheckDAO#loadCheckByID(java.lang.Integer)
	 */
	public Check loadCheckByID(Integer id) throws EMFUserError {
		Check toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
		
			SbiChecks hibCheck = (SbiChecks)aSession.load(SbiChecks.class,  id);
			
			toReturn = toCheck(hibCheck);
			tx.commit();
			
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}
		
		return toReturn;
	}

	/**
	 * Erase check.
	 * 
	 * @param check the check
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.behaviouralmodel.check.dao.ICheckDAO#eraseCheck(it.eng.spagobi.behaviouralmodel.check.bo.Check)
	 */
	public void eraseCheck(Check check) throws EMFUserError {

		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			
			SbiChecks hibCheck = (SbiChecks) aSession.load(SbiChecks.class,
					check.getCheckId());

			aSession.delete(hibCheck);
			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}

	}

	/**
	 * Insert check.
	 * 
	 * @param check the check
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.behaviouralmodel.check.dao.ICheckDAO#insertCheck(it.eng.spagobi.behaviouralmodel.check.bo.Check)
	 */
	public void insertCheck(Check check) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiChecks hibCheck = new SbiChecks();

			Criterion aCriterion = Expression.and(Expression.eq("valueId",
					check.getValueTypeId()), Expression.eq("valueCd", check
					.getValueTypeCd()));
			Criteria criteria = aSession.createCriteria(SbiDomains.class);
			criteria.add(aCriterion);

			SbiDomains checkType = (SbiDomains) criteria.uniqueResult();

			if (checkType == null){
				SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, 
					    "CheckDAOHibImpl", 
					    "insertCheck", 
					    "The Domain with value_id="+check.getValueTypeId()+" and value_cd="+check
						.getValueTypeCd()+" does not exist.");
				throw new EMFUserError(EMFErrorSeverity.ERROR, 1035);
			}
			
			hibCheck.setCheckType(checkType);
			hibCheck.setDescr(check.getDescription());
			hibCheck.setName(check.getName());
			hibCheck.setLabel(check.getLabel());
			hibCheck.setValue1(check.getFirstValue());
			hibCheck.setValue2(check.getSecondValue());
			hibCheck.setValueTypeCd(check.getValueTypeCd());
			updateSbiCommonInfo4Insert(hibCheck);
			aSession.save(hibCheck);
			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		
		}

	}

	/**
	 * Modify check.
	 * 
	 * @param check the check
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.behaviouralmodel.check.dao.ICheckDAO#modifyCheck(it.eng.spagobi.behaviouralmodel.check.bo.Check)
	 */
	public void modifyCheck(Check check) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		try {
			
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiChecks hibCheck = (SbiChecks) aSession.load(SbiChecks.class,
					check.getCheckId());
			
			Criterion aCriterion = Expression.and(Expression.eq("valueId",
					check.getValueTypeId()), Expression.eq("valueCd", check
					.getValueTypeCd()));
			Criteria criteria = aSession.createCriteria(SbiDomains.class);
			criteria.add(aCriterion);

			SbiDomains aSbiDomains = (SbiDomains) criteria.uniqueResult();
			
			if (aSbiDomains == null){
				SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, 
					    "CheckDAOHibImpl", 
					    "modifyCheck", 
					    "The Domain with value_id="+check.getValueTypeId()+" and value_cd="+check
						.getValueTypeCd()+" does not exist.");
				throw new EMFUserError(EMFErrorSeverity.ERROR, 1036);
			}

			hibCheck.setDescr(check.getDescription());
			hibCheck.setName(check.getName());
			hibCheck.setLabel(check.getLabel());
			hibCheck.setValue1(check.getFirstValue());
			hibCheck.setValue2(check.getSecondValue());
			hibCheck.setCheckType(aSbiDomains);
			hibCheck.setValueTypeCd(aSbiDomains.getValueCd());
			updateSbiCommonInfo4Update(hibCheck);
			tx.commit();
			
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {

			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}

		}

	}
	
	/**
	 * From the hibernate BI value constraint at input, gives
	 * the corrispondent <code>Check</code> object.
	 * 
	 * @param hibCheck The hybernate value constraint at input
	 * 
	 * @return The corrispondent <code>Check</code> object
	 */
	public Check toCheck(SbiChecks hibCheck){
		Check aCheck = new Check();
		aCheck.setCheckId(hibCheck.getCheckId());
		aCheck.setDescription(hibCheck.getDescr());
		aCheck.setFirstValue(hibCheck.getValue1());
		aCheck.setName(hibCheck.getName());
		aCheck.setLabel(hibCheck.getLabel());
		aCheck.setSecondValue(hibCheck.getValue2());
		aCheck.setValueTypeCd(hibCheck.getValueTypeCd());
		aCheck.setValueTypeId(hibCheck.getCheckType().getValueId());
		return  aCheck;
	}
	

	/**
	 * Checks if is referenced.
	 * 
	 * @param checkId the check id
	 * 
	 * @return true, if checks if is referenced
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.behaviouralmodel.check.dao.ICheckDAO#isReferenced(java.lang.String)
	 */
	public boolean isReferenced (String checkId) throws EMFUserError{
		boolean ref = false;
		Session aSession = null;
		Transaction tx = null;
		
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			
			Integer checkIdInt = Integer.valueOf(checkId);
			
			//String hql = "from SbiParuseCk s where s.id.sbiChecks.checkId = "+checkIdInt;
			String hql = "from SbiParuseCk s where s.id.sbiChecks.checkId = ?";
			Query aQuery = aSession.createQuery(hql);
			aQuery.setInteger(0, Integer.valueOf(checkId).intValue());
			List list = aQuery.list();
			if (list.size() > 0)
				ref = true;
			else
				ref = false;
			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {

			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}

		}
		return ref;
	}

}
