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
package it.eng.spagobi.tools.objmetadata.dao;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiSubObjects;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.metadata.SbiBinContents;
import it.eng.spagobi.tools.objmetadata.bo.ObjMetacontent;
import it.eng.spagobi.tools.objmetadata.metadata.SbiObjMetacontents;

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
 * Defines the Hibernate implementations for all DAO methods,
 * for a metadata content
 */
public class ObjMetacontentDAOHibImpl extends AbstractHibernateDAO implements IObjMetacontentDAO{

	static private Logger logger = Logger.getLogger(ObjMetacontentDAOHibImpl.class);

	/**
	 * Load object's metadata content by id.
	 * 
	 * @param id the identifier
	 * 
	 * @return the metadata content
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.tools.objmetadata.dao.IObjMetacontentDAO#loadObjMetaContentByID(java.lang.Integer)
	 */
	public ObjMetacontent loadObjMetaContentByID(Integer id) throws EMFUserError {		
		logger.debug("IN");

		ObjMetacontent toReturn = null;
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiObjMetacontents hibContent = (SbiObjMetacontents)aSession.load(SbiObjMetacontents.class, id);
			toReturn = toObjMetacontent(hibContent);
			tx.rollback();
		} catch (HibernateException he) {
			logger.error("Error while loading the metadata content with id = " + id, he);			
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen()) aSession.close();
			}
		}
		logger.debug("OUT");
		return toReturn;
	}

	/**
	 * Load object's metadata by objMetaId.
	 * 
	 * @param objMetaId the objMetaId
	 * 
	 * @return A list containing all metacontent of specific metadata
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.tools.objmetadata.dao.IObjMetacontentDAO#loadObjMetacontentByObjMetaId(java.lang.Integer)
	 */	
	public List loadObjMetacontentByObjMetaId(Integer objMetaId) throws EMFUserError {

		logger.debug("IN");
		List realResult = new ArrayList();
		Session session = null;
		Transaction tx = null;
		try {
			session = getSession();
			tx = session.beginTransaction();

			String hql = " from SbiObjMetacontents c where c.objmetaId = ?";
			Query aQuery = session.createQuery(hql);
			aQuery.setInteger(0, objMetaId.intValue());
			List hibList = aQuery.list();			
			if (hibList != null && !hibList.isEmpty()) {
				Iterator it = hibList.iterator();
				while (it.hasNext()) {
					realResult.add(toObjMetacontent((SbiObjMetacontents) it.next()));
				}
			}
			tx.rollback();
		} catch (HibernateException he) {
			logger.error("Error while loading the metadata content list with metadata id = " + objMetaId, he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (session!=null){
				if (session.isOpen()) session.close();
			}
		}
		logger.debug("OUT");
		return realResult;	

	}



	/**
	 * Loads all metacontent for one object or for one subobject, if biObjId is not null load all metacontents for object, if subObjId is not null load all metacontents for subobject 
	 * 
	 * @param biObjId The biObjId for the object to load
	 * @param subObjId The subObjId for the subObject to load
	 * 
	 * @return A list containing all metadata objects 
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */

	public List loadObjOrSubObjMetacontents(Integer biObjId, Integer subObjId) throws EMFUserError {

		logger.debug("IN");
		List realResult = new ArrayList();
		Session session = null;
		Transaction tx = null;
		Integer id = null;
		try {
			session = getSession();
			tx = session.beginTransaction();
			String hql = "";

			if(subObjId != null){
				logger.debug("laod metacontents associated to subbiobj "+subObjId);
				hql = " from SbiObjMetacontents c where c.sbiSubObjects.subObjId = ?";				
				id = subObjId;
			}
			else if(biObjId != null){
				logger.debug("laod metacontents associated to biobj "+biObjId);
				hql = " from SbiObjMetacontents c where c.sbiObjects.biobjId = ? AND c.sbiSubObjects.subObjId is null";
				id = biObjId;
			}

			Query aQuery = session.createQuery(hql);
			aQuery.setInteger(0, id.intValue());
			List hibList = aQuery.list();			
			if (hibList != null && !hibList.isEmpty()) {
				Iterator it = hibList.iterator();
				while (it.hasNext()) {
					realResult.add(toObjMetacontent((SbiObjMetacontents) it.next()));
				}
			}
			tx.rollback();
		} catch (HibernateException he) {
			logger.error("Error while loading the metadata content referring to object or subobject (check log before) with id = " + id, he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (session!=null){
				if (session.isOpen()) session.close();
			}
		}
		logger.debug("OUT");
		return realResult;	

	}











	/**
	 * Load object's metadata by objMetaId, biObjId and subobjId.
	 * 
	 * @param objMetaId the objMetaId
	 * @param biObjId the biObjId
	 * @param subObjId the subObjId
	 * 
	 * @return A list containing all metadata contents objects of a specific subObjId
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.tools.objmetadata.dao.IObjMetacontentDAO#loadObjMetacontentByObjId(java.lang.Integer, java.lang.Integer)
	 */	
	public ObjMetacontent loadObjMetacontent(Integer objMetaId, Integer biObjId, Integer subObjId) throws EMFUserError{
		logger.debug("IN");
		ObjMetacontent realResult = null;
		Session session = null;
		Transaction tx = null;
		try {
			session = getSession();
			tx = session.beginTransaction();

			String hql = " from SbiObjMetacontents c where c.objmetaId = ? and c.sbiObjects.biobjId = ? ";
			if(subObjId!=null){
				hql += "and c.sbiSubObjects.subObjId = ? ";
			}else{
				hql += "and c.sbiSubObjects.subObjId IS NULL ";
			}
			Query aQuery = session.createQuery(hql);
			aQuery.setInteger(0, objMetaId.intValue());
			aQuery.setInteger(1, biObjId.intValue());
			if(subObjId!=null){
				aQuery.setInteger(2, subObjId.intValue());
			}
			SbiObjMetacontents res =(SbiObjMetacontents) aQuery.uniqueResult();
			if(res!=null){
				realResult = toObjMetacontent(res);
			}
			tx.rollback();
		} catch (HibernateException he) {
			logger.error("Error while loading the metadata content with metadata id = " + objMetaId + 
					", biobject id = " + biObjId + ", subobject id = " + subObjId, he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (session!=null){
				if (session.isOpen()) session.close();
			}
		}
		logger.debug("OUT");
		return realResult;	

	}
	/**
	 * Load all metadata content.
	 * 
	 * @return the list
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.tools.objmetadata.dao.IObjMetacontentDAO#loadAllObjMetacontent()
	 */
	public List loadAllObjMetacontent() throws EMFUserError {

		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List realResult = new ArrayList();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = aSession.createQuery(" from SbiObjMetacontents");

			List hibList = hibQuery.list();
			if (hibList != null && !hibList.isEmpty()) {
				Iterator it = hibList.iterator();
				while (it.hasNext()) {
					realResult.add(toObjMetacontent((SbiObjMetacontents) it.next()));
				}
			}
			tx.rollback();
		} catch (HibernateException he) {
			logger.error("Error while loading all meta contents ", he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();				
			}
		}
		logger.debug("OUT");
		return realResult;
	}

	/**
	 * Modify metadata content.
	 * 
	 * @param aObjMetacontent the meta content
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.tools.objmetadata.dao.IObjMetadataDAO#modifyObjMetacontent(it.eng.spagobi.tools.objmetadata.bo.ObjMetacontent)
	 */
	public void modifyObjMetacontent(ObjMetacontent aObjMetacontent) throws EMFUserError {

		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		Criterion aCriterion = null;
		Criteria criteria = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiObjMetacontents hibContents = (SbiObjMetacontents) aSession.load(SbiObjMetacontents.class,
					new Integer(aObjMetacontent.getObjMetacontentId()));	

			// update biobject reference
			if (hibContents.getSbiObjects().getBiobjId() != aObjMetacontent.getBiobjId()) {
				aCriterion = Expression.eq("biobjId", aObjMetacontent.getBiobjId());
				criteria = aSession.createCriteria(SbiObjects.class);
				criteria.add(aCriterion);
				SbiObjects biobj = (SbiObjects) criteria.uniqueResult();
				hibContents.setSbiObjects(biobj);
			}

			// update subobject reference
			if (aObjMetacontent.getSubobjId() == null) {
				hibContents.setSbiSubObjects(null);
			} else {
				SbiSubObjects previousSubobject = hibContents.getSbiSubObjects();
				if (previousSubobject == null || previousSubobject.getSubObjId() != aObjMetacontent.getSubobjId()) {
					aCriterion = Expression.eq("subObjId", aObjMetacontent.getSubobjId());
					criteria = aSession.createCriteria(SbiSubObjects.class);
					criteria.add(aCriterion);
					SbiSubObjects subobj = (SbiSubObjects) criteria.uniqueResult();
					hibContents.setSbiSubObjects(subobj);
				}
			}
			updateSbiCommonInfo4Update(hibContents);
			// update content
			SbiBinContents binaryContent = hibContents.getSbiBinContents();
			if (binaryContent == null) {
				binaryContent = new SbiBinContents();
				binaryContent.setContent(aObjMetacontent.getContent());
			} else {
				binaryContent.setContent(aObjMetacontent.getContent());
			}
			updateSbiCommonInfo4Insert(binaryContent);
			aSession.save(binaryContent);
			hibContents.setSbiBinContents(binaryContent);

			// update metadata reference
			hibContents.setObjmetaId(aObjMetacontent.getObjmetaId());

			// update last change date
			hibContents.setLastChangeDate(aObjMetacontent.getLastChangeDate());

			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while modifing the meta content with id " + ((aObjMetacontent == null)?"":String.valueOf(aObjMetacontent.getObjMetacontentId())), he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}
		logger.debug("OUT");

	}

	/**
	 * Insert object's metadata content.
	 * 
	 * @param aObjMetacontent the metadata content
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.tools.objmetadata.dao.IObjMetadataDAO#insertObjMetacontent(it.eng.spagobi.tools.objmetadata.bo.ObjMetacontent)
	 */
	public void insertObjMetacontent(ObjMetacontent aObjMetacontent) throws EMFUserError {

		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		Criterion aCriterion = null;
		Criteria criteria = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiObjMetacontents hibContents = new SbiObjMetacontents();	

			// get biobject reference		
			aCriterion = Expression.eq("biobjId", aObjMetacontent.getBiobjId());
			criteria = aSession.createCriteria(SbiObjects.class);
			criteria.add(aCriterion);
			SbiObjects biobj = (SbiObjects) criteria.uniqueResult();
			hibContents.setSbiObjects(biobj);

			// get subobject reference
			if (aObjMetacontent.getSubobjId() == null) {
				hibContents.setSbiSubObjects(null);
			} else {
				aCriterion = Expression.eq("subObjId", aObjMetacontent.getSubobjId());
				criteria = aSession.createCriteria(SbiSubObjects.class);
				criteria.add(aCriterion);
				SbiSubObjects subobj = (SbiSubObjects) criteria.uniqueResult();
				hibContents.setSbiSubObjects(subobj);
			}

			SbiBinContents binaryContent = new SbiBinContents();
			binaryContent.setContent(aObjMetacontent.getContent());
			updateSbiCommonInfo4Insert(binaryContent);
			aSession.save(binaryContent);
			hibContents.setSbiBinContents(binaryContent);

			hibContents.setObjmetaId(aObjMetacontent.getObjmetaId());

			hibContents.setCreationDate(aObjMetacontent.getCreationDate());;
			hibContents.setLastChangeDate(aObjMetacontent.getLastChangeDate());
			updateSbiCommonInfo4Insert(hibContents);
			aSession.save(hibContents);
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while inserting the metadata content with id " + ((aObjMetacontent == null)?"":String.valueOf(aObjMetacontent.getObjMetacontentId())), he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
				logger.debug("OUT");
			}
		}

	}

	/**
	 * Erase object's metadata content
	 * 
	 * @param ObjMetacontent the metadata content
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.tools.objmetadata.dao.IObjMetadataDAO#eraseObjMetadata(it.eng.spagobi.tools.objmetadata.bo.ObjMetacontent)
	 */
	public void eraseObjMetadata(ObjMetacontent aObjMetacontent) throws EMFUserError{
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiObjMetacontents hibContents = (SbiObjMetacontents) aSession.load(SbiObjMetacontents.class,
					new Integer(aObjMetacontent.getObjMetacontentId()));

			aSession.delete(hibContents);
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while erasing the data source with id " + ((aObjMetacontent == null)?"":String.valueOf(aObjMetacontent.getObjMetacontentId())), he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
				logger.debug("OUT");
			}
		}

	}

	/**
	 * From the hibernate SbiObjMetacontent at input, gives
	 * the corrispondent <code>ObjMetacontent</code> object.
	 * 
	 * @param hibObjMetadata The hybernate metadata content
	 * 
	 * @return The corrispondent <code>ObjMetacontent</code> object
	 */
	private ObjMetacontent toObjMetacontent(SbiObjMetacontents hibObjMetacontent){
		ObjMetacontent meta = new ObjMetacontent();

		meta.setObjMetacontentId(hibObjMetacontent.getObjMetacontentId());
		meta.setObjmetaId(hibObjMetacontent.getObjmetaId());
		meta.setBiobjId(hibObjMetacontent.getSbiObjects().getBiobjId());
		if(hibObjMetacontent.getSbiSubObjects() != null){
			meta.setSubobjId(hibObjMetacontent.getSbiSubObjects().getSubObjId());
		}		
		meta.setBinaryContentId(hibObjMetacontent.getSbiBinContents().getId());
		meta.setContent(hibObjMetacontent.getSbiBinContents().getContent()); 
		meta.setCreationDate(hibObjMetacontent.getCreationDate());
		meta.setLastChangeDate(hibObjMetacontent.getLastChangeDate());

		return meta;
	}

}


