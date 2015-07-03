/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.services.dataset.service;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;
import it.eng.spagobi.tools.dataset.bo.DataSetFactory;
import it.eng.spagobi.tools.dataset.bo.IDataSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

public class DataSetSupplier {

	static private Logger logger = Logger.getLogger(DataSetSupplier.class);

	/**
	 * Gets the data set.
	 *
	 * @param documentId
	 *            the document id
	 *
	 * @return the data set
	 */
	public SpagoBiDataSet getDataSet(String documentId) {

		SpagoBiDataSet datasetConfig = null;
		BIObject obj;
		IDataSet dataSet;

		logger.debug("IN");

		logger.debug("Requested the datasource associated to document [" + documentId + "]");

		if (documentId == null) {
			return null;
		}

		// gets data source data from database
		try {
			obj = DAOFactory.getBIObjectDAO().loadBIObjectById(Integer.valueOf(documentId));
			if (obj == null) {
				logger.warn("The object with id " + documentId + " deoes not exist on database.");
				return null;
			}

			if (obj.getDataSetId() == null) {
				logger.warn("Dataset is not configured for this document:" + documentId);
				return null;
			}

			dataSet = DAOFactory.getDataSetDAO().loadDataSetById(obj.getDataSetId());
			if (dataSet == null) {
				logger.warn("The dataSet with id " + obj.getDataSetId() + " deoes not exist on database.");
				return null;
			}

			datasetConfig = dataSet.toSpagoBiDataSet();

		} catch (Exception e) {
			logger.error("The dataset is not correctly returned", e);
		} finally {
			logger.debug("OUT");
		}

		return datasetConfig;
	}

	/**
	 * Gets the data set by label.
	 *
	 * @param label
	 *            the ds label
	 *
	 * @return the data set by label
	 */
	public SpagoBiDataSet getDataSetByLabel(String label) {
		SpagoBiDataSet datasetConfig = null;
		IDataSet ds = null;

		logger.debug("IN");

		try {
			ds = DAOFactory.getDataSetDAO().loadDataSetByLabel(label);
			if (ds != null) {
				datasetConfig = ds.toSpagoBiDataSet();
			} else {
				logger.debug("Dataset with label [" + label + "] not found");
			}
		} catch (EMFUserError e) {
			logger.error("Error getting dataset with label [" + label + "]", e);
		} finally {
			logger.debug("OUT");
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
		;
		List datasets;
		ArrayList tmpList;

		logger.debug("IN");

		// gets all data source from database
		try {
			datasets = DAOFactory.getDataSetDAO().loadDataSets();
			if (datasets == null) {
				logger.warn("There are no datasets defined on the database.");
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
			logger.error("The data sources are not correctly returned", e);
		} finally {
			logger.debug("OUT");
		}

		return dataSetsConfig;
	}

	public SpagoBiDataSet saveDataSet(SpagoBiDataSet datasetConfig, String userId, HttpSession session) {
		SpagoBiDataSet toReturn = null;

		logger.debug("IN");
		try {
			IDataSet dataSet = DataSetFactory.getDataSet(datasetConfig, userId, session);
			Integer id = DAOFactory.getDataSetDAO().insertDataSet(dataSet);
			dataSet.setId(id);
			toReturn = dataSet.toSpagoBiDataSet();
		} catch (Exception e) {
			logger.error("Error while saving dataset", e);
		} finally {
			logger.debug("OUT");
		}

		return toReturn;
	}

}
