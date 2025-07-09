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
package it.eng.spagobi.behaviouralmodel.analyticaldriver.dao;

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
import org.hibernate.exception.ConstraintViolationException;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjPar;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParuse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParview;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiParameters;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.crossnavigation.dao.ICrossNavigationDAO;
import it.eng.spagobi.user.UserProfileManager;

/**
 * Defines the Hibernate implementations for all DAO methods, for a BI Object Parameter.
 *
 * @author Zoppello
 */
public class BIObjectParameterDAOHibImpl extends AbstractHibernateDAO implements IBIObjectParameterDAO {
	static private Logger logger = Logger.getLogger(BIObjectParameterDAOHibImpl.class);

	/**
	 * Load by id.
	 *
	 * @param id the id
	 *
	 * @return the sbi obj par
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IBIObjectParameterDAO#loadById(java.lang.Integer)
	 */
	@Override
	public SbiObjPar loadById(Integer id) throws EMFUserError {
		SbiObjPar hibObjPar = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			hibObjPar = (SbiObjPar) aSession.load(SbiObjPar.class, id);
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		return hibObjPar;
	}

	@Override
	public BIObjectParameter loadBiObjParameterById(Integer id) throws HibernateException {
		BIObjectParameter objPar = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiObjPar hibObjPar = (SbiObjPar) aSession.load(SbiObjPar.class, id);
			if (hibObjPar != null)
				objPar = toBIObjectParameter(hibObjPar);

			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			throw new HibernateException(he.getLocalizedMessage(), he);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		return objPar;
	}

	@Override
	public BIObjectParameter loadBiObjParameterByObjIdAndLabel(Integer objId, String label) throws EMFUserError {
		BIObjectParameter objPar = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Criterion idCriterrion = Expression.eq("sbiObject.biobjId", objId);
			Criterion labelCriterrion = Expression.eq("label", label);
			Criteria criteria = aSession.createCriteria(SbiObjPar.class);
			criteria.add(idCriterrion);
			criteria.add(labelCriterrion);

			SbiObjPar hibObjPar = (SbiObjPar) criteria.uniqueResult();
			if (hibObjPar != null)
				objPar = toBIObjectParameter(hibObjPar);

			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		return objPar;
	}

	/**
	 * Load for detail by obj par id.
	 *
	 * @param objParId the obj par id
	 *
	 * @return the BI object parameter
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IBIObjectParameterDAO#loadForDetailByObjParId(java.lang.Integer)
	 */
	@Override
	public BIObjectParameter loadForDetailByObjParId(Integer objParId) throws EMFUserError {

		BIObjectParameter toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiObjPar hibObjPar = (SbiObjPar) aSession.load(SbiObjPar.class, objParId);

			toReturn = toBIObjectParameter(hibObjPar);
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		return toReturn;
	}

