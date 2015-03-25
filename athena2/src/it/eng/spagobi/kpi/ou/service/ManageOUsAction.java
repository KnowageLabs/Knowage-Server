/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.ou.service;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.SerializationException;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.kpi.goal.metadata.bo.GoalKpi;
import it.eng.spagobi.kpi.model.bo.ModelInstance;
import it.eng.spagobi.kpi.model.bo.ModelInstanceNode;
import it.eng.spagobi.kpi.model.service.ManageModelInstancesAction;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnitGrant;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnitGrantNode;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnitHierarchy;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnitNode;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnitNodeWithGrant;
import it.eng.spagobi.kpi.ou.dao.IOrganizationalUnitDAO;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONAcknowledge;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class ManageOUsAction extends AbstractSpagoBIAction {

	private static final long serialVersionUID = -8920524215721282986L;
	// logger component
	private static Logger logger = Logger.getLogger(ManageModelInstancesAction.class);

	//Service parameter
	private final String MESSAGE_DET = "MESSAGE_DET";
	
	//Service parameter values
	private static final String GRANT_DEF = "GRANT_DEF";
	private static final String GRANT_LIST = "GRANT_LIST";
	private static final String OU_LIST = "OU_LIST";
	private static final String OU_CHILDS_LIST = "OU_CHILDS_LIST";
	private static final String OU_HIERARCHY_ROOT = "OU_HIERARCHY_ROOT";
	private static final String OU_HIERARCHY_AND_ROOT = "OU_HIERARCHY_AND_ROOT";
	private static final String OU_GRANT_ERESE = "OU_GRANT_ERESE";
	private static final String OU_GRANT_INSERT = "OU_GRANT_INSERT";
	private static final String KPI_ACTIVE_CHILDS_LIST = "KPI_ACTIVE_CHILDS_LIST";
	
	public static final String MODEL_INSTANCE_NODES = "modelinstancenodes";
	
	//JSON Objects fields names
	private final String GRANT = "grant";
	private final String GRANTNODES = "grantnodes";
	
	//PRIVATE UTILITY COLLECTION FOR GRANT NODES TO INSERT
	private ArrayList<HashMap<Integer, Integer>> utilityGrantNodesCollection = null;
	private HashMap<Integer, List<OrganizationalUnitNode>> ouChildrenByParentIDMap = new HashMap<Integer,List<OrganizationalUnitNode>>();
	private ArrayList<Integer> miChildrenToUncheckList = new ArrayList<Integer>();
		//private HashMap<String, HashMap<String, Array>>

	IOrganizationalUnitDAO orUnitDao = null;
	
	@SuppressWarnings("unchecked")
	@Override
	public void doService() {
		logger.debug("IN");
		utilityGrantNodesCollection = new ArrayList<HashMap<Integer,Integer>>();

		try {
			orUnitDao = DAOFactory.getOrganizationalUnitDAO();
			orUnitDao.setUserProfile(getUserProfile());
		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
			throw new SpagoBIServiceException(SERVICE_NAME,	"Error occurred");
		}
		
		try {
			String serviceType = this.getAttributeAsString(MESSAGE_DET);
			if (serviceType != null && serviceType.equalsIgnoreCase(GRANT_DEF)) {
				logger.debug("Loading the grant..");
				Integer grantId =  getAttributeAsInteger("grantId");
				getGrant(grantId);
				logger.debug("Grant loaded.");
			}else if (serviceType != null && serviceType.equalsIgnoreCase(GRANT_LIST)) {
				logger.debug("Loading the list of grants..");
				getGrantsList();
				logger.debug("List of grant loaded.");
			}else if (serviceType != null && serviceType.equalsIgnoreCase(OU_LIST)) {
				logger.debug("Loading the list of ous..");
				getHierarchiesList();
				logger.debug("List of ous loaded.");
			}else if (serviceType != null && serviceType.equalsIgnoreCase(OU_CHILDS_LIST)) {
				Integer grantId;
				Integer nodeId =null;
				
				//if it is the first node of a new grant the node has a string value not a numeric one
				String nodeIdString = getAttributeAsString("nodeId");
				
				try {
					nodeId = new Integer(nodeIdString);
				} catch (NumberFormatException e) {
					logger.debug("The id of ou node, we ask for the children, is not a number but, "+nodeIdString);
					try {
						writeBackToClient( new JSONAcknowledge() );
						return;
					} catch (Throwable ex) {
						logger.error("Impossible to write back the responce to the client", e);
						throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to write back the responce to the client", e);
					}
				}
				
				Integer hierarchyId = getAttributeAsInteger("ouHierarchy");
				try {
					grantId =  getAttributeAsInteger("grantId");
					logger.debug("Loading the list of ous childs of the node with id"+nodeId+" and grant "+grantId+"...");
					if ( hierarchyId == null ) {
						OrganizationalUnitGrant grant = orUnitDao.getGrant(grantId);
						hierarchyId = grant.getHierarchy().getId();
					}
				} catch(Throwable e) {
					logger.debug(e);
					grantId = null;
					logger.debug("Loading the list of ous childs of the node with id"+nodeId+"...");
				}
				getOUChildrenNodes(hierarchyId, nodeId, grantId);
				logger.debug("Loaded the list of ous childs of the node with id"+nodeId+".");
			}else if (serviceType != null && serviceType.equalsIgnoreCase(OU_HIERARCHY_AND_ROOT)) {
				
				logger.debug("Loading the grant..");
				Integer grantId =  getAttributeAsInteger("grantId");
				OrganizationalUnitGrant grant = orUnitDao.getGrant(grantId);
				logger.debug("Grant loaded.");

				OrganizationalUnitHierarchy hierarchy = grant.getHierarchy();
				
				try {
					JSONObject hierarchyWithRoot = new JSONObject();
					hierarchyWithRoot.put("ouRootName", hierarchy.getName());
					hierarchyWithRoot.put("ouRootId", -1);
					writeBackToClient( new JSONSuccess( hierarchyWithRoot ) );
				} catch (Exception e) {
					throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to serialize the responce to the client", e);
				}
				
//				OrganizationalUnitNode ou = orUnitDao.getRootNode(hierarchyId);
//				
//				try {
//					JSONObject hierarchyWithRoot = new JSONObject();
//					hierarchyWithRoot.put("ouRootName",ou.getOu().getName());
//					hierarchyWithRoot.put("ouRootId",ou.getNodeId());
//					writeBackToClient( new JSONSuccess( hierarchyWithRoot ) );
//				} catch (Exception e) {
//					throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to serialize the responce to the client", e);
//				}

				logger.debug("Loaded ou root of grant with id"+grantId);
			}else if (serviceType != null && serviceType.equalsIgnoreCase(OU_HIERARCHY_ROOT)) {
				Integer hierarchyId = getAttributeAsInteger("hierarchyId");
				Integer modelInstanceId = getAttributeAsInteger("modelInstanceId");
				List<Integer> modelInstances = new ArrayList<Integer>();

				Integer grantId =  getAttributeAsInteger("grantId");
				
//				Integer grantId;
//				try{
//					grantId =  getAttributeAsInteger("grantId");
//					logger.debug("Loading the ou root of the hierarchy with id"+hierarchyId+" and grant "+grantId+"...");
//				}catch(Throwable e){
//					grantId = null;
//					
//					try{
//						ModelInstance aModel = DAOFactory.getModelInstanceDAO().loadModelInstanceWithChildrenById(modelInstanceId);
//						modelInstances = getModelInstances(aModel.getId());
//					}catch(Exception ee){
//
//					}
//					logger.debug("Loading the ou root of the hierarchy with id"+hierarchyId+"...");
//				}
				
				getHierarchyRootNode(hierarchyId, grantId, modelInstances);
				logger.debug("Loaded the ou root of the hierarchy with id"+hierarchyId+"...");
			}else if (serviceType != null && serviceType.equalsIgnoreCase(OU_GRANT_ERESE)) {
				Integer grantId = getAttributeAsInteger("grantId");
				logger.debug("Eresing the grant with id "+grantId+"...");
				eraseGrant(grantId);
				logger.debug("Eresed the grant with id "+grantId+"...");
			}else if (serviceType != null && serviceType.equalsIgnoreCase(OU_GRANT_INSERT)) {
				JSONArray grantNodesJSON = getAttributeAsJSONArray(GRANTNODES);
				JSONObject grantJSON = getAttributeAsJSONObject(GRANT);
				logger.debug("Adding the grant "+grantNodesJSON+"..."+grantJSON);
				insertGrant(grantJSON, grantNodesJSON);
				logger.debug("Added the grant.");

			}else if (serviceType != null && serviceType.equalsIgnoreCase(KPI_ACTIVE_CHILDS_LIST)) {
				Integer ouNodeId =  getAttributeAsInteger("ouNodeId");
				Integer grantId =  getAttributeAsInteger("grantId");
				Integer goalNodeId =  null;
				try{
					goalNodeId = getAttributeAsInteger("goalNodeId");
				}catch (NumberFormatException e) {
					goalNodeId = null;
				}
				String parentId = (String)getAttributeAsString("modelInstId");
				
				List<OrganizationalUnitNodeWithGrant> ousWithGrants = orUnitDao.getGrantNodes(ouNodeId, grantId);
				List<OrganizationalUnitGrantNode> grants = new ArrayList<OrganizationalUnitGrantNode>();
				for(int i=0; i<ousWithGrants.size(); i++){
					grants.addAll(ousWithGrants.get(i).getGrants());
				}
				List<Integer> modelInstances = new ArrayList<Integer>();
				
				for(int i=0; i<grants.size(); i++){
					modelInstances.add(grants.get(i).getModelInstanceNode().getModelInstanceNodeId());
				}
				
				try{
					if(parentId == null || parentId.startsWith("xnode")){
						writeBackToClient( new JSONAcknowledge() );
						return;
					}
					ModelInstance aModel = DAOFactory.getModelInstanceDAO().loadModelInstanceWithChildrenById(Integer.parseInt(parentId));
					List<ModelInstance> children = aModel.getChildrenNodes();
					
					for(int i=0; i<children.size(); i++){
						if(modelInstances.contains(children.get(i).getId())){
							children.get(i).setActive(true);
						}
					}

					JSONArray modelChildrenJSON = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(children,	getLocale());
					
					
					if(goalNodeId!=null){
						List<GoalKpi> listGoalKpi = DAOFactory.getGoalDAO().getGoalKpi(goalNodeId);
						for(int i=0; i<modelChildrenJSON.length(); i++){
							for(int j=0; j<listGoalKpi.size(); j++){
								if(listGoalKpi.get(j).getModelInstanceId().equals( children.get(i).getId())){
									((JSONObject)modelChildrenJSON.get(i)).put("weight1", ""+listGoalKpi.get(j).getWeight1());
									((JSONObject)modelChildrenJSON.get(i)).put("weight2", ""+listGoalKpi.get(j).getWeight2());
									((JSONObject)modelChildrenJSON.get(i)).put("threshold1", ""+listGoalKpi.get(j).getThreshold1());
									((JSONObject)modelChildrenJSON.get(i)).put("threshold2", ""+listGoalKpi.get(j).getThreshold2());
									((JSONObject)modelChildrenJSON.get(i)).put("sign1", ""+listGoalKpi.get(j).getSign1());
									((JSONObject)modelChildrenJSON.get(i)).put("sign2", ""+listGoalKpi.get(j).getSign2());
									break;
								}
							}
						}
					}
					writeBackToClient(new JSONSuccess(modelChildrenJSON));

				} catch (Throwable e) {
					logger.error("Exception occurred while retrieving model tree", e);
					throw new SpagoBIServiceException(SERVICE_NAME,
							"Exception occurred while retrieving model tree", e);
				}


				logger.debug("Loaded the list of ous childs of the node with id"+ouNodeId+".");
			}else if(serviceType == null){
				logger.debug("no service");
				Assert.assertUnreachable("No service defined.");
			}
			
		} finally {
			logger.debug("OUT");
		}
	}
	

	
	public List<Integer> getModelInstances(Integer aModelId) throws EMFUserError  {

		List<Integer> modelInstances = new ArrayList<Integer>();
		List<ModelInstance> children = DAOFactory.getModelInstanceDAO().loadModelInstanceWithChildrenById(aModelId).getChildrenNodes(); 
		modelInstances.add(aModelId);
		for (int i = 0; i < children.size(); i++) {
			modelInstances.addAll(getModelInstances(children.get(i).getId()));
		}

		return modelInstances;
	}
	
	/**
	 * Load the list of grants and serialize them in a JSOMObject. The list live in the attributes with name rows
	 */
	private void getGrantsList(){
		List<OrganizationalUnitGrant> grants = orUnitDao.getGrantsList();
	
		try {
			JSONArray grantsJSON = (JSONArray) SerializerFactory.getSerializer("application/json").serialize( grants, null);
			JSONObject grantsJSONObject = new JSONObject();
			grantsJSONObject.put("rows", grantsJSON);
			writeBackToClient( new JSONSuccess( grantsJSONObject ) );
		} catch (Exception e) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to serialize the responce to the client", e);
		}
	}
	
	/**
	 * Load the list of grants and serialize them in a JSOMObject. The list live in the attributes with name rows
	 * @param grantId the id of the grant
	 */
	private void getGrant(Integer grantId){
		OrganizationalUnitGrant grant = orUnitDao.getGrant(grantId);
	
		try {
			JSONObject grantsJSON = (JSONObject) SerializerFactory.getSerializer("application/json").serialize( grant, null);
			writeBackToClient( new JSONSuccess( grantsJSON ) );
		} catch (Exception e) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to serialize the responce to the client", e);
		}
	}
	
	/**
	 * Load the list of OUHierarchies and serialize them in a JSOMObject. 
	 */
	private void getHierarchiesList(){
		List<OrganizationalUnitHierarchy> ous = orUnitDao.getHierarchiesList();
	
		try {
			JSONArray grantsJSON = (JSONArray) SerializerFactory.getSerializer("application/json").serialize( ous, null);
			writeBackToClient( new JSONSuccess( grantsJSON ) );
		} catch (IOException e) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to write back the responce to the client", e);
		} catch (SerializationException e) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to serialize the responce to the client", e);
		}
	}
	
	
	/**
	 * Load the children ou node of a passed node 
	 * @param hierarchyId the id of the hierarchy
	 * @param nodeId the id of the parent node
	 * @param grantId the id of the grant because we return OrganizationalUnitNodeWithGrant. If null
	 * 			the grant object in the OrganizationalUnitNodeWithGrant will be null
	 */
	private void getOUChildrenNodes(Integer hierarchyId, Integer nodeId, Integer grantId){
		List<OrganizationalUnitNodeWithGrant> ousWithGrants = null;
		if ( grantId == null ) {
			List<OrganizationalUnitNode> ous = null;
			if (nodeId == -1) {
				// getting hierarchy's roots
				ous = orUnitDao.getRootNodes(hierarchyId);
			} else {
				ous = orUnitDao.getChildrenNodes(nodeId);
			}
			ousWithGrants = new ArrayList<OrganizationalUnitNodeWithGrant>();
			for (int i=0; i<ous.size(); i++) {
				ousWithGrants.add(new OrganizationalUnitNodeWithGrant(ous.get(i), new ArrayList<OrganizationalUnitGrantNode>()));
			}
		} else {
			if (nodeId == -1) {
				ousWithGrants = orUnitDao.getRootNodesWithGrants(hierarchyId, grantId);
			} else {
				ousWithGrants = orUnitDao.getChildrenNodesWithGrants(nodeId, grantId);
			}
		} try {
			JSONArray grantsJSON = (JSONArray) SerializerFactory.getSerializer("application/json").serialize( ousWithGrants, null);
			writeBackToClient( new JSONSuccess( grantsJSON ) );
		} catch (IOException e) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to write back the responce to the client", e);
		} catch (SerializationException e) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to serialize the responce to the client", e);
		}
	}
	
	/**
	 * Load the ou root node of a hierarchy
	 * @param hierarchyId the id of the hierarchy
	 * @param grantId the id of the grant because we return OrganizationalUnitNodeWithGrant. If null
	 * 			the grant object in the OrganizationalUnitNodeWithGrant will be null
	 */
	private void getHierarchyRootNode(Integer hierarchyId, Integer grantId, List<Integer> modelInstances){
		
		OrganizationalUnitHierarchy hierarchy = orUnitDao.getHierarchy(hierarchyId);
		
		JSONObject response = new JSONObject();
		
		try {
			response.put("id", -1);
			response.put("path", "/");
			response.put("leaf", false);
			JSONObject dummyOU = new JSONObject();
			dummyOU.put("id", -1);
			dummyOU.put("name", hierarchy.getName());
			dummyOU.put("description", hierarchy.getName());
//			dummyOU.put("ouRootName", hierarchy.getName());
//			dummyOU.put("ouRootId", -1);
			response.put("ou", dummyOU);
		} catch (Exception e) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to serialize the responce to the client", e);
		}
		
