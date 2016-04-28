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
package it.eng.spagobi.engines.jasperreport.services;

import java.io.File;

import org.apache.log4j.Logger;

import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.engines.jasperreport.JasperReportEngine;
import it.eng.spagobi.engines.jasperreport.JasperReportEngineInstance;
import it.eng.spagobi.engines.jasperreport.JasperReportEngineTemplate;
import it.eng.spagobi.services.proxy.DataSetServiceProxy;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.engines.AbstractEngineStartServlet;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.EngineStartServletIOManager;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;

/**
 * @authors
 * Andrea Gioia (andrea.gioia@eng.it)
 * Davide Zerbetto (davide.zerbetto@eng.it)
 */
public class JasperReportEngineStartAction extends AbstractEngineStartServlet {
	
	private static String CONNECTION_NAME="connectionName";
	private static String OUTPUT_TYPE = "outputType";
	
	    
	/** Logger component. */
    private static transient Logger logger = Logger.getLogger(JasperReportEngineStartAction.class);
    
	
	public void doService( EngineStartServletIOManager servletIOManager ) throws SpagoBIEngineException {
		
		IDataSource dataSource;
        IDataSet dataSet;
        String connectionName;
        String outputType;
        
        JasperReportEngineTemplate template;
        JasperReportEngineInstance engineInstance;
	
        logger.debug("IN");
        
        try {
        	// log some contextual infos
        	logger.debug("User: [" + servletIOManager.getUserId() + "]");
        	logger.debug("Document: [" + servletIOManager.getDocumentId() + "]");
        	
        	dataSource = servletIOManager.getDataSource();
        	logger.debug("Datasource: [" + (dataSource == null? dataSource: dataSource.getLabel()) + "]");
        	if (dataSource==null){
        		logger.warn("This document doesn't have the Data Source");
        	}
        	 
        	dataSet = servletIOManager.getDataSet();
        	logger.debug("Dataset: [" + (dataSet == null? dataSource: dataSet.getName()) + "]");
        	
        	// read and log builtin parameters
        	connectionName = servletIOManager.getParameterAsString(CONNECTION_NAME);
        	logger.debug("Parameter [" + CONNECTION_NAME + "] is equal to [" + connectionName + "]");
        	
        	outputType = servletIOManager.getParameterAsString(OUTPUT_TYPE);
        	logger.debug("Parameter [" + OUTPUT_TYPE + "] is equal to [" + outputType + "]");
        	if(StringUtilities.isEmpty(outputType)) {
        		outputType = JasperReportEngine.getConfig().getDefaultOutputType();
        		servletIOManager.getEnv().put(OUTPUT_TYPE, outputType);
        		logger.debug("Parameter [" + OUTPUT_TYPE + "] has been set to the default value [" + servletIOManager.getEnv().get(OUTPUT_TYPE) + "]");
        	}
        	
        	// this proxy is used by ScriptletChart to execute and embed external chart into report
        	servletIOManager.getEnv().put(EngineConstants.ENV_DOCUMENT_EXECUTE_SERVICE_PROXY, servletIOManager.getDocumentExecuteServiceProxy());
        	
        	servletIOManager.auditServiceStartEvent();
        	
        	
        	template = new JasperReportEngineTemplate(servletIOManager.getTemplateName(), servletIOManager.getTemplate());
        	
        	
        	File reportOutputDir = JasperReportEngine.getConfig().getReportOutputDir();
        	File reportFile = File.createTempFile("report", "." + outputType, reportOutputDir);
        	DataSetServiceProxy proxyDataset = servletIOManager.getDataSetServiceProxy();
        	
        	engineInstance = JasperReportEngine.createInstance( template, servletIOManager.getEnv() , proxyDataset);
        	engineInstance.setId(servletIOManager.getParameterAsString("SBI_EXECUTION_ID"));
        	servletIOManager.getHttpSession().setAttribute(engineInstance.getId(), engineInstance);
        	
        	
        	
        	engineInstance.runReport(reportFile, servletIOManager.getRequest());
        	
        	   
        	servletIOManager.writeBackToClient(200, reportFile, true, "report." + outputType, JasperReportEngine.getConfig().getMIMEType(outputType));
        	
        	// instant cleaning
        	reportFile.delete();

        	servletIOManager.auditServiceEndEvent();
        } catch(Throwable t) {
        	throw new SpagoBIEngineException("An error occurred while executing report. Check log file for more information", t);
        } finally {
        	logger.debug("OUT");        	 
        }        

	}

}
