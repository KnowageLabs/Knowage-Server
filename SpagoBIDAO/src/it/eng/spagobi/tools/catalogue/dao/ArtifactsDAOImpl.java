/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.catalogue.dao;

import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.SpagoBIDOAException;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.tools.catalogue.bo.Artifact;
import it.eng.spagobi.tools.catalogue.bo.Content;
import it.eng.spagobi.tools.catalogue.metadata.SbiArtifact;
import it.eng.spagobi.tools.catalogue.metadata.SbiArtifactContent;
import it.eng.spagobi.utilities.assertion.Assert;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.HbmBinder;

public class ArtifactsDAOImpl extends AbstractHibernateDAO implements IArtifactsDAO {

	static private Logger logger = Logger.getLogger(ArtifactsDAOImpl.class);

	public Artifact loadArtifactById(Integer id) {
		LogMF.debug(logger, "IN: id = [{0}]", id);
		
		Artifact toReturn = null;
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
			
			SbiArtifact hibArtifact = (SbiArtifact) session.load(SbiArtifact.class, id);
			logger.debug("Artifact loaded");
			
			toReturn = toArtifact(hibArtifact, session);
			
			transaction.rollback();
		} catch (Throwable t) {
			logException(t);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDOAException("An unexpected error occured while loading artifact with id [" + id + "]", t);	
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		
		LogMF.debug(logger, "OUT: returning [{0}]", toReturn);
		return toReturn;
	}

