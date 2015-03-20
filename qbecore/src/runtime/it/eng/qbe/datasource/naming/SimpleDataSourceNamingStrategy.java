/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
