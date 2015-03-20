/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.services.registry;

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelField;
import it.eng.qbe.model.structure.IModelStructure;
import it.eng.qbe.query.CriteriaConstants;
import it.eng.qbe.query.ExpressionNode;
import it.eng.qbe.query.Query;
import it.eng.qbe.query.WhereField;
import it.eng.qbe.statement.AbstractQbeDataSet;
import it.eng.qbe.statement.AbstractStatement;
import it.eng.qbe.statement.IStatement;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.engines.qbe.QbeEngineConfig;
import it.eng.spagobi.engines.qbe.QbeEngineInstance;
import it.eng.spagobi.engines.qbe.registry.bo.RegistryConfiguration;
import it.eng.spagobi.engines.qbe.registry.bo.RegistryConfiguration.Column;
import it.eng.spagobi.engines.qbe.registry.bo.RegistryConfiguration.Filter;
import it.eng.spagobi.engines.qbe.registry.parser.RegistryConfigurationXMLParser;
import it.eng.spagobi.engines.qbe.registry.serializer.RegistryJSONDataWriter;
import it.eng.spagobi.engines.qbe.services.core.ExecuteQueryAction;
import it.eng.spagobi.engines.qbe.template.QbeTemplate;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.Field;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.datastore.Record;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.utilities.ParametersDecoder;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 * 
 */

public class LoadRegistryAction extends ExecuteQueryAction {

	private static final long serialVersionUID = -642121076148276452L;
	private final String ID_COLUMN = "ID_COLUMN";

	private final JSONArray mandatories = new JSONArray();
	private final JSONArray columnsInfos = new JSONArray();
	private String columnMaxSize = null;

	// all cells that must be colored as total
	private JSONArray summaryColorCellsArray = new JSONArray();
	// all value cells containing toal values
	private JSONArray summaryCellsArray = new JSONArray();

	private int summaryRecordsAddedCounter = 0;

	RegistryConfiguration registryConfig = null;

	@Override
	public Query getQuery() {
		Query query = buildQuery();
		return query;
	}

	@Override
	protected IStatement getStatement(Query query) {
		IStatement statement = getDataSource().createStatement(query);
		return statement;
	}

	@Override
	public void service(SourceBean request, SourceBean response) {

		try {
			if (!request.containsAttribute(START))
				request.setAttribute(START, new Integer(0));
			if (!request.containsAttribute(LIMIT))
				request.setAttribute(LIMIT, Integer.MAX_VALUE);
		} catch (SourceBeanException e) {
			throw new SpagoBIEngineServiceException(getActionName(), e);
		}
		super.service(request, response);
	}

	public boolean hasRegistryTotalLines() {
		Iterator<Column> it = registryConfig.getColumns().iterator();
		boolean found = false;
		while (it.hasNext() && !found) {
			Column column = it.next();
			String sfO = column.getSummaryFunction();
			if (sfO != null && sfO.equalsIgnoreCase("sum")) {
				found = true;
			}
		}
		return found;
	}

