/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.mapcatalogue.dao;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Restrictions;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.ICriterion;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.mapcatalogue.bo.GeoLayer;
import it.eng.spagobi.mapcatalogue.metadata.SbiGeoLayers;
import it.eng.spagobi.mapcatalogue.metadata.SbiGeoLayersRoles;

public class SbiGeoLayersDAOHibImpl extends AbstractHibernateDAO implements ISbiGeoLayersDAO {

	/**
	 * Load layer by id.
	 *
	 * @param layerID
	 *            the layer id
	 *
	 * @return the geo layer
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.mapcatalogue.dao.geo.bo.dao.ISbiGeoLayersDAO#loadLayerByID(integer)
	 */
	@Override
	public GeoLayer loadLayerByID(Integer layerID) throws EMFUserError {
		GeoLayer toReturn = null;
		Session tmpSession = null;
		Transaction tx = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			SbiGeoLayers hibLayer = (SbiGeoLayers) tmpSession.load(SbiGeoLayers.class, layerID);
			toReturn = hibLayer.toGeoLayer();
			tx.commit();

		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {

			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();

			}
		}

		return toReturn;
	}

	/**
	 * Load layer by name.
	 *
	 * @param name
	 *            the name
	 *
	 * @return the geo layer
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.mapcatalogue.dao.geo.bo.dao.ISbiGeoLayersDAO#loadLayerByName(string)
	 */
	@Override
	public GeoLayer loadLayerByLabel(String label) throws EMFUserError {
		GeoLayer biLayer = null;
		Session tmpSession = null;
		Transaction tx = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			Criterion labelCriterrion = Expression.eq("label", label);
			Criteria criteria = tmpSession.createCriteria(SbiGeoLayers.class);
			criteria.add(labelCriterrion);
			SbiGeoLayers hibLayer = (SbiGeoLayers) criteria.uniqueResult();
			if (hibLayer == null)
				return null;
			biLayer = hibLayer.toGeoLayer();

			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {

			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}

		}
		return biLayer;
	}

	/**
	 * Modify layer.
	 *
	 * @param aLayer
	 *            the a layer
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.geo.bo.dao.IEngineDAO#modifyEngine(it.eng.spagobi.bo.Engine)
	 */
	@Override
	public void modifyLayer(GeoLayer aLayer) throws EMFUserError {

		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			SbiGeoLayers hibLayer = (SbiGeoLayers) tmpSession.load(SbiGeoLayers.class, new Integer(aLayer.getLayerId()));
			hibLayer.setName(aLayer.getName());
			if (aLayer.getDescr() == null) {
				aLayer.setDescr("");
			}
			hibLayer.setDescr(aLayer.getDescr());
			hibLayer.setType(aLayer.getType());
			hibLayer.setLabel(aLayer.getLabel());
			hibLayer.setBaseLayer(aLayer.isBaseLayer());
			hibLayer.setLayerDef(null);
			hibLayer.setLayerLabel(aLayer.getLayerLabel());
			hibLayer.setLayerName(aLayer.getLayerName());
			hibLayer.setLayerIdentify(aLayer.getLayerIdentify());
			hibLayer.setLayerURL(aLayer.getLayerURL());
			hibLayer.setLayerOptions(aLayer.getLayerOptions());
			hibLayer.setLayerParams(aLayer.getLayerParams());
			hibLayer.setLayerOrder(aLayer.getLayerOrder());
			hibLayer.setCategory(aLayer.getCategory());
			hibLayer.setCategory_id(aLayer.getCategory_id());
			String path = "";

			if (aLayer.getPathFile() != null) {

				String separator = "";
				if (!aLayer.getPathFile().endsWith("" + File.separatorChar)) {
					separator += File.separatorChar;
				}

				path = aLayer.getPathFile() + separator + getTenant() + File.separator + "Layer" + File.separator;
				hibLayer.setPathFile(path);
			} else {
				hibLayer.setPathFile(null);
			}
			updateSbiCommonInfo4Update(hibLayer);
			// cancello tutti i roles associati al layer
			// query hql
			String hql = "DELETE from SbiGeoLayersRoles a WHERE a.layer.id =" + aLayer.getLayerId();
			Query q = tmpSession.createQuery(hql);
			q.executeUpdate();
			// aggiorno i ruoli utenti scelti
			for (SbiExtRoles r : aLayer.getRoles()) {

				// riaggiungo ruoli
				SbiGeoLayersRoles hibLayRol = new SbiGeoLayersRoles(aLayer.getLayerId(), r.getExtRoleId().intValue());
				updateSbiCommonInfo4Update(hibLayRol);
				tmpSession.save(hibLayRol);
				// update(hibLayRol);
			}

			tx.commit();

			// save file on server//
			if (aLayer.getPathFile() != null) {
				System.out.println("il path è:" + path);
				new File(path).mkdir();
				OutputStreamWriter out;
				try {
					String name = aLayer.getLabel();
					out = new FileWriter(path + name);
					out.write(new String(aLayer.getLayerDef()));
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {

			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}
		}

	}

	/**
	 * Insert layer.
	 *
	 * @param aLayer
	 *            the a layer
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.geo.bo.dao.IEngineDAO#insertEngine(it.eng.spagobi.bo.Engine)
	 */
	@Override
	public Integer insertLayer(GeoLayer aLayer) throws EMFUserError {
		Session tmpSession = null;
		Transaction tx = null;
		Integer id;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			SbiGeoLayers hibLayer = new SbiGeoLayers();

			hibLayer.setName(aLayer.getName());
			if (aLayer.getDescr() == null) {
				aLayer.setDescr("");
			}
			hibLayer.setDescr(aLayer.getDescr());
			hibLayer.setType(aLayer.getType());
			hibLayer.setLabel(aLayer.getLabel());
			hibLayer.setBaseLayer(aLayer.isBaseLayer());
			hibLayer.setLayerDef(null);
			hibLayer.setLayerLabel(aLayer.getLayerLabel());
			hibLayer.setLayerName(aLayer.getLayerName());
			hibLayer.setLayerIdentify(aLayer.getLayerIdentify());
			hibLayer.setLayerURL(aLayer.getLayerURL());
			hibLayer.setLayerOptions(aLayer.getLayerOptions());
			hibLayer.setLayerParams(aLayer.getLayerParams());
			hibLayer.setLayerOrder(aLayer.getLayerOrder());
			hibLayer.setCategory_id(aLayer.getCategory_id());

			if (aLayer.getCategory_id() == -1) {
				hibLayer.setCategory(null);
			} else {
				hibLayer.setCategory(aLayer.getCategory());
			}
			String path = null;
			if (aLayer.getPathFile() != null) {

				String separator = "";
				if (!aLayer.getPathFile().endsWith("" + File.separatorChar)) {
					separator += File.separatorChar;
				}

				path = aLayer.getPathFile() + separator + getTenant() + File.separator + "Layer" + File.separator;
				aLayer.setPathFile(path + aLayer.getLabel());
				hibLayer.setPathFile(path + aLayer.getLabel());
			} else {
				aLayer.setPathFile(null);
				hibLayer.setPathFile(null);
			}

			updateSbiCommonInfo4Insert(hibLayer);
			id = (Integer) tmpSession.save(hibLayer);

			// setto i ruoli utenti scelti

			for (SbiExtRoles r : aLayer.getRoles()) {
				SbiGeoLayersRoles hibLayRol = new SbiGeoLayersRoles(id, r.getExtRoleId().intValue());

				updateSbiCommonInfo4Insert(hibLayRol);
				tmpSession.save(hibLayRol);
				// insert(hibLayRol);
			}

			// hibLayer.setRoles(aLayer.getRoles());
			tx.commit();

			// save file on server//

			try {
				if (aLayer.getPathFile() != null) {
					new File(path).mkdirs();
					OutputStreamWriter out;
					String name = aLayer.getLabel();
					out = new FileWriter(path + name);
					out.write(new String(aLayer.getLayerDef()));
					out.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {

			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}

		}
		return id;
	}

	/**
	 * Erase layer.
	 *
	 * @param aLayer
	 *            the a layer
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.geo.bo.dao.IEngineDAO#eraseEngine(it.eng.spagobi.bo.Engine)
	 */

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<SbiExtRoles> listRolesFromId(final Object[] arr) {
		return list(new ICriterion() {
			@Override
			public Criteria evaluate(Session session) {
				Criteria c = session.createCriteria(SbiExtRoles.class);
				c.add(Restrictions.in("extRoleId", arr));
				return c;
			}
		});
	}

	@Override
	public void eraseLayer(Integer layerId) throws EMFUserError {

		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			SbiGeoLayers hibLayer = (SbiGeoLayers) tmpSession.load(SbiGeoLayers.class, layerId);
			if (hibLayer.getPathFile() != null) {
				// siamo nel caso in cui c'erano salvati sul server
				File file = new File(hibLayer.getPathFile() + hibLayer.getLabel());
				file.delete();
			}
			tmpSession.delete(hibLayer);

			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {

			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}

		}
	}
	/*
	 *
	 *
	 *
	 *
	 *
	 */

	@Override
	public void eraseRole(Integer roleId, Integer layerId) throws EMFUserError {

		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			SbiGeoLayersRoles hibLayRol = new SbiGeoLayersRoles(layerId, roleId);

			// delete(SbiGeoLayersRoles.class, hibLayRol);
			tmpSession.delete(hibLayRol);
			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {

			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}

		}
	}

	/**
	 * Load all layers.
	 *
	 * @return the list
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.geo.bo.dao.IEngineDAO#loadAllEngines()
	 */
	@Override
	public List<GeoLayer> loadAllLayers() throws EMFUserError {
		Session tmpSession = null;
		Transaction tx = null;
		List<GeoLayer> realResult = new ArrayList<GeoLayer>();
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			Query hibQuery = tmpSession.createQuery(" from SbiGeoLayers");

			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();
			SbiGeoLayers hibLayer;
			while (it.hasNext()) {
				hibLayer = (SbiGeoLayers) it.next();
				if (hibLayer != null) {
					final GeoLayer bilayer = hibLayer.toGeoLayer();
					realResult.add(bilayer);
				}
			}

			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {

			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}

		}
		return realResult;
	}

}
