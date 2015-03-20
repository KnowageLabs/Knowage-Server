/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.tools.udp.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IDomainDAO;
import it.eng.spagobi.kpi.config.bo.Kpi;
import it.eng.spagobi.kpi.config.metadata.SbiKpi;
import it.eng.spagobi.kpi.model.bo.Model;
import it.eng.spagobi.kpi.model.metadata.SbiKpiModel;
import it.eng.spagobi.tools.udp.bo.UdpValue;
import it.eng.spagobi.tools.udp.metadata.SbiUdp;
import it.eng.spagobi.tools.udp.metadata.SbiUdpValue;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * 
 * @see it.eng.spagobi.tools.udp.bo.SbiUdp
 * @author Antonella Giachino
 */
public class UdpValueDAOHibImpl extends AbstractHibernateDAO implements IUdpValueDAO {

	private static final Logger logger = Logger.getLogger(UdpValueDAOHibImpl.class);


	public Integer insert(SbiUdpValue propValue) {
		logger.debug("IN");
		Session session = null;
		Transaction tx = null;
		Integer id = null;
		try {
			session = getSession();
			tx = session.beginTransaction();
			updateSbiCommonInfo4Insert(propValue);
			id = (Integer)session.save(propValue);
			tx.commit();
		} catch (HibernateException e) {
			if( tx != null && tx.isActive() ){
				tx.rollback();
			}
			throw e;
		} finally {
			if(session != null){
				session.close();
			}
			logger.debug("OUT");
		}
		return id;
	}


	public void insert(Session session, SbiUdpValue propValue) {
		updateSbiCommonInfo4Insert(propValue);
		session.save(propValue);
	}

	public void update(SbiUdpValue propValue) {
		logger.debug("IN");
		Session session = getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			updateSbiCommonInfo4Update(propValue);
			session.update(propValue);
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

	public void update(Session session, SbiUdpValue propValue) {
		logger.debug("IN");
		updateSbiCommonInfo4Update(propValue);
		session.update(propValue);
		logger.debug("OUT");
	}	

	public void delete(SbiUdpValue propValue) {
		logger.debug("IN");
		Session session = getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.delete(propValue);
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

	public void delete(Session session, SbiUdpValue item) {
		logger.debug("IN");
		session.delete(item);
		logger.debug("OUT");
	}

	public void delete(Integer id) {
		logger.debug("IN");
		Session session = getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.delete(session.load(SbiUdpValue.class, id));
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


	public void delete(Session session, Integer id) {
		session.delete(session.load(SbiUdpValue.class, id));
	}

	@SuppressWarnings("unchecked")
	public SbiUdpValue findById(Integer id) {
		logger.debug("IN");
		SbiUdpValue propValue = null;
		Session session = getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			propValue = (SbiUdpValue)session.get(SbiUdpValue.class, id);
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
		return propValue;
	}


	@SuppressWarnings("unchecked")
	public List findByReferenceId(Integer kpiId, String family) {
		logger.debug("IN");
		Session aSession = getSession();
		Transaction tx = null;
		List<UdpValue> toReturn = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			String hql = "from SbiUdpValue s " +
			"	where s.referenceId = ? AND " +
			"         lower(s.family) = lower('"+family+"') AND "+
			"         s.endTs is NULL " +
			" order by s.label asc";
			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, kpiId);
			List toConvert = hqlQuery.list();
			for (Iterator iterator = toConvert.iterator(); iterator.hasNext();) {
				SbiUdpValue sbiUdpValue = (SbiUdpValue) iterator.next();
				UdpValue udpValue = toUdpValue(sbiUdpValue);
				if(toReturn == null) toReturn = new ArrayList<UdpValue>();
				toReturn.add(udpValue);
			}


		} catch (HibernateException e) {
			if( tx != null && tx.isActive() ){
				tx.rollback();
			}
			throw e;

		}finally{
			aSession.close();
		}
		logger.debug("OUT");
		return toReturn;
	}


	/**
	 *  Load a UdpValue by Id
	 */

	public UdpValue loadById(Integer id) {
		logger.debug("IN");
		Session session = getSession();
		UdpValue udpValue = null;
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			SbiUdpValue prop = (SbiUdpValue)session.get(SbiUdpValue.class, id);
			tx.commit();
			udpValue=toUdpValue(prop);

		} catch (HibernateException e) {
			if( tx != null && tx.isActive() ){
				tx.rollback();
			}
			throw e;

		}finally{
			session.close();
		}
		logger.debug("OUT");
		return udpValue;
	}

