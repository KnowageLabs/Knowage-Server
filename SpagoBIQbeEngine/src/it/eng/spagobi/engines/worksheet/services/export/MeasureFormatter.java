/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.worksheet.services.export;

import it.eng.qbe.serializer.SerializationException;
import it.eng.spagobi.engines.qbe.crosstable.CrossTab;
import it.eng.spagobi.engines.qbe.crosstable.CrossTab.CellType;
import it.eng.spagobi.engines.qbe.crosstable.CrossTab.MeasureInfo;
import it.eng.spagobi.engines.qbe.crosstable.serializer.json.CrosstabSerializationConstants;
import it.eng.spagobi.engines.worksheet.bo.MeasureScaleFactorOption;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Sheet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class MeasureFormatter {

		List<MeasureInfo> measuresInfo;
		//String[][] measureMetadata;
		boolean measureOnRow;
		DecimalFormat numberFormat;
		String pattern;
		
//		public MeasureFormatter(JSONObject crosstabDefinitionJSON, DecimalFormat numberFormat, String pattern) throws SerializationException, JSONException{
//			JSONArray measuresJSON = crosstabDefinitionJSON.optJSONArray(CrosstabSerializationConstants.MEASURES);
//			JSONObject config =  crosstabDefinitionJSON.optJSONObject(CrosstabSerializationConstants.CONFIG);
//			//Assert.assertTrue(rows != null && rows.length() > 0, "No measures specified!");
//			this.pattern = pattern;
//			this.numberFormat=numberFormat;
//			if (measuresJSON != null) {
//				measureMetadata = new String[measuresJSON.length()][3];
//				for (int i = 0; i < measuresJSON.length(); i++) {
//					JSONObject obj = (JSONObject) measuresJSON.get(i);
//					measureMetadata[i][0] = obj.getString("name");
//					measureMetadata[i][1] = obj.getString("format");
//					measureMetadata[i][2] = obj.getString("type");
//				}
//			}
//			measureOnRow = false;
//			if(config!=null){
//				measureOnRow = config.optString(CrosstabSerializationConstants.MEASURESON).equals(CrosstabSerializationConstants.ROWS);
//			}
//		}
//		
		
		public MeasureFormatter(JSONObject crosstabDefinitionJSON, DecimalFormat numberFormat, String pattern) throws SerializationException, JSONException{
			
			JSONArray measuresJSON = crosstabDefinitionJSON.optJSONArray(CrosstabSerializationConstants.MEASURES);
			JSONObject config =  crosstabDefinitionJSON.optJSONObject(CrosstabSerializationConstants.CONFIG);
			this.pattern = pattern;
			this.numberFormat=numberFormat;
			if (measuresJSON != null) {
				measuresInfo = new ArrayList<MeasureInfo>();
				for (int i = 0; i < measuresJSON.length(); i++) {
					JSONObject obj = (JSONObject) measuresJSON.get(i);
					MeasureInfo mi = new MeasureInfo(obj.getString("name"), "", obj.getString("type"), obj.getString("format"));
					measuresInfo.add(mi);
				}
			}
			measureOnRow = false;
			if(config!=null){
				measureOnRow = config.optString(CrosstabSerializationConstants.MEASURESON).equals(CrosstabSerializationConstants.ROWS);
			}
		}
		
		public MeasureFormatter(CrossTab crosstab) {
			this.measuresInfo = crosstab.getMeasures();
			this.measureOnRow = crosstab.isMeasureOnRow();
		}
		
		public String getFormat(Float f, int positionI, int positionJ) {
			int pos;
			String formatted="";
			if(measureOnRow){
				pos = positionI%measuresInfo.size();
			}else{
				pos = positionJ%measuresInfo.size();
			}
			try {
				String decimalPrecision =  (new JSONObject(measuresInfo.get(pos).getFormat())).optString(IFieldMetaData.DECIMALPRECISION);
				if(decimalPrecision!=null){
					DecimalFormat numberFormat = new DecimalFormat(pattern);
					numberFormat.setMinimumFractionDigits(new Integer(decimalPrecision));
					numberFormat.setMaximumFractionDigits(new Integer(decimalPrecision));
					formatted = numberFormat.format(f);
				}
			} catch (Exception e) {
				formatted = numberFormat.format(f);
			}
			return formatted;
		}
		
		public int getFormatXLS(int positionI, int positionJ) {
			int pos;

			if(measureOnRow){
				pos = positionI%measuresInfo.size();
			}else{
				pos = positionJ%measuresInfo.size();
			}
			try {
				String decimalPrecision =  (new JSONObject(measuresInfo.get(pos).getFormat())).optString(IFieldMetaData.DECIMALPRECISION);
				return new Integer(decimalPrecision);
			} catch (Exception e) {
				return 2;
			}
		}
		
		public Double applyScaleFactor(Double value, int positionI, int positionJ) {
			String scaleFactor = "";
			int pos;
	
			if (measureOnRow) {
				pos = positionI % measuresInfo.size();
			} else {
				pos = positionJ % measuresInfo.size();
			}
			scaleFactor = measuresInfo.get(pos).getScaleFactor();
	
			return MeasureScaleFactorOption.applyScaleFactor(value, scaleFactor);
	
		}

		public String format(double value, int i, int j, Locale locale) {
			int decimals = this.getFormatXLS(i, j);
			Double scaledValue = this.applyScaleFactor(value, i, j);
			String pattern = "#,##0";
			if (decimals > 0) {
				pattern += ".";
				for (int count = 0; count < decimals; count++) {
					pattern += "0";
				}
			}
			NumberFormat nf = NumberFormat.getInstance(locale);
			DecimalFormat formatter = (DecimalFormat) nf;
			formatter.applyPattern(pattern);
			String toReturn = formatter.format(scaledValue);
			return toReturn;
		}
		
		public String formatPercent(double value, Locale locale) {
			String pattern = "#0.00";
			NumberFormat nf = NumberFormat.getInstance(locale);
			DecimalFormat formatter = (DecimalFormat) nf;
			formatter.applyPattern(pattern);
			String toReturn = formatter.format(value);
			return toReturn;
		}

}
