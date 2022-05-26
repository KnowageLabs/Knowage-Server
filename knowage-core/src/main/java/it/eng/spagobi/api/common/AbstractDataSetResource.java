/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.api.common;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.InitialContext;
import javax.validation.ValidationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import it.eng.knowage.functionscatalog.utils.CatalogFunctionException;
import it.eng.knowage.functionscatalog.utils.CatalogFunctionRuntimeConfigDTO;
import it.eng.knowage.functionscatalog.utils.CatalogFunctionTransformer;
import it.eng.knowage.parsers.CaseChangingCharStream;
import it.eng.knowage.parsers.SQLiteLexer;
import it.eng.knowage.parsers.SQLiteParser;
import it.eng.knowage.parsers.ThrowingErrorListener;
import it.eng.qbe.dataset.FederatedDataSet;
import it.eng.qbe.dataset.QbeDataSet;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.execution.service.ExecuteAdHocUtility;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.sdk.datasets.bo.SDKDataSetParameter;
import it.eng.spagobi.tools.dataset.DatasetManagementAPI;
import it.eng.spagobi.tools.dataset.bo.AbstractJDBCDataset;
import it.eng.spagobi.tools.dataset.bo.DatasetEvaluationStrategyType;
import it.eng.spagobi.tools.dataset.bo.FlatDataSet;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.PreparedDataSet;
import it.eng.spagobi.tools.dataset.bo.SolrDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datawriter.IDataWriter;
import it.eng.spagobi.tools.dataset.common.datawriter.JSONDataWriter;
import it.eng.spagobi.tools.dataset.common.query.AggregationFunctions;
import it.eng.spagobi.tools.dataset.common.query.IAggregationFunction;
import it.eng.spagobi.tools.dataset.common.transformer.IDataStoreTransformer;
import it.eng.spagobi.tools.dataset.constants.DataSetConstants;
import it.eng.spagobi.tools.dataset.dao.DataSetFactory;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.dataset.exceptions.DatasetInUseException;
import it.eng.spagobi.tools.dataset.exceptions.ParametersNotValorizedException;
import it.eng.spagobi.tools.dataset.metasql.query.item.AbstractSelectionField;
import it.eng.spagobi.tools.dataset.metasql.query.item.CoupledCalculatedFieldProjection;
import it.eng.spagobi.tools.dataset.metasql.query.item.CoupledProjection;
import it.eng.spagobi.tools.dataset.metasql.query.item.DataStoreCalculatedField;
import it.eng.spagobi.tools.dataset.metasql.query.item.DataStoreCatalogFunctionField;
import it.eng.spagobi.tools.dataset.metasql.query.item.Filter;
import it.eng.spagobi.tools.dataset.metasql.query.item.InFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.LikeFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.MultipleProjectionSimpleFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.Projection;
import it.eng.spagobi.tools.dataset.metasql.query.item.SimpleFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.SimpleSelectionField;
import it.eng.spagobi.tools.dataset.metasql.query.item.Sorting;
import it.eng.spagobi.tools.dataset.metasql.query.item.UnsatisfiedFilter;
import it.eng.spagobi.tools.dataset.utils.DataSetUtilities;
import it.eng.spagobi.tools.dataset.utils.datamart.SpagoBICoreDatamartRetriever;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.database.DataBaseException;
import it.eng.spagobi.utilities.database.DataBaseFactory;
import it.eng.spagobi.utilities.database.IDataBase;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

public abstract class AbstractDataSetResource extends AbstractSpagoBIResource {

	private static final String REGEX_FIELDS_VALIDATION = "(?:\\\"[a-zA-Z0-9\\-\\_\\s]*\\\")";
	static protected Logger logger = Logger.getLogger(AbstractDataSetResource.class);
	private static final int SOLR_FACETS_DEFAULT_LIMIT = 10;
	private static final String VALIDATION_OK = "OK";
	private static final String VALIDATION_KO = "KO";

	// ===================================================================
	// UTILITY METHODS
	// ===================================================================

	protected DatasetManagementAPI getDatasetManagementAPI() {
		DatasetManagementAPI managementAPI = new DatasetManagementAPI(getUserProfile());
		return managementAPI;
	}

	protected Map<String, Object> getDataSetWriterProperties() throws JSONException {
		Map<String, Object> properties = new HashMap<String, Object>();
		JSONArray fieldOptions = new JSONArray("[{id: 1, options: {measureScaleFactor: 0.5}}]");
		properties.put(JSONDataWriter.PROPERTY_FIELD_OPTION, fieldOptions);
		return properties;
	}

	protected Integer[] getIdsAsIntegers(String ids) {
		Integer[] idArray = null;
		if (ids != null && !ids.isEmpty()) {
			String[] split = ids.split(",");
			idArray = new Integer[split.length];
			for (int i = 0; i < split.length; i++) {
				idArray[i] = Integer.parseInt(split[i]);
			}
		}
		return idArray;
	}

	/**
	 * TODO is isNearRealtime really needed? It comes from frontend, isn't it a specific info of the dataset?
	 *
	 * @deprecated
	 */
	@Deprecated
	public String getDataStore(String label, String parameters, Map<String, Object> drivers, String selections, String likeSelections, int maxRowCount,
			String aggregations, String summaryRow, int offset, int fetchSize, Boolean isNearRealtime, Set<String> indexes, String widgetName) {
		return getDataStore(label, parameters, drivers, selections, likeSelections, maxRowCount, aggregations, summaryRow, offset, fetchSize, isNearRealtime,
				null, indexes, widgetName);
	}

	public String getDataStore(String label, String parameters, Map<String, Object> drivers, String selections, String likeSelections, int maxRowCount,
			String aggregations, String summaryRow, int offset, int fetchSize, Set<String> indexes, String widgetName) {
		return getDataStore(label, parameters, drivers, selections, likeSelections, maxRowCount, aggregations, summaryRow, offset, fetchSize, null, null,
				indexes, widgetName);
	}

