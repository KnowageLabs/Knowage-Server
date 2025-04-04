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
package it.eng.knowage.engine.api.excel.export.oldcockpit.crosstable;

import it.eng.qbe.serializer.SerializationException;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class MeasureFormatter {

	List<CrossTab.MeasureInfo> measuresInfo;
	// String[][] measureMetadata;
	boolean measureOnRow;
	private DecimalFormat numberFormat;
	private String pattern;

	//public MeasureFormatter(JSONObject crosstabDefinitionJSON, DecimalFormat numberFormat, String pattern) throws SerializationException, JSONException {
	public MeasureFormatter(JSONObject crosstabDefinitionJSON, String pattern) throws SerializationException, JSONException {
		JSONArray measuresJSON = crosstabDefinitionJSON.optJSONArray(CrosstabSerializationConstants.MEASURES);
		JSONObject config = crosstabDefinitionJSON.optJSONObject(CrosstabSerializationConstants.CONFIG);
		this.pattern = pattern;
		this.numberFormat = new DecimalFormat(pattern);	
		if (measuresJSON != null) {
			measuresInfo = new ArrayList<>();
			for (int i = 0; i < measuresJSON.length(); i++) {
				JSONObject obj = (JSONObject) measuresJSON.get(i);
				CrossTab.MeasureInfo mi = new CrossTab.MeasureInfo(obj.getString("name"), "", obj.getString("type"), obj.getString("format"), obj.getString("format"),
						obj.optBoolean("excludeFromTotalAndSubtotal"));
				measuresInfo.add(mi);
			}
		}
		measureOnRow = false;
		if (config != null) {
			measureOnRow = config.optString(CrosstabSerializationConstants.MEASURESON).equals(CrosstabSerializationConstants.ROWS);
		}
	}

	public MeasureFormatter(CrossTab crosstab) {
		this.measuresInfo = crosstab.getMeasures();
		this.measureOnRow = crosstab.isMeasureOnRow();
	}

	public String getFormat(Float f, int positionI, int positionJ) {
		int pos;
		String formatted = "";
		if (measureOnRow) {
			pos = positionI % measuresInfo.size();
		} else {
			pos = positionJ % measuresInfo.size();
		}
		try {
			String decimalPrecision = (new JSONObject(measuresInfo.get(pos).getFormat())).optString(IFieldMetaData.DECIMALPRECISION);
			if (decimalPrecision != null) {
				DecimalFormat lnumberFormat = new DecimalFormat(pattern);
				lnumberFormat.setMinimumFractionDigits(Integer.parseInt(decimalPrecision));
				lnumberFormat.setMaximumFractionDigits(Integer.parseInt(decimalPrecision));
				formatted = lnumberFormat.format(f);
			}
		} catch (Exception e) {
			formatted = numberFormat.format(f); 
		}
		return formatted;
	}

	public int getFormatXLS(int positionI, int positionJ) {
		int pos;

		if (measureOnRow) {
			pos = positionI % measuresInfo.size();
		} else {
			pos = positionJ % measuresInfo.size();
		}
		try {
			String decimalPrecision = (new JSONObject(measuresInfo.get(pos).getFormat())).optString(IFieldMetaData.DECIMALPRECISION);
			return Integer.parseInt(decimalPrecision);
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

	// public String format(double value, int i, int j, Locale locale) {
	// int decimals = this.getFormatXLS(i, j);
	// Double scaledValue = this.applyScaleFactor(value, i, j);
	// String pattern = "#,##0";
	// if (decimals > 0) {
	// pattern += ".";
	// for (int count = 0; count < decimals; count++) {
	// pattern += "0";
	// }
	// }
	// NumberFormat nf = NumberFormat.getInstance(locale);
	// DecimalFormat formatter = (DecimalFormat) nf;
	// formatter.applyPattern(pattern);
	// String toReturn = formatter.format(scaledValue);
	// return toReturn;
	// }

	public String format(double value, int i, int j, Locale locale) {
		String pattern = getDefaultPattern(i, j);
		String toReturn = format(value, pattern, 2, i, j, locale);
		return toReturn;
	}

	public String format(double value, String pattern, int precision, int i, int j, Locale locale) {
		if (pattern == null) {
			pattern = getDefaultPattern(i, j);
		}
		Double scaledValue = this.applyScaleFactor(value, i, j);
		DecimalFormat formatter = null;
		if (pattern.equals("#.###") || pattern.equals("#.###,##")) {
			formatter = (DecimalFormat) DecimalFormat.getNumberInstance(Locale.GERMAN);
			if (pattern.equals("#.###"))
				precision = 0;
			formatter.setMinimumFractionDigits(precision);
			formatter.setMaximumFractionDigits(precision);
		} else if (pattern.equals("#,###") || pattern.equals("#,###.##")) {
			formatter = (DecimalFormat) DecimalFormat.getNumberInstance(Locale.ENGLISH);
			if (pattern.equals("#,###"))
				precision = 0;
			formatter.setMinimumFractionDigits(precision);
			formatter.setMaximumFractionDigits(precision);
		} else {
			NumberFormat nf = NumberFormat.getInstance(locale);
			formatter = (DecimalFormat) nf;
//			formatter.applyPattern(pattern);
			formatter.setMinimumFractionDigits(precision);
			formatter.setMaximumFractionDigits(precision);
		}

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

	private String getDefaultPattern(int i, int j) {
		// ex: #,##0.00
		int decimals = this.getFormatXLS(i, j);
		String toReturn = "#,##0";
		if (decimals > 0) {
			toReturn += ".";
			for (int count = 0; count < decimals; count++) {
				toReturn += "0";
			}
		}
		return toReturn;

	}

}
