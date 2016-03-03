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
package it.eng.qbe.statement;

import it.eng.qbe.datasource.AbstractDataSource;
import it.eng.qbe.model.structure.IModelField;
import it.eng.qbe.query.CalculatedSelectField;
import it.eng.qbe.query.HavingField;
import it.eng.qbe.query.ISelectField;
import it.eng.qbe.query.InLineCalculatedSelectField;
import it.eng.qbe.query.Query;
import it.eng.qbe.query.SimpleSelectField;
import it.eng.qbe.query.WhereField;
import it.eng.qbe.query.serializer.json.QueryJSONSerializer;
import it.eng.qbe.query.serializer.json.QuerySerializationConstants;
import it.eng.qbe.script.groovy.GroovyScriptAPI;
import it.eng.spagobi.tools.dataset.bo.AbstractDataSet;
import it.eng.spagobi.tools.dataset.bo.DataSetVariable;
import it.eng.spagobi.tools.dataset.common.datastore.DataStore;
import it.eng.spagobi.tools.dataset.common.datastore.Field;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStoreFilter;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.datastore.Record;
import it.eng.spagobi.tools.dataset.common.metadata.FieldMetadata;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData.FieldType;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.MetaData;
import it.eng.spagobi.tools.dataset.persist.IDataSetTableDescriptor;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.database.temporarytable.TemporaryTableManager;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.log4j.Logger;

public abstract class AbstractQbeDataSet extends AbstractDataSet {

	private IDataSource dataSource;
	protected IStatement statement;
	protected IDataStore dataStore;
	protected boolean abortOnOverflow;
	protected Map bindings;
	protected Map userProfileAttributes;
	private boolean calculateResultNumberOnLoad = true;

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(AbstractQbeDataSet.class);

	public static final String PROPERTY_IS_SEGMENT_ATTRIBUTE = "isSegmentAttribute";
	public static final String PROPERTY_IS_MANDATORY_MEASURE = "isMandatoryMeasure";

	public AbstractQbeDataSet(IStatement statement) {
		setStatement(statement);
		bindings = new HashMap();
	}

	public IDataStore getDataStore() {
		return dataStore;
	}