	@Override
	public IDataStore executeQuery(Integer start, Integer limit) {
		IDataStore dataStore = null;
		IDataSet dataSet = this.getEngineInstance().getActiveQueryAsDataSet();
		AbstractQbeDataSet qbeDataSet = (AbstractQbeDataSet) dataSet;
		IStatement statement = qbeDataSet.getStatement();
		// QueryGraph graph = statement.getQuery().getQueryGraph();
		boolean valid = true; // GraphManager.getGraphValidatorInstance(QbeEngineConfig.getInstance().getGraphValidatorImpl()).isValid(graph,
								// statement.getQuery().getQueryEntities(getDataSource()));
		// logger.debug("QueryGraph valid = " + valid);
		if (!valid) {
			throw new SpagoBIEngineServiceException(getActionName(), "error.mesage.description.relationship.not.enough");
		}
		try {
			logger.debug("Executing query ...");
			Integer maxSize = QbeEngineConfig.getInstance().getResultLimit();
			logger.debug("Configuration setting  [" + "QBE.QBE-SQL-RESULT-LIMIT.value" + "] is equals to [" + (maxSize != null ? maxSize : "none") + "]");
			String jpaQueryStr = statement.getQueryString();
			logger.debug("Executable query (HQL/JPQL): [" + jpaQueryStr + "]");
			UserProfile userProfile = (UserProfile) getEnv().get(EngineConstants.ENV_USER_PROFILE);
			auditlogger.info("[" + userProfile.getUserId() + "]:: HQL/JPQL: " + jpaQueryStr);
			auditlogger.info("[" + userProfile.getUserId() + "]:: SQL: " + statement.getSqlQueryString());

			int startI = start;
			int limitI = (limit == null ? (maxSize == null ? -1 : maxSize) : limit);
			int maxI = (maxSize == null ? -1 : maxSize.intValue());
			dataSet.loadData(startI, limitI, maxI);
			dataStore = dataSet.getDataStore();
			changeAlias(dataStore);
			Assert.assertNotNull(dataStore, "The dataStore returned by loadData method of the class [" + dataSet.getClass().getName() + "] cannot be null");
		} catch (Exception e) {
			logger.debug("Query execution aborted because of an internal exceptian");
			SpagoBIEngineServiceException exception;
			String message;

			message = "An error occurred in " + getActionName() + " service while executing query: [" + statement.getQueryString() + "]";
			exception = new SpagoBIEngineServiceException(getActionName(), message, e);
			exception.addHint("Check if the query is properly formed: [" + statement.getQueryString() + "]");
			exception.addHint("Check connection configuration");
			exception.addHint("Check the qbe jar file");

			throw exception;
		}
		logger.debug("Query executed succesfully");
		return dataStore;
	}

	@Override
	public JSONObject serializeDataStore(IDataStore dataStore) {
		logger.debug("IN");
		summaryRecordsAddedCounter = 0;
		// add the rows of summarization
		// only if there is a column with summaryFunction
		if (hasRegistryTotalLines()) {
			logger.debug("add summary lines");
			addSumRows(dataStore);
		} else {
			logger.debug("no need of summary lines");
		}

		RegistryJSONDataWriter dataSetWriter = new RegistryJSONDataWriter();
		JSONObject gridDataFeed = (JSONObject) dataSetWriter.write(dataStore);
		setMandatoryMetadata(gridDataFeed);
		setColumnMaxSize(gridDataFeed);
		setColumnsInfos(gridDataFeed);
		setSummaryInfos(gridDataFeed);
		setSummaryColorInfos(gridDataFeed);

		logger.debug("OUT");

		return gridDataFeed;
	}

	// tret Integer, Float and BigDecimal
	public Object operateWithNumbers(Object previousValue, Object currentValue) {
		logger.debug("IN");
		Object resultToReturn = null;

		if (currentValue instanceof Integer) {
			Integer currVal = (Integer) currentValue;
			Integer prevVal = (Integer) previousValue;
			Integer result = currVal + prevVal;
			resultToReturn = result;
		} else if (currentValue instanceof Float) {
			Float currValF = (Float) currentValue;
			Float prevValF = (Float) previousValue;
			// have to pass through BigDecimal because java Math sometimes break on decimal count with float and double
			BigDecimal currValB = new BigDecimal(currValF.doubleValue());
			BigDecimal prevValB = new BigDecimal(prevValF.doubleValue());
			BigDecimal newValB = prevValB.add(currValB);
			newValB = newValB.setScale(2, RoundingMode.CEILING);
			Float result = new Float(newValB.floatValue());
			resultToReturn = result;
		} else if (currentValue instanceof Double) {
			Double currValD = (Double) currentValue;
			Double prevValD = (Double) previousValue;
			// have to pass through BigDecimal because java Math sometimes break on decimal count with float and double
			BigDecimal currValB = new BigDecimal(currValD.doubleValue());
			BigDecimal prevValB = new BigDecimal(prevValD.doubleValue());
			BigDecimal newValB = prevValB.add(currValB);
			newValB = newValB.setScale(2, RoundingMode.CEILING);
			Double result = new Double(newValB.doubleValue());
			resultToReturn = result;
		} else if (currentValue instanceof BigDecimal) {
			BigDecimal currVal = (BigDecimal) currentValue;
			BigDecimal prevVal = (BigDecimal) previousValue;
			BigDecimal result = prevVal.add(currVal);
			result = result.setScale(2, RoundingMode.CEILING);
			resultToReturn = result;
		} else {
			logger.error("Error in measure type: trying to sum a value that is not a number");
			return null;
		}

		logger.debug("OUT");
		return resultToReturn;
	}

