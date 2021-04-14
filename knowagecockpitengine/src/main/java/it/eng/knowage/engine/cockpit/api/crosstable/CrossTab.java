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

package it.eng.knowage.engine.cockpit.api.crosstable;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import it.eng.knowage.engine.cockpit.api.crosstable.CrosstabDefinition.Column;
import it.eng.knowage.engine.cockpit.api.crosstable.CrosstabDefinition.Row;
import it.eng.knowage.engine.cockpit.api.crosstable.placeholder.AggregatorDelegate;
import it.eng.knowage.engine.cockpit.api.crosstable.placeholder.AverageAggregator;
import it.eng.knowage.engine.cockpit.api.crosstable.placeholder.CountAggregator;
import it.eng.knowage.engine.cockpit.api.crosstable.placeholder.JsonPathAggregatorPlaceholder;
import it.eng.knowage.engine.cockpit.api.crosstable.placeholder.MaxAggregator;
import it.eng.knowage.engine.cockpit.api.crosstable.placeholder.MinAggregator;
import it.eng.knowage.engine.cockpit.api.crosstable.placeholder.NotAvailablePlaceholder;
import it.eng.knowage.engine.cockpit.api.crosstable.placeholder.NotDefinedAggregator;
import it.eng.knowage.engine.cockpit.api.crosstable.placeholder.Placeholder;
import it.eng.knowage.engine.cockpit.api.crosstable.placeholder.StringPlaceholder;
import it.eng.knowage.engine.cockpit.api.crosstable.placeholder.SumAggregator;
import it.eng.knowage.engine.cockpit.api.crosstable.placeholder.ValuePlaceholder;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.metadata.FieldMetadata;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.groovy.GroovySandbox;

/**
 *
 * @author Alberto Alagna
 *
 *         This Class encapsulates the crossTab The publics methods are: - CrossTab(IDataStore dataStore, CrosstabDefinition crosstabDefinition) that builds the
 *         crossTab (headers structure and data) - getJSONCrossTab() that returns the JSON representation of the crosstab
 */

public class CrossTab {

	public static final String CROSSTAB_NODE_JSON_KEY = "node_key";
	public static final String CROSSTAB_NODE_JSON_CHILDS = "node_childs";
	public static final String CROSSTAB_JSON_ROWS_HEADERS = "rows";
	public static final String CROSSTAB_JSON_ROWS_HEADERS_DESCRIPTION = "rows_description";
	public static final String CROSSTAB_JSON_COLUMNS_HEADERS = "columns";
	public static final String CROSSTAB_JSON_DATA = "data";
	public static final String CROSSTAB_JSON_CONFIG = "config";
	public static final String CROSSTAB_JSON_MEASURES_METADATA = "measures_metadata";
	public static final String CROSSTAB_JSON_ROWS_HEADER_TITLE = "rowHeadersTitle";
	public static final String CROSSTAB_CELLTYPEOFCOLUMNS = "celltypeOfColumns";
	public static final String CROSSTAB_CELLTYPEOFROWS = "celltypeOfRows";

	public static final String CROSSTAB_JSON_VALUE_DESCRIPTION_MAP = "valueDescriptionMap";
	public static final String MEASURE_NAME = "name";
	public static final String MEASURE_TYPE = "type";
	public static final String MEASURE_FORMAT = "format";
	public static final String MEASURE_POSITION = "measurePosition";
	public static final String TOTAL = "Total";
	public static final String SUBTOTAL = "SubTotal";

	public static final String PATH_SEPARATOR = "_S_";

	/**
	 *
	 */
	@Deprecated
	private static final String DATA_MATRIX_NA = "";
	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy");
	private static final SimpleDateFormat TIMESTAMP_FORMATTER = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

	private static Logger logger = Logger.getLogger(CrossTab.class);

	private Node columnsRoot;
	private Node rowsRoot;

	/**
	 * Relates something like "column_1" into datastore to "Field A" in the crosstab.
	 *
	 * @deprecated Alias can be duplicated
	 */
	@Deprecated
	private Map<String, String> dsColumnName2Alias = new HashMap<String, String>();
	/**
	 * Relates something like "Field A" in the crosstab to "column_1" into datastore.
	 *
	 * @deprecated Alias can be duplicated
	 */
	@Deprecated
	private Map<String, String> alias2DsColumnName = new HashMap<String, String>();
	/**
	 * Store the metadata related to the column name.
	 */
	private Map<String, JSONObject> dsColumnName2Metadata = new HashMap<String, JSONObject>();
	/**
	 * Store the metadata related to the column alias.
	 */
	private Map<String, JSONObject> alias2Metadata = new HashMap<String, JSONObject>();

	private static final Placeholder NOT_AVAILABLE_PLACEHOLDER = new NotAvailablePlaceholder();

	private List<Node> treeLeaves = null;

	private String createJsonPathQueryFromNodes(Node currRow, Node currCol, MeasureInfo measure) {
		Map<String, String> colsValues = new TreeMap<String, String>();
		String measureColName = alias2DsColumnName.get(measure.name);

		if (currRow.isMeasure()) {
			measureColName = currRow.getColumnName();
		} else if (currCol.isMeasure()) {
			measureColName = currCol.getColumnName();
		}

		// Get columns values from row
		extractColumnsValuesFromNode(colsValues, currRow);

		// Get columns values from row
		extractColumnsValuesFromNode(colsValues, currCol);

		// Create json path
		StringBuilder ret = new StringBuilder("$");

		if (!colsValues.isEmpty()) {
			ret.append("[?(");
			for (Iterator<Entry<String, String>> iterator = colsValues.entrySet().iterator(); iterator.hasNext();) {
				Entry<String, String> entry = iterator.next();

				String value = entry.getValue();
				value = StringEscapeUtils.escapeJavaScript(value);

				ret.append("@.").append(entry.getKey()).append(" == \"").append(value).append("\"");

				if (iterator.hasNext()) {
					ret.append(" && ");
				}

			}

			ret.append(")]").append(".");
		} else {
			ret.append(".*.");
		}

		ret.append("[").append("'").append(measureColName).append("'");

		// If there is a count...
		if (!countMeasures.isEmpty()) {
			// ... select the relative column
			ret.append(",").append("'").append(countMeasures.get(0)).append("'");
		}

		ret.append("]");

		String string = ret.toString();
		return string;
	}

	private void extractColumnsValuesFromNode(Map<String, String> colsValues, Node currNode) {
		Node currRowParent = currNode;
		while (currRowParent != null) {
			String columnName = currRowParent.getColumnName();
			/*
			 * CurrNode is set to have ordering on a different column
			 */
			if (dsColumnNameSortingReferenceBiMap.getKey(columnName) != null) {
				columnName = (String) dsColumnNameSortingReferenceBiMap.getKey(columnName);
			}
			if (!currRowParent.isMeasure() && columnName != null) {
				String value = currRowParent.getValue();
				colsValues.put(columnName, value);
			}
			currRowParent = currRowParent.getParentNode();
		}
	}

	/**
	 * @param measure Measure used to determine the correct delegate
	 * @return An aggregator delegate instance
	 */
	private AggregatorDelegate getAggregatorDelegate(final MeasureInfo measure) {
		AggregatorDelegate ret = null;
		String aggregationFunction = measure.aggregationFunction;

		switch (aggregationFunction) {
		case "SUM":
			ret = SumAggregator.instance();
			break;
		case "AVG":
			ret = AverageAggregator.instance();
			break;
		case "MAX":
			ret = MaxAggregator.instance();
			break;
		case "MIN":
			ret = MinAggregator.instance();
			break;
		case "COUNT":
			ret = CountAggregator.instance();
			break;
		default:
			ret = NotDefinedAggregator.instance();
			break;
		}

		return ret;
	}

	private DocumentContext parsedValuesDataStore = null;

	Placeholder[][] dataMatrix;
	private JSONObject config;
	private final List<MeasureInfo> measures = new ArrayList<CrossTab.MeasureInfo>();
	private final List<MeasureInfo> measuresToShowOnTotalsOrSubTotals = new ArrayList<CrossTab.MeasureInfo>();
	private final List<String> countMeasures = new ArrayList<String>();
	private CrosstabDefinition crosstabDefinition;
	private List<String> rowHeadersTitles;
	private List<String[]> rowsSum; // sum calculate for the rows (summ the row
									// 1.. )
	private List<String[]> columnsSum; // sum calculate for the rows (summ the
										// row 1.. )
	private boolean measuresOnRow;
	private boolean hideZeroRows;
	private boolean fixedColumn;
	private boolean expandCollapseRows;

	// The client has a global variable
	// (Sbi.cockpit.widgets.crosstab.globalConfigs) with the list of the active
	// crosstab. This variable contains the id of the actual crosstab in the
	// list
	private Integer myGlobalId;

	// used to sort the rows/columns
	private Map<Integer, NodeComparator> columnsSortKeysMap;
	private Map<Integer, NodeComparator> rowsSortKeysMap;
	private Map<Integer, NodeComparator> measuresSortKeysMap;

	private List<String> rowCordinates;
	private List<String> columnCordinates;
	private List<String> measuresCordinates;

	private List<String> columnsSpecification = new ArrayList<String>();
	private List<String> rowsSpecification = new ArrayList<String>();

	private List<String> columnsHeaderList = new ArrayList<String>();
	private List<String> rowsHeaderList = new ArrayList<String>();
	private List<String> measuresHeaderList = new ArrayList<String>();
	private List<String> columnsHeaderIdList = new ArrayList<String>();
	private List<String> rowsHeaderIdList = new ArrayList<String>();
	private List<String> measuresHeaderIdList = new ArrayList<String>();
	/**
	 * @deprecated This list has been overridden by {@link #dsAliasSortingReferenceBiMap} and {@link #dsColumnNameSortingReferenceBiMap}
	 */
	@Deprecated
	private List<String> orderingHeaderList = new ArrayList<String>();
	/**
	 * Contains mapping between current column NAME and external referenced column NAME for sorting
	 */
	private BidiMap dsColumnNameSortingReferenceBiMap = new DualHashBidiMap();

	public enum CellType {
		DATA("data"), CF("cf"), SUBTOTAL("partialsum"), TOTAL("totals");

		CellType(String value) {
			this.value = value;
		}

		private String value;

		public String getValue() {
			return this.value;
		}
	}

	private List<CellType> celltypeOfColumns;
	private List<CellType> celltypeOfRows;

	public CrossTab() {
	}

	/**
	 * Builds the crossTab (headers structure and data)
	 *
	 * @param dataStore          : the source of the data
	 * @param crosstabDefinition : the definition of the crossTab
	 * @param fieldOptions       : fieldOptions
	 * @param calculateFields    : array of JSONObjects the CF
	 */
	public CrossTab(IDataStore dataStore, CrosstabDefinition crosstabDefinition, JSONArray calculateFields, Map<Integer, NodeComparator> columnsSortKeysMap,
			Map<Integer, NodeComparator> rowsSortKeysMap, Map<Integer, NodeComparator> measuresSortKeysMap, Integer myGlobalId) throws JSONException {
		this(dataStore, crosstabDefinition, columnsSortKeysMap, rowsSortKeysMap, measuresSortKeysMap);

		init(crosstabDefinition, calculateFields, columnsSortKeysMap, rowsSortKeysMap, measuresSortKeysMap, myGlobalId);
	}

	/**
	 * Builds the crossTab (headers structure and data)
	 *
	 * @param dataStore          : the source of the data
	 * @param crosstabDefinition : the definition of the crossTab
	 * @param fieldOptions       : fieldOptions
	 * @param calculateFields    : array of JSONObjects the CF
	 */
	public CrossTab(JSONArray datastore, JSONObject datastoreMetadata, CrosstabDefinition crosstabDefinition, JSONArray calculateFields,
			Map<Integer, NodeComparator> columnsSortKeysMap, Map<Integer, NodeComparator> rowsSortKeysMap, Map<Integer, NodeComparator> measuresSortKeysMap,
			Integer myGlobalId) throws JSONException {
		this(datastore, datastoreMetadata, crosstabDefinition, columnsSortKeysMap, rowsSortKeysMap, measuresSortKeysMap);

		init(crosstabDefinition, calculateFields, columnsSortKeysMap, rowsSortKeysMap, measuresSortKeysMap, myGlobalId);
	}

	private void init(CrosstabDefinition crosstabDefinition, JSONArray calculateFields, Map<Integer, NodeComparator> columnsSortKeysMap,
			Map<Integer, NodeComparator> rowsSortKeysMap, Map<Integer, NodeComparator> measuresSortKeysMap, Integer myGlobalId) throws JSONException {

		this.myGlobalId = myGlobalId;

		rowsSum = getTotalsOnRows(measuresOnRow);
		columnsSum = getTotalsOnColumns(measuresOnRow);

		if (calculateFields != null) {
			for (int i = 0; i < calculateFields.length(); i++) {

				JSONObject cf = calculateFields.getJSONObject(i);
				boolean horizontal = cf.getBoolean("horizontal");
				calculateCF(cf.getString("operation"), horizontal, cf.getInt("level"), cf.getString("name"), CellType.CF);
			}
		}

		addSubtotals();

		addTotals();

		List<CrosstabDefinition.Column> columns = crosstabDefinition.getColumns();
		addHeaderTitles(columns, 0, columnsRoot);
	}

