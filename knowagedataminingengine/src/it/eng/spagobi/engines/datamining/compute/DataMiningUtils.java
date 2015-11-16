/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.datamining.compute;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.engines.datamining.DataMiningEngineConfig;
import it.eng.spagobi.engines.datamining.DataMiningEngineInstance;
import it.eng.spagobi.engines.datamining.common.utils.DataMiningConstants;
import it.eng.spagobi.engines.datamining.model.DataMiningDataset;
import it.eng.spagobi.engines.datamining.model.Variable;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.DataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVWriter;

public class DataMiningUtils {
	static private Logger logger = Logger.getLogger(DataMiningUtils.class);

	public static final String UPLOADED_FILE_PATH = DataMiningEngineConfig.getInstance().getEngineConfig().getResourcePath().replaceAll("\\\\", "/")
			+ DataMiningConstants.DATA_MINING_PATH_SUFFIX;

	public static Boolean areDatasetsProvided(DataMiningEngineInstance dataminingInstance, IEngUserProfile profile) throws IOException {
		logger.debug("IN");
		Boolean areProvided = true;

		if (dataminingInstance.getDatasets() != null && !dataminingInstance.getDatasets().isEmpty()) {
			for (Iterator dsIt = dataminingInstance.getDatasets().iterator(); dsIt.hasNext();) {
				DataMiningDataset ds = (DataMiningDataset) dsIt.next();
				File fileDSDir = new File(getUserResourcesPath(profile) + ds.getName());
				if (fileDSDir != null) {
					File[] dsfiles = fileDSDir.listFiles();
					if (dsfiles == null || dsfiles.length == 0) {
						areProvided = false;

					}
				} else {
					areProvided = false;
				}
			}

		}
		logger.debug("OUT");
		return areProvided;
	}

	public static String getFileFromSpagoBIDataset(HashMap params, DataMiningDataset ds, IEngUserProfile profile) throws IOException {
		logger.debug("IN");
		String filePath = "";
		IDataSetDAO dataSetDao;
		CSVWriter writer = null;
		try {
			dataSetDao = DAOFactory.getDataSetDAO();
			dataSetDao.setUserProfile(profile);
			IDataSet spagobiDataset = dataSetDao.loadDataSetByLabel(ds.getSpagobiLabel());
			logger.debug("Got spagobi dataset");
			spagobiDataset.setParamsMap(params);
			spagobiDataset.loadData();
			DataStore dataStore = (DataStore) spagobiDataset.getDataStore();
			filePath = getUserResourcesPath(profile) + DataMiningConstants.DATA_MINING_TEMP_PATH_SUFFIX + ds.getName();
			logger.debug("Got user resource path");

			File csvdir = new File(filePath);
			if (!csvdir.exists()) {
				csvdir.mkdir();
			}

			filePath += "/" + ds.getSpagobiLabel() + DataMiningConstants.CSV_FILE_FORMAT;
			File csvFile = new File(filePath);
			csvFile.createNewFile();
			logger.debug("Created Csv file");
			writer = new CSVWriter(new FileWriter(filePath), ',');
			writeColumns(dataStore, writer);
			writeFields(dataStore, writer);
			writer.flush();

			filePath = filePath.replaceAll("\\\\", "/");
			return filePath;

		} catch (Exception e) {
			logger.error(e);
			if (writer != null) {
				writer.close();
			}
		} finally {
			if (writer != null) {
				writer.close();
			}
			logger.debug("OUT");
		}
		return filePath;

	}

	public static void writeColumns(DataStore dataStore, CSVWriter writer) {
		logger.debug("IN");
		String col = "";

		for (int j = 0; j < dataStore.getMetaData().getFieldCount(); j++) {
			IFieldMetaData fieldMetaData = dataStore.getMetaData().getFieldMeta(j);
			String fieldHeader = fieldMetaData.getAlias() != null ? fieldMetaData.getAlias() : fieldMetaData.getName();
			col += fieldHeader + DataMiningConstants.CSV_SEPARATOR;
		}
		writer.writeNext(col.split(DataMiningConstants.CSV_SEPARATOR));
		logger.debug("OUT");
	}

	public static void writeFields(DataStore dataStore, CSVWriter writer) {
		logger.debug("IN");
		Iterator records = dataStore.iterator();
		while (records.hasNext()) {
			IRecord record = (IRecord) records.next();
			String row = "";
			for (int i = 0; i < dataStore.getMetaData().getFieldCount(); i++) {
				IField field = record.getFieldAt(i);

				if (field.getValue() != null) {
					row += field.getValue().toString() + DataMiningConstants.CSV_SEPARATOR;
				} else {
					row += "" + DataMiningConstants.CSV_SEPARATOR;
				}
			}

			writer.writeNext(row.split(DataMiningConstants.CSV_SEPARATOR));
		}
		logger.debug("OUT");
	}

	public static String getUserResourcesPath(IEngUserProfile profile) throws IOException {
		logger.debug("IN");
		String userResourcePath = UPLOADED_FILE_PATH + profile.getUserUniqueIdentifier() + "/";
		logger.debug(userResourcePath);
		File userPathFile = new File(userResourcePath);
		logger.debug("Got userPathFile");
		logger.debug("OUT");
		return userResourcePath;
	}

	public static void createUserResourcesPath(IEngUserProfile profile) throws IOException {
		logger.debug("IN");
		String userResourcePath = UPLOADED_FILE_PATH + profile.getUserUniqueIdentifier() + "/";
		logger.debug(userResourcePath);
		File userPathFile = new File(userResourcePath);
		// if it doesn't exist create it
		if (!userPathFile.exists()) {
			userPathFile.mkdir();
			File temp = new File(userPathFile, DataMiningConstants.DATA_MINING_TEMP_PATH_SUFFIX);
			temp.mkdir();
		}
		logger.debug("OUT");
	}

	protected static String replaceVariables(List<Variable> variables, String code) throws Exception {
		logger.debug("IN");
		HashMap parameters = new HashMap<String, Object>();
		if (variables != null && !variables.isEmpty()) {
			for (Iterator it = variables.iterator(); it.hasNext();) {
				Variable var = (Variable) it.next();
				Object val = var.getValue();
				if (val == null) {
					val = var.getDefaultVal();
				}
				parameters.put(var.getName(), val);
			}
			if (code != null && !code.equals("")) {
				code = StringUtilities.substituteParametersInString(code, parameters, null, false);
			}
		}
		logger.debug("OUT");
		return code;
	}
}