	private void changeAlias(IDataStore dataStore) {
		logger.debug("IN");
		IMetaData metaData = dataStore.getMetaData();

		for (int i = 0; i < metaData.getFieldCount(); i++) {
			IFieldMetaData meta = metaData.getFieldMeta(i);
			Column col = registryConfig.getColumnConfiguration(meta.getAlias());
			if (col.getTitle() != null) {
				meta.setAlias(col.getTitle());
				// metaData.changeFieldAlias(i, col.getTitle());
				logger.debug("Changed alias of column " + meta.getName() + " to " + col.getTitle());
			}
		}

		logger.debug("OUT");
	}

	private void addSumRows(IDataStore dataStore) {
		logger.debug("IN");

		summaryColorCellsArray = new JSONArray();
		summaryCellsArray = new JSONArray();

		ArrayList<Integer> columnsIndexToMerge = new ArrayList<Integer>();
		ArrayList<Integer> columnsIndexToEmpty = new ArrayList<Integer>();
		ArrayList<Integer> columnsIndexToAfter = new ArrayList<Integer>();
		HashMap<Integer, Object> columnsIndexToSum2Counter = new HashMap<Integer, Object>();

		// collect columns to merge and columns to sum and colummsn to empty:
		// -- columns to merge have merge attributes until a columns with summaryFunc is found
		// then other columns that have merge attribute but no
		List<Column> columns = registryConfig.getColumns();

		Integer index = 0;
		boolean summaryFuncFound = false;
		boolean measureFound = false;

		for (Iterator iterator = columns.iterator(); iterator.hasNext();) {
			Column column = (Column) iterator.next();

			if (column.isMerge() && summaryFuncFound == false) {
				columnsIndexToMerge.add(index);
			} else if (summaryFuncFound == true && !column.isMeasure() && !measureFound) {
				columnsIndexToEmpty.add(index);
			} else if (summaryFuncFound == true && !column.isMeasure() && measureFound) {
				columnsIndexToAfter.add(index);
			} else if (column.isMeasure()) {
				columnsIndexToSum2Counter.put(index, 0);
				measureFound = true;
			}
			if (column.getSummaryFunction() != null && column.getSummaryFunction().equals("sum"))
				summaryFuncFound = true;
			index++;
		}

		// Map to store previous merge values on iteration
		HashMap<Integer, Object> previousMergeValues = new HashMap<Integer, Object>();
		for (Iterator iterator = columnsIndexToMerge.iterator(); iterator.hasNext();) {
			Integer columnIndex = (Integer) iterator.next();
			previousMergeValues.put(columnIndex, null);
		}

		TreeMap<Integer, Record> recordsToAddMap = new TreeMap<Integer, Record>();

		int sumCounter = 0; // add total row only if grouping has more than one member

		// iterate on each store row
		for (int i = 0; i < dataStore.getRecordsCount(); i++) {
			IRecord record = dataStore.getRecordAt(i);

			// get current values of column to merge
			HashMap<Integer, Object> currentMergeValues = new HashMap<Integer, Object>();

			// iterate on each column to merge and store values
			for (Iterator iterator = columnsIndexToMerge.iterator(); iterator.hasNext();) {
				Integer columnIndex = (Integer) iterator.next();
				Object value = record.getFieldAt(columnIndex).getValue();
				currentMergeValues.put(columnIndex, value);
			}

			// compare current values with previous ones
			boolean isEqual = compareValuesMaps(previousMergeValues, currentMergeValues);

			// if merging goes on update counters else add summarization line
			if (isEqual) {
				sumCounter++;
				for (Iterator iterator = columnsIndexToSum2Counter.keySet().iterator(); iterator.hasNext();) {
					Integer indexMeasure = (Integer) iterator.next();
					Object value = record.getFieldAt(indexMeasure).getValue();

					// TODO treat the case this is not a number, should keep it to null
					if (value != null) {
						// get previous value

						Object result = operateWithNumbers(columnsIndexToSum2Counter.get(indexMeasure), value);

						columnsIndexToSum2Counter.put(indexMeasure, result);
					} else {
						columnsIndexToSum2Counter.put(indexMeasure, null);
					}

				}
			} else {
				// breaking point, add summarization lines at previous index; i-1

				// add a new record only if sumCounter > 0
				if (sumCounter > 0) {
					addTotalRecord(dataStore, i, columnsIndexToMerge, columnsIndexToEmpty, columnsIndexToAfter, columnsIndexToSum2Counter, previousMergeValues,
							recordsToAddMap);
				}

				// put the counters to actual values
				for (Iterator iterator = columnsIndexToSum2Counter.keySet().iterator(); iterator.hasNext();) {
					Integer columnInd = (Integer) iterator.next();
					Object v = record.getFieldAt(columnInd).getValue();
					columnsIndexToSum2Counter.put(columnInd, v);
				}

				sumCounter = 0;
			}

			// update previousValues
			previousMergeValues = currentMergeValues;
		}

		// add final total if last records were merged
		if (sumCounter > 0) {
			addTotalRecord(dataStore, null, columnsIndexToMerge, columnsIndexToEmpty, columnsIndexToAfter, columnsIndexToSum2Counter, previousMergeValues,
					recordsToAddMap);
		}

		// finally add the record (could not add them while cycling the store)
		for (Iterator iterator = recordsToAddMap.keySet().iterator(); iterator.hasNext();) {
			Integer indexR = (Integer) iterator.next();
			Record rec = recordsToAddMap.get(indexR);
			if (indexR == -1) {
				dataStore.appendRecord(rec);
			} else {
				dataStore.insertRecord(indexR, rec);
			}
		}

		logger.debug("OUT");
	}