	/**
	 * Builds the crossTab (headers structure and data)
	 *
	 * @param dataStore          : the source of the data
	 * @param crosstabDefinition : the definition of the crossTab
	 */
	public CrossTab(JSONArray dataStoredata, JSONObject datastoreMetadata, CrosstabDefinition crosstabDefinition,
			Map<Integer, NodeComparator> columnsSortKeysMap, Map<Integer, NodeComparator> rowsSortKeysMap, Map<Integer, NodeComparator> measuresSortKeysMap)
			throws JSONException {

		JSONObject valueRecord;
		this.config = crosstabDefinition.getConfig();
		this.crosstabDefinition = crosstabDefinition;
		int cellLimit = crosstabDefinition.getCellLimit();
		boolean columnsOverflow = false; // true if the number of cell shown in
											// the crosstab is less than the
											// total number of cells
		boolean measuresOnColumns = crosstabDefinition.isMeasuresOnColumns();
		measuresOnRow = config.getString("measureson").equals("rows");
		hideZeroRows = config.optBoolean("hideZeroRows");
		fixedColumn = config.optBoolean("fixedColumn");
		expandCollapseRows = config.optBoolean("expandCollapseRows");
		List<Row> rows = crosstabDefinition.getRows();
		int rowsCount = rows.size();
		List<Column> columns = crosstabDefinition.getColumns();
		int columnsCount = columns.size();
		int measuresCount = crosstabDefinition.getMeasures().size();
		int index;

		this.columnsSortKeysMap = columnsSortKeysMap;
		this.rowsSortKeysMap = rowsSortKeysMap;
		this.measuresSortKeysMap = measuresSortKeysMap;

		rowCordinates = new LinkedList<String>();
		columnCordinates = new LinkedList<String>();
		measuresCordinates = new LinkedList<String>();

		List<String> data = new ArrayList<String>();

		columnsRoot = new Node(Node.CROSSTAB_NODE_COLUMN_ROOT);
		rowsRoot = new Node(Node.CROSSTAB_NODE_ROW_ROOT);

		JSONArray dataStoreMetadataFields = datastoreMetadata.getJSONArray("fields");

		List<String> columnsNameList = new ArrayList<String>();
		List<String> rowsNameList = new ArrayList<String>();
		List<String> measuresNameList = new ArrayList<String>();
		List<String> orderingNameList = new ArrayList<String>(); // list with external sorting columns (just associated column)

		for (int i = 0; i < crosstabDefinition.getMeasures().size(); i++) {
			measuresHeaderList.add(crosstabDefinition.getMeasures().get(i).getAlias());
			measuresHeaderIdList.add(crosstabDefinition.getMeasures().get(i).getEntityId());
		}

		initReferencesMaps(dataStoreMetadataFields);

		for (int i = 0; i < columns.size(); i++) {
			Column column = columns.get(i);
			columnsHeaderList.add(column.getAlias());
			columnsHeaderIdList.add(column.getEntityId());
			if (column.getSortingId() != null && !column.getSortingId().equals("")) {
				orderingHeaderList.add(column.getSortingId() + "|" + column.getAlias());
				// dsAliasSortingReferenceBiMap.put(column.getSortingId(), column.getAlias());
				dsColumnNameSortingReferenceBiMap.put(alias2DsColumnName.get(column.getSortingId()), alias2DsColumnName.get(column.getAlias()));
			}
		}

		for (int i = 0; i < rows.size(); i++) {
			Row row = rows.get(i);
			rowsHeaderList.add(row.getAlias());
			rowsHeaderIdList.add(row.getEntityId());
			if (row.getSortingId() != null && !row.getSortingId().equals("")) {
				orderingHeaderList.add(row.getSortingId() + "|" + row.getAlias());
				// dsAliasSortingReferenceBiMap.put(row.getSortingId(), row.getAlias());
				dsColumnNameSortingReferenceBiMap.put(alias2DsColumnName.get(row.getSortingId()), alias2DsColumnName.get(row.getAlias()));
			}
		}

		for (int i = 0; i < dataStoreMetadataFields.length(); i++) {
			if (dataStoreMetadataFields.get(i) instanceof String) {
				continue;
			}
			JSONObject jsonObject = dataStoreMetadataFields.getJSONObject(i);
			String name = jsonObject.getString("name");
			String header = jsonObject.getString("header");

			if (columnsHeaderList.contains(header) || columnsHeaderIdList.contains(header)) {
				columnsNameList.add(addNumberToColumnName(name));
			} else if (rowsHeaderList.contains(header) || rowsHeaderIdList.contains(header)) {
				rowsNameList.add(addNumberToColumnName(name));
			} else if (measuresHeaderList.contains(header) || measuresHeaderIdList.contains(header)) {
				measuresNameList.add(addNumberToColumnName(name));
			}
			if ("___COUNT".equals(header)) {
				countMeasures.add(name);
			}
		}

		// check association for external field sorting
		if (orderingHeaderList != null && orderingHeaderList.size() > 0) {
			String orderingHeadersNames = "";
			for (int i = 0; i < orderingHeaderList.size(); i++) {
				String orderingHeader = orderingHeaderList.get(i);
				String[] orderingHeaderEl = orderingHeader.split("\\|");
				if (orderingHeaderEl.length == 2) {
					orderingHeadersNames = getMetaColumnName(dataStoreMetadataFields, orderingHeaderEl[0]) + "|"
							+ getMetaColumnName(dataStoreMetadataFields, orderingHeaderEl[1]);
					orderingNameList.add(orderingHeadersNames);
				}
			}
		}

		int cellCount = 0;
		int actualRows = 0;
		int actualColumns = 0;
		// counts the cell number
		for (index = 0; index < dataStoredata.length() && (cellLimit <= 0 || cellCount < cellLimit); index++) {
			valueRecord = dataStoredata.getJSONObject(index);

			boolean columnInserted = addRecord(columnsRoot, valueRecord, columnsNameList, orderingNameList);
			boolean rowInserted = addRecord(rowsRoot, valueRecord, rowsNameList, orderingNameList);
			actualRows += rowInserted ? 1 : 0;
			actualColumns += columnInserted ? 1 : 0;
			cellCount = actualRows * actualColumns * measuresCount;
		}

//		columnsRoot.updateFathers();
//		rowsRoot.updateFathers();

		if (index < dataStoredata.length()) {
			logger.debug("Crosstab cells number limit exceeded");
			Node completeColumnsRoot = new Node("rootCompleteC");
			for (index = 0; index < dataStoredata.length(); index++) {
				valueRecord = dataStoredata.getJSONObject(index);

				addRecord(completeColumnsRoot, valueRecord, columnsNameList, orderingNameList);
			}
			columnsOverflow = columnsRoot.getLeafsNumber() < completeColumnsRoot.getLeafsNumber();
		}
		Map<String, Double> measureToOrderMap = new LinkedHashMap<String, Double>();

		for (index = 0; index < dataStoredata.length(); index++) {
			valueRecord = dataStoredata.getJSONObject(index);

			String rowPath;
			String columnPath;
			String enrichedColumnPath;
			List<String> rowPathList = new ArrayList<String>();
			List<String> colPathList = new ArrayList<String>();

			columnPath = "";
			enrichedColumnPath = "";
			for (int i = 0; i < columnsCount; i++) {
				String column = columnsNameList.get(i);
				Object value = valueRecord.get(column);
				String valueStr = null;
				// String.valueOf() instead of toString() is used also to prevent NullPointerException
				if (StringUtils.isEmpty(String.valueOf(value))) {
					valueStr = "null";
				} else {
					valueStr = value.toString();
				}
				enrichedColumnPath = enrichedColumnPath + PATH_SEPARATOR + dsColumnName2Alias.get(column) + PATH_SEPARATOR + valueStr;
				columnPath = columnPath + PATH_SEPARATOR + valueStr;
				colPathList.add(valueStr);
			}

			rowPath = "";
			for (int i = 0; i < rowsCount; i++) {
				String row = rowsNameList.get(i);
				Object value = valueRecord.get(row);
				String valueStr = null;
				// String.valueOf() instead of toString() is used also to prevent NullPointerException
				if (StringUtils.isEmpty(String.valueOf(value))) {
					valueStr = "null";
				} else {
					valueStr = value.toString();
				}
				rowPath = rowPath + PATH_SEPARATOR + valueStr;
				rowPathList.add(valueStr);
			}
			// defines array of data in according to coordinate:
			for (int i = 0; i < measuresNameList.size(); i++) {
				// apply logic for measure ordering if it's required
				if (measuresSortKeysMap.size() > 0) {
					for (Map.Entry<Integer, NodeComparator> entry : measuresSortKeysMap.entrySet()) {
						NodeComparator nodeComparator = entry.getValue();
						String measureLabel = nodeComparator.getMeasureLabel();
						int direction = nodeComparator.getDirection();
						Integer idx = getColumnIndex(measureLabel, measuresHeaderList);
						if (idx >= 0) {
							String colName = measuresNameList.get(i);
							Double value = (valueRecord.get(colName).equals("")) ? 0 : Double.valueOf(String.valueOf(valueRecord.get(colName)));
							String[] columnPathArray = enrichedColumnPath.trim().split(PATH_SEPARATOR);
							String[] entryParents = measuresSortKeysMap.get(entry.getKey()).getParentValue().split(PATH_SEPARATOR);
							// add value to order only if parents are correct (useful for deep levels)
							String valueLbl = rowPath + columnPath + PATH_SEPARATOR + colName;
							/*
							 * WARNING : in fact the following causes some sort of filtering when a record doesn't match the measure a user sorts for. The
							 * else-branch adds a dummy value to records that don't match to let them appear on the crosstable.
							 */
							if (columnPathArray != null && entryParents != null && Arrays.deepEquals(columnPathArray, entryParents)) {
								measureToOrderMap.put(valueLbl, value);
							} else {
								double dummyValue = direction == -1 ? Double.MIN_VALUE : Double.MAX_VALUE;
								measureToOrderMap.put(valueLbl, dummyValue);
							}
						}
					}
				}
				String measure = measuresNameList.get(i);
				columnCordinates.add(columnPath);
				rowCordinates.add(rowPath);
				measuresCordinates.add(rowPath + columnPath + PATH_SEPARATOR + getStringValue(valueRecord.get(measure)));
				data.add("" + getStringValue(valueRecord.get(measure)));
			}
		}

		// measure sort has priority on columns and rows
		if (measuresSortKeysMap.size() > 0) {
			Node orderedColumnsRoot = new Node(Node.CROSSTAB_NODE_COLUMN_ROOT);
			Node orderedRowsRoot = new Node(Node.CROSSTAB_NODE_ROW_ROOT);

			Map<String, Double> orderedMeasures = sortMeasures(measuresSortKeysMap, measureToOrderMap, rowsCount, columnsCount, actualRows, actualColumns);
			for (String key : orderedMeasures.keySet()) {

				int nodePosition;
				Object[] measureInfo = key.split(PATH_SEPARATOR);
				if (measuresOnColumns) {
					Node nodeToCheck = orderedRowsRoot;
					// update paths order and coordinates for rows
					for (int r = 0; r < rowsCount; r++) {
						// Node constructor needs columnName, value, description
						// we use rowsNameList to retrieve columnName and measureInfo to retrieve value and description
						Node node = new Node(rowsNameList.get(r), measureInfo[r + 1].toString(), measureInfo[r + 1].toString());
						nodePosition = nodeToCheck.getChildren().indexOf(node);
						if (nodePosition < 0) {
							nodeToCheck.addChild(node);
							nodeToCheck = node;
						} else {
							nodeToCheck = nodeToCheck.getChildren().get(nodePosition);
						}
					}

				} else {
					// update paths order and coordinates for columns
					Node n = new Node(measureInfo[2].toString(), measureInfo[2].toString());
					orderedColumnsRoot.addChild(n);
				}
			}
			if (orderedRowsRoot.getChildren().size() > 0)
				rowsRoot = orderedRowsRoot;
			else
				rowsRoot.orderedSubtree(rowsSortKeysMap);

			if (orderedColumnsRoot.getChildren().size() > 0)
				columnsRoot = orderedColumnsRoot;
			else
				columnsRoot.orderedSubtree(columnsSortKeysMap);
		} else {
			columnsRoot.orderedSubtree(columnsSortKeysMap);
			rowsRoot.orderedSubtree(rowsSortKeysMap);
		}

		columnsSpecification = getLeafsPathList(columnsRoot);
		rowsSpecification = getLeafsPathList(rowsRoot);

		if (measuresOnColumns) {
			addMeasuresToTree(columnsRoot, crosstabDefinition.getMeasures());
		} else {
			addMeasuresToTree(rowsRoot, crosstabDefinition.getMeasures());
		}
		config.put("columnsOverflow", columnsOverflow);

		// put measures' info into measures variable
		for (int i = 0; i < crosstabDefinition.getMeasures().size(); i++) {
			// the field number i contains the measure number (i - <number of
			// dimensions>)
			// but <number of dimension> is <total fields count> - <total
			// measures count>
			IFieldMetaData fieldMeta = new FieldMetadata();
			Measure relevantMeasure = crosstabDefinition.getMeasures().get(i);
			MeasureInfo measureInfo = getMeasureInfo(fieldMeta, relevantMeasure);
			measures.add(measureInfo);
			if (!measureInfo.excludeFromTotalAndSubtotal) {
				measuresToShowOnTotalsOrSubTotals.add(measureInfo);
			}
		}

		/**
		 * !!! WARNING !!! WARNING !!! WARNING !!! WARNING !!! WARNING !!! If you think that:
		 *
		 * dataStoredata.toString()
		 *
		 * Is useless think at the poor guy who lost 4 hours to understand that jsonpath works better with String than JSONObject/JSONArray.
		 *
		 * !!! WARNING !!! WARNING !!! WARNING !!! WARNING !!! WARNING !!!
		 */
		parsedValuesDataStore = JsonPath.parse(dataStoredata.toString());

		dataMatrix = getDataMatrix(columnsSpecification, rowsSpecification, columnCordinates, rowCordinates, measuresCordinates, data, measuresOnColumns,
				measuresCount, columnsRoot.getLeafsNumber());

		celltypeOfColumns = new ArrayList<CrossTab.CellType>();
		celltypeOfRows = new ArrayList<CrossTab.CellType>();

		for (int i = 0; i < dataMatrix.length; i++) {
			celltypeOfRows.add(CellType.DATA);
		}

		for (int i = 0; i < dataMatrix[0].length; i++) {
			celltypeOfColumns.add(CellType.DATA);
		}
	}

	private void initReferencesMaps(JSONArray dataStoreMetadataFields) throws JSONException {
		for (int i = 0; i < dataStoreMetadataFields.length(); i++) {
			if (dataStoreMetadataFields.get(i) instanceof String) {
				continue;
			}
			JSONObject jsonObject = dataStoreMetadataFields.getJSONObject(i);
			String name = jsonObject.getString("name");
			String header = jsonObject.getString("header");

			dsColumnName2Alias.put(name, header);
			alias2DsColumnName.put(header, name);
			dsColumnName2Metadata.put(name, jsonObject);
			alias2Metadata.put(header, jsonObject);
		}
	}

	public String addNumberToColumnName(String columnName) {
		return columnName;
	}

