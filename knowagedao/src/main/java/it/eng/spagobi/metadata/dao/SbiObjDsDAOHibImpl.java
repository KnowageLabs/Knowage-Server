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
package it.eng.spagobi.metadata.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.engines.drivers.IEngineDriver;
import it.eng.spagobi.metadata.metadata.SbiMetaObjDs;
import it.eng.spagobi.metadata.metadata.SbiMetaObjDsId;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Antonella Giachino (antonella.giachino@eng.it)
 */
public class SbiObjDsDAOHibImpl extends AbstractHibernateDAO implements ISbiObjDsDAO {

	private final String[] enginesNoChecked = { "birt", "cockpit", "console", "composit", "kpi", "svgviewer" };

	static private Logger logger = Logger.getLogger(SbiObjDsDAOHibImpl.class);

	@Override
	public List<SbiMetaObjDs> loadObjByDsId(Integer dsId) throws EMFUserError {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		List<SbiMetaObjDs> toReturn = new ArrayList();
		Query hqlQuery = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			hqlQuery = aSession.createQuery(" from SbiMetaObjDs as db where db.id.dsId = ? ");
			hqlQuery.setInteger(0, dsId);
			toReturn = hqlQuery.list();

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
		logger.debug("OUT");
		return toReturn;
	}

	@Override
	public List<SbiMetaObjDs> loadDsByObjId(Integer objId) throws EMFUserError {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		List<SbiMetaObjDs> toReturn = new ArrayList();
		Query hqlQuery = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			hqlQuery = aSession.createQuery(" from SbiMetaObjDs as db where db.id.objId = ? ");
			hqlQuery.setInteger(0, objId);
			toReturn = hqlQuery.list();

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
		logger.debug("OUT");
		return toReturn;
	}

	@Override
	public SbiMetaObjDs loadDsObjByKey(SbiMetaObjDsId objDsId) throws EMFUserError {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		SbiMetaObjDs toReturn = null;
		Query hqlQuery = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			hqlQuery = aSession
					.createQuery(" from SbiMetaObjDs as db where db.id.objId = ? and db.id.dsId = ? and db.id.versionNum = ? and db.id.organization = ?");
			hqlQuery.setInteger(0, objDsId.getObjId());
			hqlQuery.setInteger(1, objDsId.getDsId());
			hqlQuery.setInteger(2, objDsId.getVersionNum());
			hqlQuery.setString(3, objDsId.getOrganization());
			toReturn = (SbiMetaObjDs) hqlQuery.uniqueResult();

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
		logger.debug("OUT");
		return toReturn;
	}

	@Override
	public void modifyObjDs(SbiMetaObjDs aMetaObjDs) throws EMFUserError {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiMetaObjDsId hibId = new SbiMetaObjDsId();
			hibId.setObjId(aMetaObjDs.getId().getObjId());
			hibId.setDsId(aMetaObjDs.getId().getDsId());

			updateSbiCommonInfo4Update(hibId);
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
		logger.debug("OUT");
	}

	@Override
	public void insertObjDs(SbiMetaObjDs aMetaObjDs) throws EMFUserError {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			updateSbiCommonInfo4Insert(aMetaObjDs);
			aSession.save(aMetaObjDs);
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
		logger.debug("OUT");
	}

