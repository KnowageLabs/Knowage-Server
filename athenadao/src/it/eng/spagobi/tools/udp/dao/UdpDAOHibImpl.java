/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.tools.udp.dao;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.tools.udp.bo.Udp;
import it.eng.spagobi.tools.udp.metadata.SbiUdp;

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
 * 
 * @see it.eng.spagobi.tools.udp.bo.SbiUdp
 * @author Antonella Giachino
 */
public class UdpDAOHibImpl extends AbstractHibernateDAO implements IUdpDAO {

	private static final Logger logger = Logger.getLogger(UdpDAOHibImpl.class);


	public Integer insert(SbiUdp prop) {
		logger.debug("IN");
		Session session = getSession();
		Transaction tx = null;
		Integer id = null;
		try {
			tx = session.beginTransaction();
			updateSbiCommonInfo4Insert(prop);
			id = (Integer)session.save(prop);
			tx.commit();
		} catch (HibernateException e) {
			if( tx != null && tx.isActive() ){
				tx.rollback();
			}
			throw e;

		}finally{
			if(session != null){
				session.close();
			}
			logger.debug("OUT");
			return id;
		}
	}

/*
	public void insert(Session session, SbiUdp prop) {
		logger.debug("IN");
		session.save(prop);
		logger.debug("OUT");
	}
*/
	public void update(SbiUdp prop) {
		logger.debug("IN");
		Session session = getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			updateSbiCommonInfo4Update(prop);
			session.update(prop);
			tx.commit();

		} catch (HibernateException e) {
			if( tx != null && tx.isActive() ){
				tx.rollback();
			}
			throw e;

		}finally{
			session.close();
		}
		logger.debug("OUT");		
	}	

	

	public void delete(SbiUdp prop) {
		logger.debug("IN");
		Session session = getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.delete(prop);
			tx.commit();

		} catch (HibernateException e) {
			if( tx != null && tx.isActive() ){
				tx.rollback();
			}
			throw e;

		}finally{
			session.close();
		}
		logger.debug("OUT");
	}

	public void delete(Integer id) {
		logger.debug("IN");
		Session session = getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.delete(session.load(SbiUdp.class, id));
			tx.commit();

		} catch (HibernateException e) {
			if( tx != null && tx.isActive() ){
				tx.rollback();
			}
			throw e;

		}finally{
			session.close();
		}
		logger.debug("OUT");
	}



	@SuppressWarnings("unchecked")
	public SbiUdp findById(Integer id) {
		logger.debug("IN");
		SbiUdp prop = null;
		Session session = getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			prop = (SbiUdp)session.get(SbiUdp.class, id);
			tx.commit();

		} catch (HibernateException e) {
			if( tx != null && tx.isActive() ){
				tx.rollback();
			}
			throw e;

		}finally{
			session.close();
		}
		logger.debug("OUT");
		return prop;
	}



	public Udp loadById(Integer id) {
		logger.debug("IN");
		Session session = getSession();
		Udp udp = null;
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			SbiUdp prop = (SbiUdp)session.get(SbiUdp.class, id);
			tx.commit();
			udp=toUdp(prop);
		} catch (HibernateException e) {
			if( tx != null && tx.isActive() ){
				tx.rollback();
			}
			throw e;

		}finally{
			session.close();
		}
		logger.debug("OUT");
		return udp;
	}



	/**
	 *  Load a Udp by Label
	 * @throws EMFUserError 
	 */

	public Udp loadByLabel(String label) throws EMFUserError {
		logger.debug("IN");
		Udp udp = null;
		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			Criterion labelCriterrion = Expression.eq("label", label);
			Criteria criteria = tmpSession.createCriteria(SbiUdp.class);
			criteria.add(labelCriterrion);	
			SbiUdp hibUDP = (SbiUdp) criteria.uniqueResult();
			if (hibUDP == null) return null;
			udp = toUdp(hibUDP);				

			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while loading the udp with label " + label, he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (tmpSession!=null){
				if (tmpSession.isOpen()) tmpSession.close();
			}
		}
		logger.debug("OUT");
		return udp;		

	}


	/**
	 *  Load a Udp by Label and Family code
	 * @throws EMFUserError 
	 */

	public Udp loadByLabelAndFamily(String label, String family) throws EMFUserError {
		logger.debug("IN");
		Udp udp = null;
		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			// get familyId
			String hql = "from SbiDomains s " +
			"	where lower(s.valueCd) = lower(?) AND " +
			"         s.domainCd = ?";
			Query hqlQuery = tmpSession.createQuery(hql);
			hqlQuery.setString(0, family);
			hqlQuery.setString(1, "UDP_FAMILY");
			

			SbiDomains famiDom = (SbiDomains) hqlQuery.uniqueResult();
			if (famiDom == null) return null;

			Criterion labelCriterrion = Expression.eq("label", label);
			Criteria criteria2 = tmpSession.createCriteria(SbiUdp.class);
			criteria2.add(labelCriterrion);	
			Criterion famCriterrion = Expression.eq("familyId", famiDom.getValueId());
			criteria2.add(famCriterrion);	

			SbiUdp hibUDP = (SbiUdp) criteria2.uniqueResult();
			if (hibUDP == null) return null;
			udp = toUdp(hibUDP);				

			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while loading the udp with label " + label, he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (tmpSession!=null){
				if (tmpSession.isOpen()) tmpSession.close();
			}
		}
		logger.debug("OUT");
		return udp;		

	}



	@SuppressWarnings("unchecked")
	public List<SbiUdp> findAll() {
		Session session = getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();

			List<SbiUdp> list = (List<SbiUdp>)session.createQuery("from SbiUdp").list();
			tx.commit();
			return list;

		} catch (HibernateException e) {
			if( tx != null && tx.isActive() ){
				tx.rollback();
			}
			throw e;

		}finally{
			session.close();
		}
	}	


	@SuppressWarnings("unchecked")
	public List<Udp> loadAllByFamily(String familyCd) throws EMFUserError {
		logger.debug("IN");
		Session session = getSession();
		List<Udp> toReturn = null;
		// get Domain id form KPI family
		Transaction tx = null;
		try {

			Integer domainId;
			SbiDomains domain = DAOFactory.getDomainDAO().loadSbiDomainByCodeAndValue("UDP_FAMILY", familyCd);

			if(domain== null){
				logger.error("could not find domain of type UDP_FAMILY with value code "+familyCd);
				return null;
			}
			else{
				domainId = domain.getValueId();
			}


			tx = session.beginTransaction();
			Query query = session.createQuery("from SbiUdp s where s.familyId = :idFamily");
			query.setInteger("idFamily", domainId);

			List<SbiUdp> list = (List<SbiUdp>)query.list();
			if(list != null){
				toReturn = new ArrayList<Udp>();
				for (Iterator iterator = list.iterator(); iterator.hasNext();) {
					SbiUdp sbiUdp = (SbiUdp) iterator.next();
					Udp udp = toUdp(sbiUdp);
					toReturn.add(udp);
				}
			}
			tx.commit();

		} catch (HibernateException e) {
			if( tx != null && tx.isActive() ){
				tx.rollback();
			}
			throw e;

		}catch (EMFUserError e) {
			logger.error("error probably in getting asked UDP_FAMILY domain", e);
			if( tx != null && tx.isActive() ){
				tx.rollback();
			}
			throw e;

		}finally{
			session.close();
		}
		logger.debug("OUT");
		return toReturn;
	}	


	public Udp toUdp(SbiUdp sbiUdp){
		logger.debug("IN");
		Udp toReturn=new Udp();

		toReturn.setUdpId(sbiUdp.getUdpId());
		toReturn.setLabel(sbiUdp.getLabel());
		toReturn.setName(sbiUdp.getName());
		toReturn.setDescription(sbiUdp.getDescription());
		toReturn.setDataTypeId(sbiUdp.getTypeId());
		toReturn.setFamilyId(sbiUdp.getFamilyId());
		toReturn.setMultivalue(sbiUdp.isIsMultivalue());

		// get the type ValueCd
		if (sbiUdp.getTypeId() != null){
			Domain domain;
			try {
				domain = DAOFactory.getDomainDAO().loadDomainById(sbiUdp.getTypeId());
				toReturn.setDataTypeValeCd(domain.getValueCd());
			} catch (EMFUserError e) {
				logger.error("error in loading domain with Id "+sbiUdp.getTypeId(), e);
			}
		}
		logger.debug("OUT");
		return toReturn;
	}


	public Integer countUdp() throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		Integer resultNumber;
		
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
		
			String hql = "select count(*) from SbiUdp ";
			Query hqlQuery = aSession.createQuery(hql);
			Long temp = (Long)hqlQuery.uniqueResult();
			resultNumber = new Integer(temp.intValue());

		} catch (HibernateException he) {
			logger.error("Error while loading the list of SbiUdp", he);	
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


	public List<SbiUdp> loadPagedUdpList(Integer offset, Integer fetchSize)
			throws EMFUserError {
		logger.debug("IN");
		List<SbiUdp> toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		Integer resultNumber;
		Query hibernateQuery;
		
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			toReturn = new ArrayList();
			List toTransform = null;
		
			String hql = "select count(*) from SbiUdp ";
			Query hqlQuery = aSession.createQuery(hql);
			Long temp = (Long)hqlQuery.uniqueResult();
			resultNumber = new Integer(temp.intValue());
			
			offset = offset < 0 ? 0 : offset;
			if(resultNumber > 0) {
				fetchSize = (fetchSize > 0)? Math.min(fetchSize, resultNumber): resultNumber;
			}
			
			hibernateQuery = aSession.createQuery("from SbiUdp order by name");
			hibernateQuery.setFirstResult(offset);
			if(fetchSize > 0) hibernateQuery.setMaxResults(fetchSize);			

			toReturn = (List<SbiUdp>)hibernateQuery.list();	

		} catch (HibernateException he) {
			logger.error("Error while loading the list of Resources", he);	
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
		return toReturn;
	}

}