	/**
	 * Builds the crossTab (headers structure and data)
	 *
	 * @param dataStore          : the source of the data
	 * @param crosstabDefinition : the definition of the crossTab
	 */
	public CrossTab(IDataStore valuesDataStore, CrosstabDefinition crosstabDefinition, Map<Integer, NodeComparator> columnsSortKeysMap,
			Map<Integer, NodeComparator> rowsSortKeysMap, Map<Integer, NodeComparator> measuresSortKeysMap) throws JSONException {
		IRecord valueRecord;
		String rowPath;
		String columnPath;
		this.config = crosstabDefinition.getConfig();
		this.crosstabDefinition = crosstabDefinition;
		int cellLimit = crosstabDefinition.getCellLimit();
		boolean columnsOverflow = false; // true if the number of cell shown in
											// the crosstab is less than the
											// total number of cells
		boolean measuresOnColumns = crosstabDefinition.isMeasuresOnColumns();
		measuresOnRow = config.getString("measureson").equals("rows");
		int rowsCount = crosstabDefinition.getRows().size();
		int columnsCount = crosstabDefinition.getColumns().size();
		int measuresCount = crosstabDefinition.getMeasures().size();
		int index;

		this.columnsSortKeysMap = columnsSortKeysMap;
		this.rowsSortKeysMap = rowsSortKeysMap;
		this.measuresSortKeysMap = measuresSortKeysMap;

		rowCordinates = new LinkedList<String>();
		columnCordinates = new LinkedList<String>();
		List<String> data = new LinkedList<String>();

		columnsRoot = new Node(Node.CROSSTAB_NODE_COLUMN_ROOT);
		rowsRoot = new Node(Node.CROSSTAB_NODE_ROW_ROOT);

		int cellCount = 0;
		int actualRows = 0;
		int actualColumns = 0;
		for (index = 0; index < valuesDataStore.getRecordsCount() && (cellLimit <= 0 || cellCount < cellLimit); index++) {
			valueRecord = valuesDataStore.getRecordAt(index);

			boolean columnInserted = addRecord(columnsRoot, valueRecord, 0, columnsCount);
			boolean rowInserted = addRecord(rowsRoot, valueRecord, columnsCount, columnsCount + rowsCount);
			actualRows += rowInserted ? 1 : 0;
			actualColumns += columnInserted ? 1 : 0;
			cellCount = actualRows * actualColumns * measuresCount;
		}

//		columnsRoot.updateFathers();
//		rowsRoot.updateFathers();

		columnsRoot.orderedSubtree(columnsSortKeysMap);
		rowsRoot.orderedSubtree(rowsSortKeysMap);

		if (index < valuesDataStore.getRecordsCount()) {
			logger.debug("Crosstab cells number limit exceeded");
			Node completeColumnsRoot = new Node("rootCompleteC");
			for (index = 0; index < valuesDataStore.getRecordsCount(); index++) {
				valueRecord = valuesDataStore.getRecordAt(index);

				addRecord(completeColumnsRoot, valueRecord, 0, columnsCount);
			}
			columnsOverflow = columnsRoot.getLeafsNumber() < completeColumnsRoot.getLeafsNumber();
		}

		for (index = 0; index < valuesDataStore.getRecordsCount(); index++) {
			valueRecord = valuesDataStore.getRecordAt(index);
			List<IField> fields = valueRecord.getFields();
			columnPath = "";
			for (int i = 0; i < columnsCount; i++) {
				Object value = fields.get(i).getValue();
				String valueStr = null;
				if (value == null) {
					valueStr = "null";
				} else {
					valueStr = value.toString();
				}
				columnPath = columnPath + PATH_SEPARATOR + valueStr;
			}

			rowPath = "";
			for (int i = columnsCount; i < valueRecord.getFields().size() - measuresCount; i++) {
				Object value = fields.get(i).getValue();
				String valueStr = null;
				if (value == null) {
					valueStr = "null";
				} else {
					valueStr = value.toString();
				}
				rowPath = rowPath + PATH_SEPARATOR + valueStr.toString();
			}

			for (int i = valueRecord.getFields().size() - measuresCount; i < valueRecord.getFields().size(); i++) {
				columnCordinates.add(columnPath);
				rowCordinates.add(rowPath);
				data.add("" + getStringValue(fields.get(i).getValue()));
			}
		}

		List<String> columnsSpecification = getLeafsPathList(columnsRoot);
		List<String> rowsSpecification = getLeafsPathList(rowsRoot);

		if (measuresOnColumns) {
			addMeasuresToTree(columnsRoot, crosstabDefinition.getMeasures());
		} else {
			addMeasuresToTree(rowsRoot, crosstabDefinition.getMeasures());
		}
		config.put("columnsOverflow", columnsOverflow);

		// put measures' info into measures variable
		IMetaData meta = valuesDataStore.getMetaData();
		for (int i = meta.getFieldCount() - measuresCount; i < meta.getFieldCount(); i++) {
			// the field number i contains the measure number (i - <number of
			// dimensions>)
			// but <number of dimension> is <total fields count> - <total
			// measures count>
			IFieldMetaData fieldMeta = meta.getFieldMeta(i);
			Measure relevantMeasure = crosstabDefinition.getMeasures().get(i - (meta.getFieldCount() - measuresCount));
			MeasureInfo measureInfo = getMeasureInfo(fieldMeta, relevantMeasure);
			measures.add(measureInfo);
			String aggregationFunction = measureInfo.getAggregationFunction();
			if ("COUNT".equals(aggregationFunction)) {
				String column = alias2DsColumnName.get(measureInfo.getName());
				countMeasures.add(column);
			}
			if (!measureInfo.excludeFromTotalAndSubtotal) {
				measuresToShowOnTotalsOrSubTotals.add(measureInfo);
			}
		}

		/**
		 * !!! WARNING !!! WARNING !!! WARNING !!! WARNING !!! WARNING !!! If you think that:
		 *
		 * dataStoredata.toString()
		 *
		 * Is useless think at the poor guy who lost 4 hours to understand that jsonpath works better with String than JSONObject/JSONArray.
		 *
		 * !!! WARNING !!! WARNING !!! WARNING !!! WARNING !!! WARNING !!!
		 */
		parsedValuesDataStore = JsonPath.parse(valuesDataStore.toString());

		dataMatrix = getDataMatrix(columnsSpecification, rowsSpecification, columnCordinates, rowCordinates, null, data, measuresOnColumns, measuresCount,
				columnsRoot.getLeafsNumber());

		celltypeOfColumns = new ArrayList<CrossTab.CellType>();
		celltypeOfRows = new ArrayList<CrossTab.CellType>();

		for (int i = 0; i < dataMatrix.length; i++) {
			celltypeOfRows.add(CellType.DATA);
		}

		for (int i = 0; i < dataMatrix[0].length; i++) {
			celltypeOfColumns.add(CellType.DATA);
		}

	}

	private String getMetaColumnName(JSONArray metaFields, String name) throws JSONException {
		String toReturn = "";

		if (!name.equals("")) {

			for (int i = 0; i < metaFields.length(); i++) {
				if (metaFields.get(i) instanceof String) {
					continue;
				}
				if (name.equalsIgnoreCase(metaFields.getJSONObject(i).getString("header"))) {
					toReturn = metaFields.getJSONObject(i).getString("name");
					break;
				}
			}
		}
		return toReturn;
	}

	private <T extends Attribute> void addHeaderTitles(List<T> lines, int linesIndex, Node node) {
		if (linesIndex < lines.size()) {
			Node descriptionNode;
			if (node.getValue().equals(TOTAL)) {
				descriptionNode = new Node(TOTAL);
			} else {
				descriptionNode = new Node(lines.get(linesIndex).getAlias());
			}
			linesIndex++;
			List<Node> children = node.getChildren();
			List<Node> newchildren = new ArrayList<Node>();
			newchildren.add(descriptionNode);
			for (int i = 0; i < children.size(); i++) {
				descriptionNode.addChild(node.getChildren().get(i));
				addHeaderTitles(lines, linesIndex, node.getChildren().get(i));
			}
			node.setChildren(newchildren);
		}
	}

	private <T extends Attribute> JSONArray getHeaderDescriptions(List<T> lines) {
		JSONArray descriptions = new JSONArray();
		for (int i = 0; i < lines.size(); i++) {
			descriptions.put(lines.get(i).getAlias());
		}
		return descriptions;
	}

	/**
	 * Get the JSON representation of the cross tab
	 *
	 * @return JSON representation of the cross tab
	 * @throws JSONException
	 */
	public JSONObject getJSONCrossTab() throws JSONException {
		JSONObject crossTabDefinition = new JSONObject();
		crossTabDefinition.put(CROSSTAB_JSON_MEASURES_METADATA, getJSONMeasuresMetadata());
		crossTabDefinition.put(CROSSTAB_JSON_ROWS_HEADERS, rowsRoot.toJSONObject());

		JSONArray descriptions = getHeaderDescriptions(crosstabDefinition.getRows());
		crossTabDefinition.put(CROSSTAB_JSON_ROWS_HEADERS_DESCRIPTION, descriptions);

		crossTabDefinition.put(CROSSTAB_JSON_COLUMNS_HEADERS, columnsRoot.toJSONObject());
		crossTabDefinition.put(CROSSTAB_JSON_DATA, getJSONDataMatrix());
		crossTabDefinition.put(CROSSTAB_CELLTYPEOFCOLUMNS, serializeCellType(this.celltypeOfColumns));
		crossTabDefinition.put(CROSSTAB_CELLTYPEOFROWS, serializeCellType(this.celltypeOfRows));
		crossTabDefinition.put(CROSSTAB_JSON_CONFIG, config);
		return crossTabDefinition;
	}

	private JSONArray serializeCellType(List<CellType> celltypes) {
		JSONArray types = new JSONArray();
		for (int i = 0; i < celltypes.size(); i++) {
			types.put(celltypes.get(i).getValue());
		}
		return types;
	}

	/**
	 * Get the matrix that represent the data
	 *
	 * @param columnsSpecification : A list with all the possible coordinates of the columns
	 * @param rowsSpecification    : A list with all the possible coordinates of the rows
	 * @param columnCordinates     : A list with the column coordinates of all the data
	 * @param rowCordinates        : A list with the column rows of all the data
	 * @param data                 : A list with the data
	 * @param measuresOnColumns    : true if the measures live in the columns, false if the measures live in the rows
	 * @param measuresLength       : the number of the measures
	 * @return the matrix that represent the data
	 */
	private Placeholder/* <?> */[][] getDataMatrix(List<String> columnsSpecification, List<String> rowsSpecification, List<String> columnCordinates,
			List<String> rowCordinates, List<String> measuresCordinates, List<String> data, boolean measuresOnColumns, int measuresLength, int columnsN) {
		Placeholder/* <?> */[][] dataMatrix;
		int x, y;
		int rowsN;

		if (measuresOnColumns) {
			rowsN = (rowsSpecification.size() > 0 ? rowsSpecification.size() : 1);
		} else {
			rowsN = (rowsSpecification.size() > 0 ? rowsSpecification.size() : 1) * measuresLength;
		}

		dataMatrix = new Placeholder/* <?> */[rowsN][columnsN];

		// init the matrix
		for (int i = 0; i < rowsN; i++) {
			for (int j = 0; j < columnsN; j++) {
				dataMatrix[i][j] = NOT_AVAILABLE_PLACEHOLDER;
			}
		}

		if (measuresOnColumns) {
			for (int i = 0; i < data.size(); i = i + measuresLength) {
				for (int j = 0; j < measuresLength; j++) {
					String rowCoordinate = null;
					String columnCoordinate = null;

					if (rowsSpecification.size() > 0) {
						rowCoordinate = rowCordinates.get(i + j);
						x = rowsSpecification.indexOf(rowCoordinate);
						if (x < 0) {
							continue; // elements not found because crosstab is
										// too big and it was truncated
						}
					} else {
						x = 0; // crosstab with no attributes on rows
					}
					if (columnsSpecification.size() > 0) {
						columnCoordinate = columnCordinates.get(i + j);
						y = columnsSpecification.indexOf(columnCoordinate);
						if (y < 0) {
							continue; // elements not found because crosstab is
										// too big and it was truncated
						}
					} else {
						y = 0; // crosstab with no attributes on columns
					}
					if ((y * measuresLength + j) < columnsN && (y * measuresLength + j) >= 0) {
						MeasureInfo measureInfo = measures.get(j);
						Placeholder placeholder = NOT_AVAILABLE_PLACEHOLDER;
						try {
							placeholder = new ValuePlaceholder(data.get(i + j), measureInfo);
						} catch (NumberFormatException e) {
							placeholder = new StringPlaceholder(data.get(i + j), measureInfo);
						}
						dataMatrix[x][y * measuresLength + j] = placeholder;
					}
				}
			}
		} else {
			for (int i = 0; i < data.size(); i = i + measuresLength) {
				for (int j = 0; j < measuresLength; j++) {
					String rowCoordinate = null;
					String columnCoordinate = null;

					if (rowsSpecification.size() > 0) {
						rowCoordinate = rowCordinates.get(i + j);
						x = rowsSpecification.indexOf(rowCoordinate);
						if (x < 0) {
							continue; // elements not found because crosstab is
										// too big and it was truncated
						}
					} else {
						x = 0; // crosstab with no attributes on rows
					}

					if (columnsSpecification.size() > 0) {
						columnCoordinate = columnCordinates.get(i + j);
						y = columnsSpecification.indexOf(columnCoordinate);
						if (y < 0) {
							continue; // elements not found because crosstab is
										// too big and it was truncated
						}
					} else {
						y = 0; // crosstab with no attributes on columns
					}

					if (y < columnsN && y >= 0) {
						MeasureInfo measureInfo = measures.get(j);
						ValuePlaceholder valuePlaceholder = new ValuePlaceholder(data.get(i + j), measureInfo);
						dataMatrix[x * measuresLength + j][y] = valuePlaceholder;
					}
				}
			}
		}

		return dataMatrix;

	}

	/**
	 * Serialize the matrix in a JSON format
	 *
	 * @return the matrix in a JSON format
	 */
	public JSONArray getJSONDataMatrix() {

		JSONArray matrix = new JSONArray();
		JSONArray row = new JSONArray();

		// transform the matrix
		for (int i = 0; i < dataMatrix.length; i++) {
			row = new JSONArray();
			for (int j = 0; j < dataMatrix[i].length; j++) {
				row.put(dataMatrix[i][j]);
			}
			matrix.put(row);
		}

		return matrix;
	}

	/**
	 * Add to the root (columnRoot or rowRoot) a path from the root to a leaf. A record contains both the columns definition and the rows definition: (it may be
	 * something like that: C1 C2 C3 R1 R2 M1 M1, where Ci represent a column, Ri represent a row, Mi a measure). So for take a column path (C1 C2 C3), we need
	 * need a start and end position in the record (in this case 0,3)
	 *
	 * @param root          : the node in witch add the record
	 * @param record
	 * @param startPosition
	 * @param endPosition
	 */
	private boolean addRecord(Node root, JSONObject datasetRecords, List<String> attributeFieldsName, List<String> orderingList) {
		boolean toReturn = false;
		Node nodeToCheck = root;

		try {
			for (int indexFields = 0; indexFields < attributeFieldsName.size(); indexFields++) {
				String columnName = attributeFieldsName.get(indexFields);
				String valueColumn = getValueFromOrderingId(orderingList, columnName);
				if (valueColumn == null || valueColumn.equals(""))
					valueColumn = columnName; // value = description
				String valueField = datasetRecords.getString(valueColumn);
				String descriptionField = datasetRecords.getString(columnName);
				JSONObject jsonObject = dsColumnName2Metadata.get(valueColumn);
				Node node = new Node(columnName, valueField, descriptionField, jsonObject);

				int nodePosition = nodeToCheck.getChildren().indexOf(node);
				if (nodePosition < 0) {
					toReturn = true;
					nodeToCheck.addChild(node);
					nodeToCheck = node;
				} else {
					nodeToCheck = nodeToCheck.getChildren().get(nodePosition);
				}
			}
		} catch (Exception e) {
			logger.error("Error getting the values from the dataset", e);
			throw new SpagoBIEngineRuntimeException("Error getting the values from the dataset", e);
		}
		return toReturn;
	}

