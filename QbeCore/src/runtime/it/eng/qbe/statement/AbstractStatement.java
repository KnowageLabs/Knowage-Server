/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.statement;

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelField;
import it.eng.qbe.query.IQueryField;
import it.eng.qbe.query.ISelectField;
import it.eng.qbe.query.Query;
import it.eng.spagobi.utilities.objects.Couple;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Andrea Gioia
 */
public abstract class  AbstractStatement implements IStatement {

	

	
	
	IDataSource dataSource;
	

	Query query;
	
	
	Map parameters;
	
	
	String queryString;
	
	

	/** The max results. */
	int maxResults;
	
	/** If it is true (i.e. the maxResults limit is exceeded) then query execution should be stopped */
	boolean isBlocking;

	/** The fetch size. */
	int fetchSize;
	
	/** The offset. */
	int offset;
	
	/**
	 * Instantiates a new basic statement.
	 * 
	 * @param dataMartModel the data mart model
	 */
	protected AbstractStatement(IDataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	/**
	 * Create a new statement from query bound to the specific datamart-model
	 * 
	 * @param dataMartModel the data mart model
	 * @param query the query
	 */
	protected AbstractStatement(IDataSource dataSource, Query query) {
		this.dataSource = dataSource;
		this.query = query;
	}
	
	public IDataSource getDataSource() {
		return dataSource;
	}
	
	public Query getQuery() {
		return query;
	}
	
	public void setQuery(Query query) {
		this.query = query;
		this.queryString = null;
	}
	
	
	public Map getParameters() {
		return parameters;
	}

	
	public void setParameters(Map parameters) {
		this.parameters = parameters;
	}	
	
	/* (non-Javadoc)
	 * @see it.eng.qbe.model.IStatement#getOffset()
	 */
	public int getOffset() {
		return offset;
	}

	/* (non-Javadoc)
	 * @see it.eng.qbe.model.IStatement#setOffset(int)
	 */
	public void setOffset(int offset) {
		this.offset = offset;
	}
	
	/*
	 * (non-Javadoc)
	 * @see it.eng.qbe.model.IStatement#getFetchSize()
	 */
	public int getFetchSize() {
		return fetchSize;
	}

	/* (non-Javadoc)
	 * @see it.eng.qbe.model.IStatement#setFetchSize(int)
	 */
	public void setFetchSize(int fetchSize) {
		this.fetchSize = fetchSize;
	}

	/* (non-Javadoc)
	 * @see it.eng.qbe.model.IStatement#getMaxResults()
	 */
	public int getMaxResults() {
		return maxResults;
	}

	/* (non-Javadoc)
	 * @see it.eng.qbe.model.IStatement#setMaxResults(int)
	 */
	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}
	
	public boolean isMaxResultsLimitBlocking() {
		return isBlocking;
	}

	public void setIsMaxResultsLimitBlocking(boolean isBlocking) {
		this.isBlocking = isBlocking;
	}
	
	public String getQueryString() {
		return queryString;
	}	
	
	protected void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	public abstract String getValueBounded(String operandValueToBound, String operandType);
	
	public String getNextAlias(Map entityAliasesMaps) {
		int aliasesCount = 0;
		Iterator it = entityAliasesMaps.keySet().iterator();
		while(it.hasNext()) {
			String key = (String)it.next();
			Map entityAliases = (Map)entityAliasesMaps.get(key);
			aliasesCount += entityAliases.keySet().size();
		}
		
		return "t_" + aliasesCount;
	}


	private String getEntityAlias(IModelField datamartField, Map entityAliases, Map entityAliasesMaps){
		IModelEntity rootEntity;
		
		Couple queryNameAndRoot = datamartField.getQueryName();

		
		if(queryNameAndRoot.getSecond()!=null){
			rootEntity = (IModelEntity)queryNameAndRoot.getSecond(); 	
		}else{
			rootEntity = datamartField.getParent().getRoot(); 	
		}

		String rootEntityAlias = (String)entityAliases.get(rootEntity.getUniqueName());
		if(rootEntityAlias == null) {
			rootEntityAlias = getNextAlias(entityAliasesMaps);
			entityAliases.put(rootEntity.getUniqueName(), rootEntityAlias);
		}
		
		return rootEntityAlias;
	}
	
