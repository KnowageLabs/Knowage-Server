package it.eng.spagobi.engines.datamining.api;

import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.engines.datamining.common.AbstractDataMiningEngineResource;
import it.eng.spagobi.engines.datamining.common.FunctionExecutor;
import it.eng.spagobi.functions.dao.ICatalogFunctionDAO;
import it.eng.spagobi.functions.metadata.SbiCatalogFunction;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

@Path("/1.0/function")
@ManageAuthorization
public class FunctionResource extends AbstractDataMiningEngineResource {

	public static transient Logger logger = Logger.getLogger(FunctionResource.class);

	// @formatter:off
	/**
	 * @api {get} /1.0/function/execute-sample/:id Execute function by ID with sample data
	 * @apiName GET_executeSampleCatalogFunctionById
	 * @apiGroup Functions
	 *
	 * @apiVersion 0.1.0
	 *
	 * @apiDescription
	 * -- AUTHENTICATION
	 *
	 * All the Knowage RESTful services are based on Basic Authentication. Please generate your request in accordance with this
	 * requirement.
	 *
	 *
	 * -- DESCRIPTION
	 *
	 * This service can be used to execute a function (local or remote) specifying its identifier.
	 * The identifier (which is less friendly compared to label) can be obtained by querying the function catalog through its APIs.
	 *
	 * When calling this service, it is not request to provide any data. The function will use the embedded mandatory sample data.
	 * Those sample data have been provided when the functions has been added to the catalog.
	 *
	 * The response contains an array of JSONObject with three fields: 'resultType', 'result' and 'resultName'.
	 *
	 * The first field contains one of the following values: 'Image', 'Text', 'Dataset' or 'File'.
	 *
	 * In case of 'Image' and 'File', the result is provided with Base64 encoding.
	 *
	 * Also, 'File' has a special result structure (see example below). Please notice that 'filesize' and 'filetype' are optional fields.
	 * Valid values for 'filetype' are listed here: http://www.iana.org/assignments/media-types/
	 *
	 *
	 * -- IMPORTANT --
	 *
	 * Due to its nature, this service can benefit of data compression for both request and response. Because of this, the payload can be compressed
	 * when required (mostly when files and images are involved). This means that a request or a response with one or more 'File' SHOULD be sent already compressed
	 * with a header set to 'Content-Encoding': 'gzip'.
	 *
	 * In both direction this aspect must be handled by reading the request header accordingly.
	 *
	 * @apiParam {Number} id Function ID.
	 *
	 * @apiSuccess {json} response The list of functions and keywords with the specified type.
	 *
	 * @apiSuccessExample {json} Response-example:
		[
		   {
		      "resultType":"Image",
		      "result":"iVBORw0KGgoAAAANSUhEUgAAAyAA....BiJ1pQ89NBDakQohBASIEIIIYqG1CaKuIkY+OlY6623QmCC",
		      "resultName":"valuesPlot"
		   },
		   {
		      "resultType":"Text",
		      "result":"120",
		      "resultName":"maximimValue"
		   },
		   {
		   	  "resultType":"File",
		   	  "result":
		   	  {
		   	  	"filesize": "54836",
		      	"filetype": "image/jpeg",
		      	"filename": "chart.jpg",
		      	"base64":   "/9j/4AAQSkZJRgABAgAAAQAB....AAD//gAEKgD/4gIctcwIQA..."
		   	  },
		      "resultName":"fileToBeSave"
		   }
		]
	* @apiErrorExample {json} Error-Response example:
			 *    {
			 *    	"service":"",
			 *    	"errors":[
			 *    		{"message":"Here the error message."}
			 *    	]
			 *    }
		*/
	// @formatter:on

	@GET
	@Path("/execute-sample/{id}")
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

	// @formatter:off
		/**
		 * @api {get} /1.0/function/execute-sample?label=:label Execute function by label with sample data
		 * @apiName GET_executeSampleCatalogFunctionByLabel
		 * @apiGroup Functions
		 *
		 * @apiVersion 0.1.0
		 *
		 * @apiDescription
		 * -- AUTHENTICATION
		 * All the Knowage RESTful services are based on Basic Authentication. Please generate your request in accordance with this
		 * requirement.
		 *
		 *
		 * -- DESCRIPTION
		 * This service can be used to execute a function (local or remote) specifying its label.
		 * The label is usually known, but can be obtained by querying the function catalog through its GUI or APIs.
		 *
		 * Please refer to the service GET /1.0/function/execute-sample/:id to get more information about the usage.
		 *
		 * @apiParam {String} label Function label.
		 *
		 * @apiSuccess {json} response The list of functions and keywords with the specified type.
		 *
		 * @apiSuccessExample {json} Response-example:
			[
			   {
			      "resultType":"Image",
			      "result":"iVBORw0KGgoAAAANSUhEUgAAAyAA....BiJ1pQ89NBDakQohBASIEIIIYqG1CaKuIkY+OlY6623QmCC",
			      "resultName":"valuesPlot"
			   },
			   {
			      "resultType":"Text",
			      "result":"120",
			      "resultName":"maximimValue"
			   }
			]
		* @apiErrorExample {json} Error-Response example:
			 *    {
			 *    	"service":"",
			 *    	"errors":[
			 *    		{"message":"Here the error message."}
			 *    	]
			 *    }
		*/
	// @formatter:on

	@GET
	@Path("/execute-sample")
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

