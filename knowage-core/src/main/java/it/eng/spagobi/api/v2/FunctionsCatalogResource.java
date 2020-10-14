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
package it.eng.spagobi.api.v2;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.axis.encoding.Base64;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.knowage.commons.security.KnowageSystemConfiguration;
import it.eng.knowage.functionscatalog.utils.InputVariable;
import it.eng.knowage.functionscatalog.utils.OutputColumn;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.functions.dao.ICatalogFunctionDAO;
import it.eng.spagobi.functions.metadata.IInputVariable;
import it.eng.spagobi.functions.metadata.IOutputColumn;
import it.eng.spagobi.functions.metadata.SbiCatalogFunction;
import it.eng.spagobi.functions.metadata.SbiFunctionInputColumn;
import it.eng.spagobi.functions.metadata.SbiFunctionInputVariable;
import it.eng.spagobi.functions.metadata.SbiFunctionOutputColumn;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.utilities.CatalogFunction;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;
import it.eng.spagobi.utilities.rest.RestUtilities.HttpMethod;
import it.eng.spagobi.utilities.rest.RestUtilities.Response;

@Path("/2.0/functions-catalog")
@ManageAuthorization
public class FunctionsCatalogResource extends AbstractSpagoBIResource {

	public static transient Logger logger = Logger.getLogger(FunctionsCatalogResource.class);