	public String getFieldAliasNoRoles(IModelField datamartField, Map entityAliases, Map entityAliasesMaps){
		

		
		Couple queryNameAndRoot = datamartField.getQueryName();
		
		String queryName = (String) queryNameAndRoot.getFirst();
		

		String rootEntityAlias = getEntityAlias(datamartField, entityAliases, entityAliasesMaps);


		return buildFieldQueryNameWithEntityAlias(rootEntityAlias, queryName);//queryName.substring(0,1).toLowerCase()+queryName.substring(1);
	}
	
	/**
	 * Creates a list of clauses. for each role of the entity create this clause part: role+.+field name.
	 * This function is used to create the joins for the entities 
	 * @param datamartField
	 * @param entityAliases
	 * @param entityAliasesMaps
	 * @param roleName
	 * @return
	 */
	public String getFieldAliasWithRoles(IModelField datamartField, Map entityAliases, Map entityAliasesMaps, String roleName){
		
		IModelEntity rootEntity;
		
		Couple queryNameAndRoot = datamartField.getQueryName();
		
		String queryName = (String) queryNameAndRoot.getFirst();
		
		if(queryNameAndRoot.getSecond()!=null){
			rootEntity = (IModelEntity)queryNameAndRoot.getSecond(); 	
		}else{
			rootEntity = datamartField.getParent().getRoot(); 	
		}
		
		
		Set<String> entityRoleAlias = getQuery().getEntityRoleAlias(rootEntity, getDataSource());
		
		String rootEntityAlias = getEntityAlias(datamartField, entityAliases, entityAliasesMaps);

		//if there is no role for this field
		if(entityRoleAlias!=null){
			rootEntityAlias = buildEntityAliasWithRoles(rootEntity, roleName, rootEntityAlias);
		}

		
		return  buildFieldQueryNameWithEntityAlias(rootEntityAlias, queryName);//.substring(0,1).toLowerCase()+queryName.substring(1);
	}
	
	/**
	 * Creates a list of clauses. for each role of the entity create this clause part: role+.+field name.
	 * This function is used to create the joins for the entities 
	 * @param datamartField
	 * @param entityAliases
	 * @param entityAliasesMaps
	 * @param roleName
	 * @return
	 */
	public List<String> getFieldAliasWithRolesList(IModelField datamartField, Map entityAliases, Map entityAliasesMaps){
		
		List<String> toReturn = new ArrayList<String>();
		
		IModelEntity rootEntity;
		
		Couple queryNameAndRoot = datamartField.getQueryName();
		
		String queryName = (String) queryNameAndRoot.getFirst();
		
		if(queryNameAndRoot.getSecond()!=null){
			rootEntity = (IModelEntity)queryNameAndRoot.getSecond(); 	
		}else{
			rootEntity = datamartField.getParent().getRoot(); 	
		}
		
		
		Map<String, List<String>> roleAliasMap = getQuery().getMapEntityRoleField( getDataSource()).get(rootEntity);
		Set<String> roleAlias = null;
		if(roleAliasMap!=null){
			roleAlias = roleAliasMap.keySet();
		}
		
		String rootEntityAlias = getEntityAlias(datamartField, entityAliases, entityAliasesMaps);
		
		if(roleAlias!=null && roleAlias.size()>1){
			Iterator<String> iter = roleAlias.iterator();
			while(iter.hasNext()){
				String firstRole = iter.next();
				String rootEntityAliasWithRole = buildEntityAliasWithRoles(rootEntity, firstRole, rootEntityAlias);
				toReturn.add(buildFieldQueryNameWithEntityAlias(rootEntityAliasWithRole, queryName));//.substring(0,1).toLowerCase()+queryName.substring(1));
			}
		}else{
			toReturn.add(buildFieldQueryNameWithEntityAlias(rootEntityAlias, queryName));//queryName.substring(0,1).toLowerCase()+queryName.substring(1));
		}

		return toReturn;
	}
	
