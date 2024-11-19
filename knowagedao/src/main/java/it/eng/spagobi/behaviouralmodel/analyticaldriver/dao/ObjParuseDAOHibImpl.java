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
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParuse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiObjParuse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiParuse;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.utilities.SpagoBITracer;

/**
 * Defines the Hibernate implementations for all DAO methods, for a ObjParuse object.
 *
 * @author Zerbetto
 */
public class ObjParuseDAOHibImpl extends AbstractHibernateDAO implements IObjParuseDAO {

	/**
	 * Modify obj paruse.
	 *
	 * @param aObjParuse the a obj paruse
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParuseDAO#modifyObjParuse(it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParuse)
	 */
	@Override
	public void modifyObjParuse(ObjParuse aObjParuse) throws HibernateException {
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			// get the existing object
			String hql = "from SbiObjParuse s where s.id= ? ";

			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, aObjParuse.getId().intValue());

			SbiObjParuse sbiObjParuse = (SbiObjParuse) hqlQuery.uniqueResult();
			if (sbiObjParuse == null) {
				SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), "modifyObjParuse",
						"the ObjParuse relevant to BIObjectParameter with " + "id=" + aObjParuse.getParId()
								+ " and ParameterUse with " + "id=" + aObjParuse.getUseModeId() + " does not exist.");
			}
			aSession.clear();
			SbiObjPar sbiObjPar = (SbiObjPar) aSession.load(SbiObjPar.class, aObjParuse.getParId());
			SbiParuse sbiParuse = (SbiParuse) aSession.load(SbiParuse.class, aObjParuse.getUseModeId());
			this.checksDataConsistency(sbiObjPar, sbiParuse);
			SbiObjPar sbiObjParFather = (SbiObjPar) aSession.load(SbiObjPar.class, aObjParuse.getParFatherId());
			if (sbiObjParFather == null) {
				SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), "modifyObjParuse",
						"the BIObjectParameter with " + "id=" + aObjParuse.getParFatherId() + " does not exist.");
			}
			SbiObjParuse correlation = new SbiObjParuse(aObjParuse.getId());

			correlation.setSbiObjPar(sbiObjPar);
			correlation.setSbiParuse(sbiParuse);
			correlation.setSbiObjParFather(sbiObjParFather);
			correlation.setFilterOperation(aObjParuse.getFilterOperation());
			correlation.setProg(aObjParuse.getProg());
			correlation.setFilterColumn(aObjParuse.getFilterColumn());
			correlation.setPreCondition(aObjParuse.getPreCondition());
			correlation.setPostCondition(aObjParuse.getPostCondition());
			correlation.setLogicOperator(aObjParuse.getLogicOperator());

			updateSbiCommonInfo4Insert(correlation);
			aSession.update(correlation);
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			rollbackIfActive(tx);
			throw new HibernateException(he.getLocalizedMessage(), he);
		} finally {
			closeSessionIfOpen(aSession);
		}
	}

	/**
	 * Insert obj paruse.
	 *
	 * @param aObjParuse the a obj paruse
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParuseDAO#insertObjParuse(it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParuse)
	 */
	@Override
	public Integer insertObjParuse(ObjParuse aObjParuse) throws HibernateException {

		Session aSession = null;
		Transaction tx = null;
		SbiObjParuse correlation = new SbiObjParuse();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiObjPar sbiObjPar = (SbiObjPar) aSession.load(SbiObjPar.class, aObjParuse.getParId());
			SbiParuse sbiParuse = (SbiParuse) aSession.load(SbiParuse.class, aObjParuse.getUseModeId());
			this.checksDataConsistency(sbiObjPar, sbiParuse);
			SbiObjPar sbiObjParFather = (SbiObjPar) aSession.load(SbiObjPar.class, aObjParuse.getParFatherId());
			if (sbiObjParFather == null) {
				SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), "modifyObjParuse",
						"the BIObjectParameter with " + "id=" + aObjParuse.getParFatherId() + " does not exist.");

			}

			correlation.setSbiObjPar(sbiObjPar);
			correlation.setSbiParuse(sbiParuse);
			correlation.setSbiObjParFather(sbiObjParFather);
			correlation.setFilterOperation(aObjParuse.getFilterOperation());
			correlation.setProg(aObjParuse.getProg());
			correlation.setFilterColumn(aObjParuse.getFilterColumn());
			correlation.setPreCondition(aObjParuse.getPreCondition());
			correlation.setPostCondition(aObjParuse.getPostCondition());
			correlation.setLogicOperator(aObjParuse.getLogicOperator());
			updateSbiCommonInfo4Insert(correlation);
			correlation.changeId((Integer) aSession.save(correlation));
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			rollbackIfActive(tx);
			throw new HibernateException(he.getLocalizedMessage(), he);
		} finally {
			closeSessionIfOpen(aSession);
		}
		return correlation.getId();
	}

	private void checksDataConsistency(SbiObjPar sbiObjPar, SbiParuse sbiParuse) {
		if (!sbiObjPar.getSbiParameter().getParId().equals(sbiParuse.getSbiParameters().getParId())) {
			String message = "SbiParameter in  SbiObjPar:  " + sbiObjPar.getSbiParameter().getParId() + " does not equal to SbiParameter in SbiParuse: "
					+ sbiParuse.getSbiParameters().getParId();
			SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), "modifyObjParuse", message);
			throw new IllegalArgumentException("Error links:  " + message);
		}
	}

	/**
	 * Erase obj paruse.
	 *
	 * @param aObjParuse the a obj paruse
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParuseDAO#eraseObjParuse(ObjParuse)
	 */
	@Override
	public void eraseObjParuse(ObjParuse aObjParuse) throws HibernateException {
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			eraseObjParuse(aObjParuse, aSession);

			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			rollbackIfActive(tx);
			throw new HibernateException(he.getLocalizedMessage(), he);
		} finally {
			closeSessionIfOpen(aSession);
		}
	}

	public void eraseObjParuse(ObjParuse aObjParuse, Session aSession) {
		// get the existing object
		String hql = "from SbiObjParuse s where s.id = ? ";
		Query hqlQuery = aSession.createQuery(hql);
		hqlQuery.setInteger(0, aObjParuse.getId().intValue());

		SbiObjParuse sbiObjParuse = (SbiObjParuse) hqlQuery.uniqueResult();
		if (sbiObjParuse == null) {
			SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), "eraseObjParuse",
					"the ObjParuse relevant to BIObjectParameter with " + "id=" + aObjParuse.getParId()
							+ " and ParameterUse with " + "id=" + aObjParuse.getUseModeId() + " does not exist.");
		}
		aSession.delete(sbiObjParuse);
	}

	/**
	 * Load obj paruses.
	 *
	 * @param objParId the obj par id
	 *
	 * @return the list
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParuseDAO#loadObjParuses(Integer)
	 */
	@Override
	public List<ObjParuse> loadObjParuses(Integer objParId) throws HibernateException {
		List<ObjParuse> toReturn = new ArrayList<>();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			String hql = "from SbiObjParuse s where s.sbiObjPar.objParId = ? order by s.prog";
			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, objParId.intValue());
			List<SbiObjParuse> sbiObjParuses = hqlQuery.list();
			Iterator<SbiObjParuse> it = sbiObjParuses.iterator();
			while (it.hasNext()) {
				toReturn.add(toObjParuse(it.next()));
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
	 * From the hibernate SbiObjParuse at input, gives the corrispondent <code>ObjParuse</code> object.
	 *
	 * @param aSbiObjParuse The hybernate SbiObjParuse
	 *
	 * @return The corrispondent <code>ObjParuse</code>
	 */
	public ObjParuse toObjParuse(SbiObjParuse aSbiObjParuse) {
		if (aSbiObjParuse == null) {
			return null;
		}
		ObjParuse toReturn = new ObjParuse();
		toReturn.setId(aSbiObjParuse.getId());
		toReturn.setParId(aSbiObjParuse.getSbiObjPar().getObjParId());
		toReturn.setUseModeId(aSbiObjParuse.getSbiParuse().getUseId());
		toReturn.setProg(aSbiObjParuse.getProg());
		toReturn.setParFatherId(aSbiObjParuse.getSbiObjParFather().getObjParId());
		toReturn.setFilterColumn(aSbiObjParuse.getFilterColumn());
		toReturn.setFilterOperation(aSbiObjParuse.getFilterOperation());
		toReturn.setPreCondition(aSbiObjParuse.getPreCondition());
		toReturn.setPostCondition(aSbiObjParuse.getPostCondition());
		toReturn.setLogicOperator(aSbiObjParuse.getLogicOperator());
		toReturn.setParFatherUrlName(aSbiObjParuse.getSbiObjParFather().getParurlNm());
		return toReturn;
	}

	/**
	 * Gets the dependencies.
	 *
	 * @param objParFatherId the obj par father id
	 *
	 * @return the dependencies
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParuseDAO#getDependencies(Integer)
	 */
	@Override
	public List<String> getDependencies(Integer objParFatherId) throws EMFUserError {
		List<String> toReturn = new ArrayList<>();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			// get all the sbiobjparuse objects which have the parameter as the father
			String hql = "from SbiObjParuse s where s.sbiObjParFather=? ";
			Query query = aSession.createQuery(hql);
			query.setInteger(0, objParFatherId.intValue());
			List<SbiObjParuse> objParuses = query.list();
			if (objParuses == null || objParuses.isEmpty()) {
				return toReturn;
			}
			// add to the list all the distinct labels of parameter which depend form the father parameter
			Iterator<SbiObjParuse> it = objParuses.iterator();
			while (it.hasNext()) {
				SbiObjParuse objParuseHib = it.next();
				Integer objParId = objParuseHib.getSbiObjPar().getObjParId();
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
	 * Gets the all dependencies for parameter use.
	 *
	 * @param useId the use id
	 *
	 * @return the all dependencies for parameter use
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParuseDAO#getAllDependenciesForParameterUse(java.lang.Integer)
	 */
	@Override
	public List<ObjParuse> getAllDependenciesForParameterUse(Integer useId) throws EMFUserError {
		List<ObjParuse> toReturn = new ArrayList<>();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			String hql = "from SbiObjParuse s where s.sbiParuse.useId = ?";
			Query query = aSession.createQuery(hql);
			query.setInteger(0, useId.intValue());
			List<SbiObjParuse> result = query.list();
			Iterator<SbiObjParuse> it = result.iterator();
			while (it.hasNext()) {
				toReturn.add(toObjParuse(it.next()));
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
	 * @param useId the use id
	 *
	 * @return the document labels list with associated dependencies
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParuseDAO#getDocumentLabelsListWithAssociatedDependencies(java.lang.Integer)
	 */
	@Override
	public List getDocumentLabelsListWithAssociatedDependencies(Integer useId) throws EMFUserError {
		List toReturn = new ArrayList();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			String hql = "select distinct(obj.label) from SbiObjects obj, SbiObjPar p, SbiObjParuse s where obj.biobjId = p.sbiObject.biobjId and p.objParId = s.sbiObjPar.objParId and s.sbiParuse.useId = ?";
			Query query = aSession.createQuery(hql);
			query.setInteger(0, useId.intValue());
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
	 * Load obj paruse.
	 *
	 * @param objParId the obj par id
	 * @param paruseId the paruse id
	 *
	 * @return the list
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParuseDAO#loadObjParuse(java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public List<ObjParuse> loadObjParuse(Integer objParId, Integer paruseId) throws EMFUserError {
		List<ObjParuse> objparuses = new ArrayList<>();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			String hql = "from SbiObjParuse s where s.sbiObjPar.objParId=? " + " and s.sbiParuse.useId=? "
					+ " order by s.prog";

			Query query = aSession.createQuery(hql);
			query.setInteger(0, objParId.intValue());
			query.setInteger(1, paruseId.intValue());

			List<SbiObjParuse> sbiObjParuses = query.list();
			if (sbiObjParuses == null) {
				return objparuses;
			}
			Iterator<SbiObjParuse> itersbiOP = sbiObjParuses.iterator();
			while (itersbiOP.hasNext()) {
				SbiObjParuse sbiop = itersbiOP.next();
				ObjParuse op = toObjParuse(sbiop);
				objparuses.add(op);
			}
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			rollbackIfActive(tx);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		return objparuses;
	}

	@Override
	public void eraseObjParuseIfExists(ObjParuse aObjParuse, Session aSession) throws HibernateException {
		// get the existing object
		String hql = "from SbiObjParuse s where s.id = ? ";
		Query hqlQuery = aSession.createQuery(hql);
		hqlQuery.setInteger(0, aObjParuse.getId().intValue());

		SbiObjParuse sbiObjParuse = (SbiObjParuse) hqlQuery.uniqueResult();
		if (sbiObjParuse != null) {
			aSession.delete(sbiObjParuse);
		}
	}

	@Override
	public List<ObjParuse> loadObjParusesFather(Integer objParId) throws HibernateException {
		List<ObjParuse> toReturn = new ArrayList<>();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			String hql = "from SbiObjParuse s where s.sbiObjParFather.objParId = ? order by s.prog";
			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, objParId.intValue());
			List<SbiObjParuse> sbiObjParuses = hqlQuery.list();
			Iterator<SbiObjParuse> it = sbiObjParuses.iterator();
			while (it.hasNext()) {
				toReturn.add(toObjParuse(it.next()));
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

}