	private String getValueFromOrderingId(List<String> orderingList, String name) {
		for (int i = 0; i < orderingList.size(); i++) {
			String orderingName = orderingList.get(i);
			String[] ordering = orderingName.split("\\|");
			if (ordering.length == 2 && ordering[1].equalsIgnoreCase(name)) {
				return ordering[0];
			}
		}
		return null;
	}

	/**
	 * Add to the root (columnRoot or rowRoot) a path from the root to a leaf. A record contains both the columns definition and the rows definition: (it may be
	 * something like that: C1 C2 C3 R1 R2 M1 M1, where Ci represent a column, Ri represent a row, Mi a measure). So for take a column path (C1 C2 C3), we need
	 * need a start and end position in the record (in this case 0,3)
	 *
	 * @param root          : the node in witch add the record
	 * @param record
	 * @param startPosition
	 * @param endPosition
	 */
	private boolean addRecord(Node root, IRecord valueRecord, int startPosition, int endPosition) {
		boolean toReturn = false;
		IField valueField;
		Node node;
		Node nodeToCheck = root;
		int nodePosition;
		List<IField> valueFields = new ArrayList<IField>();

		valueFields = valueRecord.getFields();

		for (int indexFields = startPosition; indexFields < endPosition; indexFields++) {
			valueField = valueFields.get(indexFields);
			// there is a description
			// there is only the value
			if (valueField.getValue() != null) {
				if (valueField.getDescription() != null) {
					node = new Node(valueField.getValue().toString(), valueField.getDescription().toString());
				} else {
					node = new Node(valueField.getValue().toString());
				}
			} else {
				node = new Node("null");
			}
			nodePosition = nodeToCheck.getChildren().indexOf(node);
			if (nodePosition < 0) {
				toReturn = true;
				nodeToCheck.addChild(node);
				nodeToCheck = node;
			} else {
				nodeToCheck = nodeToCheck.getChildren().get(nodePosition);
			}
		}
		return toReturn;
	}

	/**
	 * Return a list with all the path from the node n to the leafs
	 *
	 * @param n : the root node
	 * @return list with all the path from the node n to the leafs
	 */
	private List<String> getLeafsPathList(Node n) {
		List<String> toReturn = new ArrayList<String>();
		for (int i = 0; i < n.getChildren().size(); i++) {
			toReturn.addAll(visit(n.getChildren().get(i), PATH_SEPARATOR));
		}
		return toReturn;
	}

	private List<String> visit(Node n, String prefix) {
		List<String> toReturn = new ArrayList<String>();
		String description = n.getDescription();
		if (StringUtils.isEmpty(description)) {
			description = "null";
		}
		if (n.getChildren().isEmpty()) {
			if (prefix.equals(PATH_SEPARATOR)) {
				toReturn.add(prefix + description);
			} else {
				toReturn.add(prefix + PATH_SEPARATOR + description);
			}
			return toReturn;
		} else {
			for (int i = 0; i < n.getChildren().size(); i++) {
				if (prefix.equals(PATH_SEPARATOR)) {
					toReturn.addAll(visit(n.getChildren().get(i), prefix + description));
				} else {
					toReturn.addAll(visit(n.getChildren().get(i), prefix + PATH_SEPARATOR + description));
				}
			}
			return toReturn;
		}
	}

	/**
	 * Add the measures as leafs to all the leafs
	 *
	 * @param root
	 * @param measures
	 */
	private void addMeasuresToTree(Node root, List<Measure> measures) {
		List<Node> measuresNodes = new ArrayList<Node>();
		for (Measure measure : measures) {
			String alias = measure.getAlias();
			String columnName = alias2DsColumnName.get(alias);
			measuresNodes.add(new Node(columnName, alias, alias, true));
		}
		addMeasuresToLeafs(root, measuresNodes);

	}

	// It's ok that the list of the measures is the same for every leaf
	private void addMeasuresToLeafs(Node node, List<Node> measuresNodes) {
		if (node.getChildren().size() == 0) {
			for (int i = 0; i < measuresNodes.size(); i++) {
				Node n = measuresNodes.get(i).clone();
				node.addChild(n);
			}
//			node.updateFathers();
		} else {
			for (int i = 0; i < node.getChildren().size(); i++) {
				addMeasuresToLeafs(node.getChildren().get(i), measuresNodes);
			}
		}
	}

	private static String getStringValue(Object obj) {

		if (obj == null) {
			return "NULL";
		}
		String fieldValue = null;

		Class clazz = obj.getClass();
		if (clazz == null) {
			clazz = String.class;
		}
		if (Timestamp.class.isAssignableFrom(clazz)) {
			fieldValue = TIMESTAMP_FORMATTER.format(obj);
		} else if (Date.class.isAssignableFrom(clazz)) {
			fieldValue = DATE_FORMATTER.format(obj);
		} else {
			fieldValue = obj.toString();
		}

		return fieldValue;

	}

	private MeasureInfo getMeasureInfo(IFieldMetaData fieldMeta, Measure measure) {
		Class clazz = fieldMeta.getType();
		if (clazz == null) {
			clazz = String.class;
		}

		String fieldName = measure.getAlias(); // the measure name is not the
												// name (or alias) of the field
												// coming with the datastore
												// since it is something like
												// SUM(col_0_0_) (see how
												// crosstab datastore query is
												// created)
		String aggregationFunction = null;
		if (measure.getAggregationFunction() != null) {
			aggregationFunction = measure.getAggregationFunction().getName();
		}

		Boolean excludeFromTotalAndSubtotal = measure.getExcludeFromTotalAndSubtotal();

		if (Number.class.isAssignableFrom(clazz)) {

			// BigInteger, Integer, Long, Short, Byte
			if (Integer.class.isAssignableFrom(clazz) || BigInteger.class.isAssignableFrom(clazz) || Long.class.isAssignableFrom(clazz)
					|| Short.class.isAssignableFrom(clazz) || Byte.class.isAssignableFrom(clazz)) {
				return new MeasureInfo(fieldName, measure.getEntityId(), "int", null, aggregationFunction, excludeFromTotalAndSubtotal);
			} else {
				String decimalPrecision = (String) fieldMeta.getProperty(IFieldMetaData.DECIMALPRECISION);
				if (decimalPrecision != null) {
					return new MeasureInfo(fieldName, measure.getEntityId(), "float", "{decimalPrecision:" + decimalPrecision + "}", aggregationFunction,
							excludeFromTotalAndSubtotal);
				} else {
					return new MeasureInfo(fieldName, measure.getEntityId(), "float", null, aggregationFunction, excludeFromTotalAndSubtotal);
				}
			}

		} else if (Timestamp.class.isAssignableFrom(clazz)) {
			return new MeasureInfo(fieldName, measure.getEntityId(), "timestamp", "d/m/Y H:i:s", aggregationFunction, excludeFromTotalAndSubtotal);
		} else if (Date.class.isAssignableFrom(clazz)) {
			return new MeasureInfo(fieldName, measure.getEntityId(), "date", "d/m/Y", aggregationFunction, excludeFromTotalAndSubtotal);
		} else {
			return new MeasureInfo(fieldName, measure.getEntityId(), "string", null, aggregationFunction, excludeFromTotalAndSubtotal);
		}
	}

	private JSONArray getJSONMeasuresMetadata() throws JSONException {
		JSONArray array = new JSONArray();
		for (int i = 0; i < measures.size(); i++) {
			MeasureInfo mi = measures.get(i);
			JSONObject jsonMi = new JSONObject();
			jsonMi.put(MEASURE_NAME, mi.getName());
			jsonMi.put(MEASURE_TYPE, mi.getType());
			jsonMi.put(MEASURE_FORMAT, mi.getFormat() != null ? mi.getFormat() : "");
			jsonMi.put(MEASURE_POSITION, i);
			array.put(jsonMi);
		}
		return array;
	}

	public String getMeasureScaleFactor(String name) {
		Iterator<MeasureInfo> it = measures.iterator();
		while (it.hasNext()) {
			MeasureInfo mi = it.next();
			if (mi.getName().equals(name)) {
				return mi.getScaleFactor();
			}
		}
		return "";
	}

	public static class MeasureInfo {

		final String name;
		final String type;
		final String format;
		final String id;
		String scaleFactor;
		final String aggregationFunction;
		final Boolean excludeFromTotalAndSubtotal;

		public MeasureInfo(String name, String id, String type, String format, String aggregationFunction, Boolean excludeFromTotalAndSubtotal) {
			this.name = name;
			this.type = type;
			this.format = format;
			this.id = id;
			this.aggregationFunction = aggregationFunction;
			this.excludeFromTotalAndSubtotal = excludeFromTotalAndSubtotal;
		}

		public String getName() {
			return name;
		}

		public String getType() {
			return type;
		}

		public String getFormat() {
			return format;
		}

		public String getScaleFactor() {
			return scaleFactor;
		}

		public void setScaleFactor(String scaleFactor) {
			this.scaleFactor = scaleFactor;
		}

		public String getId() {
			return id;
		}

		public String getAggregationFunction() {
			return aggregationFunction;
		}

		public Boolean getExcludeFromTotalAndSubtotal() {
			return excludeFromTotalAndSubtotal;
		}

		@Override
		public String toString() {
			return "MeasureInfo [id=" + id + ", type=" + type + ", aggregationFunction=" + aggregationFunction + "]";
		}

	}

	/*************************************************
	 * CALCULATED FIELDS
	 ************************************************/

	/**
	 * Get a list of nodes and merge them.. It builds a tree with all the node in common with the subtree in with radix in input.. For example nodes=[A,[1,2,3],
	 * A,[1,6]] the result is [A,[1]] In the leafs (in this case 1) it add the position of that header in the matrix.. In this case, suppose the id of the first
	 * occurence of A.1 is at row 3 and the second in row 7 the leafs with id A.1 has this list [3,7]
	 *
	 * @param nodes
	 * @param NodeValue
	 * @return
	 */
	private Node mergeNodes(List<Node> nodes, String NodeValue) {
		Assert.assertNotNull(nodes, "We need at least a node to merge");
		Assert.assertTrue(nodes.size() > 0, "We need at least a node to merge");
		int index;
		List<Node> commonChildNode;
		Node newNode = new Node(NodeValue);
		newNode.setCellType(CellType.CF);
		List<Node> newchilds = new ArrayList<Node>();
		if (nodes.size() > 0) {
			// get the first node. If a child of the first node
			// is not a child of the other nodes is not in common...
			Node firstNode = nodes.get(0);
			List<Node> firstNodeChilds = firstNode.getChildren();
			if (firstNodeChilds != null && firstNodeChilds.size() > 0) {
				for (int i = 0; i < firstNodeChilds.size(); i++) {
					commonChildNode = new ArrayList<Node>();
					commonChildNode.add(firstNodeChilds.get(i));
					// look for the child in all other nodes
					for (int j = 1; j < nodes.size(); j++) {
						index = nodes.get(j).getChildren().indexOf(firstNodeChilds.get(i));
						if (index >= 0) {
							commonChildNode.add(nodes.get(j).getChildren().get(index));
						} else {
							commonChildNode = null;
							break;
						}
					}
					if (commonChildNode != null) {
						newchilds.add(mergeNodes(commonChildNode, firstNodeChilds.get(i).getValue()));
					}
				}
			} else {
				// we are the leafs.. so we want the id of the node
				List<Integer> leafPositions = new ArrayList<Integer>();
				for (int j = 0; j < nodes.size(); j++) {
					leafPositions.add(nodes.get(j).getLeafPosition());
				}
				newNode.setLeafPositionsForCF(leafPositions);
			}
		}
		newNode.setChildren(newchilds);
		return newNode;
	}

	/**
	 * Remove the leafs not in the last level of the tree
	 *
	 * @param node
	 * @param level
	 */
	private void cleanTreeAfterMerge(Node node, int level) {
		int treeDepth = node.getSubTreeDepth();
		List<Node> listOfNodesToRemove = cleanTreeAfterMergeRecorsive(node, treeDepth, level);
		for (Iterator<Node> iterator = listOfNodesToRemove.iterator(); iterator.hasNext();) {
			Node node2 = iterator.next();
			node2.removeNodeFromTree();

		}
	}

	/**
	 * Remove the dead nodes (the inner nodes with no leafs)
	 *
	 * @param node
	 * @param treeDepth
	 * @param level
	 * @return
	 */
	private List<Node> cleanTreeAfterMergeRecorsive(Node node, int treeDepth, int level) {
		List<Node> listOfNodesToRemove = new ArrayList<Node>();
		if (node.getChildren().size() == 0) {
			if (level < treeDepth - 1) {
				listOfNodesToRemove.add(node);
			}
		} else {
			for (int i = 0; i < node.getChildren().size(); i++) {
				listOfNodesToRemove.addAll(cleanTreeAfterMergeRecorsive(node.getChildren().get(i), treeDepth, level + 1));
			}
		}
		return listOfNodesToRemove;
	}

	/**
	 * Calculate the calculated fields and add the result in the structure
	 *
	 * @param operation
	 * @param horizontal
	 * @param level
	 * @param cfName
	 * @param celltype
	 */
	private void calculateCF(String operation, boolean horizontal, int level, String cfName, CellType celltype) {
		Node rootNode;
		List<Node> fathersOfTheNodesOfTheLevel;
		if (horizontal) {
			rootNode = columnsRoot;
		} else {
			rootNode = rowsRoot;
		}

		if (horizontal) {
			fathersOfTheNodesOfTheLevel = rootNode.getLevel(level - 2);// because
																		// of
																		// the
																		// title
																		// of
																		// the
																		// headers
		} else {
			fathersOfTheNodesOfTheLevel = rootNode.getLevel(level - 1);
		}

		for (int i = 0; i < fathersOfTheNodesOfTheLevel.size(); i++) {
			rootNode.setLeafPositions();
			calculateCFSub(operation, fathersOfTheNodesOfTheLevel.get(i), horizontal, level, cfName, celltype);
		}
	}

	/**
	 * Calculate the calculated fields and add the result in the structure
	 *
	 * @param operation
	 * @param node       the result of the calculated fields will add as child of this node
	 * @param horizontal
	 * @param level
	 * @param cfName
	 * @param celltype
	 */
	private void calculateCF(String operation, Node node, boolean horizontal, int level, String cfName, CellType celltype) {
		Node rootNode;
		if (horizontal) {
			rootNode = columnsRoot;
		} else {
			rootNode = rowsRoot;
		}

		List<Node> fathersOfTheNodesOfTheLevel = new ArrayList<Node>();
		fathersOfTheNodesOfTheLevel.add(node);

		for (int i = 0; i < fathersOfTheNodesOfTheLevel.size(); i++) {
			rootNode.setLeafPositions();
			calculateCFSub(operation, fathersOfTheNodesOfTheLevel.get(i), horizontal, level, cfName, celltype);
		}
	}

