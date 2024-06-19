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
package it.eng.spagobi.services.dataset.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;
import it.eng.spagobi.tools.dataset.bo.DataSetFactory;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.dataset.service.ManageDatasets;

public class DataSetSupplier {

	private static final Logger LOGGER = Logger.getLogger(DataSetSupplier.class);

	/**
	 * Gets the data set.
	 *
	 * @param documentId the document id
	 *
	 * @return the data set
	 */
	public SpagoBiDataSet getDataSet(String documentId, UserProfile profile) {

		SpagoBiDataSet datasetConfig = null;
		BIObject obj;
		IDataSet dataSet;

		LOGGER.debug("IN");

		LOGGER.debug("Requested the datasource associated to document [" + documentId + "]");

		if (documentId == null) {
			return null;
		}

		// gets data source data from database
		try {
			obj = DAOFactory.getBIObjectDAO().loadBIObjectById(Integer.valueOf(documentId));
			if (obj == null) {
				LOGGER.warn("The object with id " + documentId + " deoes not exist on database.");
				return null;
			}

			if (obj.getDataSetId() == null) {
				LOGGER.warn("Dataset is not configured for this document:" + documentId);
				return null;
			}

			IDataSetDAO dao = DAOFactory.getDataSetDAO();
			dao.setUserProfile(profile);
			dataSet = dao.loadDataSetById(obj.getDataSetId());
			if (dataSet == null) {
				LOGGER.warn("The dataSet with id " + obj.getDataSetId() + " deoes not exist on database.");
				return null;
			}

			datasetConfig = dataSet.toSpagoBiDataSet();

		} catch (Exception e) {
			LOGGER.error("The dataset is not correctly returned", e);
		} finally {
			LOGGER.debug("OUT");
		}

		return datasetConfig;
	}

	/**
	 * Gets the data set by label.
	 *
	 * @param label the ds label
	 *
	 * @return the data set by label
	 */
	public SpagoBiDataSet getDataSetByLabel(String label, IEngUserProfile userProfile) {
		SpagoBiDataSet datasetConfig = null;
		IDataSet ds = null;

		LOGGER.debug("IN");

		try {
			IDataSetDAO dsDao = DAOFactory.getDataSetDAO();
			dsDao.setUserProfile(userProfile);
			ds = dsDao.loadDataSetByLabel(label);
			if (ds != null) {
				datasetConfig = ds.toSpagoBiDataSet();
			} else {
				LOGGER.debug("Dataset with label [" + label + "] not found");
			}
		} finally {
			LOGGER.debug("OUT");
		}

		return datasetConfig;
	}

	/**
	 * Gets the data set by label.
	 *
	 * @param label the ds label
	 *
	 * @return the data set by label
	 */
	public SpagoBiDataSet loadDataSetByLabelAndUserCategories(String label, IEngUserProfile userProfile) {
		SpagoBiDataSet datasetConfig = null;
		IDataSet ds = null;

		LOGGER.debug("IN");

		try {
			IDataSetDAO dsDao = DAOFactory.getDataSetDAO();
			dsDao.setUserProfile(userProfile);
			ds = dsDao.loadDataSetByLabelAndUserCategories(label);
			if (ds != null) {
				datasetConfig = ds.toSpagoBiDataSet();
			} else {
				LOGGER.debug("Dataset with label [" + label + "] not found");
			}
		} finally {
			LOGGER.debug("OUT");
		}

		return datasetConfig;
	}

	/**
	 * Gets the all data source.
	 *
	 * @return the all data source
	 */
	public SpagoBiDataSet[] getAllDataSet() {
		SpagoBiDataSet[] dataSetsConfig = null;

		List datasets;
		ArrayList tmpList;

		LOGGER.debug("IN");

		// gets all data source from database
		try {
			datasets = DAOFactory.getDataSetDAO().loadDataSets();
			if (datasets == null) {
				LOGGER.warn("There are no datasets defined on the database.");
				return null;
			}

			Iterator it = datasets.iterator();
			tmpList = new ArrayList();
			while (it.hasNext()) {
				IDataSet dataset = (IDataSet) it.next();
				SpagoBiDataSet sbds = dataset.toSpagoBiDataSet();
				tmpList.add(sbds);
			}

			dataSetsConfig = (SpagoBiDataSet[]) tmpList.toArray(new SpagoBiDataSet[tmpList.size()]);

		} catch (Exception e) {
			LOGGER.error("The data sources are not correctly returned", e);
		} finally {
			LOGGER.debug("OUT");
		}

		return dataSetsConfig;
	}

	public SpagoBiDataSet saveDataSet(SpagoBiDataSet datasetConfig, IEngUserProfile profile, HttpSession session) {
		SpagoBiDataSet toReturn = null;

		LOGGER.debug("IN");
		try {
			String userId = ((UserProfile) profile).getUserId().toString();
			IDataSet dataSet = DataSetFactory.getDataSet(datasetConfig, userId, session);
			Integer id = DAOFactory.getDataSetDAO().insertDataSet(dataSet);
			dataSet.setId(id);
			ManageDatasets md = new ManageDatasets();
			md.setProfile(profile);

			md.insertPersistenceAndScheduling(dataSet, new HashMap<>());

			toReturn = dataSet.toSpagoBiDataSet();

		} catch (Exception e) {
			LOGGER.error("Error while saving dataset", e);
		} finally {
			LOGGER.debug("OUT");
		}

		return toReturn;
	}

}
