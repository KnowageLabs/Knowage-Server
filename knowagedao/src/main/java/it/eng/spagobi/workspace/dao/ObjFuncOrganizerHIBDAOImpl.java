/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.workspace.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.workspace.bo.DocumentOrganizer;
import it.eng.spagobi.workspace.metadata.SbiFunctionsOrganizer;
import it.eng.spagobi.workspace.metadata.SbiObjFuncOrganizer;
import it.eng.spagobi.workspace.metadata.SbiObjFuncOrganizerId;

public class ObjFuncOrganizerHIBDAOImpl extends AbstractHibernateDAO implements IObjFuncOrganizerDAO {

	private static transient Logger logger = Logger.getLogger(ObjFuncOrganizerHIBDAOImpl.class);

	/**
	 * The method that collects all Organizer documents available for current user. It does not look for a particular folder, but rather for all documents that
	 * exist for the user.
	 *
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	@Override
	public List loadAllOrganizerDocuments() {

		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;

		List listOfDocuments = null;
		List toReturn = new ArrayList();

		try {

			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = aSession.createQuery(" from SbiObjFuncOrganizer");

			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();

			while (it.hasNext()) {

				SbiObjFuncOrganizer hibObj = (SbiObjFuncOrganizer) it.next();
				toReturn.add(toDocumentOrganizer(hibObj));

			}

			tx.commit();

		}

		catch (HibernateException he) {

			logException(he);
			logger.error("HibernateException", he);

			if (tx != null)
				tx.rollback();

			/**
			 * Throw this specific exception so the service that called the Hibernate method can handle it and forward the information about the error towards
			 * the client (final user).
			 *
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			throw new HibernateException(he);
		}

		finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
			logger.debug("OUT");
		}
		return toReturn;
	}

	@Override
	public List loadDocumentsByFolder(Integer folderId) {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List listOfDocuments = null;
		List toReturn = new ArrayList();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Criteria criteria = aSession.createCriteria(SbiObjFuncOrganizer.class);
			criteria.add(Restrictions.eq("id.sbiFunctionsOrganizer.functId", folderId));
			listOfDocuments = criteria.list();
			Iterator it = listOfDocuments.iterator();

			while (it.hasNext()) {
				SbiObjFuncOrganizer hibObj = (SbiObjFuncOrganizer) it.next();
				toReturn.add(toDocumentOrganizer(hibObj));
			}

			return toReturn;
		} catch (HibernateException he) {
			logException(he);
			logger.error("HibernateException", he);
			if (tx != null)
				tx.rollback();
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
			logger.debug("OUT");
		}
		return toReturn;
	}

	@Override
	public SbiObjFuncOrganizer addDocumentToOrganizer(Integer documentId) {
		logger.debug("IN");
		IEngUserProfile user = getUserProfile();
		String userId = ((UserProfile) user).getUserId().toString();
		Session aSession = null;
		Transaction tx = null;
		SbiObjFuncOrganizer hibDoc = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			hibDoc = new SbiObjFuncOrganizer();

			SbiFunctionsOrganizer sfo = new SbiFunctionsOrganizer();
			Integer funcId = findRootFolder(userId);
			sfo.setFunctId(funcId);

			SbiObjects so = new SbiObjects();
			so.setBiobjId(documentId);

			SbiObjFuncOrganizerId compId = new SbiObjFuncOrganizerId();
			compId.setSbiFunctionsOrganizer(sfo);
			compId.setSbiObjects(so);

			hibDoc.setId(compId);
			hibDoc.setProg(1);

			updateSbiCommonInfo4Insert(hibDoc);
			aSession.save(hibDoc);

			tx.commit();
		} catch (HibernateException he) {

			logException(he);

			logger.error("HibernateException", he);

			if (tx != null)
				tx.rollback();

			/**
			 * Throw this specific exception so the service that called the Hibernate method can handle it and forward the information about the error towards
			 * the client (final user).
			 *
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			throw new HibernateException(he);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen()) {
					aSession.close();
				}
				logger.debug("OUT");
			}
		}
		return hibDoc;
	}

	@Override
	public void removeDocumentFromOrganizer(Integer folderId, Integer docId) {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			try {
				deleteOrganizerDocumentById(folderId, docId, aSession);
			} catch (Exception e) {
				logger.error("Document was not deleted from the organizer!", e);
			}
			tx.commit();
			logger.debug("Document was deleted from the organizer!");
		} catch (HibernateException he) {
			logException(he);
			logger.error("Error while deleting the document from organizer.", he);
			if (tx != null)
				tx.rollback();
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
			logger.debug("OUT");
		}
	}

	public void deleteOrganizerDocumentById(Integer folderId, Integer docId, Session aSession) throws Exception {
		logger.debug("IN");

		SbiFunctionsOrganizer sfo = new SbiFunctionsOrganizer();
		sfo.setFunctId(folderId);

		SbiObjects so = new SbiObjects();
		so.setBiobjId(docId);

		SbiObjFuncOrganizerId compId = new SbiObjFuncOrganizerId();
		compId.setSbiFunctionsOrganizer(sfo);
		compId.setSbiObjects(so);

		SbiObjFuncOrganizer documentToDelete = (SbiObjFuncOrganizer) aSession.load(SbiObjFuncOrganizer.class, compId);
		aSession.delete(documentToDelete);

		logger.debug("OUT");
	}

	@Override
	public void moveDocumentToDifferentFolder(Integer documentId, Integer sourceFolderId, Integer destinationFolderId) {

		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			loadAndUpdateDocumentInOrganizerById(documentId, sourceFolderId, destinationFolderId, aSession);
			tx.commit();
		} catch (HibernateException he) {

			logException(he);

			/**
			 * The new line is more accurate and precise than the commented one (the old one).
			 *
			 * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			// logger.error("Error while deleting the document from organizer.", he);
			logger.error("Error while moving the document to another (different) folder.", he);

			if (tx != null)
				tx.rollback();

			/**
			 * Throw this specific exception so the service that called the Hibernate method can handle it and forward the information about the error towards
			 * the client (final user).
			 *
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			throw new HibernateException(he);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
			logger.debug("OUT");
		}

	}

	private DocumentOrganizer toDocumentOrganizer(SbiObjFuncOrganizer hibObj) {

		DocumentOrganizer toReturn = new DocumentOrganizer();

		SbiObjects sbiObj = hibObj.getId().getSbiObjects();
		toReturn.setBiObjId(sbiObj.getBiobjId());
		toReturn.setPreviewFile(sbiObj.getPreviewFile());
		toReturn.setDocumentLabel(sbiObj.getLabel());
		toReturn.setDocumentName(sbiObj.getName());
		toReturn.setDocumentDescription(sbiObj.getDescr());
		toReturn.setDocumentType(sbiObj.getObjectTypeCode());
		toReturn.setFunctId(hibObj.getId().getSbiFunctionsOrganizer().getFunctId());

		/**
		 * Added for the tooltip for a document when searching through Organizer documents. The goal is to see from which folder the document that is hovered
		 * comes, to see the full path.
		 *
		 * NOTE: This is not really used on the client side, since this parameter provides the path that contains the code of the document, instead of its name.
		 * Since the name is displayed for the document, the user could not know for which folder which code corresponds. The DB table should be extended so to
		 * contain also a full path with the name of each folder's name in it.
		 *
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		toReturn.setDocumentPath(hibObj.getId().getSbiFunctionsOrganizer().getPath());

		return toReturn;
	}

	private Integer findRootFolder(String user) {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		SbiFunctionsOrganizer toReturn = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Criteria criteria = aSession.createCriteria(SbiFunctionsOrganizer.class);
			criteria.add(Restrictions.eq("code", user));
			toReturn = (SbiFunctionsOrganizer) criteria.uniqueResult();
			if (toReturn == null) {
				toReturn = createRootFolder(user);
			}
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			logger.error("Error while getting the root folder of the current user.", he);
			if (tx != null)
				tx.rollback();

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		logger.debug("OUT");
		return toReturn.getFunctId();
	}

	private void loadAndUpdateDocumentInOrganizerById(Integer docId, Integer folId, Integer destId, Session aSession) {
		addToNewFolder(docId, destId, aSession);
		try {
			deleteOrganizerDocumentById(folId, docId, aSession);
		} catch (Exception e) {
			logger.error("Document was not removed from source folder!", e);
		}
	}

	private SbiObjFuncOrganizer addToNewFolder(Integer documentId, Integer destId, Session aSession) {
		logger.debug("IN");
		SbiObjFuncOrganizer hibDoc = null;
		try {
			hibDoc = new SbiObjFuncOrganizer();

			SbiFunctionsOrganizer sfo = new SbiFunctionsOrganizer();
			sfo.setFunctId(destId);

			SbiObjects so = new SbiObjects();
			so.setBiobjId(documentId);

			SbiObjFuncOrganizerId compId = new SbiObjFuncOrganizerId();
			compId.setSbiFunctionsOrganizer(sfo);
			compId.setSbiObjects(so);

			hibDoc.setId(compId);
			hibDoc.setProg(1);

			updateSbiCommonInfo4Insert(hibDoc);
			aSession.save(hibDoc);

		} catch (HibernateException he) {
			logException(he);
			logger.error("Error while adding document to new folder.", he);
		} finally {
			logger.debug("OUT");
		}
		return hibDoc;
	}

	public SbiFunctionsOrganizer createRootFolder(String user) {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		SbiFunctionsOrganizer hibFunct = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			hibFunct = new SbiFunctionsOrganizer();
			hibFunct.setCode(user);
			hibFunct.setDescr("root");
			hibFunct.setName("root");
			hibFunct.setPath("/" + user);
			hibFunct.setParentFunct(null);
			hibFunct.setProg(1);

			updateSbiCommonInfo4Insert(hibFunct);
			aSession.save(hibFunct);

			tx.commit();
		} catch (HibernateException he) {
			logger.error("HibernateException", he);

			if (tx != null)
				tx.rollback();

		} finally {
			if (aSession != null) {
				if (aSession.isOpen()) {
					aSession.close();
				}
				logger.debug("OUT");
			}
		}
		return hibFunct;
	}

}
