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
package it.eng.knowage.document.export.cockpit.converter;

import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import it.eng.knowage.document.cockpit.CockpitDocument;
import it.eng.knowage.document.cockpit.template.widget.ICockpitWidget;
import it.eng.knowage.document.export.cockpit.IConverter;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.metasql.query.item.AbstractSelectionField;
import it.eng.spagobi.tools.dataset.metasql.query.item.Filter;
import it.eng.spagobi.tools.dataset.metasql.query.item.IDataStoreConfiguration;
import it.eng.spagobi.tools.dataset.metasql.query.item.Sorting;

/**
 * @author Dragan Pirkovic
 *
 */
public class ConverterFactory {

	/**
	 * @return
	 */
	public static IConverter<Filter, JSONObject> getFilterConverter(IDataSet dataSet, JSONObject aggregations) {
		return new FilterConverter(dataSet, aggregations);
	}

	/**
	 * @return
	 */
	public static IConverter<List<Sorting>, JSONObject> getSortingsConverter(IDataSet dataSet) {
		return new SortingConverter(dataSet);
	}

	public static IConverter<JSONObject, JSONObject> getAggregationConverter(String dataSetLabel) {
		return new AggregationConverter(dataSetLabel);
	}

	public static IConverter<JSONObject, JSONArray> getSelectionsConverter(String dataSetLabel, JSONObject filters) {
		return new SelectionsConverter(dataSetLabel, filters);
	}

	/**
	 * @param documentParams
	 * @return
	 */
	public static IConverter<JSONObject, JSONObject> getParametersConverter(Map<String, String> documentParams) {
		return new ParametersConverter(documentParams);

	}

	/**
	 * @param cockpitDocument
	 * @return
	 */
	public static IConverter<IDataStoreConfiguration, ICockpitWidget> getDataStoreConfigurationConverter(CockpitDocument cockpitDocument) {
		return new DataStoreConfigurationConverter(cockpitDocument);
	}

	/**
	 * @param cockpitDocument
	 * @return
	 */
	public static IConverter<IJsonConfiguration, ICockpitWidget> getJsonConfigurationConverter(CockpitDocument cockpitDocument) {

		return new CockpitWidgetJsonConfConverter(cockpitDocument);
	}

	/**
	 * @param dataset
	 * @return
	 */
	public static IConverter<List<AbstractSelectionField>, JSONObject> getProjectionConverter(IDataSet dataset) {
		// TODO Auto-generated method stub
		return new ProjectionConverter(dataset);
	}

	/**
	 * @param dataset
	 * @return
	 */
	public static IConverter<List<AbstractSelectionField>, JSONObject> getGroupConverter(IDataSet dataset) {
		// TODO Auto-generated method stub
		return new GroupConverter(dataset);
	}

}
