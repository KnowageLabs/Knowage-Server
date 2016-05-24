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
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IDomainDAO;
import it.eng.spagobi.functions.dao.ICatalogFunctionDAO;
import it.eng.spagobi.functions.metadata.SbiCatalogFunction;
import it.eng.spagobi.functions.metadata.SbiFunctionInputDataset;
import it.eng.spagobi.functions.metadata.SbiFunctionInputVariable;
import it.eng.spagobi.functions.metadata.SbiFunctionOutput;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.utilities.CatalogFunction;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	/*
	 * @GET
	 *
	 * @Path("/getCatalogFunction/{functionId}")
	 *
	 * @Produces(MediaType.APPLICATION_JSON) public String getCatalogFunction(@PathParam("functionId") int functionId) throws IOException { logger.debug("IN");
	 *
	 * JSONObject retObj = new JSONObject();
	 *
	 * try { ICatalogFunctionDAO fcDAO = DAOFactory.getCatalogFunctionDAO(); SbiCatalogFunction function = fcDAO.loadFunction(functionId);
	 * System.out.println(function.getName()); retObj.append("function", function);
	 *
	 * } catch (Exception e) { logger.error("Error returning function identified by id " + functionId, e); throw new
	 * SpagoBIServiceException("REST service /getCatalogFunction/", "Error retturning function ", e); }
	 *
	 * logger.debug("OUT"); return retObj.toString();
	 *
	 * }
	 */

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAllCatalogFunctions() throws IOException {
		logger.debug("IN");

		JSONObject retObj = new JSONObject();
		JSONArray funcArray = new JSONArray();
		List<SbiCatalogFunction> functions = null;

		try {
			ICatalogFunctionDAO fcDAO = DAOFactory.getCatalogFunctionDAO();
			functions = fcDAO.loadAllCatalogFunctions();
			for (SbiCatalogFunction f : functions) {
				JSONObject funcJsonObject = sbiFunctionToJsonObject(f);
				funcArray.put(funcJsonObject);

			}

			retObj.put("functions", funcArray);

		} catch (Exception e) {
			logger.error("Error returning all functions in the catalog");
			throw new SpagoBIServiceException("REST service /", "Error retturning all functions ", e);
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
			ret.put("language", sbiFunction.getLanguage());
			ret.put("script", sbiFunction.getScript());
			JSONArray inputVariables = new JSONArray();
			JSONArray inputDatasets = new JSONArray();

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

				String label = dsDAO.loadDataSetById(d.getId().getDsId()).getLabel();
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
			ret.put("inputVariables", inputVariables);
			ret.put("inputDatasets", inputDatasets);

			ret.put("outputItems", outputItems);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	@POST
	@Path("/setCatalogFunction")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.FUNCTIONS_CATALOG })
	public String setCatalogFunction(String body) throws IOException {
		logger.debug("IN");
		int catalogFunctionId = -1;
		ICatalogFunctionDAO catalogFunctionDAO = null;

		CatalogFunction itemToInsert = new CatalogFunction();
		JSONObject jsonObj = null;
		String functionName = null, language = null, script = null;
		JSONObject response = new JSONObject();

		try {

			jsonObj = new JSONObject(body);
			functionName = jsonObj.getString("name");
			language = jsonObj.getString("language");
			script = jsonObj.getString("script");

			JSONArray jsonInputDatasets = jsonObj.getJSONArray("inputDatasets");
			JSONArray jsonInputVariables = jsonObj.getJSONArray("inputVariables");

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
			itemToInsert.setName(functionName);
			itemToInsert.setLanguage(language);
			itemToInsert.setScript(script);

			catalogFunctionDAO = DAOFactory.getCatalogFunctionDAO();
			catalogFunctionId = catalogFunctionDAO.insertCatalogFunction(itemToInsert, inputDatasets, inputVariables, outputs);
			System.out.println("CATALOG_FUNCTION_ID=" + catalogFunctionId);
			response = jsonObj;
			response.put("id", catalogFunctionId);

		} catch (JSONException e2) {
			e2.printStackTrace();
		} catch (EMFUserError e1) {
			e1.printStackTrace();
		}

		return response.toString();
	}

	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("updateCatalogFunction/{functionId}")
	public String updateCatalogFunction(@PathParam("functionId") int functionId, String body) {

		logger.debug("IN");
		int catalogFunctionId = -1;
		ICatalogFunctionDAO catalogFunctionDAO = null;

		CatalogFunction itemToInsert = new CatalogFunction();
		JSONObject jsonObj = null;
		String functionName = null, language = null, script = null;
		JSONObject response = new JSONObject();

		try {

			jsonObj = new JSONObject(body);
			functionName = jsonObj.getString("name");
			language = jsonObj.getString("language");
			script = jsonObj.getString("script");

			JSONArray jsonInputDatasets = jsonObj.getJSONArray("inputDatasets");
			JSONArray jsonInputVariables = jsonObj.getJSONArray("inputVariables");

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
			itemToInsert.setName(functionName);
			itemToInsert.setLanguage(language);
			itemToInsert.setScript(script);
			itemToInsert.setSbiFunctionInputDatasets(inputDatasets);
			itemToInsert.setSbiFunctionOutput(outputs);
			itemToInsert.setSbiFunctionInputVariables(inputVariables);

			catalogFunctionDAO = DAOFactory.getCatalogFunctionDAO();
			catalogFunctionDAO.setUserProfile(getUserProfile());

			catalogFunctionDAO.updateCatalogFunction(itemToInsert, functionId);

			response.put("Response", "OK");

		} catch (JSONException e2) {
			e2.printStackTrace();
		} catch (EMFUserError e1) {
			e1.printStackTrace();
		}

		return response.toString();

	}

	@GET
	@Path("/deleteFunction/{functionId}")
	@Produces(MediaType.APPLICATION_JSON)
	public String deleteCatalogFunction(@PathParam("functionId") int functionId) {
		logger.debug("IN");

		JSONObject retObj = new JSONObject();

		try {
			ICatalogFunctionDAO fcDAO = DAOFactory.getCatalogFunctionDAO();
			fcDAO.deleteCatalogFunction(functionId);

			retObj.put("response", "OK");
		} catch (EMFUserError e) {
			logger.error("Error returning function identified by id " + functionId, e);
			e.printStackTrace();
		} catch (JSONException e2) {
			e2.printStackTrace();
		}

		logger.debug("OUT");

		return retObj.toString();

	}

	/*
	 * @GET
	 * 
	 * @Path("/executeFunction/{functionId}")
	 * 
	 * @Produces(MediaType.APPLICATION_JSON) public String executeCatalogFunction(@PathParam("functionId") int functionId) { logger.debug("IN");
	 * 
	 * JSONObject retObj = new JSONObject(); SbiCatalogFunction function = null;
	 * 
	 * try { ICatalogFunctionDAO fcDAO = DAOFactory.getCatalogFunctionDAO(); function = fcDAO.getCatalogFunctionById(functionId); DataMining
	 * DataMiningEngineInstance(DataMiningTemplate template, Map env) {
	 * 
	 * retObj.put("functionResult", function); } catch (EMFUserError e) { logger.error("Error returning function identified by id " + functionId, e);
	 * e.printStackTrace(); } catch (JSONException e2) { e2.printStackTrace(); }
	 * 
	 * logger.debug("OUT");
	 * 
	 * return retObj.toString();
	 * 
	 * }
	 */

}