	/**
	 * Check if a select field with the alias exist and if so take it as field to get the query name.
	 * Used in FILTERS
	 * @param datamartField
	 * @param entityAliases
	 * @param entityAliasesMaps
	 * @param alias
	 * @return
	 */
	public String getFieldAliasWithRolesFromAlias(IModelField datamartField, Map entityAliases, Map entityAliasesMaps, String alias){
		Query query = this.getQuery();
		List<ISelectField> fields = query.getSelectFields(true);
		if(alias!=null){//if the field contains an alias, check if it contains a role
			for(int i=0; i<fields.size();i++){
				ISelectField field = fields.get(i);
				if(field.getAlias().equals(alias) && field.getName().equals(datamartField.getUniqueName())){
					return getFieldAliasWithRoles(datamartField, entityAliases, entityAliasesMaps, field);
				}
			}
		}		

		return getFieldAliasNoRoles(datamartField, entityAliases, entityAliasesMaps);
	}
	
	
	/**
	 * Used in select, group, order
	 */
	public String getFieldAliasWithRoles(IModelField datamartField, Map entityAliases, Map entityAliasesMaps, IQueryField queryField){
		
		IModelEntity rootEntity;
		
		Couple queryNameAndRoot = datamartField.getQueryName();
		String queryName = (String) queryNameAndRoot.getFirst();
		
		if(queryNameAndRoot.getSecond()!=null){
			rootEntity = (IModelEntity)queryNameAndRoot.getSecond(); 	
		}else{
			rootEntity = datamartField.getParent().getRoot(); 	
		}
		
		//List<List<Relationship>> roleAlias = (List<List<Relationship>>) rootEntity.getProperty(GraphUtilities.roleRelationsProperty);
		Map<String, List<String>> mapRoleField = getQuery().getMapEntityRoleField( getDataSource()).get(rootEntity);
		
		String rootEntityAlias = (String)entityAliases.get(rootEntity.getUniqueName());
		if(rootEntityAlias == null) {
			rootEntityAlias = getNextAlias(entityAliasesMaps);
			entityAliases.put(rootEntity.getUniqueName(), rootEntityAlias);
		}
		
		if(mapRoleField!=null && mapRoleField.keySet().size()>0){
			Iterator<String> iter = mapRoleField.keySet().iterator();
			while (iter.hasNext()) {
				String role = (String) iter.next();
				List<String> fieldsWithRole = mapRoleField.get(role);
				if(fieldsWithRole!=null && fieldsWithRole.contains(queryField.getAlias())){
					rootEntityAlias = buildEntityAliasWithRoles(rootEntity, role, rootEntityAlias);
					break;
				}
			}
		}

		return buildFieldQueryNameWithEntityAlias(rootEntityAlias, queryName);
	}
	
	
	/**
	 * Used to build the from clause
	 */
	public String buildFromEntityAliasWithRoles(IModelEntity me, String rel, String entityAlias){
		String fromClauseElement =  me.getName() + " "+ entityAlias;
		//for(int i=0; i<rel.size(); i++){
			fromClauseElement = fromClauseElement+("_"+rel).replace(" ", "");
		//}
		return fromClauseElement;
	}
	
	
	private String buildEntityAliasWithRoles(IModelEntity me, String role, String entityAlias){
		String fromClauseElement = (entityAlias+"_"+role).replace(" ", "");;
		return fromClauseElement;
	}
	
	
	protected String buildFieldQueryNameWithEntityAlias(String rootEntityAlias, String queryName) {
		return rootEntityAlias + "." + queryName;
	}
	
}
