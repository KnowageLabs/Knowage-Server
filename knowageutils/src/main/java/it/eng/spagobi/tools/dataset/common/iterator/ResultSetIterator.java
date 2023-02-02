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
package it.eng.spagobi.tools.dataset.common.iterator;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import it.eng.spagobi.tools.dataset.common.datastore.Field;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.datastore.Record;
import it.eng.spagobi.tools.dataset.common.metadata.FieldMetadata;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.MetaData;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class ResultSetIterator implements DataIterator {

	private final Connection conn;
	private final Statement stmt;
	private final ResultSet rs;
	private final IMetaData metadata;
	private final int columnCount;

	/**
	 * IMPORTANT!!! An {@code Iterator} has methods {@code hasNext()} and {@code next()} while a {@code ResultSet} object has only {@code next()}, that behaves
	 * like both {@code Iterator} {@code hasNext()} and {@code next()} at the same time, since it returns true if there are other elements while it is moving
	 * forward its internal cursor. But {@code Iterator.hasNext()} is not supposed to move forward, therefore it cannot invoke {@code ResultSet.next()}
	 * method!!! In order to harmonize those API, {@code ResultSetIterator} loads first record into {@code nextRow} variable during initialization (within the
	 * constructor) using the {@code loadNextRow} method; when {@code Iterator.next()} method is invoked, we get values from {@code nextRow} variable and then
	 * we move forward with {@code loadNextRow} method, overriding the values into {@code nextRow} variable or setting it to null in case there no more
	 * elements. {@code Iterator.hasNext()} method simply checks that {@code nextRow} is not null.
	 */
	private Object[] nextRow;

	public ResultSetIterator(Connection conn, Statement stmt, ResultSet rs) throws ClassNotFoundException, SQLException {
		this.conn = conn;
		this.stmt = stmt;
		this.rs = rs;
		this.columnCount = rs.getMetaData().getColumnCount();
		this.metadata = getMetadata(rs.getMetaData());
		loadNextRow();
	}

	private void loadNextRow() throws SQLException {
		if (rs.next()) {
			int columnsNumber = rs.getMetaData().getColumnCount();
			Object[] row = new Object[columnsNumber];
			for (int columnIndex = 1; columnIndex <= columnsNumber; columnIndex++) {
				row[columnIndex - 1] = rs.getObject(columnIndex);
			}
			this.nextRow = row;
		} else {
			this.nextRow = null;
		}
	}

	@Override
	public boolean hasNext() {
		return nextRow != null;
	}

	@Override
	public IRecord next() {
		if (!hasNext()) {
			throw new SpagoBIRuntimeException("ResultSet is empty or it was already scrolled completely");
		}
		int columnIndex = 0;
		try {
			IRecord record = new Record();
			for (columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
				Object columnValue = nextRow[columnIndex - 1];
				IField field = new Field(columnValue);
				if (columnValue != null) {
					metadata.getFieldMeta(columnIndex - 1).setType(columnValue.getClass());
				}
				record.appendField(field);
			}
			loadNextRow();
			return record;
		} catch (SQLException e) {
			throw new SpagoBIRuntimeException(e);
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("This operation has to be overriden by subclasses in order to be used.");
	}

	@Override
	public void close() {
		try {
			if (rs != null) {
				rs.close();
			}
			if (stmt != null) {
				stmt.close();
			}
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			throw new SpagoBIRuntimeException(e);
		}
	}

	private IMetaData getMetadata(ResultSetMetaData rsMetadata) {
		IMetaData metadata = new MetaData();
		FieldMetadata fieldMeta;
		String fieldName;
		int fieldSize;
		String fieldType;
		int columnIndex;
		try {
			for (columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
				fieldMeta = new FieldMetadata();
				fieldName = rs.getMetaData().getColumnLabel(columnIndex);
				fieldSize = rs.getMetaData().getColumnDisplaySize(columnIndex);
				fieldType = rs.getMetaData().getColumnClassName(columnIndex);
				fieldMeta.setName(fieldName);
				fieldMeta.getProperties().put("displaySize", fieldSize);
				if (fieldType != null) {
					if ("double".equals(fieldType.trim())) {
						fieldMeta.setType(Class.forName("java.lang.Double"));
					} else if ("int".equals(fieldType.trim())) {
						fieldMeta.setType(Class.forName("java.lang.Integer"));
					} else if ("String".equals(fieldType.trim())) {
						fieldMeta.setType(Class.forName("java.lang.String"));
					} else {
						fieldMeta.setType(Class.forName(fieldType.trim()));
					}
				}
				metadata.addFiedMeta(fieldMeta);
			}
		} catch (SQLException | ClassNotFoundException e) {
			throw new SpagoBIRuntimeException(e);
		}
		return metadata;
	}

	@Override
	public IMetaData getMetaData() {
		return metadata;
	}

}
