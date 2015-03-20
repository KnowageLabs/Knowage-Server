/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.goal.dao;

import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.kpi.goal.metadata.bo.Goal;
import it.eng.spagobi.kpi.goal.metadata.bo.GoalKpi;
import it.eng.spagobi.kpi.goal.metadata.bo.GoalNode;

import java.util.List;


public interface IGoalDAO extends ISpagoBIDao{
	/**
	 * Loading the list of the goals
	 * @return
	 */
	public List<Goal> getGoalsList();
	
	/**
	 * Remove a goal
	 * @param grantId the id of the goal to remove
	 */
	public void eraseGoal(Integer grantId);
	
	/**
	 * Insert a goal
	 * @param goal the goal to insert
	 */
	public void insertGoal(Goal goal);
	
	/**
	 * Get the root node of a goal hierarchy linked to an ou and a grant
	 * @param goalId the goal id
	 * @param ouId the id of the ou node 
	 * @return the root
	 */
	public GoalNode getRootNode(Integer goalId, Integer ouId);
	
	/**
	 * Get the children of a goal node
	 * @param nodeId the id of the node
	 * @return
	 */
	public List<GoalNode> getChildrenNodes(Integer nodeId);
	
	/**
	 * Insert a goal node
	 * @param goalNode the goal node
	 * @param fatherId the id of the father node (can be null)
	 */
	public void insertGoalNode(GoalNode goalNode, Integer fatherId);
	
	/**
	 * Remove a goal node
	 * @param goalNodeId the id of the goal node to remove
	 */
	public void ereseGoalNode(Integer goalNodeId);
	
	/**
	 * Update a goal node
	 * @param goalNode the goal node to update
	 */
	public void updateGoalNode(GoalNode goalNode);
	
	/**
	 * Update a goal node
	 * @param goalId the id of the goal
	 * @param newName the new name of the goal
	 */
	public void updateGoalName(Integer goalId, String newName);
	
	/**
	 * Insert a list of goal kpi model instances
	 * @param goalKpis the list to add
	 * @param goalNodeId the id of the goal node linked to the kpi model instances
	 */
	public void insertGoalKpis(List<GoalKpi> goalKpis, Integer goalNodeId);
	
	/**
	 * get the list of kpi model instances linked to a goalNode
	 * @param goalNodeId the id of the goal node
	 * @return
	 */
	public List<GoalKpi> getGoalKpi(Integer goalNodeId);
	
	/**
	 * Remove the list of kpi model instances linked to a goalNode
	 * @param goalNodeId the id of the goal node
	 */
	public void ereseGoalKpis(Integer goalNodeId);
	
}
