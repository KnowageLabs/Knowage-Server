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
package it.eng.spagobi.engines.kpi.utils;

import it.eng.spagobi.analiticalmodel.document.handlers.ExecutionInstance;
import it.eng.spagobi.engines.kpi.bo.KpiLine;
import it.eng.spagobi.kpi.config.bo.Kpi;
import it.eng.spagobi.kpi.config.bo.KpiValue;
import it.eng.spagobi.kpi.threshold.bo.ThresholdValue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class KpiGUIUtil {
	static transient Logger logger = Logger.getLogger(KpiGUIUtil.class);
	private ExecutionInstance kpiInstance;
	private Locale kpiInstanceLocale;
	private List parameters ;
	
	
	public void setExecutionInstance(ExecutionInstance instance, Locale locale){
		kpiInstance = instance;
		kpiInstanceLocale = locale;
		
	}

	public JSONObject recursiveGetJsonObject(KpiLine kpiLine) {
		Monitor monitor = MonitorFactory.start("kpi.engines.KpiGUIUtil.recursiveGetJsonObject");
		JSONObject jsonToReturn = new JSONObject();
		try {
			if(!kpiLine.isVisible()){
				jsonToReturn.put("hidden",true);
			}else{
				jsonToReturn.put("hidden",false);
			}
			
			
			String name = kpiLine.getModelNodeName();
			String label = kpiLine.getModelInstanceNodeId()+"";
			jsonToReturn.put("statusLabel", label);
			if(name.length() >= 50){
				name = name.substring(0,50) + "...";
			}
			jsonToReturn.put("name", name);


			jsonToReturn.put("qtip", kpiLine.getModelNodeName());
			
			List<KpiLine> children = (List<KpiLine>) kpiLine.getChildren();

			if (children != null) {
				
				JSONArray jsonArrayChildren = new JSONArray();
				for (int i = 0; i < children.size(); i++) {

					KpiLine kpiChildLine = children.get(i);
					JSONObject child  = recursiveGetJsonObject(kpiChildLine);
					jsonArrayChildren.put(child);
				}
				jsonToReturn.put("children", jsonArrayChildren);
			}
			KpiValue kpivalue= kpiLine.getValue();
			if (kpivalue != null) {
				jsonToReturn.put("actual", kpiLine.getValue().getValue());
				jsonToReturn.put("target", kpiLine.getValue().getTarget());
				
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
				Date bdate = kpiLine.getValue().getBeginDate();
				Date edate = kpiLine.getValue().getEndDate();
				if(bdate != null && edate != null){
					jsonToReturn.putOpt("beginDt", sdf.format(bdate));
					jsonToReturn.putOpt("endDt", sdf.format(edate));
				}

				if(children != null && !children.isEmpty()){
					jsonToReturn.put("iconCls", "folder");
					jsonToReturn.put("cls", "node-folder");
				}else{
					jsonToReturn.put("iconCls","has-kpi");
				}
			}else{				
				jsonToReturn.put("actual", "");
				jsonToReturn.put("target", "");
				if(children != null && !children.isEmpty()){
					jsonToReturn.put("iconCls", "folder");
					jsonToReturn.put("cls", "node-folder");
				}else{
					jsonToReturn.put("iconCls","has-kpi");
				}

			}
			String color = detectColor(kpivalue);
			jsonToReturn.put("status", color);
			jsonToReturn.put("expanded", true);
			
			setKpiInfos(kpiLine, jsonToReturn);
			setDetailInfos(kpiLine, jsonToReturn);
			
			//documents
			List documents = kpiLine.getDocuments();
			if(documents != null && !documents.isEmpty()){
				String docLabel =(String)documents.get(0);
				//return only one document
				jsonToReturn.putOpt("documentLabel", docLabel);
				//gets url for execution

				ExecutionInstance docExecInst = ExecutionInstance.getExecutionInstanceByLabel(kpiInstance, docLabel, kpiInstanceLocale);
				String executionUrl = docExecInst.getExecutionUrl(kpiInstanceLocale);
				jsonToReturn.putOpt("documentExecUrl", executionUrl);
			}



		} catch (JSONException e) {
			logger.error("Error setting children");
		} catch (Exception e) {
			logger.error("Error getting execution instances");
		}finally{
			monitor.stop();
		}

		return jsonToReturn;

	}
	private void setKpiInfos(KpiLine kpiLine, JSONObject row) throws JSONException{
		
		Monitor monitor = MonitorFactory.start("kpi.engines.KpiGUIUtil.setKpiInfos");

		row.putOpt("trend", kpiLine.getTrend());

		Kpi kpi = kpiLine.getKpi();
		row.putOpt("kpiDescr", kpi.getDescription());
		row.putOpt("kpiName", kpi.getKpiName());
		row.putOpt("kpiCode", kpi.getCode());
		row.putOpt("kpiDsLbl", kpi.getDsLabel());
		row.putOpt("kpiTypeCd", kpi.getKpiTypeCd());
		row.putOpt("measureTypeCd", kpi.getMeasureTypeCd());
		row.putOpt("scaleName", kpi.getMetricScaleCd());
		row.putOpt("targetAudience", kpi.getTargetAudience());
		
		row.putOpt("kpiInstId", kpiLine.getKpiInstId());
		monitor.stop();

	}
	private void setDetailInfos(KpiLine kpiLine, JSONObject row){
		
		Monitor monitor = MonitorFactory.start("kpi.engines.KpiGUIUtil.setDetailInfos");
		
		JSONArray thresholds = new JSONArray();
		if(kpiLine.getValue() != null){
			Double weight = kpiLine.getValue().getWeight();
			
			List thrs = kpiLine.getValue().getThresholdValues();
			if(thrs != null ){
				
				for(int i=0; i< thrs.size(); i++){
					JSONObject threshold = new JSONObject();
					ThresholdValue tv = (ThresholdValue)thrs.get(i);
					String color = tv.getColourString();
					String label = tv.getLabel();
					String type = tv.getThresholdType();
					Double max = tv.getMaxValue();
					Double min = tv.getMinValue();
					Double achieve = tv.getValue();//used to define the value to achieve
					try {
						threshold.putOpt("color", color);
						threshold.putOpt("label", label);
						threshold.putOpt("type", type);
						threshold.putOpt("max", max);
						threshold.putOpt("min", min);
						threshold.putOpt("achieve", achieve);
						
						thresholds.put(threshold);
						
					} catch (JSONException e) {
						logger.error("Error setting threshold");
					}
				}
				try {
					row.put("thresholds", thresholds);
					row.putOpt("weight", weight);
				} catch (JSONException e) {
					logger.error("Error setting thresholds");
				}
			}
		}
		monitor.stop();
	}
	private String detectColor(KpiValue value){
		String ret = "";
		if(value == null || value.getValue() == null){
			return ret;
		}

		ThresholdValue thrVal = value.getThresholdOfValue();
		if(thrVal != null ){
			if(thrVal.getColourString() != null){
				return thrVal.getColourString();
			}
			
		}else{
			//calculate it
			String val = value.getValue();
			ret = getStatus(value.getThresholdValues(), Double.parseDouble(val));
			
		}

		return ret;
		
	}
	public String getStatus(List thresholdValues, double val) {
		logger.debug("IN");
		String status = "";
		if(thresholdValues!=null && !thresholdValues.isEmpty()){
			Iterator it = thresholdValues.iterator();

			while(it.hasNext()){
				ThresholdValue t = (ThresholdValue)it.next();
				String type = t.getThresholdType();
				Double min = t.getMinValue();
				Double max = t.getMaxValue();
				if(val <= max && val >= min){
					status = t.getColourString();
				}		
				logger.debug("New interval added to the Vector");
			}
		}
		logger.debug("OUT");
		return status;
		
	}
	

}
