/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.kpi.ou.util;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnit;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnitHierarchy;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnitNode;
import it.eng.spagobi.kpi.ou.dao.OrganizationalUnitDAOImpl;
import it.eng.spagobi.kpi.ou.provider.OrganizationalUnitListProvider;
import it.eng.spagobi.utilities.tree.Node;
import it.eng.spagobi.utilities.tree.Tree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;


/**
 * This class synchronizes OU list, hierarchies list and hierarchies structure.
 * It retrieves information by a <i>it.eng.spagobi.kpi.ou.provider.OrganizationalUnitListProvider</i>.
 * Inside an instance of class <i>it.eng.spagobi.kpi.ou.bo.OrganizationalUnit</i> retrieved by the OrganizationalUnitListProvider, the "id"
 * property does not make sense, since it does not match the "id" in the SpagoBI repository. An existing OU is matched to a OU 
 * coming from the OrganizationalUnitListProvider if it has the same label; in this case, we set the id coming from SpagoBI repository.
 * For hierarchies it is the same.
 * For hierarchies structure, we consider the path of each node: if an existing path does no more exists, it is deleted (with its descendants); 
 * if a new path does not exist, it is inserted (recursively).
 * 
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class OrganizationalUnitSynchronizer {
	
	static private Logger logger = Logger.getLogger(OrganizationalUnitSynchronizer.class);
	
	private OrganizationalUnitDAOImpl dao = null;
	
	public OrganizationalUnitSynchronizer(IEngUserProfile userProfile) {
		dao = new OrganizationalUnitDAOImpl();
		dao.setUserProfile(userProfile);
	}
	
	public List<OrganizationalUnitHierarchy> synchronize() {
		logger.debug("IN");
        try {
        	OrganizationalUnitListProvider provider = getProvider();
        	logger.debug("OrganizationalUnitListProvider retrieved: " + provider);
        	provider.initialize();
        	logger.debug("Provider Initialized");
        	adjustHierarchies(provider);
        	logger.debug("Hierarchies' names adjusted");
        	
        	// retrieve new and old OU
    		List<OrganizationalUnit> newOUs = provider.getOrganizationalUnits();
    		logger.debug("Organizational Units retrieved by the provider:");
    		logger.debug(newOUs);
    		List<OrganizationalUnit> oldOUs = dao.getOrganizationalUnitList();
    		logger.debug("Current Organizational Units in repository:");
    		logger.debug(oldOUs);
    		// retrieve new and old hierarchies
    		List<OrganizationalUnitHierarchy> newHierarchies = provider.getHierarchies();
    		logger.debug("Hierarchies retrieved by the provider:");
    		logger.debug(newHierarchies);
    		List<OrganizationalUnitHierarchy> oldHierarchies = dao.getHierarchiesList();
    		logger.debug("Current Hierarchies in repository:");
    		logger.debug(oldHierarchies);
    		
    		modifyExistingOUs(newOUs, oldOUs);
    		logger.debug("Existing OUs modified");
    		insertNewOUs(newOUs, oldOUs);
    		logger.debug("New OUs inserted");
    		List<OrganizationalUnit> updatedOUs = dao.getOrganizationalUnitList();
    		logger.debug("Updated Organizational Units in repository:");
    		logger.debug(updatedOUs);

    		modifyExistingHierarchies(newHierarchies, oldHierarchies);
    		logger.debug("Existing hierarchies modified");
    		insertNewHierarchies(newHierarchies, oldHierarchies);
        	logger.debug("New hierarchies inserted");
    		List<OrganizationalUnitHierarchy> updatedHierarchies = dao.getHierarchiesList();
    		logger.debug("Updated hierarchies in repository:");
    		logger.debug(updatedHierarchies);
        	
        	List<OrganizationalUnitHierarchy> list = synchronizeHierarchiesStructure(provider);
        	logger.debug("Hierarchies' structure updated");
        	if (!list.isEmpty()) {
        		logger.error("The following hierarchies were not been updated: " + list);
        	}

        	removeNoMoreExistingHierarchies(newHierarchies, updatedHierarchies);
        	removeNoMoreExistingOUs(newOUs, updatedOUs);
        	
        	return list;
        	
		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * Adjust hierarchies' label in order to have the label starting with the company name
	 * @param provider The Organizational Units info provider
	 */
	private void adjustHierarchies(OrganizationalUnitListProvider provider) {
		logger.debug("IN: provider = " + provider);
		List<OrganizationalUnitHierarchy> newHierarchies = provider.getHierarchies();
		logger.debug("Hierarchies retrieved by the provider:");
		logger.debug(newHierarchies);
		Iterator<OrganizationalUnitHierarchy> it = newHierarchies.iterator();
		while (it.hasNext()) {
			OrganizationalUnitHierarchy h = it.next();
			String label = h.getLabel();
			String company = h.getCompany();
			logger.debug("Hierarchy label = [" + label + "], company = [" + company + "]");
			if (company != null && !company.trim().equals("") && !label.startsWith(company + " - ")) {
				h.setLabel(company + " - " + label);
				logger.info("Hierarchy label modified : new label is [" + h.getLabel() + "]");
			}
		}
		logger.debug("OUT");
	}

	private List<OrganizationalUnitHierarchy> synchronizeHierarchiesStructure(
			OrganizationalUnitListProvider provider) {
		logger.debug("IN: provider = " + provider);
		List<OrganizationalUnitHierarchy> toReturn = new ArrayList<OrganizationalUnitHierarchy>();
		List<OrganizationalUnitHierarchy> hierarchies = dao.getHierarchiesList();
		Iterator<OrganizationalUnitHierarchy> it = hierarchies.iterator();
		while (it.hasNext()) {
			OrganizationalUnitHierarchy hierarchy = it.next();
			boolean success = synchronizeHierarchyStructure(provider, hierarchy);
			if (!success) {
				toReturn.add(hierarchy);
			}
		}
		logger.debug("OUT");
		return toReturn;
	}

	private boolean synchronizeHierarchyStructure(
			OrganizationalUnitListProvider provider,
			OrganizationalUnitHierarchy hierarchy) {
		logger.debug("IN");
		boolean success = true;
		List<Tree<OrganizationalUnit>> list = provider.getHierarchyStructure(hierarchy);
		logger.debug("Tree structure for hierarchy " + hierarchy + ":");
		logger.debug(list);
		if (list != null ) {
			success = synchronizeHierarchyStructure(list, hierarchy);
			logger.debug("Structure of hierarchy " + hierarchy + " synchronized");
		}
		logger.debug("OUT : returning " + success);
		return success;
	}
	
	private boolean synchronizeHierarchyStructure(List<Tree<OrganizationalUnit>> list, OrganizationalUnitHierarchy hierarchy) {
		logger.debug("IN");
		String hierarchyName = hierarchy.getName();
		Session session = null;
		Transaction tx = null;
		boolean success = true;
		try  {
			session = ((AbstractHibernateDAO) dao).getSession();
			tx = session.beginTransaction();
			
			Iterator<Tree<OrganizationalUnit>> it = list.iterator();
			while (it.hasNext()) {
				Tree<OrganizationalUnit> tree = it.next();
				success = synchronizeHierarchyStructure(tree, hierarchy, session);
				if (success) {
					// if synchronization was successful then commit
					((AbstractHibernateDAO) dao).commitIfActiveAndClose(tx, session);
				} else {
					// if synchronization wasn't successful then rollbak
					((AbstractHibernateDAO) dao).rollbackIfActiveAndClose(tx, session);
				}
			}
		} catch (Throwable t) {
			logger.error("Error while synchronizing hierarchy structure for hierarchy " + hierarchyName, t);
			((AbstractHibernateDAO) dao).rollbackIfActiveAndClose(tx, session);
		}
		logger.debug("OUT : returning " + success);
		
		return success;
	}
	
	private boolean synchronizeHierarchyStructure(Tree<OrganizationalUnit> tree, OrganizationalUnitHierarchy hierarchy, Session session) {
		logger.debug("IN");
		boolean success = true;
		success = removeNoMoreExistingNodes(tree, hierarchy, session);
		insertNewNodes(tree, hierarchy, session);
		logger.debug("OUT : returning " + success);
		return success;
	}

	private void insertNewNodes(Tree<OrganizationalUnit> tree, OrganizationalUnitHierarchy hierarchy, Session session) {
		logger.debug("IN");
		Node<OrganizationalUnit> root = tree.getRoot();
		insertNewNodes(root, hierarchy, session);
		logger.debug("OUT");
	}
	
	private void insertNewNodes(Node<OrganizationalUnit> node, OrganizationalUnitHierarchy hierarchy, Session session) {
		logger.debug("IN: node = " + node + ", hierarchy = " + hierarchy);
		if (!exists(node, hierarchy, session)) {
			logger.debug("Node " + node + " does not exist in hierarchy " + hierarchy + ", it will be inserted.");
			Node<OrganizationalUnit> parent = node.getParent();
			insertNode(node, parent, hierarchy, session);
			logger.debug("Node " + node + " inserted in hierarchy " + hierarchy + ".");
		}
		// recursion on children
		List<Node<OrganizationalUnit>> children = node.getChildren();
		if (children != null && children.size() > 0) {
			Iterator<Node<OrganizationalUnit>> it = children.iterator();
			while (it.hasNext()) {
				Node<OrganizationalUnit> aChild = it.next();
				insertNewNodes(aChild, hierarchy, session);
			}
		}
		logger.debug("OUT");
	}
	
	private void insertNode(Node<OrganizationalUnit> node,
			Node<OrganizationalUnit> parent,
			OrganizationalUnitHierarchy hierarchy, Session session) {
		logger.debug("IN: node = " + node + ", parent = " + parent + ", hierarchy = " + hierarchy);
		OrganizationalUnitNode aNode = new OrganizationalUnitNode();
		aNode.setHierarchy(hierarchy);
		OrganizationalUnit content = node.getNodeContent();
		content = dao.getOrganizationalUnitByLabelAndName(content.getLabel(), content.getName());
		if (content == null) {
			//then insert it!!there could be a misalignment
			dao.insertOrganizationalUnit(node.getNodeContent());
			content = dao.getOrganizationalUnitByLabelAndName(node.getNodeContent().getLabel(), node.getNodeContent().getName());
		}
		aNode.setOu(content);
		aNode.setPath(node.getPath());
		if (parent != null) {
			OrganizationalUnitNode parentNode = dao.getOrganizationalUnitNode(parent.getPath(), hierarchy.getId(), session);
			Integer parentNodeId = parentNode.getNodeId();
			aNode.setParentNodeId(parentNodeId);
		}
		dao.insertOrganizationalUnitNode(aNode, session);
		logger.debug("OUT");
	}

	private boolean exists(Node<OrganizationalUnit> node, OrganizationalUnitHierarchy hierarchy, Session session) {
		logger.debug("IN: node = " + node + ", hierarchy = " + hierarchy);
		boolean toReturn = dao.existsNodeInHierarchy(node.getPath(), hierarchy.getId(), session);
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}
	
	private boolean removeNoMoreExistingNodes(Tree<OrganizationalUnit> tree, OrganizationalUnitHierarchy hierarchy, Session session) {
		logger.debug("IN");
		boolean toReturn = true;
		List<OrganizationalUnitNode> rootNodes = dao.getRootNodes(hierarchy.getId());
		if (rootNodes != null && !rootNodes.isEmpty()) {
			toReturn = removeNoMoreExistingNodes(tree, rootNodes, hierarchy, session);
		}
		logger.debug("OUT : returning " + toReturn);
		return toReturn;
	}
	
	private boolean removeNoMoreExistingNodes(Tree<OrganizationalUnit> tree,
			List<OrganizationalUnitNode> nodes,
			OrganizationalUnitHierarchy hierarchy, Session session) {
		logger.debug("IN");
		boolean success = true;
		Iterator<OrganizationalUnitNode> it = nodes.iterator();
		while (it.hasNext()) {
			OrganizationalUnitNode aNode = it.next();
			logger.debug("Examining node " + aNode + " ....");
			if (tree.containsPath(aNode.getPath())) {
				logger.debug("Node " + aNode + " exists in hierarchy "
						+ hierarchy + ".");
				// recursion
				List<OrganizationalUnitNode> children = dao.getChildrenNodes(
								aNode.getNodeId());
				// if success if false, it must remain false, therefore we put success = success && ...
				success = success && removeNoMoreExistingNodes(tree, children, hierarchy, session);
			} else {
				boolean hasGrants = dao.hasGrants(aNode);
				if (hasGrants) {
					logger.error("Node " + aNode
							+ " does no more exists but is has grants. Cannot remove it");
					success = false;
				} else {
					logger.debug("Node " + aNode
							+ " does no more exists. Removing it ....");
					dao.eraseOrganizationalUnitNode(aNode, session);
					logger.debug("Node " + aNode + " removed.");
				}
			}
		}
		logger.debug("OUT : returning " + success);
		return success;
	}

	/**
	 * Synchronizes hierarchies' list by removing no more existing hierarchies and inserting new ones
	 * @param provider The Organizational Units info provider
	 */
