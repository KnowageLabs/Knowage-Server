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
package it.eng.spagobi.engines.worksheet.serializer.json;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class WorkSheetSerializationUtils {
	
	public static final String SHEETS = "sheets";
	public static final String NAME = "name";
	public static final String HEADER = "header";
	public static final String FILTERS = "filters";
	public static final String POSITION = "position";
	public static final String CONTENT = "content";
	public static final String FOOTER = "footer";
	public static final String LAYOUT = "sheetLayout";
	public static final String GLOBAL_FILTERS = "globalFilters";
	public static final String FIELDS_OPTIONS = "fieldsOptions";
	public static final String OPTIONS = "options";
	public static final String DESIGNER = "designer";
	public static final String DESIGNER_TABLE = "Table";
	public static final String DESIGNER_PIVOT = "Pivot Table";
	public static final String DESIGNER_STATIC_PIVOT = "Static Pivot Table";
	public static final String VISIBLE_SELECT_FIELDS = "visibleselectfields";
	public static final String CATEGORY = "category";
	public static final String GROUPING_VARIABLE = "groupingVariable";
	public static final String SERIES = "series";
	public static final String CROSSTABDEFINITION = "crosstabDefinition";
	public static final String FILTERS_ON_DOMAIN_VALUES = "filtersOnDomainValues";
	public static final String WORKSHEETS_ADDITIONAL_DATA = "WORKSHEETS_ADDITIONAL_DATA";
	public static final String WORKSHEETS_ADDITIONAL_DATA_FIELDS_OPTIONS = "fieldsOptions";
	public static final String WORKSHEETS_ADDITIONAL_DATA_FIELDS_OPTIONS_OPTIONS = "options";
	public static final String WORKSHEETS_ADDITIONAL_DATA_FIELDS_OPTIONS_SCALE_FACTOR = "measureScaleFactor";
	public static final String SHEET_FILTERS_INFO = "filtersInfo";
	public static final String ORDER_TYPE = "orderType";
	
	public static JSONArray getFieldOptions(JSONObject worksheetJSON){
		JSONObject worksheetAdditionalData = worksheetJSON.optJSONObject(WORKSHEETS_ADDITIONAL_DATA);
		if (worksheetAdditionalData!=null){
			return worksheetAdditionalData.optJSONArray(WORKSHEETS_ADDITIONAL_DATA_FIELDS_OPTIONS);
		}
		return null;
	}
	
}
