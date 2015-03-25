/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.serializer;

import it.eng.spagobi.kpi.threshold.bo.Threshold;
import it.eng.spagobi.kpi.threshold.bo.ThresholdValue;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

public class ThresholdJSONSerializer implements Serializer {

	public static final String THRESHOLD_ID = "id";
	private static final String THRESHOLD_NAME = "name";
	private static final String THRESHOLD_DESCRIPTION = "description";
	private static final String THRESHOLD_CODE = "code";
	private static final String THRESHOLD_TYPE_ID = "typeId";
	private static final String THRESHOLD_TYPE_CD = "typeCd";
	private static final String THRESHOLD_VALUES = "thrValues";
	
	private static final String THR_VAL_ID = "idThrVal";
	private static final String THR_VAL_LABEL = "label";
	private static final String THR_VAL_POSITION = "position";
	private static final String THR_VAL_MIN = "min";
	private static final String THR_VAL_MIN_INCLUDED = "minIncluded";
	private static final String THR_VAL_MAX = "max";
	private static final String THR_VAL_MAX_INCLUDED = "maxIncluded";
	private static final String THR_VAL_VALUE = "val";
	private static final String THR_VAL_COLOR = "color";
	private static final String THR_VAL_SEVERITY_CD = "severityCd";
	private static final String THR_VAL_SEVERITY_ID = "severityId";
	private static final String OLD_TO_DELETE = "oldToDelete";
	
	public Object serialize(Object o, Locale locale) throws SerializationException {
		JSONObject  result = null;
		
		if( !(o instanceof Threshold) ) {
			throw new SerializationException("ThresholdJSONSerializer is unable to serialize object of type: " + o.getClass().getName());
		}
		
		try {
			Threshold thr = (Threshold)o;
			result = new JSONObject();
			
			result.put(THRESHOLD_ID, thr.getId() );
			result.put(THRESHOLD_NAME, thr.getName() );
			result.put(THRESHOLD_DESCRIPTION, thr.getDescription() );
			result.put(THRESHOLD_CODE, thr.getCode() );
			result.put(THRESHOLD_TYPE_ID, thr.getThresholdTypeId() );
			result.put(THRESHOLD_TYPE_CD, thr.getThresholdTypeCode());		
			result.put(OLD_TO_DELETE, new Boolean(false));	
			
			List thrValues = thr.getThresholdValues();
			JSONArray thValues = new JSONArray();
			if(thrValues!=null){		
				if(!thrValues.isEmpty()){
					if(thrValues.size()==1 && !thr.getThresholdTypeCode().equalsIgnoreCase("RANGE")){
						ThresholdValue thrVal = (ThresholdValue) thrValues.get(0);
						if(thrVal != null){
						 result.put(THR_VAL_ID,  thrVal.getId());
						 result.put(THR_VAL_LABEL,  thrVal.getLabel());
						 result.put(THR_VAL_POSITION,  thrVal.getPosition());
						 result.put(THR_VAL_MIN,  thrVal.getMinValue());
						 result.put(THR_VAL_MIN_INCLUDED,  thrVal.getMinClosed());
						 result.put(THR_VAL_MAX,  thrVal.getMaxValue());
						 result.put(THR_VAL_MAX_INCLUDED,  thrVal.getMaxClosed());
						 result.put(THR_VAL_VALUE,  thrVal.getValue());
						 result.put(THR_VAL_COLOR,  thrVal.getColourString());
						 result.put(THR_VAL_SEVERITY_ID,  thrVal.getSeverityId());
						 result.put(THR_VAL_SEVERITY_CD,  thrVal.getSeverityCd());
						}
					}else{					
						for (Iterator iterator = thrValues.iterator(); iterator.hasNext();) {
							
							 ThresholdValue thrVal = (ThresholdValue) iterator.next();
							 JSONObject temp = new JSONObject();
							 temp.put(THR_VAL_ID,  thrVal.getId());
							 temp.put(THR_VAL_LABEL,  thrVal.getLabel());
							 temp.put(THR_VAL_POSITION,  thrVal.getPosition());
							 temp.put(THR_VAL_MIN,  thrVal.getMinValue());
							 temp.put(THR_VAL_MIN_INCLUDED,  thrVal.getMinClosed());
							 temp.put(THR_VAL_MAX,  thrVal.getMaxValue());
							 temp.put(THR_VAL_MAX_INCLUDED,  thrVal.getMaxClosed());
							 temp.put(THR_VAL_VALUE,  thrVal.getValue());
							 temp.put(THR_VAL_COLOR,  thrVal.getColourString());
							 temp.put(THR_VAL_SEVERITY_ID,  thrVal.getSeverityId());
							 temp.put(THR_VAL_SEVERITY_CD,  thrVal.getSeverityCd());
							 thValues.put(temp);
	
						}
					}
				}
			}
			
			result.put(THRESHOLD_VALUES, thValues);
			
			
		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {
			
		}
		
		return result;
	}

}