//		OrganizationalUnitNodeWithGrant ouWithGrant;
//		
//			if(grantId==null){
//				OrganizationalUnitNode ou = orUnitDao.getRootNode(hierarchyId);
//				ouWithGrant = new OrganizationalUnitNodeWithGrant(ou, new ArrayList<OrganizationalUnitGrantNode>());
//
//			}else{
//				ouWithGrant = orUnitDao.getRootNodeWithGrants(hierarchyId, grantId);
//
//			}
		try {
//			JSONObject grantsJSON = ((JSONObject) SerializerFactory.getSerializer("application/json").serialize( ouWithGrant, null));
//			if(modelInstances.size()>0){
//				grantsJSON.remove(MODEL_INSTANCE_NODES);
//				grantsJSON.put(MODEL_INSTANCE_NODES, modelInstances);
//			}
//			
//			writeBackToClient( new JSONSuccess( grantsJSON ) );
			
			writeBackToClient( new JSONSuccess( response ) );
		} catch (Exception e) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to serialize the responce to the client", e);
		} 
	}
	
	/**
	 * Erase a grant
	 * @param grantId the id of the grant to erase
	 */
	private void eraseGrant(Integer grantId){
		orUnitDao.eraseGrant(grantId);
		try {
			writeBackToClient( new JSONAcknowledge() );
		} catch (IOException e) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to write back the responce to the client", e);
		}
	}

	/**
	 * Give a grant to a grant nodes
	 * @param grantJSON the JSON representation of the grant
	 * @param grantNodesJSON the JSON representation of the nodes 
	 */
	private void insertGrant(JSONObject grantJSON,JSONArray grantNodesJSON){
		try {
			OrganizationalUnitGrant grant = deserializeOrganizationalUnitGrant(grantJSON);
			//set unavailable
			grant.setIsAvailable(false);
			List<OrganizationalUnitGrantNode> grantNodes = null;
			logger.debug("start saving");
			if(grant.getId()!=null){
				miChildrenToUncheckList = new ArrayList<Integer>();
				ouChildrenByParentIDMap = new HashMap<Integer,List<OrganizationalUnitNode>>();
				logger.debug("modify");
				orUnitDao.eraseNodeGrants(grant.getId());
				logger.debug("erased nodes");
				orUnitDao.modifyGrant(grant);
				logger.debug("modified grant");
				grantNodes = deserializeOrganizationalUnitGrantNodesAndUpdateChilds(grantNodesJSON, grant);
				logger.debug("modify grant");
			} else {
				logger.debug("insert");
				orUnitDao.insertGrant(grant);
				logger.debug("inserted grant");
				// reload the grant in order to have its id
				grant = orUnitDao.loadGrantByLabel(grant.getLabel());
				//first time save all
				grantNodes = getAllNodesToInsert(grant);
			}
			grant.setIsAvailable(true);
			logger.debug("prepared nodes to save of size:"+grantNodes.size());
			orUnitDao.insertNodeGrants(grantNodes, grant.getId());
			logger.debug("end saving process");
			JSONObject response = new JSONObject();
			response.put("id", grant.getId());
			writeBackToClient( new JSONSuccess( response ) );
		} catch (IOException e) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to write back the responce to the client", e);
		} catch (Exception e) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to serialize the responce to the client", e);
		} 
	}
	private List <OrganizationalUnitGrantNode> getAllNodesToInsert(OrganizationalUnitGrant grant) throws EMFUserError{		
		List <OrganizationalUnitGrantNode> allnodestosave = new ArrayList<OrganizationalUnitGrantNode>();
		Integer hierarchyId = grant.getHierarchy().getId();
		List<OrganizationalUnitNode> ouNodes = orUnitDao.getOrganizationalUnitNodeList(hierarchyId);
		//get only once the list of model instance nodes
		
		List<ModelInstanceNode> miNodes = getModelInstList(grant.getModelInstance().getId());
		ModelInstanceNode modelInstNode = DAOFactory.getModelInstanceDAO().loadModelInstanceById(grant.getModelInstance().getId(), null);
		//adds root 
		miNodes.add(modelInstNode);
		
		for(int i=0; i<ouNodes.size(); i++){
			//model inst children			
			allnodestosave.addAll(buildGrantNodesForAll(ouNodes.get(i), miNodes, grant));
		}
		return allnodestosave;
	}
	
	private List<OrganizationalUnitGrantNode> buildGrantNodesForAll(OrganizationalUnitNode ouNode, List<ModelInstanceNode> miNodes,  OrganizationalUnitGrant grant) throws EMFUserError{
		List<OrganizationalUnitGrantNode> nodes = new ArrayList<OrganizationalUnitGrantNode>();
		OrganizationalUnitGrantNode childNode;

		for(int i=0; i<miNodes.size(); i++){

			childNode = new OrganizationalUnitGrantNode();
			childNode.setGrant(grant);
			childNode.setModelInstanceNode(miNodes.get(i));
			childNode.setOuNode(ouNode);
			nodes.add(childNode);
		}
		return nodes;
	}
	/**
	 * Deserialize a OrganizationalUnitGrant object
	 * @param jsonGrant the JSON representation of the OrganizationalUnitGrant object
	 * @return the OrganizationalUnitGrant
	 * @throws Exception
	 */
	private OrganizationalUnitGrant deserializeOrganizationalUnitGrant(JSONObject jsonGrant) throws Exception{
		OrganizationalUnitGrant organizationalUnitGrant = new OrganizationalUnitGrant();
		organizationalUnitGrant.setDescription(jsonGrant.getString("description"));
		
//        ConfigSingleton config = ConfigSingleton.getInstance();
//        SourceBean formatSB = (SourceBean) config.getAttribute("SPAGOBI.DATE-FORMAT-SERVER");
//	    String format = (String) formatSB.getAttribute("format");
		String format = "dd/MM/yyyy";
	    
		organizationalUnitGrant.setEndDate(toDate(jsonGrant.getString("enddate"), format));
		organizationalUnitGrant.setStartDate(toDate(jsonGrant.getString("startdate"), format));
		organizationalUnitGrant.setLabel(jsonGrant.getString("label"));
		organizationalUnitGrant.setName(jsonGrant.getString("name"));
		int id = jsonGrant.optInt("id", -1);
		logger.debug("Grant id = " + id);
		if (id != -1) {
			logger.debug("Grant id not found");
			organizationalUnitGrant.setId(id);
		}
		int hierarchyId = jsonGrant.getJSONObject("hierarchy").getInt("id");
		int modelInstanceId = jsonGrant.getJSONObject("modelinstance").getInt("modelInstId");
		ModelInstance modelInstance = DAOFactory.getModelInstanceDAO().loadModelInstanceWithChildrenById(modelInstanceId);
		OrganizationalUnitHierarchy organizationalUnitHierarchy = orUnitDao.getHierarchy(hierarchyId);
		organizationalUnitGrant.setModelInstance(modelInstance);
		organizationalUnitGrant.setHierarchy(organizationalUnitHierarchy);
		
		return organizationalUnitGrant;
	}

