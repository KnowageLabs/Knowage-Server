/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.ou.provider;

import it.eng.spago.base.SourceBean;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;


public class OrganizationalUnitListProviderFoodmart extends
		OrganizationalUnitListProvider {

	String S = Tree.NODES_PATH_SEPARATOR;
	static private Logger logger = Logger
			.getLogger(OrganizationalUnitListProviderFoodmart.class);
	
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
			if (initialContext == null) {
				logger.error("JNDI problem. Cannot get InitialContext.");
			}
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
				boolean isToBreak = executeQuery(replacedQuery, OU, toReturn);
				if(isToBreak){
					continue;
					
				}
			} catch (Exception e) {
				logger.error("Error getting OU list");
			}
		}
		return toReturn;
	}
	
	@Override
	public List<Tree<OrganizationalUnit>> getHierarchyStructure(
			OrganizationalUnitHierarchy hierarchy) {
		List<Tree<OrganizationalUnit>> toReturn = new ArrayList<Tree<OrganizationalUnit>>();
		HashMap<String, String> params = new HashMap<String, String>();
		params.put(HIERARCHY, hierarchy.getName());
		params.put(COMPANY, hierarchy.getCompany());
		Tree<OrganizationalUnit> tree = null;
		try {
			Node<OrganizationalUnit> rootNode = getRootByQueryString(getRootByHierarchy, params, null);
			if(rootNode != null){
				tree = new Tree<OrganizationalUnit>(rootNode);

				getChildrenByLevel(hierarchy.getName(), hierarchy.getCompany(), rootNode);

				toReturn.add(tree);
			}
		} catch (Exception e) {
			logger.error("Unable to get root node for hiererchy "+hierarchy.getName());
		}
		return toReturn;
	}


	private boolean executeQuery(String sqlQuery, String type, List toReturn) throws Exception {
		Connection con = null;
		boolean isToBreak = false;
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
					if(ouCode != null){
						OrganizationalUnit item = new OrganizationalUnit(null, ouCode, ouName, null);
						toReturn.add(item);
					}else{
						isToBreak = true;
						break;
					}
				}

			}
			rs.close();
			stmt.close();

		} catch (Exception e) {
			logger.error("Unable to execute query :"+e.getMessage());
		}finally{
			if(con != null)
				con.close();
			return isToBreak;
		}
	}	
	private boolean getChildrenByLevel (String hierarchy, String company, Node<OrganizationalUnit> parent){
		boolean isToBreak = false;

		HashMap<String, String> params = new HashMap<String, String>();
		params.put(HIERARCHY, hierarchy);
		try {
			List<Node<OrganizationalUnit>> children  = getNodeByQueryString(getChildrenByLevel, params, parent);
			if(children == null || children.isEmpty()){
				isToBreak = true;
			}	

		} catch (Exception e) {
			logger.error("Unable to get node for hiererchy "+hierarchy);
		}finally{
			return isToBreak;
		}
		
	}
	private Node<OrganizationalUnit> getRootByQueryString(String sqlQuery, HashMap<String, String> parameters, Node<OrganizationalUnit> ouParentNode) throws Exception {
		Node<OrganizationalUnit> root = null;
		Connection con = null;
		try {

			con = getJNDIConnection();
			PreparedStatement pstmt = con.prepareStatement(sqlQuery);
			pstmt.setString(1, (String)parameters.get(HIERARCHY));
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				
				String code =  rs.getString(CODE);
				String name =  rs.getString(NAME);
				
				OrganizationalUnit ou =new OrganizationalUnit();
				ou.setDescription("");
				ou.setLabel(code);
				ou.setName(name);
				String path = "";
				if(ouParentNode != null){
					path = ouParentNode.getPath();
				}
				if(code != null){
					root = new Node<OrganizationalUnit>(ou, path + S + ou.getLabel(), ouParentNode);				
				}

			}
			rs.close();
			pstmt.close();

		} catch (Exception e) {
			logger.error("Unable to get root by query :"+sqlQuery);
		}finally{
			if(con != null)
				con.close();
			return root;
		}
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
		String field = "store_state";
		if(level == 3){
			field = "store_city";
		}else if(level == 4){
			field = "store_name";
		}
		String replacedQuery = query.replaceAll("\\!", field);
		if(level == 3){
			replacedQuery+= " and store_state = '"+parent.getNodeContent().getName()+"'";
		}
		if(level == 4){
			replacedQuery+= " and store_city = '"+parent.getNodeContent().getName()+"' and store_state ='"+parent.getParent().getNodeContent().getName()+"'";
		}
		try {

			con = getJNDIConnection();
			PreparedStatement pstmt = con.prepareStatement(replacedQuery);
			pstmt.setString(1, (String)parameters.get(HIERARCHY));

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
				if(level == 4){
					break;
				}
				getNodeByQueryString(query, parameters, node);

			}

		} catch (SQLException sqle) {
			sqle.printStackTrace();
			logger.error("Unable to get node by query :"+sqle.getMessage());

		}finally{
			if(con != null)
				con.close();
			return children;
		}
	}


}
