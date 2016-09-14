package it.eng.spagobi.engines.datamining.api;

import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.engines.datamining.DataMiningEngine;
import it.eng.spagobi.engines.datamining.DataMiningEngineInstance;
import it.eng.spagobi.engines.datamining.bo.DataMiningResult;
import it.eng.spagobi.engines.datamining.common.AbstractDataMiningEngineResource;
import it.eng.spagobi.engines.datamining.common.FunctionExecutionUtils;
import it.eng.spagobi.engines.datamining.common.FunctionExecutor;
import it.eng.spagobi.engines.datamining.common.utils.DataMiningConstants;
import it.eng.spagobi.engines.datamining.template.DataMiningTemplate;
import it.eng.spagobi.functions.dao.ICatalogFunctionDAO;
import it.eng.spagobi.functions.metadata.SbiCatalogFunction;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Path("/1.0/function")
@ManageAuthorization
public class FunctionResource extends AbstractDataMiningEngineResource {

	private static Set<String> functionIOFields = new HashSet<String>();

	public static transient Logger logger = Logger.getLogger(FunctionResource.class);

	static {
		functionIOFields.add(DataMiningConstants.VARIABLES_IN);
		functionIOFields.add(DataMiningConstants.DATASETS_IN);
		functionIOFields.add(DataMiningConstants.DATASETS_OUT);
		functionIOFields.add(DataMiningConstants.TEXT_OUT);
		functionIOFields.add(DataMiningConstants.IMAGE_OUT);
	}

	@GET
	@Path("/execute/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.FUNCTIONS_CATALOG_USAGE })
	public String executeSampleCatalogFunctionById(@PathParam("id") int id) {
		logger.debug("IN");
		JSONArray serviceResponse = new JSONArray();
		try {
			logger.debug("Creating engine instance ...");
			DataMiningTemplate template = FunctionExecutionUtils.initializeTemplateByFunctionId(id);
			DataMiningEngineInstance dataMiningEngineInstance = DataMiningEngine.createInstance(template, getEnv());
			logger.debug("Engine instance succesfully created");

			List<DataMiningResult> dataminingExecutionResults = FunctionExecutor.executeCatalogFunction(dataMiningEngineInstance, getUserProfile());
			serviceResponse = buildDataminingResponse(dataminingExecutionResults);
		} catch (Exception e) {
			logger.error("Error creating or starting the Data Mining engine, or problems getting execution results!", e);
			throw new SpagoBIEngineRuntimeException("Error creating or starting the Data Mining engine, or problems getting execution results!", e);
		} finally {
			logger.debug("OUT");
		}
		return serviceResponse.toString();
	}

	@GET
	@Path("/execute/{label}")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.FUNCTIONS_CATALOG_USAGE })
	public String executeSampleCatalogFunctionByLabel(@PathParam("label") String label) {
		logger.debug("IN");
		String response;
		try {
			ICatalogFunctionDAO fcDAO = DAOFactory.getCatalogFunctionDAO();
			fcDAO.setUserProfile(getUserProfile());
			SbiCatalogFunction function = fcDAO.getCatalogFunctionByLabel(label);

			if (function != null) {
				response = executeSampleCatalogFunctionById(function.getFunctionId());
			} else {
				throw new SpagoBIEngineRuntimeException("Function with label [" + label + "] does not exist");
			}
		} catch (Exception e) {
			logger.error("Error creating or starting the Data Mining engine, or problems getting execution results!", e);
			throw new SpagoBIEngineRuntimeException("Error creating or starting the Data Mining engine, or problems getting execution results!", e);
		} finally {
			logger.debug("OUT");
		}
		return response;
	}

	@POST
	@Path("/execute/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.FUNCTIONS_CATALOG_USAGE })
	public String executeCatalogFunctionById(String body, @PathParam("id") int id) {
		logger.debug("IN");
		JSONArray serviceResponse = new JSONArray();
		try {
			ObjectMapper mapper = new ObjectMapper();
			Map<String, Map<String, String>> functionIOMaps = new HashMap<String, Map<String, String>>(functionIOFields.size());

			JSONArray replacements = new JSONArray(body);
			for (int i = 0; i < replacements.length(); i++) {
				JSONObject object = replacements.getJSONObject(i);
				JSONObject items = object.getJSONObject("items");
				String type = object.getString("type");
				if (functionIOFields.contains(type)) {
					Map<String, String> map = mapper.readValue(items.toString(), new TypeReference<Map<String, String>>() {
					});
					functionIOMaps.put(type, map);
				}
			}

			logger.debug("Creating engine instance ...");
			// Every map is in the form <OldValue,NewValue>
			DataMiningTemplate template = FunctionExecutionUtils.getTemplateWithReplacingValues(id, body, functionIOMaps);
			DataMiningEngineInstance dataMiningEngineInstance = DataMiningEngine.createInstance(template, getEnv());
			logger.debug("Engine instance succesfully created");

			List<DataMiningResult> dataminingExecutionResults = FunctionExecutor.executeCatalogFunction(dataMiningEngineInstance, getUserProfile());
			serviceResponse = buildDataminingResponse(dataminingExecutionResults);
		} catch (Exception e) {
			logger.error("Error starting the Data Mining engine or getting datamining engine execution results!", e);
			throw new SpagoBIRuntimeException("Error starting the Data Mining engine or getting datamining engine execution results!", e);
		} finally {
			logger.debug("OUT");
		}
		return serviceResponse.toString();
	}

	@POST
	@Path("/execute/{label}")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.FUNCTIONS_CATALOG_USAGE })
	public String executeCatalogFunctionByLabel(String body, @PathParam("label") String label) {
		logger.debug("IN");
		String response;
		try {
			ICatalogFunctionDAO fcDAO = DAOFactory.getCatalogFunctionDAO();
			fcDAO.setUserProfile(getUserProfile());
			SbiCatalogFunction function = fcDAO.getCatalogFunctionByLabel(label);

			if (function != null) {
				response = executeCatalogFunctionById(body, function.getFunctionId());
			} else {
				throw new SpagoBIEngineRuntimeException("Function with label [" + label + "] does not exist");
			}
		} catch (Exception e) {
			logger.error("Error creating or starting the Data Mining engine, or problems getting execution results!", e);
			throw new SpagoBIEngineRuntimeException("Error creating or starting the Data Mining engine, or problems getting execution results!", e);
		} finally {
			logger.debug("OUT");
		}
		return response;
	}

	private JSONArray buildDataminingResponse(List<DataMiningResult> dataminingExecutionResults) throws JSONException {
		JSONArray response = new JSONArray();
		for (DataMiningResult r : dataminingExecutionResults) {
			JSONObject o = new JSONObject();
			o.put("resultType", r.getOutputType());
			o.put("result", r.getResult());
			if (r.getOutputType().equalsIgnoreCase("Image")) {
				o.put("resultName", r.getPlotName());
			} else { // Dataset Output o Text Output
				o.put("resultName", r.getVariablename());
			}
			response.put(o);
		}
		return response;
	}

}