	private void addTotalRecord(IDataStore dataStore, Integer currentIndexRow, ArrayList<Integer> columnsIndexToMerge, ArrayList<Integer> columnsIndexToEmpty,
			ArrayList<Integer> columnsIndexToAfter, HashMap<Integer, Object> columnsIndexToSum2Counter, HashMap<Integer, Object> previousMergeValues,
			TreeMap<Integer, Record> recordsToAddMap) {
		logger.debug("IN");
		Record recordNew = new Record();

		// initialize the record
		int colCount = dataStore.getMetaData().getFieldCount();
		for (int j = 0; j < colCount; j++) {
			Field field = new Field("");
			recordNew.insertField(j, field);
		}

		Integer index = currentIndexRow == null ? null : currentIndexRow + summaryRecordsAddedCounter;

		summaryRecordsAddedCounter++;

		// insert fields for each column to merge
		for (Iterator iterator = columnsIndexToMerge.iterator(); iterator.hasNext();) {
			Integer columnIndex = (Integer) iterator.next();
			Field field = new Field();
			Object valueToPut = previousMergeValues.get(columnIndex);
			field.setValue(valueToPut);
			recordNew.insertField(columnIndex, field);
		}

		// insert field for each column to empty
		for (Iterator iterator = columnsIndexToEmpty.iterator(); iterator.hasNext();) {
			Integer columnIndex = (Integer) iterator.next();
			Field field = new Field();
			field.setValue("      ");
			recordNew.insertField(columnIndex, field);
			setNewSummaryColorCell(dataStore, index, columnIndex);
		}
		// insert field for each column to after but do not color
		for (Iterator iterator = columnsIndexToEmpty.iterator(); iterator.hasNext();) {
			Integer columnIndex = (Integer) iterator.next();
			Field field = new Field();
			field.setValue("      ");
			recordNew.insertField(columnIndex, field);
		}

		// insert fields for each column to sum
		for (Iterator iterator = columnsIndexToSum2Counter.keySet().iterator(); iterator.hasNext();) {
			Integer columnIndex = (Integer) iterator.next();
			Field field = new Field();
			Object valueToPut = columnsIndexToSum2Counter.get(columnIndex);
			field.setValue(valueToPut);
			recordNew.insertField(columnIndex, field);

			// add coordinates of summary cell
			setNewSummaryColorCell(dataStore, index, columnIndex);
			setNewSummaryCell(dataStore, index, columnIndex);

		}

		if (currentIndexRow != null) {
			// dataStore.insertRecord(currentIndex, recordNew);
			// index to put must be the currentIndexRow + the number of records that have already been added

			recordsToAddMap.put(index, recordNew);
		} else {
			// dataStore.appendRecord(recordNew);
			recordsToAddMap.put(-1, recordNew);
		}
		logger.debug("OUT");

	}

