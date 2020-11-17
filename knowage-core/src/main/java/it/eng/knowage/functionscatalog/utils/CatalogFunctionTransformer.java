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

package it.eng.knowage.functionscatalog.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.functions.dao.ICatalogFunctionDAO;
import it.eng.spagobi.functions.metadata.SbiCatalogFunction;
import it.eng.spagobi.tools.dataset.common.datastore.Field;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.common.transformer.AbstractDataStoreTransformer;

/**
 * @author Marco Balestri (marco.balestri@eng.it)
 *
 */
public class CatalogFunctionTransformer extends AbstractDataStoreTransformer {

	private final int functionId;
	private SbiCatalogFunction function;
	private final JSONObject functionConfiguration;
	private Map<String, String> inputColumns;
	private Map<String, InputVariable> inputVariables;
	private Map<String, OutputColumn> outputColumns;
	private UserProfile profile;

	private static transient Logger logger = Logger.getLogger(CatalogFunctionTransformer.class);

	public CatalogFunctionTransformer(int functionId, JSONObject catalogFunctionConfig) {
		this.functionId = functionId;
		this.functionConfiguration = catalogFunctionConfig;
		init();
	}

	private void init() {
		profile = new UserProfileResource().getUserProfileForFunctionsCatalog();
		initInputColumns();
		initOutputColumns();
		initInputVariables();
		initFunction();
		initProxy();
	}

	private void initFunction() {
		ICatalogFunctionDAO fcDAO = DAOFactory.getCatalogFunctionDAO();
		fcDAO.setUserProfile(profile);
		function = fcDAO.getCatalogFunctionById(functionId);
	}

	private void initProxy() {

	}

	private void initInputColumns() {
		inputColumns = new HashMap<String, String>();
		JSONArray inCols = functionConfiguration.optJSONArray("inputColumns");
		for (int i = 0; i < inCols.length(); i++) {
			try {
				String columnName = inCols.getJSONObject(i).getString("name");
				String dsColumn = inCols.getJSONObject(i).getString("dsColumn");
				inputColumns.put(columnName, dsColumn);
			} catch (Exception e) {
				logger.error("Error initializing input columns", e);
				inputColumns = new HashMap<String, String>();
			}
		}
	}

	private void initInputVariables() {
		inputVariables = new HashMap<String, InputVariable>();
		JSONArray inVars = functionConfiguration.optJSONArray("inputVariables");
		for (int i = 0; i < inVars.length(); i++) {
			try {
				String varName = inVars.getJSONObject(i).getString("name");
				String type = inVars.getJSONObject(i).getString("type");
				String value = inVars.getJSONObject(i).getString("value");
				InputVariable inputVariable = new InputVariable(varName, type, value);
				inputVariables.put(varName, inputVariable);
			} catch (Exception e) {
				logger.error("Error initializing input variables", e);
				inputVariables = new HashMap<String, InputVariable>();
			}
		}
	}

	private void initOutputColumns() {
		outputColumns = new HashMap<String, OutputColumn>();
		JSONArray outCols = functionConfiguration.optJSONArray("outputColumns");
		for (int i = 0; i < outCols.length(); i++) {
			try {
				String columnName = outCols.getJSONObject(i).getString("name");
				String fieldType = outCols.getJSONObject(i).getString("fieldType");
				String type = outCols.getJSONObject(i).getString("type");
				OutputColumn outputColumn = new OutputColumn(columnName, fieldType, type);
				outputColumns.put(columnName, outputColumn);
			} catch (Exception e) {
				logger.error("Error initializing output columns", e);
				outputColumns = new HashMap<String, OutputColumn>();
			}
		}
	}

	@Override
	public void transformDataSetMetaData(IDataStore dataStore) {
		IMetaData dataStoreMeta = dataStore.getMetaData();
		List<IFieldMetaData> newMeta = getNewFieldsMeta(dataStore);
		for (int i = 0; i < newMeta.size(); i++) {
			dataStoreMeta.addFiedMeta(newMeta.get(i));
		}
	}

	@Override
	public void transformDataSetRecords(IDataStore dataStore) {
		List<Field> newFields = getNewFields(dataStore);
		Iterator iterator = dataStore.iterator();
		int i = 0;
		while (iterator.hasNext()) {
			IRecord record = (IRecord) iterator.next();
			record.appendField(newFields.get(i));
			i++;
		}
	}

	private List<Field> getNewFields(IDataStore dataStore) {
		return new ArrayList<Field>();
	}

	private List<IFieldMetaData> getNewFieldsMeta(IDataStore dataStore) {
		return new ArrayList<IFieldMetaData>();
	}

}
