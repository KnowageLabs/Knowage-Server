/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.config.bo;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spagobi.kpi.model.bo.Resource;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnit;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnitGrantNode;
import it.eng.spagobi.kpi.threshold.bo.ThresholdValue;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

public class KpiValue implements Cloneable{

	private static transient Logger logger=Logger.getLogger(KpiValue.class);
	
	Integer kpiInstanceId = null;
	Integer kpiValueId = null;
	String value = null;	
	String valueDescr = null;
	String valueXml = null;
	List thresholdValues = null;
	Double weight = null;
	Double target = null;
	Date beginDate = null;
	Date endDate = null;//null beginDate + num_validity_days
	String scaleCode = null;
	String scaleName = null;
	Resource r = null;//Resource (project/process) to which refers the value
	String chartType = null;
	OrganizationalUnitGrantNode grantNodeOU = null;

	/**
	 * This function returns the value of the attribute required, if existent in the xml field
	 * 
	 * @param String attribute for which the value is requested
	 * @return The value of the attribute
	 */
	public String getValueFromStandardXmlValue(String attribute){
		String valToReturn = "";
		if(valueXml!=null){
			try {
				SourceBean xmlValueSB = SourceBean.fromXMLString(valueXml);
				valToReturn = (String)xmlValueSB.getAttribute(attribute);
			} catch (SourceBeanException e) {
				logger.error("Source Bean Exception",e);
				e.printStackTrace();
			}
		}
		return valToReturn;
	}
	
	/**
	 * This function return the ThresholdValue in which the kpiValue falls
	 * 
	 * @return The Color of the interval in which the value falls
	 */
	public ThresholdValue getThresholdOfValue() {
		logger.debug("IN");
		ThresholdValue toReturn = null;
		Double val = new Double(value);
		if (thresholdValues != null && !thresholdValues.isEmpty()) {
			Iterator it = thresholdValues.iterator();

			while (it.hasNext()) {
				ThresholdValue t = (ThresholdValue) it.next();
				String type = t.getThresholdType();
				Double min = t.getMinValue();
				Double max = t.getMaxValue();
				Boolean min_closed = t.getMinClosed()!=null?t.getMinClosed():false;
				Boolean max_closed = t.getMaxClosed()!=null?t.getMaxClosed():false;

				if (type.equals("RANGE")) {
					logger.debug("Threshold type RANGE");
					if(min_closed && max_closed){
						if (min != null && min.doubleValue()<= val.doubleValue()&& max!=null && val.doubleValue() <= max.doubleValue()) {
							toReturn = t;
							break;
						}else if(min==null && max!=null && val.doubleValue() <= max.doubleValue()){
							toReturn = t;
							break;
						}else if(max == null && min != null && min.doubleValue()<= val.doubleValue()){
							toReturn = t;
							break;
						}
					}else if(min_closed && !max_closed){
						if (min != null && min.doubleValue()<= val.doubleValue()&& max!=null && val.doubleValue() < max.doubleValue()) {
							toReturn = t;
							break;
						}else if(min==null && max!=null && val.doubleValue() < max.doubleValue()){
							toReturn = t;
							break;
						}else if(max == null && min != null && min.doubleValue()<= val.doubleValue()){
							toReturn = t;
							break;
						}					
					}else if(!min_closed && max_closed){
						if (min != null && min.doubleValue()< val.doubleValue()&& max!=null && val.doubleValue() <= max.doubleValue()) {
							toReturn = t;
							break;
						}else if(min==null && max!=null && val.doubleValue() <= max.doubleValue()){
							toReturn = t;
							break;
						}else if(max == null && min != null && min.doubleValue()< val.doubleValue()){
							toReturn = t;
							break;
						}						
					}else{
						if (min != null && min.doubleValue()< val.doubleValue()&& max!=null && val.doubleValue() < max.doubleValue()) {
							toReturn = t;
							break;
						}else if(min==null && max!=null && val.doubleValue() < max.doubleValue()){
							toReturn = t;
							break;
						}else if(max == null && min != null && min.doubleValue()< val.doubleValue()){
							toReturn = t;
							break;
						}	
					}
		
				} else if (type.equals("MINIMUM")) {
					logger.debug("Threshold type MINIMUM");
					if(min_closed){
						if (val.doubleValue() <= min.doubleValue()) {
							toReturn = t;
							break;
						}else {
							t.setColor(Color.WHITE) ;
							toReturn = t;
							break;
						}
					}else{
						if (val.doubleValue() < min.doubleValue()) {
							toReturn = t;
							break;
						}else {
							t.setColor(Color.WHITE) ;
							toReturn = t;
							break;
						}
					}
				} else if (type.equals("MAXIMUM")) {
					logger.debug("Threshold type MAXIMUM");
					if(max_closed){
						if (val.doubleValue() >= max.doubleValue()) {
							toReturn = t;
						} else {
							t.setColor(Color.WHITE);
							toReturn = t;
							break;
						}
					}else{
						if (val.doubleValue() > max.doubleValue()) {
							toReturn = t;
						} else {
							t.setColor(Color.WHITE);
							toReturn = t;
							break;
						}
					}				
				}
				logger.debug("New interval added to the Vector");
			}
		}
		logger.debug("OUT");
		return toReturn;
	}
	
	
	public String getChartType() {
		return chartType;
	}

