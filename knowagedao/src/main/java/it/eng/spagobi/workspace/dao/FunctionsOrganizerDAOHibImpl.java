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
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.workspace.bo.FunctionsOrganizer;
import it.eng.spagobi.workspace.metadata.SbiFunctionsOrganizer;

public class FunctionsOrganizerDAOHibImpl extends AbstractHibernateDAO implements IFunctionsOrganizerDAO {

	private static transient Logger logger = Logger.getLogger(FunctionsOrganizerDAOHibImpl.class);

	@Override
	public List loadFolderByUser() {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List listOfFolders = null;
		List realResult = new ArrayList();
		try {
			aSession = getSession();
			IEngUserProfile user = getUserProfile();
			String userId = ((UserProfile) user).getUserId().toString();
			tx = aSession.beginTransaction();

			Criteria criteria = aSession.createCriteria(SbiFunctionsOrganizer.class);
			Criterion rest1 = Restrictions.eq("code", userId);
			Criterion rest2 = Restrictions.eq("commonInfo.userIn", userId);
			criteria.add(Restrictions.or(rest1, rest2));
			listOfFolders = criteria.list();
			Iterator it = listOfFolders.iterator();
			if (listOfFolders.isEmpty()) {
				SbiFunctionsOrganizer root = createRootFolder(userId);
				realResult.add(toFunctionsOrganizer(root));
			} else {
				while (it.hasNext()) {
					realResult.add(toFunctionsOrganizer((SbiFunctionsOrganizer) it.next()));
				}
			}
			tx.commit();
		} catch (HibernateException he) {
			logger.error("HibernateException", he);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		logger.debug("OUT");
		return realResult;
	}

	@Override
	public SbiFunctionsOrganizer createFolder(SbiFunctionsOrganizer folder) {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		SbiFunctionsOrganizer hibFunct = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			hibFunct = new SbiFunctionsOrganizer();
			hibFunct.setCode(folder.getCode());
			hibFunct.setDescr(folder.getDescr());
			hibFunct.setName(folder.getName());
			hibFunct.setPath(folder.getPath());
			hibFunct.setParentFunct(folder.getParentFunct());
			hibFunct.setProg(folder.getProg());

			updateSbiCommonInfo4Insert(hibFunct);
			aSession.save(hibFunct);

			tx.commit();
		} catch (HibernateException he) {

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
		return hibFunct;
	}

	@Override
	public void deleteFolder(Integer folderId) {

		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			deleteOrganizerFolderById(folderId, aSession);
			tx.commit();
		} catch (Exception he) {
			logException(he);
			logger.error("Error in deleting the folder from organizer", he);
			if (tx != null)
				tx.rollback();
			throw new SpagoBIRuntimeException("Could not delete folder", he);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
	}

	public void deleteOrganizerFolderById(Integer id, Session aSession) throws Exception {
		logger.debug("IN");
		SbiFunctionsOrganizer folderToDelete = (SbiFunctionsOrganizer) aSession.load(SbiFunctionsOrganizer.class, id);
		aSession.delete(folderToDelete);
	}

	public static FunctionsOrganizer toFunctionsOrganizer(SbiFunctionsOrganizer sfo) {

		FunctionsOrganizer fo = new FunctionsOrganizer();
		fo.setFunctId(sfo.getFunctId());
		fo.setCode(sfo.getCode());
		fo.setDescr(sfo.getDescr());
		fo.setName(sfo.getName());
		fo.setParentFunct(sfo.getParentFunct());
		fo.setPath(sfo.getPath());
		fo.setProg(sfo.getProg());
		fo.setUserIn(sfo.getCommonInfo().getUserIn());
		fo.setTimeIn(sfo.getCommonInfo().getTimeIn());

		return fo;
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
			hibFunct.setDescr(user);
			hibFunct.setName("Home");
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
