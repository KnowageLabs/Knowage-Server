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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.knowage.document.export.cockpit.IConverter;
import it.eng.qbe.dataset.QbeDataSet;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.tools.dataset.DatasetManagementAPI;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.metasql.query.item.AndFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.BetweenFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.Filter;
import it.eng.spagobi.tools.dataset.metasql.query.item.InFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.LikeFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.NullaryFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.PlaceholderFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.Projection;
import it.eng.spagobi.tools.dataset.metasql.query.item.SimpleFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.SimpleFilterOperator;
import it.eng.spagobi.tools.dataset.metasql.query.item.UnaryFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.UnsatisfiedFilter;
import it.eng.spagobi.tools.dataset.utils.DataSetUtilities;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Dragan Pirkovic
 *
 */
public class FilterConverter extends CommonJSON implements IConverter<Filter, JSONObject> {

	private final JSONObject aggregations;

	/**
	 * @param dataSet
	 */
	public FilterConverter(IDataSet dataSet, JSONObject aggregations) {
		this.dataSet = dataSet;
		this.aggregations = aggregations;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.knowage.document.export.cockpit.IConverter#convert(java.lang.Object)
	 */
	@Override
	public Filter convert(JSONObject selections) {

		List<Filter> filters = new ArrayList<>(0);
		try {
			Map<String, String> columnAliasToName = new HashMap<String, String>();
			loadColumnAliasToName(getCategories(aggregations), columnAliasToName);
			loadColumnAliasToName(getMeasures(aggregations), columnAliasToName);

			filters.addAll(getFilters(dataSet.getLabel(), selections, columnAliasToName));
		} catch (JSONException e) {

		}

		return getDatasetManagementAPI().getWhereFilter(filters, new ArrayList<SimpleFilter>());
	}

	/**
	 * @return
	 */
	protected DatasetManagementAPI getDatasetManagementAPI() {
		DatasetManagementAPI managementAPI = new DatasetManagementAPI(UserProfileManager.getProfile());
		return managementAPI;
	}

	/**
	 * @return
	 */
	private UserProfile getUserProfile() {
		// TODO Auto-generated method stub
		return null;
	}

	protected List<Filter> getFilters(String datasetLabel, JSONObject selectionsObject, Map<String, String> columnAliasToColumnName) throws JSONException {
		List<Filter> filters = new ArrayList<>(0);

		if (selectionsObject.has(datasetLabel)) {
			JSONObject datasetSelectionObject = selectionsObject.getJSONObject(datasetLabel);
			Iterator<String> it = datasetSelectionObject.keys();

			IDataSet dataSet = DAOFactory.getDataSetDAO().loadDataSetByLabel(datasetLabel);

			boolean isAnEmptySelection = false;
			while (!isAnEmptySelection && it.hasNext()) {
				String columns = it.next();

				// check two cases: in case of click selection the contained is JSON array and operator is IN, in case of filter the contained is JSON object
				Object filtersObject = datasetSelectionObject.get(columns);

				String firstFilterOperator = null;
				JSONArray firstFilterValues = null;

				String secondFilterOperator = null;
				JSONArray secondFilterValues = null;

				if (filtersObject instanceof JSONArray) {

					firstFilterOperator = "IN";
					firstFilterValues = (JSONArray) filtersObject;
					for (int i = 0; i < firstFilterValues.length(); i++) { // looking for filter embedded in array
						JSONObject filterJsonObject = firstFilterValues.optJSONObject(i);
						if (filterJsonObject != null && filterJsonObject.has("filterOperator")) {
							secondFilterOperator = filterJsonObject.optString("filterOperator");
							secondFilterValues = filterJsonObject.getJSONArray("filterVals");
							firstFilterValues.remove(i);
							break; // there is at most one filter embedded in array
						}
					}
				} else if (filtersObject instanceof JSONObject) {

					JSONObject filterJsonObject = (JSONObject) filtersObject;
					firstFilterOperator = filterJsonObject.optString("filterOperator");
					firstFilterValues = filterJsonObject.getJSONArray("filterVals");
				} else {
					throw new SpagoBIRuntimeException("Not recognised filter object " + filtersObject);
				}

				SimpleFilter firstSimpleFilter = getFilter(firstFilterOperator, firstFilterValues, columns, dataSet, columnAliasToColumnName);
				if (firstSimpleFilter != null) {
					SimpleFilter secondSimpleFilter = getFilter(secondFilterOperator, secondFilterValues, columns, dataSet, columnAliasToColumnName);
					if (secondSimpleFilter != null) {
						Filter compoundFilter = getComplexFilter((InFilter) firstSimpleFilter, secondSimpleFilter);
						filters.add(compoundFilter);
					} else {
						filters.add(firstSimpleFilter);
					}
				} else {
					isAnEmptySelection = true;
				}
			}

			if (isAnEmptySelection) {
				filters.clear();
				filters.add(new UnsatisfiedFilter());
			}
		}

		return filters;
	}

	public Filter getComplexFilter(InFilter inFilter, SimpleFilter anotherFilter) {
		SimpleFilterOperator operator = anotherFilter.getOperator();
		if (SimpleFilterOperator.EQUALS_TO_MIN.equals(operator) || SimpleFilterOperator.EQUALS_TO_MAX.equals(operator)) {
			List<Object> operands = inFilter.getOperands();
			Object result = operands.get(0);
			if (result instanceof Comparable == false) {
				throw new SpagoBIRuntimeException("Unable to compare operands of type [" + result.getClass().getName() + "]");
			}
			Comparable comparableResult = (Comparable) result;
			for (int i = 1; i < operands.size(); i++) {
				Object operand = operands.get(i);
				Comparable comparableOperand = (Comparable) operand;
				if (SimpleFilterOperator.EQUALS_TO_MIN.equals(operator)) { // min case
					if (comparableOperand.compareTo(comparableResult) < 0) {
						result = operand;
						comparableResult = comparableOperand;
					}
				} else { // max case
					if (comparableOperand.compareTo(comparableResult) > 0) {
						result = operand;
						comparableResult = comparableOperand;
					}
				}
			}
			return new UnaryFilter(inFilter.getProjections().get(0), SimpleFilterOperator.EQUALS_TO, result);
		} else {
			return new AndFilter(inFilter, anotherFilter);
		}
	}

	public SimpleFilter getFilter(String operatorString, JSONArray valuesJsonArray, String columns, IDataSet dataSet,
			Map<String, String> columnAliasToColumnName) throws JSONException {
		SimpleFilter filter = null;

		if (operatorString != null) {
			SimpleFilterOperator operator = SimpleFilterOperator.ofSymbol(operatorString.toUpperCase());

			if (valuesJsonArray.length() > 0 || operator.isNullary() || operator.isPlaceholder()) {
				List<String> columnsList = getColumnList(columns, dataSet, columnAliasToColumnName);

				List<Projection> projections = new ArrayList<>(columnsList.size());
				for (String columnName : columnsList) {
					projections.add(new Projection(dataSet, columnName));
				}

				List<Object> valueObjects = new ArrayList<>(0);
				if (!operator.isNullary() && !operator.isPlaceholder()) {
					for (int i = 0; i < valuesJsonArray.length(); i++) {
						String[] valuesArray = StringUtilities.splitBetween(valuesJsonArray.getString(i), "'", "','", "'");
						for (int j = 0; j < valuesArray.length; j++) {
							Projection projection = projections.get(j % projections.size());
							valueObjects.add(DataSetUtilities.getValue(valuesArray[j], projection.getType()));
						}
					}
				}

				if (operator.isPlaceholder()) {
					filter = new PlaceholderFilter(projections.get(0), operator);
				} else {
					if (SimpleFilterOperator.IN.equals(operator)) {
						if (valueObjects.isEmpty()) {
							filter = new NullaryFilter(projections.get(0), SimpleFilterOperator.IS_NULL);
						} else {
							filter = new InFilter(projections, valueObjects);
						}
					} else if (SimpleFilterOperator.LIKE.equals(operator)) {
						filter = new LikeFilter(projections.get(0), valueObjects.get(0).toString(), LikeFilter.TYPE.PATTERN);
					} else if (SimpleFilterOperator.BETWEEN.equals(operator)) {
						filter = new BetweenFilter(projections.get(0), valueObjects.get(0), valueObjects.get(1));
					} else if (operator.isNullary()) {
						filter = new NullaryFilter(projections.get(0), operator);
					} else {
						filter = new UnaryFilter(projections.get(0), operator, valueObjects.get(0));
					}
				}
			}
		}
		return filter;
	}

	protected List<String> getColumnList(String columns, IDataSet dataSet, Map<String, String> columnAliasToColumnName) {
		List<String> columnList = new ArrayList<>(Arrays.asList(columns.trim().split("\\s*,\\s*"))); // trim spaces while splitting

		// transform QBE columns
		for (int i = 0; i < columnList.size(); i++) {
			String column = columnList.get(i);
			if (column.contains(":")) {
				QbeDataSet qbeDataSet = (QbeDataSet) dataSet;
				columnList.set(i, qbeDataSet.getColumn(column));
			}
		}

		// transform aliases
		if (columnAliasToColumnName != null) {
			Set<String> aliases = columnAliasToColumnName.keySet();
			if (aliases.size() > 0) {
				for (int i = 0; i < columnList.size(); i++) {
					String column = columnList.get(i);
					if (aliases.contains(column)) {
						columnList.set(i, columnAliasToColumnName.get(column));
					}
				}
			}
		}

		return columnList;
	}

}
