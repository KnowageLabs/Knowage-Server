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
package it.eng.spagobi.engines.config.dao;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.engines.config.bo.Exporters;
import it.eng.spagobi.engines.config.metadata.SbiEngines;
import it.eng.spagobi.engines.config.metadata.SbiExporters;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
 * for an engine.
 * 
 * @author zoppello
 */
public class EngineDAOHibImpl extends AbstractHibernateDAO implements IEngineDAO{

	
	private static transient Logger logger=Logger.getLogger(EngineDAOHibImpl.class);
	
	/**
	 * Load engine by id.
	 * 
	 * @param engineID the engine id
	 * 
	 * @return the engine
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.engines.config.dao.IEngineDAO#loadEngineByID(java.lang.Integer)
	 */
	public Engine loadEngineByID(Integer engineID) throws EMFUserError {
		logger.debug("IN");
		Engine toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		logger.debug("engine Id is "+engineID);
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiEngines hibEngine = (SbiEngines)aSession.load(SbiEngines.class,  engineID);
			logger.debug("hib engine loaded");
			toReturn = toEngine(hibEngine);
			tx.commit();

		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();
			logger.error("error in loading engine by Id", he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}
		logger.debug("OUT");
		return toReturn;
	}



	/**
	 * Load engine by label.
	 * 
	 * @param engineLabel the engine label
	 * 
	 * @return the engine
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.engines.config.dao.IEngineDAO#loadEngineByID(java.lang.Integer)
	 */


