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
package it.eng.spagobi.tools.dataset.common.datastore;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spagobi.tools.dataset.common.metadata.FieldMetadata;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData.FieldType;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.MetaData;
import it.eng.spagobi.tools.dataset.common.query.IQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.pojo.ArrayTableDataProvider;
import org.apache.metamodel.pojo.PojoDataContext;
import org.apache.metamodel.query.CompiledQuery;
import org.apache.metamodel.query.FunctionType;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.query.SelectItem;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.util.SimpleTableDef;

/**
 * @authors Angelo Bernabei (angelo.bernabei@eng.it) Andrea Gioia (andrea.gioia@eng.it)
 */
public class DataStore implements IDataStore {

	private static transient Logger logger = Logger.getLogger(DataStore.class);

	public static String DEFAULT_TABLE_NAME = "TemporaryTable";
	public static String DEFAULT_SCHEMA_NAME = "SpagoBI";

	IMetaData metaData;

	List records = null;

	Date cacheDate = null;

	public DataStore() {
		super();
		this.records = new ArrayList();
		this.metaData = new MetaData();
	}

	@Override
	public boolean isEmpty() {
		return records.isEmpty();
	}

	@Override
	public long getRecordsCount() {
		return records.size();
	}

	@Override
	public Iterator iterator() {
		return records.iterator();
	}

	@Override
	public void appendRecord(IRecord record) {
		records.add(record);
	}

	@Override
	public void prependRecord(IRecord record) {
		insertRecord(0, record);
	}

	@Override
	public void insertRecord(int position, IRecord record) {
		records.add(position, record);
	}

	@Override
	public IRecord getRecordAt(int i) {
		IRecord r = (IRecord) records.get(i);
		return r;
	}

	@Override
	public IRecord getRecordByID(Object value) {
		List result;
		final int idFieldIndex;

		idFieldIndex = getMetaData().getIdFieldIndex();
		if (idFieldIndex == -1) {
			throw new RuntimeException("Impossible to get a record by id if the id filed has not been specified before into the DataStoreMetaData object");
		}

		result = findRecords(idFieldIndex, value);

		if (result.size() > 1) {
			logger.warn("Duplicate idetifier found while searching record by id on value [" + value + "]. Only the first match will be used");
		}

		return result.size() == 1 ? (IRecord) result.get(0) : null;
	}

	@Override
	public List findRecords(int fieldIndex, Object fieldValue) {
		List fieldIndexes = new ArrayList();
		List fieldValues = new ArrayList();

		fieldIndexes.add(new Integer(fieldIndex));
		fieldValues.add(fieldValue);

		return findRecords(fieldIndexes, fieldValues);
	}

	@Override
	public List findRecords(final List fieldIndexes, final List fieldValues) {
		List results;

		results = new ArrayList();

		results = findRecords(new IRecordMatcher() {
			@Override
			public boolean match(IRecord record) {
				boolean match = true;
				for (int i = 0; i < fieldIndexes.size(); i++) {
					Integer fieldIndex = (Integer) fieldIndexes.get(i);
					Object fieldValue = fieldValues.get(i);
					IField filed = record.getFieldAt(fieldIndex.intValue());
					if (!filed.getValue().equals(fieldValue)) {
						match = false;
						break;
					}
				}
				return match;
			}
		});

		return results;
	}

	@Override
	public List findRecords(IRecordMatcher matcher) {
		List results;
		Iterator it;

		results = new ArrayList();

		it = iterator();
		while (it.hasNext()) {
			IRecord record = (IRecord) it.next();
			if (matcher.match(record)) {
				results.add(record);
			}
		}

		return results;
	}

	@Override
	public IMetaData getMetaData() {
		return this.metaData;
	}

	public void setMetaData(IMetaData metaData) {
		this.metaData = metaData;
	}

	@Override
	public List getFieldValues(int fieldIndex) {
		List results;
		Iterator it;

		results = new ArrayList();

		it = iterator();
		while (it.hasNext()) {
			IRecord record = (IRecord) it.next();
			IField field = record.getFieldAt(fieldIndex);
			if (field.getValue() != null) {
				results.add(field.getValue());
			}
		}

		return results;
	}

	@Override
	public Set getFieldDistinctValues(int fieldIndex) {
		Set results;
		Iterator it;

		results = new LinkedHashSet();

		it = iterator();
		while (it.hasNext()) {
			IRecord record = (IRecord) it.next();
			IField field = record.getFieldAt(fieldIndex);
			if (field.getValue() != null) {
				results.add(field.getValue());
			}
		}

		return results;
	}