	/**
	 * Modify bi object parameter.
	 *
	 * @param aBIObjectParameter the a bi object parameter
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IBIObjectParameterDAO#modifyBIObjectParameter(it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter)
	 */
	@Override
	public void modifyBIObjectParameter(BIObjectParameter aBIObjectParameter) throws HibernateException {

		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiObjPar hibObjPar = (SbiObjPar) aSession.load(SbiObjPar.class, aBIObjectParameter.getId());

			if (hibObjPar == null) {
				logger.error("the BIObjectParameter with id=" + aBIObjectParameter.getId() + " does not exist.");
			}

			SbiObjects aSbiObject = (SbiObjects) aSession.load(SbiObjects.class, aBIObjectParameter.getBiObjectID());
			SbiParameters aSbiParameter = (SbiParameters) aSession.load(SbiParameters.class, aBIObjectParameter.getParameter().getId());

            assert hibObjPar != null;
            hibObjPar.setSbiObject(aSbiObject);
			hibObjPar.setSbiParameter(aSbiParameter);
			hibObjPar.setLabel(aBIObjectParameter.getLabel());
			hibObjPar.setReqFl(aBIObjectParameter.getRequired().shortValue());
			hibObjPar.setModFl(aBIObjectParameter.getModifiable().shortValue());
			hibObjPar.setViewFl(aBIObjectParameter.getVisible().shortValue());
			hibObjPar.setMultFl(aBIObjectParameter.getMultivalue().shortValue());
			hibObjPar.setParurlNm(aBIObjectParameter.getParameterUrlName());

			Integer colSpan = aBIObjectParameter.getColSpan();
			Integer thickPerc = aBIObjectParameter.getThickPerc();

			Integer oldPriority = hibObjPar.getPriority();
			Integer newPriority = aBIObjectParameter.getPriority();
			if (!oldPriority.equals(newPriority)) {
				Query query;
				if (oldPriority > newPriority) {
					String hqlUpdateShiftRight = "update SbiObjPar s set s.priority = (s.priority + 1) where s.priority >= :newPriority"
							+ " and s.priority < :oldPriority and s.sbiObject.biobjId = :biobjId";
					query = aSession.createQuery(hqlUpdateShiftRight);
					query.setParameter("oldPriority", oldPriority);
					query.setParameter("newPriority", newPriority);
					query.setParameter("biobjId", aSbiObject.getBiobjId());
				} else {
					String hqlUpdateShiftLeft = "update SbiObjPar s set s.priority = (s.priority - 1) where s.priority > :oldPriority  and s.priority <= "
							+ " :newPriority and s.sbiObject.biobjId = :biobjId";
					query = aSession.createQuery(hqlUpdateShiftLeft);
					query.setParameter("oldPriority", oldPriority);
					query.setParameter("newPriority", newPriority);
					query.setParameter("biobjId", aSbiObject.getBiobjId());
				}
				query.executeUpdate();
			}
			hibObjPar.setPriority(newPriority);
			hibObjPar.setProg(1);
			hibObjPar.setColSpan(colSpan);
			hibObjPar.setThickPerc(thickPerc);

			updateSbiCommonInfo4Update(hibObjPar);
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			throw new HibernateException(he.getLocalizedMessage(), he);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
	}