//	/**
//	 * Deserialize a list of OrganizationalUnitGrantNode objects
//	 * @param JSONGrantNodes the JSON representation of the list of OrganizationalUnitGrantNode
//	 * @param grant the grant of that list
//	 * @return the deserialized object
//	 * @throws Exception
//	 */
//	private List<OrganizationalUnitGrantNode> deserializeOrganizationalUnitGrantNodes(JSONArray JSONGrantNodes, OrganizationalUnitGrant grant) throws Exception{
//		List<OrganizationalUnitGrantNode> nodes = new ArrayList<OrganizationalUnitGrantNode>();
//		for(int i=0; i<JSONGrantNodes.length(); i++){
//			
//			nodes.add(deserializeOrganizationalUnitGrantNode( JSONGrantNodes.getJSONObject(i), grant));
//		}
//		return nodes;
//	}
	
//	/**
//	 * Deserialize a OrganizationalUnitGrantNode object
//	 * @param JSONGrantNodes the JSON representation of the OrganizationalUnitGrantNode
//	 * @param grant the grant of that object
//	 * @return the deserialized object
//	 * @throws Exception
//	 */
//	private OrganizationalUnitGrantNode deserializeOrganizationalUnitGrantNode(JSONObject JSONGrantNode, OrganizationalUnitGrant grant) throws Exception{
//		OrganizationalUnitGrantNode node = new OrganizationalUnitGrantNode();
//		int hierarchyId = JSONGrantNode.getInt("hierarchyId");
//		int modelInstanceId = JSONGrantNode.getInt("modelinstance");
//		String ouPath = JSONGrantNode.getString("ouPath");
//		ModelInstanceNode modelInstanceNode = DAOFactory.getModelInstanceDAO().loadModelInstanceById(modelInstanceId, null);
//		OrganizationalUnitNode ouNode = orUnitDao.getOrganizationalUnitNode(ouPath, hierarchyId);
//		node.setGrant(grant);
//		node.setModelInstanceNode(modelInstanceNode);
//		node.setOuNode(ouNode);
//		return node;
//	}
	

	
	/**
	 * Deserialize a list of OrganizationalUnitGrantNode objects. If the nodes have not been expanded by the
	 * user, this methods load the subtree rooted in all the JSONGrantNodes and
	 * build one grant node for each node of the subtree
	 * @param JSONGrantNodes the JSON representation of the list of OrganizationalUnitGrantNode
	 * @param grant the grant of that list
	 * @return the deserialized object
	 * @throws Exception
	 */
	private List<OrganizationalUnitGrantNode> deserializeOrganizationalUnitGrantNodesAndUpdateChilds(JSONArray JSONGrantNodes, OrganizationalUnitGrant grant) throws Exception{
		
		//logger.debug(JSONGrantNodes);
		
		List<OrganizationalUnitGrantNode> nodes = new ArrayList<OrganizationalUnitGrantNode>();
		
		List<JSONObject> JSONGrantNodesFiltered = new ArrayList<JSONObject>();
		for(int y=0; y<JSONGrantNodes.length(); y++){
			JSONGrantNodesFiltered.add(JSONGrantNodes.getJSONObject(y));
		}
		
		for(int i=0; i<JSONGrantNodes.length(); i++){
			JSONObject JSONGrantNode = JSONGrantNodes.getJSONObject(i);
			int modelInstanceId = JSONGrantNode.getInt("modelinstance");
			try{
				Integer modelInstancesToUncheck = JSONGrantNode.optInt("childrenToUncheck");
				if(miChildrenToUncheckList.indexOf(modelInstancesToUncheck) == -1){
					miChildrenToUncheckList.add(modelInstancesToUncheck);				
				
					if(modelInstancesToUncheck!=null){
					
						if(-1== modelInstanceId){
							List<Integer> children = getChildren(modelInstancesToUncheck);
							for(int o=0; o<children.size(); o++){
								
								for(int y=0; y<JSONGrantNodesFiltered.size(); y++){
									if(
										JSONGrantNode.getInt("hierarchyId") ==  JSONGrantNodesFiltered.get(y).getInt("hierarchyId") &&
										JSONGrantNode.getString("ouPath").equals(JSONGrantNodesFiltered.get(y).getString("ouPath")) &&
										(children.get(o) ==  JSONGrantNodesFiltered.get(y).getInt("modelinstance") ||
												 JSONGrantNodesFiltered.get(y).getInt("modelinstance")==-1
												)
										
									){
										JSONGrantNodesFiltered.remove(y);
									}
								}
							}
						}
					}  
				}
			}catch (Throwable e){
				logger.error(e.getMessage());
			}
			
		}	
		
		
		
		for(int i=0; i<JSONGrantNodesFiltered.size(); i++){
			nodes.addAll(deserializeOrganizationalUnitGrantNodeAndUpdateChilds( JSONGrantNodesFiltered.get(i), grant));
		}
		return nodes;
	}
	
	/**
	 * Deserialize a OrganizationalUnitGrantNode object. If the node has not been expanded by the
	 * user, this methods load the subtree rooted in the JSONGrantNodes and
	 * build one grant node for each node of the subtree
	 * @param JSONGrantNodes the JSON representation of the OrganizationalUnitGrantNode
	 * @param grant the grant of that object
	 * @return the deserialized object
	 * @throws Exception
	 */
	private List<OrganizationalUnitGrantNode> deserializeOrganizationalUnitGrantNodeAndUpdateChilds(JSONObject JSONGrantNode, OrganizationalUnitGrant grant) throws Exception{
		OrganizationalUnitGrantNode node = new OrganizationalUnitGrantNode();
		List<OrganizationalUnitGrantNode> nodes = new ArrayList<OrganizationalUnitGrantNode>();
		int hierarchyId = JSONGrantNode.getInt("hierarchyId");
		int modelInstanceId = JSONGrantNode.getInt("modelinstance");
		String ouPath = JSONGrantNode.getString("ouPath");
		if(modelInstanceId>=0){
			ModelInstanceNode modelInstanceNode = DAOFactory.getModelInstanceDAO().loadModelInstanceById(modelInstanceId, null);
			OrganizationalUnitNode ouNode = orUnitDao.getOrganizationalUnitNode(ouPath, hierarchyId);
			node.setGrant(grant);
			node.setModelInstanceNode(modelInstanceNode);
			node.setOuNode(ouNode);
	
			HashMap<Integer, Integer> tempGrantNodeIds = new HashMap<Integer, Integer>();
			tempGrantNodeIds.put(modelInstanceNode.getModelInstanceNodeId(),ouNode.getNodeId());
			if(!utilityGrantNodesCollection.contains(tempGrantNodeIds)  ){      
				Integer modelInstancesToUncheck = JSONGrantNode.optInt("childrenToUncheck");
				if(modelInstancesToUncheck!=null){
	
					if(modelInstancesToUncheck.equals(modelInstanceNode.getModelInstanceNodeId())){
						return nodes;
					}
	
				}
				nodes.add(node);
				utilityGrantNodesCollection.add(tempGrantNodeIds);
			}else{
				return nodes;
			}
			boolean expanded = JSONGrantNode.getBoolean("expanded");
			if(!expanded){
				//if(JSONGrantNode.opt("childrenToUncheck")== null || JSONGrantNode.getInt("childrenToUncheck")!=modelInstanceNode.getModelInstanceNodeId()){
					nodes.addAll(buildGrantForChilds(ouNode, modelInstanceNode, grant));
				//}
			}
			Integer checkChildren = null;
			
			try{
				checkChildren = JSONGrantNode.getInt("childrenToCheck");
				ModelInstanceNode modelInstNode = DAOFactory.getModelInstanceDAO().loadModelInstanceById(checkChildren, null);
				List<OrganizationalUnitGrantNode> childrenChecked = buildGrantForModelInstChildren(ouNode, modelInstNode, grant);
				for(int i=0; i< childrenChecked.size(); i++){
					OrganizationalUnitGrantNode nodeToAdd = childrenChecked.get(i);
					HashMap<Integer, Integer> temp2GrantNodeIds = new HashMap<Integer, Integer>();
					temp2GrantNodeIds.put(nodeToAdd.getModelInstanceNode().getModelInstanceNodeId(), nodeToAdd.getOuNode().getNodeId());
					if(!utilityGrantNodesCollection.contains(temp2GrantNodeIds)){
						nodes.add(nodeToAdd);
						utilityGrantNodesCollection.add(temp2GrantNodeIds);
					}
				}
		
			}catch(Throwable t){
				logger.debug("childrenToCheck not present"); 
			}
		}

		return nodes;
	}
	
	public List<Integer> getChildren(Integer modelInstanceId) throws Exception{
		List<Integer> list = new ArrayList<Integer>();
		ModelInstance mi = DAOFactory.getModelInstanceDAO().loadModelInstanceWithChildrenById(modelInstanceId);
		list.add(mi.getId());
		if(mi.getChildrenNodes()!=null){
			for(int i=0; i<mi.getChildrenNodes().size(); i++){
				list.addAll(getChildren(((ModelInstance)mi.getChildrenNodes().get(i)).getId()));
			}
		}
		return list;
	}

	/**
	 * For each child of the OrganizationalUnitNode ouNode, build a OrganizationalUnitGrantNode
	 * with grant grant and model Instance Node modelInstanceNode
	 * @param ouNode
	 * @param modelInstanceNode
	 * @param grant
	 * @return
	 */
	private List<OrganizationalUnitGrantNode> buildGrantForChilds(OrganizationalUnitNode ouNode, ModelInstanceNode modelInstanceNode,  OrganizationalUnitGrant grant){
		List<OrganizationalUnitGrantNode> nodes = new ArrayList<OrganizationalUnitGrantNode>();
		OrganizationalUnitGrantNode childNode;
		//System.out.println(ouNode.getNodeId());
		Integer parentId =ouNode.getNodeId();
		List<OrganizationalUnitNode> childOus = new ArrayList<OrganizationalUnitNode>();
		//check if already loaded
		if(ouChildrenByParentIDMap.get(parentId) == null){
			childOus = orUnitDao.getChildrenNodes(ouNode.getNodeId());
			ouChildrenByParentIDMap.put(parentId, childOus);
		}else{
			childOus = ouChildrenByParentIDMap.get(parentId);
		}
		
		for(int i=0; i<childOus.size(); i++){
			childNode= new OrganizationalUnitGrantNode();
			childNode.setGrant(grant);
			childNode.setModelInstanceNode(modelInstanceNode);
			childNode.setOuNode(childOus.get(i));
			
			HashMap<Integer, Integer> tempGrantNodeIds = new HashMap<Integer, Integer>();
			tempGrantNodeIds.put(modelInstanceNode.getModelInstanceNodeId(), childOus.get(i).getNodeId());
			if(!utilityGrantNodesCollection.contains(tempGrantNodeIds)){
				nodes.add(childNode);
				utilityGrantNodesCollection.add(tempGrantNodeIds);
			}
			
			nodes.addAll(buildGrantForChilds(childOus.get(i), modelInstanceNode, grant));			
		}
		return nodes;
	}
	/**
	 * For each child of the ModelInstanceNode modelInstanceNode, build a OrganizationalUnitGrantNode
	 * with grant grant and model Instance Node modelInstanceNode
	 * @param ouNode
	 * @param modelInstanceNode
	 * @param grant
	 * @return
	 * @throws EMFUserError 
	 */
	private List<OrganizationalUnitGrantNode> buildGrantForModelInstChildren(OrganizationalUnitNode ouNode, ModelInstanceNode modelInstanceNode,  OrganizationalUnitGrant grant) throws EMFUserError{
		List<OrganizationalUnitGrantNode> nodes = new ArrayList<OrganizationalUnitGrantNode>();
		OrganizationalUnitGrantNode childNode;
		List<Integer> childOus = modelInstanceNode.getChildrenIds();
		for(int i=0; i<childOus.size(); i++){
			ModelInstanceNode miChildNode = DAOFactory.getModelInstanceDAO().loadModelInstanceById((Integer)childOus.get(i), null);
			childNode = new OrganizationalUnitGrantNode();
			childNode.setGrant(grant);
			childNode.setModelInstanceNode(miChildNode);
			childNode.setOuNode(ouNode);
			nodes.add(childNode);
			nodes.addAll(buildGrantForModelInstChildren(ouNode, miChildNode, grant));
		}
		return nodes;
	}
	/**adds recursevly mi children
	 * @param id
	 * @return
	 * @throws EMFUserError
	 */
	private List<ModelInstanceNode> getModelInstList(Integer id) throws EMFUserError{
		List<ModelInstanceNode> nodes = new ArrayList<ModelInstanceNode>();
		ModelInstanceNode modelInstanceNode = DAOFactory.getModelInstanceDAO().loadModelInstanceById(id, null);
		
		List<Integer> childOus = modelInstanceNode.getChildrenIds();
		for(int i=0; i<childOus.size(); i++){
			ModelInstanceNode miChildNode = DAOFactory.getModelInstanceDAO().loadModelInstanceById((Integer)childOus.get(i), null);
			nodes.add(miChildNode);
			nodes.addAll(getModelInstList(miChildNode.getModelInstanceNodeId()));
		}
		return nodes;
	}
	
	
	public Date toDate(String dateStr, String format) throws Exception {
		SimpleDateFormat dateFormat = new SimpleDateFormat();
		Date date = null;
		dateFormat.applyPattern(format);
		dateFormat.setLenient(false);
		date = dateFormat.parse(dateStr);
		return date;
	}

}