	public void setChartType(String chartType) {
		this.chartType = chartType;
	}

	public KpiValue() {
		super();
		this.thresholdValues = new ArrayList();
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getScaleCode() {
		return scaleCode;
	}

	public void setScaleCode(String scaleCode) {
		this.scaleCode = scaleCode;
	}

	public String getScaleName() {
		return scaleName;
	}

	public void setScaleName(String scaleName) {
		this.scaleName = scaleName;
	}

	public List getThresholdValues() {
		return thresholdValues;
	}

	public void setThresholdValues(List thresholds) {
		this.thresholdValues = thresholds;
	}

	public Double getWeight() {
		return weight;
	}

	public void setWeight(Double weight) {
		this.weight = weight;
	}

	public Date getBeginDate() {
		return beginDate;
	}

	public void setBeginDate(Date beginDate) {
		this.beginDate = beginDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Resource getR() {
		return r;
	}

	public void setR(Resource r) {
		this.r = r;
	}

	public Integer getKpiInstanceId() {
		return kpiInstanceId;
	}

	public void setKpiInstanceId(Integer kpiInstanceId) {
		this.kpiInstanceId = kpiInstanceId;
	}

	public Double getTarget() {
		return target;
	}

	public void setTarget(Double target) {
		this.target = target;
	}

	public String getValueDescr() {
		return valueDescr;
	}

	public void setValueDescr(String valueDescr) {
		this.valueDescr = valueDescr;
	}
	
	public KpiValue clone(){
		 KpiValue toReturn = new KpiValue();
		 toReturn.setBeginDate(beginDate);
		 toReturn.setChartType(chartType);
		 toReturn.setEndDate(endDate);
		 toReturn.setKpiInstanceId(kpiInstanceId);
		 toReturn.setR(r);
		 toReturn.setScaleCode(scaleCode);
		 toReturn.setScaleName(scaleName);
		 toReturn.setTarget(target);
		 toReturn.setThresholdValues(thresholdValues);
		 toReturn.setValue(value);
		 toReturn.setKpiValueId(kpiValueId);
		 toReturn.setValueDescr(valueDescr);
		 toReturn.setValueXml(valueXml);
		 toReturn.setWeight(weight);
		 toReturn.setGrantNodeOU(grantNodeOU);
		 return toReturn;
	}



	public OrganizationalUnitGrantNode getGrantNodeOU() {
		return grantNodeOU;
	}

	public void setGrantNodeOU(OrganizationalUnitGrantNode grantNodeOU) {
		this.grantNodeOU = grantNodeOU;
	}

	public Integer getKpiValueId() {
		return kpiValueId;
	}

	public void setKpiValueId(Integer kpiValueId) {
		this.kpiValueId = kpiValueId;
	}

	public String getValueXml() {
		return valueXml;
	}

	public void setValueXml(String valueXml) {
		this.valueXml = valueXml;
	}

}
