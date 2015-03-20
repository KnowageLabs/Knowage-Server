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
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.kpi.config.metadata.SbiKpiValue;
import it.eng.spagobi.tools.objmetadata.bo.ObjMetacontent;
import it.eng.spagobi.tools.objmetadata.bo.ObjMetadata;
import it.eng.spagobi.tools.objmetadata.metadata.SbiObjMetadata;

import java.util.ArrayList;
import java.util.Date;
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
import org.hibernate.criterion.Order;


/**
 * Defines the Hibernate implementations for all DAO methods,
 * for a object metadata.
 */
public class ObjMetadataDAOHibImpl extends AbstractHibernateDAO implements IObjMetadataDAO{
	static private Logger logger = Logger.getLogger(ObjMetadataDAOHibImpl.class);
	
	/**
	 * Load object's metadata by type
	 * 
	 * @param type the type(SHORT_TEXT or LONG_TEXT)
	 * 
	 * @return the metadata
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public List loadObjMetaDataListByType(String type) throws EMFUserError{
		logger.debug("IN");
		List toReturn = new ArrayList();
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Query hibQuery = aSession.createQuery(" from SbiObjMetadata meta where meta.dataType.valueCd = ? and meta.dataType.domainCd='OBJMETA_DATA_TYPE'" );
			hibQuery.setString(0, type);
			
			logger.debug("Type setted: "+(type!=null?type:""));

			List hibList = hibQuery.list();
			if(hibList!=null && !hibList.isEmpty()){
				Iterator it = hibList.iterator();
	
				while (it.hasNext()) {
					toReturn.add(toObjMetadata((SbiObjMetadata) it.next()));
				}
			}
			
		} catch (HibernateException he) {
			logger.error("Error while loading the metadata with type " + (type!=null?type:""), he);			

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
				logger.debug("OUT");
			}
		}
		logger.debug("OUT");
		return toReturn;
	}
	
	/**
	 * Load object's metadata by id.
	 * 
	 * @param id the identifier
	 * 
	 * @return the metadata
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.tools.objmetadata.dao.IObjMetadataDAO#loadObjMetaDataByID(java.lang.Integer)
	 */
	public ObjMetadata loadObjMetaDataByID(Integer id) throws EMFUserError {
		
		logger.debug("IN");
		ObjMetadata toReturn = null;
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiObjMetadata hibDataSource = (SbiObjMetadata)aSession.load(SbiObjMetadata.class,  id);
			toReturn = toObjMetadata(hibDataSource);
			tx.commit();
			
		} catch (HibernateException he) {
			logger.error("Error while loading the metadata with id " + id.toString(), he);			

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
				logger.debug("OUT");
			}
		}
		logger.debug("OUT");
		return toReturn;
	}

	/**
	 * Load object's metadata by label.
	 * 
	 * @param label the label
	 * 
	 * @return the metadata
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.tools.objmetadata.dao.IObjMetadataDAO#loadObjMetadataByLabel(java.lang.String)
	 */	
	public ObjMetadata loadObjMetadataByLabel(String label) throws EMFUserError {
		
		logger.debug("IN");
		ObjMetadata toReturn = null;
		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			Criterion labelCriterrion = Expression.eq("label", label);
			Criteria criteria = tmpSession.createCriteria(SbiObjMetadata.class);
			criteria.add(labelCriterrion);	
			SbiObjMetadata hibMeta = (SbiObjMetadata) criteria.uniqueResult();
			if (hibMeta == null) return null;
			toReturn = toObjMetadata(hibMeta);				
			
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while loading the metadata with label " + label, he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (tmpSession!=null){
				if (tmpSession.isOpen()) tmpSession.close();
			}
		}
		logger.debug("OUT");
		return toReturn;	
		
	}

	/**
	 * Load all metadata.
	 * 
	 * @return the list
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.tools.objmetadata.dao.IObjMetadataDAO#loadAllObjMetadata()
	 */
	public List loadAllObjMetadata() throws EMFUserError {
		logger.debug("IN");
		
		Session aSession = null;
		Transaction tx = null;
		List realResult = new ArrayList();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = aSession.createQuery(" from SbiObjMetadata");
			
			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();

			while (it.hasNext()) {
				realResult.add(toObjMetadata((SbiObjMetadata) it.next()));
			}
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while loading all metadata ", he);

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
	 * Modify metadata.
	 * 
	 * @param aObjMetadata the metadata
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.tools.objmetadata.dao.IObjMetadataDAO#modifyObjMetadata(it.eng.spagobi.tools.objmetadata.bo.ObjMetadata)
	 */
	public void modifyObjMetadata(ObjMetadata aObjMetadata) throws EMFUserError {		
		logger.debug("IN");
		
		Session aSession = null;
		Transaction tx = null;
		try {			
			aSession = getSession();
			tx = aSession.beginTransaction();
			Criterion aCriterion = Expression.eq("valueId",	aObjMetadata.getDataType());
			Criteria criteria = aSession.createCriteria(SbiDomains.class);
			criteria.add(aCriterion);

			SbiDomains dataType = (SbiDomains) criteria.uniqueResult();

			if (dataType == null){
				logger.error("The Domain with value_id= "+aObjMetadata.getDataType()+" does not exist.");
				throw new EMFUserError(EMFErrorSeverity.ERROR, 1035);
			}
			
			SbiObjMetadata hibMeta = (SbiObjMetadata)aSession.load(SbiObjMetadata.class, aObjMetadata.getObjMetaId());
			hibMeta.setLabel(aObjMetadata.getLabel());
			hibMeta.setName(aObjMetadata.getName());
			hibMeta.setDescription(aObjMetadata.getDescription());
			hibMeta.setDataType(dataType);
			updateSbiCommonInfo4Update(hibMeta);
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
			logger.debug("OUT");
		}
	
	}

	/**
	 * Insert object's metadata.
	 * 
	 * @param aObjMetadata the metadata
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.tools.objmetadata.dao.IObjMetadataDAO#insertObjMetadata(it.eng.spagobi.tools.objmetadata.bo.ObjMetadata)
	 */
	public void insertObjMetadata(ObjMetadata aObjMetadata) throws EMFUserError {
		
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			
			Criterion aCriterion = Expression.eq("valueId",	aObjMetadata.getDataType());
			Criteria criteria = aSession.createCriteria(SbiDomains.class);
			criteria.add(aCriterion);

			SbiDomains dataType = (SbiDomains) criteria.uniqueResult();

			if (dataType == null){
				logger.error("The Domain with value_id= "+aObjMetadata.getDataType()+" does not exist.");
				throw new EMFUserError(EMFErrorSeverity.ERROR, 1035);
			}
			Date now = new Date();
			// store the object note
			SbiObjMetadata hibMeta = new SbiObjMetadata();
			hibMeta.setLabel(aObjMetadata.getLabel());
			hibMeta.setName(aObjMetadata.getName());
			hibMeta.setDescription(aObjMetadata.getDescription());
			hibMeta.setDataType(dataType);
			hibMeta.setCreationDate(now);
			updateSbiCommonInfo4Insert(hibMeta);
			aSession.save(hibMeta);
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
			logger.debug("OUT");
		}	
	}

	/**
	 * Erase object's metadata
	 * 
	 * @param aObjMetadata the metadata
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.tools.objmetadata.dao.IObjMetadataDAO#eraseObjMetadata(it.eng.spagobi.tools.objmetadata.bo.ObjMetadata)
	 */
	public void eraseObjMetadata(ObjMetadata aObjMetadata) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiObjMetadata hibMeta = (SbiObjMetadata) aSession.load(SbiObjMetadata.class,
					new Integer(aObjMetadata.getObjMetaId()));
			
			//delete metadatacontents eventually associated
			List metaContents = DAOFactory.getObjMetacontentDAO().loadAllObjMetacontent();
			IObjMetacontentDAO objMetaContentDAO = DAOFactory.getObjMetacontentDAO();
			if (metaContents != null && !metaContents.isEmpty()) {
				Iterator it = metaContents.iterator();
				while (it.hasNext()) {
					ObjMetacontent objMetadataCont = (ObjMetacontent) it.next();
					if(objMetadataCont!=null && objMetadataCont.getObjmetaId().equals(hibMeta.getObjMetaId())){
						objMetaContentDAO.eraseObjMetadata(objMetadataCont);
					}
				}
			}			

			aSession.delete(hibMeta);
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while erasing the metadata with id " + ((aObjMetadata == null)?"":String.valueOf(aObjMetadata.getObjMetaId())), he);

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
	 * Checks for bi obj associated.
	 * 
	 * @param id the metadata id
	 * 
	 * @return true, if checks for bi obj associated
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.tools.objmetadata.dao.IObjMetadataDAO#hasBIObjAssociated(java.lang.String)
	 */
	public boolean hasBIObjAssociated (String id) throws EMFUserError{
		
		logger.debug("IN");		
		boolean bool = false; 
		
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Integer idInt = Integer.valueOf(id);
			
			String hql = " from SbiObjMetacontents c where c.objmetaId = ? and c.sbiObjects is not null";
			Query aQuery = aSession.createQuery(hql);
			aQuery.setInteger(0, idInt.intValue());
			List biObjectsAssocitedWithObj = aQuery.list();
			if (biObjectsAssocitedWithObj.size() > 0)
				bool = true;
			else
				bool = false;
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while getting the objects associated with the metadata with id " + id, he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}
	
		logger.debug("OUT");
		return bool;
		
	}
	
	/**
	 * Checks for bi subobject associated.
	 * 
	 * @param id the metadata id
	 * 
	 * @return true, if checks for bi subobjects associated
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.tools.objmetadata.dao.IObjMetadataDAO#hasSubObjAssociated(java.lang.String)
	 */
	public boolean hasSubObjAssociated (String id) throws EMFUserError{
		logger.debug("IN");
		boolean bool = false; 
		
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Integer idInt = Integer.valueOf(id);
			
			String hql = " from SbiObjMetacontents c where c.objmetaId = ? and c.sbiSubObjects is not null";
			Query aQuery = aSession.createQuery(hql);
			aQuery.setInteger(0, idInt.intValue());
			List biObjectsAssocitedWithSubobj = aQuery.list();
			if (biObjectsAssocitedWithSubobj.size() > 0)
				bool = true;
			else
				bool = false;
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while getting the engines associated with the data source with id " + id, he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}
	
		logger.debug("OUT");
		return bool;
		
	}
	
	/**
	 * From the hibernate SbiObjMetadata at input, gives
	 * the corrispondent <code>ObjMetadata</code> object.
	 * 
	 * @param hibObjMetadata The hybernate metadata
	 * 
	 * @return The corrispondent <code>ObjMetadata</code> object
	 */
	private ObjMetadata toObjMetadata(SbiObjMetadata hibObjMetadata){
		ObjMetadata meta = new ObjMetadata();
	
		meta.setObjMetaId(hibObjMetadata.getObjMetaId());
		meta.setLabel(hibObjMetadata.getLabel());
		meta.setName(hibObjMetadata.getName());
		meta.setDescription(hibObjMetadata.getDescription());
		meta.setDataType(hibObjMetadata.getDataType().getValueId());
		meta.setDataTypeCode(hibObjMetadata.getDataType().getValueCd());
		meta.setCreationDate(hibObjMetadata.getCreationDate());
		
		return meta;
	}
}


