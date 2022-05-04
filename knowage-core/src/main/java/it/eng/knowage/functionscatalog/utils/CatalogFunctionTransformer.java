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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import it.eng.knowage.backendservices.rest.widgets.PythonUtils;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.functions.dao.ICatalogFunctionDAO;
import it.eng.spagobi.services.common.JWTSsoService;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.tools.dataset.common.datareader.IDataReader;
import it.eng.spagobi.tools.dataset.common.datareader.JSONPathDataReader;
import it.eng.spagobi.tools.dataset.common.datareader.JSONPathDataReader.JSONPathAttribute;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.datastore.Record;
import it.eng.spagobi.tools.dataset.common.metadata.FieldMetadata;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.common.transformer.AbstractDataStoreTransformer;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.rest.RestUtilities.HttpMethod;

/**
 * @author Marco Balestri (marco.balestri@eng.it)
 *
 */
public class CatalogFunctionTransformer extends AbstractDataStoreTransformer {

	private final String functionUuid;
	private String script;
	private final CatalogFunctionRuntimeConfigDTO cfg;
	private UserProfile profile;
	private CatalogFunctionDataProxy proxy;
	private IDataStore newColumns;

	private static transient Logger logger = Logger.getLogger(CatalogFunctionTransformer.class);

	public CatalogFunctionTransformer(UserProfile profile, String functionUuid, CatalogFunctionRuntimeConfigDTO configDTO) {
		this.profile = profile;
		this.functionUuid = functionUuid;
		this.cfg = configDTO;
		init();
	}

	private void init() {
		initFunction();
		initProxy();
	}

	private void initFunction() {
		ICatalogFunctionDAO fcDAO = DAOFactory.getCatalogFunctionDAO();
		fcDAO.setUserProfile(profile);
		script = fcDAO.getCatalogFunctionScriptByUuidAndOrganization(functionUuid, TenantManager.getTenant().toString());
		if (script == null) {
			throw new SpagoBIRuntimeException("Couldn't retrieve function script from id: " + functionUuid);
		}
	}

	private void initProxy() {
		String address = getEngineAddress() + "catalog/execute";
		HttpMethod method = HttpMethod.valueOf("Post");
		Map<String, String> requestHeaders = new HashMap<String, String>();
		requestHeaders.put("Content-Type", "application/json");
		JSONObject requestBody = initRequestBody();
		proxy = new CatalogFunctionDataProxy(address, method, requestHeaders, requestBody);
	}

	String getEngineAddress() {
		String envLabel = null;
		String address = null;
		try {
			envLabel = cfg.getEnvironment();
			address = PythonUtils.getPythonAddress(envLabel);
		} catch (Exception e) {
			logger.error("Cannot retrieve environment <" + envLabel + "> address", e);
			throw new SpagoBIRuntimeException("Cannot retrieve environment <" + envLabel + "> address", e);
		}
		return address;
	}

	JSONObject initRequestBody() {
		JSONObject toReturn = new JSONObject();
		JSONObject inputs = new JSONObject();
		try {
			JSONArray inputColsArray = new JSONArray();
			for (String colName : cfg.getInputColumns().keySet()) {
				String dsColumn = cfg.getInputColumns().get(colName);
				inputColsArray.put(dsColumn);
			}

			JSONObject inputVarsObj = new JSONObject();
			for (String varName : cfg.getInputVariables().keySet()) {
				InputVariable var = cfg.getInputVariables().get(varName);
				JSONObject varObj = new JSONObject();
				varObj.put("type", var.getType());
				varObj.put("value", var.getValue());
				inputVarsObj.put(varName, varObj);
			}

			JSONArray outputColsArray = new JSONArray();
			for (String colName : cfg.getOutputColumns().keySet()) {
				OutputColumnRuntime outCol = cfg.getOutputColumns().get(colName);
				outputColsArray.put(outCol.getName());
			}

			inputs.put("inputColumns", inputColsArray);
			inputs.put("inputVariables", inputVarsObj);
			inputs.put("outputColumns", outputColsArray);

			toReturn.put("inputs", inputs);
			toReturn.put("token", getScriptJwtToken());
		} catch (Exception e) {
			logger.error("Error building request body", e);
			throw new SpagoBIRuntimeException("Error building request body", e);
		}
		return toReturn;
	}

