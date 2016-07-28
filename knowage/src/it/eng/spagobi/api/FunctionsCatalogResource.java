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
package it.eng.spagobi.api;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IDomainDAO;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.functions.dao.ICatalogFunctionDAO;
import it.eng.spagobi.functions.metadata.SbiCatalogFunction;
import it.eng.spagobi.functions.metadata.SbiFunctionInputDataset;
import it.eng.spagobi.functions.metadata.SbiFunctionInputVariable;
import it.eng.spagobi.functions.metadata.SbiFunctionOutput;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.utilities.CatalogFunction;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/1.0/FunctionsCatalog")
@ManageAuthorization
public class FunctionsCatalogResource extends AbstractSpagoBIResource {
	public static transient Logger logger = Logger.getLogger(FunctionsCatalogResource.class);

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
	@Path("/insertCatalogFunction")
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
			String language = jsonObj.getString("language");
			String script = jsonObj.getString("script");
			String owner = (String) getUserProfile().getUserUniqueIdentifier();
			String label = jsonObj.getString("label");
			String type = jsonObj.getString("type");

			JSONArray jsonInputDatasets = jsonObj.getJSONArray("inputDatasets");
			JSONArray jsonInputVariables = jsonObj.getJSONArray("inputVariables");
			JSONArray jsonKeywords = jsonObj.getJSONArray("keywords");

			JSONArray outputItems = jsonObj.getJSONArray("outputItems");

			Map<String, String> inputVariables = new HashMap<String, String>();
			List<String> inputDatasets = new ArrayList<String>();
			for (int i = 0; i < jsonInputDatasets.length(); i++) {

				JSONObject inputItemJSON = jsonInputDatasets.getJSONObject(i);

				String datasetLabel = inputItemJSON.getString("label");
				inputDatasets.add(datasetLabel);
			}

			for (int i = 0; i < jsonInputVariables.length(); i++) {

				JSONObject inputItemJSON = jsonInputVariables.getJSONObject(i);
				String varName = inputItemJSON.getString("name");
				String varValue = inputItemJSON.getString("value");
				inputVariables.put(varName, varValue);
			}

			Map<String, String> outputs = new HashMap<String, String>();
			for (int i = 0; i < outputItems.length(); i++) {

				JSONObject outputItemJSON = outputItems.getJSONObject(i);
				String outLabel = outputItemJSON.getString("label");
				String outType = outputItemJSON.getString("type");
				outputs.put(outLabel, outType);
			}

			List<String> keywordsList = new ArrayList<String>();
			for (int i = 0; i < jsonKeywords.length(); i++) {
				keywordsList.add(jsonKeywords.getString(i));
			}

			itemToInsert.setName(name);
			itemToInsert.setDescription(description);
			itemToInsert.setLanguage(language);
			itemToInsert.setScript(script);
			itemToInsert.setOwner(owner);
			itemToInsert.setKeywords(keywordsList);
			itemToInsert.setLabel(label);
			itemToInsert.setType(type);