	protected MetaData getDataStoreMeta(Query query) {
		MetaData dataStoreMeta;
		ISelectField queryFiled;
		FieldMetadata dataStoreFieldMeta;

		Map<String, String> aliasSelectedFields = QueryJSONSerializer.getFieldsNature(query, statement.getDataSource());

		dataStoreMeta = new MetaData();

		Iterator fieldsIterator = query.getSelectFields(true).iterator();
		while (fieldsIterator.hasNext()) {
			queryFiled = (ISelectField) fieldsIterator.next();

			dataStoreFieldMeta = new FieldMetadata();
			dataStoreFieldMeta.setAlias(queryFiled.getAlias());
			if (queryFiled.isSimpleField()) {
				SimpleSelectField dataMartSelectField = (SimpleSelectField) queryFiled;
				dataStoreFieldMeta.setName(((SimpleSelectField) queryFiled).getUniqueName());
				dataStoreFieldMeta.setProperty("calculated", new Boolean(false));
				dataStoreFieldMeta.setProperty("uniqueName", dataMartSelectField.getUniqueName());
				dataStoreFieldMeta.setType(Object.class);
				String format = dataMartSelectField.getPattern();
				if (format != null && !format.trim().equals("")) {
					dataStoreFieldMeta.setProperty("format", format);
				}

				IModelField datamartField = ((AbstractDataSource) statement.getDataSource()).getModelStructure().getField(dataMartSelectField.getUniqueName());
				String iconCls = datamartField.getPropertyAsString("type");
				String nature = dataMartSelectField.getNature();
				dataStoreFieldMeta.setProperty("aggregationFunction", dataMartSelectField.getFunction().getName());

				if (nature.equals(QuerySerializationConstants.FIELD_NATURE_MANDATORY_MEASURE)) {
					dataStoreFieldMeta.setFieldType(FieldType.MEASURE);
					dataStoreFieldMeta.getProperties().put(PROPERTY_IS_MANDATORY_MEASURE, Boolean.TRUE);
				} else if (nature.equals(QuerySerializationConstants.FIELD_NATURE_MEASURE)) {
					dataStoreFieldMeta.setFieldType(FieldType.MEASURE);
				} else if (nature.equals(QuerySerializationConstants.FIELD_NATURE_SEGMENT_ATTRIBUTE)) {
					dataStoreFieldMeta.setFieldType(FieldType.ATTRIBUTE);
					dataStoreFieldMeta.getProperties().put(PROPERTY_IS_SEGMENT_ATTRIBUTE, Boolean.TRUE);
				} else if (nature.equals(QuerySerializationConstants.FIELD_NATURE_ATTRIBUTE)) {
					dataStoreFieldMeta.setFieldType(FieldType.ATTRIBUTE);
				} else {
					dataStoreFieldMeta.setFieldType(FieldType.ATTRIBUTE);
				}

			} else if (queryFiled.isCalculatedField()) {
				CalculatedSelectField claculatedQueryField = (CalculatedSelectField) queryFiled;
				dataStoreFieldMeta.setName(claculatedQueryField.getAlias());
				dataStoreFieldMeta.setProperty("calculated", new Boolean(true));
				dataStoreFieldMeta.setProperty("calculatedExpert", new Boolean(true));
				// FIXME also calculated field must have uniquename for
				// uniformity
				dataStoreFieldMeta.setProperty("uniqueName", claculatedQueryField.getAlias());
				DataSetVariable variable = new DataSetVariable(claculatedQueryField.getAlias(), claculatedQueryField.getType(),
						claculatedQueryField.getExpression());
				dataStoreFieldMeta.setProperty("variable", variable);
				dataStoreFieldMeta.setType(variable.getTypeClass());

			} else if (queryFiled.isInLineCalculatedField()) {
				InLineCalculatedSelectField claculatedQueryField = (InLineCalculatedSelectField) queryFiled;
				dataStoreFieldMeta.setName(claculatedQueryField.getAlias());
				dataStoreFieldMeta.setProperty("calculated", new Boolean(false));
				// FIXME also calculated field must have uniquename for
				// uniformity
				dataStoreFieldMeta.setProperty("uniqueName", claculatedQueryField.getAlias());
				DataSetVariable variable = new DataSetVariable(claculatedQueryField.getAlias(), claculatedQueryField.getType(),
						claculatedQueryField.getExpression());
				dataStoreFieldMeta.setProperty("variable", variable);
				dataStoreFieldMeta.setType(variable.getTypeClass());

				String nature = queryFiled.getNature();
				if (nature == null) {
					nature = QueryJSONSerializer.getInLinecalculatedFieldNature(claculatedQueryField.getExpression(), aliasSelectedFields);
				}
				dataStoreFieldMeta.setProperty("nature", nature);
				if (nature.equalsIgnoreCase(QuerySerializationConstants.FIELD_NATURE_MANDATORY_MEASURE)
						|| nature.equalsIgnoreCase(QuerySerializationConstants.FIELD_NATURE_MEASURE)) {
					dataStoreFieldMeta.setFieldType(FieldType.MEASURE);
				} else {
					dataStoreFieldMeta.setFieldType(FieldType.ATTRIBUTE);
				}
			}
			dataStoreFieldMeta.setProperty("visible", new Boolean(queryFiled.isVisible()));

			dataStoreMeta.addFiedMeta(dataStoreFieldMeta);
		}

		return dataStoreMeta;
	}

