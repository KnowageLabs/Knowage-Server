package it.eng.spagobi.engines.datamining.api;

import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.engines.datamining.common.AbstractDataMiningEngineResource;
import it.eng.spagobi.engines.datamining.common.FunctionExecutor;
import it.eng.spagobi.engines.datamining.common.utils.DataMiningConstants;
import it.eng.spagobi.functions.dao.ICatalogFunctionDAO;
import it.eng.spagobi.functions.metadata.SbiCatalogFunction;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

import java.io.File;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

@Path("/1.0/functions-catalog")
@ManageAuthorization
public class FunctionExecutionResource extends AbstractDataMiningEngineResource {

	public static transient Logger logger = Logger.getLogger(FunctionExecutionResource.class);

	@GET
	@Path("/execute/sample/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.FUNCTIONS_CATALOG_USAGE })
	public String executeSampleCatalogFunctionById(@PathParam("id") int id) {
		logger.debug("IN");
		String response;
		try {
			ICatalogFunctionDAO fcDAO = DAOFactory.getCatalogFunctionDAO();
			fcDAO.setUserProfile(getUserProfile());
			SbiCatalogFunction function = fcDAO.getCatalogFunctionById(id);

			if (function != null) {
				response = FunctionExecutor.execute(null, function, getUserProfile(), getEnv());
			} else {
				throw new SpagoBIEngineRuntimeException("Function with id [" + id + "] does not exist");
			}
		} catch (Exception e) {
			logger.error("Error creating or starting the Data Mining engine, or problems getting execution results!", e);
			throw new SpagoBIEngineRuntimeException("Error creating or starting the Data Mining engine, or problems getting execution results!", e);
		} finally {
			logger.debug("OUT");
		}
		return response;
	}

	@GET
	@Path("/execute/sample")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.FUNCTIONS_CATALOG_USAGE })
	public String executeSampleCatalogFunctionByLabel(@QueryParam("label") String label) {
		logger.debug("IN");
		String response;
		try {
			ICatalogFunctionDAO fcDAO = DAOFactory.getCatalogFunctionDAO();
			fcDAO.setUserProfile(getUserProfile());
			SbiCatalogFunction function = fcDAO.getCatalogFunctionByLabel(label);

			if (function != null) {
				response = FunctionExecutor.execute(null, function, getUserProfile(), getEnv());
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
	@Path("/execute/new/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.FUNCTIONS_CATALOG_USAGE })
	public String executeCatalogFunctionById(String body, @PathParam("id") int id) {
		logger.debug("IN");
		String response;
		try {
			ICatalogFunctionDAO fcDAO = DAOFactory.getCatalogFunctionDAO();
			fcDAO.setUserProfile(getUserProfile());
			SbiCatalogFunction function = fcDAO.getCatalogFunctionById(id);

			if (function != null) {
				response = FunctionExecutor.execute(body, function, getUserProfile(), getEnv());
			} else {
				throw new SpagoBIEngineRuntimeException("Function with Id [" + id + "] does not exist");
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
	@Path("/execute/new")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.FUNCTIONS_CATALOG_USAGE })
	public String executeCatalogFunctionByLabel(String body, @QueryParam("label") String label) {
		logger.debug("IN");
		String response;
		try {
			ICatalogFunctionDAO fcDAO = DAOFactory.getCatalogFunctionDAO();
			fcDAO.setUserProfile(getUserProfile());
			SbiCatalogFunction function = fcDAO.getCatalogFunctionByLabel(label);

			if (function != null) {
				response = FunctionExecutor.execute(body, function, getUserProfile(), getEnv());
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
	@Path("/remote/example")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.FUNCTIONS_CATALOG_USAGE })
	public String executeRemoteExample(String body) {
		logger.debug("IN");
		String response;
		try {
			// Do something cool with the image....
			// Something cool with the image done...

			// ... Joking, just building a fake response
			JSONArray jsonResponse = new JSONArray();
			JSONObject jsonResponseItem = new JSONObject();

			jsonResponseItem.put("resultType", "Image");
			String path = SpagoBIUtilities.getRootResourcePath() + File.separator + "miscellaneous" + File.separator
					+ DataMiningConstants.IMAGE_DEMO_LENA_FACEDETECT;
			jsonResponseItem.put("result", SpagoBIUtilities.getImageAsBase64(path, "jpeg"));
			jsonResponseItem.put("resultName", "plot");
			jsonResponse.put(jsonResponseItem);

			jsonResponseItem = new JSONObject();
			jsonResponseItem.put("resultType", "Text");
			jsonResponseItem.put("result", "Found one face. \n Overall face sentiment: neutral");
			jsonResponseItem.put("resultName", "numFaces");
			jsonResponse.put(jsonResponseItem);

			response = jsonResponse.toString();
		} catch (Exception e) {
			throw new SpagoBIEngineRuntimeException("Error creating or starting the Data Mining engine, or problems getting execution results!", e);
		} finally {
			logger.debug("OUT");
		}
		return response;
	}

}