	/**
	 *
	 * @param operation
	 * @param node       the parent node of the CF
	 * @param horizontal
	 * @param level
	 * @param cfName
	 * @param celltype
	 */
	private void calculateCFSub(String operation, Node node, boolean horizontal, int level, String cfName, CellType celltype) {
		List<String[]> calculatedFieldResult = new ArrayList<String[]>();
		List<String> operationParsed;
		List<String> operationExpsNames;
		List<List<String>> parseOperationR = parseOperation(operation);
		operationParsed = parseOperationR.get(0);
		operationExpsNames = parseOperationR.get(1);

		List<Node> levelNodes = node.getChildren();

		Object[] expressionMap = buildExpressionMap(levelNodes, operationExpsNames);
		Map<String, Integer> expressionToIndexMap = (Map<String, Integer>) expressionMap[1];
		List<Node> nodeInvolvedInTheOperation = (List<Node>) expressionMap[0];
		if (nodeInvolvedInTheOperation.size() > 0) {

			// add the header
			int positionToAdd = node.getRightMostLeafPositionCF() + 1;

			Node mergedNode = mergeNodes(nodeInvolvedInTheOperation, cfName);
			cleanTreeAfterMerge(mergedNode, level);
			List<Node> mergedNodeLeafs = mergedNode.getLeafs();
			for (int i = 0; i < mergedNodeLeafs.size(); i++) {
				List<Integer> leafPositions = mergedNodeLeafs.get(i).getLeafPositionsForCF();
				List<String[]> arraysInvolvedInTheOperation = getArraysInvolvedInTheOperation(horizontal, operationExpsNames, expressionToIndexMap,
						leafPositions);
				calculatedFieldResult.add(executeOperationOnArrays(arraysInvolvedInTheOperation, operationParsed));
				List<String[]> totalArraysInvolvedInTheOperation = getTotalArraysInvolvedInTheOperation(horizontal, leafPositions);
				executeOperationOnTotalArraysAndUpdate(totalArraysInvolvedInTheOperation, operationParsed, horizontal, positionToAdd);
			}

			node.addChild(mergedNode);
			addCrosstabDataLine(node, positionToAdd, calculatedFieldResult, horizontal, celltype);
		}
	}

	/**
	 * Parse the operation
	 *
	 * @param operation
	 * @return
	 */
	private static List<List<String>> parseOperation(String operation) {
		String freshOp = " " + operation;
		List<String> operationParsed = new ArrayList<String>();
		List<String> operationExpsNames = new ArrayList<String>();
		int index = 0;
		// parse the operation
		while (freshOp.indexOf("field[") >= 0) {
			index = freshOp.indexOf("field[") + 6;
			operationParsed.add(freshOp.substring(0, index - 6));
			freshOp = freshOp.substring(index);
			index = freshOp.indexOf("]");
			operationExpsNames.add(freshOp.substring(0, index));
			freshOp = freshOp.substring(index + 1);
		}
		operationParsed.add(freshOp);
		List<List<String>> toReturn = new ArrayList<List<String>>();
		toReturn.add(operationParsed);
		toReturn.add(operationExpsNames);
		return toReturn;
	}

	/**
	 * Build the list of the nodes involved in the operation and the map of the indexes es: nodes: [A,[1,2]], [B[1,3]] , [C[1,3]] operationExpsNames: [A, C]
	 * nodeInvolvedInTheOperation: [A,[1,2]], [B[1,3]] expressionToIndexMap: 0,2
	 *
	 * @param nodes              the list of the nodes of the level involved in the cf
	 * @param operationExpsNames the alias of the fields in the query
	 * @return
	 */
	private Object[] buildExpressionMap(List<Node> nodes, List<String> operationExpsNames) {
		Map<String, Integer> expressionToIndexMap = new HashMap<String, Integer>();
		List<Node> nodeInvolvedInTheOperation = new ArrayList<Node>();
		int foundNode = 0;
		for (Iterator<String> iterator = operationExpsNames.iterator(); iterator.hasNext();) {
			String operationElement = iterator.next();
			if (!expressionToIndexMap.containsKey(operationElement)) {
				expressionToIndexMap.put(operationElement, foundNode);
				for (int y = 0; y < nodes.size(); y++) {
					if (nodes.get(y).getValue().equals(operationElement)) {
						nodeInvolvedInTheOperation.add(nodes.get(y));
						foundNode++;
						break;
					}
				}
			}
		}
		Object[] toReturn = new Object[2];
		toReturn[0] = nodeInvolvedInTheOperation;
		toReturn[1] = expressionToIndexMap;

		return toReturn;
	}

	/**
	 *
	 * @param horizontal
	 * @param operationExpsNames   the names of the operation : A+ D+C-(A*C) = A,D,C,A,C
	 * @param expressionToIndexMap if the operation is the same of before and the Nodes of the level are A,B,C,D the map is (A->0, B->1...)
	 * @param indexInTheArray
	 * @return
	 */
	private List<String[]> getArraysInvolvedInTheOperation(boolean horizontal, List<String> operationExpsNames, Map<String, Integer> expressionToIndexMap,
			List<Integer> indexInTheArray) {
		List<String[]> toReturn = new ArrayList<String[]>();
		for (int y = 0; y < operationExpsNames.size(); y++) {
			String alias = operationExpsNames.get(y);
			int index = expressionToIndexMap.get(alias);
			if (horizontal) {
				toReturn.add(getCrosstabDataColumn(indexInTheArray.get(index)));
			} else {
				toReturn.add(getCrosstabDataRow(indexInTheArray.get(index)));
			}
		}
		return toReturn;
	}

	private List<String[]> getTotalArraysInvolvedInTheOperation(boolean horizontal, List<Integer> indexInTheArray) {
		List<String[]> toReturn = new ArrayList<String[]>();

		for (int y = 0; y < indexInTheArray.size(); y++) {
			if (horizontal) {
				String[] sum = new String[columnsSum.size()];
				for (int i = 0; i < columnsSum.size(); i++) {
					sum[i] = columnsSum.get(i)[(indexInTheArray.get(y))];
				}
				toReturn.add(sum);
			} else {
				String[] sum = new String[rowsSum.size()];
				for (int i = 0; i < rowsSum.size(); i++) {
					sum[i] = rowsSum.get(i)[(indexInTheArray.get(y))];
				}
				toReturn.add(sum);
			}
		}
		return toReturn;
	}

	/**
	 * Build and execute the operation.. For example x+y = [4,6]
	 *
	 * @param data      list of rows/columns members of the operation l'operazione es [1,2], [3,4]
	 * @param operation the operation es: [+]
	 * @return
	 */
	private String[] executeOperationOnArrays(List<String[]> data, List<String> operation) {
		List<String> operationElements;
		int datalength = data.get(0).length;
		String[] operationResult = new String[datalength];
		for (int i = 0; i < datalength; i++) {
			operationElements = new ArrayList<String>();
			for (int j = 0; j < data.size(); j++) {
				operationElements.add(data.get(j)[i]);
			}
			operationResult[i] = executeOperationOnNumbers(operationElements, operation);
		}
		return operationResult;
	}

	private void executeOperationOnTotalArraysAndUpdate(List<String[]> data, List<String> operation, boolean horizontal, int positionToAdd) {
		String[] executeOperationOnArrays = executeOperationOnArrays(data, operation);
		if (horizontal) {
			for (int i = 0; i < executeOperationOnArrays.length; i++) {
				String[] sum = columnsSum.get(i);
				String[] newSum = new String[sum.length + 1];
				for (int y = 0; y < sum.length + 1; y++) {
					if (y < positionToAdd) {
						newSum[y] = sum[y];
					} else if (y == positionToAdd) {
						newSum[y] = executeOperationOnArrays[i];
					} else {
						newSum[y] = sum[y - 1];
					}
				}
				columnsSum.set(i, newSum);
			}
		} else {
			for (int i = 0; i < executeOperationOnArrays.length; i++) {
				String[] sum = rowsSum.get(i);
				String[] newSum = new String[sum.length + 1];
				for (int y = 0; y < sum.length + 1; y++) {
					if (y < positionToAdd) {
						newSum[y] = sum[y];
					} else if (y == positionToAdd) {
						newSum[y] = executeOperationOnArrays[i];
					} else {
						newSum[y] = sum[y - 1];
					}
				}
				rowsSum.set(i, newSum);
			}
		}
	}

	/**
	 * Execute the operation 1+2-(2*4)
	 *
	 * @param data the members of the operation.. es: 1,2,3,4
	 * @param op   the list of operator: +,-(,*,)
	 * @return
	 */
	private String executeOperationOnNumbers(List<String> data, List<String> op) {
		String operation = "";
		int i = 0;
		for (i = 0; i < op.size() - 1; i++) {
			operation = operation + op.get(i);

			if (data.get(i) == DATA_MATRIX_NA || data.get(i) == "null" || data.get(i) == null) {
				operation = operation + "0";
			} else {
				operation = operation + data.get(i);
			}
		}
		operation = operation + op.get(i);
		String evalued = (new GroovySandbox().evaluate(operation)).toString();
		return evalued;
	}

	/************************************************************
	 * TOTALS
	 */

	/**
	 * Sum the values of the rows (the right pannel)
	 *
	 * @param measuresOnRow
	 *
	 *                      TODO
	 */
	private List<String[]> getTotalsOnRows(boolean measuresOnRow) {
		List<String[]> result = new ArrayList<String[]>();
		double[] st;
		int measures = 1;
		if (!measuresOnRow) {
			measures = this.measuresToShowOnTotalsOrSubTotals.size();
		}
//		int iteration = dataMatrix[0].length / measures;
		for (int measureId = 0; measureId < measures; measureId++) {
//			MeasureInfo measureInfo = this.measuresToShowOnTotalsOrSubTotals.get(measureId);
//			String aggregationFunction = measureInfo.getAggregationFunction();
			st = new double[dataMatrix.length];
//			for (int i = 0; i < dataMatrix.length; i++) {
//				if (measuresOnRow) {
//					int measureIndex =  i % this.measuresToShowOnTotalsOrSubTotals.size();
//					measureInfo = this.measuresToShowOnTotalsOrSubTotals.get(measureIndex);
//					aggregationFunction = measureInfo.getAggregationFunction();
//				}
//				List<Double> values = new ArrayList<Double>();
//				if (!measureInfo.excludeFromTotalAndSubtotal) {
//					for (int j = 0; j < iteration; j++) {
//						try {
//							if (getCellType(i, j * measures + measureId).equals(CellType.DATA) || getCellType(i, j * measures + measureId).equals(CellType.TOTAL)) {
//								String value = dataMatrix[i][j * measures + measureId];
//								if (!value.equals(DATA_MATRIX_NA)) {
//									values.add(new Double(value));
//								}
//							}
//						} catch (Exception e) {
//							logger.debug("Cant format the number " + (dataMatrix[i][j * measures + measureId]));
//						}
//					}
//				}
//				if (!values.isEmpty()) {
//					if (aggregationFunction.equalsIgnoreCase("MAX")) {
//						st[i] = getMax(values);
//					} else if (aggregationFunction.equalsIgnoreCase("MIN")) {
//						st[i] = getMin(values);
//					} else if (aggregationFunction.equalsIgnoreCase("SUM")) {
//						st[i] = getSum(values);
//					} else if (aggregationFunction.equalsIgnoreCase("COUNT")) {
//						st[i] = getCount(values);
//					} else if (aggregationFunction.equalsIgnoreCase("AVG")) {
//						st[i] = getAvg(values);
//					} else if (aggregationFunction.equalsIgnoreCase("COUNT_DISTINCT")) {
//						st[i] = getCountDistinct(values);
//					}
//				}
//			}
			result.add(toStringArray(st));
		}
		return result;
	}

	private double getMax(List<Double> values) {
		return Collections.max(values);
	}

	private double getMin(List<Double> values) {
		return Collections.min(values);
	}

	private double getCount(List<Double> values) {
		return new Double(values.size());
	}

	private double getSum(List<Double> values) {
		double sum = 0;
		for (Double value : values)
			sum += value;
		return sum;
	}

	private double getAvg(List<Double> values) {
		double sum = getSum(values);
		double count = getCount(values);
		double avg = sum / count;
		return avg;
	}

	private double getCountDistinct(List<Double> values) {
		Set<Double> distinctValues = new HashSet<Double>(values);
		return distinctValues.size();
	}

	/**
	 * TODO
	 */
	private String[] getTotalsOfRows(int start, int length) {
		double[] st = new double[dataMatrix[0].length];

//		for (int i = 0; i < dataMatrix[0].length; i++) {
//			MeasureInfo measureInfo = measures.get(i % measures.size());
//			boolean excludeFromTotalAndSubtotal = measureInfo.excludeFromTotalAndSubtotal;
//			if (excludeFromTotalAndSubtotal) {
//				st[i] = Double.NaN;
//			} else {
//				for (int j = start; j < length + start; j++) {
//					try {
//						if (!excludeFromTotalAndSubtotal && getCellType(j, i).equals(CellType.DATA)) {
//							String value = dataMatrix[j][i];
//							if (!value.equals(DATA_MATRIX_NA)) {
//								st[i] = st[i] + new Double(value);
//							}
//						}
//					} catch (Exception e) {
//						logger.debug("Cant format the number " + (dataMatrix[j][i]));
//					}
//				}
//			}
//		}

		return toStringArray(st);
	}

	private String[] toStringArray(double[] doubleArray) {
		String[] strings = new String[doubleArray.length];
		for (int i = 0; i < doubleArray.length; i++) {
			double d = doubleArray[i];
			strings[i] = (Double.isNaN(d)) ? "" : "" + d;
		}
		return strings;
	}

