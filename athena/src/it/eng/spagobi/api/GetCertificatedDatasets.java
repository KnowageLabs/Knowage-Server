/*
 * SpagoBI, the Open Source Business Intelligence suite
 * © 2005-2015 Engineering Group
 *
 * This file is part of SpagoBI. SpagoBI is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 2.1 of the License, or any later version. 
 * SpagoBI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should have received
 * a copy of the GNU Lesser General Public License along with SpagoBI. If not, see: http://www.gnu.org/licenses/.
 * The complete text of SpagoBI license is included in the COPYING.LESSER file. 
 */
package it.eng.spagobi.api;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.execution.service.ExecuteAdHocUtility;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.filters.FilterIOManager;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 * 
 * @deprecated Use specific services exposed by DataSetResource
 * 
 */
@Path("/certificateddatasets")
public class GetCertificatedDatasets {

	static private Logger logger = Logger.getLogger(GetCertificatedDatasets.class);

	@GET
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getAllDataSet(@Context HttpServletRequest request) {
		IDataSetDAO dataSetDao = null;
		List<IDataSet> dataSets;
		
		FilterIOManager ioManager = new FilterIOManager(request, null);
		ioManager.initConetxtManager();	
		IEngUserProfile profile = (IEngUserProfile)ioManager.getFromSession(IEngUserProfile.ENG_USER_PROFILE);		
		
		JSONObject JSONReturn = new JSONObject();
		JSONArray datasetsJSONArray = new JSONArray();
		try {
			dataSetDao = DAOFactory.getDataSetDAO();
			dataSetDao.setUserProfile(profile);
			
			String isTech = request.getParameter("isTech");
			String allMyDataDS = request.getParameter("allMyDataDs");
			String typeDocWizard = (request.getParameter("typeDoc") != null && !"null".equals(request.getParameter("typeDoc")))?request.getParameter("typeDoc"):null;


			if(isTech != null && isTech.equals("true")){
				//if is technical dataset == ENTERPRISE --> get all ADMIN/DEV public datasets
				dataSets = dataSetDao.loadEnterpriseDataSets();
			} else if (allMyDataDS != null && allMyDataDS.equals("true")){
				//get all the Datasets visible for the current user (MyData,Enterprise,Shared Datasets) 
				dataSets = dataSetDao.loadMyDataDataSets(profile.getUserUniqueIdentifier().toString());
			} else {
				//else it is a custom dataset list --> get all datasets public with owner != user itself
				dataSets = dataSetDao.loadDatasetsSharedWithUser(profile.getUserUniqueIdentifier().toString());
			}

			datasetsJSONArray = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(dataSets, null);
			
			JSONArray datasetsJSONReturn = putActions(profile, datasetsJSONArray, typeDocWizard);

			JSONReturn.put("root", datasetsJSONReturn);

		} catch (Throwable t) {
			throw new SpagoBIServiceException("An unexpected error occured while instatiating the dao", t);
		}
		return JSONReturn.toString();
	}
	
