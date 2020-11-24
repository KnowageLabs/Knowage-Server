/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.dossier.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Expression;

import it.eng.knowage.engine.dossier.activity.bo.DossierActivity;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.dossier.metadata.SbiDossierActivity;
import it.eng.spagobi.tools.massiveExport.metadata.SbiProgressThread;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class SbiDossierActivityDAOHibImpl extends AbstractHibernateDAO implements ISbiDossierActivityDAO {

	private static transient Logger logger = Logger.getLogger(SbiDossierActivityDAOHibImpl.class);

	@Override
	public Integer insertNewActivity(DossierActivity dossierActivity) {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		Integer id = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiDossierActivity hibDossierActivity = toSbiDossierActivity(dossierActivity);

			updateSbiCommonInfo4Update(hibDossierActivity);
			id = (Integer) aSession.save(hibDossierActivity);

			tx.commit();

			logger.debug("Dossier activity created correctly with id: " + id);

		} catch (HibernateException he) {
			if (tx != null)
				tx.rollback();
			logger.error("Exception creating a new dossier activity", he);
			throw new SpagoBIRuntimeException("Exception creating a new dossier activity", he);
		} catch (Exception e) {
			if (tx != null)
				tx.rollback();
			logger.error("Exception creating a new dossier activity", e);
			throw new SpagoBIRuntimeException("Exception creating a new dossier activity", e);

		} finally {

			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}

		return id;
	}

	@Override
	public Integer updateActivity(DossierActivity dossierActivity, byte[] file, String type) {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		Integer id = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiDossierActivity hibDossierActivity = toSbiDossierActivity(dossierActivity);

			if (type.equals("ppt")) {
				hibDossierActivity.setBinContent(file);
			} else if (type.equals("doc")) {
				hibDossierActivity.setDocBinContent(file);
			}

			updateSbiCommonInfo4Update(hibDossierActivity);
			aSession.update(hibDossierActivity);

			tx.commit();

			logger.debug("Dossier activity updated correctly. Id of activity: " + dossierActivity.getId());

		} catch (HibernateException he) {
			if (tx != null)
				tx.rollback();
			logger.error("Exception while updating a dossier activity with id: " + dossierActivity.getId(), he);
			throw new SpagoBIRuntimeException("Exception while updating a dossier activity with id: " + dossierActivity.getId(), he);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		return id;
	}

	@Override
	public List<DossierActivity> loadAllActivities(Integer documentId) {
		logger.debug("IN");
		Session aSession = null;
		List<DossierActivity> listOfDossierActivities = new ArrayList<DossierActivity>();
		try {
			aSession = getSession();
			List<SbiDossierActivity> listSDA = new ArrayList<SbiDossierActivity>();

			Criterion aCriterion = Expression.eq("documentId", documentId);
			Criteria criteria = aSession.createCriteria(SbiDossierActivity.class);
			criteria.add(aCriterion);

			listSDA = criteria.list();

			for (int i = 0; i < listSDA.size(); i++) {
				listOfDossierActivities.add(toDossierActivity(listSDA.get(i)));
			}

		} catch (HibernateException he) {
			logger.error("Exception while laoding all dossier activities", he);
			throw new SpagoBIRuntimeException("Exception while laoding all dossier activities", he);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		return listOfDossierActivities;
	}

	@Override
	public DossierActivity loadActivity(Integer activityId) {
		logger.debug("IN");
		Session aSession = null;
		DossierActivity da = null;

		try {
			aSession = getSession();

			Criterion aCriterion = Expression.eq("id", activityId);
			Criteria criteria = aSession.createCriteria(SbiDossierActivity.class);
			criteria.add(aCriterion);

			SbiDossierActivity hibDa = (SbiDossierActivity) criteria.uniqueResult();

			if (hibDa == null)
				return null;
			da = toDossierActivity(hibDa);

			logger.debug("Loaded activity with id: " + activityId);
		} catch (HibernateException he) {
			logger.error("Exception while loading dossier activity with id: " + activityId, he);
			throw new SpagoBIRuntimeException("Exception while loading dossier activity with id: " + activityId, he);
		} finally {

			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		return da;
	}

	@Override
	public void deleteActivity(Integer activityId) {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiDossierActivity hibDossierActivity = (SbiDossierActivity) aSession.load(SbiDossierActivity.class, new Integer(activityId));
			aSession.delete(hibDossierActivity);

			tx.commit();
		} catch (HibernateException he) {
			if (tx != null)
				tx.rollback();
			logger.error("Exception creating a new dossier activity", he);
			throw new SpagoBIRuntimeException("Exception creating a new dossier activity", he);
		} finally {
			logger.debug("Dossier activity created correctly");
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
	}

	public static DossierActivity toDossierActivity(SbiDossierActivity hibDossierActivity) {

		DossierActivity da = new DossierActivity();

		da.setId(hibDossierActivity.getId());
		da.setActivity(hibDossierActivity.getActivity());
		da.setDocumentId(hibDossierActivity.getDocumentId());
		da.setParameters(hibDossierActivity.getParameters());
		da.setProgressId(hibDossierActivity.getProgress().getProgressThreadId());
		da.setPartial(hibDossierActivity.getProgress().getPartial());
		da.setStatus(hibDossierActivity.getProgress().getStatus());
		da.setTotal(hibDossierActivity.getProgress().getTotal());

		if (hibDossierActivity.getBinContent() != null) {
			da.setPptExists(true);
			da.setBinContent(hibDossierActivity.getBinContent());
			da.setHasBinContent(true);
		} else {
			da.setPptExists(false);
			da.setBinContent(null);
			da.setHasBinContent(false);
		}

		if (hibDossierActivity.getDocBinContent() != null) {
			da.setDocBinContent(hibDossierActivity.getDocBinContent());
			da.setHasDocBinContent(true);
		} else {
			da.setDocBinContent(null);
			da.setHasDocBinContent(false);
		}
		da.setCreationDate(hibDossierActivity.getCommonInfo().getTimeIn());
		da.setConfigContent(hibDossierActivity.getConfigContent());

		return da;
	}

	public SbiDossierActivity toSbiDossierActivity(DossierActivity dossierActivity) {
		SbiDossierActivity sda = new SbiDossierActivity();

		sda.setId(dossierActivity.getId());
		sda.setActivity(dossierActivity.getActivity());
		sda.setDocumentId(dossierActivity.getDocumentId());
		sda.setParameters(dossierActivity.getParameters());
		sda.setConfigContent(dossierActivity.getConfigContent());
		UserProfile userProfile = (UserProfile) this.getUserProfile();
		if (dossierActivity.getProgressId() != null) {
			sda.setProgress(new SbiProgressThread(dossierActivity.getProgressId(), (String) userProfile.getUserId()));
		}
		// sda.setPpt(dossierActivity.getBinId());

		return sda;
	}

	@Override
	public DossierActivity loadActivityByProgressThreadId(Integer progressthreadId) {
		logger.debug("IN");
		Session aSession = null;
		DossierActivity da = null;

		try {
			aSession = getSession();

			Criterion aCriterion = Expression.eq("progress.progressThreadId", progressthreadId);
			Criteria criteria = aSession.createCriteria(SbiDossierActivity.class);
			criteria.add(aCriterion);

			SbiDossierActivity hibDa = (SbiDossierActivity) criteria.uniqueResult();

			if (hibDa == null)
				return null;
			da = toDossierActivity(hibDa);

			logger.debug("Loaded activity with progressthreadId: " + progressthreadId);
		} catch (HibernateException he) {
			logger.error("Exception while loading dossier activity with id: " + progressthreadId, he);
			throw new SpagoBIRuntimeException("Exception while loading dossier activity with id: " + progressthreadId, he);
		} finally {

			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		return da;
	}

	@Override
	public Integer updateActivity(DossierActivity dossierActivity) {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		Integer id = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiDossierActivity hibDossierActivity = toSbiDossierActivity(dossierActivity);
			updateSbiCommonInfo4Update(hibDossierActivity);
			aSession.update(hibDossierActivity);

			tx.commit();

			logger.debug("Dossier activity updated correctly. Id of activity: " + dossierActivity.getId());

		} catch (HibernateException he) {
			if (tx != null)
				tx.rollback();
			logger.error("Exception while updating a dossier activity with id: " + dossierActivity.getId(), he);
			throw new SpagoBIRuntimeException("Exception while updating a dossier activity with id: " + dossierActivity.getId(), he);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		return id;
	}

}