	public Artifact loadArtifactByNameAndType(String name, String type) {
		LogMF.debug(logger, "IN: name = [{0}], type = [{1}]", name, type);
		
		Artifact toReturn = null;
		Session session = null;
		Transaction transaction = null;
		
		try {
			if (name == null) {
				throw new IllegalArgumentException("Input parameter [name] cannot be null");
			}
			if (type == null) {
				throw new IllegalArgumentException("Input parameter [type] cannot be null");
			}
			
			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch(Throwable t) {
				throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
			}
			
			Query query = session.createQuery(" from SbiArtifact m where m.name = ? and m.type = ?");
			query.setString(0, name);
			query.setString(1, type);
			SbiArtifact hibArtifact = (SbiArtifact) query.uniqueResult();
			logger.debug("Artifact loaded");
			
			toReturn = toArtifact(hibArtifact, session);
			
			transaction.rollback();
		} catch (Throwable t) {
			logException(t);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDOAException(
					"An unexpected error occured while loading artifact with name ["
							+ name + "] and type [" + type + "]", t);	
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		
		LogMF.debug(logger, "OUT: returning [{0}]", toReturn);
		return toReturn;
	}

	public List<Artifact> loadAllArtifacts(String type) {
		LogMF.debug(logger, "IN: type = [{0}]", type);
		
		List<Artifact> toReturn = new ArrayList<Artifact>();
		Session session = null;
		Transaction transaction = null;
		
		try {
			
			if (type == null) {
				throw new IllegalArgumentException("Input parameter [type] cannot be null");
			}
			
			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch(Throwable t) {
				throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
			}
			
			Query query = session.createQuery(" from SbiArtifact a where a.type = ?");
			query.setString(0, type);
			List list = query.list();
			Iterator it = list.iterator();
			while (it.hasNext()) {
				toReturn.add(toArtifact((SbiArtifact) it.next(), session));
			}
			logger.debug("Artifacts loaded");
			
			transaction.rollback();
		} catch (Throwable t) {
			logException(t);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDOAException("An unexpected error occured while loading artifacts' list", t);	
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		
		LogMF.debug(logger, "OUT: returning [{0}]", toReturn);
		return toReturn;
	}

	public void modifyArtifact(Artifact artifact) {
		LogMF.debug(logger, "IN: artifact = [{0}]", artifact);
		
		Session session = null;
		Transaction transaction = null;
		
		try {
			if (artifact == null) {
				throw new IllegalArgumentException("Input parameter [artifact] cannot be null");
			}
			if (artifact.getId() == null) {
				throw new IllegalArgumentException("Input artifact's id cannot be null");
			}
			
			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch(Throwable t) {
				throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
			}
			
			SbiArtifact hibArtifact = (SbiArtifact) session.load(SbiArtifact.class, artifact.getId());
			logger.debug("Artifact loaded");
			hibArtifact.setName(artifact.getName());
			hibArtifact.setDescription(artifact.getDescription());
			hibArtifact.setType(artifact.getType());
			hibArtifact.setModelLocker(artifact.getModelLocker());
			hibArtifact.setModelLocked(artifact.getModelLocked());
			
			updateSbiCommonInfo4Update(hibArtifact);
			session.save(hibArtifact);
			
			transaction.commit();
		} catch (Throwable t) {
			logException(t);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDOAException("An unexpected error occured while saving artifact [" + artifact + "]", t);	
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		
		logger.debug("OUT");
		
	}

	public void insertArtifact(Artifact artifact) {
		LogMF.debug(logger, "IN: artifact = [{0}]", artifact);
		
		Session session = null;
		Transaction transaction = null;
		
		try {
			if (artifact == null) {
				throw new IllegalArgumentException("Input parameter [artifact] cannot be null");
			}
			
			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch(Throwable t) {
				throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
			}
			
			SbiArtifact hibArtifact =  new SbiArtifact();
			hibArtifact.setName(artifact.getName());
			hibArtifact.setDescription(artifact.getDescription());
			hibArtifact.setType(artifact.getType());
			
			updateSbiCommonInfo4Insert(hibArtifact);
			session.save(hibArtifact);
			
			transaction.commit();
			
			artifact.setId(hibArtifact.getId());
			
		} catch (Throwable t) {
			logException(t);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDOAException("An unexpected error occured while saving artifact [" + artifact + "]", t);	
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		
		logger.debug("OUT");
		
	}

	public void eraseArtifact(Integer artifactId) {
		LogMF.debug(logger, "IN: artifact = [{0}]", artifactId);

		Session session = null;
		Transaction transaction = null;
		
		try {
			if (artifactId == null) {
				throw new IllegalArgumentException("Input artifact's id cannot be null");
			}
			
			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch(Throwable t) {
				throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
			}
			
			SbiArtifact hibArtifact = (SbiArtifact) session.load(SbiArtifact.class, artifactId);
			if (hibArtifact == null) {
				logger.warn("Artifact with id [" + artifactId + "] not found");
			} else {
				session.delete(hibArtifact);
			}
			
			transaction.commit();
		} catch (Throwable t) {
			logException(t);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDOAException("An unexpected error occured while deleting artifact with id [" + artifactId + "]", t);	
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		
		logger.debug("OUT");
		
	}
	
	private Artifact toArtifact(SbiArtifact hibArtifact, Session session) {
		logger.debug("IN");
		Artifact toReturn = null;
		if (hibArtifact != null) {
			toReturn = new Artifact();
			toReturn.setId(hibArtifact.getId());
			toReturn.setName(hibArtifact.getName());
			toReturn.setDescription(hibArtifact.getDescription());
			toReturn.setType(hibArtifact.getType());
			toReturn.setModelLocked(hibArtifact.getModelLocked());
			toReturn.setModelLocker(hibArtifact.getModelLocker());
			
			// get the current (active) Content id
			Query query = session.createQuery("select mmc.id from SbiArtifactContent mmc where mmc.artifact.id = ? and mmc.active = true ");
			query.setInteger(0, hibArtifact.getId());
			Integer currentContentId = (Integer) query.uniqueResult();
			toReturn.setCurrentContentId(currentContentId);
		}
		logger.debug("OUT");
		return toReturn;
	}

	public void insertArtifactContent(Integer artifactId, Content content) {

		LogMF.debug(logger, "IN: content = [{0}]", content);
		
		Session session = null;
		Transaction transaction = null;
		
		try {
			if (content == null) {
				throw new IllegalArgumentException("Input parameter [content] cannot be null");
			}
			if (artifactId == null) {
				throw new IllegalArgumentException("Input parameter [artifactId] cannot be null");
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
			String hql = " update SbiArtifactContent mmc set mmc.active = false where mmc.active = true and mmc.artifact.id = ? ";
			Query query = session.createQuery(hql);
			query.setInteger(0, artifactId.intValue());
			logger.debug("Updates the current content of artifact " + artifactId + " with active = false.");
			query.executeUpdate();
			// get the next prog for the new content
			Integer maxProg = null;
			Integer nextProg = null;
			hql = " select max(mmc.prog) as maxprog from SbiArtifactContent mmc where mmc.artifact.id = ? ";
			query = session.createQuery(hql);
			query.setInteger(0, artifactId.intValue());
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
			
			// store the artifact content
			SbiArtifactContent hibContent = new SbiArtifactContent();
			hibContent.setActive(new Boolean(true));
			hibContent.setCreationDate(new Date());
			hibContent.setFileName(content.getFileName());
			hibContent.setProg(nextProg);
			hibContent.setContent(content.getContent());
			SbiArtifact sbiArtifact = (SbiArtifact) session.load(SbiArtifact.class, artifactId);
			hibContent.setArtifact(sbiArtifact);
			hibContent.setCreationUser(content.getCreationUser());
			hibContent.setCreationDate(content.getCreationDate());
			hibContent.setDimension(content.getDimension());
			updateSbiCommonInfo4Insert(hibContent);
			session.save(hibContent);
			transaction.commit();
			
		} catch (Throwable t) {
			logException(t);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDOAException("An unexpected error occured while saving artifact content [" + content + "]", t);	
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		
		logger.debug("OUT");
		
	}

	public void eraseArtifactContent(Integer contentId) {
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
			
			SbiArtifactContent hibContent = (SbiArtifactContent) session.load(SbiArtifactContent.class, contentId);
			if (hibContent == null) {
				logger.warn("Content [" + hibContent + "] not found");
			} else {
				Integer artifactId = hibContent.getArtifact().getId();
				boolean itWasActive = hibContent.getActive();
				session.delete(hibContent);
				if (itWasActive) {
					Query query = session.createQuery(" from SbiArtifactContent mmc where mmc.artifact.id = " 
									+ artifactId + " order by prog desc");
					List<SbiArtifactContent> list = query.list();
					if (list != null && !list.isEmpty()) {
						SbiArtifactContent first = list.get(0);
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

	public Content loadArtifactContentById(Integer contendId) {
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
			
			SbiArtifactContent hibContent = (SbiArtifactContent) session.load(SbiArtifactContent.class, contendId);
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

	public Content loadActiveArtifactContent(Integer artifactId) {
		LogMF.debug(logger, "IN: id = [{0}]", artifactId);
		
		Content toReturn = null;
		Session session = null;
		Transaction transaction = null;
		
		try {
			if (artifactId == null) {
				throw new IllegalArgumentException("Input parameter [artifactId] cannot be null");
			}
			
			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch(Throwable t) {
				throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
			}
			
			Query query = session.createQuery(" from SbiArtifactContent mmc where mmc.artifact.id = ? and mmc.active = true ");
			query.setInteger(0, artifactId);
			SbiArtifactContent hibContent = (SbiArtifactContent) query.uniqueResult();
			logger.debug("Content loaded");
			
			toReturn = toContent(hibContent, true);
			
			transaction.rollback();
		} catch (Throwable t) {
			logException(t);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDOAException("An unexpected error occured while loading active content for artifact with id [" + artifactId + "]", t);	
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		
		LogMF.debug(logger, "OUT: returning [{0}]", toReturn);
		return toReturn;
	}

	public List<Content> loadArtifactVersions(Integer artifactId) {
		LogMF.debug(logger, "IN: id = [{0}]", artifactId);
		
		List<Content> toReturn = new ArrayList<Content>();
		Session session = null;
		Transaction transaction = null;
		
		try {
			if (artifactId == null) {
				throw new IllegalArgumentException("Input parameter [artifactId] cannot be null");
			}
			
			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch(Throwable t) {
				throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
			}
			
			Query query = session.createQuery(" from SbiArtifactContent mmc where mmc.artifact.id = ? order by mmc.prog desc");
			query.setInteger(0, artifactId);
			List<SbiArtifactContent> list = (List<SbiArtifactContent>) query.list();
			Iterator<SbiArtifactContent> it = list.iterator();
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
			throw new SpagoBIDOAException("An unexpected error occured while loading active content for artifact with id [" + artifactId + "]", t);	
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		
		LogMF.debug(logger, "OUT: returning [{0}]", toReturn);
		return toReturn;
	}

	private Content toContent(SbiArtifactContent hibContent, boolean loadByteContent) {
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

	public void setActiveVersion(Integer artifactId, Integer contentId) {
		LogMF.debug(logger, "IN: artifactId = [{0}], contentId = [{1}]", artifactId, contentId);
		
		Session session = null;
		Transaction transaction = null;
		
		try {
			if (artifactId == null) {
				throw new IllegalArgumentException("Input parameter [artifactId] cannot be null");
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
			String hql = " update SbiArtifactContent mmc set mmc.active = false where mmc.active = true and mmc.artifact.id = ? ";
			Query query = session.createQuery(hql);
			query.setInteger(0, artifactId.intValue());
			logger.debug("Updates the current content of artifact " + artifactId + " with active = false.");
			query.executeUpdate();
			
			// set to active the new active template
			hql = " update SbiArtifactContent mmc set mmc.active = true where mmc.id = ? and mmc.artifact.id = ? ";
			query = session.createQuery(hql);
			query.setInteger(0, contentId);
			query.setInteger(1, artifactId.intValue());
			logger.debug("Updates the current content " + contentId + " of artifact " + artifactId + " with active = true.");
			query.executeUpdate();
			
			transaction.commit();
			
		} catch (Throwable t) {
			logException(t);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDOAException("An unexpected error occured while saving active content [" + contentId 
					+ "] for artifact [" + artifactId + "]", t);	
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		
		logger.debug("OUT");
		
	}

/**
 * Locks model designed by Artifact id,
 * returns the userId that locks the model (that could be different from current user if it was already blocked)
 */
	public String lockArtifact(Integer artifactId, String userId) {
		logger.debug("IN");
		String userBlocking = null;		
		Session session = null;
		Transaction transaction = null;
	
		try {
			if (artifactId == null) {
				throw new IllegalArgumentException("Input parameter [artifactId] cannot be null");
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
			String hql = " update SbiArtifact ar set ar.modelLocked = ?, ar.modelLocker = ? where (ar.modelLocked = ? OR ar.modelLocked is null)  and ar.id = ? ";
			Query query = session.createQuery(hql);
			query.setBoolean(0, true); 
			query.setString(1, userId); 
			query.setBoolean(2, false); 
			query.setInteger(3, artifactId); 

			logger.debug("Lock the artifact with id " + artifactId + "");
			query.executeUpdate();
			transaction.commit();
			
			// check if current user has the lock
			SbiArtifact hibArtifact = (SbiArtifact) session.load(SbiArtifact.class, artifactId);
			logger.debug("Artifact loaded");
			Artifact art = toArtifact(hibArtifact, session);
			
			userBlocking = art.getModelLocker();
			if(art.getModelLocker() != null && art.getModelLocker().equals(userId)){
				logger.debug("Model was locked by current user");
			
			}
			else{
				logger.warn("Model was already blocked by user "+art.getModelLocker());
			}
			
		
		} catch (Throwable t) {
			logException(t);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDOAException("An unexpected error occured while locking for user[" + userId
					+ "] the artifact [" + artifactId + "]", t);	
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	
		logger.debug("OUT");
		return userBlocking;
	}
	
	
	/**
	 * Unlock model designed by Artifact id,
	 * returns user currently locking the model, that will be null if method has success, but could be from a different user is fails
	 */
		public String unlockArtifact(Integer artifactId, String userId) {
			logger.debug("IN");
			String userLocking = null;			

			Session session = null;
			Transaction transaction = null;
		
			
			try {
				
				if (artifactId == null) {
					logger.error("Input parameter [artifactId] cannot be null");
					throw new IllegalArgumentException("Input parameter [artifactId] cannot be null");
				}

				try {
					session = getSession();
					Assert.assertNotNull(session, "session cannot be null");
					transaction = session.beginTransaction();
					Assert.assertNotNull(transaction, "transaction cannot be null");
				} catch(Throwable t) {
					throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
				}
				
				// check if current user has the lock
				SbiArtifact hibArtifact = (SbiArtifact) session.load(SbiArtifact.class, artifactId);
				
				if (hibArtifact == null) {
					logger.error("Could not find artifact for id "+artifactId);
					throw new SpagoBIDOAException("Could not find artifact for id "+artifactId);
				}
				
				logger.debug("Artifact loaded");
				Artifact art = toArtifact(hibArtifact, session);
				//Admin can force unlock from other
				boolean isAdmin = UserUtilities.isAdministrator(UserUtilities.getUserProfile(userId));
				
				if((art.getModelLocked().equals(true) && art.getModelLocker().equals(userId)) || (isAdmin)){
					// set to not active the current active template
					String hql = " update SbiArtifact ar set ar.modelLocked = ?, ar.modelLocker = ? where ar.modelLocked = ?  and ar.id = ? ";
					Query query = session.createQuery(hql);
					query.setBoolean(0, false); 
					query.setString(1, null); 
					query.setBoolean(2, true); 
					query.setInteger(3, artifactId); 

					logger.debug("Unlock the artifact with id " + artifactId + "");
					query.executeUpdate();	
					
					userLocking = null;
					transaction.commit();
					
				}
				else{
					logger.warn("Could not unlock model because it is locked by another user than current one: "+art.getModelLocker());
					userLocking = art.getModelLocker();
				}

			} catch (Throwable t) {
				logException(t);
				if (transaction != null && transaction.isActive()) {
					transaction.rollback();
				}
				throw new SpagoBIDOAException("An unexpected error occured while unlocking for user[" + userId
						+ "] the artifact [" + artifactId + "]", t);	
			} finally {
				if (session != null && session.isOpen()) {
					session.close();
				}
			}
		
			logger.debug("OUT");
			return userLocking;
		}
	
		
		
		
		/**
		 * Return a String representing the artifact status
		 * can be:
		 * unlocked 
		 * locked_by_you
		 * locked_by_other
		 */
		public String getArtifactStatus(Integer artifactId, String userId){
			logger.debug("IN");
		String statusToReturn = null;

		Artifact art = loadArtifactById(artifactId);
		if(art == null)	{
			throw new SpagoBIDOAException("Artifact with id [" + artifactId + "] could not be loaded");	
		}
		else{
			Boolean locked = art.getModelLocked();
			String locker = art.getModelLocker();

			if(locked==false){
				logger.debug("Artifact with id "+artifactId+" is unlocked");
				statusToReturn="unlocked";
			}
			else{
				if(locker != null && locker.equals(userId)){
					statusToReturn="locked_by_you";	
				}
				else{
					statusToReturn="locked_by_other";	
				}
				
				
			}
			
		}
		
		return statusToReturn;
		}

		
		
		
	
		public Artifact loadArtifactByContentId(Integer contendId) {
			LogMF.debug(logger, "IN: id = [{0}]", contendId);
			
			Artifact toReturn = null;
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
				
				SbiArtifactContent hibContent = (SbiArtifactContent) session.load(SbiArtifactContent.class, contendId);
				logger.debug("Content loaded");
				
				if(hibContent == null){
					throw new SpagoBIDOAException("Content with id "+contendId+" not retrieved");
				}
				if(hibContent.getArtifact() == null){
					throw new SpagoBIDOAException("Content with id "+contendId+" retrieved but without artifact associated");					
				}
				
				SbiArtifact sbiArtifact = hibContent.getArtifact();
				
				toReturn = toArtifact(sbiArtifact, session);
				
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
}