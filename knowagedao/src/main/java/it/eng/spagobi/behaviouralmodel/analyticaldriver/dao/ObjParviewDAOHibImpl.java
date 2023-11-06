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

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjPar;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParview;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiObjParview;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.utilities.SpagoBITracer;

/**
 * Defines the Hibernate implementations for all DAO methods, for a ObjParview object.
 *
 * @author gavardi
 */
public class ObjParviewDAOHibImpl extends AbstractHibernateDAO implements IObjParviewDAO {

	/**
	 * Modify obj parview.
	 *
	 * @param aObjParview the a obj parview
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParviewDAO#modifyObjParview(it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParview)
	 */
	@Override
	public void modifyObjParview(ObjParview aObjParview) throws HibernateException {
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			// get the existing object
			String hql = "from SbiObjParview s where s.id = ? ";

			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, aObjParview.getId().intValue());

			SbiObjParview sbiObjParview = (SbiObjParview) hqlQuery.uniqueResult();
			if (sbiObjParview == null) {
				SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), "modifyObjParview",
						"the ObjParview relevant to BIObjectParameter with " + "id=" + aObjParview.getParId()
								+ "  does not exist.");

			}
			// delete the existing object
			// aSession.delete(sbiObjParview);
			// create the new object

			SbiObjPar sbiObjPar = (SbiObjPar) aSession.load(SbiObjPar.class, aObjParview.getParId());
			SbiObjPar sbiObjParFather = (SbiObjPar) aSession.load(SbiObjPar.class, aObjParview.getParFatherId());
			if (sbiObjParFather == null) {
				SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), "modifyObjParview",
						"the BIObjectParameter with " + " does not exist.");
			}

			sbiObjParview.setSbiObjPar(sbiObjPar);
			sbiObjParview.setSbiObjParFather(sbiObjParFather);
			sbiObjParview.setOperation(aObjParview.getOperation());
			sbiObjParview.setCompareValue(aObjParview.getCompareValue());
			sbiObjParview.setId(aObjParview.getId());
			sbiObjParview.setProg(aObjParview.getProg());
			sbiObjParview.setViewLabel(aObjParview.getViewLabel());

			// save new object
			updateSbiCommonInfo4Insert(sbiObjParview);
			aSession.update(sbiObjParview);
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			rollbackIfActive(tx);
			throw new HibernateException(he.getLocalizedMessage(), he);
		} finally {
			closeSessionIfOpen(aSession);
		}
		/*
		 * Criterion aCriterion = Expression.and( Expression.eq("id.sbiObjPar.objParId", aObjParuse.getObjParId()), Expression.eq("id.sbiParuse.useId",
		 * aObjParuse.getParuseId())); Criteria aCriteria = aSession.createCriteria(SbiObjParuse.class); aCriteria.add(aCriterion); SbiObjParuse sbiObjParuse =
		 * (SbiObjParuse) aCriteria.uniqueResult();
		 */
	}

	/**
	 * Insert obj parview.
	 *
	 * @param aObjParview the a obj parview
	 *
	 * @throws EMFviewrError the EMF user error
	 *
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParuseDAO#insertObjParuse(it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParuse)
	 */
	@Override
	public Integer insertObjParview(ObjParview aObjParview) throws HibernateException {
		SbiObjParview view = new SbiObjParview();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiObjPar sbiObjPar = (SbiObjPar) aSession.load(SbiObjPar.class, aObjParview.getParId());
			SbiObjPar sbiObjParFather = (SbiObjPar) aSession.load(SbiObjPar.class, aObjParview.getParFatherId());
			if (sbiObjParFather == null) {
				SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), "modifyObjParview",
						"the BIObjectParameter with " + "id=" + aObjParview.getParFatherId() + " does not exist.");

			}

			view.setSbiObjPar(sbiObjPar);
			view.setSbiObjParFather(sbiObjParFather);
			view.setOperation(aObjParview.getOperation());
			view.setCompareValue(aObjParview.getCompareValue());
			view.setProg(aObjParview.getProg());
			view.setViewLabel(aObjParview.getViewLabel());
			updateSbiCommonInfo4Insert(view);
			view.setId((Integer) aSession.save(view));
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			rollbackIfActive(tx);
			throw new HibernateException(he.getLocalizedMessage(), he);
		} finally {
			closeSessionIfOpen(aSession);
		}
		return view.getId();
	}

	/**
	 * Erase obj parview.
	 *
	 * @param aObjParview the a obj parview
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParviewDAO#eraseObjParview(ObjParview)
	 */
	@Override
	public void eraseObjParview(ObjParview aObjParview) throws HibernateException {
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			eraseObjParview(aObjParview, aSession);

			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			rollbackIfActive(tx);
			throw new HibernateException(he.getLocalizedMessage(), he);
		} finally {
			closeSessionIfOpen(aSession);
		}
		/*
		 * Criterion aCriterion = Expression.and( Expression.eq("id.sbiObjPar.objParId", aObjParuse.getObjParId()), Expression.eq("id.sbiParuse.useId",
		 * aObjParuse.getParuseId())); Criteria aCriteria = aSession.createCriteria(SbiObjParuse.class); aCriteria.add(aCriterion); SbiObjParuse sbiObjParuse =
		 * (SbiObjParuse)aCriteria.uniqueResult();
		 */
	}

	@Override
	public void eraseObjParview(ObjParview aObjParview, Session aSession) {
		// get the existing object
		/*
		 * String hql = "from SbiObjParuse s where s.id.sbiObjPar.objParId = " + aObjParuse.getObjParId() + " and s.id.sbiParuse.useId = " + aObjParuse.getParuseId() +
		 * " and s.id.sbiObjParFather.objParId = " + aObjParuse.getObjParFatherId() + " and s.id.filterOperation = '" + aObjParuse.getFilterOperation() + "'";
		 */
		String hql = "from SbiObjParview s where s.id = ?";

		Query hqlQuery = aSession.createQuery(hql);
		hqlQuery.setInteger(0, aObjParview.getId().intValue());

		SbiObjParview sbiObjParview = (SbiObjParview) hqlQuery.uniqueResult();
		if (sbiObjParview == null) {
			SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), "eraseObjParview",
					"the ObjParview relevant to BIObjectParameter with " + "id=" + aObjParview.getParId()
							+ " does not exist.");
		}
		aSession.delete(sbiObjParview);
	}

	/**
	 * Load obj parviews.
	 *
	 * @param objParId the obj par id
	 *
	 * @return the list
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParviewDAO#loadObjParviews(Integer)
	 */
	@Override
	public List<ObjParview> loadObjParviews(Integer objParId) throws HibernateException {
		List<ObjParview> toReturn = new ArrayList();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			// String hql = "from SbiObjParuse s where s.id.sbiObjPar.objParId = " + objParId + " order by s.prog";
			String hql = "from SbiObjParview s where s.sbiObjPar.objParId = ? order by s.prog";
			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, objParId.intValue());
			List sbiObjParviews = hqlQuery.list();
			Iterator it = sbiObjParviews.iterator();
			while (it.hasNext()) {
				toReturn.add(toObjParview((SbiObjParview) it.next()));
			}
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			rollbackIfActive(tx);
			throw new HibernateException(he.getLocalizedMessage(), he);
		} finally {
			closeSessionIfOpen(aSession);
		}
		return toReturn;
	}

	/**
	 * From the hibernate SbiObjParview at input, gives the corrispondent <code>ObjParview</code> object.
	 *
	 * @param aSbiObjParview The hybernate SbiObjParview
	 *
	 * @return The corrispondent <code>ObjParview</code>
	 */
	public ObjParview toObjParview(SbiObjParview aSbiObjParview) {
		if (aSbiObjParview == null)
			return null;
		ObjParview toReturn = new ObjParview();
		toReturn.setId(aSbiObjParview.getId());
		toReturn.setParId(aSbiObjParview.getSbiObjPar().getObjParId());
		toReturn.setParFatherId(aSbiObjParview.getSbiObjParFather().getObjParId());
		toReturn.setParFatherUrlName(aSbiObjParview.getSbiObjParFather().getParurlNm());
		toReturn.setOperation(aSbiObjParview.getOperation());
		toReturn.setCompareValue(aSbiObjParview.getCompareValue());
		toReturn.setProg(aSbiObjParview.getProg());
		toReturn.setViewLabel(aSbiObjParview.getViewLabel());

		return toReturn;
	}

	/**
	 * Gets the dependencies.
	 *
	 * @param objParFatherId the obj par father id
	 *
	 * @return the dependencies
	 *
	 * @throws EMFviewrError the EMF user error
	 *
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParviewDAO#getDependencies(Integer)
	 */
	@Override
	public List getDependencies(Integer objParFatherId) throws EMFUserError {
		List toReturn = new ArrayList();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			// get all the sbiobjparview objects which have the parameter as the father
			// String hql = "from SbiObjParview s where s.id.sbiObjParFather=" + objParFatherId;
			String hql = "from SbiObjParview s where s.sbiObjParFather=? ";
			Query query = aSession.createQuery(hql);
			query.setInteger(0, objParFatherId.intValue());
			List objParviews = query.list();
			if (objParviews == null || objParviews.size() == 0)
				return toReturn;
			// add to the list all the distinct labels of parameter which depend form the father parameter
			Iterator it = objParviews.iterator();
			while (it.hasNext()) {
				SbiObjParview objParviewHib = (SbiObjParview) it.next();
				Integer objParId = objParviewHib.getSbiObjPar().getObjParId();
				SbiObjPar hibObjPar = (SbiObjPar) aSession.load(SbiObjPar.class, objParId);
				String label = hibObjPar.getLabel();
				if (!toReturn.contains(label)) {
					toReturn.add(label);
				}
			}
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			rollbackIfActive(tx);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		return toReturn;
	}

	/**
	 * Gets the all dependencies for parameter view.
	 *
	 * @param viewId the view id
	 *
	 * @return the all dependencies for parameter view
	 *
	 * @throws EMFviewrError the EMF user error
	 *
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParviewDAO#getAllDependenciesForParameterview(java.lang.Integer)
	 */
	@Override
	public List getAllDependenciesForParameterview(Integer viewId) throws EMFUserError {
		List toReturn = new ArrayList();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			// String hql = "from SbiObjParview s where s.id.sbiParview.viewId = " + viewId;
			String hql = "from SbiObjParview s where s.sbiParview.viewId = ?";
			Query query = aSession.createQuery(hql);
			query.setInteger(0, viewId.intValue());
			List result = query.list();
			Iterator it = result.iterator();
			while (it.hasNext()) {
				toReturn.add(toObjParview((SbiObjParview) it.next()));
			}
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			rollbackIfActive(tx);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		return toReturn;
	}

	/**
	 * Gets the document labels list with associated dependencies.
	 *
	 * @param viewId the view id
	 *
	 * @return the document labels list with associated dependencies
	 *
	 * @throws EMFviewrError the EMF user error
	 *
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParviewDAO#getDocumentLabelsListWithAssociatedDependencies(java.lang.Integer)
	 */
	@Override
	public List getDocumentLabelsListWithAssociatedDependencies(Integer viewId) throws EMFUserError {
		List toReturn = new ArrayList();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			/*
			 * String hql = "select " + "	distinct(obj.label) " + "from " + "	SbiObjects obj, SbiObjParview s " + "where " +
			 * "	obj.sbiObjPars.objParId = s.id.sbiObjPar.objParId and " + "	s.id.sbiParview.viewId = " + viewId;
			 */
			String hql = "select " + "	distinct(obj.label) " + "from "
					+ "	SbiObjects obj, SbiObjPar p, SbiObjParview s " + "where "
					+ "	obj.biobjId = p.sbiObject.biobjId and " + "	p.objParId = s.sbiObjPar.objParId and ";
			Query query = aSession.createQuery(hql);
			query.setInteger(0, viewId.intValue());
			List result = query.list();
			toReturn = result;
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			rollbackIfActive(tx);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		return toReturn;
	}

	/**
	 * Load obj parview.
	 *
	 * @param objParId  the obj par id
	 * @param parviewId the parview id
	 *
	 * @return the list
	 *
	 * @throws EMFviewrError the EMF user error
	 *
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParviewDAO#loadObjParview(java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public List loadObjParview(Integer objParId, Integer parviewId) throws EMFUserError {
		List objparviews = new ArrayList();
		ObjParview toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			/*
			 * Criterion aCriterion = Expression.and( Expression.eq("id.sbiObjPar.objParId", objParId), Expression.eq("id.sbiParview.viewId", parviewId)); Criteria
			 * aCriteria = aSession.createCriteria(SbiObjParview.class); aCriteria.add(aCriterion); List sbiObjParviews = (List) aCriteria.list();
			 */
			/*
			 * String hql = "from SbiObjParview s where s.id.sbiObjPar.objParId=" + objParId + " and s.id.sbiParview.viewId=" + parviewId + " order by s.prog";
			 */
			String hql = "from SbiObjParview s where s.sbiObjPar.objParId=? " + " and s.sbiParview.viewId=? "
					+ " order by s.prog";

			Query query = aSession.createQuery(hql);
			query.setInteger(0, objParId.intValue());
			query.setInteger(1, parviewId.intValue());

			List sbiObjParviews = query.list();
			if (sbiObjParviews == null)
				return objparviews;
			Iterator itersbiOP = sbiObjParviews.iterator();
			while (itersbiOP.hasNext()) {
				SbiObjParview sbiop = (SbiObjParview) itersbiOP.next();
				ObjParview op = toObjParview(sbiop);
				objparviews.add(op);
			}
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			rollbackIfActive(tx);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		return objparviews;
	}

	/**
	 * Load obj parviews with father relationship
	 *
	 * @param objParId the obj par id
	 *
	 * @return the list
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParviewDAO#loadObjParviews(Integer)
	 */
	@Override
	public List<ObjParview> loadObjParviewsFather(Integer objParId) throws HibernateException {
		List<ObjParview> toReturn = new ArrayList();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			// String hql = "from SbiObjParuse s where s.id.sbiObjPar.objParId = " + objParId + " order by s.prog";
			String hql = "from SbiObjParview s where s.sbiObjParFather = ? order by s.prog";
			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, objParId.intValue());
			List sbiObjParviews = hqlQuery.list();
			Iterator it = sbiObjParviews.iterator();
			while (it.hasNext()) {
				toReturn.add(toObjParview((SbiObjParview) it.next()));
			}
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			rollbackIfActive(tx);
			throw new HibernateException(he.getLocalizedMessage(), he);
		} finally {
			closeSessionIfOpen(aSession);
		}
		return toReturn;
	}

	@Override
	public void eraseObjParviewIfExists(ObjParview aObjParview, Session aSession) throws HibernateException {
		// get the existing object
		/*
		 * String hql = "from SbiObjParuse s where s.id.sbiObjPar.objParId = " + aObjParuse.getObjParId() + " and s.id.sbiParuse.useId = " + aObjParuse.getParuseId() +
		 * " and s.id.sbiObjParFather.objParId = " + aObjParuse.getObjParFatherId() + " and s.id.filterOperation = '" + aObjParuse.getFilterOperation() + "'";
		 */
		String hql = "from SbiObjParview s where s.id = ? ";

		Query hqlQuery = aSession.createQuery(hql);
		hqlQuery.setInteger(0, aObjParview.getId().intValue());

		SbiObjParview sbiObjParview = (SbiObjParview) hqlQuery.uniqueResult();
		if (sbiObjParview == null) {
		} else {
			aSession.delete(sbiObjParview);
		}
	}

}
