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
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.Viewpoint;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiMetaModelViewpoints;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.tools.catalogue.metadata.SbiMetaModel;

public class MetaModelViewpointDAOHibImpl extends AbstractHibernateDAO implements IMetaModelViewpointDAO {

	/**
	 * Insert viewpoint.
	 *
	 * @param viewpoint
	 *            the viewpoint
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IMetaModelViewpointDAO#insertViewpoint(it.eng.spagobi.analiticalmodel.document.bo.Viewpoint)
	 */
	@Override
	public void insertMetaModelViewpoint(Viewpoint viewpoint) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiMetaModelViewpoints hibViewpoint = new SbiMetaModelViewpoints();

			// hibViewpoint.setVpId(vpId);
			SbiMetaModel aSbiMetaModel = (SbiMetaModel) aSession.load(SbiMetaModel.class, viewpoint.getBiobjId());
			hibViewpoint.setSbiMetaModel(aSbiMetaModel);
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
	 * Load viewpoint by name and meta model identifier.
	 *
	 * @param name
	 *            the name of the viewpoint
	 * @param name
	 *            The id of the meta model
	 *
	 * @return the viewpoint
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IViewpointDAO#loadViewpointByName(java.lang.String)
	 */
	@Override
	public Viewpoint loadViewpointByNameAndMetaModelId(String name, Integer metaModelId) throws EMFUserError {
		Viewpoint toReturn = null;
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			String hql = "from SbiMetaModelViewpoints vp where vp.sbiMetaModel.id = ? and vp.vpName = ?";
			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, metaModelId.intValue());
			hqlQuery.setString(1, name);

			SbiMetaModelViewpoints hibViewpoint = (SbiMetaModelViewpoints) hqlQuery.uniqueResult();
			if (hibViewpoint == null)
				return null;
			toReturn = toMetaModelViewpoint(hibViewpoint);
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
	 * Load viewpoint by id.
	 *
	 * @param id
	 *            the id
	 *
	 * @return the viewpoint
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IMetaModelViewpointDAO#loadViewpointByID(java.lang.Integer)
	 */
	@Override
	public Viewpoint loadViewpointByID(Integer id) throws EMFUserError {
		Viewpoint toReturn = null;
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiMetaModelViewpoints hibViewpoint = (SbiMetaModelViewpoints) aSession.load(SbiMetaModelViewpoints.class, id);

			toReturn = toMetaModelViewpoint(hibViewpoint);
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
	 * @param id
	 *            the id
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IMetaModelViewpointDAO#eraseViewpoint(it.eng.spagobi.analiticalmodel.document.bo.Viewpoint)
	 */
	@Override
	public void eraseViewpoint(Integer id) throws EMFUserError {

		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiMetaModelViewpoints hibViewpoint = (SbiMetaModelViewpoints) aSession.load(SbiMetaModelViewpoints.class, id);

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

	@Override
	public List loadAccessibleViewpointsByMetaModelId(Integer metaModelId, IEngUserProfile userProfile) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;

		List realResult = new ArrayList();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			// String hql = "from SbiViewpoints vp where vp.sbiObject.biobjId = " + objId + " and (vp.vpScope = 'Public' or " +
			// "vp.vpOwner = '" + ((UserProfile)userProfile).getUserId().toString() + "')";

			String hql = "from SbiMetaModelViewpoints vp where vp.sbiMetaModel.id = ? and (vp.vpScope = 'Public' or vp.vpScope = 'PUBLIC' or  "
					+ "vp.vpOwner = ?)";

			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, metaModelId.intValue());
			hqlQuery.setString(1, (String) ((UserProfile) userProfile).getUserId());

			List hibList = hqlQuery.list();

			tx.commit();

			Iterator it = hibList.iterator();
			while (it.hasNext()) {
				realResult.add(toMetaModelViewpoint((SbiMetaModelViewpoints) it.next()));
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
	 * From the hibernate BI value viewpoint at input, gives the corrispondent <code>Viepoint</code> object.
	 *
	 * @param hibViewpoint
	 *            The hybernate viewpoint at input
	 * @return The corrispondent <code>Viewpoint</code> object
	 */
	public Viewpoint toMetaModelViewpoint(SbiMetaModelViewpoints hibViewpoint) {
		Viewpoint aViewpoint = new Viewpoint();
		aViewpoint.setVpId(hibViewpoint.getVpId());
		aViewpoint.setBiobjId(hibViewpoint.getSbiMetaModel().getId());
		aViewpoint.setVpOwner(hibViewpoint.getVpOwner());
		aViewpoint.setVpName(hibViewpoint.getVpName());
		aViewpoint.setVpDesc(hibViewpoint.getVpDesc());
		aViewpoint.setVpScope(hibViewpoint.getVpScope());
		aViewpoint.setVpValueParams(hibViewpoint.getVpValueParams());
		aViewpoint.setVpCreationDate(hibViewpoint.getVpCreationDate());
		return aViewpoint;
	}

}