	public String getDataStore(String label, String parameters, Map<String, Object> drivers, String selections, String likeSelections, int maxRowCount,
			String aggregations, String summaryRow, int offset, int fetchSize, Boolean isNearRealtime, String options, Set<String> indexes, String widgetName) {
		logger.debug("IN");
		Monitor totalTiming = MonitorFactory.start("Knowage.AbstractDataSetResource.getDataStore");
		try {
			Monitor timing = MonitorFactory.start("Knowage.AbstractDataSetResource.getDataStore:validateParams");

			int maxResults = Integer.parseInt(SingletonConfig.getInstance().getConfigValue("SPAGOBI.API.DATASET.MAX_ROWS_NUMBER"));
			logger.debug("Offset [" + offset + "], fetch size [" + fetchSize + "], max results[" + maxResults + "]");

			if (maxResults <= 0) {
				throw new SpagoBIRuntimeException("SPAGOBI.API.DATASET.MAX_ROWS_NUMBER value cannot be a non-positive integer");
			}
			if (offset < 0) {
				logger.debug("Offset size is not valid. Setting it to [0] by default.");
				offset = 0;
			}
			if (fetchSize < -1) {
				logger.debug("Fetch size is not valid. Setting it to [-1] by default.");
				fetchSize = -1;
			}
			if (fetchSize > maxResults) {
				throw new IllegalArgumentException("The page requested is too big. Max page size is equals to [" + maxResults + "]");
			}
			if (maxRowCount > maxResults) {
				throw new IllegalArgumentException("The dataset requested is too big. Max row count is equals to [" + maxResults + "]");
			}

			IDataSetDAO dataSetDao = DAOFactory.getDataSetDAO();
			dataSetDao.setUserProfile(getUserProfile());
			IDataSet dataSet = dataSetDao.loadDataSetByLabel(label);
			if (isNearRealtime == null) {
				isNearRealtime = isNearRealtimeSupported(dataSet);
			}
			Assert.assertNotNull(dataSet, "Unable to load dataset with label [" + label + "]");
			dataSet.setUserProfile(getUserProfile());
			dataSet.setDrivers(drivers);

			timing.stop();
			timing = MonitorFactory.start("Knowage.AbstractDataSetResource.getDataStore:getQueryDetails");

			List<AbstractSelectionField> projections = new ArrayList<AbstractSelectionField>(0);
			List<AbstractSelectionField> groups = new ArrayList<AbstractSelectionField>(0);
			List<Sorting> sortings = new ArrayList<Sorting>(0);
			Map<String, String> columnAliasToName = new HashMap<String, String>();
			if (aggregations != null && !aggregations.isEmpty()) {
				JSONObject aggregationsObject = new JSONObject(aggregations);
				JSONArray categoriesObject = aggregationsObject.getJSONArray("categories");
				JSONArray measuresObject = aggregationsObject.getJSONArray("measures");

				loadColumnAliasToName(categoriesObject, columnAliasToName);
				loadColumnAliasToName(measuresObject, columnAliasToName);

				Map<String, Object> optionMap;
				if (options != null && !options.isEmpty()) {
					ObjectMapper objectMapper = new ObjectMapper();
					optionMap = new HashMap<>((Map<? extends String, ?>) objectMapper.readValue(options, new TypeReference<Map<String, Object>>() {
					}));
				} else {
					optionMap = new HashMap<>();
				}
				applyOptions(dataSet, optionMap);

				projections.addAll(getProjections(dataSet, categoriesObject, measuresObject, columnAliasToName));
				groups.addAll(getGroups(dataSet, categoriesObject, measuresObject, columnAliasToName, hasSolrFacetPivotOption(dataSet, optionMap)));
				sortings.addAll(getSortings(dataSet, categoriesObject, measuresObject, columnAliasToName));

				if (isSolrDataset(dataSet)) {
					IDataSet dataSetCopy = null;
					if (dataSet instanceof VersionedDataSet) {
						dataSetCopy = ((VersionedDataSet) dataSet).getWrappedDataset();
					}
					SolrDataSet solrDS = (SolrDataSet) dataSetCopy;
					solrDS.setFacetsLimitOption(getSolrFacetLimitOption(optionMap));
					((VersionedDataSet) dataSet).setWrappedDataset(solrDS);
				}

			}

			List<Filter> filters = new ArrayList<>(0);
			if (selections != null && !selections.isEmpty()) {
				JSONObject selectionsObject = new JSONObject(selections);
				if (selectionsObject.names() != null) {
					filters.addAll(getFilters(label, selectionsObject, columnAliasToName));
				}
			}

			List<SimpleFilter> likeFilters = new ArrayList<>(0);
			if (likeSelections != null && !likeSelections.equals("")) {
				JSONObject likeSelectionsObject = new JSONObject(likeSelections);
				if (likeSelectionsObject.names() != null) {
					likeFilters.addAll(getLikeFilters(label, likeSelectionsObject, columnAliasToName));
				}
			}

			Monitor timingMinMax = MonitorFactory.start("Knowage.AbstractDataSetResource.getDataStore:calculateMinMax");
			filters = getDatasetManagementAPI().calculateMinMaxFilters(dataSet, isNearRealtime, DataSetUtilities.getParametersMap(parameters), filters,
					likeFilters, indexes);
			timingMinMax.stop();

			Filter where = getDatasetManagementAPI().getWhereFilter(filters, likeFilters);

			timing.stop();

			List<List<AbstractSelectionField>> summaryRowArray = getSummaryRowArray(summaryRow, dataSet, columnAliasToName);

			IDataStore dataStore = getDatasetManagementAPI().getDataStore(dataSet, isNearRealtime, DataSetUtilities.getParametersMap(parameters), projections,
					where, groups, sortings, summaryRowArray, offset, fetchSize, maxRowCount, indexes);

			// if required apply function from catalog
			String catalogFuncId = getCatalogFunctionUuid(projections);
			if (catalogFuncId != null) {
				CatalogFunctionRuntimeConfigDTO catalogFunctionConfig = getCatalogFunctionConfiguration(projections);
				IDataStoreTransformer functionTransformer = new CatalogFunctionTransformer(getUserProfile(), catalogFuncId, catalogFunctionConfig);
				functionTransformer.transform(dataStore);
			}

			IDataWriter dataWriter = getDataStoreWriter();

			timing = MonitorFactory.start("Knowage.AbstractDataSetResource.getDataStore:convertToJson");
			Object gridDataFeed = dataWriter.write(dataStore);
			timing.stop();

			String stringFeed = gridDataFeed.toString();
			return stringFeed;
		} catch (ValidationException v) {
			throw v;
		} catch (ParametersNotValorizedException p) {
			throw p;
		} catch (CatalogFunctionException c) {
			throw c;
		} catch (Throwable t) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", t);
		} finally {
			totalTiming.stop();
			logger.debug("OUT");
		}
	}

	private String getCatalogFunctionUuid(List<AbstractSelectionField> projections) {
		String uuid = null;
		for (AbstractSelectionField p : projections) {
			if (p instanceof DataStoreCatalogFunctionField) {
				String oldUuid = uuid;
				uuid = ((DataStoreCatalogFunctionField) p).getCatalogFunctionUuid();
				if (oldUuid != null && !oldUuid.equals(uuid))
					throw new SpagoBIRuntimeException("Only one function supported");
			}
		}
		return uuid;
	}

	private CatalogFunctionRuntimeConfigDTO getCatalogFunctionConfiguration(List<AbstractSelectionField> projections) {
		for (AbstractSelectionField p : projections) {
			if (p instanceof DataStoreCatalogFunctionField) {
				// we can take the first configuration since all the other ones will be identical
				return CatalogFunctionRuntimeConfigDTO.fromJSON(((DataStoreCatalogFunctionField) p).getCatalogFunctionConfig());
			}
		}
		throw new SpagoBIRuntimeException("Couldn't retrieve function configuration");
	}

	@SuppressWarnings("unused")
	private List<List<AbstractSelectionField>> getSummaryRowArray(String summaryJson, IDataSet dataSet, Map<String, String> columnAliasToName)
			throws JSONException {
		if (summaryJson != null && !summaryJson.isEmpty()) {

			List<List<AbstractSelectionField>> returnList = new ArrayList<List<AbstractSelectionField>>();
			JSONArray summaryRowObject = new JSONArray(summaryJson);

			if (summaryRowObject != null && summaryRowObject.length() == 1) {

				// old way

				JSONObject summaryRowObjectArray = summaryRowObject.getJSONObject(0);
				JSONArray summaryRowMeasuresObject = summaryRowObjectArray.getJSONArray("measures");

				List<AbstractSelectionField> summaryRowProjections = new ArrayList<AbstractSelectionField>(0);

				summaryRowProjections.addAll(getProjections(dataSet, new JSONArray(), summaryRowMeasuresObject, columnAliasToName));

				returnList.add(summaryRowProjections);

				return returnList;

			} else { // new way

				for (int i = 0; i < summaryRowObject.length(); i++) {

					JSONObject jsonObj = summaryRowObject.getJSONObject(i);

					JSONArray summaryRowMeasuresObject = jsonObj.getJSONArray("measures");

					List<AbstractSelectionField> summaryRowProjections = new ArrayList<AbstractSelectionField>(0);

					summaryRowProjections.addAll(getProjections(dataSet, new JSONArray(), summaryRowMeasuresObject, columnAliasToName));

					returnList.add(summaryRowProjections);

				}
				return returnList;
			}
		}
		return null;
	}

	private void applyOptions(IDataSet dataSet, Map<String, Object> options) {
		if (hasSolrSimpleOption(dataSet, options)) {
			applySolrSimpleOption(dataSet);
		} else if (hasSolrFacetPivotOption(dataSet, options)) {
			applySolrFacetPivotOption(dataSet);
		}
	}

	private void applySolrFacetPivotOption(IDataSet dataSet) {
		if (dataSet instanceof VersionedDataSet) {
			VersionedDataSet versionedDataSet = (VersionedDataSet) dataSet;
			dataSet = versionedDataSet.getWrappedDataset();
		}
		SolrDataSet solrDataSet = (SolrDataSet) dataSet;
		solrDataSet.setEvaluationStrategy(DatasetEvaluationStrategyType.SOLR_FACET_PIVOT);
	}

	private void applySolrSimpleOption(IDataSet dataSet) {
		if (dataSet instanceof VersionedDataSet) {
			VersionedDataSet versionedDataSet = (VersionedDataSet) dataSet;
			dataSet = versionedDataSet.getWrappedDataset();
		}
		SolrDataSet solrDataSet = (SolrDataSet) dataSet;
		solrDataSet.setEvaluationStrategy(DatasetEvaluationStrategyType.SOLR_SIMPLE);
	}

	private boolean hasSolrFacetPivotOption(IDataSet dataSet, Map<String, Object> options) {
		return isSolrDataset(dataSet) && Boolean.TRUE.equals(options.get("solrFacetPivot"));
	}

	private boolean hasSolrSimpleOption(IDataSet dataSet, Map<String, Object> options) {
		return isSolrDataset(dataSet) && Boolean.TRUE.equals(options.get("solrSimple"));
	}

	private int getSolrFacetLimitOption(Map<String, Object> options) {
		if (options.get("facetsLimit") != null) {

			return Integer.parseInt(options.get("facetsLimit").toString());
		}
		return SOLR_FACETS_DEFAULT_LIMIT;
	}

	private boolean isSolrDataset(IDataSet dataSet) {
		if (dataSet instanceof VersionedDataSet) {
			dataSet = ((VersionedDataSet) dataSet).getWrappedDataset();
		}
		return dataSet instanceof SolrDataSet;
	}

	protected List<AbstractSelectionField> getProjectionsSummary(IDataSet dataSet, JSONArray categories, JSONArray measures,
			Map<String, String> columnAliasToName) throws JSONException {
		ArrayList<AbstractSelectionField> projections = new ArrayList<AbstractSelectionField>(categories.length() + measures.length());

		for (int i = 0; i < categories.length(); i++) {
			JSONObject category = categories.getJSONObject(i);
			addProjection(dataSet, projections, category, columnAliasToName);

		}

		for (int i = 0; i < measures.length(); i++) {
			JSONObject measure = measures.getJSONObject(i);
			addProjection(dataSet, projections, measure, columnAliasToName);

		}

		return projections;
	}

	protected List<AbstractSelectionField> getProjections(IDataSet dataSet, JSONArray categories, JSONArray measures, Map<String, String> columnAliasToName)
			throws JSONException, ValidationException {
		ArrayList<AbstractSelectionField> projections = new ArrayList<>(categories.length() + measures.length());
		addProjections(dataSet, categories, columnAliasToName, projections);
		addProjections(dataSet, measures, columnAliasToName, projections);
		return projections;
	}

	private void addProjections(IDataSet dataSet, JSONArray categories, Map<String, String> columnAliasToName, ArrayList<AbstractSelectionField> projections)
			throws JSONException, ValidationException {
		for (int i = 0; i < categories.length(); i++) {
			JSONObject category = categories.getJSONObject(i);
			addProjection(dataSet, projections, category, columnAliasToName);
		}
	}

	private void addProjection(IDataSet dataSet, ArrayList<AbstractSelectionField> projections, JSONObject catOrMeasure, Map<String, String> columnAliasToName)
			throws JSONException, ValidationException {

		String functionObj = catOrMeasure.optString("funct");
		if (functionObj.startsWith("[")) { // check if it is an array
			// call for each aggregation function
			JSONArray functs = new JSONArray(functionObj);
			for (int j = 0; j < functs.length(); j++) {
				String functName = functs.getString(j);
				AbstractSelectionField projection = getProjectionWithFunct(dataSet, catOrMeasure, columnAliasToName, functName);
				projections.add(projection);
			}
		} else {
			// only one aggregation function
			AbstractSelectionField projection = getProjection(dataSet, catOrMeasure, columnAliasToName);
			projections.add(projection);
		}

	}

	private AbstractSelectionField getProjectionWithFunct(IDataSet dataSet, JSONObject jsonObject, Map<String, String> columnAliasToName, String functName)
			throws JSONException, ValidationException {
		String columnName = getColumnName(jsonObject, columnAliasToName);
		String columnAlias = getColumnAlias(jsonObject, columnAliasToName);
		IAggregationFunction function = AggregationFunctions.get(functName);
		String functionColumnName = jsonObject.optString("functColumn");
		AbstractSelectionField projection = null;
		if (jsonObject.has("formula")) {
			function = AggregationFunctions.get("NONE");
		}
		if (jsonObject.has("catalogFunctionId")) { // check if the column is coming from catalog function
			String catalogFuncUuid = jsonObject.getString("catalogFunctionId");
			JSONObject catalogFuncConf = jsonObject.getJSONObject("catalogFunctionConfig");
			projection = new DataStoreCatalogFunctionField(function, columnAlias, columnAlias, catalogFuncUuid, catalogFuncConf);
		} else if (!function.equals(AggregationFunctions.COUNT_FUNCTION) && functionColumnName != null && !functionColumnName.isEmpty()) {
			if (jsonObject.has("formula")) {
				String formula = jsonObject.optString("formula");
				DataStoreCalculatedField aggregatedProjection = new DataStoreCalculatedField(dataSet, functionColumnName, formula);
				projection = new CoupledCalculatedFieldProjection(function, aggregatedProjection, dataSet, columnName, columnAlias);
			} else {
				Projection aggregatedProjection = new Projection(dataSet, functionColumnName);
				projection = new CoupledProjection(function, aggregatedProjection, dataSet, columnName, columnAlias);
			}
		} else {
			if (jsonObject.has("formula")) {
				String formula = jsonObject.optString("formula");
				projection = new DataStoreCalculatedField(function, dataSet, columnAlias, columnAlias, formula);
			} else {
				projection = new Projection(function, dataSet, columnName, columnAlias);
			}
		}
		return projection;
	}

	public String validateFormula(String formula, List<SimpleSelectionField> columns) throws ValidationException, JSONException {

		validateBrackets(formula);
		validateFields(formula, columns);

		formula = "select ".concat(formula);
		CharStream inputStream = CharStreams.fromString(formula);
		SQLiteLexer tokenSource = new SQLiteLexer(new CaseChangingCharStream(inputStream, true));
		TokenStream tokenStream = new CommonTokenStream(tokenSource);
		SQLiteParser sQLiteParser = new SQLiteParser(tokenStream);

		sQLiteParser.addErrorListener(ThrowingErrorListener.INSTANCE);
		try {
			ParseTree root = sQLiteParser.select_stmt();
			root.toStringTree();
		} catch (Exception e) {
			throw new ValidationException(e);
		}
		if (sQLiteParser.getNumberOfSyntaxErrors() > 0) {
			throw new ValidationException();

		}

		return VALIDATION_OK;
	}

	private void validateBrackets(String formula) {
		int roundBrackets = 0;
		int squareBrackets = 0;
		int curlyBrackets = 0;

		for (int i = 0; i < formula.length(); i++) {
			switch (formula.charAt(i)) {
			case '(':
				roundBrackets++;
				break;
			case ')':
				roundBrackets--;
				break;
			case '[':
				squareBrackets++;
				break;
			case ']':
				squareBrackets--;
				break;
			case '{':
				curlyBrackets++;
				break;
			case '}':
				curlyBrackets--;
				break;

			default:
				break;
			}
		}

		if (roundBrackets != 0 || squareBrackets != 0 || curlyBrackets != 0)
			throw new ValidationException();

	}

	private void validateFields(String formula, List<SimpleSelectionField> columns) {
		String regex = REGEX_FIELDS_VALIDATION;
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(formula);

		while (m.find()) {
			boolean found = false;
			for (SimpleSelectionField simpleSelectionField : columns) {

				if (simpleSelectionField.getName().equals(m.group(0).replace("\"", ""))) {
					found = true;
					break;
				}
			}

			if (!found)
				throw new ValidationException();
		}

	}

	private AbstractSelectionField getProjection(IDataSet dataSet, JSONObject jsonObject, Map<String, String> columnAliasToName) throws JSONException {
		return getProjectionWithFunct(dataSet, jsonObject, columnAliasToName, jsonObject.optString("funct")); // caso in cui ci siano facets complesse (coupled
		// proj)
	}

	private String getColumnName(JSONObject jsonObject, Map<String, String> columnAliasToName) throws JSONException {
		if (jsonObject.isNull("id") && jsonObject.isNull("columnName")) {
			return getColumnAlias(jsonObject, columnAliasToName);
		} else {

			if (jsonObject.has("formula")) {
				// it is a calculated field
				return jsonObject.getString("formula");
			}

			String id = jsonObject.getString("id");
			boolean isIdMatching = columnAliasToName.containsKey(id) || columnAliasToName.containsValue(id);

			String columnName = jsonObject.getString("columnName");
			boolean isColumnNameMatching = columnAliasToName.containsKey(columnName) || columnAliasToName.containsValue(columnName);

			Assert.assertTrue(isIdMatching || isColumnNameMatching, "Column name [" + columnName + "] not found in dataset metadata");
			return isColumnNameMatching ? columnName : id;
		}
	}

	private String getColumnAlias(JSONObject jsonObject, Map<String, String> columnAliasToName) throws JSONException {
		String columnAlias = jsonObject.getString("alias");
		Assert.assertTrue(columnAliasToName.containsKey(columnAlias) || columnAliasToName.containsValue(columnAlias),
				"Column alias [" + columnAlias + "] not found in dataset metadata");
		return columnAlias;
	}

	protected List<AbstractSelectionField> getGroups(IDataSet dataSet, JSONArray categories, JSONArray measures, Map<String, String> columnAliasToName,
			boolean forceGroups) throws JSONException {
		ArrayList<AbstractSelectionField> groups = new ArrayList<>(0);

		// hasAggregationInCategory se categoria di aggregazione del for ha una funzione di aggregazione

		boolean hasAggregatedMeasures = hasAggregations(measures);

		for (int i = 0; i < categories.length(); i++) {
			JSONObject category = categories.getJSONObject(i);
			String functionName = category.optString("funct");
			if (forceGroups || hasAggregatedMeasures || hasAggregationInCategory(category) || hasCountAggregation(functionName)) {
				AbstractSelectionField projection = getProjection(dataSet, category, columnAliasToName);
				groups.add(projection);
			}
		}

		if (forceGroups) {
			for (int i = 0; i < measures.length(); i++) {
				JSONObject measure = measures.getJSONObject(i);
				String functionName = measure.optString("funct");
				if (hasNoneAggregation(functionName)) {
					AbstractSelectionField selection = getProjection(dataSet, measure, columnAliasToName);

					if (selection instanceof Projection) {
						groups.add(selection);
					} else if (selection instanceof DataStoreCalculatedField) {
						groups.add(selection);
					}

				}
			}
		}

		return groups;
	}

	private boolean hasCountAggregation(String functionName) { // caso in cui arrivano facets semplici
		return AggregationFunctions.get(functionName).getName().equals(AggregationFunctions.COUNT)
				|| AggregationFunctions.get(functionName).getName().equals(AggregationFunctions.COUNT_DISTINCT);
	}

	private boolean hasNoneAggregation(String functionName) {
		return AggregationFunctions.get(functionName).getName().equals(AggregationFunctions.NONE);
	}

	private boolean hasAggregations(JSONArray fields) throws JSONException {
		for (int i = 0; i < fields.length(); i++) {
			JSONObject field = fields.getJSONObject(i);
			String functionName = field.optString("funct");
			if (!AggregationFunctions.get(functionName).getName().equals(AggregationFunctions.NONE) && !field.has("formula")) {
				return true;
			}
			if (field.has("formula")) {
				for (String aggr : AggregationFunctions.getAggregationsList()) {
					String regex = ".*" + aggr + ".*";
					boolean hasAggregationFunction = Pattern.matches(regex, field.getString("formula"));
					if (hasAggregationFunction)
						return true;
				}
			}
		}
		return false;
	}

	private boolean hasAggregationInCategory(JSONObject field) throws JSONException {
		String functionName = field.optString("funct");
		if (!AggregationFunctions.get(functionName).getName().equals(AggregationFunctions.NONE)) {
			return true;
		}

		return false;
	}

	protected List<Sorting> getSortings(IDataSet dataSet, JSONArray categories, JSONArray measures, Map<String, String> columnAliasToName)
			throws JSONException {
		ArrayList<Sorting> sortings = new ArrayList<Sorting>(0);

		for (int i = 0; i < categories.length(); i++) {
			JSONObject categoryObject = categories.getJSONObject(i);
			Sorting sorting = getSorting(dataSet, categoryObject, columnAliasToName);
			if (sorting != null) {
				sortings.add(sorting);
			}
		}

		for (int i = 0; i < measures.length(); i++) {
			JSONObject measure = measures.getJSONObject(i);
			Sorting sorting = getSorting(dataSet, measure, columnAliasToName);
			if (sorting != null) {
				sortings.add(sorting);
			}
		}

		return sortings;
	}

	private Sorting getSorting(IDataSet dataSet, JSONObject jsonObject, Map<String, String> columnAliasToName) throws JSONException {
		Sorting sorting = null;

		String orderType = (String) jsonObject.opt("orderType");
		if (orderType != null && !orderType.isEmpty() && ("ASC".equalsIgnoreCase(orderType) || "DESC".equalsIgnoreCase(orderType))) {
			IAggregationFunction function = AggregationFunctions.get(jsonObject.optString("funct"));
			String orderColumn = (String) jsonObject.opt("orderColumn");

			if (jsonObject.has("formula")) {
				DataStoreCalculatedField projection;
				if (orderColumn != null && !orderColumn.isEmpty() && !orderType.isEmpty()) {
					String alias = jsonObject.optString("alias");
					projection = new DataStoreCalculatedField(function, dataSet, orderColumn, alias, jsonObject.getString("formula"));
				} else {
					String columnName = getColumnName(jsonObject, columnAliasToName);
					projection = new DataStoreCalculatedField(function, dataSet, columnName, orderColumn);
				}

				boolean isAscending = "ASC".equalsIgnoreCase(orderType);

				sorting = new Sorting(projection, isAscending);
			} else {
				Projection projection;
				String alias = jsonObject.optString("alias");
				if (orderColumn != null && !orderColumn.isEmpty() && !orderType.isEmpty()) {
					projection = new Projection(function, dataSet, orderColumn, alias);
				} else {
					String columnName = getColumnName(jsonObject, columnAliasToName);
					projection = new Projection(function, dataSet, columnName, alias);
				}

				boolean isAscending = "ASC".equalsIgnoreCase(orderType);

				sorting = new Sorting(projection, isAscending);
			}
		}

		return sorting;
	}

	protected List<Filter> getFilters(String datasetLabel, JSONObject selectionsObject, Map<String, String> columnAliasToColumnName) throws JSONException {
		List<Filter> filters = new ArrayList<>(0);

		if (selectionsObject.has(datasetLabel)) {
			JSONObject datasetSelectionObject = selectionsObject.getJSONObject(datasetLabel);
			Iterator<String> it = datasetSelectionObject.keys();

			IDataSet dataSet = getDataSetDAO().loadDataSetByLabel(datasetLabel);

			boolean isAnEmptySelection = false;

			while (!isAnEmptySelection && it.hasNext()) {
				String columnsString = it.next();

				JSONArray valuesJsonArray = datasetSelectionObject.getJSONArray(columnsString);
				if (valuesJsonArray.length() == 0) {
					isAnEmptySelection = true;
					break;
				}

				List<String> columnsList = getColumnList(columnsString, dataSet, columnAliasToColumnName);
				List<Projection> projections = new ArrayList<>(columnsList.size());
				for (String columnName : columnsList) {
					projections.add(new Projection(dataSet, columnName));
				}

				List<Object> valueObjects = new ArrayList<>(0);
				for (int i = 0; i < valuesJsonArray.length(); i++) {
					String[] valuesArray = StringUtilities.splitBetween(valuesJsonArray.getString(i), "'", "','", "'");
					for (int j = 0; j < valuesArray.length; j++) {
						Projection projection = projections.get(j % projections.size());
						valueObjects.add(DataSetUtilities.getValue(valuesArray[j], projection.getType()));
					}
				}

				MultipleProjectionSimpleFilter inFilter = new InFilter(projections, valueObjects);
				filters.add(inFilter);
			}

			if (isAnEmptySelection) {
				filters.clear();
				filters.add(new UnsatisfiedFilter());
			}
		}

		return filters;
	}

	protected List<SimpleFilter> getLikeFilters(String datasetLabel, JSONObject likeSelectionsObject, Map<String, String> columnAliasToColumnName)
			throws JSONException {
		List<SimpleFilter> likeFilters = new ArrayList<>(0);

		if (likeSelectionsObject.has(datasetLabel)) {
			IDataSet dataSet = getDataSetDAO().loadDataSetByLabel(datasetLabel);
			boolean isAnEmptySelection = false;

			JSONObject datasetSelectionObject = likeSelectionsObject.getJSONObject(datasetLabel);
			Iterator<String> it = datasetSelectionObject.keys();
			while (!isAnEmptySelection && it.hasNext()) {
				String columns = it.next();
				String value = datasetSelectionObject.getString(columns);
				if (value == null || value.isEmpty()) {
					isAnEmptySelection = true;
					break;
				}

				List<String> columnsList = getColumnList(columns, dataSet, columnAliasToColumnName);
				List<Projection> projections = new ArrayList<>(columnsList.size());
				for (String columnName : columnsList) {
					projections.add(new Projection(dataSet, columnName));
				}

				for (Projection projection : projections) {
					SimpleFilter filter = new LikeFilter(projection, value, LikeFilter.TYPE.SIMPLE);
					likeFilters.add(filter);
				}
			}

			if (isAnEmptySelection) {
				likeFilters.clear();
				likeFilters.add(new UnsatisfiedFilter());
			}
		}

		return likeFilters;
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

	protected IDataWriter getDataStoreWriter() throws JSONException {
		JSONDataWriter dataWriter = new JSONDataWriter(getDataSetWriterProperties());
		dataWriter.setLocale(buildLocaleFromSession());
		return dataWriter;
	}

	protected void loadColumnAliasToName(JSONArray jsonArray, Map<String, String> columnAliasToName) throws JSONException {
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject category = jsonArray.getJSONObject(i);
			String alias = category.optString("alias");
			if (alias != null && !alias.isEmpty()) {
				String id = category.optString("id");
				if (id != null && !id.isEmpty()) {
					columnAliasToName.put(alias, id);
				}

				String columnName = category.optString("columnName");
				if (columnName != null && !columnName.isEmpty()) {
					columnAliasToName.put(alias, columnName);
				}
			}
		}
	}

	protected String serializeDataSet(IDataSet dataSet, String typeDocWizard) throws JSONException {
		try {
			JSONObject datasetsJSONObject = (JSONObject) SerializerFactory.getSerializer("application/json").serialize(dataSet, null);
			JSONArray datasetsJSONArray = new JSONArray();
			datasetsJSONArray.put(datasetsJSONObject);
			JSONArray datasetsJSONReturn = putActions(getUserProfile(), datasetsJSONArray, typeDocWizard);
			return datasetsJSONReturn.toString();
		} catch (Throwable t) {
			throw new RuntimeException("An unexpected error occured while serializing results", t);
		}
	}

	/**
	 * @param profile
	 * @param datasetsJSONArray
	 * @param typeDocWizard     Usato dalla my analysis per visualizzare solo i dataset su cui è possi bile costruire un certo tipo di analisi selfservice. Al
	 *                          momento filtra la lista dei dataset solo nel caso del GEO in cui vengono eliminati tutti i dataset che non contengono un
	 *                          riferimento alla dimensione spaziale. Ovviamente il fatto che un metodo che si chiama putActions filtri in modo silente la lista
	 *                          dei dataset è una follia che andrebbe rifattorizzata al più presto.
	 * @return
	 * @throws JSONException
	 * @throws EMFInternalError
	 */
	protected JSONArray putActions(IEngUserProfile profile, JSONArray datasetsJSONArray, String typeDocWizard) throws JSONException, EMFInternalError {

		Engine qbeEngine = null;
		try {
			qbeEngine = ExecuteAdHocUtility.getQbeEngine();
		} catch (SpagoBIRuntimeException r) {
			// the qbe engine is not found
			logger.info("Engine not found. ", r);
		}

		Engine geoEngine = null;
		try {
			geoEngine = ExecuteAdHocUtility.getGeoreportEngine();
		} catch (SpagoBIRuntimeException r) {
			// the geo engine is not found
			logger.info("Engine not found. ", r);
		}

		JSONObject detailAction = new JSONObject();
		detailAction.put("name", "detaildataset");
		detailAction.put("description", "Dataset detail");

		JSONObject deleteAction = new JSONObject();
		deleteAction.put("name", "delete");
		deleteAction.put("description", "Delete dataset");

		JSONObject georeportAction = new JSONObject();
		georeportAction.put("name", "georeport");
		georeportAction.put("description", "Show Map");

		JSONObject qbeAction = new JSONObject();
		qbeAction.put("name", "qbe");
		qbeAction.put("description", "Show Qbe");

		JSONArray datasetsJSONReturn = new JSONArray();
		for (int i = 0; i < datasetsJSONArray.length(); i++) {
			JSONObject datasetJSON = datasetsJSONArray.getJSONObject(i);
			JSONArray actions = new JSONArray();

			if (typeDocWizard == null) {
				actions.put(detailAction);
				if (((UserProfile) profile).getUserId().toString().equals(datasetJSON.get("owner"))) {
					// the delete action is able only for private dataset
					actions.put(deleteAction);
				}
			}

			boolean isGeoDataset = false;

			try {
				// String meta = datasetJSON.getString("meta"); // [A]
				// isGeoDataset = ExecuteAdHocUtility.hasGeoHierarchy(meta); //
				// [A]

				String meta = datasetJSON.optString("meta");

				if (meta != null && !meta.equals(""))
					isGeoDataset = ExecuteAdHocUtility.hasGeoHierarchy(meta);

			} catch (Exception e) {
				logger.error("Error during check of Geo spatial column", e);
			}

			if (isGeoDataset && geoEngine != null) {
				actions.put(georeportAction);
			}

			String dsType = datasetJSON.optString(DataSetConstants.DS_TYPE_CD);
			if (dsType == null || !dsType.equals(DataSetFactory.FEDERATED_DS_TYPE)) {
				if (qbeEngine != null && (typeDocWizard == null || typeDocWizard.equalsIgnoreCase("REPORT"))) {
					if (profile.getFunctionalities() != null && profile.getFunctionalities().contains(SpagoBIConstants.BUILD_QBE_QUERIES_FUNCTIONALITY)) {
						actions.put(qbeAction);
					}
				}
			}

			datasetJSON.put("actions", actions);

			if ("GEO".equalsIgnoreCase(typeDocWizard)) {
				// if is caming from myAnalysis - create Geo Document - must
				// shows only ds geospatial --> isGeoDataset == true
				if (geoEngine != null && isGeoDataset) {
					datasetsJSONReturn.put(datasetJSON);
				}
			} else {
				datasetsJSONReturn.put(datasetJSON);
			}

		}
		return datasetsJSONReturn;
	}

	public String getDataSet(String label) {
		logger.debug("IN");
		try {
			IDataSet dataSet = getDatasetManagementAPI().getDataSet(label);
			return serializeDataSet(dataSet, null);
		} catch (Throwable t) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", t);
		} finally {
			logger.debug("OUT");
		}
	}

	public Response deleteDataset(String label) {
		IDataSetDAO datasetDao = DAOFactory.getDataSetDAO();
		IDataSet dataset = getDatasetManagementAPI().getDataSet(label);

		try {
			datasetDao.deleteDataSet(dataset.getId());
		} catch (Exception e) {
			String message = null;
			if (e instanceof DatasetInUseException) {
				DatasetInUseException dui = (DatasetInUseException) e;
				message = dui.getMessage() + "Used by: objects" + dui.getObjectsLabel() + " federations: " + dui.getFederationsLabel();
			} else {
				message = "Error while deleting the specified dataset";
			}

			logger.error("Error while deleting the specified dataset", e);
			throw new SpagoBIRuntimeException(message, e);
		}

		try {
			if (dataset.getDsType().equalsIgnoreCase(DataSetConstants.PREPARED_DATASET)) {
				if (dataset instanceof VersionedDataSet)
					dataset = ((VersionedDataSet) dataset).getWrappedDataset();
				String instanceId = ((PreparedDataSet) dataset).getDataPreparationInstance();
				Client restClient = ClientBuilder.newClient();
				InitialContext context = new InitialContext();
				String serviceUrl = (String) context.lookup("java:comp/env/service_url");
				URL serviceUrlAsURL = new URL(serviceUrl);
				serviceUrlAsURL = new URL(serviceUrlAsURL.getProtocol(), serviceUrlAsURL.getHost(), serviceUrlAsURL.getPort(), "", null);
				String token = getUserProfile().getUserUniqueIdentifier().toString();
				// delete Avro resources
				Response response = restClient.target(serviceUrlAsURL + "/knowage-data-preparation/api/1.0/instance/" + instanceId).request()
						.header("X-Kn-Authorization", token).get();
				JSONObject instance = new JSONObject(response.readEntity(String.class));
				int sourceDsId = instance.getInt("dataSetId");
				deleteAvroFolder(sourceDsId);
				// delete data preparation process instance
				restClient.target(serviceUrlAsURL + "/knowage-data-preparation/api/1.0/instance/" + instanceId).request().header("X-Kn-Authorization", token)
						.delete();
			}
		} catch (Exception e) {
			logger.error("Cannot delete PreparedDataSet related resources (process instance, avro file) for dataset " + label, e);
		}

		return Response.ok().build();
	}

	private void deleteAvroFolder(int dsId) {
		try {
			Path avroExportFolder = Paths.get(SpagoBIUtilities.getResourcePath(), "dataPreparation", (String) getUserProfile().getUserId(),
					Integer.toString(dsId));
			Files.walkFileTree(avroExportFolder, new SimpleFileVisitor<Path>() {

				// delete directories or folders
				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}

				// delete files
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			logger.error("Error while clearing status files", e);
		}
	}

	public Response execute(String label, String body) {
		SDKDataSetParameter[] parameters = null;

		if (request.getParameterMap() != null && request.getParameterMap().size() > 0) {

			parameters = new SDKDataSetParameter[request.getParameterMap().size()];

			int i = 0;
			for (Iterator iterator = request.getParameterMap().keySet().iterator(); iterator.hasNext();) {
				String key = (String) iterator.next();
				String[] values = request.getParameterMap().get(key);
				SDKDataSetParameter sdkDataSetParameter = new SDKDataSetParameter();
				sdkDataSetParameter.setName(key);
				sdkDataSetParameter.setValues(values);
				parameters[i] = sdkDataSetParameter;
				i++;
			}
		}
		return Response.ok(executeDataSet(label, parameters)).build();
	}

	protected String executeDataSet(String label, SDKDataSetParameter[] params) {
		logger.debug("IN: label in input = " + label);

		try {
			if (label == null) {
				logger.warn("DataSet identifier in input is null!");
				return null;
			}
			IDataSet dataSet = DAOFactory.getDataSetDAO().loadDataSetByLabel(label);
			if (dataSet == null) {
				logger.warn("DataSet with label [" + label + "] not existing.");
				return null;
			}
			if (params != null && params.length > 0) {
				HashMap parametersFilled = new HashMap();
				for (int i = 0; i < params.length; i++) {
					SDKDataSetParameter par = params[i];
					parametersFilled.put(par.getName(), par.getValues()[0]);
					logger.debug("Add parameter: " + par.getName() + "/" + par.getValues()[0]);
				}
				dataSet.setParamsMap(parametersFilled);
			}

			// add the jar retriver in case of a Qbe DataSet
			if (dataSet instanceof QbeDataSet
					|| (dataSet instanceof VersionedDataSet && ((VersionedDataSet) dataSet).getWrappedDataset() instanceof QbeDataSet)) {
				SpagoBICoreDatamartRetriever retriever = new SpagoBICoreDatamartRetriever();
				Map parameters = dataSet.getParamsMap();
				if (parameters == null) {
					parameters = new HashMap();
					dataSet.setParamsMap(parameters);
				}
				dataSet.getParamsMap().put(SpagoBIConstants.DATAMART_RETRIEVER, retriever);
			}
			// get user profile's attributes
			UserProfile userProfile = UserProfileManager.getProfile();
			if (userProfile != null) {
				Map attributes = userProfile.getUserAttributes();
				dataSet.setUserProfileAttributes(attributes);
			}
			dataSet.loadData();

			JSONDataWriter writer = new JSONDataWriter();
			return (writer.write(dataSet.getDataStore())).toString();
		} catch (Exception e) {
			logger.error("Error while executing dataset", e);
			throw new SpagoBIRuntimeException("Error while executing dataset", e);
		}
	}

	protected IDataSetDAO getDataSetDAO() {
		IDataSetDAO dsDAO = DAOFactory.getDataSetDAO();
		return dsDAO;
	}

	protected final boolean isNearRealtimeSupported(IDataSet dataSet) throws DataBaseException {
		boolean isNearRealtimeSupported = false;
		dataSet = dataSet instanceof VersionedDataSet ? ((VersionedDataSet) dataSet).getWrappedDataset() : dataSet;
		if (dataSet instanceof AbstractJDBCDataset) {
			IDataBase database = DataBaseFactory.getDataBase(dataSet.getDataSource());
			isNearRealtimeSupported = database.getDatabaseDialect().isInLineViewSupported() && !dataSet.hasDataStoreTransformer();
		} else if (dataSet instanceof FederatedDataSet) {
			isNearRealtimeSupported = false;
		} else if (dataSet instanceof QbeDataSet) {
			IDataBase database = DataBaseFactory.getDataBase(dataSet.getDataSource());
			isNearRealtimeSupported = database.getDatabaseDialect().isInLineViewSupported() && !dataSet.hasDataStoreTransformer();
		} else if (dataSet instanceof FlatDataSet || dataSet.isPersisted() || dataSet instanceof PreparedDataSet
				|| dataSet.getClass().equals(SolrDataSet.class)) {
			isNearRealtimeSupported = true;
		}
		return isNearRealtimeSupported;
	}

}