	@Override
	public Set<String> getFieldDistinctValuesAsString(int fieldIndex) {
		Set<String> results;
		Iterator it;

		results = new LinkedHashSet();

		it = iterator();
		while (it.hasNext()) {
			IRecord record = (IRecord) it.next();
			IField field = record.getFieldAt(fieldIndex);
			if (field.getValue() != null) {
				String normalizedValue;
				if (field.getValue() instanceof Number) {
					Number number = (Number) field.getValue();
					Double numericValue = number.doubleValue();
					if ((numericValue == Math.floor(numericValue)) && !Double.isInfinite(numericValue)) {
						// the number is an integer, this will remove the .0 trailing zeros
						int numericInt = numericValue.intValue();
						normalizedValue = String.valueOf(numericInt);
					} else {
						normalizedValue = String.valueOf(numericValue);

					}
				}

				normalizedValue = String.valueOf(field.getValue());
				results.add(normalizedValue);
			}
		}

		return results;
	}

	@Override
	public void sortRecords(int fieldIndex) {
		final int fIndex = fieldIndex;

		/*
		 * Class fieldType = getMetaData().getFieldType(fieldIndex); if(!fieldType.isInstance(Comparable.class)) { throw new
		 * RuntimeException("Impossible to sort DataStore on field " + fieldIndex + "because the type of this filed [" + fieldType.getName() +
		 * "] does not implement the Comparable interface"); }
		 */

		sortRecords(new Comparator() {
			@Override
			public int compare(Object o1, Object o2) {
				IRecord record1 = (IRecord) o1;
				IRecord record2 = (IRecord) o2;
				IField field1 = record1.getFieldAt(fIndex);
				IField field2 = record2.getFieldAt(fIndex);
				Comparable value1 = (Comparable) field1.getValue();
				Comparable value2 = (Comparable) field2.getValue();

				if (value1 == null && value2 == null)
					return 0;
				else if (value1 == null)
					return -1;
				else if (value2 == null)
					return 1;

				return value1.compareTo(value2);
			}
		});
	}

	@Override
	public void sortRecords(int fieldIndex, Comparator filedComparator) {
		final int fIndex = fieldIndex;
		final Comparator fComaparator = filedComparator;
		sortRecords(new Comparator() {
			@Override
			public int compare(Object o1, Object o2) {
				IRecord record1 = (IRecord) o1;
				IRecord record2 = (IRecord) o2;
				IField field1 = record1.getFieldAt(fIndex);
				IField field2 = record2.getFieldAt(fIndex);
				return fComaparator.compare(field1, field2);
			}
		});
	}

	@Override
	public void sortRecords(Comparator recordComparator) {
		Collections.sort(records, recordComparator);
	}

	public List getRecords() {
		return records;
	}

	public void setRecords(List records) {
		this.records = records;
	}

	@Override
	public SourceBean toSourceBean() throws SourceBeanException {
		SourceBean sb1 = new SourceBean("ROWS");
		Iterator it = iterator();
		while (it.hasNext()) {
			SourceBean sb2 = new SourceBean("ROW");
			IRecord record = (IRecord) it.next();
			for (int i = 0; i < getMetaData().getFieldCount(); i++) {
				IField field = record.getFieldAt(i);
				IFieldMetaData fieldMeta = getMetaData().getFieldMeta(i);
				String name = fieldMeta.getName();
				Object value = field.getValue();
				Class type = fieldMeta.getType();
				if (value == null)
					value = new String("");
				sb2.setAttribute(name, value);
			}
			sb1.setAttribute(sb2);
		}
		return sb1;
	}

	@Override
	public String toXml() {
		String xml;

		logger.debug("IN");

		xml = "<ROWS>";
		Iterator it = iterator();
		while (it.hasNext()) {
			IRecord record = (IRecord) it.next();
			xml += "<ROW ";
			for (int j = 0; j < getMetaData().getFieldCount(); j++) {
				IField field = record.getFieldAt(j);
				IFieldMetaData fieldMetaData = getMetaData().getFieldMeta(j);
				String fieldHeader = fieldMetaData.getAlias() != null ? fieldMetaData.getAlias() : fieldMetaData.getName();
				xml += fieldHeader + "=\"" + field.getValue() + "\" ";
			}
			xml += " />";

		}
		xml += "</ROWS>";

		logger.debug("OUT");

		return xml;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("DataStore: [\n");
		buffer.append("MetaData:\n");
		buffer.append(metaData);
		buffer.append("\n");
		buffer.append("Records:\n");
		Iterator it = this.iterator();
		while (it.hasNext()) {
			IRecord record = (IRecord) it.next();
			buffer.append(record);
			buffer.append("\n");
		}
		buffer.append("]");
		return buffer.toString();
	}

	@Override
	public Date getCacheDate() {
		return cacheDate;
	}

	@Override
	public void setCacheDate(Date cacheDate) {
		this.cacheDate = cacheDate;
	}