//	private void synchronizeHierarchies(OrganizationalUnitListProvider provider) {
//		logger.debug("IN: provider = " + provider);
//		List<OrganizationalUnitHierarchy> newHierarchies = provider.getHierarchies();
//		logger.debug("Hierarchies retrieved by the provider:");
//		logger.debug(newHierarchies);
//		List<OrganizationalUnitHierarchy> oldHierarchies = dao.getHierarchiesList();
//		logger.debug("Current Hierarchies in repository:");
//		logger.debug(oldHierarchies);
//		removeNoMoreExistingHierarchies(newHierarchies, oldHierarchies);
//		modifyExistingHierarchies(newHierarchies, oldHierarchies);
//		insertNewHierarchies(newHierarchies, oldHierarchies);
//		logger.debug("OUT");
//	}

	/**
	 * Synchronizes OU list by removing no more existing OUs and inserting new ones
	 * @param provider The Organizational Units info provider
	 */
//	private void synchronizeOU(OrganizationalUnitListProvider provider) {
//		logger.debug("IN: provider = " + provider);
//		List<OrganizationalUnit> newOUs = provider.getOrganizationalUnits();
//		logger.debug("Organizational Units retrieved by the provider:");
//		logger.debug(newOUs);
//		List<OrganizationalUnit> oldOUs = dao.getOrganizationalUnitList();
//		logger.debug("Current Organizational Units in repository:");
//		logger.debug(oldOUs);
//		removeNoMoreExistingOUs(newOUs, oldOUs);
//		modifyExistingOUs(newOUs, oldOUs);
//		insertNewOUs(newOUs, oldOUs);
//		logger.debug("OUT");
//	}
	
	private void removeNoMoreExistingOUs(List<OrganizationalUnit> newOUs, List<OrganizationalUnit> oldOUs) {
		logger.debug("IN");
		Iterator<OrganizationalUnit> it = oldOUs.iterator();
		while (it.hasNext()) {
			OrganizationalUnit ou = it.next();
			if (!newOUs.contains(ou)) {
				boolean isInHierarchy = dao.isInAHierarchy(ou);
				if (isInHierarchy) {
					logger.warn("Organizational Unit [" + ou + "] does no more exists but it is used in a hierarchy, cannot remove it!");
				} else {
					logger.debug("OU " + ou + " does no more exists. Removing it ...");
					dao.eraseOrganizationalUnit(ou.getId());
					logger.debug("OU " + ou + " removed.");
				}
			}
		}
		logger.debug("OUT");
	}
	
	private void modifyExistingOUs(List<OrganizationalUnit> newOUs, List<OrganizationalUnit> oldOUs) {
		logger.debug("IN");
		Iterator<OrganizationalUnit> it = oldOUs.iterator();
		while (it.hasNext()) {
			OrganizationalUnit ou = it.next();
			int index = newOUs.indexOf(ou);
			if (index >= 0) {
				OrganizationalUnit newOU = newOUs.get(index);
				if (!newOU.deepEquals(ou)) {
					logger.debug("OU " + ou + " has been changed. Updating it ...");
					ou.setName(newOU.getName());
					ou.setDescription(newOU.getDescription());
					dao.modifyOrganizationalUnit(ou);
					logger.debug("OU updated: " + ou);
				}
				newOU.setId(ou.getId()); // setting the current OU id
				logger.debug("OU id updated: " + newOU);
			}
		}
		logger.debug("OUT");
	}
	
	private void insertNewOUs(List<OrganizationalUnit> newOUs, List<OrganizationalUnit> oldOUs) {
		logger.debug("IN");
		Iterator<OrganizationalUnit> it = newOUs.iterator();
		while (it.hasNext()) {
			OrganizationalUnit ou = it.next();
			if (!oldOUs.contains(ou)) {
				logger.debug("OU " + ou + " does not exists. Inserting it ...");
				dao.insertOrganizationalUnit(ou);
				logger.debug("OU inserted: " + ou);
			}
		}
		logger.debug("OUT");
	}
	
	private void removeNoMoreExistingHierarchies(List<OrganizationalUnitHierarchy> newHierarchies, List<OrganizationalUnitHierarchy> oldHierarchies) {
		logger.debug("IN");
		Iterator<OrganizationalUnitHierarchy> it = oldHierarchies.iterator();
		while (it.hasNext()) {
			OrganizationalUnitHierarchy h = it.next();
			if (!newHierarchies.contains(h)) {
				boolean hasGrant = dao.hasGrants(h);
				if (hasGrant) {
					logger.error("Hierarchy [" + h + "] does no more exists but it has some grants, cannot remove it!");
				} else {
					logger.debug("Hierarchy " + h + " does no more exists. Removing it ...");
					dao.eraseHierarchy(h.getId());
					logger.debug("Hierarchy " + h + " removed.");
				}
			}
		}
		logger.debug("OUT");
	}
	
	private void modifyExistingHierarchies(List<OrganizationalUnitHierarchy> newHierarchies, List<OrganizationalUnitHierarchy> oldHierarchies) {
		logger.debug("IN");
		Iterator<OrganizationalUnitHierarchy> it = oldHierarchies.iterator();
		while (it.hasNext()) {
			OrganizationalUnitHierarchy h = it.next();
			int index = newHierarchies.indexOf(h);
			if (index >= 0) {
				OrganizationalUnitHierarchy newHierarchy = newHierarchies.get(index);
				if (!newHierarchy.deepEquals(h)) {
					logger.debug("Hierarchy" + h + " has been changed. Updating it ...");
					h.setName(newHierarchy.getName());
					h.setDescription(newHierarchy.getDescription());
					h.setTarget(newHierarchy.getTarget());
					h.setCompany(newHierarchy.getCompany());
					dao.modifyHierarchy(h);
					logger.debug("Hierarchy updated: " + h);
				}
				newHierarchy.setId(h.getId()); // setting the current hierarchy id
				logger.debug("Hierarchy id updated: " + newHierarchy);
			}
		}
		logger.debug("OUT");
	}
	
	private void insertNewHierarchies(List<OrganizationalUnitHierarchy> newHierarchies, List<OrganizationalUnitHierarchy> oldHierarchies) {
		logger.debug("IN");
		Iterator<OrganizationalUnitHierarchy> it = newHierarchies.iterator();
		while (it.hasNext()) {
			OrganizationalUnitHierarchy h = it.next();
			if (!oldHierarchies.contains(h)) {
				logger.debug("Hierarchy " + h + " does not exists. Inserting it ...");
				dao.insertHierarchy(h);
				logger.debug("Hierarchy inserted: " + h);
			}
		}
		logger.debug("OUT");
	}

	private OrganizationalUnitListProvider getProvider() {
		logger.debug("IN");
		OrganizationalUnitListProvider o = null;
		try {
			logger.debug("Instantiating provider ...");
			String prodiverClassName = SingletonConfig.getInstance().getConfigValue("SPAGOBI.ORGANIZATIONAL-UNIT.provider");
			o = (OrganizationalUnitListProvider) Class.forName(prodiverClassName).newInstance();
			logger.debug("Provider instantiated succesfully");
			logger.debug("OUT");
		} catch (Exception e) {
			logger.error("Cannot get Organizational Unit list provider class", e);
			throw new RuntimeException("Cannot get Organizational Unit list provider class", e);
		}
		return o;
	}

}