	/**
	 * Sum the values of the columns (the bottom panel)
	 *
	 * @param measuresOnRow
	 * @return
	 *
	 *         TODO
	 */
	private List<String[]> getTotalsOnColumns(boolean measuresOnRow) {
		List<String[]> result = new ArrayList<String[]>();
		double[] st;
//		int measures = 1; //default for measures on column : it sums 1 column at a time
//		if (measuresOnRow) {
//			measures = this.measures.size();
//		}
//		int iteration = dataMatrix.length / measures;
//		for (int measureId = 0; measureId < measures; measureId++) {
//			MeasureInfo measureInfo = this.measuresToShowOnTotalsOrSubTotals.get(measureId);
		st = new double[dataMatrix[0].length];
//			Map<Integer,List<Double>> valuesMap = new HashMap<Integer,List<Double>>();
//			for (int i = 0; i < iteration; i++) {
//				for (int j = 0; j < dataMatrix[0].length; j++) {
//					MeasureInfo measureInfoNew = this.measures.get(j % this.measures.size());
//					try {
//						if (getCellType(i * measures + measureId, j).equals(CellType.DATA) || getCellType(i * measures + measureId, j).equals(CellType.SUBTOTAL) || getCellType(i * measures + measureId, j).equals(CellType.TOTAL)) {
//							String value = dataMatrix[i * measures + measureId][j];
//							if (measureInfoNew.excludeFromTotalAndSubtotal) {
//								if (!valuesMap.containsKey(j)) {
//									valuesMap.put(j, Collections.EMPTY_LIST);
//								}
//							} else if (!value.equals(DATA_MATRIX_NA)) {
//								if (valuesMap.containsKey(j)) {
//									valuesMap.get(j).add( new Double(value));
//								} else {
//									List<Double> list = new ArrayList<Double>();
//									list.add(new Double(value));
//									valuesMap.put(j,list );
//								}
//							}
//						}
//					} catch (Exception e) {
//						logger.debug("Cant format the number " + (dataMatrix[i * measures + measureId][j]));
//					}
//				}
//			}
//
//			Iterator<Map.Entry<Integer,List<Double>>> it = valuesMap.entrySet().iterator();
//			while (it.hasNext()) {
//				Map.Entry<Integer,List<Double>> pair = it.next();
//				List<Double> values = pair.getValue();
//				Integer index = pair.getKey();
//				int measureIndex;
//				if (measuresOnRow) {
//					measureIndex = measureId;
//				} else {
//					measureIndex = index % this.measuresToShowOnTotalsOrSubTotals.size();
//				}
//
//				String aggregationFunction = measureInfo.getAggregationFunction();
//				if (!values.isEmpty()) {
//					if (aggregationFunction.equalsIgnoreCase("MAX")) {
//						st[index] = getMax(values);
//					} else if (aggregationFunction.equalsIgnoreCase("MIN")) {
//						st[index] = getMin(values);
//					} else if (aggregationFunction.equalsIgnoreCase("SUM")) {
//						st[index] = getSum(values);
//					} else if (aggregationFunction.equalsIgnoreCase("COUNT")) {
//						st[index] = getCount(values);
//					} else if (aggregationFunction.equalsIgnoreCase("AVG")) {
//						st[index] = getAvg(values);
//					} else if (aggregationFunction.equalsIgnoreCase("COUNT_DISTINCT")) {
//						st[index] = getCountDistinct(values);
//					}
//				} else {
//					st[index] = Double.NaN;
//				}
//
//			}
//
		result.add(toStringArray(st));
//		}
		return result;
	}

	/**
	 * TODO
	 */
	private String[] getTotalsOfColumns(int start, int length) {
		double[] st = new double[dataMatrix.length];

//		for (int i = 0; i < dataMatrix.length; i++) {
//			for (int j = start; j < length + start; j++) {
//				try {
//					if (getCellType(i, j).equals(CellType.DATA)) {
//						String value = dataMatrix[i][j];
//						if (!value.equals(DATA_MATRIX_NA)) {
//							st[i] = st[i] + new Double(value);
//						}
//					}
//				} catch (Exception e) {
//					logger.debug("Cant format the number " + (dataMatrix[i][j]));
//				}
//			}
//		}

		return toStringArray(st);
	}

	/**
	 * TODO
	 */
	private double getTotalsOfColumn(int column, CellType type) {

		double sum = 0;
//		int nrows = (type.getValue().equalsIgnoreCase("partialsum")) ? dataMatrix.length - 1 : dataMatrix.length; //  if subtotal doesn't sum that partial total
//		for (int y = 0; y < nrows; y++) {
//			if (celltypeOfColumns.get(column).equals(type)) {
//				CellType prevCellType = getCellType(y,column-1);
//				String value = dataMatrix[y][column];
//				if (!value.equals(DATA_MATRIX_NA)) {
//					sum = sum + new Double(value);
//				}
//			}
//		}
		return sum;
	}

	/**
	 * TODO
	 */
	private String[] getTotalsOfColumnWithMeasure(int colunm) {

		double[] st = new double[measures.size()];
//		int measurescount = 0;
//
//		for (int y = 0; y < dataMatrix.length; y++) {
//			if (!celltypeOfRows.get(y).equals(CellType.CF)) {
//				st[measurescount % measures.size()] = st[measurescount % measures.size()] + new Double(dataMatrix[y][colunm]);
//				measurescount++;
//			}
//		}

		return toStringArray(st);
	}

	private int getSubtotalCellsNumber(List<CellType> celltypeOfRows) {
		int toReturn = 0;

		for (int c = 0; c < celltypeOfRows.size(); c++) {
			if (celltypeOfRows.get(c).getValue().equals("partialsum"))
				toReturn++;
		}
		return toReturn;
	}

	/**
	 * TODO
	 */
	private double getTotalsOfRow(int row, CellType type) {
		double sum = 0;
//		int nSubtotals = getSubtotalCellsNumber(celltypeOfRows);
//		nSubtotals = (nSubtotals == 0) ? 0 : nSubtotals-1;
//		int nMaxCol = dataMatrix[0].length - (nSubtotals);
//		for (int y = 0; y < dataMatrix[0].length; y++) {
////			if (y < celltypeOfRows.size() && celltypeOfRows.get(y).equals(type)) { //ORIG
//			if (y < celltypeOfRows.size() && celltypeOfRows.get(row).equals(type)) {
//					String value = dataMatrix[row][y];
////					if (!value.equals(DATA_MATRIX_NA)) {
//					if (nMaxCol <= 0 || (!value.equals(DATA_MATRIX_NA) && y < nMaxCol)) { //if maxcol <= 0 add always the value
//						sum = sum + new Double(value);
//					}
//			}
//
//		}
		return sum;
	}

	/**
	 * TODO
	 */
	private String[] getTotalsOfRowWithMeasure(int row) {

		double[] st = new double[measures.size()];
//		int measurescount = 0;
//
//		for (int y = 0; y < dataMatrix[0].length; y++) {
//			if (!celltypeOfColumns.get(y).equals(CellType.CF)) {
//				String value = dataMatrix[row][y];
//				if (!value.equals(DATA_MATRIX_NA)) {
//					st[measurescount % measures.size()] = st[measurescount % measures.size()] + new Double(value);
//				}
//				measurescount++;
//			}
//		}

		return toStringArray(st);
	}

	/**
	 * Get the super total (sum of sums)
	 *
	 * @param measuresNumber the number of the measures
	 * @return
	 */
	private String[] getSuperTotal(int measuresNumber) {

		double[] st = new double[measuresNumber];

		String[] aggregationFunctions = new String[measuresNumber];
		List<Integer> notDisplayedSuperTotal = new ArrayList<Integer>();

		for (int i = 0; i < measuresNumber; i++) {
			MeasureInfo measureInfo = this.measuresToShowOnTotalsOrSubTotals.get(i);
			String aggregationFunction = measureInfo.getAggregationFunction();
			aggregationFunctions[i] = aggregationFunction;
		}

		if (measuresOnRow) {
			for (int i = 0; i < measuresNumber; i++) {
				for (int y = 0; y < columnsSum.get(0).length; y++) {
					if (aggregationFunctions[i].equalsIgnoreCase("SUM")) {
						if (celltypeOfColumns.get(y).equals(CellType.DATA)) {
							String value = columnsSum.get(0)[y];
							if (!value.equals(DATA_MATRIX_NA)) {
								st[i] = st[i] + new Double(value);
							}
						}
					} else {
						// we don't display a total in other cases
						// save indexes of elements not displayed
						notDisplayedSuperTotal.add(i);
					}

				}
			}
		} else {
			int measureIteration = 0;
			for (int y = 0; y < columnsSum.get(0).length; y++) {
				if (celltypeOfColumns.get(y).equals(CellType.DATA)) {
					String value = columnsSum.get(0)[y];

					if (!value.equals(DATA_MATRIX_NA)) {
						int index = measureIteration % measuresNumber;

						if (aggregationFunctions[index].equalsIgnoreCase("SUM")) {
							st[index] = st[index] + new Double(value);
						} else {
							// we don't display a total in other cases
							// save indexes of elements not displayed
							notDisplayedSuperTotal.add(index);
						}
					}
					measureIteration++;
				}
			}

		}

		String[] toReturn = toStringArray(st);
		for (Integer index : notDisplayedSuperTotal) {
			toReturn[index] = "";
		}
		return toReturn;
	}

	/**
	 *
	 * @param withMeasures
	 * @param deepth       = tree depth-1
	 * @return
	 */
	private Node getHeaderTotalSubTree(boolean withMeasures, int deepth, boolean onRows) {
		String labelTotal = CrossTab.TOTAL;
		if (onRows && !this.config.optString("rowtotalLabel").equals(""))
			labelTotal = this.config.optString("rowtotalLabel");
		else if (!onRows && !this.config.optString("columntotalLabel").equals(""))
			labelTotal = this.config.optString("columntotalLabel");

		Node node = new Node(TOTAL, labelTotal);
		if (withMeasures && deepth == 2) {
			for (int i = 0; i < measuresToShowOnTotalsOrSubTotals.size(); i++) {
				node.addChild(new Node(measuresToShowOnTotalsOrSubTotals.get(i).getName()));
			}
		} else {
			if (deepth > 1) {
				node.addChild(getHeaderTotalSubTree(withMeasures, deepth - 1, onRows));
			}
		}
		return node;
	}

	private void addTotals() throws JSONException {
		Boolean rowsTotals = config.optBoolean("calculatetotalsoncolumns");
		Boolean columnsTotals = config.optBoolean("calculatetotalsonrows");

		// If there are no columns to show
		rowsTotals &= !measuresToShowOnTotalsOrSubTotals.isEmpty();
		columnsTotals &= !measuresToShowOnTotalsOrSubTotals.isEmpty();

		if (rowsTotals) {
			rowsRoot.addChild(getHeaderTotalSubTree(measuresOnRow, rowsRoot.getSubTreeDepth() - 1, false));
			addCrosstabDataRow(rowsRoot, dataMatrix.length, columnsSum, CellType.TOTAL);
		}

		if (columnsTotals) {
			// add the total of totals
			int measures = this.measuresToShowOnTotalsOrSubTotals.size();
			String[] superTotals = getSuperTotal(measures);

			if (measuresOnRow) {
				String[] freshRowsSum = Arrays.copyOf(rowsSum.get(0), rowsSum.get(0).length + measures);
				for (int j = 0; j < superTotals.length; j++) {
					freshRowsSum[rowsSum.get(0).length + j] = superTotals[j];
				}
				rowsSum.set(0, freshRowsSum);
			} else {
				for (int j = 0; j < superTotals.length; j++) {
					String[] freshRowsSum = Arrays.copyOf(rowsSum.get(j), rowsSum.get(j).length + 1);
					freshRowsSum[rowsSum.get(j).length] = superTotals[j];
					rowsSum.set(j, freshRowsSum);
				}
			}
			columnsRoot.addChild(getHeaderTotalSubTree(!measuresOnRow, columnsRoot.getSubTreeDepth() - 1, true));
			addCrosstabDataColumns(rowsRoot, dataMatrix[0].length, rowsSum, CellType.TOTAL);
		}
	}

	/************************************************************************
	 * SUBTOTALS
	 */
	public void addSubtotals() {
		Boolean rowsTotals = config.optBoolean("calculatesubtotalsonrows");
		Boolean columnsTotals = config.optBoolean("calculatesubtotalsoncolumns");

		// If there are no columns to show
		rowsTotals &= !measuresToShowOnTotalsOrSubTotals.isEmpty();
		columnsTotals &= !measuresToShowOnTotalsOrSubTotals.isEmpty();

		if (measuresOnRow) {
			if (rowsTotals) {
				if (!measuresOnRow) {
					addSubtotalsToTheNodeFirstLevel(columnsRoot, true, 0);
					addSubtotalsToTheTree(columnsRoot, true, 0);
				} else {
					int startPosition = 0;
					for (int i = 0; i < columnsRoot.getChildren().size(); i++) {
						startPosition = addSubtotalsToTheTreeNoMeasure(columnsRoot.getChildren().get(i), true, startPosition);
					}
				}
			}

			if (columnsTotals) {
				if (measuresOnRow) {
					addSubtotalsToTheNodeFirstLevel(rowsRoot, false, 0);
					addSubtotalsToTheTree(rowsRoot, false, 0);
				} else {
					int startPosition = 0;
					for (int i = 0; i < rowsRoot.getChildren().size(); i++) {
						startPosition = addSubtotalsToTheTreeNoMeasure(rowsRoot.getChildren().get(i), false, startPosition);
					}
				}
			}
		} else {

			if (columnsTotals) {
				if (measuresOnRow) {
					addSubtotalsToTheNodeFirstLevel(rowsRoot, false, 0);
					addSubtotalsToTheTree(rowsRoot, false, 0);
				} else {
					int startPosition = 0;
					for (int i = 0; i < rowsRoot.getChildren().size(); i++) {
						startPosition = addSubtotalsToTheTreeNoMeasure(rowsRoot.getChildren().get(i), false, startPosition);
					}
				}
			}
			if (rowsTotals) {
				if (!measuresOnRow) {
					addSubtotalsToTheNodeFirstLevel(columnsRoot, true, 0);
					addSubtotalsToTheTree(columnsRoot, true, 0);
				} else {
					int startPosition = 0;
					for (int i = 0; i < columnsRoot.getChildren().size(); i++) {
						startPosition = addSubtotalsToTheTreeNoMeasure(columnsRoot.getChildren().get(i), true, startPosition);
					}
				}
			}
		}
	}

	public int addSubtotalsToTheTreeNoMeasure(Node node, boolean horizontal, int startingPosition) {

		int start = startingPosition;
		int length = node.getLeafsNumber();
		String[] total;
		List<Node> children = node.getChildren();
		if (children.size() > 0) {
			int freshStartingPosition = startingPosition;

			if (horizontal) {
				total = getTotalsOfColumns(start, length);
			} else {
				total = getTotalsOfRows(start, length);
			}

			List<String[]> linesums = new ArrayList<String[]>();
			linesums.add(total);
			addCrosstabDataLine(node, freshStartingPosition + node.getLeafsNumber(), linesums, horizontal, CellType.SUBTOTAL);

			for (int i = 0; i < children.size(); i++) {
				Node currChild = children.get(i);
				freshStartingPosition = addSubtotalsToTheTreeNoMeasure(currChild, horizontal, freshStartingPosition);
			}

			Node totalNode = buildSubtotalNode(node.getSubTreeDepth() - 1, false, horizontal);
			node.addChild(totalNode);
//			node.updateFathers();
			return startingPosition + node.getLeafsNumber();
		}
		return startingPosition + 1;

	}

