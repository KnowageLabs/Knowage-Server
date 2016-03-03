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