	private JSONArray putActions(IEngUserProfile profile, JSONArray datasetsJSONArray, String typeDocWizard)
			throws JSONException, EMFInternalError {
		
		Engine wsEngine = null;
		try{
			wsEngine = ExecuteAdHocUtility.getWorksheetEngine() ;
		}catch(SpagoBIRuntimeException r){
			//the ws engine is not found
			logger.info("Engine not found. ", r);
		}
		
		Engine qbeEngine = null;
		try{
			qbeEngine = ExecuteAdHocUtility.getQbeEngine() ;
		}catch(SpagoBIRuntimeException r){
			//the qbe engine is not found
			logger.info("Engine not found. ", r);
		}
		
		Engine geoEngine = null;
		try{
			geoEngine = ExecuteAdHocUtility.getGeoreportEngine() ;
		}catch(SpagoBIRuntimeException r){
			//the geo engine is not found
			logger.info("Engine not found. ", r);
		}
		JSONObject detailAction = new JSONObject();
		detailAction.put("name", "detaildataset");
		detailAction.put("description", "Dataset detail");	
		
		JSONObject deleteAction = new JSONObject();
		deleteAction.put("name", "delete");
		deleteAction.put("description", "Delete dataset");		
		
		JSONObject worksheetAction = new JSONObject();
		worksheetAction.put("name", "worksheet");
		worksheetAction.put("description", "Show Worksheet");
		
		JSONObject georeportAction = new JSONObject();
		georeportAction.put("name", "georeport");
		georeportAction.put("description", "Show Map");
		
		JSONObject qbeAction = new JSONObject();
		qbeAction.put("name", "qbe");
		qbeAction.put("description", "Show Qbe");
		
		JSONArray datasetsJSONReturn = new JSONArray();	
		for(int i = 0; i < datasetsJSONArray.length(); i++) {
			JSONArray actions = new JSONArray();
			JSONObject datasetJSON = datasetsJSONArray.getJSONObject(i);
			
			if (typeDocWizard == null){
				actions.put(detailAction);						
				if (profile.getUserUniqueIdentifier().toString().equals(datasetJSON.get("owner"))){
					//the delete action is able only for private dataset
					actions.put(deleteAction);
				}
			}
			
			boolean isGeoDataset = false;
			try{
				String meta = datasetJSON.getString("meta");
				isGeoDataset = ExecuteAdHocUtility.hasGeoHierarchy(meta);				
			} catch(Exception e) {
				logger.error("Error during ceck of Geo spatial column", e);
			}
			if (isGeoDataset && geoEngine != null && typeDocWizard != null && 
					typeDocWizard.equalsIgnoreCase("GEO")){
				actions.put(georeportAction); //enable the icon to CREATE a new geo document
			}else{
				if (isGeoDataset && geoEngine != null){
//				if (isGeoDataset && geoEngine != null && 
//						profile.getUserUniqueIdentifier().toString().equals(datasetJSON.get("owner"))){
					actions.put(georeportAction); // Annotated view map action to release SpagoBI 4
				}
			}
			if (wsEngine != null && typeDocWizard == null || typeDocWizard.equalsIgnoreCase("REPORT")){
				actions.put(worksheetAction);			
		
				if (qbeEngine != null && profile.getFunctionalities().contains(SpagoBIConstants.BUILD_QBE_QUERIES_FUNCTIONALITY)){
					actions.put(qbeAction);
				}
			}
			
			datasetJSON.put("actions", actions);
			if (typeDocWizard != null && typeDocWizard.equalsIgnoreCase("GEO")){
				//if is caming from myAnalysis - create Geo Document - must shows only ds geospatial --> isGeoDataset == true
				if (geoEngine != null  && isGeoDataset)
					datasetsJSONReturn.put(datasetJSON);
			}else
				datasetsJSONReturn.put(datasetJSON);
		}
		return datasetsJSONReturn;
	}

	@GET
	@Path("/getflatdataset")
	@Produces(MediaType.APPLICATION_JSON)
	public String getFlatDataSet(@Context HttpServletRequest req) {
		IDataSetDAO dataSetDao = null;
		List<IDataSet> dataSets;
		IEngUserProfile profile = (IEngUserProfile) req.getSession()
				.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		JSONObject JSONReturn = new JSONObject();
		JSONArray datasetsJSONArray = new JSONArray();
		try {
			dataSetDao = DAOFactory.getDataSetDAO();
			dataSetDao.setUserProfile(profile);
			dataSets = dataSetDao.loadFlatDatasets();
			//dataSets = dataSetDao.loadFlatDatasets(profile.getUserUniqueIdentifier().toString());

			datasetsJSONArray = (JSONArray) SerializerFactory.getSerializer(
					"application/json").serialize(dataSets, null);
			
			JSONArray datasetsJSONReturn = putActions(profile, datasetsJSONArray, null);

			JSONReturn.put("root", datasetsJSONReturn);

		} catch (Throwable t) {
			throw new SpagoBIServiceException(
					"An unexpected error occured while instatiating the dao", t);
		}
		return JSONReturn.toString();

	}
}
