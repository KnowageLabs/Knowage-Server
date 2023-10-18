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
package it.eng.spagobi.analiticalmodel.document.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.Viewpoint;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiViewpoints;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;

/**
 * Defines the Hibernate implementations for all DAO methods, for a viewpoint.
 *
 * @author Giachino
 */
public class ViewpointDAOHimpl extends AbstractHibernateDAO implements IViewpointDAO {

	/**
	 * Load all viewpoints.
	 *
	 * @return the list
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IViewpointDAO#loadAllViewpoints()
	 */
	@Override
	public List<Viewpoint> loadAllViewpoints() throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;

		List<Viewpoint> realResult = new ArrayList<>();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = aSession.createQuery(" from SbiViewpoints");
			List<SbiViewpoints> hibList = hibQuery.list();

			tx.commit();

			Iterator<SbiViewpoints> it = hibList.iterator();

			while (it.hasNext()) {
				realResult.add(toViewpoint(it.next()));
			}
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
		return realResult;
	}

	/**
	 * Load all viewpoints by obj id.
	 *
	 * @param objId the obj id
	 *
	 * @return the list
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IViewpointDAO#loadAllViewpoints()
	 */
	@Override
	public List<Viewpoint> loadAllViewpointsByObjID(Integer objId) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;

		List<Viewpoint> realResult = new ArrayList<>();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			String hql = "from SbiViewpoints vp where vp.sbiObject.biobjId = ?";

			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, objId.intValue());

			List<SbiViewpoints> hibList = hqlQuery.list();

			tx.commit();

			Iterator<SbiViewpoints> it = hibList.iterator();
			while (it.hasNext()) {
				realResult.add(toViewpoint(it.next()));
			}
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} catch (Exception ex) {
			logException(ex);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		return realResult;
	}

	/**
	 * Load viewpoint by id.
	 *
	 * @param id the id
	 *
	 * @return the viewpoint
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IViewpointDAO#loadViewpointByID(java.lang.Integer)
	 */
	@Override
	public Viewpoint loadViewpointByID(Integer id) throws EMFUserError {
		Viewpoint toReturn = null;
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiViewpoints hibViewpoint = (SbiViewpoints) aSession.load(SbiViewpoints.class, id);

			toReturn = toViewpoint(hibViewpoint);
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
	 * Load viewpoint by name and document identifier.
	 *
	 * @param name the name of the viewpoint
	 * @param name The id of the document
	 *
	 * @return the viewpoint
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IViewpointDAO#loadViewpointByName(java.lang.String)
	 */
	@Override
	public Viewpoint loadViewpointByNameAndBIObjectId(String name, Integer biobjectId) throws EMFUserError {
		Viewpoint toReturn = null;
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			String hql = "from SbiViewpoints vp where vp.sbiObject.biobjId = ? and vp.vpName = ?";
			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, biobjectId.intValue());
			hqlQuery.setString(1, name);

			SbiViewpoints hibViewpoint = (SbiViewpoints) hqlQuery.uniqueResult();
			if (hibViewpoint == null)
				return null;
			toReturn = toViewpoint(hibViewpoint);
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
	 * Erase viewpoint.
	 *
	 * @param id the id
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IViewpointDAO#eraseViewpoint(it.eng.spagobi.analiticalmodel.document.bo.Viewpoint)
	 */
	@Override
	public void eraseViewpoint(Integer id) throws EMFUserError {

		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiViewpoints hibViewpoint = (SbiViewpoints) aSession.load(SbiViewpoints.class, id);

			aSession.delete(hibViewpoint);
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

	}

	/**
	 * Insert viewpoint.
	 *
	 * @param viewpoint the viewpoint
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IViewpointDAO#insertViewpoint(it.eng.spagobi.analiticalmodel.document.bo.Viewpoint)
	 */
	@Override
	public void insertViewpoint(Viewpoint viewpoint) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiViewpoints hibViewpoint = new SbiViewpoints();

			SbiObjects aSbiObject = (SbiObjects) aSession.load(SbiObjects.class, viewpoint.getBiobjId());
			hibViewpoint.setSbiObject(aSbiObject);
			hibViewpoint.setVpDesc(viewpoint.getVpDesc());
			hibViewpoint.setVpOwner(viewpoint.getVpOwner());
			hibViewpoint.setVpName(viewpoint.getVpName());
			hibViewpoint.setVpScope(viewpoint.getVpScope());
			hibViewpoint.setVpValueParams(viewpoint.getVpValueParams());
			hibViewpoint.setVpCreationDate(viewpoint.getVpCreationDate());
			updateSbiCommonInfo4Insert(hibViewpoint);
			aSession.save(hibViewpoint);
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

	}

	/**
	 * From the hibernate BI value viewpoint at input, gives the corrispondent <code>Viepoint</code> object.
	 *
	 * @param hibViewpoint The hybernate viewpoint at input
	 * @return The corrispondent <code>Viewpoint</code> object
	 */
	public Viewpoint toViewpoint(SbiViewpoints hibViewpoint) {
		Viewpoint aViewpoint = new Viewpoint();
		aViewpoint.setVpId(hibViewpoint.getVpId());
		aViewpoint.setBiobjId(hibViewpoint.getSbiObject().getBiobjId());
		aViewpoint.setVpOwner(hibViewpoint.getVpOwner());
		aViewpoint.setVpName(hibViewpoint.getVpName());
		aViewpoint.setVpDesc(hibViewpoint.getVpDesc());
		aViewpoint.setVpScope(hibViewpoint.getVpScope());
		aViewpoint.setVpValueParams(hibViewpoint.getVpValueParams());
		aViewpoint.setVpCreationDate(hibViewpoint.getVpCreationDate());
		return aViewpoint;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IViewpointDAO#loadAccessibleViewpointsByObjId(java.lang.Integer, it.eng.spago.security.IEngUserProfile)
	 */
	@Override
	public List<Viewpoint> loadAccessibleViewpointsByObjId(Integer objId, IEngUserProfile userProfile)
			throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;

		List<Viewpoint> realResult = new ArrayList<>();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			String hql = "from SbiViewpoints vp where vp.sbiObject.biobjId = ? and (vp.vpScope = 'Public' or vp.vpScope = 'PUBLIC' or  "
					+ "vp.vpOwner = ?)";

			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, objId.intValue());
			hqlQuery.setString(1, (String) ((UserProfile) userProfile).getUserId());

			List<SbiViewpoints> hibList = hqlQuery.list();

			tx.commit();

			Iterator<SbiViewpoints> it = hibList.iterator();
			while (it.hasNext()) {
				realResult.add(toViewpoint(it.next()));
			}
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
		return realResult;
	}

}
