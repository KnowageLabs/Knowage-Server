/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.mapcatalogue.dao;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.mapcatalogue.bo.GeoFeature;
import it.eng.spagobi.mapcatalogue.metadata.SbiGeoFeatures;

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
 * @author giachino
 *
 */
public class SbiGeoFeaturesDAOHibImpl extends AbstractHibernateDAO implements ISbiGeoFeaturesDAO{
	
	/**
	 * Load feature by id.
	 * 
	 * @param featureID the feature id
	 * 
	 * @return the geo feature
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.mapcatalogue.dao.geo.bo.dao.ISbiGeoFeaturesDAO#loadFeatureByID(integer)
	 */
	public GeoFeature loadFeatureByID(Integer featureID) throws EMFUserError {
		GeoFeature toReturn = null;
		Session tmpSession = null;
		Transaction tx = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			//toReturn = (SbiGeoFeatures)tmpSession.load(SbiGeoFeatures.class,  featureID);
			SbiGeoFeatures hibFeature = (SbiGeoFeatures)tmpSession.load(SbiGeoFeatures.class,  featureID);
			toReturn = hibFeature.toGeoFeature();
			tx.commit();
			
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			
			if (tmpSession!=null){
				if (tmpSession.isOpen()) tmpSession.close();
				
			}
		}
		
		return toReturn;
	}
	
	/**
	 * Load feature by name.
	 * 
	 * @param name the name
	 * 
	 * @return the geo feature
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.mapcatalogue.dao.geo.bo.dao.ISbiGeoFeaturesDAO#loadFeatureByName(string)
	 */
	public GeoFeature loadFeatureByName(String name) throws EMFUserError {
		GeoFeature biFeature = null;
		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			Criterion labelCriterrion = Expression.eq("name",
					name);
			Criteria criteria = tmpSession.createCriteria(SbiGeoFeatures.class);
			criteria.add(labelCriterrion);	
			//List tmpLst = criteria.list();
			//return first feature (unique)
			//if (tmpLst != null && tmpLst.size()>0 ) biFeature = (SbiGeoFeatures)tmpLst.get(0);
			SbiGeoFeatures hibFeature = (SbiGeoFeatures) criteria.uniqueResult();
			if (hibFeature == null) return null;
			biFeature = hibFeature.toGeoFeature();	
			
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			
			if (tmpSession!=null){
				if (tmpSession.isOpen()) tmpSession.close();
			}
			
		}
		return biFeature;		
	}

	
	/**
	 * Modify feature.
	 * 
	 * @param aFeature the a feature
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.geo.bo.dao.IEngineDAO#modifyEngine(it.eng.spagobi.bo.Engine)
	 */
	public void modifyFeature(GeoFeature aFeature) throws EMFUserError {
		
		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			SbiGeoFeatures hibFeature = (SbiGeoFeatures) tmpSession.load(SbiGeoFeatures.class, new Integer(aFeature.getFeatureId()));
			hibFeature.setName(aFeature.getName());
			hibFeature.setDescr(aFeature.getDescr());
			hibFeature.setType(aFeature.getType());	
			updateSbiCommonInfo4Update(hibFeature);
			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			
			if (tmpSession!=null){
				if (tmpSession.isOpen()) tmpSession.close();
			}
			
		}

	}

	/**
	 * Insert feature.
	 * 
	 * @param aFeature the a feature
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.geo.bo.dao.IEngineDAO#insertEngine(it.eng.spagobi.bo.Engine)
	 */
	public void insertFeature(GeoFeature aFeature) throws EMFUserError {		
		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			SbiGeoFeatures hibFeature = new SbiGeoFeatures();
			//hibFeature.setFeatureId(new Integer(-1));
			hibFeature.setName(aFeature.getName());
			hibFeature.setDescr(aFeature.getDescr());
			hibFeature.setType(aFeature.getType());
			updateSbiCommonInfo4Insert(hibFeature);
			tmpSession.save(hibFeature);
			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			
			if (tmpSession!=null){
				if (tmpSession.isOpen()) tmpSession.close();
			}
			
		}
	}

	/**
	 * Erase feature.
	 * 
	 * @param aFeature the a feature
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.geo.bo.dao.IEngineDAO#eraseEngine(it.eng.spagobi.bo.Engine)
	 */
	public void eraseFeature(GeoFeature aFeature) throws EMFUserError {
		
		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			SbiGeoFeatures hibFeature = (SbiGeoFeatures) tmpSession.load(SbiGeoFeatures.class,
					new Integer(aFeature.getFeatureId()));

			tmpSession.delete(hibFeature);
			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			
			if (tmpSession!=null){
				if (tmpSession.isOpen()) tmpSession.close();
			}
			
		}
	}
	
	/**
	 * Load all features.
	 * 
	 * @return the list
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.geo.bo.dao.IEngineDAO#loadAllEngines()
	 */
	public List loadAllFeatures() throws EMFUserError {
		Session tmpSession = null;
		Transaction tx = null;
		List realResult = new ArrayList();
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			Query hibQuery = tmpSession.createQuery(" from SbiGeoFeatures");
			
			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();			
			while (it.hasNext()) {			
				SbiGeoFeatures hibFeature = (SbiGeoFeatures) it.next();	
				if (hibFeature != null) {
					GeoFeature bifeature = hibFeature.toGeoFeature();	
					realResult.add(bifeature);
				}
			}

			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			
			if (tmpSession!=null){
				if (tmpSession.isOpen()) tmpSession.close();
			}
			
		}
		return realResult;
	}
	
	/**
	 * Checks for maps associated.
	 * 
	 * @param featureId the feature id
	 * 
	 * @return true, if checks for maps associated
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.mapcatalogue.dao.geo.bo.dao.ISbiGeoMapsDAO#hasFeaturesAssociated(java.lang.String)
	 */
	public boolean hasMapsAssociated (String featureId) throws EMFUserError{
		boolean bool = false; 
		
		
		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			Integer mapIdInt = Integer.valueOf(featureId);
			
			//String hql = " from SbiGeoMapFeatures s where s.id.featureId = "+ mapIdInt;
			String hql = " from SbiGeoMapFeatures s where s.id.featureId = ?";
			Query aQuery = tmpSession.createQuery(hql);
			aQuery.setInteger(0, mapIdInt.intValue());
			
			List biFeaturesAssocitedWithMap = aQuery.list();
			if (biFeaturesAssocitedWithMap.size() > 0)
				bool = true;
			else
				bool = false;
			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (tmpSession!=null){
				if (tmpSession.isOpen()) tmpSession.close();
			}
		}
		return bool;
		
	}





}