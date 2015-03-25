/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.ou.provider;

import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnit;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnitHierarchy;
import it.eng.spagobi.utilities.tree.Node;
import it.eng.spagobi.utilities.tree.Tree;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;


public class OrganizationalUnitListProviderDB extends
		OrganizationalUnitListProvider {

	String S = Tree.NODES_PATH_SEPARATOR;
	static private Logger logger = Logger
			.getLogger(OrganizationalUnitListProviderDB.class);
	
	private static String jndiDatasource;
	private static final String HIERARCHY = "hierarchy";
	private static final String OU = "ou";
	private static final String COMPANY = "company"; 
	private static final String CODE = "code";
	private static final String NAME = "name";
	
	private String getHierarchiesQuery = null;
	private String getOUsQuery = null;
	private String getRootByHierarchy = null;
	private String getChildrenByLevel = null;
	private String getRootLeaves = null;
	/*
	 * tabella dws_t_an_aggr_ce 
	 * ag_tab nome gerarchia 
	 * ag_cetabl1.....15 codici uo 
	 * ag_detabl1....15 sono i nomi delle UO 
	 * 15 livelli di profondità 
	 * AG_DIM è il codice foglia 
	 * cd_az è il codice azienda
	 */	
	@Override
	public void initialize() {		
		SingletonConfig singleConfig = SingletonConfig.getInstance();
		jndiDatasource = singleConfig.getConfigValue("SPAGOBI.ORGANIZATIONAL-UNIT.jndiDatasource");
		getHierarchiesQuery = singleConfig.getConfigValue("SPAGOBI.ORGANIZATIONAL-UNIT.getHierarchiesQuery");
		getOUsQuery= singleConfig.getConfigValue("SPAGOBI.ORGANIZATIONAL-UNIT.getOUsQuery");
		getRootByHierarchy = singleConfig.getConfigValue("SPAGOBI.ORGANIZATIONAL-UNIT.getRootByHierarchy");
		getChildrenByLevel= singleConfig.getConfigValue("SPAGOBI.ORGANIZATIONAL-UNIT.getChildrenByLevel");
		getRootLeaves= singleConfig.getConfigValue("SPAGOBI.ORGANIZATIONAL-UNIT.getRootLeaves");
	}

	 static Connection getJNDIConnection() {

		Connection result = null;
		try {
			Context initialContext = new InitialContext();
			DataSource datasource = (DataSource) initialContext
					.lookup(jndiDatasource);
			if (datasource != null) {
				result = datasource.getConnection();
			} else {
				logger.error("Failed to lookup datasource.");
			}
		} catch (NamingException ex) {
			logger.error("Cannot get connection: " + ex.getMessage());
		} catch (SQLException ex) {
			logger.error("Cannot get connection: " + ex.getMessage());
		}
		return result;
	}
	@Override
	public List<OrganizationalUnitHierarchy> getHierarchies() {
		List<OrganizationalUnitHierarchy> toReturn = new ArrayList<OrganizationalUnitHierarchy>();

		try {
			executeQuery(getHierarchiesQuery, HIERARCHY, toReturn);

		} catch (Exception e) {
			logger.error("Error getting hiererchies list");
		}
		return toReturn;
	}

	@Override
	public List<OrganizationalUnit> getOrganizationalUnits() {
		List<OrganizationalUnit> toReturn = new ArrayList<OrganizationalUnit>();
		for(int i= 1; i<= 15 ; i++){
			try {
				String replacedQuery = getOUsQuery.replaceAll("\\!", Integer.toString(i));
				executeQuery(replacedQuery, OU, toReturn);
			} catch (Exception e) {
				logger.error("Error getting OU list");
			}
		}
		return toReturn;
	}
	
	@Override
	public List<Tree<OrganizationalUnit>> getHierarchyStructure(
			OrganizationalUnitHierarchy hierarchy) {
		logger.debug("IN");
		List<Tree<OrganizationalUnit>> toReturn = new ArrayList<Tree<OrganizationalUnit>>();
		HashMap<String, String> params = new HashMap<String, String>();
		params.put(HIERARCHY, hierarchy.getName());
		params.put(COMPANY, hierarchy.getCompany());
		try {
			List<Node<OrganizationalUnit>> actualRoots = getRootByQueryString(getRootByHierarchy, params, null);
			if (!actualRoots.isEmpty()) {
				Iterator<Node<OrganizationalUnit>> it = actualRoots.iterator();
				while (it.hasNext()) {
					Node<OrganizationalUnit> actualRoot = it.next();
					Tree<OrganizationalUnit> tree = new Tree<OrganizationalUnit>(actualRoot);
					//get root leaves
					getRootLeaves(hierarchy.getName(), hierarchy.getCompany(), actualRoot);
					//get root children nodes
					getChildrenByLevel(hierarchy.getName(), hierarchy.getCompany(), actualRoot);
					// add tree to the forest
					toReturn.add(tree);
				}
			}
		} catch (Exception e) {
			logger.error("Unable to get root node for hierarchy " + hierarchy.getName());
		}
		logger.debug("OUT");
		return toReturn;

	}
	private List<Node<OrganizationalUnit>> getRootLeaves (String hierarchy, String company, Node<OrganizationalUnit> parent){
		List<Node<OrganizationalUnit>> children = new ArrayList<Node<OrganizationalUnit>>();
		HashMap<String, String> params = new HashMap<String, String>();
		params.put(HIERARCHY, hierarchy);
		params.put(COMPANY, company);
		try {
			children = getLeavesByQueryString(getRootLeaves, params, parent);
		} catch (Exception e) {
			logger.error("Unable to get node for hiererchy "+hierarchy);
		}
		return children;
		
	}

	private void executeQuery(String sqlQuery, String type, List toReturn) throws Exception {
		Connection con = null;
		try {

			con = getJNDIConnection();
			Statement stmt = con.createStatement();
			logger.debug(sqlQuery);
			ResultSet rs = stmt.executeQuery(sqlQuery);
			while (rs.next()) {

				if(type.equals(HIERARCHY)){						
					String hierName =  rs.getString("HIERARCHY");
					String company =  rs.getString("COMPANY");
					if(hierName != null){					
						OrganizationalUnitHierarchy item = new OrganizationalUnitHierarchy(null, hierName, hierName, null, null, company);
						toReturn.add(item);
					}

				}else if(type.equals(OU)){						

					String ouName =  rs.getString("NAME");
					String ouCode =  rs.getString("CODE");
					if (ouCode != null) {
						OrganizationalUnit item = new OrganizationalUnit(null, ouCode, ouName, null);
						toReturn.add(item);
					}
				}

			}
			rs.close();
			stmt.close();

		} catch (Exception e) {
			logger.error("Unable to execute query :"+e.getMessage());
		} finally {
			if(con != null)
				con.close();
		}
	}	
	private boolean getChildrenByLevel (String hierarchy, String company, Node<OrganizationalUnit> parent){
		boolean isToBreak = false;

		HashMap<String, String> params = new HashMap<String, String>();
		params.put(HIERARCHY, hierarchy);
		params.put(COMPANY, company);
		try {
			List<Node<OrganizationalUnit>> children  = getNodeByQueryString(getChildrenByLevel, params, parent);
			if(children == null || children.isEmpty()){
				isToBreak = true;
			}	

		} catch (Exception e) {
			logger.error("Unable to get node for hiererchy "+hierarchy);
		}
		return isToBreak;
		
	}
	private List<Node<OrganizationalUnit>> getRootByQueryString(String sqlQuery, HashMap<String, String> parameters, Node<OrganizationalUnit> ouParentNode) throws Exception {
		List<Node<OrganizationalUnit>> toReturn = new ArrayList<Node<OrganizationalUnit>>();
		Connection con = null;
		try {

			con = getJNDIConnection();
			PreparedStatement pstmt = con.prepareStatement(sqlQuery);
			pstmt.setString(1, (String)parameters.get(HIERARCHY));
			pstmt.setString(2, (String)parameters.get(COMPANY));
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				
				String code =  rs.getString(CODE);
				String name =  rs.getString(NAME);
				
				OrganizationalUnit ou = new OrganizationalUnit();
				ou.setDescription("");
				ou.setLabel(code);
				ou.setName(name);
				String path = "";
				if (ouParentNode != null) {
					path = ouParentNode.getPath();
				}
				if (code != null) {
					Node<OrganizationalUnit> node = new Node<OrganizationalUnit>(ou, path + S + ou.getLabel(), ouParentNode);
					toReturn.add(node);				
				}

			}
			rs.close();
			pstmt.close();

		} catch (Exception e) {
			logger.error("Unable to get root by query :"+sqlQuery);
		} finally {
			if (con != null) {
				con.close();
			}
		}
		return toReturn;
	}
	private List<Node<OrganizationalUnit>> getLeavesByQueryString(String query, HashMap<String, String> parameters, Node<OrganizationalUnit> parent) throws Exception {
		List<Node<OrganizationalUnit>> children = new ArrayList<Node<OrganizationalUnit>>();
		Connection con = null;

		try {

			con = getJNDIConnection();
			PreparedStatement pstmt = con.prepareStatement(query);
			pstmt.setString(1, (String)parameters.get(HIERARCHY));
			pstmt.setString(2, (String)parameters.get(COMPANY));


			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				
				String code =  rs.getString(CODE);
				String name =  rs.getString(NAME);
				
				OrganizationalUnit ou =new OrganizationalUnit();
				ou.setDescription("");
				ou.setLabel(code);
				ou.setName(name);
				String path =  parent.getPath();

				if(code != null){
					Node<OrganizationalUnit> node = new Node<OrganizationalUnit>(ou, path + S+ ou.getLabel(), parent);		
					children.add(node);
				}
			}
			rs.close();
			pstmt.close();
			parent.setChildren(children);

		} catch (SQLException sqle) {
			sqle.printStackTrace();
			logger.error("Unable to get node by query :"+sqle.getMessage());

		} finally {
			if(con != null) {
				con.close();
			}
		}
		return children;
	}
	private List<Node<OrganizationalUnit>> getNodeByQueryString(String query, HashMap<String, String> parameters, Node<OrganizationalUnit> parent) throws Exception {
		List<Node<OrganizationalUnit>> children = parent.getChildren();
		if(children == null){
			children = new ArrayList<Node<OrganizationalUnit>>();
		}
		int level = 2;

		if(parent != null){
			StringTokenizer st = new StringTokenizer(parent.getPath(), S, false);
			level = st.countTokens()+1;
		}
		logger.debug("level "+level+" for parent "+parent.getNodeContent().getLabel()+" with path:"+parent.getPath());
		Connection con = null;

		String replacedQuery = query.replaceAll("\\!", Integer.toString(level));
		replacedQuery = replacedQuery.replaceAll("\\%", Integer.toString(level-1));

		try {

			con = getJNDIConnection();
			PreparedStatement pstmt = con.prepareStatement(replacedQuery);
			pstmt.setString(1, (String)parameters.get(HIERARCHY));
			pstmt.setString(2, (String)parameters.get(COMPANY));
			pstmt.setString(3, parent.getNodeContent().getLabel());
			pstmt.setString(4, parent.getNodeContent().getName());


			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				
				String code =  rs.getString(CODE);
				String name =  rs.getString(NAME);
				
				OrganizationalUnit ou =new OrganizationalUnit();
				ou.setDescription("");
				ou.setLabel(code);
				ou.setName(name);
				String path = "";
				if(level != 1){
					path = parent.getPath();
				}
				if(code != null){
					Node<OrganizationalUnit> node = new Node<OrganizationalUnit>(ou, path + S+ ou.getLabel(), parent);		
					children.add(node);
				}
			}
			rs.close();
			pstmt.close();
			
			///sets children to parent node
			parent.setChildren(children);

			for(int i=0 ; i<children.size(); i++){
				Node<OrganizationalUnit> node = (Node<OrganizationalUnit>)children.get(i);
				if(level == 15){
					break;
				}
				getNodeByQueryString(query, parameters, node);

			}

		} catch (SQLException sqle) {
			sqle.printStackTrace();
			logger.error("Unable to get node by query :"+sqle.getMessage());

		} finally {
			if (con != null) {
				con.close();
			}
		}
		return children;
	}


}
