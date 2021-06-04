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

import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.SQLDataException;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.tools.dataset.common.datastore.DataStore;
import it.eng.spagobi.tools.dataset.common.datastore.Field;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.datastore.Record;
import it.eng.spagobi.tools.dataset.common.metadata.FieldMetadata;
import it.eng.spagobi.tools.dataset.common.metadata.MetaData;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;

/**
 * @author Monica Franceschini (monica.franceschini@eng.it)
 */
public class JDBCHiveDataReader extends AbstractDataReader {

	private static transient Logger logger = Logger.getLogger(JDBCHiveDataReader.class);

	public JDBCHiveDataReader() {
	}

	@Override
	public boolean isOffsetSupported() {
		return true;
	}

	@Override
	public boolean isFetchSizeSupported() {
		return true;
	}

	@Override
	public boolean isMaxResultsSupported() {
		return true;
	}

	@Override
	public IDataStore read(Object data) throws EMFUserError, EMFInternalError {
		DataStore dataStore = null;
		MetaData dataStoreMeta;
		int columnCount;
		int columnIndex;
		ResultSet rs;

		FieldMetadata fieldMeta;

		logger.debug("IN");

		rs = (ResultSet) data;

		dataStore = new DataStore();
		dataStoreMeta = new MetaData();
		dataStore.setMetaData(dataStoreMeta);

		try {
			logger.debug("Reading data ...");
			if (getOffset() > 0) {
				logger.debug("Offset is equal to [" + getOffset() + "]");
				int i = 0;
				while (i != getOffset()) {
					rs.next();
					i++;
				}

			} else {
				logger.debug("Offset not set");
			}

			long maxRecToParse = Long.MAX_VALUE;
			if (getFetchSize() > 0) {
				maxRecToParse = getFetchSize();
				logger.debug("FetchSize is equal to [" + maxRecToParse + "]");
			} else {
				logger.debug("FetchSize not set");
			}
			int recCount = 0;
			int resultNumber = 0;
			while ((recCount < maxRecToParse) && rs.next()) {
				IRecord record = new Record(dataStore);
				logger.debug("Reading metadata ...");
				columnCount = rs.getMetaData().getColumnCount();

				for (columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
					String fieldName = rs.getMetaData().getColumnLabel(columnIndex);
					fieldMeta = new FieldMetadata();

					Object columnValue = null;
					try {
						columnValue = rs.getObject(columnIndex);
					} catch (SQLDataException e) {
						logger.warn("Skipping column " + fieldName + " because of the following error" , e);
					}
					IField field = new Field(columnValue);

					record.appendField(field);

					logger.debug("Field [" + columnIndex + "] name is equal to [" + fieldName + "]");
					if (dataStore.getMetaData().getFieldIndex(fieldName) == -1) {
						fieldMeta.setName(fieldName);
						Class tpeClass = String.class;
						try {
							tpeClass = getType(rs.getMetaData().getColumnTypeName(columnIndex));
						} catch (Exception e) {
							logger.error("Can not read the type of the the clumn with index [" + columnIndex + "] and name [" + fieldName + "]", e);
						}
						fieldMeta.setType(tpeClass);
						dataStore.getMetaData().addFiedMeta(fieldMeta);
					}
				}

				dataStore.appendRecord(record);
				recCount++;
			}
			logger.debug("Readed [" + recCount + "] records");
			logger.debug("Data readed succcesfully");

			if (this.isCalculateResultNumberEnabled()) {
				logger.debug("Calculation of result set number is enabled");
				resultNumber = getResultNumber(rs, maxRecToParse, recCount);
				dataStore.getMetaData().setProperty("resultNumber", new Integer(resultNumber));
			} else {
				logger.debug("Calculation of result set number is NOT enabled");
			}

		} catch (SQLException e) {
			logger.error("An unexpected error occured while reading resultset", e);
		} finally {
			logger.debug("OUT");
		}

		return dataStore;
	}

	private int getResultNumber(ResultSet rs, long maxRecToParse, int recCount) throws SQLException {
		logger.debug("IN");

		int toReturn = recCount;
		int remaining = 0;

		logger.debug("resultset type [" + rs.getType() + "] (" + (rs.getType() == ResultSet.TYPE_FORWARD_ONLY) + ")");
		if (rs.getType() == ResultSet.TYPE_FORWARD_ONLY) {

			int recordsReaded = 0;
			if (recCount < maxRecToParse) {
				// records read where less then max records to read, therefore the resultset has been completely read
				recordsReaded = recCount;
			} else {
				recordsReaded = getFetchSize();
				// recordsCount = rs.getRow();
				while (rs.next()) {
					remaining++;
					// do nothing, just scroll result set
				}
			}

			toReturn = getOffset() + recordsReaded + remaining;
		}

		logger.debug("Reading total record numeber is equal to [" + toReturn + "]");
		logger.debug("OUT " + toReturn);
		return toReturn;
	}

	private Class getType(String type) throws SpagoBIEngineException {
		type = type.toUpperCase();
		if (type.equals("STRING") || type.equals("CHAR") || type.equals("VARCHAR") || type.equals("LONGVARCHAR")) {
			return String.class;
		} else if (type.equals("NUMERIC") || type.equals("DECIMAL")) {
			return java.math.BigDecimal.class;
		} else if (type.equals("BIT") || type.equals("BOOLEAN")) {
			return Boolean.class;
		} else if (type.equals("TINYINT")) {
			return Byte.class;
		} else if (type.equals("SMALLINT")) {
			return Short.class;
		} else if (type.equals("INT") || type.equals("INTEGER")) {
			return Integer.class;
		} else if (type.equals("BIGINT")) {
			return Long.class;
		} else if (type.equals("REAL")) {
			return Float.class;
		} else if (type.equals("FLOAT") || type.equals("DOUBLE") || type.equals("DOUBLE PRECISION")) {
			return Double.class;
		} else if (type.equals("BINARY") || type.equals("VARBINARY") || type.equals("LONGVARBINARY")) {
			return String.class;
		} else if (type.equals("DATE") || type.equals("TIME")) {
			return java.sql.Date.class;
		} else if (type.equals("TIME")) {
			return java.sql.Time.class;
		} else if (type.equals("TIMESTAMP")) {
			return java.sql.Timestamp.class;
		} else if (type.equals("ARRAY")) {
			return String.class;
		} else if (type.equals("STRUCT")) {
			return String.class;
		} else if (type.equals("REF")) {
			return Ref.class;
		} else if (type.equals("DATALINK")) {
			return java.net.URL.class;
		} else if (type.equals("MAP")) {
			return String.class;
		}
		throw new SpagoBIEngineException("Can not find a java type for [" + type + "]");
	}

}
