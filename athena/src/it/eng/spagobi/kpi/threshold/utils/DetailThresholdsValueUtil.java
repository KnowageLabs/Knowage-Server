/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.threshold.utils;

import java.awt.Color;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.kpi.threshold.bo.Threshold;
import it.eng.spagobi.kpi.threshold.bo.ThresholdValue;

public class DetailThresholdsValueUtil {

	public static void selectThresholdValue(Integer id,
			SourceBean serviceResponse) throws SourceBeanException,
			EMFUserError {
		ThresholdValue toReturn = DAOFactory.getThresholdValueDAO()
		.loadThresholdValueById(id);
		serviceResponse.setAttribute("THRESHOLDVALUE", toReturn);
	}

	public static void updateThresholdValueFromRequest(
			SourceBean serviceRequest, Integer id) throws EMFUserError {
		ThresholdValue threshold = getThresholdValueFromRequest(serviceRequest);
		threshold.setId(id);
		DAOFactory.getThresholdValueDAO().modifyThresholdValue(threshold);

	}

	private static ThresholdValue getThresholdValueFromRequest(
			SourceBean serviceRequest) {
		String sId = (String) serviceRequest.getAttribute("id");
		String sThresholdId = (String) serviceRequest
		.getAttribute("threshold_id");
		String sPosition = (String) serviceRequest.getAttribute("position");
		String label = (String) serviceRequest.getAttribute("label");
		String sMinValue = (String) serviceRequest.getAttribute("min_Value");
		String sMaxValue = (String) serviceRequest.getAttribute("max_Value");
		String colour = (String) serviceRequest.getAttribute("colour");

		String sValue = (String)serviceRequest.getAttribute("value");
		String sMinClosed = (String)serviceRequest.getAttribute("min_closed");
		String sMaxClosed = (String)serviceRequest.getAttribute("max_closed");
		
		Boolean minClosed = false;
		if (sMinClosed != null){
			minClosed = true;
		}

		Boolean maxClosed = false;
		if (sMaxClosed != null){
			maxClosed = true;
		}

		
		String sSeverityId = (String) serviceRequest
		.getAttribute("severity_id");
		String sThresholdType = null;

		ThresholdValue toReturn = new ThresholdValue();

		Integer id = null;
		if (sId != null && !(sId.trim().equals(""))) {
			id = Integer.parseInt(sId);
		}

		Integer thresholdId = null;
		if (sThresholdId != null && !(sThresholdId.trim().equals(""))) {
			thresholdId = Integer.parseInt(sThresholdId);
			try {
				Threshold threshold = DAOFactory.getThresholdDAO().loadThresholdById(thresholdId);
				Domain threshodlType = DAOFactory.getDomainDAO().loadDomainById(threshold.getThresholdTypeId());
				sThresholdType = threshodlType.getValueCd();
			} catch (EMFUserError e) {
			}
			
		}

		Integer position = null;
		if (sPosition != null && !(sPosition.trim().equals(""))) {
			try{
				position = Integer.parseInt(sPosition);
			} catch(NumberFormatException nfe){

			}
		}

		Double minValue = null;
		if (sMinValue != null && !(sMinValue.trim().equals(""))) {
			try{
				minValue = new Double(sMinValue);
			} catch(NumberFormatException nfe){

			} 
		}

		Double maxValue = null;
		if (sMaxValue != null && !(sMaxValue.trim().equals(""))) {
			try{
				maxValue = new Double(sMaxValue);
			} catch(NumberFormatException nfe){

			}
		}
		
		Double value = null;
		if (sValue != null && !(sValue.trim().equals(""))) {
			try{
				value = new Double(sValue);
			} catch(NumberFormatException nfe){

			}
		}
		
		

		Integer severityId = null;
		if (sSeverityId != null && !(sSeverityId.trim().equals(""))) {
			severityId = Integer.parseInt(sSeverityId);
		}

		toReturn.setId(id);
		toReturn.setThresholdId(thresholdId);
		toReturn.setPosition(position);
		toReturn.setLabel(label);
		toReturn.setMinValue(minValue);
		toReturn.setMaxValue(maxValue);
		
		toReturn.setMaxClosed(maxClosed);
		toReturn.setMinClosed(minClosed);
		toReturn.setValue(value);

		Color col=null;
		try{
			col=Color.decode(colour);
		}
		catch (Exception e) {
			col=Color.red;
		}
		toReturn.setColor(col);
		toReturn.setColourString(colour);
		toReturn.setSeverityId(severityId);
		toReturn.setThresholdType(sThresholdType);

		return toReturn;

	}

	public static void newThresholdValue(SourceBean serviceRequest,
			SourceBean serviceResponse) throws EMFUserError,
			SourceBeanException {
		ThresholdValue toCreate = getThresholdValueFromRequest(serviceRequest);

		Integer thresholdValueId = DAOFactory.getThresholdValueDAO()
		.insertThresholdValue(toCreate);

		serviceResponse.setAttribute("ID", thresholdValueId);
		serviceResponse.setAttribute("MESSAGE", SpagoBIConstants.DETAIL_SELECT);
		selectThresholdValue(thresholdValueId, serviceResponse);

	}

	public static void restoreThresholdValue(Integer id, SourceBean serviceRequest,
			SourceBean serviceResponse) throws Exception {
		ThresholdValue toReturn = getThresholdValueFromRequest(serviceRequest);
		if (id != null) {
			toReturn.setId(id);
		}
		serviceResponse.setAttribute("THRESHOLDVALUE", toReturn);
	}

}
