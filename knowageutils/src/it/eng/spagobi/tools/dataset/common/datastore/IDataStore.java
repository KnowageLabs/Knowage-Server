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
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.common.query.IQuery;

import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @authors Angelo Bernabei (angelo.bernabei@eng.it) Andrea Gioia (andrea.gioia@eng.it)
 */
public interface IDataStore {

	IMetaData getMetaData();

	Iterator iterator();

	boolean isEmpty();

	long getRecordsCount();

	IRecord getRecordAt(int i);

	IRecord getRecordByID(Object value);

	List<IRecord> findRecords(int fieldIndex, Object fieldValue);

	List<IRecord> findRecords(final List fieldIndexes, final List fieldValues);

	List<IRecord> findRecords(IRecordMatcher matcher);

	List getFieldValues(int fieldIndex);

	Set getFieldDistinctValues(int fieldIndex);

	Set<String> getFieldDistinctValuesAsString(int fieldIndex);

	void sortRecords(int fieldIndex);

	void sortRecords(int fieldIndex, Comparator filedComparator);

	void sortRecords(Comparator recordComparator);

	void appendRecord(IRecord r);

	void prependRecord(IRecord record);

	void insertRecord(int recordIndex, IRecord record);

	IDataStore aggregateAndFilterRecords(String sqlQuery);

	IDataStore aggregateAndFilterRecords(IQuery query);

	/**
	 * @deprecated use the proper DataWriter instead
	 */
	@Deprecated
	String toXml();

	/**
	 * @deprecated use the proper DataWriter instead
	 */
	@Deprecated
	SourceBean toSourceBean() throws SourceBeanException;

	Date getCacheDate();

	void setCacheDate(Date cacheDate);

}
