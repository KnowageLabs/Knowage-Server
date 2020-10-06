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

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.functions.dao.ICatalogFunctionDAO;
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

	// @formatter:off
	/**
	 * @api {get} /1.0/functions-catalog Request Functions
	 * @apiName GET_getAllCatalogFunctions
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
	 * This service can be called to obtain a complete list of functions registered in the catalog. In case the current user is not
	 * the owner of the function, the fields 'language', 'script' and 'url' will never be returned, but empty.
	 *
	 * @apiSuccess {json} functions The list of functions and keywords.
	 *
	 * @apiSuccessExample {json} Response-example:
		{
		   "functions":[
		      {
		         "id":16,
		         "name":"tst_average",
		         "description":"tst_average",
		         "language":"R",
		         "script":"x <- c($P{firstvar}, $P{secondvar}, $P{thirdvar})\nav <- mean(x)",
		         "owner":"test_admin",
		         "label":"tst_average",
		         "type":"Utilities",
		         "remote":"false",
		         "url":"",
		         "keywords":[
		            "average"
		         ],
		         "inputVariables":[
		            {
		               "name":"thirdvar",
		               "value":"-5"
		            },
		            {
		               "name":"secondvar",
		               "value":"11"
		            },
		            {
		               "name":"firstvar",
		               "value":"5"
		            }
		         ],
		         "inputDatasets":[

		         ],
		         "inputFiles":[
		         	{
		               "name":"sourcefile",
		               "value":
		               		{
						   	  	"filesize": "54836",
						      	"filetype": "image/jpeg",
						      	"filename": "sourcechart.jpg",
		   	  				}
		            }

		         ],
		         "outputItems":[
		            {
		               "type":"Text",
		               "label":"av"
		            },
		             {
		               "type":"File",
		               "label":"myfile"
		            }
		         ]
		      },
		      {
		         "id":17,
		         "name":"tst_average_ds",
		         "description":"tst_average_ds",
		         "language":"",
		         "script":"",
		         "owner":"test_dev",
		         "label":"tst_average_ds",
		         "type":"Utilities",
		         "remote":"true",
		         "url":"",
		         "keywords":[

		         ],
		         "inputVariables":[

		         ],
		         "inputDatasets":[
		            {
		               "label":"TST_FUNC_CAT_LP"
		            }
		         ],
		         "outputItems":[
		            {
		               "type":"Dataset",
		               "label":"dsoutput"
		            }
		         ]
		      }
		   ],
		   "keywords":[
		      "average"
		   ]
		}
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
		 * This service can be called to obtain a filtered list of functions registered in the catalog. The list is filtered by the type.
		 * In case the current user is not the owner of the function, the fields 'language', 'script' and 'url' will never be returned, but empty.
		 *
		 * @apiParam {String} type Function type.
		 *
		 * @apiSuccess {json} functions The list of functions and keywords with the specified type.
		 *
		 * @apiSuccessExample {json} Response-example:
			{
			   "functions":[
			      {
			         "id":16,
			         "name":"tst_average",
			         "description":"tst_average",
			         "language":"R",
			         "script":"x <- c($P{firstvar}, $P{secondvar}, $P{thirdvar})\nav <- mean(x)",
			         "owner":"test_admin",
			         "label":"tst_average",
			         "type":"Utilities",
			         "remote":"false",
		         	 "url":"",
			         "keywords":[
			            "average"
			         ],
			         "inputVariables":[
			            {
			               "name":"thirdvar",
			               "value":"-5"
			            },
			            {
			               "name":"secondvar",
			               "value":"11"
			            },
			            {
			               "name":"firstvar",
			               "value":"5"
			            }
			         ],
			         "inputDatasets":[

			         ],
			         "outputItems":[
			            {
			               "type":"Text",
			               "label":"av"
			            }
			         ]
			      },
			      {
			         "id":17,
			         "name":"tst_average_ds",
			         "description":"tst_average_ds",
			         "language":"",
			         "script":"",
			         "owner":"test_dev",
			         "label":"tst_average_ds",
			         "type":"Utilities",
			         "remote":"false",
		             "url":""
			         "keywords":[

			         ],
			         "inputVariables":[

			         ],
			         "inputDatasets":[
			            {
			               "label":"TST_FUNC_CAT_LP"
			            }
			         ],
			         "outputItems":[
			            {
			               "type":"Dataset",
			               "label":"dsoutput"
			            }
			         ]
			      }
			   ],
			   "keywords":[
			      "average"
			   ]
			}
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
		 * This service can be called to add a new function to the catalog. If 'remote' field is false, then script and language MUST
		 * be valorized since the function will be execute within the Knowage Datamining capabilities. Otherwise script and language cannot be provided
		 * and their value MUST be ignored.
		 *
		 * Also, if 'remote' field is true, the output fields still must be provided, even if the output is generated remotely.
		 * Those defined output are used to validate the response provided by the remote function. As instance, if a remote function has
		 * a dataset as output, then it MUST take care of save that dataset inside Knowage (through API) before returning the response.
		 * Knowage will validate the output by looking into its repository to see if that dataset has been added.
		 *
	 *
	 * @apiParam {json} function Function detail.
	 * @apiParamExample {json} Request-Example:
	 *
			{
			   "id":"",
			   "name":"tst_average_ds",
			   "description":"tst_average_ds",
			   "language":"R",
			   "script":"z<-TST_FUNC_CAT_LP\ndsoutput<-rbind(z,rowMeans(z, na.rm = TRUE))\nfieldoutput<-${field}\ndsoutput",
			   "owner":"test_admin",
			   "label":"tst_average_ds",
			   "type":"Utilities",
			   "remote":"false",
		       "url":"",
			   "keywords":[
			      "average",
			      "cool",
			      "math"
			   ],
			   "inputVariables":[
			      {
			         "name":"field",
			         "value":"5"
			      }
			   ],
			   "inputDatasets":[
			      {
			         "label":"TST_FUNC_CAT_LP"
			      }
			   ],
			   "outputItems":[
			      {
			         "type":"Dataset",
			         "label":"dsoutput"
			      },
			      {
			         "label":"fieldoutput",
			         "type":"Text"
			      }
			   ]
			}
	 *
	 * @apiSuccess {json} response The response of the update.
	 *
	 * @apiSuccessExample {json} Response-example:
			{
			   "id":17,
			   "name":"tst_average_ds",
			   "description":"tst_average_ds",
			   "language":"R",
			   "script":"z<-TST_FUNC_CAT_LP\ndsoutput<-rbind(z,rowMeans(z, na.rm = TRUE))\nfieldoutput<-${field}\ndsoutput",
			   "owner":"test_admin",
			   "label":"tst_average_ds",
			   "type":"Utilities",
			   "remote":"false",
		       "url":"",
			   "keywords":[
			      "average",
			      "cool",
			      "math"
			   ],
			   "inputVariables":[
			      {
			         "name":"field",
			         "value":"5"
			      }
			   ],
			   "inputDatasets":[
			      {
			         "label":"TST_FUNC_CAT_LP"
			      }
			   ],
			   "outputItems":[
			      {
			         "type":"Dataset",
			         "label":"dsoutput"
			      },
			      {
			         "label":"fieldoutput",
			         "type":"Text"
			      }
			   ]
			}
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
			String family = jsonObj.getString("functionFamily");
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

			Map<String, String> inputVariables = new HashMap<String, String>();
			Map<String, String> inputColumns = new HashMap<String, String>();
			Map<String, String> outputColumns = new HashMap<String, String>();

			for (int i = 0; i < jsonInputColumns.length(); i++) {

				JSONObject inputItemJSON = jsonInputColumns.getJSONObject(i);
				String colName = inputItemJSON.getString("name");
				String colType = inputItemJSON.getString("type");
				inputColumns.put(colName, colType);
			}

			for (int i = 0; i < jsonInputVariables.length(); i++) {

				JSONObject inputItemJSON = jsonInputVariables.getJSONObject(i);
				String varName = inputItemJSON.getString("name");
				String varValue = inputItemJSON.getString("value");
				inputVariables.put(varName, varValue);
			}

			for (int i = 0; i < jsonOutputColumns.length(); i++) {

				JSONObject outputItemJSON = jsonOutputColumns.getJSONObject(i);
				String colName = outputItemJSON.getString("name");
				String colType = outputItemJSON.getString("type");
				outputColumns.put(colName, colType);
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

	// @formatter:off
		/**
		 * @api {put} /1.0/functions-catalog/update/:id Update Function
		 * @apiName PUT_updateCatalogFunction
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
		 * This service can be called to update an existing function to the catalog. If 'remote' field is false, then script and language MUST
		 * be valorized since the function will be execute within the Knowage Datamining capabilities. Otherwise script and language cannot be provided
		 * and their value MUST be ignored.
		 *
		 * Also, if 'remote' field is true, the output fields still must be provided, even if the output is generated remotely.
		 * Those defined outputs are used to validate the response provided by the remote function. As instance, if a remote function has
		 * a dataset as output, then it MUST take care of save that dataset inside Knowage (through API) before returning the response.
		 * Knowage will validate the output by looking into its repository to see if that dataset has been added.
		 *
		 *
		 * @apiParam {String} id Function id.
		 *
		 * @apiParam {json} function Function detail.
		 * @apiParamExample {json} Request-Example:
		 *
				{
				   "id":17,
				   "name":"tst_average_ds",
				   "description":"tst_average_ds",
				   "language":"R",
				   "script":"z<-TST_FUNC_CAT_LP\ndsoutput<-rbind(z,rowMeans(z, na.rm = TRUE))\nfieldoutput<-${field}\ndsoutput",
				   "owner":"test_admin",
				   "label":"tst_average_ds",
				   "type":"Utilities",
				   "remote":"false",
		           "url":"",
				   "keywords":[
				      "average",
				      "cool",
				      "math"
				   ],
				   "inputVariables":[
				      {
				         "name":"field",
				         "value":"5"
				      }
				   ],
				   "inputDatasets":[
				      {
				         "label":"TST_FUNC_CAT_LP"
				      }
				   ],
				   "outputItems":[
				      {
				         "type":"Dataset",
				         "label":"dsoutput"
				      },
				      {
				         "label":"fieldoutput",
				         "type":"Text"
				      }
				   ]
				}
		 *
		 * @apiSuccess {json} response The response of the update.
		 *
		 * @apiSuccessExample {json} Response-example:
			{
				"Response":"OK"
			}

		* @apiErrorExample {json} Error-Response example:
		 *    {
		 *    	"service":"",
		 *    	"errors":[
		 *    		{"message":"Here the message."}
		 *    	]
		 *    }
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
			String benchmarks = jsonObj.getString("benchmarks");
			String language = jsonObj.getString("language");
			String family = jsonObj.getString("functionFamily");
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
			Map<String, String> outputColumns = new HashMap<String, String>();
			Map<String, String> inputVariables = new HashMap<String, String>();

			for (int i = 0; i < jsonInputColumns.length(); i++) {
				JSONObject inputColumnJSON = jsonInputColumns.getJSONObject(i);
				String colName = inputColumnJSON.getString("name");
				String colType = inputColumnJSON.getString("type");
				inputColumns.put(colName, colType);
			}

			for (int i = 0; i < jsonInputVariables.length(); i++) {
				JSONObject inputItemJSON = jsonInputVariables.getJSONObject(i);
				String varName = inputItemJSON.getString("name");
				String varValue = inputItemJSON.getString("value");
				inputVariables.put(varName, varValue);
			}

			for (int i = 0; i < jsonOutputColumns.length(); i++) {
				JSONObject outputColumnJSON = jsonOutputColumns.getJSONObject(i);
				String colName = outputColumnJSON.getString("name");
				String colType = outputColumnJSON.getString("type");
				inputColumns.put(colName, colType);
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

	// @formatter:off
	/**
	 * @api {get} /1.0/functions-catalog/delete/:id Delete Function by ID
	 * @apiName GET_deleteCatalogFunction
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
		 * This service can be called to delete an existing function to the catalog.
		 *
	 *
	 * @apiParam {String} id Function id.
	 *
	 * @apiSuccess {json} response The response of the update.
	 *
	 * @apiSuccessExample {json} Response-example:
		{
			"Response":"OK"
		}
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

	// @formatter:off
		/**
		 * @api {get} /1.0/function/execute/sample/:id Execute function by ID with sample data
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

	// @formatter:off
			/**
			 * @api {get} /1.0/function/execute/sample?label=:label Execute function by label with sample data
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
			 * Please refer to the service GET /1.0/function/execute/sample/:id to get more information about the usage.
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

	// @formatter:off
			/**
			 * @api {POST} /1.0/function/execute/new/:id Execute function by ID with provided data
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

	// @formatter:off
				/**
				 * @api {POST} /1.0/function/execute/new?label=:label Execute function by label with provided data
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
				 * Please refer to the service GET /1.0/function/execute/new/:id to get more information about the usage.
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
		String knowageContext = GeneralUtilities.getSpagoBiContext();
		String resourceUri = request.getRequestURI().replaceFirst(knowageContext, knowageContext + DATA_MINING_ENGINE_SUFFIX);
		String queryParams = request.getQueryString();

		StringBuilder sb = new StringBuilder();
		sb.append(GeneralUtilities.getSpagoBiHost());
		sb.append(resourceUri);
		if (queryParams != null) {
			sb.append("?");
			sb.append(queryParams);
		}

		return sb.toString();
	}
}
