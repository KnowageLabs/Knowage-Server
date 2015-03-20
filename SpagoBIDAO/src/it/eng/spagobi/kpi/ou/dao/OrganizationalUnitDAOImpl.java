/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.ou.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.kpi.model.bo.ModelInstance;
import it.eng.spagobi.kpi.model.bo.ModelInstanceNode;
import it.eng.spagobi.kpi.model.dao.ModelInstanceDAOImpl;
import it.eng.spagobi.kpi.model.metadata.SbiKpiModelInst;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnit;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnitGrant;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnitGrantNode;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnitHierarchy;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnitNode;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnitNodeWithGrant;
import it.eng.spagobi.kpi.ou.metadata.SbiOrgUnit;
import it.eng.spagobi.kpi.ou.metadata.SbiOrgUnitGrant;
import it.eng.spagobi.kpi.ou.metadata.SbiOrgUnitGrantNodes;
import it.eng.spagobi.kpi.ou.metadata.SbiOrgUnitGrantNodesId;
import it.eng.spagobi.kpi.ou.metadata.SbiOrgUnitHierarchies;
import it.eng.spagobi.kpi.ou.metadata.SbiOrgUnitNodes;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.tree.Tree;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * 
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class OrganizationalUnitDAOImpl extends AbstractHibernateDAO implements IOrganizationalUnitDAO {

	static private Logger logger = Logger.getLogger(OrganizationalUnitDAOImpl.class);

	public List<OrganizationalUnit> getOrganizationalUnitList() {
		logger.debug("IN");
		List<OrganizationalUnit> toReturn = new ArrayList<OrganizationalUnit>();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = aSession.createQuery(" from SbiOrgUnit");

			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();

			while (it.hasNext()) {
				toReturn.add(toOrganizationalUnit((SbiOrgUnit) it.next()));
			}
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}
	public List<OrganizationalUnitNode> getOrganizationalUnitNodeList(Integer hierarchyId) {
		logger.debug("IN");
		List<OrganizationalUnitNode> toReturn = new ArrayList<OrganizationalUnitNode>();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = aSession.createQuery(" from SbiOrgUnitNodes n where n.sbiOrgUnitHierarchies.id = ? ");
			hibQuery.setInteger(0, hierarchyId);
			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();

			while (it.hasNext()) {
				toReturn.add(toOrganizationalUnitNode((SbiOrgUnitNodes) it.next()));
			}
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}
	public List<OrganizationalUnitHierarchy> getHierarchiesList() {
		logger.debug("IN");
		List<OrganizationalUnitHierarchy> toReturn = new ArrayList<OrganizationalUnitHierarchy>();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = aSession.createQuery(" from SbiOrgUnitHierarchies");

			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();

			while (it.hasNext()) {
				toReturn.add(toOrganizationalUnitHierarchy((SbiOrgUnitHierarchies) it.next()));
			}
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}

	public OrganizationalUnit getOrganizationalUnit(Integer id) {
		logger.debug("IN: id = " + id);
		OrganizationalUnit toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiOrgUnit hibOU = (SbiOrgUnit) aSession.load(SbiOrgUnit.class, id);

			toReturn = toOrganizationalUnit(hibOU);
			
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}

	public OrganizationalUnitHierarchy getHierarchy(Integer id) {
		logger.debug("IN: id = " + id);
		OrganizationalUnitHierarchy toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiOrgUnitHierarchies hibHierarchy = (SbiOrgUnitHierarchies) aSession.load(SbiOrgUnitHierarchies.class, id);

			toReturn = toOrganizationalUnitHierarchy(hibHierarchy);
			
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}
	
	public List<OrganizationalUnitNode> getRootNodes(Integer hierarchyId) {
		logger.debug("IN: hierarchyId = " + hierarchyId);
		List<OrganizationalUnitNode> toReturn = new ArrayList<OrganizationalUnitNode>();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = aSession.createQuery(" from SbiOrgUnitNodes n where n.sbiOrgUnitHierarchies.id = ? " +
					" and n.sbiOrgUnitNodes is null");
			hibQuery.setInteger(0, hierarchyId);
			
			List roots = hibQuery.list();

			if (roots != null && !roots.isEmpty()) {
				Iterator it = roots.iterator();
				while (it.hasNext()) {
					SbiOrgUnitNodes root = (SbiOrgUnitNodes) it.next();
					toReturn.add(toOrganizationalUnitNode(root));
				}
			}
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}

	public List<OrganizationalUnitNode> getChildrenNodes(Integer nodeId) {
		logger.debug("IN: nodeId = " + nodeId);
		List<OrganizationalUnitNode> toReturn = new ArrayList<OrganizationalUnitNode>();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = aSession.createQuery(" from SbiOrgUnitNodes n where n.sbiOrgUnitNodes.nodeId = ? ");
			hibQuery.setInteger(0, nodeId);
			
			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();

			while (it.hasNext()) {
				toReturn.add(toOrganizationalUnitNode((SbiOrgUnitNodes) it.next()));
			}
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}
	
	public List<OrganizationalUnitGrant> getGrantsList() {
		logger.debug("IN");
		List<OrganizationalUnitGrant> toReturn = new ArrayList<OrganizationalUnitGrant>();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = aSession.createQuery(" from SbiOrgUnitGrant ");

			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();

			while (it.hasNext()) {
				toReturn.add(toOrganizationalUnitGrant((SbiOrgUnitGrant) it.next(), aSession));
			}
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}
	
	public OrganizationalUnitGrant getGrant(Integer grantId) {
		logger.debug("IN");
		List<OrganizationalUnitGrant> toReturn = new ArrayList<OrganizationalUnitGrant>();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = aSession.createQuery(" from SbiOrgUnitGrant s where s.id = ? ");
			hibQuery.setInteger(0, grantId);
			
			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();

			while (it.hasNext()) {
				toReturn.add(toOrganizationalUnitGrant((SbiOrgUnitGrant) it.next(), aSession));
			}
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: returning " + toReturn);
		if(toReturn.size()>0){
			return toReturn.get(0);
		}else 
			throw new SpagoBIRuntimeException("No grant found with id "+grantId);
	}
	
	
	public List<OrganizationalUnitGrantNode> getNodeGrants(Integer nodeId, Integer grantId) {
		logger.debug("IN: nodeId = " + nodeId);
		List<OrganizationalUnitGrantNode> toReturn = new ArrayList<OrganizationalUnitGrantNode>();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = aSession.createQuery(" from SbiOrgUnitGrantNodes s where s.id.nodeId = ? " +
					" and s.id.grantId = ?");
			hibQuery.setInteger(0, nodeId);
			hibQuery.setInteger(1, grantId);
			
			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();

			while (it.hasNext()) {
				toReturn.add(toOrganizationalUnitGrantNode((SbiOrgUnitGrantNodes) it.next(), aSession));
			}
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}

	
	public void eraseOrganizationalUnit(Integer ouId) {
		logger.debug("IN: ouId = " + ouId);
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			
			SbiOrgUnit hibOU = (SbiOrgUnit) aSession.load(SbiOrgUnit.class, ouId);
			aSession.delete(hibOU);
			
			tx.commit();
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: OrganizationalUnit removed successfully.");
	}

	public void insertOrganizationalUnit(OrganizationalUnit ou) {
		logger.debug("IN: ou = " + ou);
		if (ou.getLabel().contains(Tree.NODES_PATH_SEPARATOR)) 
			throw new SpagoBIRuntimeException("OrganizationalUnit label cannot contain " + Tree.NODES_PATH_SEPARATOR + " character");
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			
			SbiOrgUnit hibOU = new SbiOrgUnit();
			hibOU.setLabel(ou.getLabel());
			hibOU.setName(ou.getName());
			hibOU.setDescription(ou.getDescription());

			//look for preexisting one with same same label-name key
			Query hibQuery = aSession.createQuery(" from SbiOrgUnit s where s.name = ? and s.label = ?");
			hibQuery.setString(0, ou.getName());
			hibQuery.setString(1, ou.getLabel());
	
			SbiOrgUnit exists= (SbiOrgUnit)hibQuery.uniqueResult();
			updateSbiCommonInfo4Insert(hibOU);
			if(exists == null){
				aSession.save(hibOU);	
				tx.commit();
				ou.setId(hibOU.getId());
			}else{
				ou.setId(exists.getId());
				tx.commit();
			}
			
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: OrganizationalUnit inserted successfully with id " + ou.getId());
	}
	
	public void modifyOrganizationalUnit(OrganizationalUnit ou) {
		logger.debug("IN: ou = " + ou);
		if (ou.getLabel().contains(Tree.NODES_PATH_SEPARATOR)) 
			throw new SpagoBIRuntimeException("OrganizationalUnit label cannot contain " + Tree.NODES_PATH_SEPARATOR + " character");
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			
			SbiOrgUnit hibOU = (SbiOrgUnit) aSession.load(SbiOrgUnit.class, ou.getId());
			hibOU.setLabel(ou.getLabel());
			hibOU.setName(ou.getName());
			hibOU.setDescription(ou.getDescription());
			updateSbiCommonInfo4Update(hibOU);
			aSession.save(hibOU);
			
			tx.commit();
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: OrganizationalUnit modified successfully");
	}

	public void eraseHierarchy(Integer hierarchyId) {
		logger.debug("IN: hierarchyId = " + hierarchyId);
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiOrgUnitHierarchies hibHierarchy = (SbiOrgUnitHierarchies) aSession.load(SbiOrgUnitHierarchies.class, hierarchyId);
			aSession.delete(hibHierarchy);
			
			tx.commit();
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: Hierarchy removed successfully");
		
	}

	public void insertHierarchy(OrganizationalUnitHierarchy h) {
		logger.debug("IN: h = " + h);
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			
			SbiOrgUnitHierarchies hibHierarchy = new SbiOrgUnitHierarchies();
			hibHierarchy.setLabel(h.getLabel());
			hibHierarchy.setName(h.getName());
			hibHierarchy.setDescription(h.getDescription());
			hibHierarchy.setTarget(h.getTarget());
			hibHierarchy.setCompany(h.getCompany());
			updateSbiCommonInfo4Insert(hibHierarchy);
			aSession.save(hibHierarchy);
			
			tx.commit();
			h.setId(hibHierarchy.getId());
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: Hierarchy inserted successfully with id " + h.getId());
		
	}
	
	public void modifyHierarchy(OrganizationalUnitHierarchy h) {
		logger.debug("IN: h = " + h);
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			
			SbiOrgUnitHierarchies hibHierarchy = (SbiOrgUnitHierarchies) aSession.load(SbiOrgUnitHierarchies.class, h.getId());
			hibHierarchy.setLabel(h.getLabel());
			hibHierarchy.setName(h.getName());
			hibHierarchy.setDescription(h.getDescription());
			hibHierarchy.setTarget(h.getTarget());
			hibHierarchy.setCompany(h.getCompany());
			updateSbiCommonInfo4Update(hibHierarchy);
			aSession.save(hibHierarchy);
			
			tx.commit();
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: Hierarchy modified successfully");
		
	}

	public void eraseOrganizationalUnitNode(OrganizationalUnitNode node) {
		logger.debug("IN: node = " + node);
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			eraseOrganizationalUnitNode(node, aSession);
			
			tx.commit();
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: node removed successfully.");
	}
	
	public void eraseOrganizationalUnitNode(OrganizationalUnitNode node, Session aSession) {
		logger.debug("IN: node = " + node);
		SbiOrgUnitNodes hibNode = (SbiOrgUnitNodes) aSession.load(SbiOrgUnitNodes.class, node.getNodeId());
		aSession.delete(hibNode);
		logger.debug("OUT: node removed successfully.");
	}
	

	public boolean existsNodeInHierarchy(String path, Integer hierarchyId) {
		logger.debug("IN: path = " + path + ", hierarchy = " + hierarchyId);
		boolean toReturn = false;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			toReturn = existsNodeInHierarchy(path, hierarchyId, aSession);
			
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}
	
	public boolean existsNodeInHierarchy(String path, Integer hierarchyId,
			Session aSession) {
		boolean toReturn = false;
		logger.debug("IN: path = " + path + ", hierarchy = " + hierarchyId);
		Query hibQuery = aSession.createQuery(" from SbiOrgUnitNodes n where n.sbiOrgUnitHierarchies.id = ? " +
				" and n.path = ? ");
		hibQuery.setInteger(0, hierarchyId);
		hibQuery.setString(1, path);
		
		List hibList = hibQuery.list();
		toReturn = !hibList.isEmpty();
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}
	
	public OrganizationalUnitNode getOrganizationalUnitNode(String path, Integer hierarchyId) {
		logger.debug("IN: path = " + path + ", hierarchy = " + hierarchyId);
		OrganizationalUnitNode toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			toReturn = getOrganizationalUnitNode(path, hierarchyId, aSession);
			
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}
	
	public OrganizationalUnitNode getOrganizationalUnitNode(String path, Integer hierarchyId, Session aSession) {
		logger.debug("IN: path = " + path + ", hierarchy = " + hierarchyId);
		OrganizationalUnitNode toReturn = null;
		Query hibQuery = aSession.createQuery(" from SbiOrgUnitNodes n where n.sbiOrgUnitHierarchies.id = ? " +
				" and n.path = ? ");
		hibQuery.setInteger(0, hierarchyId);
		hibQuery.setString(1, path);
		
		SbiOrgUnitNodes hibNode = (SbiOrgUnitNodes) hibQuery.uniqueResult();
		toReturn = toOrganizationalUnitNode(hibNode);
		
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}

	public void insertOrganizationalUnitNode(OrganizationalUnitNode aNode) {
		logger.debug("IN: aNode = " + aNode);
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			
			insertOrganizationalUnitNode(aNode, aSession);
			
			tx.commit();
			
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: OrganizationalUnitNode inserted successfully with id " + aNode.getNodeId());
	}
	
	public void insertOrganizationalUnitNode(OrganizationalUnitNode aNode, Session aSession) {
		SbiOrgUnitNodes hibNode = new SbiOrgUnitNodes();
		
		Query hibQuery = aSession.createQuery(" from SbiOrgUnitHierarchies s where s.id = ? ");
		hibQuery.setInteger(0, aNode.getHierarchy().getId());
		SbiOrgUnitHierarchies hierarchy = (SbiOrgUnitHierarchies) hibQuery.uniqueResult();
		hibNode.setSbiOrgUnitHierarchies(hierarchy);
		
		hibNode.setPath(aNode.getPath());
		
		hibQuery = aSession.createQuery(" from SbiOrgUnit s where s.id = ? ");
		hibQuery.setInteger(0, aNode.getOu().getId());
		SbiOrgUnit ou = (SbiOrgUnit) hibQuery.uniqueResult();
		hibNode.setSbiOrgUnit(ou);
		
		if (aNode.getParentNodeId() != null) {
			hibQuery = aSession.createQuery(" from SbiOrgUnitNodes s where s.nodeId = ? ");
			hibQuery.setInteger(0, aNode.getParentNodeId());
			SbiOrgUnitNodes parentNode = (SbiOrgUnitNodes) hibQuery.uniqueResult();
			hibNode.setSbiOrgUnitNodes(parentNode);
		}
		updateSbiCommonInfo4Insert(hibNode);
		aSession.save(hibNode);
		
		aNode.setNodeId(hibNode.getNodeId());
		
		logger.debug("OUT: OrganizationalUnitNode inserted successfully with id " + aNode.getNodeId());
	}
	

	public void insertGrant(OrganizationalUnitGrant grant) {
		logger.debug("IN: grant = " + grant);
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiOrgUnitGrant hibGrant = new SbiOrgUnitGrant();
			hibGrant.setLabel(grant.getLabel());
			hibGrant.setName(grant.getName());
			hibGrant.setDescription(grant.getDescription());
			hibGrant.setStartDate(grant.getStartDate());
			hibGrant.setEndDate(grant.getEndDate());
			hibGrant.setIsAvailable(grant.getIsAvailable());
			// set hierarchy
			Integer hierachyId = grant.getHierarchy().getId();
			Query query = aSession.createQuery(" from SbiOrgUnitHierarchies s where s.id = ? ");
			query.setInteger(0, hierachyId);
			SbiOrgUnitHierarchies h = (SbiOrgUnitHierarchies) query.uniqueResult();
			hibGrant.setSbiOrgUnitHierarchies(h);
			
			// set kpi model instance
			Integer kpiModelInstId = grant.getModelInstance().getId();
			query = aSession.createQuery(" from SbiKpiModelInst s where s.kpiModelInst = ? ");
			query.setInteger(0, kpiModelInstId);
			SbiKpiModelInst s = (SbiKpiModelInst) query.uniqueResult();
			hibGrant.setSbiKpiModelInst(s);
			updateSbiCommonInfo4Insert(hibGrant);
			aSession.save(hibGrant);
			tx.commit();
			
			grant.setId(hibGrant.getId());
			
			
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: OrganizationalUnitGrant inserted successfully with id " + grant.getId());
	}

	public void modifyGrant(OrganizationalUnitGrant grant) {
		logger.debug("IN: grant = " + grant);
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiOrgUnitGrant hibGrant = (SbiOrgUnitGrant) aSession.load(SbiOrgUnitGrant.class, grant.getId());
			hibGrant.setLabel(grant.getLabel());
			hibGrant.setName(grant.getName());
			hibGrant.setDescription(grant.getDescription());
			hibGrant.setStartDate(grant.getStartDate());
			hibGrant.setEndDate(grant.getEndDate());
			hibGrant.setIsAvailable(grant.getIsAvailable());
			// if hierarchy and/or kpi model instance have been changed, erase previous defined node grants
			Integer previousHierachyId = hibGrant.getSbiOrgUnitHierarchies().getId();
			Integer newHierachyId = grant.getHierarchy().getId();
			Integer previousKpiModelInstId = hibGrant.getSbiKpiModelInst().getKpiModelInst();
			Integer newKpiModelInstId = grant.getModelInstance().getId();
			if (previousHierachyId.intValue() != newHierachyId.intValue() 
					|| previousKpiModelInstId.intValue() != newKpiModelInstId.intValue()) {
				String hql = "delete from SbiOrgUnitGrantNodes s where s.id.grantId = ?";
		        Query query = aSession.createQuery(hql);
		        query.setInteger(0, hibGrant.getId());
		        query.executeUpdate();
			}
			
			// update hierarchy
			if (previousHierachyId.intValue() != newHierachyId.intValue()) {
				Query query = aSession.createQuery(" from SbiOrgUnitHierarchies s where s.id = ? ");
				query.setInteger(0, newHierachyId);
				SbiOrgUnitHierarchies h = (SbiOrgUnitHierarchies) query.uniqueResult();
				hibGrant.setSbiOrgUnitHierarchies(h);
			}
			
			// update kpi model instance
			if (previousKpiModelInstId.intValue() != newKpiModelInstId.intValue()) {
				Query query = aSession.createQuery(" from SbiKpiModelInst s where s.kpiModelInst = ? ");
				query.setInteger(0, newKpiModelInstId);
				SbiKpiModelInst s = (SbiKpiModelInst) query.uniqueResult();
				hibGrant.setSbiKpiModelInst(s);
			}
			updateSbiCommonInfo4Update(hibGrant);
			aSession.save(hibGrant);
			
			tx.commit();
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: OrganizationalUnitGrant modified successfully.");
	}
	
	public void eraseNodeGrants(Integer grantId) {
		logger.debug("IN: grantId = " + grantId);
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			
			String hql = "delete from SbiOrgUnitGrantNodes s where s.id.grantId = ?";
			Query query = aSession.createQuery(hql);
			query.setInteger(0, grantId);
			query.executeUpdate();

			tx.commit();
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: OrganizationalUnitGrant modified successfully.");
	}

	public void eraseGrant(Integer grantId) {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiOrgUnitGrant hibGrant = (SbiOrgUnitGrant) aSession.load(SbiOrgUnitGrant.class, grantId);
			aSession.delete(hibGrant);
			
			tx.commit();
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: OrganizationalUnitGrant removed successfully.");
	}
	
	public void insertNodeGrants(List<OrganizationalUnitGrantNode> grantNodes, Integer grantId) {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiOrgUnitGrant hibGrant = (SbiOrgUnitGrant) aSession.load(SbiOrgUnitGrant.class, grantId);
			Iterator<OrganizationalUnitGrantNode> it = grantNodes.iterator();
			while (it.hasNext()) {
				OrganizationalUnitGrantNode aGrantNode = it.next();

				Integer hierarchyNodeId = aGrantNode.getOuNode().getNodeId();
				Integer kpiModelInstNodeId = aGrantNode.getModelInstanceNode().getModelInstanceNodeId();
				
				SbiOrgUnitGrantNodes grantNode = new SbiOrgUnitGrantNodes();
			
				SbiOrgUnitGrantNodesId grantNodeId = new SbiOrgUnitGrantNodesId(hierarchyNodeId, kpiModelInstNodeId, grantId);
				grantNode.setId(grantNodeId);
				
				SbiOrgUnitNodes hibNode = (SbiOrgUnitNodes) aSession.load(SbiOrgUnitNodes.class, hierarchyNodeId);
				grantNode.setSbiOrgUnitNodes(hibNode);
				
				SbiKpiModelInst kpiModelInst = (SbiKpiModelInst) aSession.load(SbiKpiModelInst.class, kpiModelInstNodeId);
				grantNode.setSbiKpiModelInst(kpiModelInst);
				
				
				grantNode.setSbiOrgUnitGrant(hibGrant);
				logger.debug("Saving grant node with node Id:"+grantNodeId.getNodeId()+" modelInst Id "+grantNodeId.getKpiModelInstNodeId()+" ang grant Id "+grantNodeId.getGrantId());
				//System.out.println("Saving grant node with node Id:"+grantNodeId.getNodeId()+" modelInst Id "+grantNodeId.getKpiModelInstNodeId()+" ang grant Id "+grantNodeId.getGrantId());
				updateSbiCommonInfo4Insert(grantNode);
				aSession.save(grantNode);
			}
			//sets grant available if everithing ok
			hibGrant.setIsAvailable(true);
			aSession.save(hibGrant);
			tx.commit();
		}catch(Exception e){

			logger.error(e.getMessage());
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: List of OrganizationalUnitGrantNode inserted successfully.");
		
	}
	

	public List<OrganizationalUnitNodeWithGrant> getRootNodesWithGrants(
			Integer hierarchyId, Integer grantId) {
		logger.debug("IN: hierarchyId = " + hierarchyId + ", grantId = " + grantId);
		List<OrganizationalUnitNodeWithGrant> toReturn = new ArrayList<OrganizationalUnitNodeWithGrant>();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = aSession.createQuery(" from SbiOrgUnitNodes n where n.sbiOrgUnitHierarchies.id = ? " +
					" and n.sbiOrgUnitNodes is null");
			hibQuery.setInteger(0, hierarchyId);
			
			List roots = hibQuery.list();

			if (roots != null && !roots.isEmpty()) {
				Iterator it = roots.iterator();
				while (it.hasNext()) {
					SbiOrgUnitNodes root = (SbiOrgUnitNodes) it.next();
					OrganizationalUnitNode node = toOrganizationalUnitNode(root);
					OrganizationalUnitNodeWithGrant nodeWithGrant = getNodeWithGrants(node, grantId, aSession);
					toReturn.add(nodeWithGrant);
				}
			}
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}

	private OrganizationalUnitNodeWithGrant getNodeWithGrants(
			OrganizationalUnitNode node, Integer grantId, Session aSession) {
		logger.debug("IN");
		OrganizationalUnitNodeWithGrant toReturn = null;
		List<OrganizationalUnitGrantNode> grants = new ArrayList<OrganizationalUnitGrantNode>();
		Query hibQuery = aSession.createQuery(" from SbiOrgUnitGrantNodes n where n.id.nodeId = ? " +
			" and n.id.grantId = ?");
		hibQuery.setInteger(0, node.getNodeId());
		hibQuery.setInteger(1, grantId);
		List hibList = hibQuery.list();
		Iterator it = hibList.iterator();
		while (it.hasNext()) {
			grants.add(toOrganizationalUnitGrantNode((SbiOrgUnitGrantNodes) it.next(), aSession));
		}
		toReturn = new OrganizationalUnitNodeWithGrant(node, grants);
		logger.debug("OUT");
		return toReturn;
	}
	
	public List<OrganizationalUnitNodeWithGrant> getChildrenNodesWithGrants(
			Integer nodeId, Integer grantId) {
		logger.debug("IN: nodeId = " + nodeId + ", grantId = " + grantId);
		List<OrganizationalUnitNodeWithGrant> toReturn = new ArrayList<OrganizationalUnitNodeWithGrant>();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			
			Query hibQuery = aSession.createQuery(" from SbiOrgUnitNodes n where n.sbiOrgUnitNodes.nodeId = ? ");
			hibQuery.setInteger(0, nodeId);
			
			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();

			while (it.hasNext()) {
				OrganizationalUnitNode node = toOrganizationalUnitNode((SbiOrgUnitNodes) it.next());
				OrganizationalUnitNodeWithGrant nodeWithGrants = getNodeWithGrants(node, grantId, aSession);
				toReturn.add(nodeWithGrants);
			}
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}
	
	public List<OrganizationalUnitNodeWithGrant> getGrantNodes(Integer nodeId, Integer grantId) {
		logger.debug("IN: nodeId = " + nodeId + ", grantId = " + grantId);
		List<OrganizationalUnitNodeWithGrant> toReturn = new ArrayList<OrganizationalUnitNodeWithGrant>();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			
			Query hibQuery = aSession.createQuery(" from SbiOrgUnitNodes n where n.nodeId = ? ");
			hibQuery.setInteger(0, nodeId);
			
			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();

			while (it.hasNext()) {
				OrganizationalUnitNode node = toOrganizationalUnitNode((SbiOrgUnitNodes) it.next());
				OrganizationalUnitNodeWithGrant nodeWithGrants = getNodeWithGrants(node, grantId, aSession);
				toReturn.add(nodeWithGrants);
			}
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}
	
	public List<OrganizationalUnitGrantNode> getGrants(
			Integer kpiModelInstanceId) {
		logger.debug("IN: kpiModelInstanceId = " + kpiModelInstanceId);
		List<OrganizationalUnitGrantNode> toReturn = new ArrayList<OrganizationalUnitGrantNode>();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			
			Query hibQuery = aSession.createQuery(" from SbiOrgUnitGrantNodes n where n.id.kpiModelInstNodeId = ? ");
			hibQuery.setInteger(0, kpiModelInstanceId);
			
			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();

			while (it.hasNext()) {
				toReturn.add(toOrganizationalUnitGrantNode((SbiOrgUnitGrantNodes) it.next(), aSession));
			}
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}
	
	public List<OrganizationalUnitGrantNode> getGrantsValidByDate(
			Integer kpiModelInstanceId, Date now) {
		logger.debug("IN: kpiModelInstanceId = " + kpiModelInstanceId);
		List<OrganizationalUnitGrantNode> toReturn = new ArrayList<OrganizationalUnitGrantNode>();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			
			Query hibQuery = aSession.createQuery(" from SbiOrgUnitGrantNodes n where n.id.kpiModelInstNodeId = ? and ? between n.sbiOrgUnitGrant.startDate and n.sbiOrgUnitGrant.endDate");
			hibQuery.setInteger(0, kpiModelInstanceId);
			hibQuery.setDate(1, now);
			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();

			while (it.hasNext()) {
				toReturn.add(toOrganizationalUnitGrantNode((SbiOrgUnitGrantNodes) it.next(), aSession));
			}
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}
	public OrganizationalUnitGrant toOrganizationalUnitGrant(
			SbiOrgUnitGrant hibGrant, Session aSession) {
		OrganizationalUnitHierarchy hierarchy = toOrganizationalUnitHierarchy(hibGrant.getSbiOrgUnitHierarchies());
		ModelInstance modelInstance = ModelInstanceDAOImpl.toModelInstanceWithoutChildren(hibGrant.getSbiKpiModelInst(), aSession);
		OrganizationalUnitGrant grant = new OrganizationalUnitGrant(hibGrant.getId(), hibGrant.getIsAvailable(), modelInstance, 
				hierarchy, hibGrant.getStartDate(), hibGrant.getEndDate(), hibGrant.getLabel(), 
				hibGrant.getName(), hibGrant.getDescription());
		return grant;
	}

	public OrganizationalUnit toOrganizationalUnit(SbiOrgUnit hibOrgUnit){
		OrganizationalUnit ou = new OrganizationalUnit(hibOrgUnit.getId(), hibOrgUnit.getLabel(), 
				hibOrgUnit.getName(), hibOrgUnit.getDescription());
		return ou;
	}
	
	public OrganizationalUnitHierarchy toOrganizationalUnitHierarchy(SbiOrgUnitHierarchies hibOrgUnitHierarchies){
		OrganizationalUnitHierarchy hierarchy = new OrganizationalUnitHierarchy(hibOrgUnitHierarchies.getId(), 
				hibOrgUnitHierarchies.getLabel(), hibOrgUnitHierarchies.getName(), hibOrgUnitHierarchies.getDescription(), 
				hibOrgUnitHierarchies.getTarget(), hibOrgUnitHierarchies.getCompany());
		return hierarchy;
	}
	
	public OrganizationalUnitNode toOrganizationalUnitNode(SbiOrgUnitNodes hibOrgUnitNode) {
		OrganizationalUnit ou = toOrganizationalUnit(hibOrgUnitNode.getSbiOrgUnit());
		OrganizationalUnitHierarchy hierarchy = toOrganizationalUnitHierarchy(hibOrgUnitNode.getSbiOrgUnitHierarchies());
		OrganizationalUnitNode node = new OrganizationalUnitNode(hibOrgUnitNode.getNodeId(), ou, hierarchy, 
				hibOrgUnitNode.getPath(), 
				hibOrgUnitNode.getSbiOrgUnitNodes() == null ? null : hibOrgUnitNode.getSbiOrgUnitNodes().getNodeId() );

		Session aSession = null;
		Transaction tx = null;
		
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			
			Query hibQuery = aSession.createQuery(" from SbiOrgUnitNodes n where n.sbiOrgUnitNodes.nodeId = ? ");
			hibQuery.setInteger(0, node.getNodeId());
			
			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();

			node.setLeaf(!it.hasNext());

		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		
		return node;
	}
	
	
	public OrganizationalUnitGrantNode toOrganizationalUnitGrantNode(
			SbiOrgUnitGrantNodes hibGrantNode, Session aSession) {
		OrganizationalUnitNode ouNode = toOrganizationalUnitNode(hibGrantNode.getSbiOrgUnitNodes());
		ModelInstanceNode modelInstanceNode;
		try {
			modelInstanceNode = ModelInstanceDAOImpl.toModelInstanceNode(hibGrantNode.getSbiKpiModelInst());
		} catch (EMFUserError e) {
			throw new RuntimeException(e);
		}
		OrganizationalUnitGrant grant = toOrganizationalUnitGrant(hibGrantNode.getSbiOrgUnitGrant(), aSession);
		OrganizationalUnitGrantNode grantNode = new OrganizationalUnitGrantNode(ouNode, modelInstanceNode, grant);
		return grantNode;
	}

	public OrganizationalUnit getOrganizationalUnitByLabelAndName(String label, String name) {
		logger.debug("IN: label = " + label+" name = "+name);
		OrganizationalUnit toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Query hibQuery = aSession.createQuery(" from SbiOrgUnit o where o.label = ? and o.name = ?");
			hibQuery.setString(0, label);
			hibQuery.setString(1, name);
			SbiOrgUnit hibOU = (SbiOrgUnit)hibQuery.uniqueResult();
			if(hibOU != null){
				toReturn = toOrganizationalUnit(hibOU);
			}else
				return null;
			
			
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}

	public List<OrganizationalUnitNode> getNodes() {
		logger.debug("IN");
		List<OrganizationalUnitNode> toReturn = new ArrayList<OrganizationalUnitNode>();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			
			Query hibQuery = aSession.createQuery(" from SbiOrgUnitNodes ");
			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();

			while (it.hasNext()) {
				OrganizationalUnitNode node = toOrganizationalUnitNode((SbiOrgUnitNodes) it.next());
				toReturn.add(node);
			}
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}

	public void eraseNodeGrant(OrganizationalUnitGrantNode grantNode) {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			String hql = "delete from SbiOrgUnitGrantNodes s where s.id.grantId = ? and s.id.nodeId = ? and s.id.kpiModelInstNodeId = ? ";
			Query query = aSession.createQuery(hql);
			query.setInteger(0, grantNode.getGrant().getId());
			query.setInteger(1, grantNode.getOuNode().getNodeId());
			query.setInteger(2, grantNode.getModelInstanceNode().getModelInstanceNodeId());
			query.executeUpdate();
			
			tx.commit();
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: OrganizationalUnitGrantNode deleted successfully.");
	}
	
	
	
	public boolean hasGrants(OrganizationalUnitNode node) {
		logger.debug("IN");
		boolean toReturn = false;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = aSession.createQuery(" from SbiOrgUnitGrantNodes s where s.id.nodeId = ?");
			hibQuery.setInteger(0, node.getNodeId());
			
			List hibList = hibQuery.list();
			if (hibList != null && !hibList.isEmpty()) {
				toReturn = true;
			}
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}
	
	
	
	public boolean isInAHierarchy(OrganizationalUnit ou) {
		logger.debug("IN");
		boolean toReturn = false;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = aSession.createQuery(" from SbiOrgUnitNodes n where n.sbiOrgUnit.id = ?");
			hibQuery.setInteger(0, ou.getId());
			
			List hibList = hibQuery.list();
			if (hibList != null && !hibList.isEmpty()) {
				toReturn = true;
			}
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}
	
	
	
	public boolean hasGrants(OrganizationalUnitHierarchy h) {
		logger.debug("IN");
		boolean toReturn = false;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = aSession.createQuery(" from SbiOrgUnitGrantNodes s where s.sbiOrgUnitHierarchies.id = ?");
			hibQuery.setInteger(0, h.getId());
			
			List hibList = hibQuery.list();
			if (hibList != null && !hibList.isEmpty()) {
				toReturn = true;
			}
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}
	
	
	public OrganizationalUnitGrant loadGrantByLabel(String label) {
		logger.debug("IN");
		OrganizationalUnitGrant toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = aSession.createQuery(" from SbiOrgUnitGrant s where s.label = ? ");
			hibQuery.setString(0, label);
			SbiOrgUnitGrant hibGrant = (SbiOrgUnitGrant) hibQuery.uniqueResult();

			if (hibGrant != null) {
				toReturn = toOrganizationalUnitGrant(hibGrant, aSession);
			}
			
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}



}