	public void addSubtotalsToTheTree(Node node, boolean horizontal, int startingPosition) {
		if (node.getSubTreeDepth() <= 4) {
			return;
		} else {
			for (int i = 0; i < node.getChildren().size(); i++) {
				addSubtotalsToTheTree(node.getChildren().get(i), horizontal, startingPosition);
				startingPosition = addSubtotalsToTheNodeUpLevel(node.getChildren().get(i), horizontal, startingPosition);
			}
		}
	}

	public int addSubtotalsToTheNodeUpLevel(Node node, boolean horizontal, int startingPosition) {
		List<Node> children = node.getChildren();
		List<List<Integer>> valuesTosum = new ArrayList<List<Integer>>();
		List<String[]> linesums = new ArrayList<String[]>();

		for (int y = 0; y < measures.size(); y++) {
			valuesTosum.add(new ArrayList<Integer>());
		}

		for (int i = 0; i < children.size(); i++) {
			startingPosition = startingPosition + children.get(i).getLeafsNumber();
			for (int y = 0; y < measures.size(); y++) {
				List<Integer> indexformeasuery = valuesTosum.get(y);
				indexformeasuery.add(startingPosition - measures.size() + y);
			}
		}

		Node subtotalNode = buildSubtotalNode(node.getSubTreeDepth() - 2, true, horizontal);
		node.addChild(subtotalNode);

		for (int y = 0; y < valuesTosum.size(); y++) {
			List<Integer> linesToSum = valuesTosum.get(y);
			linesums.add(getTotals(linesToSum, horizontal));
		}

		addCrosstabDataLine(node, startingPosition, linesums, horizontal, CellType.SUBTOTAL);

		return startingPosition + measures.size();
	}

	public int addSubtotalsToTheNodeFirstLevel(Node node, boolean horizontal, int positionToAddNode) {
		Node n = node;
		List<String[]> linesums = new ArrayList<String[]>();
		if (n.getChildren().size() > 0 && // has children
				n.getChildren().get(0).getChildren().size() > 0 && // has
																	// granchildren
				n.getChildren().get(0).getChildren().get(0).getChildren().size() > 0) { // the
			// granchildren
			// are
			// not
			// leaf
			for (int i = 0; i < n.getChildren().size(); i++) {
				positionToAddNode = addSubtotalsToTheNodeFirstLevel(n.getChildren().get(i), horizontal, positionToAddNode);
			}
		} else {
			Node subtotalNode = buildSubtotalNode(1, true, horizontal);
			int measuresCount = measures.size();
			for (int y = 0; y < measuresCount; y++) {
				MeasureInfo measureInfo = measures.get(y);

				if (measureInfo.excludeFromTotalAndSubtotal) {
					continue;
				}

				List<Integer> linesToSum = new ArrayList<Integer>();
				if ((!measuresOnRow && crosstabDefinition.getColumns().size() == 0) || (measuresOnRow && crosstabDefinition.getRows().size() == 0)) {
					// no columns required: just measures
					linesToSum.add(positionToAddNode + y);
				} else {
					for (int k = 0; k < n.getChildren().size(); k++) {
						linesToSum.add(positionToAddNode + measuresCount * k + y);
					}
				}
				linesums.add(getTotals(linesToSum, horizontal));
			}
			if ((!measuresOnRow && crosstabDefinition.getColumns().size() == 0) || (measuresOnRow && crosstabDefinition.getRows().size() == 0)) {
				positionToAddNode = positionToAddNode + measuresCount;
			} else {
				positionToAddNode = positionToAddNode + measuresCount * n.getChildren().size();
			}
			node.addChild(subtotalNode);
			addCrosstabDataLine(node, positionToAddNode, linesums, horizontal, CellType.SUBTOTAL);
			positionToAddNode = positionToAddNode + linesums.size();
		}
		return positionToAddNode;

	}

	public Node buildSubtotalNode(int totalHeadersNumber, boolean withMeasures, boolean horizontal) {
		String labelSubTotal = CrossTab.SUBTOTAL;

		if (horizontal && !this.config.optString("rowsubtotalLabel").equals(""))
			labelSubTotal = this.config.optString("rowsubtotalLabel");
		else if (!horizontal && !this.config.optString("columnsubtotalLabel").equals(""))
			labelSubTotal = this.config.optString("columnsubtotalLabel");

		Node node = new Node(SUBTOTAL, labelSubTotal);
		Node toReturn;
		int i = 1;
		if (withMeasures) {
			for (int j = 0; j < measuresToShowOnTotalsOrSubTotals.size(); j++) {
				MeasureInfo measureInfo = measuresToShowOnTotalsOrSubTotals.get(j);
				node.addChild(new Node(measureInfo.getName()));
			}
		}
		toReturn = node;
		for (; i < totalHeadersNumber; i++) {
			toReturn = new Node(SUBTOTAL, labelSubTotal);
			toReturn.addChild(node);
			node = toReturn;
		}
//		node.updateFathers();

		return toReturn;

	}

	/**
	 * Get totals just for subtotals columns.
	 *
	 * TODO
	 */
	private String[] getTotals(List<Integer> lines, boolean horizontal) {
		double sum[];
		if (!horizontal) {
			sum = new double[dataMatrix[0].length];
//			for (int i = 0; i < dataMatrix[0].length; i++) {
//				String value = dataMatrix[lines.get(0)][i];
//				if (!value.equals(DATA_MATRIX_NA) &&
//						(getCellType(lines.get(0), i).equals(CellType.DATA) || getCellType(lines.get(0), i).equals(CellType.SUBTOTAL))) { //get SUBTOTAL too for grand-subtotal
//					sum[i] = new Double(value);
//				}
//			}
//			for (int j = 1; j < lines.size(); j++) {
//				for (int i = 0; i < dataMatrix[0].length; i++) {
//					String value = dataMatrix[lines.get(j)][i];
////					if (!value.equals(DATA_MATRIX_NA) && (getCellType(lines.get(j), i).equals(CellType.DATA))) {
//					if (!value.equals(DATA_MATRIX_NA) &&
//							(getCellType(lines.get(j), i).equals(CellType.DATA)|| getCellType(lines.get(j), i).equals(CellType.SUBTOTAL))) {
//						sum[i] = sum[i] + new Double(value);
//					}
//				}
//			}
		} else {
			sum = new double[dataMatrix.length];
//			for (int i = 0; i < dataMatrix.length; i++) {
//				String value = dataMatrix[i][lines.get(0)];
////				if (!value.equals(DATA_MATRIX_NA) && (getCellType(i, lines.get(0)).equals(CellType.DATA))) { // ORIG
//				if (!value.equals(DATA_MATRIX_NA) &&
//						(getCellType(i, lines.get(0)).equals(CellType.DATA) || getCellType(i, lines.get(0)).equals(CellType.SUBTOTAL))) { //get SUBTOTAL too for grand-subtotal
//					sum[i] = new Double(value);
//				}
//			}
//			int startJ = 1;
//			for (int j = startJ; j < lines.size(); j++) {
//				for (int i = 0; i < dataMatrix.length; i++) {
//					String value = dataMatrix[i][lines.get(j)];
////					if (!value.equals(DATA_MATRIX_NA) && (getCellType(i, lines.get(j)).equals(CellType.DATA))) {
//					if (!value.equals(DATA_MATRIX_NA) &&
//							(getCellType(i, lines.get(j)).equals(CellType.DATA) || getCellType(i, lines.get(j)).equals(CellType.SUBTOTAL))) { //get SUBTOTAL too for grand-subtotal
//						sum[i] = sum[i] + new Double(value);
//					}
//
//				}
//			}
		}

		return toStringArray(sum);
	}

	/********************************************************
	 * UTILITY METHODS
	 ************************************************************/

	/**
	 * Returns a column of the data matrix
	 *
	 * @param i the id of the column to get
	 * @return the i-th column of the data matrix
	 *
	 *         TODO
	 */
	private String[] getCrosstabDataColumn(int i) {
		String[] column = new String[dataMatrix.length];
//		for (int j = 0; j < dataMatrix.length; j++) {
//			column[j] = dataMatrix[j][i];
//		}
		return column;
	}

	/**
	 * Returns a row of the data matrix
	 *
	 * @param i the id of the row to get
	 * @return the i-th row of the data matrix
	 */
	private String[] getCrosstabDataRow(int i) {
		String[] ret = null;

		int length = dataMatrix[i].length;

		ret = new String[length];

		for (int j = 0; j < length; j++) {
			ret[j] = dataMatrix[i][j].getValueAsString();
		}

		return ret;
	}

	/**
	 * Inserts lines in the crosstab data matrix
	 *
	 * @param startposition the position where insert the rows/columns into the matrix
	 * @param line          the lines to insert
	 * @param horizontal    true to insert columns/false to insert rows
	 * @param type          the type of the data
	 */
	public void addCrosstabDataLine(Node currNode, int startposition, List<String[]> line, boolean horizontal, CellType type) {
		if (horizontal) {
			addCrosstabDataColumns(currNode, startposition, line, type);
		} else {
			addCrosstabDataRow(currNode, startposition, line, type);
		}
	}

	/**
	 * Inserts columns in the crosstab data matrix
	 *
	 * @param startposition the position where insert the columns into the matrix
	 * @param colums        the lines to insert
	 * @param type          the type of the data
	 */
	public void addCrosstabDataColumns(Node currNode, int startposition, List<String[]> colums, CellType type) {
		Assert.assertNotNull(dataMatrix, "The data matrix must not be null");
		Assert.assertTrue(startposition <= dataMatrix[0].length,
				"The position you want to add the columns is bigger than the table size ts=" + dataMatrix[0].length + " position= " + startposition);

		final List<MeasureInfo> measures = getMeasures();
		final int measureSize = measures.size();

		Placeholder/* <?> */[][] newData = new Placeholder[dataMatrix.length][];
		for (int i = 0; i < dataMatrix.length; i++) {
			newData[i] = new Placeholder/* <?> */[dataMatrix[0].length + colums.size()];
		}

		int columnsToAddSize = colums.size();
		for (int i = 0; i < dataMatrix.length; i++) {

			// Add the column before the new one
			for (int x = 0; x < startposition; x++) {
				newData[i][x] = dataMatrix[i][x];
			}

			// Add the new column
			for (int x = 0; x < columnsToAddSize; x++) {
				Placeholder/* <?> */ newPlaceholder = null;
				int index = (!measuresOnRow) ? x % measureSize : i % measureSize;
				MeasureInfo measureInfo = measures.get(index);

				Node currRow = this.rowsRoot.getLeafs().get(i);
				Node currCol = currNode;
				String path = createJsonPathQueryFromNodes(currRow, currCol, measureInfo);
				newPlaceholder = new JsonPathAggregatorPlaceholder(this, measureInfo, getAggregatorDelegate(measureInfo), type, path);

				newData[i][startposition + x] = newPlaceholder;
			}

			// Add the column after the new one
			for (int x = 0; x < dataMatrix[0].length - startposition; x++) {
				newData[i][startposition + columnsToAddSize + x] = dataMatrix[i][startposition + x];
			}

		}

		// update the list of columns type
		for (int i = 0; i < colums.size(); i++) {
			celltypeOfColumns.add(i + startposition, type);
		}

		dataMatrix = newData;

		if (type.equals(CellType.SUBTOTAL)) {
			if (!measuresOnRow) {
				for (int j = 0; j < columnsSum.size(); j++) {
					String[] aColumnsSum = new String[columnsSum.get(j).length + colums.size()];
					for (int y = 0; y < startposition; y++) {
						aColumnsSum[y] = columnsSum.get(j)[y];
					}

					for (int y = 0; y < colums.size(); y++) {
						aColumnsSum[startposition + y] = "" + getTotalsOfColumn(startposition + y, CellType.SUBTOTAL);
					}

					for (int y = startposition; y < columnsSum.get(j).length; y++) {
						aColumnsSum[y + colums.size()] = columnsSum.get(j)[y];
					}
					columnsSum.set(j, aColumnsSum);
				}
			} else {
				// colums.size() is 1
				String[] subtotal = getTotalsOfColumnWithMeasure(startposition);
				for (int j = 0; j < columnsSum.size(); j++) {

					String[] aColumnsSum = new String[columnsSum.get(j).length + 1];
					for (int y = 0; y < startposition; y++) {
						aColumnsSum[y] = columnsSum.get(j)[y];
					}

					aColumnsSum[startposition] = subtotal[j];

					for (int y = startposition; y < columnsSum.get(j).length; y++) {
						aColumnsSum[y + 1] = columnsSum.get(j)[y];
					}
					columnsSum.set(j, aColumnsSum);
				}
			}

		}

	}

	/**
	 * Inserts rows in the crosstab data matrix
	 *
	 * @param startposition the position where insert the rows into the matrix
	 * @param colums        the lines to insert
	 * @param type          the type of the data
	 */
	public void addCrosstabDataRow(Node currNode, int startposition, List<String[]> rows, CellType type) {
		Assert.assertNotNull(dataMatrix, "The data matrix must not be null");
		Assert.assertTrue(startposition <= dataMatrix.length,
				"The position you want to add the rows is bigger than the table size ts=" + dataMatrix[0].length + " position= " + startposition);

		final List<MeasureInfo> measures = getMeasures();
		final int measureSize = measures.size();
		final int rowsToAddSize = rows.size();
		final int colsToAddSize = rows.isEmpty() ? 0 : rows.get(0).length;

		Placeholder/* <?> */[][] newData = new Placeholder[dataMatrix.length + rows.size()][];
		for (int i = 0; i < (dataMatrix.length + rows.size()); i++) {
			newData[i] = new Placeholder/* <?> */[rows.get(0).length];
		}

		// Add rows before the new one
		for (int x = 0; x < startposition; x++) {
			newData[x] = dataMatrix[x];
		}

		// Add the new rows
		for (int x = 0; x < rowsToAddSize; x++) {
			newData[startposition + x] = new Placeholder/* <?> */[colsToAddSize];
			for (int j = 0; j < colsToAddSize; j++) {
				Placeholder/* <?> */ newPlaceholder = null;
				int index = j /* % measureSize */;
				MeasureInfo measureInfo = measures.get(!measuresOnRow ? j % measureSize : x);

				Node currRow = currNode;
				Node currCol = this.columnsRoot.getLeafs().get(index);
				String path = createJsonPathQueryFromNodes(currRow, currCol, measureInfo);
				newPlaceholder = new JsonPathAggregatorPlaceholder(this, measureInfo, getAggregatorDelegate(measureInfo), type, path);

				newData[startposition + x][j] = newPlaceholder;
			}
		}

		// Add rows after the new one
		for (int x = 0; x < dataMatrix.length - startposition; x++) {
			newData[startposition + rowsToAddSize + x] = dataMatrix[startposition + x];
		}

		// Update the list of rows type
		for (int i = 0; i < rows.size(); i++) {
			celltypeOfRows.add(i + startposition, type);
		}

		// Replace the original matrix
		dataMatrix = newData;

		if (type.equals(CellType.SUBTOTAL)) {
			if (measuresOnRow) {
				for (int j = 0; j < rowsSum.size(); j++) {
					String[] aRowsSum = new String[rowsSum.get(j).length + rows.size()];
					for (int y = 0; y < startposition; y++) {
						aRowsSum[y] = rowsSum.get(j)[y];
					}

					for (int y = 0; y < rows.size(); y++) {
						aRowsSum[startposition + y] = "" + getTotalsOfRow(startposition + y, CellType.SUBTOTAL);
					}

					for (int y = startposition; y < rowsSum.get(j).length; y++) {
						aRowsSum[y + rows.size()] = rowsSum.get(j)[y];
					}
					rowsSum.set(j, aRowsSum);
				}
			} else {
				// colums.size() is 1
				String[] subtotal = getTotalsOfRowWithMeasure(startposition);
				for (int j = 0; j < rowsSum.size(); j++) {
					String[] aRowsSum = new String[rowsSum.get(j).length + rows.size()];
					for (int y = 0; y < startposition; y++) {
						aRowsSum[y] = rowsSum.get(j)[y];
					}

					aRowsSum[startposition] = subtotal[j];

					for (int y = startposition; y < rowsSum.get(j).length; y++) {
						aRowsSum[y + rows.size()] = rowsSum.get(j)[y];
					}
					rowsSum.set(j, aRowsSum);
				}
			}
		}
	}

