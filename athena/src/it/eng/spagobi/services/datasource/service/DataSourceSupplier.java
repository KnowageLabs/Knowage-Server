/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.services.datasource.service;

import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IDomainDAO;
import it.eng.spagobi.services.datasource.bo.SpagoBiDataSource;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.datasource.bo.IDataSource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

public class DataSourceSupplier {
	static private Logger logger = Logger.getLogger(DataSourceSupplier.class);

	/**
	 * Gets the data source.
	 * 
	 * @param documentId
	 *            the document id
	 * 
	 * @return the data source
	 */
	public SpagoBiDataSource getDataSource(String documentId) {
		logger.debug("IN.documentId:" + documentId);

		SpagoBiDataSource toReturn = null;
		if (documentId == null)
			return null;

		// gets data source data from database
		try {
			BIObject obj = DAOFactory.getBIObjectDAO().loadBIObjectById(
					Integer.valueOf(documentId));
			if (obj == null) {
				logger.error("The object with id " + documentId
						+ " is not found on the database.");
				return null;
			}
			Integer dsId = null;
			if (obj.getDataSourceId() != null) {
				dsId = obj.getDataSourceId();
				logger.debug("Using document datasource id = " + dsId);
			}
			if (dsId == null) {
				logger.debug("Data source is not configured for document. Looking for any dataset ...");
				Integer datasetId = obj.getDataSetId();
				if (datasetId != null) {
					logger.debug("Document has a dataset. Looking for its datasource ...");
					IDataSet dataset = DAOFactory.getDataSetDAO().loadDataSetById(datasetId);
					IDataSource datasource = dataset.getDataSource();
					if (datasource != null) {
						logger.debug("Document's dataset has a datasource. Returning this one, that is " + datasource.getLabel());
						toReturn = datasource.toSpagoBiDataSource();
					} else {
						logger.debug("Document's dataset has no datasource. Returning null");
						return null;
					}
				} else {
					logger.debug("Document has no dataset. Returning null");
					return null;
				}
			} else {
				IDataSource ds = DAOFactory.getDataSourceDAO().loadDataSourceByID(
						dsId);
				if (ds == null) {
					logger.error("The data source with id " + obj.getDataSourceId()
							+ " is not found on the database.");
					return null;
				}

				toReturn = toSpagoBiDataSource(ds);
			}

		} catch (Exception e) {
			logger.error("The data source is not correctly returned", e);
			toReturn = null;

		}
		logger.debug("OUT");
		return toReturn;
	}

	/**
	 * Gets the data source by label.
	 * 
	 * @param dsLabel
	 *            the ds label
	 * 
	 * @return the data source by label
	 */
	public SpagoBiDataSource getDataSourceByLabel(String dsLabel) {
		logger.debug("IN");
		SpagoBiDataSource sbds = new SpagoBiDataSource();

		// gets data source data from database
		try {
			IDataSource ds = DAOFactory.getDataSourceDAO()
					.loadDataSourceByLabel(dsLabel);
			if (ds == null) {
				logger.warn("The data source with label " + dsLabel
						+ " is not found on the database.");
				return null;
			}
			sbds = toSpagoBiDataSource(ds);

		} catch (Exception e) {
			logger.error("The data source is not correctly returned", e);
		}
		logger.debug("OUT");
		return sbds;
	}

	/**
	 * Gets the data source by label.
	 * 
	 * @param dsLabel
	 *            the ds label
	 * 
	 * @return the data source by label
	 */
	public SpagoBiDataSource getDataSourceById(int id) {
		logger.debug("IN");
		SpagoBiDataSource sbds = new SpagoBiDataSource();

		// gets data source data from database
		try {
			IDataSource ds = DAOFactory.getDataSourceDAO().loadDataSourceByID(
					id);
			if (ds == null) {
				logger.warn("The data source with id " + id
						+ " is not found on the database.");
				return null;
			}

			sbds = toSpagoBiDataSource(ds);

		} catch (Exception e) {
			logger.error("The data source is not correctly returned", e);
		}
		logger.debug("OUT");
		return sbds;
	}

	private SpagoBiDataSource toSpagoBiDataSource(IDataSource ds)
			throws Exception {
		SpagoBiDataSource sbds = new SpagoBiDataSource();
		sbds.setLabel(ds.getLabel());
		sbds.setJndiName(ds.getJndi());
		sbds.setUrl(ds.getUrlConnection());
		sbds.setUser(ds.getUser());
		sbds.setPassword(ds.getPwd());
		sbds.setDriver(ds.getDriver());
		sbds.setMultiSchema(ds.getMultiSchema());
		sbds.setSchemaAttribute(ds.getSchemaAttribute());
		// gets dialect informations
		IDomainDAO domaindao = DAOFactory.getDomainDAO();
		Domain doDialect = domaindao.loadDomainById(ds.getDialectId());
		sbds.setHibDialectClass(doDialect.getValueCd());
		sbds.setHibDialectName(doDialect.getValueName());
		sbds.setReadOnly(ds.checkIsReadOnly());
		sbds.setWriteDefault(ds.checkIsWriteDefault());
		return sbds;
	}

	/**
	 * Gets the all data source.
	 * 
	 * @return the all data source
	 */
	public SpagoBiDataSource[] getAllDataSource() {
		logger.debug("IN");
		ArrayList tmpList = new ArrayList();

		// gets all data source from database
		try {
			List lstDs = DAOFactory.getDataSourceDAO().loadAllDataSources();
			if (lstDs == null) {
				logger.warn("Data sources aren't found on the database.");
				return null;
			}

			Iterator dsIt = lstDs.iterator();
			while (dsIt.hasNext()) {
				IDataSource ds = (IDataSource) dsIt.next();
				SpagoBiDataSource sbds = toSpagoBiDataSource(ds);
				tmpList.add(sbds);
			}
		} catch (Exception e) {
			logger.error("The data sources are not correctly returned", e);
		}
		// mapping generic array list into array of SpagoBiDataSource objects
		SpagoBiDataSource[] arDS = new SpagoBiDataSource[tmpList.size()];
		for (int i = 0; i < tmpList.size(); i++) {
			arDS[i] = (SpagoBiDataSource) tmpList.get(i);
		}
		logger.debug("OUT");
		return arDS;
	}
}