	private boolean compareValuesMaps(Map<Integer, Object> previousValues, Map<Integer, Object> currentValues) {
		logger.debug("IN");
		boolean isEqual = true;
		for (Iterator iterator = currentValues.keySet().iterator(); iterator.hasNext() && isEqual;) {
			Integer index = (Integer) iterator.next();
			Object currValue = currentValues.get(index);
			Object prevValue = previousValues.get(index);

			if ((currValue == null && prevValue == null) || (currValue != null && currValue.equals(prevValue))) {
			} else {
				isEqual = false;
			}
		}

		logger.debug("OUT");
		return isEqual;
	}

	private void setSummaryColorInfos(JSONObject gridDataFeed) {
		try {
			((JSONObject) gridDataFeed.get("metaData")).put("summaryColorCellsCoordinates", summaryColorCellsArray);

		} catch (JSONException e) {
			logger.error("Error setting summary color cells coordinates" + e.getMessage());
		}
	}

	private void setSummaryInfos(JSONObject gridDataFeed) {
		try {
			((JSONObject) gridDataFeed.get("metaData")).put("summaryCellsCoordinates", summaryCellsArray);

		} catch (JSONException e) {
			logger.error("Error setting summary cells coordinates" + e.getMessage());
		}
	}

	private void setMandatoryMetadata(JSONObject gridDataFeed) {
		try {
			((JSONObject) gridDataFeed.get("metaData")).put("mandatory", mandatories);

		} catch (JSONException e) {
			logger.error("Error  setting mandatory informations " + e.getMessage());
		}
	}

	private void setColumnsInfos(JSONObject gridDataFeed) {
		try {
			((JSONObject) gridDataFeed.get("metaData")).put("columnsInfos", columnsInfos);

		} catch (JSONException e) {
			logger.error("Error setting columns size informations " + e.getMessage());
		}
	}

	private void setColumnMaxSize(JSONObject gridDataFeed) {
		try {
			((JSONObject) gridDataFeed.get("metaData")).put("maxSize", columnMaxSize);

		} catch (JSONException e) {
			logger.error("Error setting max columns size informations " + e.getMessage());
		}
	}

	private void getMandatoryMetadata(Column column) {
		try {
			// mandatory management
			String mandatoryColumn = column.getMandatoryColumn();
			String mandatoryValue = column.getMandatoryValue();
			JSONObject mandatory = new JSONObject();

			if (mandatoryColumn != null && mandatoryValue != null) {
				mandatory.put("mandatoryColumn", mandatoryColumn);
				mandatory.put("mandatoryValue", mandatoryValue);
				mandatory.put("column", column.getField());
				mandatories.put(mandatory);
			}
		} catch (JSONException e) {
			logger.error("Error getting mandatory informations from template " + e.getMessage());
		}
	}