	/**
	 * Get the CellType of the cell
	 *
	 * @param row    the row
	 * @param column the column
	 * @return the celltype of the cell
	 */
	public CellType getCellType(int row, int column) {
		CellType cellCellType;
		CellType rowCellType = celltypeOfRows.get(row);
		CellType columnCellType = celltypeOfColumns.get(column);
		cellCellType = rowCellType;
		if (columnCellType.compareTo(rowCellType) > 0) {
			cellCellType = columnCellType;
		}
		return cellCellType;
	}

	public boolean isCellFromSubtotalsColumn(int column) {
		CellType columnType = celltypeOfColumns.get(column);
		if (columnType.getValue().equalsIgnoreCase("partialsum"))
			return true;
		else
			return false;
	}

	public boolean isCellFromTotalsColumn(int column) {
		CellType columnType = celltypeOfColumns.get(column);
		if (columnType.getValue().equalsIgnoreCase("totals"))
			return true;
		else
			return false;
	}

	public Node getColumnsRoot() {
		return columnsRoot;
	}

	public Node getRowsRoot() {
		return rowsRoot;
	}

	public int getTotalNumberOfRows() {
		int columnsDepth = this.getColumnsRoot().getSubTreeDepth();
		int rowsNumber = this.getDataMatrix().length;
		// + 1 because there may be also the bottom row with the totals
		return columnsDepth + rowsNumber + 1;
	}

	/**
	 * TODO
	 */
	public String[][] getDataMatrix() {
		String[][] ret = null;

		int rowsCount = dataMatrix.length;
		int colsCount = dataMatrix[0].length;

		ret = new String[rowsCount][colsCount];

		for (int row = 0; row < rowsCount; row++) {
			for (int col = 0; col < colsCount; col++) {
				ret[row][col] = dataMatrix[row][col].getValueAsString();
			}
		}

		return ret;
	}

	public Placeholder[][] getPlaceholderDataMatrix() {
		return dataMatrix;
	}

	public List<String> getRowCordinates() {
		return rowCordinates;
	}

	public void setRowCordinates(List<String> rowCordinates) {
		this.rowCordinates = rowCordinates;
	}

	public List<String> getColumnCordinates() {
		return columnCordinates;
	}

	/**
	 * @param columnCordinates the columnCordinates to set
	 */
	public void setColumnCordinates(List<String> columnCordinates) {
		this.columnCordinates = columnCordinates;
	}

	/**
	 * @return the measuresCordinates
	 */
	public List<String> getMeasuresCordinates() {
		return measuresCordinates;
	}

	/**
	 * @return the columnsSpecification
	 */
	public List<String> getColumnsSpecification() {
		return columnsSpecification;
	}

	/**
	 * @param columnsSpecification the columnsSpecification to set
	 */
	public void setColumnsSpecification(List<String> columnsSpecification) {
		this.columnsSpecification = columnsSpecification;
	}

	/**
	 * @return the rowsSpecification
	 */
	public List<String> getRowsSpecification() {
		return rowsSpecification;
	}

	/**
	 * @param rowsSpecification the rowsSpecification to set
	 */
	public void setRowsSpecification(List<String> rowsSpecification) {
		this.rowsSpecification = rowsSpecification;
	}

	/**
	 * @param measuresCordinates the measuresCordinates to set
	 */
	public void setMeasuresCordinates(List<String> measuresCordinates) {
		this.measuresCordinates = measuresCordinates;
	}

	private void addMeasuresScaleFactor() {
		for (int i = 0; i < measures.size(); i++) {
			MeasureInfo measure = measures.get(i);
			measure.setScaleFactor("NONE");
		}
	}

	public List<String> getRowHeadersTitles() {
		if (rowHeadersTitles == null) {
			rowHeadersTitles = new ArrayList<String>();
			List<CrosstabDefinition.Row> rows = crosstabDefinition.getRows();
			for (int i = 0; i < rows.size(); i++) {
				rowHeadersTitles.add(rows.get(i).getAlias());
			}
		}
		return rowHeadersTitles;
	}

	/**
	 * @return the columnsHeaderList
	 */
	public List<String> getColumnsHeaderList() {
		return columnsHeaderList;
	}

	/**
	 * @param columnsHeaderList the columnsHeaderList to set
	 */
	public void setColumnsHeaderList(List<String> columnsHeaderList) {
		this.columnsHeaderList = columnsHeaderList;
	}

	/**
	 * @return the rowsHeaderList
	 */
	public List<String> getRowsHeaderList() {
		return rowsHeaderList;
	}

	/**
	 * @param rowsHeaderList the rowsHeaderList to set
	 */
	public void setRowsHeaderList(List<String> rowsHeaderList) {
		this.rowsHeaderList = rowsHeaderList;
	}

	/**
	 * @return the columnsHeaderIdList
	 */
	public List<String> getColumnsHeaderIdList() {
		return columnsHeaderIdList;
	}

	/**
	 * @param columnsHeaderIdList the columnsHeaderIdList to set
	 */
	public void setColumnsHeaderIdList(List<String> columnsHeaderIdList) {
		this.columnsHeaderIdList = columnsHeaderIdList;
	}

	/**
	 * @return the rowsHeaderIdList
	 */
	public List<String> getRowsHeaderIdList() {
		return rowsHeaderIdList;
	}

	/**
	 * @param rowsHeaderIdList the rowsHeaderIdList to set
	 */
	public void setRowsHeaderIdList(List<String> rowsHeaderIdList) {
		this.rowsHeaderIdList = rowsHeaderIdList;
	}

	public List<MeasureInfo> getMeasures() {
		return measures;
	}

	public List<MeasureInfo> getMeasuresToShowOnTotalsOrSubTotals() {
		return measuresToShowOnTotalsOrSubTotals;
	}

	/**
	 * @return the celltypeOfColumns
	 */
	public List<CellType> getCelltypeOfColumns() {
		return celltypeOfColumns;
	}

	public int getOffsetInColumnSubtree(int colIdx) {
		int i = 0;
		while (i + getColumnSubtreeNumberOfLeaves(i) <= colIdx) {
			i = i + getColumnSubtreeNumberOfLeaves(i);
		}
		return colIdx - i;
	}

	/**
	 * Returns the number of leaves of the current subtree: Takes the root node of the column tree, visits the first level in depth, and return the number of
	 * leaves of this first level
	 *
	 * @return the number of leaves
	 */
	private int getColumnSubtreeNumberOfLeaves(int colIdx) {
		if (columnsHeaderList.size() == 0) {
			// i don't have any columns defined
			return measures.size();
		}

		if (treeLeaves == null)
			initTreeLeavesList();

		Node n = treeLeaves.get(colIdx);
		return n.getFirstAncestor().getLeafsNumber();
	}

	private void initTreeLeavesList() {
		treeLeaves = new ArrayList<Node>();
		recursiveAddLeaves(columnsRoot);
	}

	private void recursiveAddLeaves(Node n) {
		if (n == null)
			return;

		List<Node> children = n.getChildren();

		if (children == null || children.size() == 0) {
			treeLeaves.add(n);
			return;
		}

		for (int i = 0; i < children.size(); i++) {
			recursiveAddLeaves(children.get(i));
		}
	}

//	/**
//	 * @param celltypeOfColumns
//	 *            the celltypeOfColumns to set
//	 */
//	public void setCelltypeOfColumns(List<CellType> celltypeOfColumns) {
//		this.celltypeOfColumns = celltypeOfColumns;
//	}
//
//	/**
//	 * @return the celltypeOfRows
//	 */
//	public List<CellType> getCelltypeOfRows() {
//		return celltypeOfRows;
//	}
//
//	/**
//	 * @param celltypeOfRows
//	 *            the celltypeOfRows to set
//	 */
//	public void setCelltypeOfRows(List<CellType> celltypeOfRows) {
//		this.celltypeOfRows = celltypeOfRows;
//	}

	public boolean isMeasureOnRow() {
		try {
			return config.getString("measureson").equals("rows");
		} catch (Exception e) {
			logger.error("Error reading the configuration of the crosstab", e);
			throw new SpagoBIRuntimeException("Error reading the configuration of the crosstab", e);
		}
	}

	public boolean isHideZeroRows() {
		return hideZeroRows;
	}

	public boolean isFixedColumn() {
		return fixedColumn;
	}

	public boolean isExpandCollapseRows() {
		return expandCollapseRows;
	}

	public boolean isCalculateTotalsOnColumns() {
		return config.optBoolean("calculatetotalsoncolumns");
	}

	public boolean isCalculateTotalsOnRows() {
		return config.optBoolean("calculatetotalsonrows");
	}

	public String getColumnAliasFromName(String columnName) {
		return dsColumnName2Alias.get(columnName);
	}

	public String getColumnNameFromAlias(String columnName) {
		return alias2DsColumnName.get(columnName);
	}

	public String getHTMLCrossTab(Locale locale) {
		CrossTabHTMLSerializer serializer = new CrossTabHTMLSerializer(locale, myGlobalId, columnsSortKeysMap, rowsSortKeysMap, measuresSortKeysMap, null);
		String html = serializer.serialize(this);
		return html;
	}

	public String getHTMLCrossTab(Locale locale, JSONObject variables) {
		CrossTabHTMLSerializer serializer = new CrossTabHTMLSerializer(locale, myGlobalId, columnsSortKeysMap, rowsSortKeysMap, measuresSortKeysMap, variables);
		String html = serializer.serialize(this);
		return html;
	}

	public CrosstabDefinition getCrosstabDefinition() {
		return crosstabDefinition;
	}

	private Map<String, Double> sortMeasures(Map<Integer, NodeComparator> sortKeys, Map<String, Double> values, int rowsCount, int columnsCount, int totRows,
			int totColumns) {
		Map<String, Double> valuesCopy = new HashMap<String, Double>(values);
		Map toReturn = new LinkedHashMap<String, Double>();
		List valuesToOrder = new ArrayList();

		ValueComparator comparator = null;
		if (sortKeys != null) {
			int idx = 0;
			Iterator<Integer> mapIter = sortKeys.keySet().iterator();
			while (mapIter.hasNext()) {
				Object field = mapIter.next();
				idx = (Integer) field;
			}
			// only a measure at time could sort values
			comparator = new ValueComparator(sortKeys.get(idx).getDirection());
		}

		// sort measure on rows
		for (int c = 0; c < values.size(); c++) {
			List valuesForCategory = getRowsCategoryValues(valuesCopy, rowsCount);
			if (valuesForCategory.size() == 0) {
//				valuesToOrder.add(new Double("0")); //no value to order
				continue;
			}
			if (valuesForCategory.size() > 0 && comparator != null) {
				Collections.sort(valuesForCategory, comparator);
			} else {
				Collections.sort(valuesForCategory);
			}
			valuesToOrder.addAll(valuesForCategory); // add ordered sublist
			c = valuesToOrder.size() - 1; // update counter
		}

		// reproduce order to original map
		for (int v = 0; v < valuesToOrder.size(); v++) {
			Double value = (Double) valuesToOrder.get(v);
			String valueLabel = getLabelFromValue(value, values);
			toReturn.put(valueLabel, value);
		}

		return toReturn;
	}

	/**
	 * Returns the sorted sub-list on values considering the father category's value
	 *
	 * @param key
	 * @param values
	 * @param numCategories
	 * @return
	 */
	private List getRowsCategoryValues(Map<String, Double> values, int numCategories) {
		List toReturn = new ArrayList();
		Map<String, Double> valuesCopy = (Map<String, Double>) ((HashMap<String, Double>) values).clone();

		String parentValue = "";
		for (String key : valuesCopy.keySet()) {
			String[] measureInfo = key.split(PATH_SEPARATOR);

			if (parentValue.equals("") && numCategories > 1)
				parentValue = measureInfo[numCategories - 1]; // parentValue is setted only with more categories
			if (!parentValue.equals("") && measureInfo[numCategories - 1].equalsIgnoreCase(parentValue)) {
				// if it's last category level and the parent is the same add value to the list, else returns the list
				toReturn.add(valuesCopy.get(key));
				values.remove(key);
			} else if (numCategories == 1) {
				// only one category case: put directly the value in the list to order
				toReturn.add(valuesCopy.get(key));
				values.remove(key);
			}
			// else
			// break; //it must continue because the input list coudn't be sorted on previous categories
		}

		return toReturn;
	}

	private String getLabelFromValue(Double value, Map<String, Double> originalValues) {
		String toReturn = null;
		for (String key : originalValues.keySet()) {
			Double originalValue = originalValues.get(key);
			if (originalValue == value) {
				toReturn = key;
				break;
			}
		}

		return toReturn;
	}

	private int getColumnIndex(String columnLabel, List measuresHeaderList) {
		int toReturn = -1;

		for (int l = 0; l < measuresHeaderList.size(); l++) {
			if (columnLabel.equalsIgnoreCase((String) measuresHeaderList.get(l))) {
				toReturn = l;
				break;
			}
		}
		return toReturn;
	}

	/**
	 * @return the parsedValuesDataStore
	 */
	public DocumentContext getParsedValuesDataStore() {
		return parsedValuesDataStore;
	}

	/**
	 * @return the countMeasures
	 */
	public List<String> getCountMeasures() {
		return countMeasures;
	}

}
