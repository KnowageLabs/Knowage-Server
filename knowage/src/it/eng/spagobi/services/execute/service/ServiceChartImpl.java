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

package it.eng.spagobi.services.execute.service;


import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.engines.chart.bo.ChartImpl;
import it.eng.spagobi.engines.chart.utils.DatasetMap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * @author Angelo Bernabei
 *         angelo.bernabei@eng.it
 * @author Giulio Gavardi
 *         giulio.gavardi@eng.it
 *
 */
public class ServiceChartImpl {

	private static transient Logger logger=Logger.getLogger(ServiceChartImpl.class);


	public byte[] executeChart(String token,String userId,String label,HashMap parameters){

		logger.debug("IN");
		Monitor monitor =MonitorFactory.start("spagobi.service.execute.executeChart");
		logger.debug("Getting profile");
		
		byte[] returnImage=null;
		IEngUserProfile userProfile=null;

		try {
			userProfile=it.eng.spagobi.commons.utilities.GeneralUtilities.createNewUserProfile(userId);
		} catch (Exception e2) {
			logger.error("Error recovering profile",e2);
			return "".getBytes();
		}


		logger.debug("Getting the chart object");

		IBIObjectDAO dao;
		BIObject obj=null;
		try {
			dao = DAOFactory.getBIObjectDAO();
			if(label!=null)
				obj=dao.loadBIObjectByLabel(label); 
		} catch (EMFUserError e) {
			logger.error("Error in recovering object",e);
			return "".getBytes();
		}


		//***************************** GET THE TEMPLATE******************************************

		if(obj!=null){

			logger.debug("Getting template");

			SourceBean content = null;
			byte[] contentBytes = null;
			try{
				ObjTemplate template = DAOFactory.getObjTemplateDAO().getBIObjectActiveTemplate(obj.getId());
				if(template==null) throw new Exception("Active Template null");
				contentBytes = template.getContent();
				if(contentBytes==null) throw new Exception("Content of the Active template null"); 

				// get bytes of template and transform them into a SourceBean

				String contentStr = new String(contentBytes);
				content = SourceBean.fromXMLString(contentStr);
			} catch (Exception e) {
				logger.error("Error in reading template",e);
				return "".getBytes();
			}

			String type=content.getName();
			String subtype = (String)content.getAttribute("type");

			String data="";
			try{
				if(obj.getDataSetId()!=null){
					data=obj.getDataSetId().toString();
				} else {
					throw new Exception("Data Set not defined");				    
				}
			}catch (Exception e) {
				logger.error("Error in reading dataset",e);
				return "".getBytes();
			}



			//***************************** GET PARAMETERS******************************************


			logger.debug("Getting parameters");

			HashMap parametersMap=null;

			//Search if the chart has parameters

			List parametersList=null;
			try {
				parametersList=DAOFactory.getBIObjectDAO().getBIObjectParameters(obj);
			} catch (EMFUserError e1) {
				logger.error("Error in retrieving parameters", e1);
				return "".getBytes();			}
			parametersMap=new HashMap();
			if(parametersList!=null && !parametersList.isEmpty()){
				for (Iterator iterator = parametersList.iterator(); iterator.hasNext();) {
					BIObjectParameter par= (BIObjectParameter) iterator.next();
					String url=par.getParameterUrlName();

					String value=(String)parameters.get(url);
					//List values=par.getParameterValues();
					if(value!=null){
						parametersMap.put(url, value);
					}
				}	
			}
			
			// if there are other parameters (like targets or baseline) that do not belong to the BiObject pass those anyway, extend this behaviour if necessary
			for (Iterator iterator = parameters.keySet().iterator(); iterator.hasNext();) {
				String namePar = (String) iterator.next();
				if(namePar.startsWith("target") || namePar.startsWith("baseline")){
					Object value=parameters.get(namePar);
					parametersMap.put(namePar, value);
				}
			}
			

			logger.debug("Creating the chart");

			ChartImpl sbi=null;	

			// set the right chart type
			sbi=ChartImpl.createChart(type, subtype);
			sbi.setProfile(userProfile);
			sbi.setType(type);
			sbi.setSubtype(subtype);
			sbi.setData(data);
			sbi.setParametersObject(parametersMap);
			// configure the chart with template parameters
			sbi.configureChart(content);


			DatasetMap datasets=null;
			try{
				datasets=sbi.calculateValue();
			}	
			catch (Exception e) {
				logger.error("Error in reading the value, check the dataset",e);
				return "".getBytes();
			}

			JFreeChart chart=null;
			// create the chart
			chart = sbi.createChart(datasets);

			ByteArrayOutputStream out=null;
			try{

				logger.debug("Write PNG Image");

				out=new ByteArrayOutputStream();	
				ChartUtilities.writeChartAsPNG(out, chart, sbi.getWidth(), sbi.getHeight());
				returnImage=out.toByteArray();

			}
			catch (Exception e) {
				logger.error("Error while creating the image",e);
				return "".getBytes();
			}
			finally{
				try {
					out.close();
				} catch (IOException e) {
					logger.error("Error while closing stream",e);
				}
				monitor.stop();
			}
			//out.flush();
		}

		return returnImage;


	}
	
	

}
