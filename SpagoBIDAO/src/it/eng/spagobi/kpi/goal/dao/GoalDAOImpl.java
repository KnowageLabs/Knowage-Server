/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.goal.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.kpi.goal.metadata.SbiGoal;
import it.eng.spagobi.kpi.goal.metadata.SbiGoalHierarchy;
import it.eng.spagobi.kpi.goal.metadata.SbiGoalKpi;
import it.eng.spagobi.kpi.goal.metadata.bo.Goal;
import it.eng.spagobi.kpi.goal.metadata.bo.GoalKpi;
import it.eng.spagobi.kpi.goal.metadata.bo.GoalNode;


public class GoalDAOImpl extends AbstractHibernateDAO implements IGoalDAO{
	
	static private Logger logger = Logger.getLogger(GoalDAOImpl.class);

	@SuppressWarnings("rawtypes")
	public List<Goal> getGoalsList() {
		logger.debug("IN");
		List<Goal> toReturn = new ArrayList<Goal>();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = aSession.createQuery(" from SbiGoal");

			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();

			while (it.hasNext()) {
				toReturn.add(toGoal((SbiGoal) it.next()));
			}
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}
	
	public void insertGoal(Goal goal) {
		logger.debug("IN: goal = " + goal);
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			
			SbiGoal hibGoal = new SbiGoal();
			hibGoal.setName(goal.getName());
			hibGoal.setLabel(goal.getLabel());
			hibGoal.setDescription(goal.getDescription());
			hibGoal.setStartDate(goal.getStartDate());
			hibGoal.setEndDate(goal.getEndDate());
			hibGoal.setGrantId(goal.getGrantId());
			updateSbiCommonInfo4Insert(hibGoal);
			if(goal.getId()!=null){
				hibGoal.setGoalId(goal.getId());
				aSession.update(hibGoal);					
				tx.commit();
			}else{
				aSession.save(hibGoal);	
				tx.commit();
				goal.setId(hibGoal.getGoalId());
			}
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: OrganizationalUnit inserted successfully with id " + goal.getId());
	}
	
	public void eraseGoal(Integer grantId) {
		logger.debug("IN: ouId = " + grantId);
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			
			SbiGoal hibGrant = (SbiGoal) aSession.load(SbiGoal.class, grantId);
			aSession.delete(hibGrant);
			
			tx.commit();
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: OrganizationalUnit removed successfully.");
	}
	

	public GoalNode getRootNode(Integer goalId, Integer ouId) {
		logger.debug("IN: goalId = " + goalId);
		GoalNode toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = aSession.createQuery(" from SbiGoalHierarchy n where n.goalId = ? " +
					" and n.sbiGoalHierarchy is null and n.orgUnitId= ?");
			hibQuery.setInteger(0, goalId);
			hibQuery.setInteger(1, ouId);
			
			SbiGoalHierarchy root = (SbiGoalHierarchy) hibQuery.uniqueResult();

			if (root != null) {
				toReturn = toGoalNode(root);
			}
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}
	
	public List<GoalNode> getChildrenNodes(Integer nodeId) {
		logger.debug("IN: nodeId = " + nodeId);
		List<GoalNode> toReturn = new ArrayList<GoalNode>();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = aSession.createQuery(" from SbiGoalHierarchy n where n.sbiGoalHierarchy.id = ? ");
			hibQuery.setInteger(0, nodeId);
			
			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();

			while (it.hasNext()) {
				toReturn.add(toGoalNode((SbiGoalHierarchy) it.next()));
			}
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}
	
