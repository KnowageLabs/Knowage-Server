/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.serializer;

import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.kpi.config.bo.Kpi;
import it.eng.spagobi.kpi.model.bo.Model;
import it.eng.spagobi.kpi.model.bo.ModelExtended;
import it.eng.spagobi.kpi.model.bo.Resource;
import it.eng.spagobi.tools.udp.bo.UdpValue;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

public class ModelExtendedJSONSerializer implements Serializer {

	public static final String MODEL_ID = "modelId";
	public static final String MODEL_GUIID = "id";
	public static final String MODEL_PARENT_ID = "parentId";
	private static final String MODEL_CODE = "code";
	private static final String MODEL_DESCRIPTION = "description";
	private static final String MODEL_LABEL = "label";
	private static final String MODEL_NAME = "name";
	private static final String MODEL_TYPE = "type";
	private static final String MODEL_TYPE_ID = "typeId";
	private static final String MODEL_TYPE_DESCR = "typeDescr";
	private static final String MODEL_KPI = "kpi";
	private static final String MODEL_KPI_ID = "kpiId";
	private static final String MODEL_IS_LEAF = "leaf";
	private static final String MODEL_TEXT = "text";
	private static final String UDP_VALUES = "udpValues";
	//extended fields
	private static final String KPI_NAME = "kpiName";
	private static final String KPI_LABEL = "modelUuid";
	private static final String KPI_THRESHOLD = "kpiInstThrName";
	private static final String KPI_WEIGHT = "kpiInstWeight";
	private static final String KPI_TARGET = "kpiInstTarget";
	
	private static final String MODEL_ERROR = "error";
	
	public Object serialize(Object o, Locale locale) throws SerializationException {
		JSONObject  result = null;
		
		if( !(o instanceof ModelExtended) ) {
			throw new SerializationException("ModelExtendedJSONSerializer is unable to serialize object of type: " + o.getClass().getName());
		}
		
		try {
			ModelExtended modelExtended = (ModelExtended)o;
			Model model = modelExtended.getModel();
			result = new JSONObject();
			
			result.put(MODEL_ID, model.getId() );
			result.put(MODEL_GUIID, model.getGuiId() );
			result.put(MODEL_PARENT_ID, model.getParentId() );
			result.put(MODEL_CODE, model.getCode() );
			result.put(MODEL_NAME, model.getName() );
			result.put(MODEL_LABEL, model.getLabel() );
			result.put(MODEL_DESCRIPTION, model.getDescription() );		
			
			//find kpi name
			if(model.getKpiId() != null){
				result.put(MODEL_KPI_ID, model.getKpiId());
				Kpi kpi = DAOFactory.getKpiDAO().loadKpiById(model.getKpiId());
				if(kpi != null){
					result.put(MODEL_KPI, kpi.getKpiName());
				}else{
					result.put(MODEL_KPI, "");
				}
				result.put(KPI_NAME, kpi.getKpiName());
				result.put(KPI_LABEL, kpi.getModelReference());
				if(kpi.getThreshold() != null){
					result.put(KPI_THRESHOLD, kpi.getThreshold().getName());
				}
				result.put(KPI_WEIGHT, kpi.getStandardWeight());
				result.put(KPI_TARGET, kpi.getTargetAudience());

			
			}
			result.put(MODEL_TYPE, model.getTypeCd() );
			result.put(MODEL_TYPE_ID, model.getTypeId() );
			result.put(MODEL_TYPE_DESCR, model.getTypeDescription() );
			if(model.getChildrenNodes() != null && !model.getChildrenNodes().isEmpty()){
				result.put(MODEL_IS_LEAF, false );
			}else{
				result.put(MODEL_IS_LEAF, true );
			}
			result.put(MODEL_TEXT, model.getCode()+" - "+ model.getName() );
			result.put(MODEL_ERROR, false);
			// put udpValues assocated to ModelInstance Node
			List udpValues = model.getUdpValues();
			JSONArray udpValuesJSON = new JSONArray();

			if(udpValues != null){
				Iterator itUdpValues = udpValues.iterator();
				while(itUdpValues.hasNext()){
					UdpValue udpValue = (UdpValue)itUdpValues.next();
					if(udpValue != null){
						JSONObject jsonVal = new JSONObject();
						//jsonVal.put("family", udpValue.getFamily());
						jsonVal.put("label", udpValue.getLabel());
						jsonVal.put("value", udpValue.getValue());
						jsonVal.put("family", udpValue.getFamily());
						jsonVal.put("type", udpValue.getTypeLabel());
						udpValuesJSON.put(jsonVal);
					}				
				}	
			}
			result.put(UDP_VALUES, udpValuesJSON);
		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {
			
		}
		
		return result;
	}

}