			catalogFunctionDAO = DAOFactory.getCatalogFunctionDAO();
			catalogFunctionId = catalogFunctionDAO.insertCatalogFunction(itemToInsert, inputDatasets, inputVariables, outputs);
			logger.debug("Catalog function ID equals to [" + catalogFunctionId + "]");
			response = jsonObj;
			response.put("id", catalogFunctionId);

		} catch (EMFUserError | JSONException e) {
			throw new SpagoBIServiceException("Error while insert catalog function", e);
		}
		return response.toString();
	}

	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("updateCatalogFunction/{functionId}")
	@UserConstraint(functionalities = { SpagoBIConstants.FUNCTIONS_CATALOG_MANAGEMENT })
	public String updateCatalogFunction(@PathParam("functionId") int functionId, String body) {
		logger.debug("IN");
		ICatalogFunctionDAO catalogFunctionDAO = null;

		if (!hasPermission(functionId)) {
			throw new SpagoBIRuntimeException("You are not owner or administrator. Permission denied.");
		}

		CatalogFunction itemToInsert = new CatalogFunction();
		JSONObject response = new JSONObject();

		try {
			JSONObject jsonObj = new JSONObject(body);
			String name = jsonObj.getString("name");
			String description = jsonObj.getString("description");
			String language = jsonObj.getString("language");
			String script = jsonObj.getString("script");
			String owner = (String) getUserProfile().getUserUniqueIdentifier();
			String label = jsonObj.getString("label");
			String type = jsonObj.getString("type");

			JSONArray jsonInputDatasets = jsonObj.getJSONArray("inputDatasets");
			JSONArray jsonInputVariables = jsonObj.getJSONArray("inputVariables");
			JSONArray outputItems = jsonObj.getJSONArray("outputItems");
			JSONArray keywords = jsonObj.getJSONArray("keywords");

			Map<String, String> inputVariables = new HashMap<String, String>();
			List<String> inputDatasets = new ArrayList<String>();
			for (int i = 0; i < jsonInputDatasets.length(); i++) {
				JSONObject inputItemJSON = jsonInputDatasets.getJSONObject(i);
				String datasetLabel = inputItemJSON.getString("label");
				inputDatasets.add(datasetLabel);
			}

			for (int i = 0; i < jsonInputVariables.length(); i++) {
				JSONObject inputItemJSON = jsonInputVariables.getJSONObject(i);
				String varName = inputItemJSON.getString("name");
				String varValue = inputItemJSON.getString("value");
				inputVariables.put(varName, varValue);
			}

			Map<String, String> outputs = new HashMap<String, String>();
			for (int i = 0; i < outputItems.length(); i++) {
				JSONObject outputItemJSON = outputItems.getJSONObject(i);
				String outLabel = outputItemJSON.getString("label");
				String outType = outputItemJSON.getString("type");
				outputs.put(outLabel, outType);
			}
			List<String> keyList = new ArrayList<String>();
			for (int i = 0; i < keywords.length(); i++) {
				String key = keywords.getString(i);
				keyList.add(key);
			}

			itemToInsert.setName(name);
			itemToInsert.setDescription(description);
			itemToInsert.setLanguage(language);
			itemToInsert.setScript(script);
			itemToInsert.setOwner(owner);
			itemToInsert.setInputDatasets(inputDatasets);
			itemToInsert.setOutputs(outputs);
			itemToInsert.setInputVariables(inputVariables);
			itemToInsert.setKeywords(keyList);
			itemToInsert.setLabel(label);
			itemToInsert.setType(type);

			catalogFunctionDAO = DAOFactory.getCatalogFunctionDAO();
			catalogFunctionDAO.setUserProfile(getUserProfile());
			catalogFunctionDAO.updateCatalogFunction(itemToInsert, functionId);

			response.put("Response", "OK");

		} catch (EMFUserError | JSONException e) {
			throw new SpagoBIServiceException("Error while update catalog function " + functionId, e);
		}
		return response.toString();
	}

	@GET
	@Path("/deleteFunction/{functionId}")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.FUNCTIONS_CATALOG_MANAGEMENT })
	public String deleteCatalogFunction(@PathParam("functionId") int functionId) {
		logger.debug("IN");

		if (!hasPermission(functionId)) {
			throw new SpagoBIRuntimeException("You are not owner or administrator. Permission denied.");
		}

		// Delete functionalities can be used by administrator and developer
		JSONObject retObj = new JSONObject();
		try {
			ICatalogFunctionDAO fcDAO = DAOFactory.getCatalogFunctionDAO();
			fcDAO.setUserProfile(getUserProfile());
			fcDAO.deleteCatalogFunction(functionId);
			retObj.put("Response", "OK");
		} catch (EMFUserError | JSONException e) {
			throw new SpagoBIServiceException("Error returning function identified by id " + functionId, e);
		}
		logger.debug("OUT");
		return retObj.toString();
	}

	private JSONObject sbiFunctionToJsonObject(SbiCatalogFunction sbiFunction) {

		JSONObject ret = null;
		try {
			ret = new JSONObject();
			ret.put("id", sbiFunction.getFunctionId());
			ret.put("name", sbiFunction.getName());
			ret.put("description", sbiFunction.getDescription());
			ret.put("language", sbiFunction.getLanguage());
			ret.put("script", sbiFunction.getScript());
			ret.put("owner", sbiFunction.getOwner());
			ret.put("label", sbiFunction.getLabel());
			ret.put("type", sbiFunction.getType());

			JSONArray inputVariables = new JSONArray();
			JSONArray inputDatasets = new JSONArray();
			JSONArray keywords = new JSONArray();

			for (Object obj : sbiFunction.getSbiFunctionInputVariables()) {
				JSONObject objToInsert = new JSONObject();
				SbiFunctionInputVariable v = (SbiFunctionInputVariable) obj;
				objToInsert.put("type", "Simple Input");
				objToInsert.put("name", v.getId().getVarName());
				objToInsert.put("functionId", v.getId().getFunctionId());
				objToInsert.put("value", v.getVarValue());
				inputVariables.put(objToInsert);
			}

			IDataSetDAO dsDAO = DAOFactory.getDataSetDAO();
			for (Object obj : sbiFunction.getSbiFunctionInputDatasets()) {
				JSONObject objToInsert = new JSONObject();
				SbiFunctionInputDataset d = (SbiFunctionInputDataset) obj;
				objToInsert.put("type", "Dataset Input");
				objToInsert.put("functionId", d.getId().getFunctionId());
				inputDatasets.put(objToInsert);
				String label = null;
				IDataSet loadedDS = dsDAO.loadDataSetById(d.getId().getDsId());
				if (loadedDS != null) {
					label = loadedDS.getLabel();
				} else {
					label = "DS not found in DB";
				}
				objToInsert.put("label", label);
			}

			JSONArray outputItems = new JSONArray();
			IDomainDAO domainDAO = DAOFactory.getDomainDAO();
			for (Object obj : sbiFunction.getSbiFunctionOutputs()) {
				JSONObject objToInsert = new JSONObject();

				SbiFunctionOutput o = (SbiFunctionOutput) obj;

				objToInsert.put("type", "Simple Input");
				objToInsert.put("functionId", o.getId().getFunctionId());
				objToInsert.put("label", o.getId().getLabel());
				String typeName = domainDAO.loadDomainById(o.getOutType()).getValueName();
				objToInsert.put("type", typeName);
				outputItems.put(objToInsert);
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
			ret.put("inputDatasets", inputDatasets);

			ret.put("outputItems", outputItems);

		} catch (Exception e) {
			throw new SpagoBIServiceException("Error while insert catalog function", e);
		}
		return ret;
	}

	private boolean hasPermission(int functionId) {
		try {
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
					String userId = (String) profile.getUserUniqueIdentifier();
					if (function.getOwner().equals(userId)) {
						return true;
					} else
						return false;
				}
			}
		} catch (EMFUserError e) {
			throw new SpagoBIRuntimeException("Error while getting or using the DAO", e);
		}
	}
}
