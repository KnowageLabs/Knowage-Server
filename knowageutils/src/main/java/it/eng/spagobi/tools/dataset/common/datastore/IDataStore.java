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

import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gnu.trove.set.hash.TLongHashSet;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.common.query.IQuery;

/**
 * @authors Angelo Bernabei (angelo.bernabei@eng.it)
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public interface IDataStore {

	IMetaData getMetaData();

	void setMetaData(IMetaData metaData);

	Iterator<IRecord> iterator();

	boolean isEmpty();

	long getRecordsCount();

	List<IRecord> getRecords();

	void setRecords(List<IRecord> records);

	IRecord getRecordAt(int i);

	IRecord getRecordByID(Object value);

	List<IRecord> findRecords(int fieldIndex, Object fieldValue);

	List<IRecord> findRecords(final List fieldIndexes, final List fieldValues);

	List<IRecord> findRecords(IRecordMatcher... matcher);

	List getFieldValues(int fieldIndex);

	Set getFieldDistinctValues(int fieldIndex);

	Set<String> getFieldDistinctValuesAsString(int fieldIndex);

	Map<Integer, Set<Object>> getFieldsDistinctValues(final List<Integer> fieldIndexes);

	Map<Integer, Set<String>> getFieldsDistinctValuesAsString(final List<Integer> fieldIndexes);

	Map<String, TLongHashSet> getFieldsDistinctValuesAsLongHash(final List<Integer> fieldIndexes);

	void sortRecords(int fieldIndex);

	void sortRecords(int fieldIndex, Comparator filedComparator);

	void sortRecords(Comparator<IRecord> recordComparator);

	void appendRecord(IRecord r);

	void prependRecord(IRecord record);

	void insertRecord(int recordIndex, IRecord record);

	IDataStore aggregateAndFilterRecords(String sqlQuery, int offset, int fetchSize, String dateFormatJava);

	IDataStore aggregateAndFilterRecords(String sqlQuery, int offset, int fetchSize, int maxRowCount, String dateFormatJava);

	IDataStore aggregateAndFilterRecords(String sqlQuery, List<Object> values, int offset, int fetchSize, int maxRowCount, String dateFormatJava);

	IDataStore aggregateAndFilterRecords(IQuery query, String dateFormatJava);

	org.apache.metamodel.data.DataSet getMetaModelResultSet(String sqlQuery);

	/**
	 * @deprecated use the proper DataWriter instead
	 */
	@Deprecated
	String toXml();

	Date getCacheDate();

	void setCacheDate(Date cacheDate);

	void adjustMetadata(IMetaData dataSetMetadata);

}
