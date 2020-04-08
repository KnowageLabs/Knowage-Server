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
package it.eng.spagobi.tools.dataset.common.datareader;

import static it.eng.spagobi.tools.dataset.solr.ExtendedSolrQuery.FACET_PIVOT_CATEGORY_ALIAS_POSTFIX;
import static it.eng.spagobi.tools.dataset.solr.ExtendedSolrQuery.FACET_PIVOT_MEASURE_ALIAS_PREFIX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class SolrFacetPivotDataReader extends SolrDataReader {

	static private Logger logger = Logger.getLogger(SolrFacetPivotDataReader.class);

	private Map<String, String> nameToAliasMap;
	private Map<String, Boolean> sortedCategories;

	public SolrFacetPivotDataReader(String jsonPathItems, List<JSONPathAttribute> jsonPathAttributes) {
		super(jsonPathItems, jsonPathAttributes);
		nameToAliasMap = new HashMap<>();
		sortedCategories = new HashMap<>();
	}

	@Override
	public IDataStore read(Object data) {
		JSONObject jsonObject = getHighlightedData((String) data);
		try {
			transformFacetPivotToDocs(jsonObject);
		} catch (JSONException e) {
			throw new SpagoBIRuntimeException("Unable to transform facet pivot to docs", e);
		}
		update(jsonObject);
		return super.read(jsonObject.toString());
	}

	private void transformFacetPivotToDocs(JSONObject jsonObject) throws JSONException {
		JSONObject facetContainer = new JSONObject(jsonObject.getJSONObject("responseHeader").getJSONObject("params").getString("json.facet"));
		while (isFacetContainer(facetContainer)) {
			String rawCategoryAlias = getRawCategoryAlias(facetContainer);
			String categoryAlias = getCategoryAlias(rawCategoryAlias);
			JSONObject facet = facetContainer.getJSONObject(rawCategoryAlias);
			String sort = facet.optString("sort");
			if (!sort.isEmpty()) {
				sortedCategories.put(categoryAlias, "index asc".equals(sort));
			}
			facetContainer = facet.optJSONObject("facet");
		}

		JSONObject facets = jsonObject.getJSONObject("facets");

		if (facets.getInt("count") == 0) {
			jsonObject.remove("facets");
			JSONObject response = jsonObject.getJSONObject("response");
			response.put("numFound", 0);
		} else {
			JSONArray docs = getDocsFromFacetContainer(facets);
			jsonObject.remove("facets");
			JSONObject response = jsonObject.getJSONObject("response");
			response.put("docs", docs);
			response.put("numFound", docs.length());
		}
	}

	private JSONArray getDocsFromFacetContainer(JSONObject facetContainer) throws JSONException {
		String rawCategoryAlias = getRawCategoryAlias(facetContainer);
		Assert.assertNotNull(rawCategoryAlias, "Unable to find inner facet");
		String categoryAlias = getCategoryAlias(rawCategoryAlias);
		JSONObject innerFacet = facetContainer.getJSONObject(rawCategoryAlias);
		return getDocsFromFacet(categoryAlias, innerFacet);
	}

	private JSONArray getDocsFromFacet(String categoryAlias, JSONObject facet) throws JSONException {
		JSONArray docs = new JSONArray();
		if (sortedCategories.containsKey(categoryAlias) && !sortedCategories.get(categoryAlias)) {
			addDocs(getBucketDocs(categoryAlias, facet), docs);
			addDocs(getMissingDocs(categoryAlias, facet), docs);
		} else {
			addDocs(getMissingDocs(categoryAlias, facet), docs);
			addDocs(getBucketDocs(categoryAlias, facet), docs);
		}
		return docs;
	}

	private void addDocs(JSONArray src, JSONArray dst) throws JSONException {
		for (int i = 0; i < src.length(); i++) {
			dst.put(src.getJSONObject(i));
		}
	}

	private JSONArray getMissingDocs(String categoryAlias, JSONObject facet) throws JSONException {
		JSONObject missing = facet.getJSONObject("missing");
		long count = missing.getLong("count");
		if (count > 0) {
			Object value = "";
			return getDocs(missing, categoryAlias, value);
		} else {
			return new JSONArray();
		}
	}

	private JSONArray getBucketDocs(String categoryAlias, JSONObject facet) throws JSONException {
		JSONArray docs = new JSONArray();
		JSONArray buckets = facet.getJSONArray("buckets");
		for (int i = 0; i < buckets.length(); i++) {
			JSONObject bucket = buckets.getJSONObject(i);
			Object value = bucket.get("val");
			JSONArray bucketDocs = getDocs(bucket, categoryAlias, value);
			addDocs(bucketDocs, docs);
		}
		return docs;
	}

	private JSONArray getDocs(JSONObject jsonObject, String categoryAlias, Object value) throws JSONException {
		JSONArray docs;
		if (isFacetContainer(jsonObject)) {
			docs = getDocsFromFacetContainer(jsonObject);
		} else {
			docs = new JSONArray();
			docs.put(getDoc(jsonObject));
		}
		enrichDocs(docs, categoryAlias, value);
		return docs;
	}

	private void enrichDocs(JSONArray docs, String categoryAlias, Object value) throws JSONException {
		for (int i = 0; i < docs.length(); i++) {
			docs.getJSONObject(i).put(categoryAlias, value);
		}
	}

	private JSONObject getDoc(JSONObject jsonObject) throws JSONException {
		JSONObject doc = new JSONObject();
		Iterator<String> keys = jsonObject.keys();
		while (keys.hasNext()) {
			String rawMeasureAlias = keys.next();
			if (isRawMeasureAlias(rawMeasureAlias)) {
				String measureAlias = getMeasureAlias(rawMeasureAlias);
				Object value = jsonObject.get(rawMeasureAlias);
				if (value instanceof JSONObject) {
					value = ((JSONObject) value).getLong("count");
				}
				doc.put(measureAlias, value);
			}
		}
		return doc;
	}

	private String getCategoryAlias(String rawCategoryAlias) {
		Assert.assertTrue(isRawCategoryAlias(rawCategoryAlias), "[" + rawCategoryAlias + "] is not a raw category alias");
		return rawCategoryAlias.substring(0, rawCategoryAlias.length() - FACET_PIVOT_CATEGORY_ALIAS_POSTFIX.length());
	}

	private boolean isRawCategoryAlias(String rawCategoryAlias) {
		return rawCategoryAlias.endsWith(FACET_PIVOT_CATEGORY_ALIAS_POSTFIX);
	}

	private String getMeasureAlias(String rawMeasureAlias) {
		Assert.assertTrue(isRawMeasureAlias(rawMeasureAlias), "[" + rawMeasureAlias + "] is not a raw measure alias");
		return rawMeasureAlias.substring(FACET_PIVOT_MEASURE_ALIAS_PREFIX.length());
	}

	private boolean isRawMeasureAlias(String rawMeasureAlias) {
		return rawMeasureAlias.startsWith(FACET_PIVOT_MEASURE_ALIAS_PREFIX);
	}

	private boolean isFacetContainer(JSONObject facetContainer) {
		if (facetContainer != null) {
			Iterator<String> keys = facetContainer.keys();
			while (keys.hasNext()) {
				String rawCategoryAlias = keys.next();
				if (isRawCategoryAlias(rawCategoryAlias)) {
					return true;
				}
			}
		}
		return false;
	}

	private String getRawCategoryAlias(JSONObject facetContainer) {
		Iterator<String> keys = facetContainer.keys();
		while (keys.hasNext()) {
			String rawCategoryAlias = keys.next();
			if (isRawCategoryAlias(rawCategoryAlias)) {
				return rawCategoryAlias;
			}
		}
		return null;
	}

	private void update(JSONObject jsonObject) {
		List<JSONPathAttribute> jsonPathAttributes = getJsonPathAttributes();
		Map<String, String> jsonPathAttributeNameToTypeMap = new HashMap<>();
		for (JSONPathAttribute jsonPathAttribute : jsonPathAttributes) {
			jsonPathAttributeNameToTypeMap.put(jsonPathAttribute.getName(), jsonPathAttribute.getJsonPathType());
		}
		List<JSONPathAttribute> newJsonPathAttributes = new ArrayList<>();

		nameToAliasMap.clear();

		try {
			JSONObject facetContainer = new JSONObject(jsonObject.getJSONObject("responseHeader").getJSONObject("params").getString("json.facet"));
			while (isFacetContainer(facetContainer)) {
				String rawCategoryAlias = getRawCategoryAlias(facetContainer);
				String categoryAlias = getCategoryAlias(rawCategoryAlias);
				JSONObject facet = facetContainer.getJSONObject(rawCategoryAlias);
				String categoryName = facet.getString("field");
				nameToAliasMap.put(categoryName, categoryAlias);
				newJsonPathAttributes.add(new JSONPathAttribute(categoryAlias, "$." + categoryAlias, jsonPathAttributeNameToTypeMap.get(categoryName)));
				facetContainer = facet.optJSONObject("facet");
			}
			if (facetContainer != null) {
				Pattern aggregatedMeasureDefinitionPattern = Pattern.compile("\\w+\\((\\w+)\\)");
				Iterator<String> keys = facetContainer.keys();
				while (keys.hasNext()) {
					String rawMeasureAlias = keys.next();
					if (isRawMeasureAlias(rawMeasureAlias)) {
						String measureAlias = getMeasureAlias(rawMeasureAlias);

						String measureName;
						String measureDefinition = facetContainer.getString(rawMeasureAlias);
						Matcher matcher = aggregatedMeasureDefinitionPattern.matcher(measureDefinition);
						if (matcher.find()) {
							measureName = matcher.group(1);
						} else {
							JSONObject definition = new JSONObject(measureDefinition);
							if (definition.has("field")) {
								measureName = definition.getString("field");
							} else {
								throw new SpagoBIRuntimeException("Unable to retrieve definition of measure with alias [" + measureAlias + "]");
							}
						}

						nameToAliasMap.put(measureName, measureAlias);
						newJsonPathAttributes.add(new JSONPathAttribute(measureAlias, "$." + measureAlias, jsonPathAttributeNameToTypeMap.get(measureName)));
					}
				}
			}
		} catch (JSONException e) {
			throw new SpagoBIRuntimeException("Unable to retrieve mapping between names and aliases", e);
		}

		jsonPathAttributes.clear();
		jsonPathAttributes.addAll(newJsonPathAttributes);
	}

	public <K, V> K getKey(Map<K, V> map, V value) {
		for (Entry<K, V> entry : map.entrySet()) {
			if (entry.getValue().equals(value)) {
				return entry.getKey();
			}
		}
		return null;
	}

	@Override
	protected String getAlias(String name) {
		return nameToAliasMap.containsKey(name) ? nameToAliasMap.get(name) : name;
	}

	@Override
	protected String getName(String name) {
		return nameToAliasMap.containsValue(name) ? getKey(nameToAliasMap, name) : name;
	}

	@Override
	protected Object getJSONPathValue(Object o, String jsonPathValue) throws JSONException {
		// can be an array with a single value, a single object or also null (not found)
		Object res = null;
		try {
			if (jsonPathValue.contains(" ")) {
				String initial = "$.";
				jsonPathValue = jsonPathValue.substring(jsonPathValue.indexOf("$") + 2);
				jsonPathValue = "['" + jsonPathValue + "']";
				res = JsonPath.read(o, initial.concat(jsonPathValue));
			} else {
				res = JsonPath.read(o, jsonPathValue);
			}
		} catch (PathNotFoundException e) {
			logger.debug("JPath not found " + jsonPathValue);
		}

		return res;
	}
}
