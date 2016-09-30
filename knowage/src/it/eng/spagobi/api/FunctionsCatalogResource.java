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
import it.eng.spagobi.functions.metadata.SbiFunctionInputFile;
import it.eng.spagobi.functions.metadata.SbiFunctionInputVariable;
import it.eng.spagobi.functions.metadata.SbiFunctionOutput;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.utilities.CatalogFunction;
import it.eng.spagobi.utilities.CatalogFunctionInputFile;
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

@Path("/1.0/functions-catalog")
@ManageAuthorization
public class FunctionsCatalogResource extends AbstractSpagoBIResource {
	public static transient Logger logger = Logger.getLogger(FunctionsCatalogResource.class);

	// @formatter:off
	/**
	 * @api {get} /1.0/functions-catalog Request Functions
	 * @apiName GET_getAllCatalogFunctions
	 * @apiGroup Functions
	 *
	 * @apiSuccess {json} functions The list of functions and keywords.
	 *
	 * @apiSuccessExample {json} Response-example: { "functions":[ { "id":16, "name":"tst_average", "description":"tst_average", "language":"R",
	 *                    "script":"x <- c($P{firstvar}, $P{secondvar}, $P{thirdvar})\nav <- mean(x)", "owner":"test_admin", "label":"tst_average",
	 *                    "type":"Utilities", "keywords":[ "average" ], "inputVariables":[ { "type":"Simple Input", "name":"thirdvar", "functionId":16,
	 *                    "value":"-5" }, { "type":"Simple Input", "name":"secondvar", "functionId":16, "value":"11" }, { "type":"Simple Input",
	 *                    "name":"firstvar", "functionId":16, "value":"5" } ], "inputDatasets":[
	 *
	 *                    ], "outputItems":[ { "type":"Text", "functionId":16, "label":"av" } ] }, { "id":17, "name":"tst_average_ds",
	 *                    "description":"tst_average_ds", "language":"R", "script":"z<-TST_FUNC_CAT_LP\ndsoutput<-rbind(z,rowMeans(z, na.rm = TRUE))\ndsoutput",
	 *                    "owner":"test_admin", "label":"tst_average_ds", "type":"Utilities", "keywords":[
	 *
	 *                    ], "inputVariables":[
	 *
	 *                    ], "inputDatasets":[ { "type":"Dataset Input", "functionId":17, "label":"TST_FUNC_CAT_LP" } ], "outputItems":[ { "type":"Dataset",
	 *                    "functionId":17, "label":"dsoutput" } ] } ], "keywords":[ "average" ] }
	 */
	// @formatter:on

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

	// @formatter:off
	/**
	 * @api {get} /1.0/functions-catalog/:type Request Functions by type
	 * @apiName GET_getCatalogFunctionsByType
	 * @apiGroup Functions
	 *
	 * @apiParam {String} type Function type.
	 *
	 * @apiSuccess {json} functions The list of functions and keywords with the specified type.
	 *
	 * @apiSuccessExample {json} Response-example: { "functions":[ { "id":16, "name":"tst_average", "description":"tst_average", "language":"R",
	 *                    "script":"x <- c($P{firstvar}, $P{secondvar}, $P{thirdvar})\nav <- mean(x)", "owner":"test_admin", "label":"tst_average",
	 *                    "type":"Utilities", "keywords":[ "average" ], "inputVariables":[ { "type":"Simple Input", "name":"thirdvar", "functionId":16,
	 *                    "value":"-5" }, { "type":"Simple Input", "name":"secondvar", "functionId":16, "value":"11" }, { "type":"Simple Input",
	 *                    "name":"firstvar", "functionId":16, "value":"5" } ], "inputDatasets":[
	 *
	 *                    ], "outputItems":[ { "type":"Text", "functionId":16, "label":"av" } ] }, { "id":17, "name":"tst_average_ds",
	 *                    "description":"tst_average_ds", "language":"R", "script":"z<-TST_FUNC_CAT_LP\ndsoutput<-rbind(z,rowMeans(z, na.rm = TRUE))\ndsoutput",
	 *                    "owner":"test_admin", "label":"tst_average_ds", "type":"Utilities", "keywords":[
	 *
	 *                    ], "inputVariables":[
	 *
	 *                    ], "inputDatasets":[ { "type":"Dataset Input", "functionId":17, "label":"TST_FUNC_CAT_LP" } ], "outputItems":[ { "type":"Dataset",
	 *                    "functionId":17, "label":"dsoutput" } ] } ], "keywords":[ "average" ] }
	 */
	// @formatter:on

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

