/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.qbe.statement.hive;

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.query.Query;
import it.eng.qbe.statement.sql.SQLStatement;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class HiveQLStatement extends SQLStatement{
	private Map<String,String> fieldAliasMap = new HashMap<String, String>();
	
	public static transient Logger logger = Logger.getLogger(HiveQLStatement.class);
	
	protected HiveQLStatement(IDataSource dataSource) {
		super(dataSource);
	}
	
	public HiveQLStatement(IDataSource dataSource, Query query) {
		super(dataSource, query);
	}
	
	public String getFieldAlias(String rootEntityAlias, String queryName){
		return  queryName;
	}

}