	public Engine loadEngineByLabel(String engineLabel) throws EMFUserError {
		logger.debug("IN");
		Engine engine = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			logger.debug("engine label is "+engineLabel);
			aSession = getSession();
			tx = aSession.beginTransaction();
//			Criterion labelCriterrion = Expression.eq("label",
//					engineLabel);
//			Criteria criteria = aSession.createCriteria(SbiEngines.class);
//			criteria.add(labelCriterrion);
//			SbiEngines hibEngine = (SbiEngines) criteria.uniqueResult();
			Query hibQuery = aSession.createQuery("select oe.sbiEngines from SbiOrganizationEngine oe "
					+ "where oe.sbiEngines.label = ? and oe.sbiOrganizations.name = ?" );
			hibQuery.setString(0, engineLabel);
			hibQuery.setString(1, getTenant());
			SbiEngines hibEngine = (SbiEngines) hibQuery.uniqueResult();
			
			if (hibEngine == null) {
				logger.error("A null engine has been returned for label"+engineLabel);
				return null;
			}
			engine = toEngine(hibEngine);
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error in retrieving engine by label "+engineLabel, he);

			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}
		logger.debug("OUT");
		return engine;
	}


	/**
	 * Load engine by driver name.
	 * 
	 * @param engineLabel the driver name
	 * 
	 * @return the engine
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.engines.config.dao.IEngineDAO#loadEngineByID(java.lang.Integer)
	 */


	public Engine loadEngineByDriver(String driver) throws EMFUserError {
		logger.debug("IN");
		Engine engine = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			logger.debug("engine driver is "+driver);
			aSession = getSession();
			tx = aSession.beginTransaction();
			
//			Criterion labelCriterrion = Expression.eq("driverNm",
//										driver);
//			Criteria criteria = aSession.createCriteria(SbiEngines.class);
//			criteria.add(labelCriterrion);
//			SbiEngines hibEngine = (SbiEngines) criteria.uniqueResult();
			
			Query hibQuery = aSession.createQuery("select oe.sbiEngines from SbiOrganizationEngine oe "
					+ "where oe.sbiEngines.driverNm = ? and oe.sbiOrganizations.name = ?" );
			hibQuery.setString(0, driver);
			hibQuery.setString(1, getTenant());

			SbiEngines hibEngine = (SbiEngines) hibQuery.uniqueResult();
			
			if (hibEngine == null) {
				logger.error("No engine with driver [" + driver + "] was found.");
				return null;
			}
			engine = toEngine(hibEngine);
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error in retrieving engine by label "+driver, he);

			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}
		logger.debug("OUT");
		return engine;
	}

	/**
	 * Load all engines.
	 * 
	 * @return the list
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.engines.config.dao.IEngineDAO#loadAllEngines()
	 */
	public List loadAllEngines() throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List realResult = new ArrayList();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = aSession.createQuery(" from SbiEngines");

			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();

			while (it.hasNext()) {
				realResult.add(toEngine((SbiEngines) it.next()));
			}
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			logger.error("Error in loading all engines", he);
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
	 * Load paged engines list
	 * 
	 * @return the list
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.engines.config.dao.IEngineDAO#loadAllEngines()
	 */
	public List loadPagedEnginesList(Integer offset, Integer fetchSize) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List realResult = new ArrayList();
		try {
			
			if(offset == null) {
				logger.warn("Input parameter [offset] is null. It will be set to [0]");
				offset = new Integer(0);
			}
			if(fetchSize == null) {
				logger.warn("Input parameter [offset] is null. It will be set to [" + Integer.MAX_VALUE + "]");
				fetchSize = Integer.MAX_VALUE;
			}
			
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query countQuery = aSession.createQuery("select count(*) from SbiEngines sb ");
			Long resultNumber = (Long)countQuery.uniqueResult();

			offset = offset < 0 ? 0 : offset;
			if(resultNumber > 0) {
				fetchSize = (fetchSize > 0) ? 
						Math.min(fetchSize, resultNumber.intValue()) 
						: resultNumber.intValue();
			}
			
			Query listQuery = aSession.createQuery("from SbiEngines h order by h.label " );
			listQuery.setFirstResult(offset);
			if(fetchSize > 0) listQuery.setMaxResults(fetchSize);			

			List enginesList = listQuery.list();	

			Iterator it = enginesList.iterator();

			while (it.hasNext()) {
				realResult.add(toEngine((SbiEngines) it.next()));
			}
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			logger.error("Error in loading all engines", he);
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
	
	
	

	public List<Engine> loadAllEnginesByTenant() throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List realResult = new ArrayList();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = aSession.createQuery("select oe.sbiEngines from SbiOrganizationEngine oe "
					+ "where oe.sbiOrganizations.name = ?" );
			hibQuery.setString(0, getTenant());
			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();

			while (it.hasNext()) {
				realResult.add(toEngine((SbiEngines) it.next()));
			}
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			logger.error("Error in loading all engines", he);
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
	 * Load all engines for bi object type.
	 * 
	 * @param biobjectType the biobject type
	 * 
	 * @return the list
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.engines.config.dao.IEngineDAO#loadAllEnginesForBIObjectType(java.lang.String)
	 */
	public List<Engine> loadAllEnginesForBIObjectType(String biobjectType) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;

		logger.debug("BiObject Type is "+biobjectType);
		List<Engine> realResult = new ArrayList<Engine>();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = aSession.createQuery(" from SbiEngines engines where engines.biobjType.valueCd = ?" );
			hibQuery.setString(0, biobjectType);
			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();

			while (it.hasNext()) {
				realResult.add(toEngine((SbiEngines) it.next()));
			}
		} catch (HibernateException he) {
			logger.debug("Error in loading ecgines for biObject Type "+biobjectType, he);
			logException(he);

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

	public List<Engine> loadAllEnginesForBIObjectTypeAndTenant(
			String biobjectType) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;

		logger.debug("BiObject Type is "+biobjectType);
		List<Engine> realResult = new ArrayList<Engine>();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = aSession.createQuery("select oe.sbiEngines from SbiOrganizationEngine oe "
					+ "where oe.sbiOrganizations.name = ? and oe.sbiEngines.biobjType.valueCd = ?" );
			hibQuery.setString(0, getTenant());
			hibQuery.setString(1, biobjectType);
			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();

			while (it.hasNext()) {
				realResult.add(toEngine((SbiEngines) it.next()));
			}
		} catch (HibernateException he) {
			logger.debug("Error in loading ecgines for biObject Type "+biobjectType, he);
			logException(he);

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
	 * Modify engine.
	 * 
	 * @param aEngine the a engine
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.engines.config.dao.IEngineDAO#modifyEngine(it.eng.spagobi.engines.config.bo.Engine)
	 */
	public void modifyEngine(Engine aEngine) throws EMFUserError {
		logger.debug("IN"); 

		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiEngines hibEngine = (SbiEngines) aSession.load(SbiEngines.class,
					aEngine.getId());
			SbiDomains hibDomainBiobjType = (SbiDomains) aSession.load(SbiDomains.class,
					aEngine.getBiobjTypeId());
			SbiDomains hibDomainEngineType = (SbiDomains) aSession.load(SbiDomains.class,
					aEngine.getEngineTypeId());

			hibEngine.setName(aEngine.getName());
			hibEngine.setLabel(aEngine.getLabel());
			hibEngine.setDescr(aEngine.getDescription());
			hibEngine.setDriverNm(aEngine.getDriverName());
			hibEngine.setEncrypt(new Short((short) aEngine.getCriptable()
					.intValue()));
			hibEngine.setMainUrl(aEngine.getUrl());
			hibEngine.setObjUplDir(aEngine.getDirUpload());
			hibEngine.setObjUseDir(aEngine.getDirUsable());
			hibEngine.setSecnUrl(aEngine.getSecondaryUrl());
			hibEngine.setEngineType(hibDomainEngineType);
			hibEngine.setClassNm(aEngine.getClassName());
			hibEngine.setBiobjType(hibDomainBiobjType);
			hibEngine.setUseDataSet(new Boolean(aEngine.getUseDataSet()));			
			hibEngine.setUseDataSource(new Boolean(aEngine.getUseDataSource()));						
			updateSbiCommonInfo4Update(hibEngine, true);
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			logger.error("Error in modifying engine ",he);
			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}
		logger.debug("IN");

	}

	/**
	 * Insert engine.
	 * 
	 * @param aEngine the a engine
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.engines.config.dao.IEngineDAO#insertEngine(it.eng.spagobi.engines.config.bo.Engine)
	 */
	public void insertEngine(Engine aEngine) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiDomains hibDomainBiobjType = (SbiDomains) aSession.load(SbiDomains.class,
					aEngine.getBiobjTypeId());
			SbiDomains hibDomainEngineType = (SbiDomains) aSession.load(SbiDomains.class,
					aEngine.getEngineTypeId());
			SbiEngines hibEngine = new SbiEngines();
			hibEngine.setName(aEngine.getName());
			hibEngine.setLabel(aEngine.getLabel());
			hibEngine.setDescr(aEngine.getDescription());
			hibEngine.setDriverNm(aEngine.getDriverName());
			hibEngine.setEncrypt(new Short((short) aEngine.getCriptable()
					.intValue()));
			hibEngine.setMainUrl(aEngine.getUrl());
			hibEngine.setObjUplDir(aEngine.getDirUpload());
			hibEngine.setObjUseDir(aEngine.getDirUsable());
			hibEngine.setSecnUrl(aEngine.getSecondaryUrl());
			hibEngine.setEngineType(hibDomainEngineType);
			hibEngine.setClassNm(aEngine.getClassName());
			hibEngine.setBiobjType(hibDomainBiobjType);
			hibEngine.setUseDataSet(new Boolean(aEngine.getUseDataSet()));
			hibEngine.setUseDataSource(new Boolean(aEngine.getUseDataSource()));
			updateSbiCommonInfo4Insert(hibEngine,true);
			aSession.save(hibEngine);
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			logger.error("Inserting new engine ",he);
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
	 * Erase engine.
	 * 
	 * @param aEngine the a engine
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.engines.config.dao.IEngineDAO#eraseEngine(it.eng.spagobi.engines.config.bo.Engine)
	 */
	public void eraseEngine(Engine aEngine) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiEngines hibEngine = (SbiEngines) aSession.load(SbiEngines.class,
					aEngine.getId());
			Set <SbiExporters> exporters = hibEngine.getSbiExporterses();
			if(exporters != null && exporters.size() != 0){
				//query hsql to load exporters
				String hql = " from SbiExporters s where s.sbiEngines.engineId = ?";
				Query aQuery = aSession.createQuery(hql);
				aQuery.setInteger(0, aEngine.getId().intValue());
				List associatedExportersToDelete = aQuery.list();
				for(int i=0; i<associatedExportersToDelete.size(); i++){
					SbiExporters exporter = (SbiExporters)associatedExportersToDelete.get(i);
					aSession.delete(exporter);
					aSession.flush();
				}
			}
			aSession.delete(hibEngine);
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			logger.error("Error in erasing engine ",he);

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
	 * From the hibernate Engine at input, gives
	 * the corrispondent <code>Engine</code> object.
	 * 
	 * @param hibEngine The hybernate engine
	 * 
	 * @return The corrispondent <code>Engine</code> object
	 */
	public Engine toEngine(SbiEngines hibEngine){
		logger.debug("IN");
		if(hibEngine!= null)logger.debug("Label is "+hibEngine.getLabel());
		Engine eng = new Engine();
		eng.setCriptable(new Integer(hibEngine.getEncrypt().intValue()));
		eng.setDescription(hibEngine.getDescr());
		eng.setDirUpload(hibEngine.getObjUplDir());
		eng.setDirUsable(hibEngine.getObjUseDir());
		eng.setDriverName(hibEngine.getDriverNm());
		eng.setId(hibEngine.getEngineId());
		eng.setName(hibEngine.getName());
		eng.setLabel(hibEngine.getLabel());
		eng.setUseDataSet(hibEngine.getUseDataSet().booleanValue());
		eng.setUseDataSource(hibEngine.getUseDataSource().booleanValue());		
		eng.setSecondaryUrl(hibEngine.getSecnUrl());
		eng.setUrl(hibEngine.getMainUrl());
		eng.setLabel(hibEngine.getLabel());
		eng.setEngineTypeId(hibEngine.getEngineType().getValueId());
		eng.setClassName(hibEngine.getClassNm());
		eng.setBiobjTypeId(hibEngine.getBiobjType().getValueId());
		logger.debug("OUT");

		return eng;
	}





	/**
	 * From the hibernate Exporter at input, gives
	 * the corrispondent <code>Engine</code> object.
	 * 
	 * @param hibEngine The hybernate engine
	 * 
	 * @return The corrispondent <code>Engine</code> object
	 */
	public Exporters toExporter(SbiExporters hibExps){
		logger.debug("IN");
		Exporters exp = new Exporters();

		SbiEngines hibEngine=hibExps.getSbiEngines();
		exp.setEngineId(hibEngine.getEngineId());

		SbiDomains hibDomains=hibExps.getSbiDomains();
		exp.setDomainId(hibDomains.getValueId());

		exp.setDefaultValue(hibExps.isDefaultValue());
		logger.debug("OUT");
		return exp;
	}




	/**
	 * Checks for bi obj associated.
	 * 
	 * @param engineId the engine id
	 * 
	 * @return true, if checks for bi obj associated
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.engines.config.dao.IEngineDAO#hasBIObjAssociated(java.lang.String)
	 */
	public boolean hasBIObjAssociated (String engineId) throws EMFUserError{
		/**
		 * TODO Hibernate Implementation
		 */
		boolean bool = false; 
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Integer engineIdInt = Integer.valueOf(engineId);

			//String hql = " from SbiObjects s where s.sbiEngines.engineId = "+ engineIdInt;
			String hql = " from SbiObjects s where s.sbiEngines.engineId = ?";
			Query aQuery = aSession.createQuery(hql);
			aQuery.setInteger(0, engineIdInt.intValue());
			List biObjectsAssocitedWithEngine = aQuery.list();
			if (biObjectsAssocitedWithEngine.size() > 0)
				bool = true;
			else
				bool = false;
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			logger.error("HAs biObject associated", he);
			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}
		logger.debug("IN");
		return bool;
	}

	public List getAssociatedExporters(Engine engine) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		List<Exporters> toReturn=new ArrayList<Exporters>();
		Transaction tx = null;
		Integer engineId=engine.getId();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			//String hql = " from SbiObjects s where s.sbiEngines.engineId = "+ engineIdInt;
			String hql = " from SbiExporters s where s.sbiEngines.engineId = ?";
			Query aQuery = aSession.createQuery(hql);
			aQuery.setInteger(0, engineId.intValue());
			List exportersOfEngine = aQuery.list();
			if(exportersOfEngine!=null){
				for (Iterator iterator = exportersOfEngine.iterator(); iterator.hasNext();) {
					SbiExporters object = (SbiExporters) iterator.next();
					Exporters exp=toExporter(object);
					toReturn.add(exp);
				}
			}
			tx.commit();
			logger.debug("OUT");
			return toReturn;

		} catch (HibernateException he) {
			logException(he);
			logger.error("error in getting Associated Exporters", he);
			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}
	

	}

	
	
	public Integer countEngines() throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		Integer resultNumber;
		
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
		
			String hql = "select count(*) from SbiEngines ";
			Query hqlQuery = aSession.createQuery(hql);
			Long temp = (Long)hqlQuery.uniqueResult();
			resultNumber = new Integer(temp.intValue());

		} catch (HibernateException he) {
			logger.error("Error while loading the list of BIEngines", he);	
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

}
