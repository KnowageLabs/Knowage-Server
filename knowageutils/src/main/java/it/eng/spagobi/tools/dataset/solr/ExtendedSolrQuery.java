/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2018 Engineering Ingegneria Informatica S.p.A.
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
 *
 */

package it.eng.spagobi.tools.dataset.solr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.query.AggregationFunctions;
import it.eng.spagobi.tools.dataset.common.query.IAggregationFunction;
import it.eng.spagobi.tools.dataset.metasql.query.item.AbstractSelectionField;
import it.eng.spagobi.tools.dataset.metasql.query.item.CoupledProjection;
import it.eng.spagobi.tools.dataset.metasql.query.item.DataStoreCalculatedField;
import it.eng.spagobi.tools.dataset.metasql.query.item.Filter;
import it.eng.spagobi.tools.dataset.metasql.query.item.Projection;
import it.eng.spagobi.tools.dataset.metasql.query.item.Sorting;

public class ExtendedSolrQuery extends SolrQuery {

	public static final String FACET_PIVOT_MEASURE_ALIAS_PREFIX = "_";
	public static final String FACET_PIVOT_CATEGORY_ALIAS_POSTFIX = "_facet";

	private static final Logger logger = Logger.getLogger(ExtendedSolrQuery.class);

	private int facetLimit = -1;

	public ExtendedSolrQuery(SolrQuery initialQuery) {
		if (initialQuery.getQuery() != null) {
			setQuery(initialQuery.getQuery());
		}
		if (initialQuery.getFilterQueries() != null) {
			setFilterQueries(initialQuery.getFilterQueries());
		}
		if (initialQuery.getFields() != null) {
			setFields(initialQuery.getFields());
		}
		if (initialQuery.getSorts() != null) {
			setSorts(initialQuery.getSorts());
		}

		String facetLimitStr = SingletonConfig.getInstance().getConfigValue("SPAGOBI.DATASET.SOLR.FACET_LIMIT");
		if (facetLimitStr != null) {
			facetLimit = Integer.valueOf(facetLimitStr);
		}
		logger.debug("Limiting the number of buckets for facet query to " + facetLimitStr + " buckets");
	}

	public ExtendedSolrQuery filter(Filter filter) {
		return filter(filter, null);
	}

	public ExtendedSolrQuery filter(Filter filter, List<String> highlightFields) {
		if (filter != null) {
			SolrFilterVisitor visitor = new SolrFilterVisitor(highlightFields);
			visitor.apply(this, filter);
		}
		return this;
	}

	public ExtendedSolrQuery fields(List<AbstractSelectionField> projections) {
		if (!projections.isEmpty()) {
			setFields(null);
			for (AbstractSelectionField projection : projections) {
				if (projection instanceof Projection) {
					Projection proj = (Projection) projection;
					addField(proj.getName());
				} else {
					DataStoreCalculatedField proj = (DataStoreCalculatedField) projection;
					addField(proj.getName());
				}
			}
		}
		return this;
	}

	public ExtendedSolrQuery jsonFacets(List<AbstractSelectionField> groups, int limit) throws JsonProcessingException {
		if (!groups.isEmpty()) {
			Map<String, JsonFacet> jsonFacetMap = new HashMap<>(groups.size());

			for (AbstractSelectionField group : groups) {

				if (group instanceof Projection) {

					Projection proj = (Projection) group;
					JsonFacet jsonFacet;
					if (group instanceof CoupledProjection) {
						jsonFacet = new AggregationJsonFacet(proj.getName(), proj.getAggregationFunction(),
								((CoupledProjection) group).getAggregatedProjection().getName(),limit);
					} else {
						String type = proj.getType().getName();
						if (type.equalsIgnoreCase("java.lang.String")) {
							jsonFacet = new CountJsonFacet(proj.getName(), limit, 0);
						} else {
							jsonFacet = new CountJsonFacet(proj.getName(), limit);
						}
					}
					jsonFacetMap.put(proj.getName(), jsonFacet);
				} else {
					DataStoreCalculatedField proj = (DataStoreCalculatedField) group;
					JsonFacet jsonFacet;
					if (group instanceof CoupledProjection) {
						jsonFacet = new AggregationJsonFacet(proj.getName(), proj.getAggregationFunction(),
								((CoupledProjection) group).getAggregatedProjection().getName(),limit);
					} else {
						String type = proj.getType().getName();
						if (type.equalsIgnoreCase("java.lang.String")) {
							jsonFacet = new CountJsonFacet(proj.getName(), limit, 0);
						} else {
							jsonFacet = new CountJsonFacet(proj.getName(), limit);
						}
					}
					jsonFacetMap.put(proj.getName(), jsonFacet);

				}
			}

			add("json.facet", new ObjectMapper().writeValueAsString(jsonFacetMap));
		}
		return this;
	}