	@Override
	public IDataStore aggregateAndFilterRecords(IQuery query) {
		return aggregateAndFilterRecords(query.toSql(DEFAULT_SCHEMA_NAME, DEFAULT_TABLE_NAME));
	}

	@Override
	public IDataStore aggregateAndFilterRecords(String sqlQuery) {

		// **************************************************************************************************************
		// ***** This part build data structures used to convert a SpagoBI DataStore into an MetaModel DataContext ******
		// **************************************************************************************************************
		int fieldCount = this.metaData.getFieldCount();

		String[] columnNames = new String[fieldCount];
		ColumnType[] columnTypes = new ColumnType[fieldCount];
		HashMap<String, FieldType> fieldTypes = new HashMap<String, FieldType>();

		Collection<Object[]> arrays = new ArrayList<Object[]>();

		for (int i = 0; i < fieldCount; i++) {
			String columnName = this.metaData.getFieldName(i);

			if(columnName.contains(":")){
				columnNames[i] = this.metaData.getFieldAlias(i);
			}else{
				columnNames[i] = columnName;
			}
			Class type = this.metaData.getFieldType(i);
			if (type == Integer.class) {
				columnTypes[i] = ColumnType.INTEGER;
			} else if (type == Double.class) {
				columnTypes[i] = ColumnType.DOUBLE;
			} else {
				columnTypes[i] = ColumnType.STRING;
			}
			fieldTypes.put(columnName, this.metaData.getFieldMeta(i).getFieldType());
		}
		for (Object r : this.records) {
			IRecord record = (IRecord) r;
			Object[] row = new Object[fieldCount];
			for (int i = 0; i < fieldCount; i++) {
				row[i] = record.getFieldAt(i).getValue();
			}
			arrays.add(row);
		}

		// *************************************************************************************************
		// ****** This part create a DataContext for doing aggregation and filter define by the query ******
		// *************************************************************************************************

		String uniqueTableName = UUID.randomUUID().toString().replace('-', '_');
		SimpleTableDef dataStoreDef = new SimpleTableDef(uniqueTableName, columnNames, columnTypes);
		ArrayTableDataProvider dataStoreTableProvider = new ArrayTableDataProvider(dataStoreDef, arrays);
		DataContext dataContext = new PojoDataContext(DEFAULT_SCHEMA_NAME, dataStoreTableProvider);

		// Change table name to be concurrency-safe
		String newSqlQuery = sqlQuery.replace(DEFAULT_TABLE_NAME, uniqueTableName);
		Query query = dataContext.parseQuery(newSqlQuery);
		CompiledQuery cQuery = dataContext.compileQuery(query);
		DataSet dataSet = dataContext.executeQuery(cQuery);
		// DataSet dataSet = dataContext.executeQuery(query);

		// *************************************************************************************************
		// **** This part generates a SpagoBI datastore starting from the Apache MetaModel dataset *********
		// *************************************************************************************************
		IDataStore dataStore = new DataStore();

		int resultCount = 0;
		while (dataSet.next()) {
			Row row = dataSet.getRow();
			IRecord record = getRecordFromRow(row, dataStore);
			dataStore.appendRecord(record);
			resultCount++;
		}

		SelectItem[] selectItems = dataSet.getSelectItems();
		for (int i = 0; i < selectItems.length; i++) {
			IFieldMetaData fieldMetaData = getFieldMetaDataFromSelectItem(selectItems[i], fieldTypes);
			dataStore.getMetaData().addFiedMeta(fieldMetaData);
		}
		dataStore.getMetaData().setProperty("resultNumber", resultCount);

		return dataStore;
	}

	private IFieldMetaData getFieldMetaDataFromSelectItem(SelectItem selectItem, HashMap<String, FieldType> fieldTypes) {

		IFieldMetaData fieldMetaData = new FieldMetadata();

		String alias = selectItem.getAlias();
		Class type = selectItem.getExpectedColumnType().getJavaEquivalentClass();
		if ((type != Double.class) && (type != Integer.class)) {
			type = String.class;
		}
		String name = selectItem.getColumn().getName();
		FunctionType function = selectItem.getFunction();
		FieldType fieldType;

		if (function != null) {
			fieldType = FieldType.MEASURE;
			name = function.toString() + "(" + name + ")";
		} else {
			fieldType = fieldTypes.get(name);
		}

		fieldMetaData.setName(name);
		fieldMetaData.setAlias(alias);
		fieldMetaData.setFieldType(fieldType);
		fieldMetaData.setType(type);

		return fieldMetaData;
	}

	private IRecord getRecordFromRow(Row row, IDataStore dataStore) {
		IRecord record = new Record(dataStore);
		Object[] values = row.getValues();
		for (int i = 0; i < values.length; i++) {
			record.appendField(new Field(values[i]));
		}
		return record;
	}

}