	/**
	 *  Load a UdpValue by refrence Id, udpId, family
	 */

	public UdpValue loadByReferenceIdAndUdpId(Integer referenceId, Integer udpId, String family) {
		logger.debug("IN");
		UdpValue toReturn = null;
		Session tmpSession = getSession();
		Transaction tx = null;
		try {
			tx = tmpSession.beginTransaction();
			String hql = "from SbiUdpValue s " +
			"	where s.referenceId = ? AND " +
			"         s.sbiUdp.udpId = ? AND "+
			"         lower(s.family) = lower('"+family+"') AND "+
			"         s.endTs is NULL " +
			" order by s.label asc";
			Query hqlQuery = tmpSession.createQuery(hql);
			hqlQuery.setInteger(0, referenceId);
			hqlQuery.setInteger(1, udpId);

			SbiUdpValue hibValueUDP = (SbiUdpValue) hqlQuery.uniqueResult();
			if (hibValueUDP == null) return null;
			toReturn = toUdpValue(hibValueUDP);				

			//tx.commit();

		} catch (HibernateException e) {
			if( tx != null && tx.isActive() ){
				tx.rollback();
			}
			throw e;

		}
		finally{
			tmpSession.close();
		}
		logger.debug("OUT");		
		return toReturn;
	}




	@SuppressWarnings("unchecked")
	public List<SbiUdpValue> findAll() {
		logger.debug("IN");
		Session session = getSession();
		List<SbiUdpValue> list = null;
		Transaction tx = null;
		try {
			tx = session.beginTransaction();

			list = (List<SbiUdpValue>)session.createQuery("from SbiUdpValue").list();
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
		return list;

	}	


	public UdpValue toUdpValue(SbiUdpValue sbiUdpValue){
		logger.debug("IN");
		UdpValue toReturn=new UdpValue();

		toReturn.setUdpValueId(sbiUdpValue.getUdpValueId());
		toReturn.setUdpId(sbiUdpValue.getSbiUdp().getUdpId());
		toReturn.setReferenceId(sbiUdpValue.getReferenceId());
		toReturn.setLabel(sbiUdpValue.getSbiUdp().getLabel()); //denormilized
		toReturn.setName(sbiUdpValue.getSbiUdp().getName());	//denormilized		

		try{
			IDomainDAO aDomainDAO = DAOFactory.getDomainDAO();
			Domain familyDomain = aDomainDAO.loadDomainById(sbiUdpValue.getSbiUdp().getFamilyId());
			toReturn.setFamily(familyDomain.getValueCd()); //denormilized
		} catch (Exception he) {
			logger.error(he);
		} 

		Integer typeId = sbiUdpValue.getSbiUdp().getTypeId();
		if(typeId != null){
			try{
				IDomainDAO aDomainDAO = DAOFactory.getDomainDAO();
				Domain typeDomain = aDomainDAO.loadDomainById(typeId);
				toReturn.setTypeLabel(typeDomain.getValueCd()); //denormilized
			} catch (Exception he) {
				logger.error(he);
			} 
		}


		toReturn.setValue(sbiUdpValue.getValue());
		toReturn.setProg(sbiUdpValue.getProg());
		toReturn.setBeginTs(sbiUdpValue.getBeginTs());
		toReturn.setEndTs(sbiUdpValue.getEndTs());

		logger.debug("OUT");
		return toReturn;
	}




	/**
	 * Given a ModelInstance Node or a Kpi
	 *  Get the Udp Value, update the existing one, add the new ones
	 * @throws EMFUserError 
	 */
	public void insertOrUpdateRelatedUdpValues(Object object, Object sbiObject, Session aSession, String family) throws EMFUserError{
		logger.debug("IN");

		SbiKpi sbiKpi = null;
		Kpi kpi = null;
		SbiKpiModel sbiKpiModel = null;
		Model modelNode = null;
		boolean isKpi = false;
		if(family.equalsIgnoreCase("KPI")){
			isKpi = true;
			sbiKpi = (SbiKpi)sbiObject;
			kpi = (Kpi)object;
			logger.debug("kpi udp attributes");
		}
		else if(family.equalsIgnoreCase("MODEL")){
			isKpi = false;
			sbiKpiModel = (SbiKpiModel)sbiObject;
			modelNode = (Model)object;
			logger.debug("model udp attributes");
		}
		else {
			logger.debug("family not recognied "+ family);
			return;
		}

		// if there are values associated 
		List<UdpValue> udpValues = null;
		Integer idObject = null;
		if(isKpi){
			udpValues = kpi.getUdpValues();
			idObject = sbiKpi.getKpiId();
		}
		else{
			udpValues = modelNode.getUdpValues();			
			idObject = sbiKpiModel.getKpiModelId();
		}		
		if(udpValues != null){
			// an udp value is never erased for a kpi once memorized, that is because by user interface integer have no null value and boolean too
			// these are current UdpValues; for each:
			for (Iterator iterator = udpValues.iterator(); iterator.hasNext();) {
				UdpValue udpValue = (UdpValue) iterator.next();
				// the tow ids of relationship; Kpi / Model and Udp				
				Integer udpId = udpValue.getUdpId();

				// search if KpiValue is already present, in that case update otherwise insert
				SbiUdpValue sbiUdpValue  = null;
				SbiUdpValue sbiUdpValueToClose  = null;

				UdpValue already = DAOFactory.getUdpDAOValue().loadByReferenceIdAndUdpId(idObject, udpValue.getUdpId(), family);						
				boolean inserting = true;
				boolean openNewOne = true;

				if(already == null){
					sbiUdpValue = new SbiUdpValue();					
				}
				else{
					inserting = false;
					// check if value has changed, if not so don't open a new one
					SbiUdpValue sbiUdpValueRetrieved = (SbiUdpValue) aSession.load(SbiUdpValue.class,already.getUdpValueId());												

					if(udpValue.getValue() != null &&  udpValue.getValue().equals(already.getValue())){
						// same value as before, simple update
						openNewOne = false;
						sbiUdpValue = sbiUdpValueRetrieved;
					}
					else{
						// new value, close preceding open a new one
						sbiUdpValueToClose = sbiUdpValueRetrieved;
						sbiUdpValue = new SbiUdpValue();
					}
				}

				// fill SbiUdpValue values
				sbiUdpValue.setLabel(udpValue.getLabel());
				sbiUdpValue.setName(udpValue.getName());
				sbiUdpValue.setProg(udpValue.getProg());
				sbiUdpValue.setFamily(udpValue.getFamily());

				sbiUdpValue.setReferenceId(idObject);
				SbiUdp hibUdp = (SbiUdp) aSession.load(SbiUdp.class,
						udpId);
				sbiUdpValue.setSbiUdp(hibUdp);
				sbiUdpValue.setValue(udpValue.getValue());

				if(inserting){
					logger.debug("Inserting Udp association between udp "+udpValue.getLabel() + " referencing family " + udpValue.getFamily() + 
							" with id "+ udpValue.getReferenceId() + "with value "+sbiUdpValue.getValue());
					sbiUdpValue.setBeginTs(new Date());
					this.insert(aSession, sbiUdpValue);					
					logger.debug("value to Udp "+hibUdp.getLabel()+ " has been inserted");
				}
				else{
					// the update must close the previous record and open a new one, but only if value has changed
					if(openNewOne){
						logger.debug("Close previous udp value and open Udp association between udp "+udpValue.getLabel() + " referencing family " + udpValue.getFamily() + 
								" with id "+ udpValue.getReferenceId() + "with value "+sbiUdpValue.getValue());
						// close previous one
						sbiUdpValueToClose.setBeginTs(already.getBeginTs());
						sbiUdpValueToClose.setEndTs(new Date());
						this.update(aSession, sbiUdpValueToClose);
						// insert new one
						sbiUdpValue.setBeginTs(new Date());
						this.insert(aSession, sbiUdpValue);
					}
					else{
						logger.debug("Update without closing Udp association between udp "+udpValue.getLabel() + " referencing family " + udpValue.getFamily() + 
								" with id "+ udpValue.getReferenceId() + "with value "+sbiUdpValue.getValue());						
						// just update fields no new opening
						sbiUdpValue.setBeginTs(already.getBeginTs());
						this.update(aSession, sbiUdpValue);						
					}

					logger.debug("value to Udp "+hibUdp.getLabel()+ " has been updated; associated to a "+family);
				}
			}
		}	
		logger.debug("OUT");

	}

}

