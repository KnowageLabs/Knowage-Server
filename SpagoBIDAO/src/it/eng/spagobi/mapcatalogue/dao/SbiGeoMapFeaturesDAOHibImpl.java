/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.mapcatalogue.dao;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.mapcatalogue.bo.GeoFeature;
import it.eng.spagobi.mapcatalogue.bo.GeoMap;
import it.eng.spagobi.mapcatalogue.bo.GeoMapFeature;
import it.eng.spagobi.mapcatalogue.metadata.SbiGeoMapFeatures;
import it.eng.spagobi.mapcatalogue.metadata.SbiGeoMapFeaturesId;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * @author giachino
 *
 */
public class SbiGeoMapFeaturesDAOHibImpl extends AbstractHibernateDAO implements ISbiGeoMapFeaturesDAO {
	
	/**
	 * Load feature names by map id.
	 * 
	 * @param mapId the map id
	 * 
	 * @return the list
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.mapcatalogue.dao.bo.dao.ISbiGeoMapFeaturesDAO#loadFeatureNamesByMapId(java.lang.Integer)
	 */
	public List loadFeatureNamesByMapId(Integer mapId) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		List realResult = new ArrayList();
		String hql = null;
		Query hqlQuery = null;
		
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			/*hql = " from SbiGeoMapFeatures as mf " + 
	         "where mf.id.mapId = " + mapId.toString();*/
			
			hql = " from SbiGeoMapFeatures as mf " + 
	         "where mf.id.mapId = ?" ;
			
			hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, mapId.intValue());
			List hibList = hqlQuery.list();
			
			Iterator it = hibList.iterator();
			ISbiGeoFeaturesDAO featuresDAO = DAOFactory.getSbiGeoFeaturesDAO();
			SbiGeoMapFeatures tmpMapFeature = null;
			GeoFeature tmpFeature = null;
			while (it.hasNext()) {			
				tmpMapFeature = (SbiGeoMapFeatures) it.next();
				SbiGeoMapFeaturesId tmpMapFeatureId = tmpMapFeature.getId();				
				tmpFeature = featuresDAO.loadFeatureByID(new Integer(tmpMapFeatureId.getFeatureId()));
		
				if (tmpFeature != null)
					realResult.add((String)tmpFeature.getName());
			}
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
		return realResult;
	}

	/**
	 * Load features by map id.
	 * 
	 * @param mapId the map id
	 * 
	 * @return the list
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.mapcatalogue.dao.bo.dao.ISbiGeoMapFeaturesDAO#loadFeaturesByMapId(java.lang.Integer)
	 */
	public List loadFeaturesByMapId(Integer mapId) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		List realResult = new ArrayList();
		String hql = null;
		Query hqlQuery = null;
		
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			/*hql = " from SbiGeoMapFeatures as mf " + 
	         "where mf.id.mapId = " + mapId.toString();*/
			
			hql = " from SbiGeoMapFeatures as mf " + 
	         "where mf.id.mapId = ?";
			
			hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, mapId.intValue());
			List hibList = hqlQuery.list();
			
			Iterator it = hibList.iterator();
			ISbiGeoFeaturesDAO featuresDAO = DAOFactory.getSbiGeoFeaturesDAO();
			SbiGeoMapFeatures tmpMapFeature = null;
			GeoFeature tmpFeature = null;
			while (it.hasNext()) {			
				tmpMapFeature = (SbiGeoMapFeatures) it.next();
				SbiGeoMapFeaturesId tmpMapFeatureId = tmpMapFeature.getId();				
				tmpFeature = featuresDAO.loadFeatureByID(new Integer(tmpMapFeatureId.getFeatureId()));
		
				if (tmpFeature != null)
					realResult.add((GeoFeature)tmpFeature);
			}
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
		return realResult;
	}

	/**
	 * Load map names by feature id.
	 * 
	 * @param featureId the feature id
	 * 
	 * @return the list
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.mapcatalogue.dao.bo.dao.ISbiGeoMapFeaturesDAO#loadMapNamesByFeatureId(java.lang.Integer)
	 */	
	public List loadMapNamesByFeatureId(Integer featureId) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		List realResult = new ArrayList();
		String hql = null;
		Query hqlQuery = null;
		
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			/*hql = " from SbiGeoMapFeatures as mf " + 
	         "where mf.id.featureId = " + featureId.toString();*/
			
			hql = " from SbiGeoMapFeatures as mf " + 
	         "where mf.id.featureId = ?" ;
			
			hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, featureId.intValue());
			List hibList = hqlQuery.list();
			
			Iterator it = hibList.iterator();
			ISbiGeoMapsDAO mapDAO = DAOFactory.getSbiGeoMapsDAO();
			SbiGeoMapFeatures tmpMapFeature = null;
			GeoMap tmpMap = null;
			while (it.hasNext()) {				
				tmpMapFeature = (SbiGeoMapFeatures) it.next();
				SbiGeoMapFeaturesId tmpMapFeatureId = tmpMapFeature.getId();				
				tmpMap = mapDAO.loadMapByID(new Integer(tmpMapFeatureId.getMapId()));
				if (tmpMap != null)
					realResult.add((String)tmpMap.getName());
			}
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
		return realResult;
	}

	/**
	 * Load maps by feature id.
	 * 
	 * @param featureId the feature id
	 * 
	 * @return the list
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.mapcatalogue.dao.bo.dao.ISbiGeoMapFeaturesDAO#loadMapsByFeatureId(java.lang.Integer)
	 */	
	public List loadMapsByFeatureId(Integer featureId) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		List realResult = new ArrayList();
		String hql = null;
		Query hqlQuery = null;
		
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			/*hql = " from SbiGeoMapFeatures as mf " + 
	         "where mf.id.featureId = " + featureId.toString();*/
			
			hql = " from SbiGeoMapFeatures as mf " + 
	         "where mf.id.featureId = ?" ;
			
			hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, featureId.intValue());
			List hibList = hqlQuery.list();
			
			Iterator it = hibList.iterator();
			ISbiGeoMapsDAO mapDAO = DAOFactory.getSbiGeoMapsDAO();
			SbiGeoMapFeatures tmpMapFeature = null;
			GeoMap tmpMap = null;
			while (it.hasNext()) {				
				tmpMapFeature = (SbiGeoMapFeatures) it.next();
				SbiGeoMapFeaturesId tmpMapFeatureId = tmpMapFeature.getId();				
				tmpMap = mapDAO.loadMapByID(new Integer(tmpMapFeatureId.getMapId()));
				if (tmpMap != null)
					realResult.add((GeoMap)tmpMap);
			}
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
		return realResult;
	}

	/**
	 * Load map features.
	 * 
	 * @param mapId the map id
	 * @param featureId the feature id
	 * 
	 * @return the geo map feature
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.mapcatalogue.dao.bo.dao.ISbiGeoMapFeaturesDAO#loadMapFeatures(java.lang.Integer, java.lang.Integer)
	 */
	public GeoMapFeature loadMapFeatures(Integer mapId, Integer featureId) throws EMFUserError {
		GeoMapFeature toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			
			/*String hql = "from SbiGeoMapFeatures s where s.id.mapId=" + mapId.toString() + 
			             " and s.id.featureId=" +  featureId.toString();*/
			
			String hql = "from SbiGeoMapFeatures s where s.id.mapId=?"  + 
            " and s.id.featureId=?" ;
			Query query = aSession.createQuery(hql);
			query.setInteger(0, mapId.intValue());
			query.setInteger(1, featureId.intValue());
			//toReturn =(GeoMapFeature) query.uniqueResult();			
			SbiGeoMapFeatures hibMapFeature = (SbiGeoMapFeatures)query.uniqueResult();
			if (hibMapFeature == null) return null;
			toReturn = toGeoMapFeature(hibMapFeature);
			
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
	 * Modify map features.
	 * 
	 * @param aMapFeature the a map feature
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.mapcatalogue.dao.geo.geo.bo.dao.ISbiGeoMapFeaturesDAO#modifySbiGeoMapFeatures(it.eng.spagobi.geo.bo.SbiGeoMapFeatures)
	 */
	public void modifyMapFeatures(GeoMapFeature aMapFeature) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiGeoMapFeaturesId hibMapFeatureId = new SbiGeoMapFeaturesId();
			hibMapFeatureId.setMapId(aMapFeature.getMapId());
			hibMapFeatureId.setFeatureId(aMapFeature.getFeatureId());

			SbiGeoMapFeatures hibFeature = (SbiGeoMapFeatures) aSession.load(SbiGeoMapFeatures.class, hibMapFeatureId);
			hibFeature.setSvgGroup(aMapFeature.getSvgGroup());
			hibFeature.setVisibleFlag(aMapFeature.getVisibleFlag());
			updateSbiCommonInfo4Update(hibFeature);
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
	 * Insert map features.
	 * 
	 * @param aMapFeature the a map feature
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.mapcatalogue.dao.geo.bo.dao.ISbiGeoMapFeaturesDAO#insertMapFeatures(it.eng.spagobi.geo.bo.SbiGeoMapFeatures)
	 */
	public void insertMapFeatures(GeoMapFeature aMapFeature) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiGeoMapFeatures hibMapFeature = new SbiGeoMapFeatures();	
			
			SbiGeoMapFeaturesId hibMapFeatureId = new SbiGeoMapFeaturesId();			
			hibMapFeatureId.setMapId(aMapFeature.getMapId());
			hibMapFeatureId.setFeatureId(aMapFeature.getFeatureId());
			hibMapFeature.setId(hibMapFeatureId);
			
			hibMapFeature.setSvgGroup(aMapFeature.getSvgGroup());
			hibMapFeature.setVisibleFlag(aMapFeature.getVisibleFlag());
			updateSbiCommonInfo4Insert(hibMapFeature);
			aSession.save(hibMapFeature);
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
	 * Erase map features.
	 * 
	 * @param aMapFeature the a map feature
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.mapcatalogue.dao.bo.dao.ISbiGeoMapFeaturesDAO#eraseMapFeatures(it.eng.spagobi.mapcatalogue.bo.GeoMapFeature)
	 */
	public void eraseMapFeatures(GeoMapFeature aMapFeature) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			
			SbiGeoMapFeaturesId hibMapFeatureId = new SbiGeoMapFeaturesId();			
			hibMapFeatureId.setMapId(aMapFeature.getMapId());
			hibMapFeatureId.setFeatureId(aMapFeature.getFeatureId());
			
			SbiGeoMapFeatures hibMapFeature = (SbiGeoMapFeatures) aSession.load(SbiGeoMapFeatures.class, hibMapFeatureId);

			aSession.delete(hibMapFeature);
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
	 * From the Hibernate MapFeature relation at input, gives the corrispondent
	 * <code>GeoMapFeature</code> object.
	 * 
	 * @param hibMapFeature The Hibernate Map object
	 * 
	 * @return the corrispondent output <code>GeoMap</code>
	 */
	public GeoMapFeature toGeoMapFeature(SbiGeoMapFeatures hibMapFeature){
		
		GeoMapFeature mapFeature = new GeoMapFeature();					
		
		mapFeature.setMapId(hibMapFeature.getId().getMapId());
		mapFeature.setFeatureId(hibMapFeature.getId().getFeatureId());
		mapFeature.setSvgGroup(hibMapFeature.getSvgGroup());
		mapFeature.setVisibleFlag(hibMapFeature.getVisibleFlag());
		
		/*List maps = new ArrayList();
	
		Set hibFeatures = hibMap.getSbiGeoMapFeatureses();			
		for (Iterator it = hibFeatures.iterator(); it.hasNext(); ) {
			SbiGeoMapFeatures hibMapFeatures = (SbiGeoMapFeatures) it.next();				
			Integer featureId = hibMapFeatures.getId().getFeatureId();				
			features.add(featureId);
		}
			
		mapFeature.setSbiGeoMaps(maps);
		
		List features = new ArrayList();
		
		Set hibFeatures = hibMap.getSbiGeoMapFeatureses();			
		for (Iterator it = hibFeatures.iterator(); it.hasNext(); ) {
			SbiGeoMapFeatures hibMapFeatures = (SbiGeoMapFeatures) it.next();				
			Integer featureId = hibMapFeatures.getId().getFeatureId();				
			features.add(featureId);
		}*/
		return mapFeature;
	}

}