	protected DataStore toDataStore(List result) {
		DataStore dataStore;
		MetaData dataStoreMeta;
		Object[] row;

		dataStore = new DataStore();
		dataStoreMeta = getDataStoreMeta(statement.getQuery());
		dataStore.setMetaData(dataStoreMeta);

		Iterator it = result.iterator();
		while (it.hasNext()) {
			Object o = it.next();

			if (!(o instanceof Object[])) {
				row = new Object[1];
				row[0] = o == null ? "" : o;
			} else {
				row = (Object[]) o;
			}

			IRecord record = new Record(dataStore);
			for (int i = 0, j = 0; i < dataStoreMeta.getFieldCount(); i++) {
				IFieldMetaData fieldMeta = dataStoreMeta.getFieldMeta(i);
				Boolean calculated = (Boolean) fieldMeta.getProperty("calculated");
				if (calculated.booleanValue() == false) {
					Assert.assertTrue(j < row.length, "Impossible to read field [" + fieldMeta.getName() + "] from resultset");
					record.appendField(new Field(row[j]));
					if (row[j] != null)
						fieldMeta.setType(row[j].getClass());
					j++;
				} else {
					DataSetVariable variable = (DataSetVariable) fieldMeta.getProperty("variable");
					if (variable.getResetType() == DataSetVariable.RESET_TYPE_RECORD) {
						variable.reset();
					}

					record.appendField(new Field(variable.getValue()));
					if (variable.getValue() != null)
						fieldMeta.setType(variable.getValue().getClass());
				}
			}

			processCalculatedFields(record, dataStore);
			dataStore.appendRecord(record);
		}

		return dataStore;
	}

	private void processCalculatedFields(IRecord record, IDataStore dataStore) {
		IMetaData dataStoreMeta;
		List calculatedFieldsMeta;

		dataStoreMeta = dataStore.getMetaData();
		calculatedFieldsMeta = dataStoreMeta.findFieldMeta("calculated", Boolean.TRUE);
		for (int i = 0; i < calculatedFieldsMeta.size(); i++) {
			IFieldMetaData fieldMeta = (IFieldMetaData) calculatedFieldsMeta.get(i);
			DataSetVariable variable = (DataSetVariable) fieldMeta.getProperty("variable");

			ScriptEngineManager scriptManager = new ScriptEngineManager();
			ScriptEngine groovyScriptEngine = scriptManager.getEngineByName("groovy");

			// handle bindings
			// ... static bindings first
			Iterator it = bindings.keySet().iterator();
			while (it.hasNext()) {
				String bindingName = (String) it.next();
				Object bindingValue = bindings.get(bindingName);
				groovyScriptEngine.put(bindingName, bindingValue);
			}

			// ... then runtime bindings
			Map qFields = new HashMap();
			Map dmFields = new HashMap();
			Object[] columns = new Object[dataStoreMeta.getFieldCount()];
			for (int j = 0; j < dataStoreMeta.getFieldCount(); j++) {
				qFields.put(dataStoreMeta.getFieldMeta(j).getAlias(), record.getFieldAt(j).getValue());
				dmFields.put(dataStoreMeta.getFieldMeta(j).getProperty("uniqueName"), record.getFieldAt(j).getValue());
				columns[j] = record.getFieldAt(j).getValue();
			}

			groovyScriptEngine.put("qFields", qFields); // key = alias
			groovyScriptEngine.put("dmFields", dmFields); // key = id
			groovyScriptEngine.put("fields", qFields); // default key = alias
			groovyScriptEngine.put("columns", columns); // key = col-index
			groovyScriptEngine.put("api", new GroovyScriptAPI());

			// show time
			Object calculatedValue = null;
			try {
				calculatedValue = groovyScriptEngine.eval(variable.getExpression());

			} catch (ScriptException ex) {
				calculatedValue = "NA";
				ex.printStackTrace();
			}

			logger.debug("Field [" + fieldMeta.getName() + "] is equals to [" + calculatedValue + "]");
			variable.setValue(calculatedValue);

			record.getFieldAt(dataStoreMeta.getFieldIndex(fieldMeta.getAlias())).setValue(variable.getValue());
		}
	}

	public IStatement getStatement() {
		return statement;
	}

	public void setStatement(IStatement statement) {
		this.statement = statement;
	}

	public boolean isAbortOnOverflow() {
		return abortOnOverflow;
	}