	public SolrQuery jsonFacets(List<AbstractSelectionField> projectionsAbs, List<AbstractSelectionField> groups, List<Sorting> sortings) throws JSONException {
		List<AbstractSelectionField> projections = new ArrayList<AbstractSelectionField>();
		for (AbstractSelectionField projection : projectionsAbs) {
			projections.add(projection);
		}

		List<AbstractSelectionField> measures = getMeasures(projections, groups);
		JSONObject jsonFacet = getMeasureFacet(measures);

		List<AbstractSelectionField> unsortedGroups = getUnsortedGroups(groups, sortings);
		for (AbstractSelectionField unsortedGroup : unsortedGroups) {
			jsonFacet = getJsonFacet(unsortedGroup, jsonFacet);
		}

		for (int i = sortings.size() - 1; i >= 0; i--) {
			Sorting sorting = sortings.get(i);
			jsonFacet = getJsonFacet(sorting, jsonFacet);
		}

		add("json.facet", jsonFacet.toString());
		return this;
	}

	private List<AbstractSelectionField> getMeasures(List<AbstractSelectionField> projections, List<AbstractSelectionField> groups) {
		Map<String, AbstractSelectionField> measureMap = new HashMap<>();
		for (AbstractSelectionField projection : projections) {
			if (projection instanceof Projection) {
				Projection pr = (Projection) projection;
				measureMap.put(pr.getName(), pr);
			} else {
				DataStoreCalculatedField pr = (DataStoreCalculatedField) projection;
				measureMap.put(pr.getName(), pr);
			}

		}
		for (AbstractSelectionField group : groups) {

			if (group instanceof Projection) {
				Projection pr = (Projection) group;
				measureMap.remove(pr.getName());
			} else {
				DataStoreCalculatedField pr = (DataStoreCalculatedField) group;
				measureMap.remove(pr.getName());
			}
		}
		return Arrays.asList(measureMap.values().toArray(new AbstractSelectionField[0]));
	}

	private JSONObject getMeasureFacet(List<AbstractSelectionField> measures) throws JSONException {
		JSONObject jsonObject = new JSONObject();
		for (AbstractSelectionField measure : measures) {

			if (measure instanceof Projection) {

				Projection pr = (Projection) measure;
				IAggregationFunction aggregationFunction = pr.getAggregationFunction();
				String key = FACET_PIVOT_MEASURE_ALIAS_PREFIX + pr.getAliasOrName();
				if (AggregationFunctions.COUNT.equals(aggregationFunction.getName())) {
					JSONObject value = new JSONObject();
					value.put("type", "query");
					value.put("field", pr.getName());
					jsonObject.put(key, value);
				} else {
					String value = String.format("%s(%s)", getAggregationFunction(aggregationFunction), pr.getName());
					jsonObject.put(key, value);
				}
			}

			else {

				DataStoreCalculatedField pr = (DataStoreCalculatedField) measure;
				IAggregationFunction aggregationFunction = pr.getAggregationFunction();
				String key = FACET_PIVOT_MEASURE_ALIAS_PREFIX + pr.getAliasOrName();
				if (AggregationFunctions.COUNT.equals(aggregationFunction.getName())) {
					JSONObject value = new JSONObject();
					value.put("type", "query");
					value.put("field", pr.getName());
					jsonObject.put(key, value);
				} else {
					String value = String.format("%s(%s)", getAggregationFunction(aggregationFunction), pr.getName());
					jsonObject.put(key, value);
				}

			}

		}
		return jsonObject;
	}

	private String getAggregationFunction(IAggregationFunction aggregationFunction) {
		if (AggregationFunctions.COUNT_DISTINCT.equals(aggregationFunction.getName())) {
			return "unique";
		} else {
			return aggregationFunction.getName().toLowerCase();
		}
	}

