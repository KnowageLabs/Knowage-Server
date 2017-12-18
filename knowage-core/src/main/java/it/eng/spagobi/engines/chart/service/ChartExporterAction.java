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
package it.eng.spagobi.engines.chart.service;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.analiticalmodel.document.handlers.ExecutionInstance;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.container.CoreContextManager;
import it.eng.spagobi.container.SpagoBISessionContainer;
import it.eng.spagobi.container.strategy.LightNavigatorContextRetrieverStrategy;
import it.eng.spagobi.engines.exporters.ChartExporter;
import it.eng.spagobi.utilities.assertion.Assert;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class ChartExporterAction extends AbstractSpagoBIAction {

	private static transient Logger logger=Logger.getLogger(ChartExporterAction.class);
	public static final String MULTICHART = "multichart";
	public static final String ORIENTATION = "orientation_multichart";
	public static final String OUTPUT_TYPE = "outputType";
	public static final String CONF = "CONF";

	/**
	 * This action is called by the user who wants to export the result of a Chart in PDF
	 * 
	 */

	public void doService() {
		logger.debug("IN");
		
		File tmpFile=null;
		boolean isMultichart = false;
		String orientationType = "";
		String outputType="";
		
		//HttpServletRequest httpRequest = getHttpRequest();
		//HttpSession session=httpRequest.getSession();

	//	this.freezeHttpResponse();

		try{
			// recover BiObject Name
			Integer id = this.getAttributeAsInteger(SpagoBIConstants.OBJECT_ID);
			if(id==null){
				logger.error("Document id not found");
				return;
			}
			BIObject document=DAOFactory.getBIObjectDAO().loadBIObjectById(id);
			String docName=document.getName();

			
//			**************get the template*****************
			logger.debug("getting template");

			SourceBean content = null;
			byte[] contentBytes = null;
			try{
				ObjTemplate template = DAOFactory.getObjTemplateDAO().getBIObjectActiveTemplate(Integer.valueOf(id));
				if(template==null) throw new Exception("Active Template null");
				contentBytes = template.getContent();
				if(contentBytes==null) {
					logger.error("TEMPLATE DOESN'T EXIST !!!!!!!!!!!!!!!!!!!!!!!!!!!");
					EMFUserError userError = new EMFUserError(EMFErrorSeverity.ERROR, 2007);
					userError.setBundle("messages");
					throw userError; 
				}

				// get bytes of template and transform them into a SourceBean

				String contentStr = new String(contentBytes);
				content = SourceBean.fromXMLString(contentStr);
			} catch (Exception e) {
				logger.error("Error while converting the Template bytes into a SourceBean object");
				EMFUserError userError = new EMFUserError(EMFErrorSeverity.ERROR, 2003);
				userError.setBundle("messages");
				throw userError;
			}
			

			// get all the data parameters 
			try{					
				Map dataParameters = new HashMap();
				SourceBean dataSB = (SourceBean)content.getAttribute(CONF);
				List dataAttrsList = dataSB.getContainedSourceBeanAttributes();
				Iterator dataAttrsIter = dataAttrsList.iterator();
				while(dataAttrsIter.hasNext()) {
					SourceBeanAttribute paramSBA = (SourceBeanAttribute)dataAttrsIter.next();
					SourceBean param = (SourceBean)paramSBA.getValue();
					String nameParam = (String)param.getAttribute("name");
					String valueParam = (String)param.getAttribute("value");
					dataParameters.put(nameParam, valueParam);
				}

				if(dataParameters.get(MULTICHART)!=null && (((String)dataParameters.get(MULTICHART)).equalsIgnoreCase("true") ))	
					isMultichart=true;
				
				orientationType="";
				if(dataParameters.get(ORIENTATION)!=null && !(((String)dataParameters.get(ORIENTATION)).equalsIgnoreCase("") )){	
					orientationType=(String)dataParameters.get(ORIENTATION);
				}
			}
			catch (Exception e) {
				logger.error(e.getCause()+" "+e.getStackTrace());
				logger.error("many error in reading data source parameters",e);
			}
					
			//Recover uuId				
			String uuId = this.getAttributeAsString("uuid");
			if(uuId==null) {	
				//Recover executionContextId	
				/*String executionContextId = (serviceRequest.getAttribute("executionContextId"))==null?"":(String)serviceRequest.getAttribute("executionContextId");
				RequestContainer requestContainer = getRequestContainer();
				SessionContainer sessionContainer = requestContainer.getSessionContainer();

				CoreContextManager contextManager = new CoreContextManager(new SpagoBISessionContainer(sessionContainer), 
						new LightNavigatorContextRetrieverStrategy(serviceRequest));
				logger.debug("sessionContainer and permanentContainer retrived");
				

				ExecutionInstance instance = contextManager.getExecutionInstance(executionContextId);
				*/
				ExecutionInstance executionInstance = getContext().getExecutionInstance( ExecutionInstance.class.getName() );
				Assert.assertNotNull(executionInstance, "Execution instance cannot be null");
				uuId = executionInstance.getExecutionId();
			}
			//Recover outputType		;
			outputType=this.getAttributeAsString(OUTPUT_TYPE);	

			ChartExporter exporter=new ChartExporter();
			
			String mimeType = "";
			if (outputType.equalsIgnoreCase("PDF")){
				mimeType = "application/pdf";
				tmpFile=exporter.getChartPDF(uuId, isMultichart, orientationType);
			}

			logger.debug("Chart exported succesfully");

			HttpServletResponse response = getHttpResponse();
			response.setContentType(mimeType);							
			response.setHeader("Content-Disposition", "filename=\"chart." + outputType + "\";");
			response.setContentLength((int) tmpFile.length());

			BufferedInputStream in = new BufferedInputStream(new FileInputStream(tmpFile));
			int b = -1;
			while ((b = in.read()) != -1) {
				response.getOutputStream().write(b);
			}
			response.getOutputStream().flush();
			in.close();
			logger.debug("OUT");


		} catch(Throwable e) {
			logger.error("An exception has occured", e);
		} finally {
			if ("PDF".equalsIgnoreCase(outputType)){
				tmpFile.delete();
			}

		}
	}

}
