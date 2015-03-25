/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.kpi.service;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.dispatching.action.AbstractHttpAction;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;

public class CreateTrendAction extends AbstractHttpAction{
	
	private static transient Logger logger=Logger.getLogger(CreateTrendAction.class);
	protected String publisher_Name= "TREND_DEFAULT_PUB";//Kpi metadata default publisher
	
	public void service(SourceBean serviceRequest, SourceBean serviceResponse)
	throws Exception {
		logger.debug("IN");
		String pub_Name = (String)serviceRequest.getAttribute("trend_publisher_Name");
		if(pub_Name!=null && !pub_Name.equals("")){
			publisher_Name = pub_Name;
		}
		IMessageBuilder msgBuilder = MessageBuilderFactory.getMessageBuilder();
		RequestContainer requestContainer = RequestContainer.getRequestContainer();
		HttpServletRequest request = getHttpRequest();
		SessionContainer session = requestContainer.getSessionContainer();
		IEngUserProfile profile = (IEngUserProfile) session.getPermanentContainer().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		String tempRId = (String)serviceRequest.getAttribute("RESOURCE_ID");
		logger.debug("Got Resource ID:"+(tempRId!=null ? tempRId : "null"));
		Integer resID = null;
		if (tempRId!= null){
			resID = new Integer(tempRId);
		}
		String endDate = (String)serviceRequest.getAttribute("END_DATE");
		logger.debug("Got End Date:"+(endDate!=null ? endDate : "null"));
		String TimeRangeFrom = (String)serviceRequest.getAttribute("TimeRangeFrom");
		logger.debug("Got TimeRangeFrom:"+(TimeRangeFrom!=null ? TimeRangeFrom : "null"));
		String TimeRangeTo = (String)serviceRequest.getAttribute("TimeRangeTo");
		logger.debug("Got TimeRangeTo:"+(TimeRangeTo!=null ? TimeRangeTo : "null"));
		String resName = (String)serviceRequest.getAttribute("RESOURCE_NAME");
		logger.debug("Got resource name:"+(resName!=null ? resName : "null"));
		String tempKpiInstId = (String)serviceRequest.getAttribute("KPI_INST_ID");
		logger.debug("Got KpiInstance ID:"+(tempKpiInstId!=null ? tempKpiInstId : "null"));
		Integer kpiInstId = new Integer(tempKpiInstId);
		String formatSB = (SingletonConfig.getInstance().getConfigValue("SPAGOBI.DATE-FORMAT-SERVER.format"));
		String format = formatSB;
		logger.debug("Got Date format: "+(format!=null ? format : "null"));
		SimpleDateFormat f = new SimpleDateFormat();
		f.applyPattern(format);	
		Date d = new Date();
		d = f.parse(endDate);
		Long milliseconds = d.getTime();
		Calendar calendar = new GregorianCalendar();
		int ore = calendar.get(Calendar.HOUR); 
		int minuti = calendar.get(Calendar.MINUTE); 
		int secondi = calendar.get(Calendar.SECOND); 
		int AM = calendar.get(Calendar.AM_PM);//if AM then int=0, if PM then int=1
		if(AM==0){
			int millisec =  (secondi*1000) + (minuti *60*1000) + (ore*60*60*1000);
			Long milliSecToAdd = new Long (millisec);
			milliseconds = new Long(milliseconds.longValue()+milliSecToAdd.longValue());
			d = new Date(milliseconds);
		}else{
			int millisec =  (secondi*1000) + (minuti *60*1000) + ((ore+12)*60*60*1000);
			Long milliSecToAdd = new Long (millisec);
			milliseconds = new Long(milliseconds.longValue()+milliSecToAdd.longValue());
			d = new Date(milliseconds);
		}  
		Date timeFrom = null;
		if (TimeRangeFrom!=null){
			timeFrom = f.parse(TimeRangeFrom);
		}
		Date timeTo = null;
		if (TimeRangeTo!=null){
			timeTo = f.parse(TimeRangeTo);
		}
		
		serviceResponse.setAttribute("kpiInstId", kpiInstId);
		serviceResponse.setAttribute("resName", resName!=null ? resName : "");
		serviceResponse.setAttribute("TimeRangeFrom", TimeRangeFrom!=null ? TimeRangeFrom : "");
		serviceResponse.setAttribute("TimeRangeTo", TimeRangeTo!=null ? TimeRangeTo : "");
		serviceResponse.setAttribute("endDate", endDate);
		if(timeFrom!=null){
			serviceResponse.setAttribute("timeFrom", timeFrom);
		}
		if(timeTo!=null){
			serviceResponse.setAttribute("timeTo", timeTo);
		}
		serviceResponse.setAttribute("d", d);
		if(resID!=null){
			serviceResponse.setAttribute("resID", resID);
		}
		
		serviceResponse.setAttribute("publisher_Name", publisher_Name);
		
		logger.debug("OUT");
	}
}