	// set in array cell that will be colored as total
	private void setNewSummaryColorCell(IDataStore dataStore, Integer row, Integer column) {
		if (row == null) {
			// if row not specified means is referring to last row that will be store lenght + already added summary rows length
			row = Long.valueOf(dataStore.getRecordsCount()).intValue() + summaryRecordsAddedCounter - 1; // row starts from 0
		}

		try {
			JSONObject obj = new JSONObject();
			obj.put("row", row);
			obj.put("column", column);
			summaryColorCellsArray.put(obj);
		} catch (JSONException e) {
			logger.error("Error while tracing summary cell in row " + row + " and column " + column + ": " + e.getMessage());
		}
	}

	// set in array cells that will contain total value
	private void setNewSummaryCell(IDataStore dataStore, Integer row, Integer column) {
		if (row == null) {
			// if row not specified means is referring to last row that will be store lenght + already added summary rows length
			row = Long.valueOf(dataStore.getRecordsCount()).intValue() + summaryRecordsAddedCounter - 1; // row starts from 0
		}

		try {
			JSONObject obj = new JSONObject();
			obj.put("row", row);
			obj.put("column", column);
			summaryCellsArray.put(obj);
		} catch (JSONException e) {
			logger.error("Error while tracing summary cell in row " + row + " and column " + column + ": " + e.getMessage());
		}
	}

	private void getColumnsInfos(Column column) {
		try {
			Integer size = column.getSize();
			String sizeColumn = column.getField();
			boolean unsigned = column.isUnsigned();
			JSONObject infoObj = new JSONObject();

			infoObj.putOpt("sizeColumn", sizeColumn);
			infoObj.putOpt("size", size);

			infoObj.putOpt("unsigned", unsigned);
			if (size != null || unsigned != false) {
				columnsInfos.put(infoObj);
			}

		} catch (JSONException e) {
			logger.error("Error getting size column informations from template " + e.getMessage());
		}
	}

	private Query buildQuery() {
		logger.debug("IN");
		Query query = null;
		try {
			QbeEngineInstance qbeEngineInstance = getEngineInstance();
			Map env = qbeEngineInstance.getEnv();

			query = new Query();
			query.setDistinctClauseEnabled(false);
			IModelEntity entity = getSelectedEntity();

			QbeEngineInstance engineInstance = getEngineInstance();
			QbeTemplate template = engineInstance.getTemplate();
			registryConfig = (RegistryConfiguration) template.getProperty("registryConfiguration");
			List<Column> columns = registryConfig.getColumns();
			columnMaxSize = registryConfig.getColumnsMaxSize();
			Iterator<Column> it = columns.iterator();

			Map<String, String> fieldNameIdMap = new HashMap<String, String>();

			while (it.hasNext()) {
				Column column = it.next();
				getMandatoryMetadata(column);
				getColumnsInfos(column);
				IModelField field = getColumnModelField(column, entity);
				if (field == null) {
					logger.error("Field " + column.getField() + " not found!!");
				} else {
					String name = field.getPropertyAsString("label");
					if (name == null || name.length() == 0) {
						name = field.getName();
					}

					String sorter = column.getSorter() != null && (column.getSorter().equalsIgnoreCase("ASC") || column.getSorter().equalsIgnoreCase("DESC")) ? column
							.getSorter().toUpperCase() : null;

					query.addSelectFiled(field.getUniqueName(), "NONE", field.getName(), true, true, false, sorter, field.getPropertyAsString("format"));
					fieldNameIdMap.put(column.getField(), field.getUniqueName());
				}
			}

			// get Drivers and filters

			List<RegistryConfiguration.Filter> filters = registryConfig.getFilters();
			int i = 0;
			ArrayList<ExpressionNode> expressionNodes = new ArrayList<ExpressionNode>();
			for (Iterator iterator = filters.iterator(); iterator.hasNext();) {
				Filter filter = (Filter) iterator.next();
				addFilter(i, query, env, fieldNameIdMap, filter, expressionNodes);
				i++;
			}
			// put together expression nodes
			if (expressionNodes.size() == 1) {
				query.setWhereClauseStructure(expressionNodes.get(0));
			} else if (expressionNodes.size() > 1) {
				ExpressionNode exprNodeAnd = new ExpressionNode("NODE_OP", "AND");
				exprNodeAnd.setChildNodes(expressionNodes);
				query.setWhereClauseStructure(exprNodeAnd);
			}

		} finally {
			logger.debug("OUT");
		}
		return query;
	}

