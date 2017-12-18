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
package it.eng.qbe.datasource.naming;

import it.eng.qbe.datasource.configuration.CompositeDataSourceConfiguration;
import it.eng.qbe.datasource.configuration.IDataSourceConfiguration;
import it.eng.spagobi.tools.datasource.bo.IDataSource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * The Class QbeNamingStrategy.
 * 
 * @author Andrea Gioia
 */
public class SimpleDataSourceNamingStrategy implements IDataSourceNamingStrategy {
	
	public static final String DATASOURCE_NAME_SUFFIX = "DS";	
	public static final String STRING_SEPARETOR = "_";
	
    public static transient Logger logger = Logger.getLogger(SimpleDataSourceNamingStrategy.class);
	
    /* (non-Javadoc)
	 * @see it.eng.qbe.naming.NamingStrategy#getDatasourceName(java.util.List, it.eng.qbe.datasource.DBConnection)
	 */
	public String getDataSourceName(IDataSourceConfiguration configuration) {
		IDataSource connection = (IDataSource)configuration.loadDataSourceProperties().get("datasource");
		List<String> modelNames = new ArrayList<String>();
		if(configuration instanceof CompositeDataSourceConfiguration){
			CompositeDataSourceConfiguration cc = (CompositeDataSourceConfiguration)configuration;
			Iterator<IDataSourceConfiguration> it = cc.getSubConfigurations().iterator();
			while(it.hasNext()) modelNames.add(it.next().getModelName());
		} else {
			modelNames.add(configuration.getModelName());
		}
		String datasourceName = getDatasourceUnqualifiedName(modelNames, connection);
		return datasourceName + STRING_SEPARETOR + DATASOURCE_NAME_SUFFIX;
	}	
	
	
	private String getDatamartName(List datamartNames) {
		String datamartName = getDatamartUnqualifiedName(datamartNames);
		return datamartName;
	}	
	
	private String getDatamartUnqualifiedName(List datamartNames) {
		String name = null;
				
		name = "";
		for(int i = 0; i < datamartNames.size(); i++) {
			name += (i==0?"":"_") + (String)datamartNames.get(i);
		}
		
		if(datamartNames.size()>1){
			name = "_" + name;		
		}
		
		return name;
	}
	
	private String getDatasourceUnqualifiedName(List datamartNames, IDataSource connection) {
		String datasourceName = getDatamartName(datamartNames);
		if(connection!= null){
			if ( connection.checkIsJndi()) {
				datasourceName += "@" + connection.getJndi();
			} else {
				datasourceName += "@" + connection.getUser() + "@" + connection.getUrlConnection();
			}
		}

		logger.info("Using " + datasourceName + " as datasource unqualified name");
		return datasourceName;
	}
}
