/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.serializer;

import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.kpi.config.bo.Kpi;
import it.eng.spagobi.kpi.config.bo.KpiDocuments;
import it.eng.spagobi.kpi.model.bo.Resource;
import it.eng.spagobi.profiling.bean.SbiExtUserRoles;
import it.eng.spagobi.tools.udp.bo.UdpValue;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

public class KpiJSONSerializer implements Serializer {

	public static final String KPI_ID = "id";
	private static final String KPI_NAME = "name";
	private static final String KPI_CODE = "code";
	private static final String KPI_DESCRIPTION = "description";
	private static final String KPI_WEIGHT = "weight";
	private static final String IS_ADDITIVE = "isAdditive";
	private static final String KPI_DATASET = "dataset";
	private static final String KPI_THR = "threshold";
	private static final String KPI_DOCS = "documents";
	private static final String KPI_INTERPRETATION = "interpretation";
	private static final String KPI_ALGDESC = "algdesc";
	private static final String KPI_INPUT_ATTR = "inputAttr";
	private static final String KPI_MODEL_REFERENCE = "modelReference";
	private static final String KPI_TARGET_AUDIENCE = "targetAudience";
	private static final String UDP_VALUES = "udpValues";

	private static final String KPI_TYPE_ID = "kpiTypeId";
	private static final String KPI_TYPE_CD = "kpiTypeCd";
	private static final String METRIC_SCALE_TYPE_ID = "metricScaleId";
	private static final String METRIC_SCALE_TYPE_CD = "metricScaleCd";
	private static final String MEASURE_TYPE_ID = "measureTypeId";
	private static final String MEASURE_TYPE_CD = "measureTypeCd";

	public Object serialize(Object o, Locale locale) throws SerializationException {
		JSONObject  result = null;

		if( !(o instanceof Kpi) ) {
			throw new SerializationException("KpiJSONSerializer is unable to serialize object of type: " + o.getClass().getName());
		}

		try {
			Kpi kpi = (Kpi)o;
			result = new JSONObject();

			result.put(KPI_ID, kpi.getKpiId() );
			result.put(KPI_NAME, kpi.getKpiName());
			result.put(KPI_DESCRIPTION, kpi.getDescription() );
			result.put(KPI_CODE, kpi.getCode() );
			result.put(KPI_WEIGHT, kpi.getStandardWeight() );
			boolean isAdd = false;
			if(kpi.getIsAdditive()!=null){
				isAdd = kpi.getIsAdditive().booleanValue();
			}
			result.put(IS_ADDITIVE, isAdd);
			result.put(KPI_DATASET, kpi.getDsLabel());
			if(kpi.getThreshold()!=null){
				result.put(KPI_THR, kpi.getThreshold().getCode() );
			}
			//roles
			List userDocs = kpi.getSbiKpiDocuments();
			Iterator itDocs = userDocs.iterator();
			JSONArray documentsJSON = new JSONArray();
			//documentsJSON.put("documents");

			while(itDocs.hasNext()){
				//JSONObject jsonDoc = new JSONObject();
				KpiDocuments kpiDoc = (KpiDocuments)itDocs.next();
				//jsonDoc.put("label", kpiDoc.getBiObjLabel());
				documentsJSON.put(kpiDoc.getBiObjLabel());
			}	
			result.put(KPI_DOCS, documentsJSON);

			// put udpValues assocated to KPI
			List udpValues = kpi.getUdpValues();
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
				result.put(UDP_VALUES, udpValuesJSON);
			}


			//result.put(KPI_DOCS, kpi.getDocumentLabel() );
			result.put(KPI_INTERPRETATION, kpi.getInterpretation() );
			result.put(KPI_ALGDESC, kpi.getMetric() );
			result.put(KPI_INPUT_ATTR, kpi.getInputAttribute() );
			result.put(KPI_MODEL_REFERENCE, kpi.getModelReference() );
			result.put(KPI_TARGET_AUDIENCE, kpi.getTargetAudience() );

			result.put(KPI_TYPE_ID, kpi.getKpiTypeId() );
			result.put(KPI_TYPE_CD, kpi.getKpiTypeCd());		
			result.put(METRIC_SCALE_TYPE_ID, kpi.getMetricScaleId() );
			result.put(METRIC_SCALE_TYPE_CD, kpi.getMetricScaleCd());	
			result.put(MEASURE_TYPE_ID, kpi.getMeasureTypeId() );
			result.put(MEASURE_TYPE_CD, kpi.getMeasureTypeCd());	

		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {

		}
		return result;
	}
}