	// @formatter:off
	/**
	 * @api {post} /1.0/functions-catalog/insert Insert Function
	 * @apiName POST_insertCatalogFunction
	 * @apiGroup Functions
	 *
	 * @apiParam {json} function Function detail.
	 * @apiParamExample {json} Request-Example:
	 *
	 *                  { "id":"", "name":"tst_average_ds", "description":"tst_average_ds", "language":"R",
	 *                  "script":"z<-TST_FUNC_CAT_LP\ndsoutput<-rbind(z,rowMeans(z, na.rm = TRUE))\nfieldoutput<-${field}\ndsoutput", "owner":"test_admin",
	 *                  "label":"tst_average_ds", "type":"Utilities", "keywords":[ "average", "cool", "math" ], "inputVariables":[ { "name":"field", "value":"5"
	 *                  } ], "inputDatasets":[ { "type":"Dataset Input", "functionId":17, "label":"TST_FUNC_CAT_LP" } ], "outputItems":[ { "type":"Dataset",
	 *                  "functionId":17, "label":"dsoutput" }, { "label":"fieldoutput", "type":"Text" } ] }
	 *
	 * @apiSuccess {json} response The response of the update.
	 *
	 * @apiSuccessExample {json} Response-example: { "Response":"OK" }
	 */
	// @formatter:on

	@POST
	@Path("/insert")
	@Produces(MediaType.APPLICATION_JSON)
	// @Consumes(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.FUNCTIONS_CATALOG_MANAGEMENT })
	public String insertCatalogFunction(String body) throws IOException {
		logger.debug("IN");
		ICatalogFunctionDAO catalogFunctionDAO = null;

		CatalogFunction itemToInsert = new CatalogFunction();
		JSONObject response = new JSONObject();

		try {
			int catalogFunctionId = -1;
			String url = "";
			JSONObject jsonObj = new JSONObject(body);
			String name = jsonObj.getString("name");
			String description = jsonObj.getString("description");
			String language = jsonObj.getString("language");
			String script = jsonObj.getString("script");
			String owner = (String) getUserProfile().getUserUniqueIdentifier();
			String label = jsonObj.getString("label");
			String type = jsonObj.getString("type");
			if (jsonObj.has("url")) {
				url = jsonObj.getString("url");
			}
			boolean remote = jsonObj.getBoolean("remote");

			JSONArray jsonInputDatasets = jsonObj.getJSONArray("inputDatasets");
			JSONArray jsonInputVariables = jsonObj.getJSONArray("inputVariables");
			JSONArray jsonInputFiles = jsonObj.getJSONArray("inputFiles");

			JSONArray jsonKeywords = jsonObj.getJSONArray("keywords");

			JSONArray outputItems = jsonObj.getJSONArray("outputItems");

			Map<String, String> inputVariables = new HashMap<String, String>();
			List<String> inputDatasets = new ArrayList<String>();
			List<CatalogFunctionInputFile> inputFiles = new ArrayList<CatalogFunctionInputFile>();

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

			for (int i = 0; i < jsonInputFiles.length(); i++) {

				JSONObject inputItemJSON = jsonInputFiles.getJSONObject(i);
				String fileName = inputItemJSON.getString("fileName");
				String alias = inputItemJSON.getString("alias");
				byte[] content = inputItemJSON.getString("content").getBytes();
				inputFiles.add(new CatalogFunctionInputFile(fileName, alias, content));
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
			itemToInsert.setUrl(url);
			itemToInsert.setRemote(remote);

			catalogFunctionDAO = DAOFactory.getCatalogFunctionDAO();
			catalogFunctionId = catalogFunctionDAO.insertCatalogFunction(itemToInsert, inputDatasets, inputVariables, outputs, inputFiles);
			logger.debug("Catalog function ID equals to [" + catalogFunctionId + "]");
			response = jsonObj;
			response.put("id", catalogFunctionId);

		} catch (EMFUserError | JSONException e) {
			throw new SpagoBIServiceException("Error while insert catalog function", e);
		}
		return response.toString();
	}

	// @formatter:off
	/**
	 * @api {put} /1.0/functions-catalog/update/:id Update Function
	 * @apiName PUT_updateCatalogFunction
	 * @apiGroup Functions
	 *
	 * @apiParam {String} id Function id.
	 *
	 * @apiParam {json} function Function detail.
	 * @apiParamExample {json} Request-Example:
	 *
	 *                  { "id":17, "name":"tst_average_ds", "description":"tst_average_ds", "language":"R",
	 *                  "script":"z<-TST_FUNC_CAT_LP\ndsoutput<-rbind(z,rowMeans(z, na.rm = TRUE))\nfieldoutput<-${field}\ndsoutput", "owner":"test_admin",
	 *                  "label":"tst_average_ds", "type":"Utilities", "keywords":[ "average", "cool", "math" ], "inputVariables":[ { "name":"field", "value":"5"
	 *                  } ], "inputDatasets":[ { "type":"Dataset Input", "functionId":17, "label":"TST_FUNC_CAT_LP" } ], "outputItems":[ { "type":"Dataset",
	 *                  "functionId":17, "label":"dsoutput" }, { "label":"fieldoutput", "type":"Text" } ] }
	 *
	 * @apiSuccess {json} response The response of the update.
	 *
	 * @apiSuccessExample {json} Response-example: { "Response":"OK" }
	 */
	// @formatter:on

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
			String language = jsonObj.getString("language");
			String script = jsonObj.getString("script");
			String owner = (String) getUserProfile().getUserUniqueIdentifier();
			String label = jsonObj.getString("label");
			String type = jsonObj.getString("type");
			String url = jsonObj.getString("url");
			boolean remote = jsonObj.getBoolean("remote");

			JSONArray jsonInputDatasets = jsonObj.getJSONArray("inputDatasets");
			JSONArray jsonInputVariables = jsonObj.getJSONArray("inputVariables");
			JSONArray jsonInputFiles = jsonObj.getJSONArray("inputFiles");

			JSONArray outputItems = jsonObj.getJSONArray("outputItems");
			JSONArray keywords = jsonObj.getJSONArray("keywords");

			Map<String, String> inputVariables = new HashMap<String, String>();
			List<String> inputDatasets = new ArrayList<String>();
			List<CatalogFunctionInputFile> inputFiles = new ArrayList<CatalogFunctionInputFile>();

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

			for (int i = 0; i < jsonInputFiles.length(); i++) {
				JSONObject inputItemJSON = jsonInputFiles.getJSONObject(i);
				String fileName = inputItemJSON.getString("fileName");
				byte[] content = inputItemJSON.getString("content").getBytes();
				String alias = inputItemJSON.getString("alias");
				inputFiles.add(new CatalogFunctionInputFile(fileName, alias, content));
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
			itemToInsert.setInputFiles(inputFiles);
			itemToInsert.setKeywords(keyList);
			itemToInsert.setLabel(label);
			itemToInsert.setType(type);
			if (url != null) {
				itemToInsert.setUrl(url);
			}

			itemToInsert.setRemote(remote);

			catalogFunctionDAO = DAOFactory.getCatalogFunctionDAO();
			catalogFunctionDAO.setUserProfile(getUserProfile());
			catalogFunctionDAO.updateCatalogFunction(itemToInsert, id);

			response.put("Response", "OK");

		} catch (EMFUserError | JSONException e) {
			throw new SpagoBIServiceException("Error while update catalog function " + id, e);
		}
		return response.toString();
	}

