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

import java.io.InputStream;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.script.ScriptEngineManager;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

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
import it.eng.spagobi.commons.bo.UserProfile;
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
import it.eng.spagobi.utilities.groovy.GroovySandbox;

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

	@Override
	public IDataStore getDataStore() {
		return dataStore;
	}

	protected IMetaData getDataStoreMeta(Query query) {
		IMetaData dataStoreMeta;
		ISelectField queryField;
		FieldMetadata dataStoreFieldMeta;

		Map<String, String> aliasSelectedFields = QueryJSONSerializer.getFieldsNature(query, statement.getDataSource());
		dataStoreMeta = new MetaData();

		Iterator fieldsIterator = query.getSelectFields(true).iterator();
		while (fieldsIterator.hasNext()) {
			queryField = (ISelectField) fieldsIterator.next();

			dataStoreFieldMeta = new FieldMetadata();
			dataStoreFieldMeta.setAlias(queryField.getAlias());
			if (queryField.isSimpleField()) {
				SimpleSelectField dataMartSelectField = (SimpleSelectField) queryField;
				dataStoreFieldMeta.setName(((SimpleSelectField) queryField).getAlias());
				dataStoreFieldMeta.setProperty("calculated", new Boolean(false));
				dataStoreFieldMeta.setProperty("uniqueName", dataMartSelectField.getUniqueName());

				if (dataMartSelectField.getFunction().getName().equals("NONE") && dataMartSelectField.getJavaClass() != null) {
					dataStoreFieldMeta.setType(dataMartSelectField.getJavaClass());
				} else {
					dataStoreFieldMeta.setType(Object.class);
				}

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

			} else if (queryField.isCalculatedField()) {
				CalculatedSelectField calculatedQueryField = (CalculatedSelectField) queryField;
				dataStoreFieldMeta.setName(calculatedQueryField.getAlias());
				dataStoreFieldMeta.setProperty("calculated", new Boolean(true));
				dataStoreFieldMeta.setProperty("calculatedExpert", new Boolean(true));
				// FIXME also calculated field must have uniquename for
				// uniformity
				dataStoreFieldMeta.setProperty("uniqueName", calculatedQueryField.getAlias());
				DataSetVariable variable = new DataSetVariable(calculatedQueryField.getAlias(), calculatedQueryField.getType(),
						calculatedQueryField.getExpression());
				dataStoreFieldMeta.setProperty("variable", variable);
				dataStoreFieldMeta.setType(variable.getTypeClass());

			} else if (queryField.isInLineCalculatedField()) {
				InLineCalculatedSelectField calculatedQueryField = (InLineCalculatedSelectField) queryField;
				dataStoreFieldMeta.setName(calculatedQueryField.getAlias());
				dataStoreFieldMeta.setProperty("calculated", new Boolean(false));
				// FIXME also calculated field must have uniquename for
				// uniformity
				dataStoreFieldMeta.setProperty("uniqueName", calculatedQueryField.getAlias());
				DataSetVariable variable = new DataSetVariable(calculatedQueryField.getAlias(), calculatedQueryField.getType(),
						calculatedQueryField.getExpression());
				dataStoreFieldMeta.setProperty("variable", variable);
				dataStoreFieldMeta.setType(variable.getTypeClass());

				String nature = queryField.getNature();
				if (nature == null) {
					nature = QueryJSONSerializer.getInLinecalculatedFieldNature(calculatedQueryField.getExpression(), aliasSelectedFields);
				}
				dataStoreFieldMeta.setProperty("nature", nature);
				if (nature.equalsIgnoreCase(QuerySerializationConstants.FIELD_NATURE_MANDATORY_MEASURE)
						|| nature.equalsIgnoreCase(QuerySerializationConstants.FIELD_NATURE_MEASURE)) {
					dataStoreFieldMeta.setFieldType(FieldType.MEASURE);
				} else {
					dataStoreFieldMeta.setFieldType(FieldType.ATTRIBUTE);
				}
			}
			dataStoreFieldMeta.setProperty("visible", new Boolean(queryField.isVisible()));

			dataStoreMeta.addFiedMeta(dataStoreFieldMeta);
		}

		return dataStoreMeta;
	}

	protected IDataStore toDataStore(List result, IMetaData dataStoreMeta) {
		IDataStore dataStore = new DataStore();
		dataStore.setMetaData(dataStoreMeta);

		Iterator it = result.iterator();
		int r = 0;
		while (it.hasNext()) {
			Object o = it.next();
			IRecord record = toRecord(o, dataStoreMeta);
			((Record) record).setDataStore(dataStore);
			logger.debug("-------------RECORD " + r + "---------------");
			r++;
			processCalculatedFields(record, dataStore);
			dataStore.appendRecord(record);
		}

		return dataStore;
	}

	public static IRecord toRecord(Object o, IMetaData dataStoreMeta) {
		Object[] row;
		if (!(o instanceof Object[])) {
			row = new Object[1];
			row[0] = o;
		} else {
			row = (Object[]) o;
		}
		LogMF.debug(logger, "Processing record {0}", Arrays.toString(row));
		IRecord record = new Record();
		for (int i = 0, j = 0; i < dataStoreMeta.getFieldCount(); i++) {
			IFieldMetaData fieldMeta = dataStoreMeta.getFieldMeta(i);
			Boolean calculated = (Boolean) fieldMeta.getProperty("calculated");
			if (calculated.booleanValue() == false) {
				Assert.assertTrue(j < row.length, "Impossible to read field [" + fieldMeta.getName() + "] from resultset");

				if (row[j] instanceof java.sql.Clob) {
					Clob clob = (Clob) row[j];
					InputStream in;
					try {
						in = clob.getAsciiStream();
					} catch (SQLException e) {
						logger.error("Error in reading clob");
						throw new RuntimeException(e);
					}
					try (Scanner s = new Scanner(in)) {
						s.useDelimiter("\\A");

						String clobAsString = s.hasNext() ? s.next() : "";
						record.appendField(new Field(clobAsString));
					}
					if (row[j] != null)
						fieldMeta.setType(row[j].getClass());
				} else {

					record.appendField(new Field(row[j]));
					if (row[j] != null)
						fieldMeta.setType(row[j].getClass());
				}
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
		return record;
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
			// ScriptEngine groovyScriptEngine = scriptManager.getEngineByName("groovy");

			Map<String, Object> groovyBindings = new HashMap<String, Object>();
			// handle bindings
			// ... static bindings first
			Iterator it = bindings.keySet().iterator();
			while (it.hasNext()) {
				String bindingName = (String) it.next();
				Object bindingValue = bindings.get(bindingName);
				// groovyScriptEngine.put(bindingName, bindingValue);
				groovyBindings.put(bindingName, bindingValue);
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

			groovyBindings.put("qFields", qFields); // key = alias
			groovyBindings.put("dmFields", dmFields); // key = id
			groovyBindings.put("fields", qFields); // default key = alias
			groovyBindings.put("columns", columns); // key = col-index
			groovyBindings.put("api", new GroovyScriptAPI());

			// show time
			Object calculatedValue = null;
			try {
				// calculatedValue = groovyScriptEngine.eval(variable.getExpression());

				GroovySandbox groovySandbox = new GroovySandbox(new Class[] { GroovyScriptAPI.class });
				groovySandbox.setBindings(groovyBindings);
				calculatedValue = groovySandbox.evaluate(variable.getExpression());

			} catch (Exception ex) {
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

	@Override
	public void setAbortOnOverflow(boolean abortOnOverflow) {
		this.abortOnOverflow = abortOnOverflow;
	}

	@Override
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
	@Override
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

	@Override
	public String getSignature() {
		UserProfile profile = getUserProfile();
		LogMF.debug(logger, "User profile is {0}", profile);
		String datasourceSignature = this.getDataSource().getSignature(profile);
		String querySignature = getSQLQuery(true);
		String toReturn = datasourceSignature + "_" + querySignature;
		LogMF.debug(logger, "Dataset signature is {0}", toReturn);
		return toReturn;
	}

	@Override
	public Map getUserProfileAttributes() {
		return userProfileAttributes;
	}

	@Override
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

	@Override
	public IDataStore decode(IDataStore datastore) {
		return datastore;
	}

	@Override
	public IDataStore test(int offset, int fetchSize, int maxResults) {
		this.loadData(offset, fetchSize, maxResults);
		return getDataStore();
	}

	@Override
	public IDataStore test() {
		loadData();
		return getDataStore();
	}

	@Override
	public void setMetadata(IMetaData metadata) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isCalculateResultNumberOnLoadEnabled() {
		return calculateResultNumberOnLoad;
	}

	@Override
	public void setCalculateResultNumberOnLoad(boolean enabled) {
		calculateResultNumberOnLoad = enabled;
	}

	// @Override
	// public void setRuntimeDrivers(Map<String, String> drivers) {
	// super.setRuntimeDrivers(drivers);
	// }

	// @Override
	// public Map<String, String> getRuntimeDrivers() {
	// return getRuntimeDrivers();
	// }

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
	 * @param jdbcMetadata     the metadata retrieved by executing the JDBC dataset
	 * @param qbeQueryMetaData the metadata of the Qbe query
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