	// @formatter:off
		/**
		 * @api {POST} /1.0/function/execute/:id Execute function by ID with provided data
		 * @apiName POST_executeCatalogFunctionById
		 * @apiGroup Functions
		 *
		 * @apiVersion 0.1.0
		 *
		 * @apiDescription
		 * -- AUTHENTICATION
		 *
		 * All the Knowage RESTful services are based on Basic Authentication. Please generate your request in accordance with this
		 * requirement.
		 *
		 *
		 * -- DESCRIPTION
		 *
		 * This service can be used to execute a function (local or remote) specifying its identifier and providing specific data.
		 * The identifier (which is less friendly compared to label) can be obtained by querying the function catalog through its APIs.
		 *
		 * The request contains an array of three JSONObject, which defines input type and provided data.
		 * When calling this service, the caller MUST provide all the data. Optional fields are not allowed.
		 * Please pay attention to the special structure for 'filesIn' inputs. Like inside the response file structure, 'filesize' and 'filetype' are useful but optional.
		 *
		 * The response contains an array of JSONObject with three fields: 'resultType', 'result' and 'resultName'.
		 *
		 * The first field contains one of the following values: 'Image', 'Text', 'Dataset' or 'File'.
		 *
		 * In case of 'Image' and 'File', the result is provided with Base64 encoding.
		 *
		 * Also, 'File' has a special result structure (see example below). Please notice that 'filesize' and 'filetype' are optional fields.
		 * Valid values for 'filetype' are listed here: http://www.iana.org/assignments/media-types/
		 *
		 *
		 * -- IMPORTANT --
		 *
		 * Due to its nature, this service can benefit of data compression for both request and response. Because of this, the payload can be compressed
		 * when required (mostly when files and images are involved). This means that a request or a response with one or more 'File' SHOULD be sent already compressed
		 * with a header set to 'Content-Encoding': 'gzip'.
		 *
		 * In both direction this aspect must be handled by reading the request header accordingly.
		 *
		 * @apiParam {Number} id Function id.
		 * @apiParam {json} function Function detail.
		 * @apiParamExample {json} Request-Example:
		 *
				[
				   {
				      "type":"variablesIn",
				      "items":{
				         "a":"3",
				         "b":"3"
				      }
				   },
				   {
				      "type":"datasetsIn",
				      "items":{
				         "df":"df2"
				      }
				   },
				   {
				      "type":"filesIn",
				      "items":{
				         "df": {
				         	"filename":"traffic.avi",
				         	"base64":"/9j/4AAQSkZJRgABAgAAAQAB....AAD//gAEKgD/4gIctcwIQA..."
				         }
				      }
				   }
				]
			 *
			 * @apiSuccess {json} response The results from the execute function.
			 *
			 * @apiSuccessExample {json} Response-example:
				[
				   {
				      "resultType":"Image",
				      "result":"iVBORw0KGgoAAAANSUhEUgAAA...==NSUhEUgtfgf",
				      "resultName":"res"
				   },
				   {
				      "resultType":"Dataset",
				      "result":"biadmin_function_catalog_datasetOutNEW",
				      "resultName":"datasetOut"
				   }
				]
			* @apiErrorExample {json} Error-Response example:
			 *    {
			 *    	"service":"",
			 *    	"errors":[
			 *    		{"message":"Here the error message."}
			 *    	]
			 *    }
		*/
	// @formatter:on

	@POST
	@Path("/execute/{id}")
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

	// @formatter:off
			/**
			 * @api {POST} /1.0/function/execute?label=:label Execute function by label with provided data
			 * @apiName POST_executeCatalogFunctionByLabel
			 * @apiGroup Functions
			 *
			 * @apiVersion 0.1.0
			 *
			 * @apiDescription
			 * -- AUTHENTICATION
			 * All the Knowage RESTful services are based on Basic Authentication. Please generate your request in accordance with this
			 * requirement.
			 *
			 *
			 * -- DESCRIPTION
			 * This service can be used to execute a function (local or remote) specifying its label and providing specific data.
			 * The label is usually known, but can be obtained by querying the function catalog through its GUI or APIs.
			 *
			 * Please refer to the service GET /1.0/function/execute/:id to get more information about the usage.
			 *
			 * @apiParam {String} label Function label.
			 * @apiParam {json} function Function detail.
			 * @apiParamExample {json} Request-Example:
			 *
				[
				   {
				      "type":"variablesIn",
				      "items":{
				         "a":"3",
				         "b":"3"
				      }
				   },
				   {
				      "type":"datasetsIn",
				      "items":{
				         "df":"df2"
				      }
				   }
				]
			 *
			 * @apiSuccess {json} response The results from the execute function.
			 *
			 * @apiSuccessExample {json} Response-example:
				[
				   {
				      "resultType":"Image",
				      "result":"iVBORw0KGgoAAAANSUhEUgAAA...==NSUhEUgtfgf",
				      "resultName":"res"
				   },
				   {
				      "resultType":"Dataset",
				      "result":"biadmin_function_catalog_datasetOutNEW",
				      "resultName":"datasetOut"
				   }
				]
			* @apiErrorExample {json} Error-Response example:
			 *    {
			 *    	"service":"",
			 *    	"errors":[
			 *    		{"message":"Here the error message."}
			 *    	]
			 *    }
		*/
	// @formatter:on

	@POST
	@Path("/execute")
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

}