	public void setAbortOnOverflow(boolean abortOnOverflow) {
		this.abortOnOverflow = abortOnOverflow;
	}

	public void addBinding(String bindingName, Object bindingValue) {
		bindings.put(bindingName, bindingValue);
	}

	@Override
	public Object getQuery() {
		return this.statement.getQuery();
	}

	@Override
	public void setQuery(Object query) {
		this.statement.setQuery((it.eng.qbe.query.Query) query);

	}

	public String getSQLQuery(boolean includeInjectedFilters) {
		return statement.getSqlQueryString();
	}

	@Override
	public IDataSetTableDescriptor persist(String tableName, IDataSource dataSource) {
		IDataSource dataSourceForReading = this.getDataSourceForReading();
		if (dataSourceForReading == null) {
			// dataset is neither flat nor persisted
			dataSourceForReading = this.getDataSource();
		}
		if (dataSource.getLabel().equals(dataSourceForReading.getLabel())) {
			// case when datasource for reading is the same for writing --> use
			// TT table with CREATE AS SELECT
			logger.debug("Datasource for reading is the same for writing --> use TT table with CREATE AS SELECT");
			try {
				String sql = getSQLQuery(true);
				List<String> fields = getDataSetSelectedFields(statement.getQuery());
				return TemporaryTableManager.createTable(fields, sql, tableName, dataSource);
			} catch (Exception e) {
				logger.error("Error creating the temporary table with name " + tableName, e);
				throw new SpagoBIEngineRuntimeException("Error creating the temporary table with name " + tableName, e);
			}
		} else {
			// case when datasource for reading is not the same for writing -->
			// use super method, i.e. load datastore and write it into
			// destination
			logger.debug("Datasource for reading is not the same for writing --> use super method, i.e. load datastore and write it into destination");
			return super.persist(tableName, dataSource);
		}

	}

	// TODO merge with AbstractDataSet.getDomainValues
	@Override
	public IDataStore getDomainValues(String fieldName, Integer start, Integer limit, IDataStoreFilter filter) {
		if (isPersisted() || isFlatDataset()) {
			int index = this.getStatement().getQuery().getSelectFieldIndex(fieldName);
			String alias = (this.getStatement().getQuery().getSelectFieldByIndex(index)).getAlias();
			return getDomainValuesFromPersistenceTable(alias, start, limit, filter);
		} else {
			return getDomainValuesFromTemporaryTable(fieldName, start, limit, filter);
		}
	}

	@Override
	protected IDataStore getDomainValuesFromTemporaryTable(String fieldName, Integer start, Integer limit, IDataStoreFilter filter) {
		IDataStore toReturn = super.getDomainValuesFromTemporaryTable(fieldName, start, limit, filter);
		toReturn.getMetaData().changeFieldAlias(0, fieldName);
		return toReturn;
	}

	private List<String> getDataSetSelectedFields(Query qbeQuery) {
		List<String> toReturn = new ArrayList<String>();
		List<ISelectField> selectFieldsNames = qbeQuery.getSelectFields(true);
		for (int i = 0; i < selectFieldsNames.size(); i++) {
			ISelectField selectField = selectFieldsNames.get(i);
			toReturn.add(selectField.getName());
		}
		return toReturn;
	}

	/**
	 * Build a datasource.. We need this object to build a JDBCDataSet
	 *
	 * @return
	 */
	public IDataSource getDataSource() {
		if (dataSource == null) {
			dataSource = ((AbstractDataSource) statement.getDataSource()).getToolsDataSource();
		}
		return dataSource;
	}

	@Override
	public IMetaData getMetadata() {
		return getDataStoreMeta(statement.getQuery());
	}

	public String getSignature() {
		return getSQLQuery(true);
	}

	public Map getUserProfileAttributes() {
		return userProfileAttributes;
	}

	public void setUserProfileAttributes(Map attributes) {
		this.userProfileAttributes = attributes;
		getStatement().setProfileAttributes(attributes);

	}

	@Override
	public void setParamsMap(Map paramsMap) {
		this.getStatement().setParameters(paramsMap);
	}

