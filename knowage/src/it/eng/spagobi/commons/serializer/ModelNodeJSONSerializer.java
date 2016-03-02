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
package it.eng.spagobi.commons.serializer;

import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.kpi.config.bo.Kpi;
import it.eng.spagobi.kpi.model.bo.Model;
import it.eng.spagobi.kpi.model.bo.Resource;
import it.eng.spagobi.tools.udp.bo.UdpValue;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

public class ModelNodeJSONSerializer implements Serializer {

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
	private static final String MODEL_KPI_CODE = "kpiCode";
	private static final String MODEL_IS_LEAF = "leaf";
	private static final String MODEL_TEXT = "text";
	private static final String UDP_VALUES = "udpValues";
	
	private static final String MODEL_ERROR = "error";

	
	public Object serialize(Object o, Locale locale) throws SerializationException {
		JSONObject  result = null;
		
		if( !(o instanceof Model) ) {
			throw new SerializationException("ModelNodeJSONSerializer is unable to serialize object of type: " + o.getClass().getName());
		}
		
		try {
			Model model = (Model)o;
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
					result.put(MODEL_KPI_CODE, kpi.getCode());
				}else{
					result.put(MODEL_KPI, "");
				}
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
