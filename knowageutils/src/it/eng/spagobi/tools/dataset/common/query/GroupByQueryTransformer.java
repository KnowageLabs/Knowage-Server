/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
