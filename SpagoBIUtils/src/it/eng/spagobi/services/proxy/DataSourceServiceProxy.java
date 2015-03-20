/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.services.proxy;


import it.eng.spagobi.services.datasource.bo.SpagoBiDataSource;
import it.eng.spagobi.services.datasource.stub.DataSourceServiceServiceLocator;
import it.eng.spagobi.services.security.exceptions.SecurityException;
import it.eng.spagobi.tools.datasource.bo.DataSourceFactory;
import it.eng.spagobi.tools.datasource.bo.IDataSource;

import javax.servlet.http.HttpSession;
import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;

/**
 * This is a proxy for using the Data Source Service:
 * Ex.
 * 
	DataSourceServiceProxy proxyDS=new DataSourceServiceProxy();
	SpagoBiDataSource ds=proxyDS.getDataSource("2");
	logger.debug("DS="+ds.getJndiName());

 *
 */
public final class DataSourceServiceProxy extends AbstractServiceProxy{
    
	static private final String SERVICE_NAME = "DataSource Service";
	
    static private Logger logger = Logger.getLogger(DataSourceServiceProxy.class);
    

    /**
     * The Constructor.
     * 
     * @param user User ID utente
     * @param session Sessione http
     */
    public DataSourceServiceProxy(String user,HttpSession session) {
	super(user,session);
    }

    private DataSourceServiceProxy() {
	super();
    }   
    
    private it.eng.spagobi.services.datasource.stub.DataSourceService lookUp() throws SecurityException {
	try {
	    DataSourceServiceServiceLocator locator = new DataSourceServiceServiceLocator();
	    it.eng.spagobi.services.datasource.stub.DataSourceService service=null;
	    if (serviceUrl!=null ){
		    service = locator.getDataSourceService(serviceUrl);		
	    }else {
		    service = locator.getDataSourceService();		
	    }
	    return service;
	} catch (ServiceException e) {
	    logger.error("Impossible to locate [" + SERVICE_NAME + "] at [" + serviceUrl + "]");
	    throw new SecurityException("Impossible to locate [" + SERVICE_NAME + "] at [" + serviceUrl + "]", e);
	}
    }
    
    /**
     * Return the SpagoBiDataSource object, contains the information about
     * the DWH connection.
     * 
     * @param documentId document id
     * 
     * @return SpagoBiDataSource object
     */
    public IDataSource getDataSource(String documentId) {
    	IDataSource dataSource = null;
    	SpagoBiDataSource dataSourceConfig = null;
		
    	logger.debug("IN.documentId="+documentId);
		if (documentId==null || documentId.length()==0){
		    logger.error("documentId is NULL");
		    return null;
		}	
		try {
			dataSourceConfig = lookUp().getDataSource(readTicket(), userId,documentId);
			if(dataSourceConfig != null) {
				dataSource = DataSourceFactory.getDataSource( dataSourceConfig );
			}
			
		} catch (Exception e) {
		    logger.error("Error during Service LookUp",e);
		}finally{
		    logger.debug("OUT");
		}
		return dataSource;
    }
    
    /**
     * Return the SpagoBiDataSource object, contains the information about
     * the DWH connection.
     * 
     * @param label document label
     * 
     * @return SpagoBiDataSource object
     */
    public IDataSource getDataSourceByLabel(String label) {
    	IDataSource dataSource = null;
    	SpagoBiDataSource dataSourceConfig = null;
		
    	
    	logger.debug("IN.label="+label);
		if (label==null || label.length()==0){
		    logger.error("label is NULL");
		    return null;
		}	
		try {
			dataSourceConfig = lookUp().getDataSourceByLabel(readTicket(), userId,label);
			if(dataSourceConfig != null) {
				dataSource = DataSourceFactory.getDataSource( dataSourceConfig );
			}
		} catch (Exception e) {
		    logger.error("Error during Service LookUp",e);
		}finally{
		    logger.debug("OUT");
		}
		return dataSource;
    }  
    
    /**
     * Return the SpagoBiDataSource object, contains the information about
     * the DWH connection.
     * 
     * @param id label of the datasource
     * 
     * @return SpagoBiDataSource object
     */
    public IDataSource getDataSourceById(int id) {
    	IDataSource dataSource = null;
    	SpagoBiDataSource dataSourceConfig = null;
		
    	
    	logger.debug("IN.id="+id);

		try {
			dataSourceConfig = lookUp().getDataSourceById(readTicket(), userId,id);
			if(dataSourceConfig != null) {
				dataSource = DataSourceFactory.getDataSource( dataSourceConfig );
			}
		} catch (Exception e) {
		    logger.error("Error during Service LookUp",e);
		}finally{
		    logger.debug("OUT");
		}
		return dataSource;
    }    

}