	private void addSort(int i, Query query, Map env, Map<String, String> fieldNameIdMap, ArrayList<ExpressionNode> expressionNodes) {
		logger.debug("IN");

		// if(requestContainsAttribute("sort")){
		// String sortField = getAttributeAsString("sort");
		// logger.debug("Sort by "+sortField);
		//
		// //query.getO
		//
		//
		// // sorting by
		//
		// }

		logger.debug("OUT");
	}

	private void addFilter(int i, Query query, Map env, Map<String, String> fieldNameIdMap, Filter filter, ArrayList<ExpressionNode> expressionNodes) {
		logger.debug("IN");

		ExpressionNode node = query.getWhereClauseStructure();
		ExpressionNode nodeToInsert = new ExpressionNode("NODE_OP", "AND");

		// in case it is a driver
		if (filter.getPresentationType().equals(RegistryConfigurationXMLParser.PRESENTATION_TYPE_DRIVER)) {
			String driverName = filter.getDriverName();
			String fieldName = filter.getField();

			Object value = env.get(driverName);

			if (value != null && !value.toString().equals("")) {

				// TODO, change this behaviour
				if (value.toString().contains(",")) {
					value = "{,{" + value + "}}";
				}
				List valuesList = new ParametersDecoder().decode(value.toString());
				String[] valuesArr = new String[valuesList.size()];

				for (int j = 0; j < valuesList.size(); j++) {
					String val = valuesList.get(j).toString();
					valuesArr[j] = val;
				}

				logger.debug("Set filter from analytical deriver " + driverName + ": " + filter.getField() + "=" + value);

				String fieldId = fieldNameIdMap.get(fieldName);
				String[] fields = new String[] { fieldId };
				WhereField.Operand left = new WhereField.Operand(fields, "driverName", AbstractStatement.OPERAND_TYPE_SIMPLE_FIELD, null, null);

				WhereField.Operand right = new WhereField.Operand(valuesArr, "value", AbstractStatement.OPERAND_TYPE_STATIC, null, null);

				if (valuesArr.length > 1) {
					query.addWhereField("Driver_" + i, driverName, false, left, CriteriaConstants.IN, right, "AND");
				} else {
					query.addWhereField("Driver_" + i, driverName, false, left, CriteriaConstants.EQUALS_TO, right, "AND");
				}

				ExpressionNode newFilterNode = new ExpressionNode("NODE_CONST", "$F{" + "Driver_" + i + "}");
				// query.setWhereClauseStructure(newFilterNode);
				expressionNodes.add(newFilterNode);
			}

			// query.setWhereClauseStructure(whereClauseStructure)
		}
		// in case it is a filter and has a value setted
		else if (requestContainsAttribute(filter.getField())) {

			String value = getAttribute(filter.getField()).toString();
			if (value != null && !value.equalsIgnoreCase("")) {
				logger.debug("Set filter " + filter.getField() + "=" + value);

				String fieldId = fieldNameIdMap.get(filter.getField());
				String[] fields = new String[] { fieldId };
				String[] values = new String[] { value };

				WhereField.Operand left = new WhereField.Operand(fields, "filterName", AbstractStatement.OPERAND_TYPE_SIMPLE_FIELD, null, null);

				WhereField.Operand right = new WhereField.Operand(values, "value", AbstractStatement.OPERAND_TYPE_STATIC, null, null);

				// if filter type is manual use it as string starting, else as equals
				if (filter.getPresentationType().equals(RegistryConfigurationXMLParser.PRESENTATION_TYPE_COMBO)) {
					query.addWhereField("Filter_" + i, filter.getField(), false, left, CriteriaConstants.EQUALS_TO, right, "AND");
				} else {
					query.addWhereField("Filter_" + i, filter.getField(), false, left, CriteriaConstants.STARTS_WITH, right, "AND");
				}

				ExpressionNode newFilterNode = new ExpressionNode("NODE_CONST", "$F{" + "Filter_" + i + "}");
				// query.setWhereClauseStructure(newFilterNode);
				expressionNodes.add(newFilterNode);

			}
		}
		logger.debug("OUT");
	}

