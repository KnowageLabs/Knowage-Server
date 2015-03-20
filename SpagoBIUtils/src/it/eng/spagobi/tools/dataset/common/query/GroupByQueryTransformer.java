/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.common.query;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class GroupByQueryTransformer extends AbstractQueryTransformer{
	
	List groupByColumnNames;
	List groupByColumnAliases;
	List aggregateColumnNames;
	List aggregateFunctions;
	List aggregateColumnAliases;
	
	public GroupByQueryTransformer() {
		this(null);
		
	}
	
	public GroupByQueryTransformer(IQueryTransformer previousTransformer) {
		super(previousTransformer);
		groupByColumnNames = new ArrayList();
		groupByColumnAliases = new ArrayList();
		aggregateColumnNames = new ArrayList();
		aggregateFunctions = new ArrayList();
		aggregateColumnAliases = new ArrayList();
	}
	
	public Object execTransformation(Object statement) {
	
		String transformedStatment = null;
		String alias;
		
		String subQueryAlias = "t" + System.currentTimeMillis();
    	
		transformedStatment = "SELECT ";
		for(int i = 0; i < groupByColumnNames.size(); i++) {
			alias = (String)(groupByColumnAliases.get(i) == null?groupByColumnNames.get(i): groupByColumnAliases.get(i));
			alias = alias.trim();
			if( !(alias.startsWith("'") || alias.startsWith("\"")) ) {
				alias = "\"" + alias + "\"";
			}
			transformedStatment += (i>0)? ", ": "";
	    	transformedStatment += subQueryAlias + "." + groupByColumnNames.get(i) + " AS " + alias;
		}
		
    	for(int i = 0; i < aggregateColumnNames.size(); i++) {
    		
    		String aggFunc = (String)aggregateFunctions.get(i);
    		IAggregationFunction function = AggregationFunctions.get(aggFunc);
    		alias = (String)(aggregateColumnAliases.get(i) == null?aggregateColumnNames.get(i): aggregateColumnAliases.get(i));
    		if( !(alias.startsWith("'") || alias.startsWith("\"")) ) {
				alias = "\"" + alias + "\"";
			}
    		String columnName = (String)aggregateColumnNames.get(i);
    		columnName = columnName.trim().equalsIgnoreCase("*")? columnName: subQueryAlias + "." + columnName;
    		transformedStatment +=  ", " + function.apply(columnName) + " AS " + alias;
    	}
    	transformedStatment += " \nFROM ( " + statement + ") " + subQueryAlias;
    	transformedStatment += " \nGROUP BY ";
    	for(int i = 0; i < groupByColumnNames.size(); i++) {
    		transformedStatment += (i>0)? ", ": "";
    		transformedStatment += subQueryAlias + "." + groupByColumnNames.get(i);
    	}
    	
		return transformedStatment;
	}
	
	public void addGrouByColumn(String columnName) {
		addGrouByColumn(columnName, null);
	}
	public void addGrouByColumn(String columnName, String columnAlias) {
		groupByColumnNames.add(columnName);
		groupByColumnAliases.add(columnAlias);
	}
	
	public void addAggregateColumn(String columnName, String aggregationFunction) {
		this.addAggregateColumn(columnName, aggregationFunction, null);
	}
			
	public void addAggregateColumn(String columnName, String aggregationFunction, String columnAlias) {
		aggregateColumnNames.add(columnName);
		aggregateFunctions.add(aggregationFunction);
		aggregateColumnAliases.add(columnAlias);
	}
	
	
}