	// @formatter:off
	/**
	 * @api {get} /1.0/functions-catalog/delete/:id Delete Function by ID
	 * @apiName GET_deleteCatalogFunction
	 * @apiGroup Functions
	 *
	 * @apiParam {String} id Function id.
	 *
	 * @apiSuccess {json} response The response of the update.
	 *
	 * @apiSuccessExample {json} Response-example: { "Response":"OK" }
	 */
	// @formatter:on

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
		} catch (EMFUserError | JSONException e) {
			throw new SpagoBIServiceException("Error returning function identified by id " + id, e);
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
			ret.put("url", sbiFunction.getUrl());
			ret.put("remote", sbiFunction.getRemote());

			JSONArray inputVariables = new JSONArray();
			JSONArray inputDatasets = new JSONArray();
			JSONArray inputFiles = new JSONArray();

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

			for (Object obj : sbiFunction.getSbiFunctionInputFiles()) {
				JSONObject objToInsert = new JSONObject();
				SbiFunctionInputFile f = (SbiFunctionInputFile) obj;
				objToInsert.put("type", "Simple Input");
				objToInsert.put("fileName", f.getId().getFileName());
				objToInsert.put("functionId", f.getId().getFunctionId());
				objToInsert.put("content", f.getContent());
				objToInsert.put("alias", f.getAlias());

				inputFiles.put(objToInsert);
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
			ret.put("inputFiles", inputFiles);

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
