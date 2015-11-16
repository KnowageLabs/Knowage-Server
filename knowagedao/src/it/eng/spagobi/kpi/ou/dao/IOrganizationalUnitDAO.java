/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.ou.dao;

import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnit;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnitGrant;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnitGrantNode;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnitHierarchy;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnitNode;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnitNodeWithGrant;

import java.util.Date;
import java.util.List;


public interface IOrganizationalUnitDAO extends ISpagoBIDao {

	/**
	 * @return the list of OU
	 */
	public List<OrganizationalUnit> getOrganizationalUnitList();
	
	/**
	 * @return the OU with the given identifier
	 */
	public OrganizationalUnit getOrganizationalUnit(Integer id);
	/**
	 * @return the OU with the given label and name (unique key)
	 */
	public OrganizationalUnit getOrganizationalUnitByLabelAndName(String label, String name);	
	/**
	 * Removes the organizational unit
	 */
	public void eraseOrganizationalUnit(Integer ouId);
	
	/**
	 * Inserts the organizational unit
	 */
	public void insertOrganizationalUnit(OrganizationalUnit ou);
	
	/**
	 * Modifies the organizational unit
	 */
	public void modifyOrganizationalUnit(OrganizationalUnit ou);
	
	/**
	 * @return the list of hierarchies
	 */
	public List<OrganizationalUnitHierarchy> getHierarchiesList();
	
	/**
	 * @return the Hierarchy with the given identifier
	 */
	public OrganizationalUnitHierarchy getHierarchy(Integer id);
	
	/**
	 * Removes the hierarchy
	 */
	public void eraseHierarchy(Integer hierarchyId);
	
	/**
	 * Inserts the hierarchy
	 */
	public void insertHierarchy(OrganizationalUnitHierarchy h);
	
	/**
	 * Modifies the hierarchy
	 */
	public void modifyHierarchy(OrganizationalUnitHierarchy h);
	
	
	public boolean isInAHierarchy(OrganizationalUnit ou);
	
	/**
	 * @return the root nodes for a single hierarchy
	 */
	public List<OrganizationalUnitNode> getRootNodes(Integer hierarchyId);
	
	/**
	 * @return the root nodes (with grants) for a single hierarchy
	 */
	public List<OrganizationalUnitNodeWithGrant> getRootNodesWithGrants(Integer hierarchyId, Integer grantId);
	
	/**
	 * @return the list of children nodes for a single node of a hierarchy
	 */
	public List<OrganizationalUnitNode> getChildrenNodes(Integer nodeId);
	
	/**
	 * @return the list of children nodes for a single node of a hierarchy
	 */
	public List<OrganizationalUnitNodeWithGrant> getChildrenNodesWithGrants(Integer nodeId, Integer grantId);
	
	/**
	 * @return the list of grants (i.e. association between a KPI model instance and a OU hierarchy)
	 */
	public List<OrganizationalUnitGrant> getGrantsList();
	
	/**
	 * Inserts a new grant
	 * @param grant
	 */
	public void insertGrant(OrganizationalUnitGrant grant);
	
	/**
	 * Modify a grant
	 * @param grant
	 */
	public void modifyGrant(OrganizationalUnitGrant grant);
	
	/**
	 * Removes a grant
	 * @param grantId The grant identifier
	 */
	public void eraseGrant(Integer grantId);
	
	/**
	 * @return the list of grants for a single node of a hierarchy (i.e. association between a KPI model instance node and a OU hierarchy node)
	 */
	public List<OrganizationalUnitGrantNode> getNodeGrants(Integer nodeId, Integer grantId);
	
	/**
	 * @return true if the input node has a grant 
	 */
	public boolean hasGrants(OrganizationalUnitNode node);
	
	/**
	 * @return true if the input hierarchy has a grant 
	 */
	public boolean hasGrants(OrganizationalUnitHierarchy h);
	
	/**
	 * Inserts a list of grant nodes (a grant node is an association between a hierarchy node and a KPI model instance node 
	 * in the context of a grant)
	 * @param grantNodes
	 */
	public void insertNodeGrants(List<OrganizationalUnitGrantNode> grantNodes, Integer grantId);
	
	/**
	 * Remove all the grant nodes of a grant(a grant node is an association between a hierarchy node and a KPI model instance node 
	 * in the context of a grant)
	 * @param grantNodes
	 */
	public void eraseNodeGrants(Integer grantId);
	
	/**
	 * Removes a node from the structure with its descendants
	 * @param node The node to be removed
	 */
	public void eraseOrganizationalUnitNode(OrganizationalUnitNode node);

	/**
	 * Checks if the input path exists in the given hierarchy
	 * @param path
	 * @param hierarchyId The hierarchy identifier
	 * @return true if the path exists in the given hierarchy, false otherwise
	 */
	public boolean existsNodeInHierarchy(String path, Integer hierarchyId);
	
	/**
	 * Retrieve the node with the input path in the given hierarchy
	 * @param path
	 * @param hierarchyId The hierarchy identifier
	 * @return the node with the input path in the given hierarchy
	 */
	public OrganizationalUnitNode getOrganizationalUnitNode(String path, Integer hierarchyId);

	/**
	 * Inserts the input node in the hierarchy
	 * @param aNode
	 */
	public void insertOrganizationalUnitNode(OrganizationalUnitNode aNode);
	
	/**
	 * Retrieves the grants associated the KPI model instance node identified by the input integer
	 * @param kpiModelInstanceId
	 * @return the grants associated the KPI model instance node identified by the input integer
	 */
	public List<OrganizationalUnitGrantNode> getGrants(Integer kpiModelInstanceId);
	
	/**
	 * Retrieves all the grant nodes associated to an ou and a grant
	 * @param ouNodeId
	 * @param grantId
	 * @return the grant nodes associated to an ou and a grant
	 */
	public List<OrganizationalUnitNodeWithGrant> getGrantNodes(Integer ouNodeId, Integer grantId);
	
	/**
	 * Retrieves the grant with id grantId
	 * @param grantId the id of the grant
	 * @return the grant with grantId
	 */
	public OrganizationalUnitGrant getGrant(Integer grantId);
	
	
	/**
	 * Retrieves the grant by its label
	 * @param label the label of the grant
	 * @return the grant
	 */
	public OrganizationalUnitGrant loadGrantByLabel(String label);
	
	/**
	 * Retrieves the grants associated the KPI model instance node identified by the input integer,
	 * valid in the real time
	 * @param kpiModelInstanceId
	 * @param now
	 * @return the grants associated the KPI model instance node identified by the input integer
	 */
	public List<OrganizationalUnitGrantNode> getGrantsValidByDate(Integer kpiModelInstanceId, Date now);
	/**
	 * Retrieves all ou nodes
	 * @return all the ou nodes
	 */
	public List<OrganizationalUnitNode> getNodes();
	/**
	 * Remove the grant nodes of a grant 
	 * @param grantNode to erase
	 */
	public void eraseNodeGrant(OrganizationalUnitGrantNode grantNode);
	
	public List<OrganizationalUnitNode> getOrganizationalUnitNodeList(Integer hierarchyId);
}