	private IModelField getColumnModelField(Column column, IModelEntity entity) {
		if (column.getSubEntity() != null) { // in case it is a subEntity attribute, look for the field inside it

			// In order to recover subentities the new way if DEFAULT_MAX_RECURSION_LEVEL is set to zero
			/*
			 * QbeEngineInstance engineInstance = getEngineInstance(); QbeTemplate template = engineInstance.getTemplate(); // takes the only datamart's name
			 * configured String modelName = (String) template.getDatamartNames().get(0); IModelStructure md = getDataSource().getModelStructure(); IModelEntity
			 * subEntity = md.getEntity(column.getSubEntity());
			 */

			String entityUName = entity.getUniqueName();
			String subEntityKey = entityUName.substring(0, entityUName.lastIndexOf("::")) + "::" + column.getSubEntity() + "(" + column.getForeignKey() + ")";
			IModelEntity subEntity = entity.getSubEntity(subEntityKey);
			if (subEntity == null) {
				throw new SpagoBIEngineServiceException(getActionName(), "Sub-entity [" + column.getSubEntity() + "] not found in entity [" + entity.getName()
						+ "]!");
			}
			entity = subEntity;
		}
		logger.debug("Looking for attribute " + column.getField() + " in entity " + entity.getName() + " ...");
		List<IModelField> fields = entity.getAllFields();
		Iterator<IModelField> it = fields.iterator();
		while (it.hasNext()) {
			IModelField field = it.next();
			if (field.getName().equals(column.getField())) {
				return field;
			}
		}
		return null;
	}

	private IModelEntity getSelectedEntity() {
		logger.debug("IN");
		IModelEntity entity = null;
		try {
			IDataSource ds = getDataSource();

			// change max recursion level to data source

			Map<String, Object> properties = ds.getConfiguration().loadDataSourceProperties();
			properties.put("maxRecursionLevel", "5");
			// ds.setDataMartModelAccessModality(modelAccessModality);

			IModelStructure structure = ds.getModelStructure();
			QbeEngineInstance engineInstance = getEngineInstance();
			QbeTemplate template = engineInstance.getTemplate();
			if (template.isComposite()) { // composite Qbe is not supported
				logger.error("Template is composite. This is not supported by the Registry engine");
				throw new SpagoBIEngineServiceException(getActionName(), "Template is composite. This is not supported by the Registry engine");
			}
			// takes the only datamart's name configured
			String modelName = (String) template.getDatamartNames().get(0);
			RegistryConfiguration registryConfig = (RegistryConfiguration) template.getProperty("registryConfiguration");
			String entityName = registryConfig.getEntity();

			int index = entityName.lastIndexOf(".");
			entityName = entityName + "::" + entityName.substring(index + 1); // entity name is something like it.eng.Store::Store
			logger.debug("Looking for entity [" + entityName + "] in model [" + modelName + "] ...");
			entity = structure.getRootEntity(modelName, entityName);
			logger.debug("Entity [" + entityName + "] was found");
			if (entity == null) {
				logger.error("Entity [" + entityName + "] not found!");
				throw new SpagoBIEngineServiceException(getActionName(), "Entity [" + entityName + "] not found!");
			}
		} finally {
			logger.debug("OUT");
		}
		return entity;
	}

}