	private List<AbstractSelectionField> getUnsortedGroups(List<AbstractSelectionField> groups, List<Sorting> sortings) {
		Set<String> sorted = new HashSet<>();
		for (Sorting sorting : sortings) {
			sorted.add(sorting.getProjection().getName());
		}
		List<AbstractSelectionField> unsortedGroups = new ArrayList<>(groups.size() - sortings.size());
		for (AbstractSelectionField group : groups) {
			if (group instanceof Projection) {
				Projection pr = (Projection) group;
				if (!sorted.contains(pr.getName())) {
					unsortedGroups.add(group);
				}
			} else {
				DataStoreCalculatedField pr = (DataStoreCalculatedField) group;
				if (!sorted.contains(pr.getName())) {
					unsortedGroups.add(group);
				}
			}
		}
		return unsortedGroups;
	}

	private JSONObject getJsonFacet(AbstractSelectionField projection, JSONObject jsonFacet) throws JSONException {
		String fieldFacets = "";
		String field = "";
		if (projection instanceof Projection) {
			Projection proj = (Projection) projection;
			fieldFacets = proj.getAliasOrName();
			field = proj.getName();
		} else {
			DataStoreCalculatedField proj = (DataStoreCalculatedField) projection;
			fieldFacets = proj.getAliasOrName();
			field = proj.getName();
		}
		JSONObject innerFacet = new JSONObject();
		innerFacet.put("type", "terms");
		innerFacet.put("field", field);
		innerFacet.put("limit", facetLimit);
		innerFacet.put("missing", true);
		if (jsonFacet.length() > 0) {
			innerFacet.put("facet", jsonFacet);
		}

		JSONObject outerFacet = new JSONObject();
		outerFacet.put(fieldFacets + FACET_PIVOT_CATEGORY_ALIAS_POSTFIX, innerFacet);
		return outerFacet;
	}

	private JSONObject getJsonFacet(Sorting sorting, JSONObject jsonFacet) throws JSONException {

		AbstractSelectionField projs = sorting.getProjection();

		if (projs instanceof Projection) {
			Projection projection = (Projection) projs;
			JSONObject outerFacet = getJsonFacet(projection, jsonFacet);
			JSONObject innerFacet = outerFacet.getJSONObject(projection.getAliasOrName() + FACET_PIVOT_CATEGORY_ALIAS_POSTFIX);
			innerFacet.put("sort", "index " + (sorting.isAscending() ? "asc" : "desc"));
			return outerFacet;
		} else {
			DataStoreCalculatedField projection = (DataStoreCalculatedField) projs;
			JSONObject outerFacet = getJsonFacet(projection, jsonFacet);
			JSONObject innerFacet = outerFacet.getJSONObject(projection.getAliasOrName() + FACET_PIVOT_CATEGORY_ALIAS_POSTFIX);
			innerFacet.put("sort", "index " + (sorting.isAscending() ? "asc" : "desc"));
			return outerFacet;
		}
	}

	public ExtendedSolrQuery facets(List<Projection> groups) {
		if (!groups.isEmpty()) {
			String[] facetFields = new String[groups.size()];
			for (int i = 0; i < groups.size(); i++) {
				facetFields[i] = groups.get(i).getName();
			}
			addFacetField(facetFields);
		}
		return this;
	}

	public ExtendedSolrQuery facets(IDataSet dataSet, String... columnNames) {
		List<Projection> facets = new ArrayList<>(columnNames.length);
		for (String columnName : columnNames) {
			facets.add(new Projection(dataSet, columnName));
		}
		return facets(facets);
	}

	public ExtendedSolrQuery stats(List<AbstractSelectionField> projections) {
		if (!projections.isEmpty()) {
			for (AbstractSelectionField proj : projections) {
				Projection projection = (Projection) proj;
				setGetFieldStatistics(projection.getName());
				if (AggregationFunctions.COUNT_DISTINCT.equals(projection.getAggregationFunction().getName())) {
					addStatsFieldCalcDistinct(projection.getName(), true);
				}
			}
		}
		return this;
	}

	public ExtendedSolrQuery sorts(List<Sorting> sortings) {
		if (!sortings.isEmpty()) {
			for (Sorting sorting : sortings) {
				SolrQuery.ORDER order = sorting.isAscending() ? SolrQuery.ORDER.asc : SolrQuery.ORDER.desc;
				String item = sorting.getProjection().getName();
				addSort(item, order);
			}
		}
		return this;
	}
}
