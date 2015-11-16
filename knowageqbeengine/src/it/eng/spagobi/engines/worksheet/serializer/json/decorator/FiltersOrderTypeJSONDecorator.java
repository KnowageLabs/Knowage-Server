/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.worksheet.serializer.json.decorator;


import it.eng.qbe.query.AbstractSelectField;
import it.eng.spagobi.engines.worksheet.serializer.json.FieldsSerializationConstants;
import it.eng.spagobi.engines.worksheet.serializer.json.FilterJSONSerializer;
import it.eng.spagobi.engines.worksheet.serializer.json.WorkSheetSerializationUtils;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.utilities.json.AbstractJSONDecorator;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;


/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class FiltersOrderTypeJSONDecorator extends AbstractJSONDecorator {

	public static transient Logger logger = Logger.getLogger(FiltersOrderTypeJSONDecorator.class);
	
	private IDataSet dataSet = null;
	
	public FiltersOrderTypeJSONDecorator(IDataSet dataSet) {
		this.dataSet = dataSet;
	}
	
	@Override
	protected void doDecoration(JSONObject json) {
		try {
			JSONArray sheets = json.getJSONArray(WorkSheetSerializationUtils.SHEETS);
			for (int i = 0 ; i < sheets.length() ; i++) {
				JSONObject sheet = sheets.getJSONObject(i);
				addFiltersOrderType(sheet);
			}
		} catch (Exception e) {
			throw new RuntimeException("Error while decorating JSON Object", e);
		}
	}

	private void addFiltersOrderType(JSONObject sheetJSON) throws Exception {
		JSONObject filtersJSON = sheetJSON.getJSONObject(WorkSheetSerializationUtils.FILTERS);
		JSONArray arrayJSON = filtersJSON.getJSONArray(WorkSheetSerializationUtils.FILTERS);
		for (int i = 0 ; i < arrayJSON.length() ; i++ ) {
			JSONObject aFilter = arrayJSON.getJSONObject(i);
			String fieldName = aFilter.getString(FieldsSerializationConstants.ID);
			String orderType = getOrderTypeForFilter(fieldName);
			logger.debug("Putting order type [" + orderType + "] for field [" + fieldName + "]");
			aFilter.put(WorkSheetSerializationUtils.ORDER_TYPE, orderType);
		}
	}

	private String getOrderTypeForFilter(String fieldName) {
		//Get the order type of the field values in the field metadata
		int fieldIndex = this.dataSet.getMetadata().getFieldIndex(fieldName);
		IFieldMetaData dataSetFieldMetadata = this.dataSet.getMetadata().getFieldMeta(fieldIndex);
		String orderType = AbstractSelectField.ORDER_ASC; //default ascendant
		String orderTypeMeta = (String)dataSetFieldMetadata.getProperty(IFieldMetaData.ORDERTYPE);
		if (orderTypeMeta != null
				&& (orderTypeMeta.equals(AbstractSelectField.ORDER_ASC) || orderTypeMeta
						.equals(AbstractSelectField.ORDER_DESC))) {
			orderType = orderTypeMeta;
		}
		return orderType;
	}

}