	public void insertGoalNode(GoalNode goalNode, Integer fatherId) {
		logger.debug("IN: goalNode = " + goalNode);
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiGoalHierarchy hibGoalHierarchy = new SbiGoalHierarchy();
			hibGoalHierarchy.setName(goalNode.getName());
			hibGoalHierarchy.setLabel(goalNode.getLabel());
			hibGoalHierarchy.setGoal(goalNode.getGoalDescr());
			hibGoalHierarchy.setOrgUnitId(goalNode.getOuId());
			hibGoalHierarchy.setGoalId(goalNode.getGoalId());
			if(fatherId!=null){
				Query hibQuery = aSession.createQuery(" from SbiGoalHierarchy s where s.goalHierarchyId = ? ");
				hibQuery.setInteger(0, fatherId);
				SbiGoalHierarchy father= (SbiGoalHierarchy)hibQuery.uniqueResult();
				hibGoalHierarchy.setSbiGoalHierarchy(father);
			}
			updateSbiCommonInfo4Insert(hibGoalHierarchy);
			aSession.save(hibGoalHierarchy);	
			tx.commit();
			goalNode.setId(hibGoalHierarchy.getGoalHierarchyId());
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: OrganizationalUnit inserted successfully with id " + goalNode.getId());
	}
	
	public void ereseGoalNode(Integer goalNodeId){
		logger.debug("IN: goalNodeId = " + goalNodeId);
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiGoalHierarchy hibGrantNode = (SbiGoalHierarchy) aSession.load(SbiGoalHierarchy.class, goalNodeId);
			aSession.delete(hibGrantNode);

			tx.commit();
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: Goal Node removed successfully.");
	}
	
	public void updateGoalName(Integer goalId, String newName){
		logger.debug("IN: goalNodeId = " + goalId);
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiGoalHierarchy hibGrantNode = (SbiGoalHierarchy) aSession.load(SbiGoalHierarchy.class, goalId);
			hibGrantNode.setName(newName);
			updateSbiCommonInfo4Update(hibGrantNode);
			aSession.update(hibGrantNode);

			tx.commit();
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: Goal Node removed successfully.");
	}
	
	public void updateGoalNode(GoalNode goalNode) {
		logger.debug("IN: goalNode = " + goalNode);
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Query hibQuery = aSession.createQuery(" from SbiGoalHierarchy s where s.goalHierarchyId = ? ");
			hibQuery.setInteger(0, goalNode.getId());
			SbiGoalHierarchy exists= (SbiGoalHierarchy)hibQuery.uniqueResult();
			exists.setName(goalNode.getName());
			exists.setLabel(goalNode.getLabel());
			exists.setGoal(goalNode.getGoalDescr());
			updateSbiCommonInfo4Update(exists);
			aSession.update(exists);	
			tx.commit();			
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: OrganizationalUnit inserted successfully with id " + goalNode.getId());
	}
	
	
	public void insertGoalKpis(List<GoalKpi> goalKpis, Integer goalNodeId){
		
		logger.debug("IN: goalKpis = " + goalKpis);
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			
			Query hibQuery = aSession.createQuery(" from SbiGoalHierarchy s where s.goalHierarchyId = ? ");
			hibQuery.setInteger(0, goalNodeId);
			SbiGoalHierarchy goalNodel= (SbiGoalHierarchy)hibQuery.uniqueResult();

			for(int i=0; i<goalKpis.size(); i++){
				insertGoalKpi(goalKpis.get(i), goalNodel, aSession, tx);
			}
			
			tx.commit();
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: goalKpis = " + goalKpis);
	}
	
	public void ereseGoalKpis(Integer goalNodeId){
		logger.debug("IN: getting goalkpis forn goal = " + goalNodeId);
		List<GoalKpi> toReturn = new ArrayList<GoalKpi>();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = aSession.createQuery(" from SbiGoalKpi n where n.sbiGoalHierarchy.goalHierarchyId = ? ");
			hibQuery.setInteger(0, goalNodeId);
			
			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();

			while (it.hasNext()) {
				aSession.delete((SbiGoalKpi) it.next());
			}
			tx.commit();
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: returning " + toReturn);
	}
	