	public static String DATA_MINING_ENGINE_SUFFIX = "dataminingengine";

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.FUNCTIONS_CATALOG_USAGE, SpagoBIConstants.FUNCTIONS_CATALOG_MANAGEMENT })
	public String getAllCatalogFunctions() throws IOException {
		logger.debug("IN");

		JSONObject retObj = new JSONObject();

		try {
			JSONArray funcArray = new JSONArray();
			ICatalogFunctionDAO fcDAO = DAOFactory.getCatalogFunctionDAO();
			fcDAO.setUserProfile(getUserProfile());
			List<SbiCatalogFunction> functions = fcDAO.loadAllCatalogFunctions();

			// Addition: return a field keyword_list containing random selected keywords
			JSONArray keywordsArray = new JSONArray();
			Set<String> keywordSet = new HashSet<String>();

			for (SbiCatalogFunction f : functions) {
				JSONObject funcJsonObject = sbiFunctionToJsonObject(f);
				funcArray.put(funcJsonObject);
				for (String key : f.getKeywords().split(",")) {
					if (!key.equals("")) {
						keywordSet.add(key);
					}
				}
			}

			// returning 10 random keywords from all functions keywords. Putting keywords into a set randomize the choice and delete duplicates
			Object[] keywordSetArray = keywordSet.toArray();
			for (int i = 0; i < Math.min(keywordSetArray.length, 10); i++) {
				keywordsArray.put(keywordSetArray[i]);
			}

			retObj.put("functions", funcArray);
			retObj.put("keywords", keywordsArray);

		} catch (Exception e) {
			logger.error("Error returning all functions in the catalog");
			throw new SpagoBIServiceException("REST service /", "Error retturning all functions ", e);
		}

		logger.debug("OUT");
		return retObj.toString();

	}

	@GET
	@Path("/{type}")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.FUNCTIONS_CATALOG_USAGE, SpagoBIConstants.FUNCTIONS_CATALOG_MANAGEMENT })
	public String getCatalogFunctionsByType(@PathParam("type") String type) throws IOException {
		logger.debug("IN");

		JSONObject retObj = new JSONObject();

		try {
			JSONArray funcArray = new JSONArray();
			ICatalogFunctionDAO fcDAO = DAOFactory.getCatalogFunctionDAO();
			fcDAO.setUserProfile(getUserProfile());
			List<SbiCatalogFunction> allFunctions = fcDAO.loadAllCatalogFunctions();
			List<SbiCatalogFunction> filteredFunctions = new ArrayList<SbiCatalogFunction>();
			for (SbiCatalogFunction func : allFunctions) {
				if (func.getType().equals(type)) {
					filteredFunctions.add(func);
				}
			}

			// Addition: return a field keyword_list containing random selected keywords
			JSONArray keywordsArray = new JSONArray();
			Set<String> keywordSet = new HashSet<String>();

			for (SbiCatalogFunction f : filteredFunctions) {
				JSONObject funcJsonObject = sbiFunctionToJsonObject(f);
				funcArray.put(funcJsonObject);
				for (String key : f.getKeywords().split(",")) {
					if (!key.equals("")) {
						keywordSet.add(key);
					}
				}
			}

			// returning 10 random keywords from all functions keywords. Putting keywords into a set randomize the choice and delete duplicates
			Object[] keywordSetArray = keywordSet.toArray();
			for (int i = 0; i < Math.min(keywordSetArray.length, 10); i++) {
				keywordsArray.put(keywordSetArray[i]);
			}

			retObj.put("functions", funcArray);
			retObj.put("keywords", keywordsArray);

		} catch (Exception e) {
			logger.error("Error returning all functions in the catalog");
			throw new SpagoBIServiceException("REST service /", "Error retturning all functions ", e);
		}

		logger.debug("OUT");
		return retObj.toString();

	}

	@POST
	@Path("/insert")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.FUNCTIONS_CATALOG_MANAGEMENT })
	public String insertCatalogFunction(String body) throws IOException {
		logger.debug("IN");
		ICatalogFunctionDAO catalogFunctionDAO = null;

		CatalogFunction itemToInsert = new CatalogFunction();
		JSONObject response = new JSONObject();

		try {
			int catalogFunctionId = -1;
			JSONObject jsonObj = new JSONObject(body);
			String name = jsonObj.getString("name");
			String description = jsonObj.getString("description");
			String benchmarks = jsonObj.getString("benchmarks");
			String language = jsonObj.getString("language");
			String family = jsonObj.has("functionFamily") ? jsonObj.getString("functionFamily") : "online";
			String onlineScript = null, offlineScriptTrain = null, offlineScriptUse = null;
			if (family.equals("online")) {
				onlineScript = jsonObj.getString("onlineScript");
			} else {
				offlineScriptTrain = jsonObj.getString("offlineScriptTrainModel");
				offlineScriptUse = jsonObj.getString("offlineScriptUseModel");
			}
			String owner = (String) getUserProfile().getUserId();
			String label = jsonObj.getString("label");
			String type = jsonObj.getString("type");

			JSONArray jsonInputColumns = jsonObj.getJSONArray("inputColumns");
			JSONArray jsonInputVariables = jsonObj.getJSONArray("inputVariables");

			JSONArray jsonKeywords = jsonObj.getJSONArray("keywords");

			JSONArray jsonOutputColumns = jsonObj.getJSONArray("outputColumns");

			Map<String, InputVariable> inputVariables = new HashMap<String, InputVariable>();
			Map<String, String> inputColumns = new HashMap<String, String>();
			Map<String, IOutputColumn> outputColumns = new HashMap<String, IOutputColumn>();

			for (int i = 0; i < jsonInputColumns.length(); i++) {

				JSONObject inputItemJSON = jsonInputColumns.getJSONObject(i);
				String colName = inputItemJSON.getString("name");
				String colType = inputItemJSON.getString("type");
				inputColumns.put(colName, colType);
			}

			for (int i = 0; i < jsonInputVariables.length(); i++) {

				JSONObject inputItemJSON = jsonInputVariables.getJSONObject(i);
				String varName = inputItemJSON.getString("name");
				String varType = inputItemJSON.getString("type");
				String varValue = inputItemJSON.optString("value");
				inputVariables.put(varName, new InputVariable(varName, varType, varValue));
			}

			for (int i = 0; i < jsonOutputColumns.length(); i++) {

				JSONObject outputItemJSON = jsonOutputColumns.getJSONObject(i);
				String colName = outputItemJSON.getString("name");
				String colFieldType = outputItemJSON.getString("fieldType");
				String colType = outputItemJSON.getString("type");
				outputColumns.put(colName, new OutputColumn(colName, colFieldType, colType));
			}

			List<String> keywordsList = new ArrayList<String>();
			for (int i = 0; i < jsonKeywords.length(); i++) {
				keywordsList.add(jsonKeywords.getString(i));
			}

			itemToInsert.setName(name);
			itemToInsert.setDescription(description);
			itemToInsert.setBenchmarks(benchmarks);
			itemToInsert.setLanguage(language);
			itemToInsert.setFamily(family);
			itemToInsert.setOnlineScript(onlineScript);
			itemToInsert.setOfflineScriptTrain(offlineScriptTrain);
			itemToInsert.setOfflineScriptUse(offlineScriptUse);
			itemToInsert.setOwner(owner);
			itemToInsert.setKeywords(keywordsList);
			itemToInsert.setLabel(label);
			itemToInsert.setType(type);

			catalogFunctionDAO = DAOFactory.getCatalogFunctionDAO();
			catalogFunctionId = catalogFunctionDAO.insertCatalogFunction(itemToInsert, inputColumns, inputVariables, outputColumns);
			logger.debug("Catalog function ID equals to [" + catalogFunctionId + "]");
			response = jsonObj;
			response.put("id", catalogFunctionId);

		} catch (EMFUserError | JSONException e) {
			throw new SpagoBIServiceException("Error while insert catalog function", e);
		}
		return response.toString();
	}

	@PUT
	@Path("/update/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.FUNCTIONS_CATALOG_MANAGEMENT })
	public String updateCatalogFunction(@PathParam("id") int id, String body) {
		logger.debug("IN");
		ICatalogFunctionDAO catalogFunctionDAO = null;

		if (!hasPermission(id)) {
			throw new SpagoBIRuntimeException("You are not owner or administrator. Permission denied.");
		}

		CatalogFunction itemToInsert = new CatalogFunction();
		JSONObject response = new JSONObject();

		try {
			JSONObject jsonObj = new JSONObject(body);
			String name = jsonObj.getString("name");
			String description = jsonObj.getString("description");
			String benchmarks = jsonObj.getString("benchmarks");
			String language = jsonObj.getString("language");
			String family = jsonObj.has("functionFamily") ? jsonObj.getString("functionFamily") : "online";
			String onlineScript = null, offlineScriptTrain = null, offlineScriptUse = null;
			if (family.equals("online")) {
				onlineScript = jsonObj.getString("onlineScript");
			} else {
				offlineScriptTrain = jsonObj.getString("offlineScriptTrainModel");
				offlineScriptUse = jsonObj.getString("offlineScriptUseModel");
			}
			String owner = (String) getUserProfile().getUserId();
			String label = jsonObj.getString("label");
			String type = jsonObj.getString("type");

			JSONArray jsonInputColumns = jsonObj.getJSONArray("inputColumns");
			JSONArray jsonInputVariables = jsonObj.getJSONArray("inputVariables");
			JSONArray jsonOutputColumns = jsonObj.getJSONArray("outputColumns");
			JSONArray keywords = jsonObj.getJSONArray("keywords");

			Map<String, String> inputColumns = new HashMap<String, String>();
			Map<String, IOutputColumn> outputColumns = new HashMap<String, IOutputColumn>();
			Map<String, IInputVariable> inputVariables = new HashMap<String, IInputVariable>();

			for (int i = 0; i < jsonInputColumns.length(); i++) {
				JSONObject inputColumnJSON = jsonInputColumns.getJSONObject(i);
				String colName = inputColumnJSON.getString("name");
				String colType = inputColumnJSON.getString("type");
				inputColumns.put(colName, colType);
			}

			for (int i = 0; i < jsonInputVariables.length(); i++) {
				JSONObject inputItemJSON = jsonInputVariables.getJSONObject(i);
				String varName = inputItemJSON.getString("name");
				String varType = inputItemJSON.getString("type");
				String varValue = inputItemJSON.optString("value");
				inputVariables.put(varName, new InputVariable(varName, varType, varValue));
			}

			for (int i = 0; i < jsonOutputColumns.length(); i++) {
				JSONObject outputColumnJSON = jsonOutputColumns.getJSONObject(i);
				String colName = outputColumnJSON.getString("name");
				String colFieldType = outputColumnJSON.getString("fieldType");
				String colType = outputColumnJSON.getString("type");
				outputColumns.put(colName, new OutputColumn(colName, colFieldType, colType));
			}

			List<String> keyList = new ArrayList<String>();
			for (int i = 0; i < keywords.length(); i++) {
				String key = keywords.getString(i);
				keyList.add(key);
			}

			itemToInsert.setName(name);
			itemToInsert.setDescription(description);
			itemToInsert.setBenchmarks(benchmarks);
			itemToInsert.setLanguage(language);
			itemToInsert.setFamily(family);
			itemToInsert.setOnlineScript(onlineScript);
			itemToInsert.setOfflineScriptTrain(offlineScriptTrain);
			itemToInsert.setOfflineScriptUse(offlineScriptUse);
			itemToInsert.setOwner(owner);
			itemToInsert.setInputColumns(inputColumns);
			itemToInsert.setOutputColumns(outputColumns);
			itemToInsert.setInputVariables(inputVariables);
			itemToInsert.setKeywords(keyList);
			itemToInsert.setLabel(label);
			itemToInsert.setType(type);

			catalogFunctionDAO = DAOFactory.getCatalogFunctionDAO();
			catalogFunctionDAO.setUserProfile(getUserProfile());

			SbiCatalogFunction oldFunction = catalogFunctionDAO.getCatalogFunctionById(id);
			if (oldFunction == null) {
				throw new SpagoBIRuntimeException("no old function in db with Id:" + id);
			}

			catalogFunctionDAO.updateCatalogFunction(itemToInsert, id);

			response.put("Response", "OK");

		} catch (JSONException e) {
			throw new SpagoBIServiceException("Error while update catalog function " + id, e);
		}
		return response.toString();
	}

	@GET
	@Path("/delete/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.FUNCTIONS_CATALOG_MANAGEMENT })
	public String deleteCatalogFunction(@PathParam("id") int id) {
		logger.debug("IN");

		if (!hasPermission(id)) {
			throw new SpagoBIRuntimeException("You are not owner or administrator. Permission denied.");
		}

		// Delete functionalities can be used by administrator and developer
		JSONObject retObj = new JSONObject();
		try {
			ICatalogFunctionDAO fcDAO = DAOFactory.getCatalogFunctionDAO();
			fcDAO.setUserProfile(getUserProfile());
			fcDAO.deleteCatalogFunction(id);
			retObj.put("Response", "OK");
		} catch (JSONException e) {
			throw new SpagoBIServiceException("Error returning function identified by id " + id, e);
		}
		logger.debug("OUT");
		return retObj.toString();
	}

	private JSONObject sbiFunctionToJsonObject(SbiCatalogFunction sbiFunction) {

		JSONObject ret = null;
		boolean hasPermission = hasPermission(sbiFunction.getFunctionId());
		try {
			ret = new JSONObject();
			ret.put("id", sbiFunction.getFunctionId());
			ret.put("name", sbiFunction.getName());
			ret.put("description", sbiFunction.getDescription());
			ret.put("benchmarks", sbiFunction.getBenchmarks());
			ret.put("language", hasPermission ? sbiFunction.getLanguage() : "");
			ret.put("family", hasPermission ? sbiFunction.getFamily() : "");
			ret.put("onlineScript", hasPermission ? sbiFunction.getOnlineScript() : "");
			ret.put("offlineScriptTrainModel", hasPermission ? sbiFunction.getOfflineScriptTrain() : "");
			ret.put("offlineScriptUseModel", hasPermission ? sbiFunction.getOfflineScriptUse() : "");
			ret.put("owner", sbiFunction.getOwner());
			ret.put("label", sbiFunction.getLabel());
			ret.put("type", sbiFunction.getType());

			JSONArray inputVariables = new JSONArray();
			JSONArray inputColumns = new JSONArray();
			JSONArray outputColumns = new JSONArray();

			JSONArray keywords = new JSONArray();

			for (Object obj : sbiFunction.getSbiFunctionInputVariables()) {
				JSONObject objToInsert = new JSONObject();
				SbiFunctionInputVariable v = (SbiFunctionInputVariable) obj;
				objToInsert.put("name", v.getId().getVarName());
				objToInsert.put("type", v.getVarType());
				objToInsert.put("value", v.getVarValue());
				inputVariables.put(objToInsert);
			}

			for (Object obj : sbiFunction.getSbiFunctionInputColumns()) {
				JSONObject objToInsert = new JSONObject();
				SbiFunctionInputColumn c = (SbiFunctionInputColumn) obj;
				objToInsert.put("name", c.getId().getColName());
				objToInsert.put("type", c.getColType());
				inputColumns.put(objToInsert);
			}

			for (Object obj : sbiFunction.getSbiFunctionOutputColumns()) {
				JSONObject objToInsert = new JSONObject();
				SbiFunctionOutputColumn c = (SbiFunctionOutputColumn) obj;
				objToInsert.put("name", c.getId().getColName());
				objToInsert.put("fieldType", c.getColFieldType());
				objToInsert.put("type", c.getColType());
				outputColumns.put(objToInsert);
			}

			String keywordsString = sbiFunction.getKeywords();
			if (keywordsString != null && !keywordsString.equals("")) {
				String[] keywordArray = keywordsString.split(",");
				for (String keyword : keywordArray) {
					keywords.put(keyword);
				}
			}

			ret.put("keywords", keywords);
			ret.put("inputVariables", inputVariables);
			ret.put("inputColumns", inputColumns);
			ret.put("outputColumns", outputColumns);

		} catch (Exception e) {
			throw new SpagoBIServiceException("Error while insert catalog function", e);
		}
		return ret;
	}

	private boolean hasPermission(int functionId) {
		UserProfile profile = getUserProfile();
		if (UserUtilities.hasAdministratorRole(profile)) {
			return true;
		} else { // is a developer
			ICatalogFunctionDAO catalogFunctionDAO = DAOFactory.getCatalogFunctionDAO();
			catalogFunctionDAO.setUserProfile(profile);
			SbiCatalogFunction function = catalogFunctionDAO.getCatalogFunctionById(functionId);
			if (function == null) {
				throw new SpagoBIRuntimeException("Impossible to find a function with ID [" + functionId + "]. Cannot update or delete.");
			} else {
				String userId = (String) profile.getUserId();
				if (function.getOwner().equals(userId)) {
					return true;
				} else
					return false;
			}
		}
	}

	// ------------------- START FUNCTIONS CATALOG EXECUTE FORWARDER ------------------

	@GET
	@Path("/execute/sample/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.FUNCTIONS_CATALOG_USAGE, SpagoBIConstants.FUNCTIONS_CATALOG_MANAGEMENT })
	public String executeSampleCatalogFunctionById(@PathParam("id") int id) {
		logger.debug("IN");
		logger.debug("Received request for executing function with id [" + id + "]");
		Response response;
		try {
			Map<String, String> headers = getHeadersToForward(request);
			String url = getForwardingUrl(request);
			response = RestUtilities.makeRequest(HttpMethod.Get, url, headers, "", null, true);
		} catch (Exception e) {
			throw new SpagoBIEngineRuntimeException("Error while attempting to forward request to the datamining engine or getting the response", e);
		} finally {
			logger.debug("OUT");
		}
		return response.getResponseBody();
	}

	@GET
	@Path("/execute/sample")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.FUNCTIONS_CATALOG_USAGE, SpagoBIConstants.FUNCTIONS_CATALOG_MANAGEMENT })
	public String executeSampleCatalogFunctionByLabel(@QueryParam("label") String label) {
		logger.debug("IN");
		logger.debug("Received request for executing function with label [" + label + "]");
		Response response;
		try {
			Map<String, String> headers = getHeadersToForward(request);
			String url = getForwardingUrl(request);
			response = RestUtilities.makeRequest(HttpMethod.Get, url, headers, "", null, true);
		} catch (Exception e) {
			throw new SpagoBIEngineRuntimeException("Error while attempting to forward request to the datamining engine or getting the response", e);
		} finally {
			logger.debug("OUT");
		}
		return response.getResponseBody();
	}

	@POST
	@Path("/execute/new/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.FUNCTIONS_CATALOG_USAGE, SpagoBIConstants.FUNCTIONS_CATALOG_MANAGEMENT })
	public String executeCatalogFunctionById(String body, @PathParam("id") int id) {
		logger.debug("IN");
		logger.debug("Received request for executing function with id [" + id + "]");
		Response response;
		try {
			Map<String, String> headers = getHeadersToForward(request);
			String url = getForwardingUrl(request);
			response = RestUtilities.makeRequest(HttpMethod.Post, url, headers, body, null, true);
		} catch (Exception e) {
			throw new SpagoBIEngineRuntimeException("Error while attempting to forward request to the datamining engine or getting the response", e);
		} finally {
			logger.debug("OUT");
		}
		return response.getResponseBody();
	}

	@POST
	@Path("/execute/new")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.FUNCTIONS_CATALOG_USAGE, SpagoBIConstants.FUNCTIONS_CATALOG_MANAGEMENT })
	public String executeCatalogFunctionByLabel(String body, @QueryParam("label") String label) {
		logger.debug("IN");
		logger.debug("Received request for executing function with label [" + label + "]");
		Response response;
		try {
			Map<String, String> headers = getHeadersToForward(request);
			String url = getForwardingUrl(request);
			response = RestUtilities.makeRequest(HttpMethod.Post, url, headers, body, null, true);
		} catch (Exception e) {
			throw new SpagoBIEngineRuntimeException("Error while attempting to forward request to the datamining engine or getting the response", e);
		} finally {
			logger.debug("OUT");
		}
		return response.getResponseBody();
	}

	private Map<String, String> getHeadersToForward(HttpServletRequest request) throws UnsupportedEncodingException {
		Map<String, String> headers = RestUtilities.toHeaders(request);
		String userId = (String) getUserProfile().getUserUniqueIdentifier();
		logger.debug("Adding auth for user " + userId);
		String encodedBytes = Base64.encode(userId.getBytes("UTF-8"));
		headers.put("Authorization", "Direct " + encodedBytes);
		return headers;
	}

	private String getForwardingUrl(HttpServletRequest request) {
		String knowageContext = KnowageSystemConfiguration.getKnowageContext();
		String resourceUri = request.getRequestURI().replaceFirst(knowageContext, knowageContext + DATA_MINING_ENGINE_SUFFIX);
		String queryParams = request.getQueryString();

		StringBuilder sb = new StringBuilder();
		sb.append(resourceUri);
		if (queryParams != null) {
			sb.append("?");
			sb.append(queryParams);
		}

		return sb.toString();
	}
}
