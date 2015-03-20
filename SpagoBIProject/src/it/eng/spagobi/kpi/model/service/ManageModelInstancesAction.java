/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.model.service;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.kpi.config.bo.Kpi;
import it.eng.spagobi.kpi.config.bo.KpiInstance;
import it.eng.spagobi.kpi.config.dao.IKpiInstanceDAO;
import it.eng.spagobi.kpi.model.bo.Model;
import it.eng.spagobi.kpi.model.bo.ModelInstance;
import it.eng.spagobi.kpi.model.bo.ModelResources;
import it.eng.spagobi.kpi.model.bo.ModelResourcesExtended;
import it.eng.spagobi.kpi.model.bo.Resource;
import it.eng.spagobi.kpi.model.dao.IModelInstanceDAO;
import it.eng.spagobi.kpi.model.dao.IModelResourceDAO;
import it.eng.spagobi.kpi.threshold.bo.Threshold;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ManageModelInstancesAction extends AbstractSpagoBIAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8920524215721282986L;
	// logger component
	private static Logger logger = Logger.getLogger(ManageModelInstancesAction.class);
	private final String MESSAGE_DET = "MESSAGE_DET";
	// type of service
	private final String MODELINSTS_LIST = "MODELINSTS_LIST";
	private final String MODELINST_RESOURCE_LIST = "MODELINST_RESOURCE_LIST";
	private final String MODELINSTS_NODE_DETAILS = "MODELINSTS_NODE_DETAILS";
	private final String MODELINST_RESOURCE_SAVE = "MODELINST_RESOURCE_SAVE";
	private final String MODELINSTS_NODES_LIST = "MODELINSTS_NODES_LIST";
	private final String MODELINSTS_NODES_SAVE = "MODELINSTS_NODES_SAVE";
	private final String MODELINSTS_NODE_DELETE = "MODELINSTS_NODE_DELETE";
	private final String MODELINSTS_KPI_RESTORE = "MODELINSTS_KPI_RESTORE";

	private final String MODELINSTS_COPY_MODEL = "MODELINSTS_COPY_MODEL";
	private final String MODELINSTS_SAVE_ROOT = "MODELINSTS_SAVE_ROOT";

	private final String MODEL_DOMAIN_TYPE_ROOT = "MODEL_ROOT";
	private final String MODEL_DOMAIN_TYPE_NODE = "MODEL_NODE";

	private final String THRESHOLD_DOMAIN_TYPE = "THRESHOLD_TYPE";
	private final String KPI_CHART_TYPE = "KPI_CHART";

	public static String START = "start";
	public static String LIMIT = "limit";
	public static Integer START_DEFAULT = 0;
	public static Integer LIMIT_DEFAULT = 16;

	private final String NODES_TO_SAVE = "nodes";
	private final String DROPPED_NODES_TO_SAVE = "droppedNodes";
	private final String ROOT_TO_SAVE = "rootNode";

	IModelInstanceDAO modelDao;
	IModelResourceDAO modelResourcesDao ;
	
	@Override
	public void doService() {
		logger.debug("IN");

		try {
			modelDao = DAOFactory.getModelInstanceDAO();
			modelResourcesDao = DAOFactory.getModelResourcesDAO();
			modelDao.setUserProfile(getUserProfile());
			modelResourcesDao.setUserProfile(getUserProfile());
			
		} catch (EMFUserError e1) {
			logger.error(e1.getMessage(), e1);
			throw new SpagoBIServiceException(SERVICE_NAME,	"Error occurred");
		}
		Locale locale = getLocale();

		String serviceType = this.getAttributeAsString(MESSAGE_DET);
		logger.debug("Service type "+serviceType);

		if (serviceType != null && serviceType.equalsIgnoreCase(MODELINSTS_LIST)) {

			try {				
				List modelRootsList = modelDao.loadModelsInstanceRoot();

				logger.debug("Loaded models list");
				JSONArray modelsListJSON = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(modelRootsList,locale);
				JSONObject modelsResponseJSON = createJSONResponseModelsList(modelsListJSON,modelRootsList.size());

				writeBackToClient(new JSONSuccess(modelsResponseJSON));

			} catch (Throwable e) {
				logger.error("Exception occurred while retrieving model tree", e);
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Exception occurred while retrieving model tree", e);
			}
		}else if (serviceType != null && serviceType.equalsIgnoreCase(MODELINSTS_NODES_LIST)) {

			try {	

				String parentId = (String)getAttributeAsString("modelInstId");
				if(parentId == null || parentId.startsWith("xnode")){
					writeBackToClient(new JSONSuccess("'OK'"));
					return;
				} 
				ModelInstance aModel = modelDao.loadModelInstanceWithChildrenById(Integer.parseInt(parentId));

				logger.debug("Loaded model tree");
				JSONArray modelChildrenJSON = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(aModel.getChildrenNodes(),	locale);
				writeBackToClient(new JSONSuccess(modelChildrenJSON));

			} catch (Throwable e) {
				logger.error("Exception occurred while retrieving model tree", e);
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Exception occurred while retrieving model tree", e);
			}
		}else if (serviceType != null && serviceType.equalsIgnoreCase(MODELINSTS_NODE_DETAILS)) {

			try {	

				String node = (String)getAttributeAsString("modelInstId");

				ModelInstance aModel = modelDao.loadModelInstanceWithChildrenById(Integer.parseInt(node));

				logger.debug("Loaded model tree");
				JSONObject modelChildrenJSON = (JSONObject) SerializerFactory.getSerializer("application/json").serialize(aModel, locale);
				writeBackToClient(new JSONSuccess(modelChildrenJSON));

			} catch (Throwable e) {
				logger.error("Exception occurred while retrieving model tree", e);
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Exception occurred while retrieving model tree", e);
			}
		} else if (serviceType != null	&& serviceType.equalsIgnoreCase(MODELINSTS_NODES_SAVE)) {

			JSONArray nodesToSaveJSON = getAttributeAsJSONArray(NODES_TO_SAVE);
			JSONArray droppedNodesToSaveJSON = getAttributeAsJSONArray(DROPPED_NODES_TO_SAVE);
			JSONObject rootObj = getAttributeAsJSONObject(ROOT_TO_SAVE);

			List<ModelInstance> modelNodes = null;
			List<ModelInstance> modelNodesDD = null;
			ModelInstance root = null;
			Vector idsToRemove = new Vector();
			if(nodesToSaveJSON != null || droppedNodesToSaveJSON != null){
				JSONObject response = new JSONObject();

				try {
					modelNodesDD = deserializeNodesJSONArrayDD(droppedNodesToSaveJSON);

					//clean nodes modified from DD ones
					for(int i=0; i<modelNodesDD.size(); i++){
						ModelInstance mi = modelNodesDD.get(i);
						String guidToSkip = mi.getGuiId();
						//if already present in modified nodes...
						for(int k =0; k< nodesToSaveJSON.length(); k++){
							JSONObject objMod = nodesToSaveJSON.getJSONObject(k);
							String guiId = "";
							try{
								guiId = objMod.getString("id");
							}catch(Throwable t){
								logger.debug("Dropped node guiid doesn't exist"); 
							}
							if(guiId.equals(guidToSkip)){
								idsToRemove.add(guidToSkip);
							}
						}
						
					}
					modelNodes = deserializeJSONArray(nodesToSaveJSON, idsToRemove);


					//save DD nodes
					if(rootObj != null){						
						root = deserializeJSONObjectDD(rootObj, new ArrayList<ModelInstance>());	
						modelNodesDD.add(root);
					}
					if(modelNodesDD != null && !modelNodesDD.isEmpty()){
						if(!isTreeStructureOfDDNodes(droppedNodesToSaveJSON)){
							response = saveModelNodeInstances(modelNodesDD);
						}else{
							response = recursiveStart(modelNodesDD, root, response);
						}
						
					}
					try{
						if( rootObj.getBoolean("toSave")){
							//root node has been modified
							modelNodes.add(root);
						}
					}catch(Throwable e){
						logger.debug("Root node is not modified");
					}
					response = saveModelNodeInstances(modelNodes);
					writeBackToClient(new JSONSuccess(response));

				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					try {
						writeBackToClient(new JSONSuccess(response));
					} catch (IOException e1) {
						logger.error("Exception occurred while sending response", e);
						throw new SpagoBIServiceException(SERVICE_NAME,
								"Exception occurred while sending response", e);
					}
					throw new SpagoBIServiceException(SERVICE_NAME,
							"Exception saving model instance nodes", e);
				}

			}

		} else if (serviceType != null	&& serviceType.equalsIgnoreCase(MODELINSTS_NODE_DELETE)) {

			Integer modelInstId = getAttributeAsInteger("modelInstId");
			try {
				boolean result = modelDao.deleteModelInstance(modelInstId);
				logger.debug("Model instance node deleted");
				writeBackToClient( new JSONSuccess("'Operation succeded'") );
			} catch (Throwable e) {
				logger.error("Exception occurred while retrieving model instance to delete", e);
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Exception occurred while retrieving model instance to delete", e);
			}


		}else if (serviceType != null	&& serviceType.equalsIgnoreCase(MODELINSTS_KPI_RESTORE)) {

			Integer kpiId = getAttributeAsInteger("kpiId");
			try {
				Kpi kpiToRestore = DAOFactory.getKpiDAO().loadKpiById(kpiId);

				logger.debug("Found kpi to restore");
				JSONObject kpiJson = (JSONObject) SerializerFactory.getSerializer("application/json").serialize(kpiToRestore,	locale);
				writeBackToClient(new JSONSuccess(kpiJson));
			} catch (Throwable e) {
				logger.error("Exception occurred while retrieving kpi to restore", e);
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Exception occurred while retrieving kpi to restore", e);
			}


		}else if (serviceType != null	&& serviceType.equalsIgnoreCase(MODELINST_RESOURCE_LIST)) {

			try {
				Integer modelInstId = null;
				try{
					modelInstId = getAttributeAsInteger("modelInstId");
				}catch (Exception e) {
					// TODO: handle exception
					logger.debug("No model Instance Id");
				}
				Integer start = getAttributeAsInteger( START );
				Integer limit = getAttributeAsInteger( LIMIT );

				if(start==null){
					start = START_DEFAULT;
				}
				if(limit==null){
					limit = LIMIT_DEFAULT;
				}
				List<ModelResourcesExtended> modelResourcesExtenList = new ArrayList<ModelResourcesExtended>();
				//extract resources
				List<ModelResources> modelResources = new ArrayList<ModelResources>();
				if(modelInstId != null){
					modelResources = modelResourcesDao.loadModelResourceByModelId(modelInstId);
				}
				HashMap<Integer, ModelResources> modResourcesIds = new HashMap<Integer, ModelResources>();
				if(modelResources != null){
					for(int i =0;i<modelResources.size(); i++){
						ModelResources mr = modelResources.get(i);
						modResourcesIds.put(mr.getResourceId(), mr);
					}
				}
				//extract all resources
				Vector resourcesIds = new Vector<Integer>();

				List<Resource> allResources = (List<Resource>)getSessionContainer().getAttribute("ALL_RESOURCES_LIST");

				//if null than extract
				if(allResources == null){
					allResources = DAOFactory.getResourceDAO().loadPagedResourcesList(start,limit);
				}
				modelResourcesExtendedListCreate(modelResourcesExtenList, allResources, modResourcesIds);

				logger.debug("Loaded model resources");
				JSONArray modelsResourcesJSON = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(modelResourcesExtenList,locale);
				JSONObject modelsResourcesResponseJSON = createJSONResponsemodelsResourcesList(modelsResourcesJSON, modelResourcesExtenList.size());

				writeBackToClient(new JSONSuccess(modelsResourcesResponseJSON));


			} catch (Throwable e) {
				logger.error("Exception occurred while retrieving model tree", e);
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Exception occurred while retrieving model tree", e);
			}

		}else if(serviceType != null	&& serviceType.equalsIgnoreCase(MODELINST_RESOURCE_SAVE)){
			JSONArray resToSaveJSON = getAttributeAsJSONArray("ids");
			Integer modelId = getAttributeAsInteger("modelInstId");

			try {

				List ids = deserializeResourceJSONArray(resToSaveJSON);
				List toAddIds = ids;
				//loops over all model resources
				List<ModelResources> mrs = modelResourcesDao.loadModelResourceByModelId(modelId);
				for(int i=0; i< mrs.size(); i++){
					ModelResources modelres = mrs.get(i);
					if(!ids.contains(modelres.getResourceId())){
						//to remove
						modelResourcesDao.removeModelResource(modelId, modelres.getResourceId());
					}else {
						//already present so remove it from the list
						toAddIds.remove(modelres.getResourceId());
					}


				}
				//now adds new ones					
				for(int i=0; i< toAddIds.size(); i++){
					Integer resourceId = (Integer)toAddIds.get(i);
					modelResourcesDao.addModelResource(modelId, resourceId);						
				}

				writeBackToClient(new JSONSuccess("'Operation succeded'"));


			} catch (EMFUserError e) {
				logger.error(e.getMessage(), e);
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Exception saving resources", e);
			} catch (JSONException e) {
				logger.error(e.getMessage(), e);
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Exception deserializing resources", e);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Exception in response", e);
			}

		}else if (serviceType != null && serviceType.equalsIgnoreCase(MODELINSTS_COPY_MODEL)) {

			try {	
				//saves all model nodes hierarchy as model instance
				JSONObject response = new JSONObject();
				Integer modelId = (Integer)getAttributeAsInteger("modelId");

				response = recurseOverModelTree(modelId, response, null);

				logger.debug("Loaded model tree");		

				writeBackToClient(new JSONSuccess(response));

			} catch (Throwable e) {
				logger.error("Exception occurred while copying model tree", e);
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Exception occurred while copying model tree", e);
			}
		}else if (serviceType != null && serviceType.equalsIgnoreCase(MODELINSTS_SAVE_ROOT)) {

			try {	
				//saves all model nodes hierarchy as model instance
				JSONObject response = new JSONObject();
				Integer modelId = (Integer)getAttributeAsInteger("modelId");
				Model model = DAOFactory.getModelDAO().loadModelWithoutChildrenById(modelId);
				ModelInstance modelInstNode = new ModelInstance();
				modelInstNode = fillModelInstanceByModel(model, modelInstNode, null);
				
				modelInstNode = setProgressiveOnDuplicate(modelInstNode);
				
				Integer miId = modelDao.insertModelInstanceWithKpi(modelInstNode);
				
				response.append("root", miId);
				response.append("rootlabel", modelInstNode.getLabel());
				response.append("rootname", modelInstNode.getName());
				
				String text = modelInstNode.getName() ;
				if(text.length()>= 20){
					text = text.substring(0, 19)+"...";
				}
				text = modelInstNode.getModel().getCode()+" - "+ text;
				response.append("roottext", text);

				logger.debug("Loaded model tree");		

				writeBackToClient(new JSONSuccess(response));

			} catch (Throwable e) {
				logger.error("Exception occurred while saving model instance root", e);
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Exception occurred while saving model instance root", e);
			}
		}else if(serviceType == null){
			try {

				List thrTypesList = DAOFactory.getDomainDAO().loadListDomainsByType(THRESHOLD_DOMAIN_TYPE);
				getSessionContainer().setAttribute("thrTypesList", thrTypesList);
				Integer start = getAttributeAsInteger( START );
				Integer limit = getAttributeAsInteger( LIMIT );

				if(start==null){
					start = START_DEFAULT;
				}
				if(limit==null){
					limit = LIMIT_DEFAULT;
				}
				List<Resource> allResources = DAOFactory.getResourceDAO().loadPagedResourcesList(start,limit);
				getSessionContainer().setAttribute("ALL_RESOURCES_LIST", allResources);
				//Chart Types
				List kpiChartTypesList = DAOFactory.getDomainDAO().loadListDomainsByType(KPI_CHART_TYPE);
				getSessionContainer().setAttribute("kpiChartTypesList", kpiChartTypesList);

			} catch (EMFUserError e) {
				logger.error(e.getMessage(), e);
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Exception retrieving resources types", e);
			}
		}
		logger.debug("OUT");

	}
	
	private boolean isTreeStructureOfDDNodes(JSONArray ddnodes){
		for(int i =0;i<ddnodes.length(); i++){

			JSONObject obj = (JSONObject)ddnodes.optJSONObject(i);
			try{
				obj.get("children");
				return true;
			}catch (Exception e) {
				return false;
			}
		}
		return false;
	}
	private ModelInstance setProgressiveOnDuplicate(ModelInstance modelInst) throws EMFUserError{
		String name = modelInst.getName();
		Integer howManyExistent = modelDao.getExistentRootsByName(name);
		if(howManyExistent != null && howManyExistent.intValue() != 0){
			String newName = name + "_"+(howManyExistent.intValue()+1);
			modelInst.setName(newName);
		}
		return modelInst;
	}
	private void modelResourcesExtendedListCreate(List<ModelResourcesExtended> modelResourcesExtenList,
			List<Resource> allResources,
			HashMap<Integer, ModelResources> modResourcesIds ){
		if(allResources != null){
			for(int i =0;i<allResources.size(); i++){
				Resource res = allResources.get(i);
				if(!modResourcesIds.keySet().contains(res.getId())){
					ModelResourcesExtended extendedRes = new ModelResourcesExtended(res, new ModelResources());
					modelResourcesExtenList.add(extendedRes);
				}else{
					ModelResourcesExtended extendedRes = new ModelResourcesExtended(res, modResourcesIds.get(res.getId()));
					modelResourcesExtenList.add(extendedRes);
				}
			}
		}
	}

	/**
	 * Creates a json array with children models informations
	 * 
	 * @param rows
	 * @return
	 * @throws JSONException
	 */
	private JSONObject createJSONResponseModelsList(JSONArray rows, Integer totalModelsNumber)throws JSONException {
		JSONObject results;
		results = new JSONObject();
		results.put("total", totalModelsNumber);
		results.put("title", "ModelsList");
		results.put("rows", rows);
		return results;
	}

	/**
	 * Creates a json array with children resources
	 * 
	 * @param rows
	 * @return
	 * @throws JSONException
	 */
	private JSONObject createJSONResponsemodelsResourcesList(JSONArray rows, Integer totalModelsNumber)throws JSONException {
		JSONObject results;
		results = new JSONObject();
		results.put("total", totalModelsNumber);
		results.put("title", "ResourcesList");
		results.put("rows", rows);
		return results;
	}

	private ModelInstance fillModelInstanceByModel(Model model, ModelInstance modelInstNode, Integer parentId) throws EMFUserError{
		modelInstNode.setName(model.getName());
		modelInstNode.setDescription(model.getDescription());
		modelInstNode.setModel(model);
		modelInstNode.setParentId(parentId);
		modelInstNode.setLabel(java.util.UUID.randomUUID().toString());

		Integer kpiId = model.getKpiId();
		if(kpiId != null){
			Kpi kpi = DAOFactory.getKpiDAO().loadKpiById(kpiId);
			KpiInstance kpiInst = new KpiInstance();
			kpiInst.setKpi(kpiId);
			Threshold thrSrc = kpi.getThreshold();
			if(thrSrc != null){
				kpiInst.setThresholdId(thrSrc.getId());
			}
			kpiInst.setWeight(kpi.getStandardWeight());
			modelInstNode.setKpiInstance(kpiInst);
		}

		return modelInstNode;

	}
	private JSONObject recurseOverModelTree(Integer id, JSONObject response, Integer parentId) throws JSONException{

		try {
			Model model =DAOFactory.getModelDAO().loadModelWithChildrenById(id);
			Integer modelInstId = null;
			if(id != null){
				ModelInstance modelInstNode = new ModelInstance();

				//save root first
				try {
					modelInstNode = fillModelInstanceByModel(model, modelInstNode, parentId);
					modelInstNode = setProgressiveOnDuplicate(modelInstNode);
					//save node as ModelInstance node
					modelInstId = modelDao.insertModelInstanceWithKpi(modelInstNode);
					modelInstNode.setId(modelInstId);

					if(parentId == null){
						response.append("root",modelInstId.intValue()+"");
						response.append("rootlabel", modelInstNode.getLabel());
						response.append("rootname", modelInstNode.getName());
						String text = modelInstNode.getName() ;
						if(text.length()>= 20){
							text = text.substring(0, 19)+"...";
						}
						text = modelInstNode.getModel().getCode()+" - "+ text;
						response.append("roottext", text);
					}
				} catch (EMFUserError e) {
					response.append("tree", "KO");
				}

				List <Model> children = model.getChildrenNodes();
				if(children == null){
					try {
						DAOFactory.getModelDAO().loadModelWithChildrenById(parentId);
					} catch (EMFUserError e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if(children != null && ! children.isEmpty()){
					for(int i=0; i< children.size(); i++){
						recurseOverModelTree(((Model)children.get(i)).getId(), response, modelInstId);
					}
				}
			}
		} catch (EMFUserError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}

	private JSONObject recursiveStart(List<ModelInstance> modelInstList, ModelInstance root, JSONObject response) throws JSONException{
		//first time--> searches for root
		Integer parentIdToSearch = null;

		Integer id = root.getId();
		recurseOverTree(modelInstList, root, parentIdToSearch, response, false);


		return response;
	}
	private JSONObject recurseOverTree(List<ModelInstance> modelInstList, ModelInstance modelInstance, Integer parentId, JSONObject response, boolean isToSave) throws JSONException{

		ModelInstance modInstToSave = modelInstance;
		//found  root child
		Integer oldId = null;
		if(modInstToSave.getGuiId() != null){
			try{
				oldId =Integer.valueOf(modInstToSave.getGuiId());//incorrect
			}catch(Throwable t){
				if(!isToSave && modInstToSave.getId() != null){
					oldId = modInstToSave.getId();
				}else
					oldId = null;
			}
		}
		modInstToSave.setParentId(parentId);				
		//save it 
		try {
			Integer genId = modInstToSave.getId();
			if(isToSave){

				genId = modelDao.insertModelInstanceWithKpi(modInstToSave);
				modInstToSave.setId(genId);
			}

			response.append(modInstToSave.getGuiId(), "OK");

			List<ModelInstance> nodes = findNextNodes(modelInstList, oldId);

			if(nodes == null || nodes.isEmpty()){
				//try another way
				nodes = modInstToSave.getChildrenNodes();

			}
			if(nodes != null && !nodes.isEmpty()){
				for (int i=0; i< nodes.size(); i++){
					ModelInstance modInst = (ModelInstance)nodes.get(i);
					recurseOverTree(modelInstList, modInst, genId, response, true);
				}
			}

		} catch (EMFUserError e) {
			logger.error(e.getMessage());
			response.append(modInstToSave.getGuiId(), "KO");
		}
		return response;
	}
	
	private List<ModelInstance> findNextNodes(List<ModelInstance> modelInstList, Integer parentIDToSearch){
		List<ModelInstance> nodes = new ArrayList<ModelInstance>();
		for(int i=0; i< modelInstList.size(); i++){			
			ModelInstance modInstToSave = (ModelInstance)modelInstList.get(i);

			if(parentIDToSearch == null ){
				if(modInstToSave.getParentId() == parentIDToSearch 
						&& modInstToSave.getGuiId().matches("^\\d+$")){
					nodes.add(modInstToSave);
				}
			}else{
				if(modInstToSave.getParentId() != null 
						&&(modInstToSave.getParentId().intValue() == parentIDToSearch.intValue())
						&& modInstToSave.getGuiId().matches("^\\d+$")){
					nodes.add(modInstToSave);
				}
			}
		}
		return nodes;
	}
	
	private List<ModelInstance> deserializeNodesJSONArrayDD(JSONArray rows) throws JSONException{
		List<ModelInstance> toReturn = new ArrayList<ModelInstance>();

		for(int i=0; i< rows.length(); i++){

			JSONObject obj = (JSONObject)rows.get(i);
			ModelInstance modelInst = deserializeJSONObjectDD(obj, toReturn);
			toReturn.add(modelInst);
		}	
		return toReturn;
	}

	private ModelInstance fillModelInstance(JSONObject obj, ModelInstance modelInst){

		String guiId = "";
		try{
			guiId = obj.getString("id");
			modelInst.setGuiId(guiId);
		}catch(Throwable t){
			//nothing--> new node dropped!
			modelInst.setGuiId(null);
		}

		try{

			modelInst.setId(obj.getInt("modelInstId"));
		}catch(Throwable t){
			//nothing
			modelInst.setId(null);
		}

		try{
			modelInst.setParentId(obj.getInt("parentId"));
		}catch(Throwable t){
			//nothing
			modelInst.setParentId(null);
		}

		try{
			String descr ;
			try{
				descr = obj.getString("description");
			}catch(Throwable t){
				descr = null;
			}
			modelInst.setDescription(descr);
			String label ;
			try{
				label = obj.getString("label");
			}catch(Throwable t){
				label = java.util.UUID.randomUUID().toString();
			}
			modelInst.setLabel(label);
			String name ;
			try{
				name = obj.getString("name");
			}catch(Throwable t){
				name = null;
			}
			modelInst.setName(name);
			//or defined model uuid
			String modelUuid;
			try{
				modelUuid = obj.getString("modelUuid");
			}catch(Throwable t){
				modelUuid = null;

			}
			Integer modelId = obj.getInt("modelId");
			try{
				Model model = DAOFactory.getModelDAO().loadModelWithoutChildrenById(modelId);
				modelInst.setModel(model);
			}catch(Throwable t){
				//nothing
				logger.error("no model!");
				modelInst.setModel(null);
			}
			try{
				IKpiInstanceDAO kpiInstDao = DAOFactory.getKpiInstanceDAO();
				String kpiIdStr ;
				try{
					kpiIdStr = obj.getString("kpiId");
				}catch(Throwable t){
					kpiIdStr = null;
				}
				String kpiInIDStr;
				try{
					kpiInIDStr = obj.getString("kpiInstId");
				}catch(Throwable t){
					kpiInIDStr = null;

				}
				KpiInstance kpiInstance = null;
				if(kpiInIDStr != null){
					//existing kpi instance means model instance exists
					kpiInstance = kpiInstDao.loadKpiInstanceById(obj.getInt("kpiInstId"));

				}else{
					//new kpi instance 
					if(kpiIdStr != null){
						if(obj.get("kpiId")!= null && !obj.getString("kpiId").equalsIgnoreCase("")){
							kpiInstance = new KpiInstance();
							int idd = obj.getInt("kpiId");
							kpiInstDao.setKpiInstanceFromKPI(kpiInstance, idd);
						}
					}	
				}
				String kpiInstPeriodicity;
				try{
					kpiInstPeriodicity = obj.getString("kpiInstPeriodicity");
					kpiInstance.setPeriodicityId(Integer.valueOf(kpiInstPeriodicity));
				}catch(Throwable t){
					kpiInstPeriodicity = null;
					if(kpiInstance != null)	kpiInstance.setPeriodicityId(null);
				}


				String kpiInstChartTypeId;
				try{
					kpiInstChartTypeId = obj.getString("kpiInstChartTypeId");
					kpiInstance.setChartTypeId(Integer.valueOf(kpiInstChartTypeId));
				}catch(Throwable t){
					kpiInstChartTypeId = null;
					if(kpiInstance != null) kpiInstance.setChartTypeId(null);
				}

				String kpiInstTarget;
				try{
					kpiInstTarget = obj.getString("kpiInstTarget");
					kpiInstance.setTarget(Double.valueOf(kpiInstTarget));
				}catch(Throwable t){
					kpiInstTarget = null;
					if(kpiInstance != null) kpiInstance.setTarget(null);
				}


				String kpiInstThrCode;
				try{
					kpiInstThrCode = obj.getString("kpiInstThrName");
					Threshold thr = DAOFactory.getThresholdDAO().loadThresholdByCode(kpiInstThrCode);
					if(thr != null){
						kpiInstance.setThresholdId(thr.getId());
					}
				}catch(Throwable t){
					kpiInstThrCode = null;
					if(kpiInstance != null) kpiInstance.setThresholdId(null);
				}

				String kpiInstWeight;
				try{
					kpiInstWeight = obj.getString("kpiInstWeight");
					kpiInstance.setWeight(Double.valueOf(kpiInstWeight));
				}catch(Throwable t){
					kpiInstWeight = null;
					if(kpiInstance != null)
						kpiInstance.setWeight(null);
				}
				String saveHistory;
				try{
					saveHistory = obj.getString("kpiInstSaveHistory");
					kpiInstance.setSaveKpiHistory(true);
				}catch(Throwable t){
					if(kpiInstance != null)
						kpiInstance.setSaveKpiHistory(false);

				}

				modelInst.setKpiInstance(kpiInstance);

			}catch(Throwable t){
				//nothing
				modelInst.setKpiInstance(null);
			}
		}catch(Throwable t){
			logger.debug("Deserialization error on node: "+guiId);
		}
		return modelInst;
	}
	private ModelInstance deserializeJSONObjectDD (JSONObject obj, List<ModelInstance> nodeslist)throws JSONException{

		ModelInstance modelInst = new ModelInstance();

		fillModelInstance(obj, modelInst);
		//children
		JSONArray children ;
		try{
			children = obj.getJSONArray("children");
			List <ModelInstance> childrenMI = new ArrayList<ModelInstance>();
			for(int k=0; k<children.length(); k++){
				JSONObject jsonchild = (JSONObject)children.get(k);
				childrenMI.add(deserializeJSONObjectDD(jsonchild, nodeslist));
			}
			modelInst.setChildrenNodes(childrenMI);
		}catch(Throwable t){
			//nothing
			modelInst.setChildrenNodes(null);
		}
		nodeslist.add(modelInst);
		return modelInst;
	}
	private List<ModelInstance> deserializeJSONArray(JSONArray rows, Vector idsToRemove) throws JSONException{
		List<ModelInstance> toReturn = new ArrayList<ModelInstance>();

		for(int i=0; i< rows.length(); i++){

			JSONObject obj = (JSONObject)rows.get(i);

			ModelInstance modelInst = new ModelInstance();

			fillModelInstance(obj, modelInst);
			//skip root node if new one:
			if(!(modelInst.getParentId() == null && modelInst.getId() == null) &&
					(!idsToRemove.contains(modelInst.getGuiId()))){
				toReturn.add(modelInst);
			}

		}	
		return toReturn;
	}
	private List<Integer> deserializeResourceJSONArray(JSONArray rows) throws JSONException{
		List<Integer> toReturn = new ArrayList<Integer>();
		if(rows != null){
			for(int i=0; i< rows.length(); i++){

				JSONObject obj = (JSONObject)rows.get(i);
				toReturn.add(obj.getInt("id"));
			}
		}
		return toReturn;
	}

	private JSONObject saveModelNodeInstances(List<ModelInstance> nodesToSave) throws Exception{
		JSONArray errorNodes = new JSONArray();

		JSONObject respObj = new JSONObject();

		//loop over nodes and order them ascending
		TreeMap<Integer, ModelInstance> treeMap = new TreeMap<Integer, ModelInstance>();
		for(int i= 0; i<nodesToSave.size(); i++){

			ModelInstance modelInstance = (ModelInstance)nodesToSave.get(i);

			//loads all nodes guiid with type error

			respObj.put(modelInstance.getGuiId(), "OK");

			if(modelInstance.getParentId() != null){
				//look up for its id: if null --> newly created node
				Integer id = modelInstance.getId();
				if(id == null){
					treeMap.put(Integer.valueOf("-"+i+1), modelInstance);
				}else{
					//else to modify node
					treeMap.put(modelInstance.getId(), modelInstance);
				}

			}else{
				//root node --> save first
				try {
					if(modelInstance.getId()  != null){
						modelDao.modifyModelInstance(modelInstance);
						respObj.put(modelInstance.getGuiId(), modelInstance.getId());
					}else{
						if(modelInstance.getId() == null &&
								modelInstance.getParentId() == null &&
								modelInstance.getGuiId() == null){
							//new model instance root --> insert it first
							logger.debug("new model instance root");
						}
						Integer index = modelDao.insertModelInstance(modelInstance);
						respObj.put(modelInstance.getGuiId(), index);
					}
				} catch (Exception e) {
					//send error!!!		
					respObj.put(modelInstance.getGuiId(), "KO");

				}
			}
		}

		Set set = treeMap.entrySet();
		// Get an iterator
		Iterator it = set.iterator(); 
		//loop again over treemap
		while(it.hasNext()) {
			Map.Entry orderedEntry = (Map.Entry)it.next();
			//check that parent exists
			ModelInstance orderedNode = (ModelInstance)orderedEntry.getValue();

			//GET JSON OBJECT VALUE
			Integer parentId = orderedNode.getParentId();
			try {
				ModelInstance parent = modelDao.loadModelInstanceWithoutChildrenById(parentId);
				if(parent != null){						
					//if parent exists--> save					
					//if node id is negative --> insert
					if(orderedNode.getId() == null){
						Integer newId = modelDao.insertModelInstance(orderedNode);
						if (newId != null){
							orderedNode.setId(newId);
							respObj.put(orderedNode.getGuiId(), newId);
						}else{						
							respObj.put(orderedNode.getGuiId(), "KO");
						}
					}else{
						//else update
						modelDao.modifyModelInstance(orderedNode);
						respObj.put(orderedNode.getGuiId(), orderedNode.getId());
					}

				}
			} catch (Exception e) {
				//if parentId != null but no parent node stored on db --> exception
				respObj.put(orderedNode.getGuiId(), "KO");
			}

		} 
		return respObj;
	}


}