	private void insertGoalKpi(GoalKpi goalKpi, SbiGoalHierarchy goalNodel, Session aSession, Transaction tx) {
		logger.debug("IN: goalKpi = " + goalKpi);
		SbiGoalKpi exists = null;

		SbiGoalKpi hibSbiGoalKpi = new SbiGoalKpi();
		//hibSbiGoalKpi.setGoalKpiId(goalKpi.getId());
		hibSbiGoalKpi.setKpiInstanceId(goalKpi.getModelInstanceId());
		hibSbiGoalKpi.setSbiGoalHierarchy(goalNodel);
		if(goalKpi.getThreshold1()!=null){
			hibSbiGoalKpi.setThreshold1(goalKpi.getThreshold1());
		}
		hibSbiGoalKpi.setThreshold1sign(goalKpi.getSign1());
		if(goalKpi.getThreshold2()!=null){
			hibSbiGoalKpi.setThreshold2(goalKpi.getThreshold2());
		}
		hibSbiGoalKpi.setThreshold2sign(goalKpi.getSign2());
		hibSbiGoalKpi.setWeight1(goalKpi.getWeight1());
		hibSbiGoalKpi.setWeight2(goalKpi.getWeight2());

		//look for preexisting one with same same label-name key

		if(goalKpi.getId()!=null){
			Query hibQuery = aSession.createQuery(" from SbiGoalKpi n where n.sbiGoalHierarchy.goalHierarchyId = ? and n.kpiInstanceId = ?");
			hibQuery.setInteger(0, goalNodel.getGoalHierarchyId());
			hibQuery.setInteger(1, hibSbiGoalKpi.getKpiInstanceId());
			exists= (SbiGoalKpi)hibQuery.uniqueResult();
		}
		updateSbiCommonInfo4Insert(hibSbiGoalKpi);
		if(exists == null){
			aSession.save(hibSbiGoalKpi);	
			goalKpi.setId(hibSbiGoalKpi.getGoalKpiId());
		}else{
			aSession.delete(exists);
			aSession.save(hibSbiGoalKpi);	
			goalKpi.setId(hibSbiGoalKpi.getGoalKpiId());
		}
	
		logger.debug("OUT: goalKpi = " + goalKpi);

	}
	
	public List<GoalKpi> getGoalKpi(Integer goalNodeId) {
		logger.debug("IN: getting goalkpis forn goal = " + goalNodeId);
		List<GoalKpi> toReturn = new ArrayList<GoalKpi>();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = aSession.createQuery(" from SbiGoalKpi n where n.sbiGoalHierarchy.goalHierarchyId = ? ");
			hibQuery.setInteger(0, goalNodeId);
			
			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();

			while (it.hasNext()) {
				toReturn.add(toGoalKpi((SbiGoalKpi) it.next()));
			}
		} finally {
			rollbackIfActiveAndClose(tx, aSession);
		}
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}
	
	public GoalKpi toGoalKpi(SbiGoalKpi hibernateGoalKpi) {
		GoalKpi g = new GoalKpi(hibernateGoalKpi.getKpiInstanceId(), hibernateGoalKpi.getWeight1(), hibernateGoalKpi.getWeight2(), hibernateGoalKpi.getThreshold1(), hibernateGoalKpi.getThreshold2(), hibernateGoalKpi.getThreshold1sign(), hibernateGoalKpi.getThreshold2sign(), hibernateGoalKpi.getGoalKpiId(),  hibernateGoalKpi.getSbiGoalHierarchy().getGoalHierarchyId());
		return g;
	}
	
	public Goal toGoal(SbiGoal hibernateGoal) {
		Goal g = new Goal(hibernateGoal.getGoalId(), hibernateGoal.getStartDate(), hibernateGoal.getEndDate(), hibernateGoal.getName(), hibernateGoal.getLabel(), hibernateGoal.getDescription(), hibernateGoal.getGrantId());
		return g;
	}
	
	public GoalNode toGoalNode(SbiGoalHierarchy hibernateGoalNode) {
		GoalNode g = new GoalNode(hibernateGoalNode.getName(), hibernateGoalNode.getLabel(), hibernateGoalNode.getGoal(), hibernateGoalNode.getGoalId(), hibernateGoalNode.getOrgUnitId());
		g.setId(hibernateGoalNode.getGoalHierarchyId());
		return g;
	}

}