	/**
	 * Store the relation between the BI document and its dataset into the SBI_META_OBJ_DS (Only objects with UNIQUE relation 1 to 1 with the dataset: NO
	 * COCKPIT, CONSOLE, DOCUMENT COMPOSITION, ... )
	 *
	 * @param biObj the document object
	 */
	@Override
	public void insertUniqueRelationFromObj(BIObject biObj) throws EMFUserError {
		logger.debug("IN");

		try {
			Engine engineObj = biObj.getEngine();
			if (useUniqueDataset(engineObj.getLabel())) {

				Integer objId = biObj.getId();
				logger.debug("Document ID used for insert relation is: " + objId);

				Integer dsId = biObj.getDataSetId();
				logger.debug("Dataset ID used for insert relation is: " + dsId);

				if (dsId == null) {
					// if the dataset isn't chosen don't insert the relation...
					logger.debug("Dataset is not setted for the document with id [" + objId + "]. Relation with document impossible to save.");
					// ... and delete relations if they are present (for old save action)
					logger.debug("Removing old relations with the object...");
					List<SbiMetaObjDs> lstRels = DAOFactory.getSbiObjDsDAO().loadDsByObjId(objId);
					for (SbiMetaObjDs r : lstRels) {
						SbiMetaObjDs delObjDs = new SbiMetaObjDs();
						SbiMetaObjDsId delObjDsId = new SbiMetaObjDsId();
						delObjDsId.setDsId(r.getId().getDsId());
						delObjDsId.setOrganization(r.getId().getOrganization());
						delObjDsId.setVersionNum(r.getId().getVersionNum());
						delObjDsId.setObjId(objId);
						delObjDs.setId(delObjDsId);
						DAOFactory.getSbiObjDsDAO().deleteObjDs(delObjDs);
					}
					logger.debug("Old relations are removed correctly!");
					return;
				}

				VersionedDataSet ds = ((VersionedDataSet) DAOFactory.getDataSetDAO().loadDataSetById(dsId));
				String dsOrganization = ds.getOrganization();
				logger.debug("Dataset organization used for insert relation is: " + dsOrganization);
				Integer dsVersion = ds.getVersionNum();
				logger.debug("Dataset version used for insert relation is: " + dsVersion);

				// creating relation object
				SbiMetaObjDs relObjDs = new SbiMetaObjDs();
				SbiMetaObjDsId relObjDsId = new SbiMetaObjDsId();
				relObjDsId.setDsId(dsId);
				relObjDsId.setOrganization(dsOrganization);
				relObjDsId.setVersionNum(dsVersion);
				relObjDsId.setObjId(objId);
				relObjDs.setId(relObjDsId);

				// check if the relation already exists (in this context will be only ONE relation)
				List<SbiMetaObjDs> lstDs = loadDsByObjId(objId);
				if (lstDs.size() == 1) {
					// if the relation already exists delete the old and insert the new one
					SbiMetaObjDs oldDs = lstDs.get(0);
					deleteObjDs(oldDs);
					insertObjDs(relObjDs);
				} else {
					insertObjDs(relObjDs);
				}
			}
		} catch (Exception e) {
			logger.error("An error occured while storing relation between dataset and docuemnt. Error: " + e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		}
		logger.debug("OUT");
	}

	/**
	 * Store the relation between the BI document and its dataset into the SBI_META_OBJ_DS
	 *
	 * @param biObj the document object
	 */
	@Override
	public void insertRelationFromCockpit(BIObject obj) throws EMFUserError {
		logger.debug("IN");
		try {
			// 0. get template with configuration
			String driverName = obj.getEngine().getDriverName();
			if (driverName != null && !"".equals(driverName)) {
				IEngineDriver driver = (IEngineDriver) Class.forName(driverName).newInstance();
				ObjTemplate template = obj.getActiveTemplate();
				ArrayList<String> datasetsAssociated = null;
				if (template != null)
					datasetsAssociated = driver.getDatasetAssociated(template.getContent());
				if (datasetsAssociated != null) {
					HashMap<Integer, Boolean> lstDsInsertedForObj = new HashMap<Integer, Boolean>();
					for (Iterator<String> iterator = datasetsAssociated.iterator(); iterator.hasNext();) {
						String dsLabel = iterator.next();
						logger.debug("Insert relation for dataset with label [" + dsLabel + "]");
						VersionedDataSet ds = ((VersionedDataSet) DAOFactory.getDataSetDAO().loadDataSetByLabel(dsLabel));
						// insert only relations with new ds
						if (lstDsInsertedForObj.get(ds.getId()) != null) {
							continue;
						}
						String dsOrganization = ds.getOrganization();
						logger.debug("Dataset organization used for insert relation is: " + dsOrganization);
						Integer dsVersion = ds.getVersionNum();
						logger.debug("Dataset version used for insert relation is: " + dsVersion);

						// creating relation object
						SbiMetaObjDs relObjDs = new SbiMetaObjDs();
						SbiMetaObjDsId relObjDsId = new SbiMetaObjDsId();
						relObjDsId.setDsId(ds.getId());
						relObjDsId.setOrganization(dsOrganization);
						relObjDsId.setVersionNum(dsVersion);
						relObjDsId.setObjId(obj.getId());
						relObjDs.setId(relObjDsId);

						DAOFactory.getSbiObjDsDAO().insertObjDs(relObjDs);
						lstDsInsertedForObj.put(ds.getId(), true);
					}
				}
			} else {
				logger.debug("The document doesn't use any dataset! ");
			}
		} catch (Exception e) {
			logger.error("An error occured while inserting relation between cockpit document and its datasets. Error:  " + e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		}

		logger.debug("OUT");
	}

	/**
	 * Store the relation between the BI document and its dataset into the SBI_META_OBJ_DS
	 *
	 * @param biObj the document object
	 */
	private void insertRelationFromSVG(BIObject obj) throws EMFUserError {
		logger.debug("IN");

		try {
			// 0. get template with configuration
			String template = null;
			try {
				template = new String(obj.getActiveTemplate().getContent());
			} catch (Exception ex) {
				logger.error(
						"An error occured while recovering the template, so the relations between document and dataset cannot be inserted. Please check the template uploaded!");
				return;
			}
			SourceBean templateSB = SourceBean.fromXMLString(template);

			HashMap<Integer, Boolean> lstDsInsertedForObj = new HashMap<Integer, Boolean>();
			// 1. search used datasets
			SourceBean dmSB = (SourceBean) templateSB.getAttribute("DATAMART_PROVIDER");
			SourceBean hierarchySB = (SourceBean) dmSB.getAttribute("HIERARCHY");

			// 2. delete all relations between document and datasets if exist
			DAOFactory.getSbiObjDsDAO().deleteObjDsbyObjId(obj.getId());

			List members = hierarchySB.getAttributeAsList("MEMBER");
			// 3. insert the new relations between document and datasets
			int numInserted = 0;
			for (int i = 0; i < members.size(); i++) {
				SourceBean memberSB = null;
				try {
					logger.debug("Parsing member  [" + i + "]");
					memberSB = (SourceBean) members.get(i);

					String dsLabel = (String) memberSB.getAttribute("measure_dataset");
					logger.debug("Insert relation for dataset with label [" + dsLabel + "]");

					VersionedDataSet ds = ((VersionedDataSet) DAOFactory.getDataSetDAO().loadDataSetByLabel(dsLabel));

					if (ds == null) {
						String message = "No dataset found with label [" + dsLabel + "]";
						logger.error(message);
						throw new SpagoBIRuntimeException(message);
					}
					// insert only relations with new ds
					if (lstDsInsertedForObj.get(ds.getId()) != null) {
						continue;
					}
					String dsOrganization = ds.getOrganization();
					logger.debug("Dataset organization used for insert relation is: " + dsOrganization);
					Integer dsVersion = ds.getVersionNum();
					logger.debug("Dataset version used for insert relation is: " + dsVersion);

					// creating meta relation object
					SbiMetaObjDs relMetaObjDs = new SbiMetaObjDs();
					SbiMetaObjDsId relMetaObjDsId = new SbiMetaObjDsId();
					relMetaObjDsId.setDsId(ds.getId());
					relMetaObjDsId.setOrganization(dsOrganization);
					relMetaObjDsId.setVersionNum(dsVersion);
					relMetaObjDsId.setObjId(obj.getId());
					relMetaObjDs.setId(relMetaObjDsId);
					DAOFactory.getSbiObjDsDAO().insertObjDs(relMetaObjDs);

					// creating relation object
					ArrayList<String> arr = new ArrayList<String>();
					arr.add(dsLabel);

					Session currentSession = getSession();
					Transaction tx = currentSession.beginTransaction();
					DAOFactory.getBIObjDataSetDAO().updateObjectNotDetailDatasets(obj, arr, currentSession);
					currentSession.flush();
					tx.commit();
					currentSession.close();

					lstDsInsertedForObj.put(ds.getId(), true);
					numInserted++;

				} catch (Throwable t) {
					throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
				}
			} // for
			if (numInserted == 0) {
				logger.debug("The document doesn't use any dataset! ");
			}
		} catch (Exception e) {
			logger.error(
					"An error occured while inserting relation between SVG document and its datasets. Please check the document template and the existance of the datasets referenced. Error:  "
							+ e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 20001);
		}
		logger.debug("OUT");
	}

	/**
	 * Store the relation between the BI document and its dataset into the SBI_META_OBJ_DS (Only objects with UNIQUE relation 1 to 1 with the dataset: NO
	 * COCKPIT, CONSOLE, DOCUMENT COMPOSITION, ... )
	 *
	 * @param biObj the document object
	 */
	@Override
	public void insertRelationsFromObj(BIObject biObj) throws EMFUserError {
		logger.debug("IN");

		try {
			Engine engineObj = biObj.getEngine();

			if (engineObj.getLabel().toLowerCase().contains("cockpit")) {
				deleteObjDsbyObjId(biObj.getId());
				insertRelationFromCockpit(biObj);
			} else if (engineObj.getLabel().toLowerCase().contains("svgviewer")) {
				insertRelationFromSVG(biObj);
			} else if (useUniqueDataset(engineObj.getLabel())) {
				insertUniqueRelationFromObj(biObj);
			}
		} catch (Exception e) {
			logger.debug("An error occured while storing relation between dataset and document. Error: " + e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 20001);
		}
		logger.debug("OUT");
	}

	@Override
	public void deleteObjDs(SbiMetaObjDs aMetaObjDs) throws EMFUserError {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		try {
			SbiMetaObjDs rel = loadDsObjByKey(aMetaObjDs.getId());

			if (rel == null)
				return;

			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiMetaObjDsId hibId = new SbiMetaObjDsId();
			hibId.setObjId(aMetaObjDs.getId().getObjId());
			hibId.setDsId(aMetaObjDs.getId().getDsId());
			hibId.setOrganization(aMetaObjDs.getId().getOrganization());
			hibId.setVersionNum(aMetaObjDs.getId().getVersionNum());

			SbiMetaObjDs hib = (SbiMetaObjDs) aSession.load(SbiMetaObjDs.class, hibId);

			aSession.delete(hib);
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
		logger.debug("OUT");
	}

	@Override
	public void deleteObjDsbyObjId(Integer objId) throws EMFUserError {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		try {
			List<SbiMetaObjDs> lstRel = loadDsByObjId(objId);

			if (lstRel == null)
				return;

			aSession = getSession();
			tx = aSession.beginTransaction();

			for (SbiMetaObjDs r : lstRel) {
				SbiMetaObjDsId hibId = new SbiMetaObjDsId();
				hibId.setObjId(r.getId().getObjId());
				hibId.setDsId(r.getId().getDsId());
				hibId.setOrganization(r.getId().getOrganization());
				hibId.setVersionNum(r.getId().getVersionNum());

				SbiMetaObjDs hib = (SbiMetaObjDs) aSession.load(SbiMetaObjDs.class, hibId);

				aSession.delete(hib);
			}
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
		logger.debug("OUT");
	}

	/**
	 * Returns true if the engine uses standard dataset management (1:1 with the document)
	 *
	 * @param engineLabel : the engine label
	 * @return true if the engine use standard dataset (1:1), false otherwise
	 */
	private boolean useUniqueDataset(String engineLabel) {
		boolean toReturn = true;
		for (String e : enginesNoChecked) {
			if (engineLabel.toLowerCase().contains(e)) {
				toReturn = false;
				break;
			}
		}
		return toReturn;
	}

}