	private String getScriptJwtToken() {
		// replace keywords
		for (String colName : cfg.getInputColumns().keySet()) {
			String dsColumn = cfg.getInputColumns().get(colName);
			script = script.replace("${" + colName + "}", "${" + dsColumn + "}");
		}
		for (String colName : cfg.getOutputColumns().keySet()) {
			OutputColumnRuntime outCol = cfg.getOutputColumns().get(colName);
			script = script.replace("${" + outCol.getName() + "}", "${" + colName + "}");
		}
		// create token
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MINUTE, 5);
		Date expiresAt = calendar.getTime();
		String jwtToken = JWTSsoService.pythonScript2jwtToken(script, expiresAt);

		return jwtToken;
	}

	@Override
	public void transformDataSetRecords(IDataStore dataStore) {
		try {
			proxy.setDataStore(dataStore);
			// we use JSON data reader with the same config used for Python DataSet
			IDataReader dataReader = new JSONPathDataReader("$[*]", new ArrayList<JSONPathAttribute>(), true, false);
			newColumns = proxy.load(dataReader);
			for (int i = 0; i < newColumns.getRecords().size(); i++) {
				IRecord newRecord = (Record) newColumns.getRecords().get(i);
				IRecord oldRecord = (Record) dataStore.getRecords().get(i);
				for (int j = 0; j < newRecord.getFields().size(); j++) {
					IField newField = newRecord.getFields().get(j);
					oldRecord.appendField(newField);
				}
			}
		} catch (CatalogFunctionException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Error transforming records: ", e);
			throw new SpagoBIRuntimeException("Error transforming records: ", e);
		}
	}

	@Override
	public void transformDataSetMetaData(IDataStore dataStore) {
		IMetaData dataStoreMeta = dataStore.getMetaData();
		List<IFieldMetaData> newMeta = getNewFieldsMeta();
		try {
			for (int i = 0; i < newMeta.size(); i++) {
				dataStoreMeta.addFiedMeta(newMeta.get(i));
			}
		} catch (Exception e) {
			logger.error("Error transforming metadata: ", e);
			throw new SpagoBIRuntimeException("Error transforming metadata: ", e);
		}
	}

	private List<IFieldMetaData> getNewFieldsMeta() {
		List<IFieldMetaData> newMetaList = new ArrayList<IFieldMetaData>();
		try {
			for (int i = 0; i < newColumns.getMetaData().getFieldsMeta().size(); i++) {
				IFieldMetaData proxyMeta = (IFieldMetaData) newColumns.getMetaData().getFieldsMeta().get(i);
				FieldMetadata newMeta = new FieldMetadata();
				newMeta.setName(proxyMeta.getName());
				String alias = getColumnAlias(proxyMeta.getName());
				newMeta.setAlias(StringUtils.isBlank(alias) ? proxyMeta.getName() : alias);
				newMeta.setType(proxyMeta.getType());
				newMeta.setFieldType(proxyMeta.getFieldType());
				newMetaList.add(newMeta);
			}
		} catch (Exception e) {
			logger.error("Error getting new fields meta", e);
			throw new SpagoBIRuntimeException("Error getting new fields meta", e);
		}
		return newMetaList;
	}

	private String getColumnAlias(String targetColName) {
		for (String colName : cfg.getOutputColumns().keySet()) {
			OutputColumnRuntime col = cfg.getOutputColumns().get(colName);
			if (targetColName.equals(col.getName()))
				return col.getAlias();
		}
		return null;
	}
}
