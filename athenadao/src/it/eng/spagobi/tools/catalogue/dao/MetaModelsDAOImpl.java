/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.catalogue.dao;

import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.SpagoBIDOAException;
import it.eng.spagobi.tools.catalogue.bo.Content;
import it.eng.spagobi.tools.catalogue.bo.MetaModel;
import it.eng.spagobi.tools.catalogue.metadata.SbiMetaModel;
import it.eng.spagobi.tools.catalogue.metadata.SbiMetaModelContent;
import it.eng.spagobi.tools.datasource.dao.DataSourceDAOHibImpl;
import it.eng.spagobi.tools.datasource.metadata.SbiDataSource;
import it.eng.spagobi.utilities.assertion.Assert;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Expression;

public class MetaModelsDAOImpl extends AbstractHibernateDAO implements IMetaModelsDAO {

	static private Logger logger = Logger.getLogger(MetaModelsDAOImpl.class);
	



	public MetaModel loadMetaModelById(Integer id) {
		LogMF.debug(logger, "IN: id = [{0}]", id);
		
		MetaModel toReturn = null;
		Session session = null;
		Transaction transaction = null;
		
		try {
			if (id == null) {
				throw new IllegalArgumentException("Input parameter [id] cannot be null");
			}
			
			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch(Throwable t) {
				throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
			}
			
			SbiMetaModel hibModel = (SbiMetaModel) session.load(SbiMetaModel.class, id);
			logger.debug("Model loaded");
			
			toReturn = toModel(hibModel);
			
			transaction.rollback();
		} catch (Throwable t) {
			logException(t);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDOAException("An unexpected error occured while loading model with id [" + id + "]", t);	
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		
		LogMF.debug(logger, "OUT: returning [{0}]", toReturn);
		return toReturn;
	}

	public MetaModel loadMetaModelByName(String name ) {
		LogMF.debug(logger, "IN: name = [{0}]", name);
		
		MetaModel toReturn = null;
		Session session = null;
		Transaction transaction = null;
		
		try {
			if (name == null) {
				throw new IllegalArgumentException("Input parameter [name] cannot be null");
			}
			
			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch(Throwable t) {
				throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
			}
			Query query = null;
			query = session.createQuery(" from SbiMetaModel m where m.name = ?");
			query.setString(0, name);

			SbiMetaModel hibModel = (SbiMetaModel) query.uniqueResult();
			logger.debug("Model loaded");
			
			toReturn = toModel(hibModel);
			
			transaction.rollback();
		} catch (Throwable t) {
			logException(t);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDOAException("An unexpected error occured while loading model with name [" + name + "]", t);	
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		
		LogMF.debug(logger, "OUT: returning [{0}]", toReturn);
		return toReturn;
	}
	

	
	public List<MetaModel> loadMetaModelByCategories(List<Integer> categories) {
		LogMF.debug(logger, "IN: category = [{0}]", categories);
		
		List<MetaModel> toReturn = new ArrayList<MetaModel>();
		Session session = null;
		Transaction transaction = null;
		
		try {
			if (categories == null || categories.size()==0) {
				throw new IllegalArgumentException("Input parameter [category] cannot be null");
			}
			
			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch(Throwable t) {
				throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
			}
			
			String queryString = "from SbiMetaModel m where m.category in (";
			for(int i=0; i<categories.size(); i++){
				queryString = queryString+" ? ,";
			}
			queryString = queryString.substring(0,queryString.length()-2) +")";
			
			Query query = session.createQuery(queryString);
						
			for(int i=0; i<categories.size(); i++){
				query.setInteger(i, categories.get(i));
			}
			
			List list = query.list();
			Iterator it = list.iterator();
			while (it.hasNext()) {
				toReturn.add(toModel((SbiMetaModel) it.next()));
			}
			logger.debug("Models loaded");
				
			transaction.rollback();
		} catch (Throwable t) {
			logException(t);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDOAException("An unexpected error occured while loading model with categories [" + categories + "]", t);	
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		
		LogMF.debug(logger, "OUT: returning [{0}]", toReturn);
		return toReturn;		
	}
	
	
	public List<MetaModel> loadMetaModelByFilter(String filter) {
		return loadMetaModelByFilter(filter, null); 
	}
	
	public List<MetaModel> loadMetaModelByFilter(String filter, List<Integer> categories) {
		LogMF.debug(logger, "IN: filter = [{0}]", filter);
		
		List<MetaModel> toReturn = new ArrayList<MetaModel>();
		Session session = null;
		Transaction transaction = null;
		
		try {
			if (filter == null) {
				throw new IllegalArgumentException("Input parameter [category] cannot be null");
			}
			
			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch(Throwable t) {
				throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
			}
			
			
			String queryString = " from SbiMetaModel m where "+filter;
			
			if(categories!=null && categories.size()>0){
				queryString = queryString+" and   m.category in (";
				for(int i=0; i<categories.size(); i++){
					queryString = queryString+" ? ,";
				}
				queryString = queryString.substring(0,queryString.length()-2) +")";
			}
			
			Query query = session.createQuery(queryString);
			
			if(categories!=null && categories.size()>0){
				for(int i=0; i<categories.size(); i++){
					query.setInteger(i, categories.get(i));
				}
			}
		
			List list = query.list();
			Iterator it = list.iterator();
			while (it.hasNext()) {
				toReturn.add(toModel((SbiMetaModel) it.next()));
			}
			logger.debug("Models loaded");
				
			transaction.rollback();
		} catch (Throwable t) {
			logException(t);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDOAException("An unexpected error occured while loading model with filter [" + filter + "]", t);	
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		
		LogMF.debug(logger, "OUT: returning [{0}]", toReturn);
		return toReturn;		
	}
	

	public List<MetaModel> loadAllMetaModels() {
		logger.debug("IN");
		
		List<MetaModel> toReturn = new ArrayList<MetaModel>();
		Session session = null;
		Transaction transaction = null;
		
		try {
			
			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch(Throwable t) {
				throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
			}
			
			Query query = session.createQuery(" from SbiMetaModel");
			List list = query.list();
			Iterator it = list.iterator();
			while (it.hasNext()) {
				toReturn.add(toModel((SbiMetaModel) it.next()));
			}
			logger.debug("Models loaded");
			
			transaction.rollback();
		} catch (Throwable t) {
			logException(t);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDOAException("An unexpected error occured while loading models' list", t);	
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		
		LogMF.debug(logger, "OUT: returning [{0}]", toReturn);
		return toReturn;
	}

	public void modifyMetaModel(MetaModel model) {
		LogMF.debug(logger, "IN: model = [{0}]", model);
		
		Session session = null;
		Transaction transaction = null;
		
		try {
			if (model == null) {
				throw new IllegalArgumentException("Input parameter [model] cannot be null");
			}
			if (model.getId() == null) {
				throw new IllegalArgumentException("Input model's id cannot be null");
			}
			
			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch(Throwable t) {
				throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
			}
			
			SbiMetaModel hibModel = (SbiMetaModel) session.load(SbiMetaModel.class, model.getId());
			logger.debug("Model loaded");
			hibModel.setName(model.getName());
			hibModel.setDescription(model.getDescription());
			hibModel.setCategory(model.getCategory());
			if(model.getDataSourceLabel()!=null && !model.getDataSourceLabel().equals("")){
				Criterion aCriterion = Expression.eq("label",model.getDataSourceLabel());
				Criteria criteria = session.createCriteria(SbiDataSource.class);
				criteria.add(aCriterion);
	
				SbiDataSource datasource = (SbiDataSource) criteria.uniqueResult();
				
				hibModel.setDataSource(datasource);
			}
		
			updateSbiCommonInfo4Update(hibModel);
			session.save(hibModel);
			
			transaction.commit();
		} catch (Throwable t) {
			logException(t);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDOAException("An unexpected error occured while saving model [" + model + "]", t);	
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		
		logger.debug("OUT");
		
	}

	public void insertMetaModel(MetaModel model) {
		LogMF.debug(logger, "IN: model = [{0}]", model);
		
		Session session = null;
		Transaction transaction = null;
		
		try {
			if (model == null) {
				throw new IllegalArgumentException("Input parameter [model] cannot be null");
			}
			
			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
				

			} catch(Throwable t) {
				throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
			}
			
			SbiMetaModel hibModel =  new SbiMetaModel();
			hibModel.setName(model.getName());
			hibModel.setDescription(model.getDescription());
			hibModel.setCategory(model.getCategory());
			if(model.getDataSourceLabel()!=null && !model.getDataSourceLabel().equals("")){
					Criterion aCriterion = Expression.eq("label",model.getDataSourceLabel());
				Criteria criteria = session.createCriteria(SbiDataSource.class);
				criteria.add(aCriterion);

				SbiDataSource datasource = (SbiDataSource) criteria.uniqueResult();
				
				hibModel.setDataSource(datasource);
			}
			

			updateSbiCommonInfo4Insert(hibModel);
			session.save(hibModel);
			
			transaction.commit();
			
			model.setId(hibModel.getId());
			
		} catch (Throwable t) {
			logException(t);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDOAException("An unexpected error occured while saving model [" + model + "]", t);	
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		
		logger.debug("OUT");
		
	}

	public void eraseMetaModel(Integer modelId) {
		LogMF.debug(logger, "IN: model = [{0}]", modelId);

		Session session = null;
		Transaction transaction = null;
		
		try {
			if (modelId == null) {
				throw new IllegalArgumentException("Input model's id cannot be null");
			}
			
			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch(Throwable t) {
				throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
			}
			
			SbiMetaModel hibModel = (SbiMetaModel) session.load(SbiMetaModel.class, modelId);
			if (hibModel == null) {
				logger.warn("Model with id [" + modelId + "] not found");
			} else {
				session.delete(hibModel);
			}
			
			transaction.commit();
		} catch (Throwable t) {
			logException(t);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDOAException("An unexpected error occured while deleting model with id [" + modelId + "]", t);	
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		
		logger.debug("OUT");
		
	}
	
	private MetaModel toModel(SbiMetaModel hibModel) {
		logger.debug("IN");
		MetaModel toReturn = null;
		if (hibModel != null) {
			toReturn = new MetaModel();
			toReturn.setId(hibModel.getId());
			toReturn.setName(hibModel.getName());
			toReturn.setDescription(hibModel.getDescription());
			toReturn.setCategory(hibModel.getCategory());
			if(hibModel.getDataSource()!=null){
				toReturn.setDataSourceLabel(DataSourceDAOHibImpl.toDataSource(hibModel.getDataSource()).getLabel());
			}
		
		}
		logger.debug("OUT");
		return toReturn;
	}

	public void insertMetaModelContent(Integer modelId, Content content) {

		LogMF.debug(logger, "IN: content = [{0}]", content);
		
		Session session = null;
		Transaction transaction = null;
		
		try {
			if (content == null) {
				throw new IllegalArgumentException("Input parameter [content] cannot be null");
			}
			if (modelId == null) {
				throw new IllegalArgumentException("Input parameter [modelId] cannot be null");
			}
			
			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch(Throwable t) {
				throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
			}
			
			// set to not active the current active template
			String hql = " update SbiMetaModelContent mmc set mmc.active = false where mmc.active = true and mmc.model.id = ? ";
			Query query = session.createQuery(hql);
			query.setInteger(0, modelId.intValue());
			logger.debug("Updates the current content of model " + modelId + " with active = false.");
			query.executeUpdate();
			// get the next prog for the new content
			Integer maxProg = null;
			Integer nextProg = null;
			hql = " select max(mmc.prog) as maxprog from SbiMetaModelContent mmc where mmc.model.id = ? ";
			query = session.createQuery(hql);
			query.setInteger(0, modelId.intValue());
			List result = query.list();
			Iterator it = result.iterator();
			while (it.hasNext()){
				maxProg = (Integer) it.next();				
			}
			logger.debug("Current max prog : " + maxProg);
			if (maxProg == null) {
				nextProg = new Integer(1);
			} else {
				nextProg = new Integer(maxProg.intValue() + 1);
			}
			logger.debug("Next prog: " + nextProg);
			
			// store the model content
			SbiMetaModelContent hibContent = new SbiMetaModelContent();
			hibContent.setActive(new Boolean(true));
			hibContent.setCreationDate(content.getCreationDate());
			hibContent.setCreationUser(content.getCreationUser());
			hibContent.setFileName(content.getFileName());
			hibContent.setProg(nextProg);
			hibContent.setContent(content.getContent());
			hibContent.setDimension(content.getDimension());
			SbiMetaModel sbiModel = (SbiMetaModel) session.load(SbiMetaModel.class, modelId);
			hibContent.setModel(sbiModel);
			updateSbiCommonInfo4Insert(hibContent);
			session.save(hibContent);
			transaction.commit();
			
		} catch (Throwable t) {
			logException(t);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDOAException("An unexpected error occured while saving model content [" + content + "]", t);	
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		
		logger.debug("OUT");
		
	}

	public void eraseMetaModelContent(Integer contentId) {
		LogMF.debug(logger, "IN: content = [{0}]", contentId);

		Session session = null;
		Transaction transaction = null;
		
		try {
			if (contentId == null) {
				throw new IllegalArgumentException("Input content's id cannot be null");
			}
			
			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch(Throwable t) {
				throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
			}
			
			SbiMetaModelContent hibContent = (SbiMetaModelContent) session.load(SbiMetaModelContent.class, contentId);
			if (hibContent == null) {
				logger.warn("Content [" + hibContent + "] not found");
			} else {
				Integer modelId = hibContent.getModel().getId();
				boolean itWasActive = hibContent.getActive();
				session.delete(hibContent);
				if (itWasActive) {
					Query query = session.createQuery(" from SbiMetaModelContent mmc where mmc.model.id = " 
									+ modelId + " order by prog desc");
					List<SbiMetaModelContent> list = query.list();
					if (list != null && !list.isEmpty()) {
						SbiMetaModelContent first = list.get(0);
						first.setActive(true);
						session.save(first);
					}
				}
			}
			
			transaction.commit();
		} catch (Throwable t) {
			logException(t);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDOAException("An unexpected error occured while deleting content with id [" + contentId + "]", t);	
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		
		logger.debug("OUT");
		
	}

	public Content loadMetaModelContentById(Integer contendId) {
		LogMF.debug(logger, "IN: id = [{0}]", contendId);
		
		Content toReturn = null;
		Session session = null;
		Transaction transaction = null;
		
		try {
			if (contendId == null) {
				throw new IllegalArgumentException("Input parameter [contendId] cannot be null");
			}
			
			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch(Throwable t) {
				throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
			}
			
			SbiMetaModelContent hibContent = (SbiMetaModelContent) session.load(SbiMetaModelContent.class, contendId);
			logger.debug("Content loaded");
			
			toReturn = toContent(hibContent, true);
			
			transaction.rollback();
		} catch (Throwable t) {
			logException(t);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDOAException("An unexpected error occured while loading content with id [" + contendId + "]", t);	
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		
		LogMF.debug(logger, "OUT: returning [{0}]", toReturn);
		return toReturn;
	}
	
	

	public Content loadActiveMetaModelContentById(Integer modelId) {
		LogMF.debug(logger, "IN: id = [{0}]", modelId);
		
		Content toReturn = null;
		Session session = null;
		Transaction transaction = null;
		
		try {
			if (modelId == null) {
				throw new IllegalArgumentException("Input parameter [modelId] cannot be null");
			}
			
			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch(Throwable t) {
				throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
			}
			
			Query query = session.createQuery(" from SbiMetaModelContent mmc where mmc.model.id = ? and mmc.active = true ");
			query.setInteger(0, modelId);
			SbiMetaModelContent hibContent = (SbiMetaModelContent) query.uniqueResult();
			logger.debug("Content loaded");
			
			toReturn = toContent(hibContent, true);
			
			transaction.rollback();
		} catch (Throwable t) {
			logException(t);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDOAException("An unexpected error occured while loading active content for model with id [" + modelId + "]", t);	
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		
		LogMF.debug(logger, "OUT: returning [{0}]", toReturn);
		return toReturn;
	}
	
	public Content loadActiveMetaModelContentByName(String modelName) {
		LogMF.debug(logger, "IN: name = [{0}]", modelName);
		
		Content toReturn = null;
		Session session = null;
		Transaction transaction = null;
		
		try {
			if (modelName == null) {
				throw new IllegalArgumentException("Input parameter [modelName] cannot be null");
			}
			
			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch(Throwable t) {
				throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
			}
			
			Query query = session.createQuery(" from SbiMetaModelContent mmc where mmc.model.name = ? and mmc.active = true ");
			query.setString(0, modelName);
			SbiMetaModelContent hibContent = (SbiMetaModelContent) query.uniqueResult();
			logger.debug("Content loaded");
			
			toReturn = toContent(hibContent, true);
			
			transaction.rollback();
		} catch (Throwable t) {
			logException(t);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDOAException("An unexpected error occured while loading active content for model with id [" + modelName + "]", t);	
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		
		LogMF.debug(logger, "OUT: returning [{0}]", toReturn);
		return toReturn;
	}
	
	public long getActiveMetaModelContentLastModified(String modelName) {
		long lastModified = -1;
		
		Content content = loadActiveMetaModelContentByName(modelName);
		if(content != null &&  content.getCreationDate() != null) {
			lastModified = content.getCreationDate().getTime();
		} 
		
		return lastModified;
	}

	public List<Content> loadMetaModelVersions(Integer modelId) {
		LogMF.debug(logger, "IN: id = [{0}]", modelId);
		
		List<Content> toReturn = new ArrayList<Content>();
		Session session = null;
		Transaction transaction = null;
		
		try {
			if (modelId == null) {
				throw new IllegalArgumentException("Input parameter [modelId] cannot be null");
			}
			
			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch(Throwable t) {
				throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
			}
			
			Query query = session.createQuery(" from SbiMetaModelContent mmc where mmc.model.id = ? order by mmc.prog desc");
			query.setInteger(0, modelId);
			List<SbiMetaModelContent> list = (List<SbiMetaModelContent>) query.list();
			Iterator<SbiMetaModelContent> it = list.iterator();
			while (it.hasNext()) {
				toReturn.add(toContent(it.next(), false));
			}
			logger.debug("Contents loaded");
			
			transaction.rollback();
		} catch (Throwable t) {
			logException(t);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDOAException("An unexpected error occured while loading active content for model with id [" + modelId + "]", t);	
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		
		LogMF.debug(logger, "OUT: returning [{0}]", toReturn);
		return toReturn;
	}

	private Content toContent(SbiMetaModelContent hibContent, boolean loadByteContent) {
		logger.debug("IN");
		Content toReturn = null;
		if (hibContent != null) {
			toReturn = new Content();
			toReturn.setId(hibContent.getId());
			toReturn.setCreationUser(hibContent.getCreationUser());
			toReturn.setCreationDate(hibContent.getCreationDate());
			toReturn.setActive(hibContent.getActive());
			toReturn.setFileName(hibContent.getFileName());
			toReturn.setDimension(hibContent.getDimension());
			if (loadByteContent) {
				toReturn.setContent(hibContent.getContent());
			}
		}
		logger.debug("OUT");
		return toReturn;
	}

	public void setActiveVersion(Integer modelId, Integer contentId) {
		LogMF.debug(logger, "IN: modelId = [{0}], contentId = [{1}]", modelId, contentId);
		
		Session session = null;
		Transaction transaction = null;
		
		try {
			if (modelId == null) {
				throw new IllegalArgumentException("Input parameter [modelId] cannot be null");
			}
			if (contentId == null) {
				throw new IllegalArgumentException("Input parameter [contentId] cannot be null");
			}
			
			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch(Throwable t) {
				throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
			}
			
			// set to not active the current active template
			String hql = " update SbiMetaModelContent mmc set mmc.active = false where mmc.active = true and mmc.model.id = ? ";
			Query query = session.createQuery(hql);
			query.setInteger(0, modelId.intValue());
			logger.debug("Updates the current content of model " + modelId + " with active = false.");
			query.executeUpdate();
			
			// set to active the new active template
			hql = " update SbiMetaModelContent mmc set mmc.active = true where mmc.id = ? and mmc.model.id = ? ";
			query = session.createQuery(hql);
			query.setInteger(0, contentId);
			query.setInteger(1, modelId.intValue());
			logger.debug("Updates the current content " + contentId + " of model " + modelId + " with active = true.");
			query.executeUpdate();
			
			transaction.commit();
			
		} catch (Throwable t) {
			logException(t);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDOAException("An unexpected error occured while saving active content [" + contentId 
					+ "] for model [" + modelId + "]", t);	
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		
		logger.debug("OUT");
		
	}
}