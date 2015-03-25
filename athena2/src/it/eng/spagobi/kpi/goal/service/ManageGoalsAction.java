/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.goal.service;

import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.kpi.goal.dao.IGoalDAO;
import it.eng.spagobi.kpi.goal.metadata.bo.Goal;
import it.eng.spagobi.kpi.goal.metadata.bo.GoalKpi;
import it.eng.spagobi.kpi.goal.metadata.bo.GoalNode;
import it.eng.spagobi.kpi.model.bo.ModelInstance;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnitGrant;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnitGrantNode;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnitNodeWithGrant;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONAcknowledge;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ManageGoalsAction extends AbstractSpagoBIAction {

	private static final long serialVersionUID = -5502086385334047520L;

	// logger component
	private static Logger logger = Logger.getLogger(ManageGoalsAction.class);

	//Service parameter
	private final String MESSAGE_DET = "MESSAGE_DET";
	
	//Service parameter values
	private static final String GOALS_LIST = "GOALS_LIST";
	private static final String OU_GOAL_ROOT = "OU_GOAL_ROOT";
	private static final String GOAL_ERESE = "GOAL_ERESE";
	private static final String GOAL_INSERT = "GOAL_INSERT";
	private static final String GOAL_NODE_CHILD = "GOAL_NODE_CHILD";
	private static final String UPDATE_GOAL_NAME = "UPDATE_GOAL_NAME";
	private static final String KPI_GOAL_ROOT_NODE = "KPI_GOAL_ROOT_NODE";
	private static final String INSERT_GOAL_NODE = "INSERT_GOAL_NODE";
	private static final String ERESE_GOAL_NODE = "ERESE_GOAL_NODE";
	private static final String INSERT_GOAL_DETAILS = "INSERT_GOAL_DETAILS";
	
	public static final String GOAL = "goal";
	public static final String ID = "id";
	public static final String LABEL = "label";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String START_DATE = "startdate";
	public static final String END_DATE = "enddate";
	public static final String GRANT = "grant";
	public static final String GOAL_ID = "goalId";
	public static final String OU = "ou";
	public static final String GOALS = "goals";
	public static final String KPIS = "kpis";
	
	public static final String MODELINSTID = "modelInstId";
	public static final String WEIGHT1 = "weight1";
	public static final String WEIGHT2 = "weight2";
	public static final String THRESHOLD1 = "threshold1";
	public static final String SIGN1 = "sign1";
	public static final String SIGN2 = "sign2";
	public static final String THRESHOLD2 = "threshold2";
	
	public static final String GOAL_DESC = "goalDesc";
	public static final String GOAL_FATHER = "fatherCountNode";
	public static final String GOAL_NODE_COUNT = "nodeCount";

	IGoalDAO daoGoal=null;
	
	@Override
	public void doService() {

	
		logger.debug("IN");
		
		try {
			daoGoal = DAOFactory.getGoalDAO();

		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
			throw new SpagoBIServiceException(SERVICE_NAME,	"Error occurred");
		}
		
		String serviceType = this.getAttributeAsString(MESSAGE_DET);
		
		if (serviceType != null && serviceType.equalsIgnoreCase(GOAL_NODE_CHILD)) {
			logger.debug("Loading the children for the goal with id:");
			try{
				Integer nodeId =  getAttributeAsInteger("nodeId");
				logger.debug("nodeId:"+ nodeId);
				List<GoalNode> children = daoGoal.getChildrenNodes(nodeId);
				JSONArray ja = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(children,	getLocale());
				writeBackToClient(new JSONSuccess(ja));				
			} catch (NumberFormatException e) {
				logger.debug("The goal node "+ getAttribute("nodeId") +" is not saved yet ");
				try{
					writeBackToClient(new JSONSuccess(new JSONArray()));
				}catch (IOException io){
					logger.debug("Error getting the child of the goal node");
					throw new SpagoBIServiceException(SERVICE_NAME, "Error getting the child of the goal node",e);
				}
			} catch (Exception ee){
				logger.debug("Error getting the child of the goal node");
				throw new SpagoBIServiceException(SERVICE_NAME, "Error getting the child of the goal node",ee);
			}
			logger.debug("Loaded children");
		}
		if (serviceType != null && serviceType.equalsIgnoreCase(OU_GOAL_ROOT)) {
			logger.debug("Loading the root node of the goal tree with:");
			JSONObject jo;
			Integer goalId =  getAttributeAsInteger("goalId");
			Integer ouId =  getAttributeAsInteger("ouId");
			logger.debug("goalId: "+goalId+" ,ouId "+ouId);
			GoalNode root = daoGoal.getRootNode(goalId, ouId);
			try{
				if(root!=null){
					jo = (JSONObject) SerializerFactory.getSerializer("application/json").serialize(root, getLocale());
				}else{
					jo = new JSONObject();
				}
				writeBackToClient(new JSONSuccess(jo));	
			} catch (Exception e){
				logger.debug("Error getting the root of the goal");
				throw new SpagoBIServiceException(SERVICE_NAME, "Error getting the root of the goal",e);
			}
			logger.debug("Goal root loaded");
		}
		if (serviceType != null && serviceType.equalsIgnoreCase(INSERT_GOAL_NODE)) {
			logger.debug("Adding the goal node: ");
			JSONObject JSONGoal =  getAttributeAsJSONObject("goalNode");
			Integer goalId =  getAttributeAsInteger("goalId");
			Integer ouId =  getAttributeAsInteger("ouId");
			logger.debug("JSONGoal: "+JSONGoal);
			try{
				GoalNode gn = insertGoalNode(JSONGoal, null, goalId, ouId);
				JSONObject jo = (JSONObject) SerializerFactory.getSerializer("application/json").serialize(gn, getLocale());
				writeBackToClient(new JSONSuccess(jo));	
			} catch (Exception e){
				logger.debug("Error inserting the goal node");
				throw new SpagoBIServiceException(SERVICE_NAME, "Error inserting the goal node",e);
			}
			logger.debug("Node added");
		}
		if (serviceType != null && serviceType.equalsIgnoreCase(INSERT_GOAL_DETAILS)) {
			logger.debug("Adding the details of the goal node: ");
			List<GoalKpi> kpisList = new ArrayList<GoalKpi>();
			JSONObject JSONGoalDetails =  getAttributeAsJSONObject("goalDetails");
			JSONObject goalNode =  JSONGoalDetails.optJSONObject("goalNode");
			Integer goalNodeId =  goalNode.optInt("id");
			String goalDesc =  goalNode.optString("goalDesc");
			String goalName =  goalNode.optString("name");
			logger.debug(goalNode);
			GoalNode gn = new GoalNode(goalName, "", goalDesc, null, null);
			gn.setId(goalNodeId);
			
			JSONArray kpis =  JSONGoalDetails.optJSONArray("kpis");
			try{
				for(int i=0; i<kpis.length(); i++){
					kpisList.add(deserializeKpiNode((JSONObject)kpis.get(i), goalNodeId));
				}
				daoGoal.ereseGoalKpis(goalNodeId);
				daoGoal.insertGoalKpis(kpisList, goalNodeId);
				daoGoal.updateGoalNode(gn);
				writeBackToClient(new JSONAcknowledge());	
			} catch (Exception e){
				logger.debug("Error adding the details to the goal node");
				throw new SpagoBIServiceException(SERVICE_NAME, "Error adding the details to the goal node",e);
			}
			logger.debug("Added the goal details");
		}
		if (serviceType != null && serviceType.equalsIgnoreCase(UPDATE_GOAL_NAME)) {
			logger.debug("Updating the name of the goal with id:");
			Integer goalNode =  getAttributeAsInteger("goalId");
			logger.debug("id:"+goalNode);
			String goalName =  getAttributeAsString("newName");
			
			daoGoal.updateGoalName(goalNode, goalName);
			try{
				writeBackToClient(new JSONAcknowledge());
			} catch (IOException e){
				logger.debug("Error sending the response after the erese of the goal node");
				throw new SpagoBIServiceException(SERVICE_NAME, "Error sending the response after the erese of the goal node",e);
			}
			logger.debug("Goal name updated");
		}
		if (serviceType != null && serviceType.equalsIgnoreCase(ERESE_GOAL_NODE)) {
			logger.debug("Removing the goal node with id:");
			Integer goalNode =  getAttributeAsInteger("id");
			logger.debug("id:"+goalNode);
			daoGoal.ereseGoalNode(goalNode);
			try{
				writeBackToClient(new JSONAcknowledge());
			} catch (IOException e){
				logger.debug("Error sending the response after the erese of the goal node");
				throw new SpagoBIServiceException(SERVICE_NAME, "Error sending the response after the erese of the goal node",e);
			}
			logger.debug("Goal removed");
		}
		if (serviceType != null && serviceType.equalsIgnoreCase(KPI_GOAL_ROOT_NODE)) {
			logger.debug("Loading the kpi model instances linked to the goal: ");
			Integer grantId =  getAttributeAsInteger("grantId");
			Integer goalNodeId =  getAttributeAsInteger("goalNodeId");
			Integer ouNodeId =  getAttributeAsInteger("ouNodeId");
			logger.debug("grantId: "+grantId+", goalNodeId"+goalNodeId);
			OrganizationalUnitGrant grant = DAOFactory.getOrganizationalUnitDAO().getGrant(grantId);
			ModelInstance mi = grant.getModelInstance();
			
			List<OrganizationalUnitNodeWithGrant> ousWithGrants = DAOFactory.getOrganizationalUnitDAO().getGrantNodes(ouNodeId, grantId);
			List<OrganizationalUnitGrantNode> grants = new ArrayList<OrganizationalUnitGrantNode>();
			for(int i=0; i<ousWithGrants.size(); i++){
				grants.addAll(ousWithGrants.get(i).getGrants());
			}
			List<Integer> modelInstances = new ArrayList<Integer>();
			
			for(int i=0; i<grants.size(); i++){
				modelInstances.add(grants.get(i).getModelInstanceNode().getModelInstanceNodeId());
			}
			
			if(modelInstances.contains(mi.getId())){
				mi.setActive(true);
			}
			
			try {
				JSONObject modelInstanceJSON = (JSONObject) SerializerFactory.getSerializer("application/json").serialize( mi, null);
				
				if(goalNodeId!=null){
					List<GoalKpi> listGoalKpi = daoGoal.getGoalKpi(goalNodeId);
					for(int i=0; i<listGoalKpi.size(); i++){
						if(listGoalKpi.get(i).getModelInstanceId().equals( mi.getId())){
							modelInstanceJSON.put("weight1", ""+listGoalKpi.get(i).getWeight1());
							modelInstanceJSON.put("weight2", ""+listGoalKpi.get(i).getWeight2());
							modelInstanceJSON.put("sign1", ""+listGoalKpi.get(i).getSign1());
							if(listGoalKpi.get(i).getSign1()==110){
								modelInstanceJSON.put("threshold1", "");
							}else{
								modelInstanceJSON.put("threshold1", ""+listGoalKpi.get(i).getThreshold1());
							}
							modelInstanceJSON.put("sign2", ""+listGoalKpi.get(i).getSign2());
							if(listGoalKpi.get(i).getSign2()==110){
								modelInstanceJSON.put("threshold2", "");
							}else{
								modelInstanceJSON.put("threshold2", ""+listGoalKpi.get(i).getThreshold2());
							}
							break;
						}
					}
				}
				writeBackToClient( new JSONSuccess( modelInstanceJSON ) );
			} catch (Exception e) {
				logger.debug("Impossible to serialize the responce to the client");
				throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to serialize the responce to the client", e);
			}
			logger.debug("kpi model instances loaded.");
		}
	
		
		if (serviceType != null && serviceType.equalsIgnoreCase(GOAL_ERESE)) {
			logger.debug("Removing the goal with id:");
			Integer goalId = getAttributeAsInteger("goalId");
			logger.debug(goalId);
			logger.debug(goalId);
			daoGoal.eraseGoal(goalId);
			try{
				writeBackToClient( new JSONAcknowledge() );
			} catch (IOException e){
				logger.debug("Error sending the response after the erese of the goal");
				throw new SpagoBIServiceException(SERVICE_NAME, "Error sending the response after the erese of the goal",e);
			}
			logger.debug("Goal removed");
		}
		if (serviceType != null && serviceType.equalsIgnoreCase(GOAL_INSERT)) {
			logger.debug("Adding the goal: ");
			JSONObject goalJSON = getAttributeAsJSONObject(GOAL);
			logger.debug(goalJSON);
			try{
				Goal goal = deserializeGoal(goalJSON);
				daoGoal.insertGoal(goal);
				writeBackToClient( new JSONAcknowledge() );
			} catch (Exception e){
				logger.debug("Error inserting the goal");
				throw new SpagoBIServiceException(SERVICE_NAME, "Error inserting the goal",e);
			}
			logger.debug("Goal added");
		}
		if (serviceType != null && serviceType.equalsIgnoreCase(GOALS_LIST)) {
			logger.debug("Getting the list of the goals");
			try{
			List<Goal> goals = daoGoal.getGoalsList();
			JSONArray jo = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(goals,	getLocale());
			JSONObject grantsJSONObject = new JSONObject();
			grantsJSONObject.put("rows",jo);
			writeBackToClient(new JSONSuccess(grantsJSONObject));	
			} catch (Exception e){
				logger.debug("Error getting the goals list");
				throw new SpagoBIServiceException(SERVICE_NAME, "Error getting the goals list",e);
			}
			logger.debug("List loaded");
		}
		
	}
	
	private Goal deserializeGoal(JSONObject JSONGoal) throws Exception{
		String name = (String) JSONGoal.opt(NAME);
		Integer id = null;
		try {
			id =  JSONGoal.getInt(ID);
		} catch (JSONException e) {	
			//the count still be null
		}
		String description = (String) JSONGoal.opt(DESCRIPTION);
		String start_date = (String) JSONGoal.opt(START_DATE);
		String end_date = (String) JSONGoal.opt(END_DATE);
		Integer grant =JSONGoal.optInt(GRANT);
		String label = (String) JSONGoal.opt(LABEL);
		Goal g = new Goal(id, toDate(start_date), toDate(end_date), name, label, description, grant);
		return g;
	}
	
	
	private GoalNode insertGoalNode(JSONObject goal, JSONArray kpis, Integer goalId, Integer ou) throws Exception{
		
		GoalNode gn = deserializeGoalNode(goal,goalId,ou);
		daoGoal.insertGoalNode(gn, gn.getFatherCountId());
		if(kpis!=null){
			insertKpiNodesArray(kpis, gn.getId());
		}
		return gn;
	}

	
	private GoalNode deserializeGoalNode(JSONObject goalNode, Integer goalId, Integer ou) throws Exception{
		String name = (String) goalNode.opt(NAME);
		String goalDescr = (String) goalNode.opt(GOAL_DESC);
		if(goalDescr==null){
			goalDescr = "";
		}
		Integer fatherCount = null;
		try {
			fatherCount = goalNode.getInt(GOAL_FATHER);
		} catch (JSONException e) {	
			//the count still be null
		}
		GoalNode gn = new GoalNode(name, "", goalDescr, goalId, ou);
		gn.setFatherCountId(fatherCount);
		return gn;
	}
	
	private void insertKpiNodesArray(JSONArray kpis, Integer goalNodeId) throws Exception{
		List<GoalKpi> goalKpis = new ArrayList<GoalKpi>();
		for(int i=0;i<kpis.length(); i++){
			goalKpis.add(deserializeKpiNode(kpis.getJSONObject(i),goalNodeId));
		}
		daoGoal.ereseGoalKpis(goalNodeId);
		daoGoal.insertGoalKpis(goalKpis, goalNodeId);
	}
	
	private GoalKpi deserializeKpiNode(JSONObject JSONGoal, Integer goalNodeId) throws Exception{
		Double threshold1 = null;
		Double threshold2 = null;
		Integer modelInstance = JSONGoal.optInt(MODELINSTID);
		Double weight1 = JSONGoal.optDouble(WEIGHT1);
		Double weight2 = JSONGoal.optDouble(WEIGHT2);
		if(JSONGoal.optString(THRESHOLD1)==null || JSONGoal.optString(THRESHOLD1).length()!=0){
			threshold1 = JSONGoal.optDouble(THRESHOLD1);
		}
		if(JSONGoal.optString(THRESHOLD2)==null || JSONGoal.optString(THRESHOLD2).length()!=0){
			threshold2 = JSONGoal.optDouble(THRESHOLD2);
		}
		Integer sign1 = JSONGoal.optInt(SIGN1);
		Integer sign2 = JSONGoal.optInt(SIGN2);
		Integer id =  JSONGoal.optInt(ID);
		GoalKpi gk = new GoalKpi(modelInstance, weight1, weight2, threshold1, threshold2, sign1, sign2, id, goalNodeId);
		return gk;
	}

	
	private Date toDate(String dateStr) throws Exception {
		SimpleDateFormat dateFormat = new SimpleDateFormat();
		Date date = null;
		dateFormat.applyPattern("yyyy-MM-dd");
		dateFormat.setLenient(false);
		date = dateFormat.parse(dateStr);
		return date;
	}
	
}
