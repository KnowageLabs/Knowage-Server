/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.kpi.service;

import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import it.eng.spago.base.SourceBean;
import it.eng.spago.dispatching.action.AbstractHttpAction;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.kpi.config.bo.Kpi;
import it.eng.spagobi.kpi.config.bo.KpiInstance;
import it.eng.spagobi.kpi.config.dao.IKpiInstanceDAO;
import it.eng.spagobi.kpi.model.bo.ModelInstanceNode;
import it.eng.spagobi.kpi.model.dao.IModelInstanceDAO;
import it.eng.spagobi.kpi.threshold.bo.Threshold;

public class CreateKpiMetadataAction extends AbstractHttpAction{
	
	private static transient Logger logger=Logger.getLogger(CreateKpiMetadataAction.class);
	protected String publisher_Name= "KPI_METADATA_DEFAULT_PUB";//Kpi metadata default publisher
	
	public void service(SourceBean serviceRequest, SourceBean serviceResponse)
	throws Exception {
		logger.debug("IN");
		String pub_Name = (String)serviceRequest.getAttribute("metadata_publisher_Name");
		if(pub_Name!=null && !pub_Name.equals("")){
			publisher_Name = pub_Name;
		}
		String kpiInstanceID = (String)serviceRequest.getAttribute("KPI_INST_ID");
		String kpiBeginDate = (String)serviceRequest.getAttribute("KPI_BEGIN_DATE");
		String kpiEndDate = (String)serviceRequest.getAttribute("KPI_END_DATE");
		String kpiTarget = (String)serviceRequest.getAttribute("KPI_TARGET");
		String kpiValue = (String)serviceRequest.getAttribute("KPI_VALUE");
		String kpiWeight = (String)serviceRequest.getAttribute("KPI_WEIGHT");
		String kpiValueDescr = (String)serviceRequest.getAttribute("KPI_VALUE_DESCR");
		String kpiModelInstanceId = (String)serviceRequest.getAttribute("KPI_MODEL_INST_ID");
		String weightedValue = (String)serviceRequest.getAttribute("WEIGHTED_VALUE");
		String kpiValID = (String)serviceRequest.getAttribute("KPI_VALUE_ID");
		String xml = null;
		if(kpiValID!=null && !kpiValID.equals("")){
			xml = DAOFactory.getKpiDAO().loadKPIValueXml(new Integer(kpiValID));
		}
			
		if (kpiInstanceID!=null){
			IKpiInstanceDAO kpiInstDAO=DAOFactory.getKpiInstanceDAO();
			KpiInstance kI = kpiInstDAO.loadKpiInstanceById(new Integer(kpiInstanceID));
			Integer kpiID = kI.getKpi();
			if (kpiID!=null){
				Kpi k = DAOFactory.getKpiDAO().loadKpiById(kpiID);
				String kpiCode = k.getCode();
				String kpiDescription = k.getDescription();
				String kpiInterpretation = k.getInterpretation();
				String kpiName = k.getKpiName();
				String thresholdName = "";
				List thresholdValues = null;
				if(kI.getThresholdId()!=null){
					thresholdValues=DAOFactory.getThresholdValueDAO().loadThresholdValuesByThresholdId(kI.getThresholdId());
					Threshold thres = DAOFactory.getThresholdDAO().loadThresholdById(kI.getThresholdId());
					thresholdName = thres.getName();
				}
				
				if (kpiCode!=null){
					serviceResponse.setAttribute("KPI_CODE", kpiCode);
				}else{
					serviceResponse.setAttribute("KPI_CODE", "");
				}
				if (kpiDescription!=null){
					serviceResponse.setAttribute("KPI_DESCRIPTION", kpiDescription);
				}else{
					serviceResponse.setAttribute("KPI_DESCRIPTION", "");
				}
				if (kpiInterpretation!=null){
					serviceResponse.setAttribute("KPI_INTERPRETATION", kpiInterpretation);
				}else{
					serviceResponse.setAttribute("KPI_INTERPRETATION", "");
				}
				if (kpiName!=null){
					serviceResponse.setAttribute("KPI_NAME", kpiName);
				}else{
					serviceResponse.setAttribute("KPI_NAME", "");
				}
				if (thresholdValues!=null){
					serviceResponse.setAttribute("KPI_THRESHOLDS", thresholdValues);
				}else{
					serviceResponse.setAttribute("KPI_THRESHOLDS", new ArrayList());
				}
				if (thresholdName!=null){
					serviceResponse.setAttribute("THRESHOLD_NAME", thresholdName);
				}else{
					serviceResponse.setAttribute("THRESHOLD_NAME", "");
				}
			}
		}
		if (kpiModelInstanceId!=null){
			Integer id = new Integer(kpiModelInstanceId);
			Date d = new Date();
			IModelInstanceDAO modInstDAO=DAOFactory.getModelInstanceDAO();
			ModelInstanceNode n = modInstDAO.loadModelInstanceById(id, d);
			String name =n.getName();
			String descr = n.getDescr();
			if (name!=null){
				serviceResponse.setAttribute("MODEL_INST_NAME", name);
			}else{
				serviceResponse.setAttribute("MODEL_INST_NAME", "");
			}
			if (descr!=null){
				serviceResponse.setAttribute("MODEL_INST_DESCR", descr);
			}else{
				serviceResponse.setAttribute("MODEL_INST_DESCR", "");
			}			
		}
		
		if (kpiBeginDate!=null){
			serviceResponse.setAttribute("KPI_BEGIN_DATE", kpiBeginDate);
		}else{
			serviceResponse.setAttribute("KPI_BEGIN_DATE", "");
		}
		if (kpiEndDate!=null){
			serviceResponse.setAttribute("KPI_END_DATE", kpiEndDate);
		}else{
			serviceResponse.setAttribute("KPI_END_DATE", "");
		}
		if (kpiTarget!=null){
			serviceResponse.setAttribute("KPI_TARGET", kpiTarget);
		}else{
			serviceResponse.setAttribute("KPI_TARGET", "");
		}
		if (kpiValue!=null){
			serviceResponse.setAttribute("KPI_VALUE", kpiValue);
		}else{
			serviceResponse.setAttribute("KPI_VALUE", "");
		}
		if (kpiWeight!=null){
			serviceResponse.setAttribute("KPI_WEIGHT", kpiWeight);
		}else{
			serviceResponse.setAttribute("KPI_WEIGHT", "");
		}
		if (weightedValue!=null){
			serviceResponse.setAttribute("WEIGHTED_VALUE", weightedValue);
		}else{
			serviceResponse.setAttribute("WEIGHTED_VALUE", "false");
		}
		
		if (kpiValueDescr!=null){
			serviceResponse.setAttribute("KPI_VALUE_DESCR", kpiValueDescr);
		}else{
			serviceResponse.setAttribute("KPI_VALUE_DESCR", "");
		}
		
		if (xml!=null){
			serviceResponse.setAttribute("KPI_XML_DATA", xml);
		}else{
			serviceResponse.setAttribute("KPI_XML_DATA", "");
		}
		
		serviceResponse.setAttribute("publisher_Name", publisher_Name);
		
		logger.debug("OUT");
		
	}
}
