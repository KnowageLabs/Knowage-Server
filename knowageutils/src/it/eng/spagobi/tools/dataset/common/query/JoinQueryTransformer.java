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
public class JoinQueryTransformer extends AbstractQueryTransformer {
	
	List selectColumnNames;
	List selectColumnAlias;
	String statmentToJoin;
	String statmentFKColumnName;
	String statmentToJoinRefColumnName;
	
	public JoinQueryTransformer() {
		this(null);
		selectColumnNames = new ArrayList();
		selectColumnAlias = new ArrayList();
	}
	
	public JoinQueryTransformer(IQueryTransformer previousTransformer) {
		super(previousTransformer);
		selectColumnNames = new ArrayList();
		selectColumnAlias = new ArrayList();
	}
	
	public Object execTransformation(Object statement) {
		String transformedStatment = null;
		
		String statement1Alias = "g1" + System.currentTimeMillis();
		String statement2Alias = "g2" + System.currentTimeMillis();
		
		transformedStatment = "SELECT ";
		for(int i = 0; i < selectColumnNames.size(); i++) {    		
    		String columnName = (String)selectColumnNames.get(i);
    		transformedStatment += (i==0)?"": ", ";
    		transformedStatment +=  columnName + " AS " + columnName;
    	}
		
		transformedStatment += " \nFROM ( \n" + statement + "\n ) " + statement1Alias;
		transformedStatment += ", (" + statmentToJoin + ") " + statement2Alias;
		
		transformedStatment += " \nWHERE " + statement1Alias + "." + statmentFKColumnName;
		transformedStatment += " = " + statement2Alias + "." + statmentToJoinRefColumnName;
		
		return transformedStatment;
	}

	public void setJoinCondition(String statmentFKColumnName, String statmentToJoinRefColumnName) {
		this.statmentFKColumnName = statmentFKColumnName;
		this.statmentToJoinRefColumnName = statmentToJoinRefColumnName;
	}

	public void setStatmentToJoin(String statmentToJoin) {
		this.statmentToJoin = statmentToJoin;
	}

	
	public void addSelectColumn(String columnName) {
		addSelectColumn(columnName, null);
	}
	public void addSelectColumn(String columnName, String columnAlias) {
		selectColumnNames.add(columnName);
		selectColumnAlias.add(columnAlias);
	}
}
