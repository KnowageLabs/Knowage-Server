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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.Valid;
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

import it.eng.knowage.functionscatalog.utils.CatalogFunctionDTO;
import it.eng.knowage.functionscatalog.utils.InputColumnDTO;
import it.eng.knowage.functionscatalog.utils.InputVariable;
import it.eng.knowage.functionscatalog.utils.InputVariableDTO;
import it.eng.knowage.functionscatalog.utils.OutputColumn;
import it.eng.knowage.functionscatalog.utils.OutputColumnDTO;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.functions.dao.FunctionInUseException;
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
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

@Path("/2.0/functions-catalog")
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
				if (f.getKeywords() != null && f.getKeywords().split(",").length > 0) {
					for (String key : f.getKeywords().split(",")) {
						if (!key.equals("")) {
							keywordSet.add(key);
						}
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
	@Path("/keywords")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.FUNCTIONS_CATALOG_USAGE, SpagoBIConstants.FUNCTIONS_CATALOG_MANAGEMENT })
	public String getAllKeywords() throws IOException {
		logger.debug("IN");
		JSONArray keywordsArray = new JSONArray();

		try {
			ICatalogFunctionDAO fcDAO = DAOFactory.getCatalogFunctionDAO();
			fcDAO.setUserProfile(getUserProfile());
			List<SbiCatalogFunction> functions = fcDAO.loadAllCatalogFunctions();

			// Addition: return a field keyword_list containing random selected keywords

			Set<String> keywordSet = new HashSet<String>();

			for (SbiCatalogFunction f : functions) {
				JSONObject funcJsonObject = sbiFunctionToJsonObject(f);
				if (f.getKeywords() != null && f.getKeywords().split(",").length > 0) {
					for (String key : f.getKeywords().split(",")) {
						if (!key.equals("")) {
							keywordSet.add(key);
						}
					}
				}
			}

			// returning 10 random keywords from all functions keywords. Putting keywords into a set randomize the choice and delete duplicates
			Object[] keywordSetArray = keywordSet.toArray();
			for (int i = 0; i < Math.min(keywordSetArray.length, 10); i++) {
				keywordsArray.put(keywordSetArray[i]);
			}

		} catch (Exception e) {
			logger.error("Error returning all functions in the catalog");
			throw new SpagoBIServiceException("REST service /", "Error retturning all functions ", e);
		}

		logger.debug("OUT");
		return keywordsArray.toString();

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
				if (func.getType() != null && func.getType().equals(type)) {
					filteredFunctions.add(func);
				}
			}

			// Addition: return a field keyword_list containing random selected keywords
			JSONArray keywordsArray = new JSONArray();
			Set<String> keywordSet = new HashSet<String>();

			for (SbiCatalogFunction f : filteredFunctions) {
				JSONObject funcJsonObject = sbiFunctionToJsonObject(f);
				funcArray.put(funcJsonObject);
				if (f.getKeywords() != null && f.getKeywords().split(",").length > 0) {
					for (String key : f.getKeywords().split(",")) {
						if (!key.equals("")) {
							keywordSet.add(key);
						}
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
	public String insertCatalogFunction(@Valid CatalogFunctionDTO funcDTO) throws IOException {
		logger.debug("IN");
		JSONObject response = new JSONObject();
		try {
			List<String> keywords = funcDTO.getKeywords();
			CatalogFunction itemToInsert = toCatalogFunction(funcDTO, keywords);
			Map<String, IInputVariable> inputVariables = toInputVariablesMap(funcDTO.getInputVariables());
			Map<String, String> inputColumns = toInputColumnsMap(funcDTO.getInputColumns());
			Map<String, IOutputColumn> outputColumns = toOutputColumnsMap(funcDTO.getOutputColumns());

			ICatalogFunctionDAO catalogFunctionDAO = DAOFactory.getCatalogFunctionDAO();
			String catalogFunctionUuid = catalogFunctionDAO.insertCatalogFunction(itemToInsert, inputColumns, inputVariables, outputColumns);

			logger.debug("Catalog function ID equals to [" + catalogFunctionUuid + "]");
			response.put("id", catalogFunctionUuid);
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
	public String updateCatalogFunction(@PathParam("id") String uuid, @Valid CatalogFunctionDTO funcDTO) {
		logger.debug("IN");
		JSONObject response = new JSONObject();

		if (!hasPermission(uuid)) {
			throw new SpagoBIRuntimeException("You are not owner or administrator. Permission denied.");
		}

		try {
			List<String> keywords = funcDTO.getKeywords();
			CatalogFunction itemToInsert = toCatalogFunction(funcDTO, keywords);
			Map<String, IInputVariable> inputVariables = toInputVariablesMap(funcDTO.getInputVariables());
			Map<String, String> inputColumns = toInputColumnsMap(funcDTO.getInputColumns());
			Map<String, IOutputColumn> outputColumns = toOutputColumnsMap(funcDTO.getOutputColumns());
			itemToInsert.setInputColumns(inputColumns);
			itemToInsert.setOutputColumns(outputColumns);
			itemToInsert.setInputVariables(inputVariables);

			ICatalogFunctionDAO catalogFunctionDAO = DAOFactory.getCatalogFunctionDAO();
			catalogFunctionDAO.setUserProfile(getUserProfile());

			SbiCatalogFunction oldFunction = catalogFunctionDAO.getCatalogFunctionByUuid(uuid);
			if (oldFunction == null) {
				throw new SpagoBIRuntimeException("no old function in db with Id:" + uuid);
			}
			catalogFunctionDAO.updateCatalogFunction(itemToInsert, uuid);

			response.put("Response", "OK");
		} catch (JSONException e) {
			throw new SpagoBIServiceException("Error while update catalog function " + uuid, e);
		}
		return response.toString();
	}

	@GET
	@Path("/delete/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.FUNCTIONS_CATALOG_MANAGEMENT })
	public String deleteCatalogFunction(@PathParam("id") String uuid) {
		logger.debug("IN");

		if (!hasPermission(uuid)) {
			throw new SpagoBIRuntimeException("You are not owner or administrator. Permission denied.");
		}

		// Delete functionalities can be used by administrator and developer
		JSONObject retObj = new JSONObject();
		try {
			ICatalogFunctionDAO fcDAO = DAOFactory.getCatalogFunctionDAO();
			fcDAO.setUserProfile(getUserProfile());
			fcDAO.deleteCatalogFunction(uuid);
			retObj.put("Response", "OK");
		} catch (FunctionInUseException fiue) {
			try {
				retObj.put("FunctionInUseException", fiue.getMessage());
			} catch (JSONException e) {
				throw new SpagoBIRuntimeException(e);
			}
		} catch (Exception e) {
			String message = "Error while deleting the function with uuid: " + uuid;
			logger.error(message, e);
			throw new SpagoBIRuntimeException(message, e);
		}
		logger.debug("OUT");
		return retObj.toString();
	}

	private JSONObject sbiFunctionToJsonObject(SbiCatalogFunction sbiFunction) {

		JSONObject ret = null;
		boolean hasPermission = hasPermission(sbiFunction.getFunctionUuid());
		try {
			ret = new JSONObject();
			ret.put("id", sbiFunction.getFunctionUuid());
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

	private boolean hasPermission(String functionUuid) {
		UserProfile profile = getUserProfile();
		if (UserUtilities.hasAdministratorRole(profile)) {
			return true;
		} else { // is a developer
			ICatalogFunctionDAO catalogFunctionDAO = DAOFactory.getCatalogFunctionDAO();
			catalogFunctionDAO.setUserProfile(profile);
			SbiCatalogFunction function = catalogFunctionDAO.getCatalogFunctionByUuid(functionUuid);
			if (function == null) {
				throw new SpagoBIRuntimeException("Impossible to find a function with ID [" + functionUuid + "]. Cannot update or delete.");
			} else {
				String userId = (String) profile.getUserId();
				if (function.getOwner().equals(userId)) {
					return true;
				} else
					return false;
			}
		}
	}

	CatalogFunction toCatalogFunction(CatalogFunctionDTO funcDTO, List<String> keywords) {
		CatalogFunction toReturn = new CatalogFunction();

		toReturn.setName(funcDTO.getName());
		toReturn.setDescription(funcDTO.getDescription());
		toReturn.setBenchmarks(funcDTO.getBenchmarks());
		toReturn.setLanguage(funcDTO.getLanguage());
		toReturn.setFamily(funcDTO.getFamily());
		toReturn.setOnlineScript(funcDTO.getOnlineScript());
		toReturn.setOfflineScriptTrain(funcDTO.getOfflineScriptTrainModel());
		toReturn.setOfflineScriptUse(funcDTO.getOfflineScriptUseModel());
		toReturn.setOwner((String) getUserProfile().getUserId());
		toReturn.setKeywords(keywords);
		toReturn.setLabel(funcDTO.getLabel());
		toReturn.setType(funcDTO.getType());

		return toReturn;
	}

	private Map<String, String> toInputColumnsMap(List<InputColumnDTO> inputColumnsDTO) {
		Map<String, String> inputColumns = new HashMap<String, String>();
		for (int i = 0; i < inputColumnsDTO.size(); i++) {
			InputColumnDTO inputCol = inputColumnsDTO.get(i);
			String colName = inputCol.getName();
			String colType = inputCol.getType();
			inputColumns.put(colName, colType);
		}
		return inputColumns;
	}

	private Map<String, IInputVariable> toInputVariablesMap(List<InputVariableDTO> inputVariablesDTO) {
		Map<String, IInputVariable> inputVariables = new HashMap<String, IInputVariable>();
		for (int i = 0; i < inputVariablesDTO.size(); i++) {
			InputVariableDTO inputVar = inputVariablesDTO.get(i);
			String varName = inputVar.getName();
			String varType = inputVar.getType();
			String varValue = inputVar.getValue();
			inputVariables.put(varName, new InputVariable(varName, varType, varValue));
		}
		return inputVariables;
	}

	private Map<String, IOutputColumn> toOutputColumnsMap(List<OutputColumnDTO> outputColumnsDTO) {
		Map<String, IOutputColumn> outputColumns = new HashMap<String, IOutputColumn>();
		for (int i = 0; i < outputColumnsDTO.size(); i++) {
			OutputColumnDTO outputCol = outputColumnsDTO.get(i);
			String colName = outputCol.getName();
			String colFieldType = outputCol.getFieldType();
			String colType = outputCol.getType();
			outputColumns.put(colName, new OutputColumn(colName, colFieldType, colType));
		}
		return outputColumns;
	}

}