	@Override
	public Map getParamsMap() {
		return this.getStatement().getParameters();
	}

	public IDataStore decode(IDataStore datastore) {
		return datastore;
	}

	public IDataStore test(int offset, int fetchSize, int maxResults) {
		this.loadData(offset, fetchSize, maxResults);
		return getDataStore();
	}

	public IDataStore test() {
		loadData();
		return getDataStore();
	}

	@Override
	public void setMetadata(IMetaData metadata) {
		// TODO Auto-generated method stub

	}

	public boolean isCalculateResultNumberOnLoadEnabled() {
		return calculateResultNumberOnLoad;
	}

	public void setCalculateResultNumberOnLoad(boolean enabled) {
		calculateResultNumberOnLoad = enabled;
	}

	public void updateParameters(it.eng.qbe.query.Query query, Map parameters) {
		logger.debug("IN");
		List whereFields = query.getWhereFields();
		Iterator whereFieldsIt = whereFields.iterator();
		while (whereFieldsIt.hasNext()) {
			WhereField whereField = (WhereField) whereFieldsIt.next();
			if (whereField.isPromptable()) {
				String key = getParameterKey(whereField.getRightOperand().values[0]);
				if (key != null) {
					String parameterValues = (String) parameters.get(key);
					if (parameterValues != null) {
						String[] promptValues = new String[] { parameterValues }; // TODO
																					// how
																					// to
																					// manage
																					// multi-values
																					// prompts?
						logger.debug("Read prompts " + promptValues + " for promptable filter " + whereField.getName() + ".");
						whereField.getRightOperand().lastValues = promptValues;
					}
				}
			}
		}
		List havingFields = query.getHavingFields();
		Iterator havingFieldsIt = havingFields.iterator();
		while (havingFieldsIt.hasNext()) {
			HavingField havingField = (HavingField) havingFieldsIt.next();
			if (havingField.isPromptable()) {
				String key = getParameterKey(havingField.getRightOperand().values[0]);
				if (key != null) {
					String parameterValues = (String) parameters.get(key);
					if (parameterValues != null) {
						String[] promptValues = new String[] { parameterValues }; // TODO
																					// how
																					// to
																					// manage
																					// multi-values
																					// prompts?
						logger.debug("Read prompt value " + promptValues + " for promptable filter " + havingField.getName() + ".");
						havingField.getRightOperand().lastValues = promptValues;
					}
				}
			}
		}
		logger.debug("OUT");
	}

	protected String getParameterKey(String fieldValue) {
		int beginIndex = fieldValue.indexOf("P{");
		int endIndex = fieldValue.indexOf("}");
		if (beginIndex > 0 && endIndex > 0 && endIndex > beginIndex) {
			return fieldValue.substring(beginIndex + 2, endIndex);
		} else {
			return null;
		}
	}

	/**
	 * Adjusts the metadata of the datastore retrieved by a JDBCDataSet, since executed JDBC dataset does not contain correct metadata (name, alias,
	 * attribute/measure) therefore we need to merge metadata
	 *
	 * @param jdbcMetadata
	 *            the metadata retrieved by executing the JDBC dataset
	 * @param qbeQueryMetaData
	 *            the metadata of the Qbe query
	 */
	protected IMetaData mergeMetadata(IMetaData jdbcMetadata, IMetaData qbeQueryMetaData) {
		int count = jdbcMetadata.getFieldCount();
		for (int i = 0; i < count; i++) {
			IFieldMetaData jdbcFieldMeta = jdbcMetadata.getFieldMeta(i);
			// positional matching between the 2 metadata
			IFieldMetaData qbeFieldMeta = qbeQueryMetaData.getFieldMeta(i);
			jdbcFieldMeta.setFieldType(qbeFieldMeta.getFieldType());
			jdbcFieldMeta.setName(qbeFieldMeta.getName());
			jdbcFieldMeta.setAlias(qbeFieldMeta.getAlias());
		}
		return jdbcMetadata;
	}

}
