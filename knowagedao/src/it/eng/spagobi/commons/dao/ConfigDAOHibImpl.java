/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.dao;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.metadata.SbiConfig;
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
 * @author Monia Spinelli
 */
public class ConfigDAOHibImpl extends AbstractHibernateDAO implements IConfigDAO {

    static private Logger logger = Logger.getLogger(ConfigDAOHibImpl.class);
    
    /* (non-Javadoc)
     * @see it.eng.spagobi.commons.dao.IUserFunctionalityDAO#loadAllConfigParameters()
     */
    public List loadAllConfigParameters() throws Exception{
    	logger.debug("IN");
		
		ArrayList toReturn = new ArrayList();
		Session aSession = null;
		Transaction tx = null;
		try{
			aSession = getSession();
			tx = aSession.beginTransaction();
			
			List roleTypes = new ArrayList();
			
			Query hibQuery = aSession.createQuery(" from SbiConfig");
			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();			
			while (it.hasNext()) {			
				SbiConfig hibMap = (SbiConfig) it.next();	
				if (hibMap != null) {
					Config biMap = hibMap.toConfig();	
					toReturn.add(biMap);
				}
			}
			tx.commit();
		}catch(HibernateException he){
			logger.error("HibernateException during query",he);
			
			if (tx != null) tx.rollback();	
	
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);  
		
		}finally{
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
			logger.debug("OUT");
		}
		return toReturn;
	
    }

    /**
	 * Load configuration by id.
	 * 
	 * @param id the configuration id
	 * 
	 * @return the config object
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.common.bo.dao.ISbiConfigDAO#loadConfigParametersById(integer)
	 */
    public Config loadConfigParametersById(String id) throws Exception {
    	logger.debug("IN");
    	Config toReturn = null;
		Session tmpSession = null;
		Transaction tx = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			SbiConfig hibMap = (SbiConfig)tmpSession.load(SbiConfig.class,  id);
			toReturn = hibMap.toConfig();
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
			logger.debug("OUT");
		}		
		return toReturn;
    }
    
    /**
	 * Load configuration by complete label.
	 * 
	 * @param label the configuration label
	 * 
	 * @return the config object
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.common.bo.dao.ISbiConfigDAO#loadConfigParametersById(string)
	 */
    public Config loadConfigParametersByLabel(String label) throws Exception{
    	logger.debug("IN");
    	Config toReturn = null;
		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			Criterion labelCriterrion = Expression.eq("label",label);
			Criteria criteria = tmpSession.createCriteria(SbiConfig.class);
			criteria.add(labelCriterrion);	
	
			SbiConfig hibConfig = (SbiConfig) criteria.uniqueResult();
			if (hibConfig == null) return null;
			toReturn = hibConfig.toConfig();				

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
			logger.debug("OUT");
		}
		return toReturn;		
    }
    
    /**
	 * Load configuration by a property node.
	 * 
	 * @param prop the configuration label
	 * 
	 * @return a list with all children of the property node
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.common.bo.dao.ISbiConfigDAO#loadConfigParametersByProperties(string)
	 */
    public List loadConfigParametersByProperties(String prop) throws Exception{
    	logger.debug("IN");
    	
		ArrayList toReturn = new ArrayList(); 
		List allConfig = loadAllConfigParameters();
		//filter with the 'prop' parameter
		Iterator it = allConfig.iterator();			
		while (it.hasNext()) {			
			Config tmpConf = (Config) it.next();	
			if (tmpConf.isActive() && tmpConf.getLabel().startsWith(prop))
				toReturn.add(tmpConf);
		}
		
		return toReturn;
    }
    
    public SbiConfig fromConfig(Config config){
		SbiConfig hibConfig = new SbiConfig();
		hibConfig.setValueCheck(config.getValueCheck());
		hibConfig.setId(config.getId());
		hibConfig.setName(config.getName());
		hibConfig.setLabel(config.getLabel());
		hibConfig.setDescription(config.getDescription());
		hibConfig.setCategory(config.getCategory());
		return hibConfig;
	}
	
	/**
	 * Save config by id.
	 * 
	 * @param id the id
	 * 
	 * @return void
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 */
	public void saveConfig(Config config) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			
			SbiConfig hibConfig = null;
			Integer id = config.getId();
			
			Criterion domainCriterrion = Expression.eq("valueId",config.getValueTypeId());
			Criteria domainCriteria = aSession.createCriteria(SbiDomains.class);
			domainCriteria.add(domainCriterrion);	
			
			SbiDomains hibDomains = (SbiDomains) domainCriteria.uniqueResult();	
			
			if(id!=null){
				//modification
				logger.debug("Update Config");
				hibConfig = (SbiConfig) aSession.load(SbiConfig.class, id);
				updateSbiCommonInfo4Update(hibConfig);
				hibConfig.setLabel(config.getLabel());
				hibConfig.setDescription(config.getDescription());
				hibConfig.setName(config.getName());
				hibConfig.setValueCheck(config.getValueCheck());
				hibConfig.setIsActive(config.isActive());
				hibConfig.setSbiDomains(hibDomains);
				hibConfig.setCategory(config.getCategory());
			}
			else{
				//insertion
				logger.debug("Insert new Config");
				hibConfig = fromConfig(config);
				updateSbiCommonInfo4Insert(hibConfig);
				hibConfig.setSbiDomains(hibDomains);
			}
			
			Integer newId = (Integer) aSession.save(hibConfig);
				
			tx.commit();
			
			config.setId(newId);

		} catch (HibernateException he) {
			logger.error("HibernateException", he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		SingletonConfig.getInstance().clearCache();
		logger.debug("OUT");
	}



    /**
     * Delete config by id.
     * 
     * @param id the id
     * 
     * @return void
     * 
     * @throws EMFUserError the EMF user error
     * 
     */
    public void delete(Integer idConfig)  throws EMFUserError {
    	logger.debug("IN");
    	Session sess = null;
    	Transaction tx = null;

    	try {
    		sess = getSession();
    		tx = sess.beginTransaction();
    		
    		Criterion aCriterion = Expression.eq("id", idConfig);
    		Criteria criteria = sess.createCriteria(SbiConfig.class);
    		criteria.add(aCriterion);
    		SbiConfig aSbiConfig = (SbiConfig) criteria.uniqueResult();
    		if (aSbiConfig!=null) sess.delete(aSbiConfig);
    		tx.commit();
    		
    	} catch (HibernateException he) {
    		logger.error("HibernateException",he);

    		if (tx != null)
    			tx.rollback();

    		throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

    	} finally {
    		if (sess!=null){
    			if (sess.isOpen()) sess.close();
    		}
    	}
    	SingletonConfig.getInstance().clearCache();
    	logger.debug("OUT");
    }

}