	/**
	 * Insert bi object parameter.
	 *
	 * @param aBIObjectParameter the a bi object parameter
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IBIObjectParameterDAO#insertBIObjectParameter(it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter)
	 */
	@Override
	public Integer insertBIObjectParameter(BIObjectParameter aBIObjectParameter) throws HibernateException {
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiObjects aSbiObject = (SbiObjects) aSession.load(SbiObjects.class, aBIObjectParameter.getBiObjectID());
			SbiParameters aSbiParameter = (SbiParameters) aSession.load(SbiParameters.class, aBIObjectParameter.getParameter().getId());

			SbiObjPar hibObjectParameterNew = new SbiObjPar();
			hibObjectParameterNew.setSbiObject(aSbiObject);
			hibObjectParameterNew.setSbiParameter(aSbiParameter);
			hibObjectParameterNew.setProg(new Integer(1));
			hibObjectParameterNew.setLabel(aBIObjectParameter.getLabel());
			hibObjectParameterNew.setReqFl(new Short(aBIObjectParameter.getRequired().shortValue()));
			hibObjectParameterNew.setModFl(new Short(aBIObjectParameter.getModifiable().shortValue()));
			hibObjectParameterNew.setViewFl(new Short(aBIObjectParameter.getVisible().shortValue()));
			hibObjectParameterNew.setMultFl(new Short(aBIObjectParameter.getMultivalue().shortValue()));
			hibObjectParameterNew.setParurlNm(aBIObjectParameter.getParameterUrlName());
			hibObjectParameterNew.setColSpan(aBIObjectParameter.getColSpan());
			hibObjectParameterNew.setThickPerc(aBIObjectParameter.getThickPerc());

			String hqlUpdateShiftRight = "update SbiObjPar s set s.priority = (s.priority + 1) where s.priority >= :aBIObjectParameterPriority"
					+ " and s.sbiObject.biobjId = :biobjId";
			Query query = aSession.createQuery(hqlUpdateShiftRight);
			query.setParameter("aBIObjectParameterPriority", aBIObjectParameter.getPriority());
			query.setParameter("biobjId", aSbiObject.getBiobjId());
			query.executeUpdate();

			hibObjectParameterNew.setPriority(aBIObjectParameter.getPriority());
			updateSbiCommonInfo4Insert(hibObjectParameterNew);
			hibObjectParameterNew.getCommonInfo().setUserIn((String) UserProfileManager.getProfile().getUserId());
			Integer id = (Integer) aSession.save(hibObjectParameterNew);
			tx.commit();
			return id;
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();

			throw new HibernateException(he.getLocalizedMessage(), he);

		} finally {

			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}

		}

	}

	/**
	 * Erase bi object parameter.
	 *
	 * @param aBIObjectParameter the a bi object parameter
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IBIObjectParameterDAO#eraseBIObjectParameter(it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter)
	 */
	@Override
	public void eraseBIObjectParameter(BIObjectParameter aBIObjectParameter, boolean alsoDependencies) throws HibernateException {

		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			eraseBIObjectParameter(aBIObjectParameter, aSession, alsoDependencies);

			tx.commit();
		} catch (ConstraintViolationException e) {
			throw new HibernateException(e.getLocalizedMessage(), e);
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			throw new HibernateException(he.getLocalizedMessage(), he);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
	}

	@Override
	public void eraseBIObjectParameterDependencies(BIObjectParameter aBIObjectParameter, Session aSession) throws EMFUserError {
		logger.debug("IN");
		logger.debug("Delete dependencies for object parameter with id " + aBIObjectParameter.getId());
		SbiObjPar hibObjPar = (SbiObjPar) aSession.load(SbiObjPar.class, aBIObjectParameter.getId());

		if (hibObjPar == null) {
			logger.error("the BIObjectParameter with id=" + aBIObjectParameter.getId() + " does not exist.");
			throw new EMFUserError(EMFErrorSeverity.ERROR, 1034);
		}

		// deletes all ObjParuse object (dependencies) of the biObjectParameter
		ObjParuseDAOHibImpl objParuseDAO = new ObjParuseDAOHibImpl();
		List objParuses = objParuseDAO.loadObjParuses(hibObjPar.getObjParId());
		Iterator itObjParuses = objParuses.iterator();
		while (itObjParuses.hasNext()) {
			ObjParuse aObjParuse = (ObjParuse) itObjParuses.next();
			objParuseDAO.eraseObjParuse(aObjParuse, aSession);
		}

		// delete also all ObjParView (visibility dependencies) of the biObjectParameter
		IObjParviewDAO objParviewDAO = DAOFactory.getObjParviewDAO();
		List objParview = objParviewDAO.loadObjParviews(hibObjPar.getObjParId());
		Iterator itObjParviews = objParview.iterator();
		while (itObjParviews.hasNext()) {
			ObjParview aObjParview = (ObjParview) itObjParviews.next();
			objParviewDAO.eraseObjParview(aObjParview, aSession);
		}
		logger.debug("OUT");

	}

	@Override
	public void eraseBIObjectParametersByObjectId(Integer biObjId, Session currSession) throws EMFUserError {
		logger.debug("IN");
		SbiObjects hibObjects = null;
		try {
			hibObjects = (SbiObjects) currSession.load(SbiObjects.class, biObjId);
			Set<SbiObjPar> setObjPars = hibObjects.getSbiObjPars();

			logger.debug("delete all objParameters for obj with label " + hibObjects.getLabel());

			for (Iterator iterator = setObjPars.iterator(); iterator.hasNext();) {
				SbiObjPar sbiObjPar = (SbiObjPar) iterator.next();
				BIObjectParameter biObjPar = toBIObjectParameter(sbiObjPar);
				logger.debug("delete biObjPar with label " + sbiObjPar.getLabel() + " and url name " + sbiObjPar.getParurlNm());
				eraseBIObjectParameter(biObjPar, currSession, true);
			}
		} catch (Exception he) {
			logger.error("Erro while deleting obj pars associated to document with label = " + hibObjects != null ? hibObjects.getLabel() : "null", he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		}

		// aaa
		logger.debug("OUT");
	}

	public void eraseBIObjectParameter(BIObjectParameter aBIObjectParameter, Session aSession, boolean alsoDependencies) throws HibernateException {
		SbiObjPar hibObjPar = (SbiObjPar) aSession.load(SbiObjPar.class, aBIObjectParameter.getId());

		if (hibObjPar == null) {
			logger.error("the BIObjectParameter with id=" + aBIObjectParameter.getId() + " does not exist.");
		}

		if (alsoDependencies) {
			// deletes all ObjParuse object (dependencies) of the biObjectParameter
			ObjParuseDAOHibImpl objParuseDAO = new ObjParuseDAOHibImpl();
			List objParuses = objParuseDAO.loadObjParuses(hibObjPar.getObjParId());
			Iterator itObjParuses = objParuses.iterator();
			while (itObjParuses.hasNext()) {
				ObjParuse aObjParuse = (ObjParuse) itObjParuses.next();
				objParuseDAO.eraseObjParuseIfExists(aObjParuse, aSession);
			}

			// deletes all ObjParuse object (dependencies) of the biObjectParameter that have a father relationship
			List objParusesFather = objParuseDAO.loadObjParusesFather(hibObjPar.getObjParId());
			Iterator itObjParusesFather = objParusesFather.iterator();
			while (itObjParusesFather.hasNext()) {
				ObjParuse aObjParuseFather = (ObjParuse) itObjParusesFather.next();
				objParuseDAO.eraseObjParuseIfExists(aObjParuseFather, aSession);
			}

			// delete also all ObjParView (visibility dependencies) of the biObjectParameter
			IObjParviewDAO objParviewDAO = DAOFactory.getObjParviewDAO();
			List objParview = objParviewDAO.loadObjParviews(hibObjPar.getObjParId());
			Iterator itObjParviews = objParview.iterator();
			while (itObjParviews.hasNext()) {
				ObjParview aObjParview = (ObjParview) itObjParviews.next();
				objParviewDAO.eraseObjParviewIfExists(aObjParview, aSession);
			}

			// delete also all ObjParView (visibility dependencies) of the biObjectParameter father
			List objParviewFather = objParviewDAO.loadObjParviewsFather(hibObjPar.getObjParId());
			Iterator itObjParviewsFather = objParviewFather.iterator();
			while (itObjParviewsFather.hasNext()) {
				ObjParview aObjParviewFather = (ObjParview) itObjParviewsFather.next();
				objParviewDAO.eraseObjParviewIfExists(aObjParviewFather, aSession);
			}

			// Delete CROSS NAVIGATION PARAMETER
			ICrossNavigationDAO crossNavigationDao = DAOFactory.getCrossNavigationDAO();
			crossNavigationDao.deleteByBIObjectParameter(aBIObjectParameter, aSession);

		}

		aSession.delete(hibObjPar);

		Integer biobjId = hibObjPar.getSbiObject().getBiobjId();

		String hqlUpdateShiftRight = "update SbiObjPar s set s.priority = (s.priority - 1) where s.priority >= :hibObjParPriority"
				+ " and s.sbiObject.biobjId = :biobjId";
		Query query = aSession.createQuery(hqlUpdateShiftRight);
		query.setParameter("hibObjParPriority", hibObjPar.getPriority());
		query.setParameter("biobjId", biobjId);
		query.executeUpdate();
	}

	/**
	 * Gets the document labels list using parameter.
	 *
	 * @param parId the par id
	 *
	 * @return the document labels list using parameter
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IBIObjectParameterDAO#getDocumentLabelsListUsingParameter(java.lang.Integer)
	 */
	@Override
	public List<String> getDocumentLabelsListUsingParameter(Integer parId) throws HibernateException {

		List<String> toReturn = new ArrayList<>();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			String hql = "select " + "	distinct(obj.label) " + "from " + "	SbiObjects obj, SbiObjPar objPar " + "where "
					+ "	obj.biobjId = objPar.sbiObject.biobjId and " + "	objPar.sbiParameter.parId = :parId";
			Query query = aSession.createQuery(hql);
			query.setParameter("parId", parId);
			List<String> result = query.list();

			toReturn = result;

			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			throw new HibernateException(he.getLocalizedMessage(), he);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		return toReturn;
	}

	/**
	 * Load bi object parameters by id.
	 *
	 * @param biObjectID the bi object id
	 *
	 * @return the list
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IBIObjectParameterDAO#loadBIObjectParametersById(java.lang.Integer)
	 */
	@Override
	public List<BIObjectParameter> loadBIObjectParametersById(Integer biObjectID) throws HibernateException {

		Session aSession = null;
		Transaction tx = null;
		List<BIObjectParameter> resultList = new ArrayList<>();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			String hql = "from SbiObjPar s where s.sbiObject.biobjId = :biObjectID  order by s.priority asc";

			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setParameter("biObjectID", biObjectID);
			List hibObjectPars = hqlQuery.list();

			Iterator it = hibObjectPars.iterator();
			int count = 1;
			while (it.hasNext()) {
				BIObjectParameter aBIObjectParameter = toBIObjectParameter((SbiObjPar) it.next());
				// *****************************************************************
				// **************** START PRIORITY CONTROL *************************
				// *****************************************************************
				Integer priority = aBIObjectParameter.getPriority();
				// if the priority is different from the value expected,
				// recalculates it for all the parameter of the document
				if (priority == null || priority.intValue() != count) {
					logger.error(
							"The priorities of the biparameters for the document with id = " + biObjectID + " are not sorted. Priority recalculation starts.");
					recalculateBiParametersPriority(biObjectID, aSession);
					// restarts this method in order to load updated priorities
					aBIObjectParameter.setPriority(new Integer(count));
				}
				count++;
				// *****************************************************************
				// **************** END PRIORITY CONTROL ***************************
				// *****************************************************************
				resultList.add(aBIObjectParameter);
			}
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			throw new HibernateException(he.getLocalizedMessage(), he);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		return resultList;
	}

	/**
	 * Recalculates the priority of all the BiParameters of the document, identified by its biObjectID, in the Hibernate session passed at input.
	 *
	 * @param biObjectID The id of the document
	 * @param aSession   The Hibernate session
	 */
	public void recalculateBiParametersPriority(Integer biObjectID, Session aSession) {
		String hql = "from SbiObjPar s where s.sbiObject.biobjId = :biObjectID order by s.priority asc";
		Query hqlQuery = aSession.createQuery(hql);
		hqlQuery.setParameter("biObjectID", biObjectID);
		List hibObjectPars = hqlQuery.list();
		Iterator it = hibObjectPars.iterator();
		int count = 1;
		while (it.hasNext()) {
			SbiObjPar aSbiObjPar = (SbiObjPar) it.next();
			aSbiObjPar.setPriority(new Integer(count));
			count++;
			aSession.save(aSbiObjPar);
		}
	}

	/**
	 * From the hibernate BI object parameter at input, gives the corrispondent <code>BIObjectParameter</code> object.
	 *
	 * @param hiObjPar The hybernate BI object parameter
	 *
	 * @return The corrispondent <code>BIObjectParameter</code>
	 */
	public BIObjectParameter toBIObjectParameter(SbiObjPar hiObjPar) {
		BIObjectParameter aBIObjectParameter = new BIObjectParameter();
		aBIObjectParameter.setId(hiObjPar.getObjParId());
		aBIObjectParameter.setLabel(hiObjPar.getLabel());
		aBIObjectParameter.setModifiable(new Integer(hiObjPar.getModFl().intValue()));
		aBIObjectParameter.setMultivalue(new Integer(hiObjPar.getMultFl().intValue()));
		aBIObjectParameter.setBiObjectID(hiObjPar.getSbiObject().getBiobjId());
		aBIObjectParameter.setParameterUrlName(hiObjPar.getParurlNm());
		aBIObjectParameter.setParID(hiObjPar.getSbiParameter().getParId());
		aBIObjectParameter.setRequired(new Integer(hiObjPar.getReqFl().intValue()));
		aBIObjectParameter.setVisible(new Integer(hiObjPar.getViewFl().intValue()));
		aBIObjectParameter.setPriority(hiObjPar.getPriority());
		aBIObjectParameter.setProg(hiObjPar.getProg());
		aBIObjectParameter.setColSpan(hiObjPar.getColSpan());
		aBIObjectParameter.setThickPerc(hiObjPar.getThickPerc());

		Parameter parameter = new Parameter();
		parameter.setId(hiObjPar.getSbiParameter().getParId());
		parameter.setType(hiObjPar.getSbiParameter().getParameterTypeCode());
		aBIObjectParameter.setParameter(parameter);
		return aBIObjectParameter;
	}

}
