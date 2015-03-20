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
