package it.eng.spagobi.tools.dataset.common.iterator;

import it.eng.spagobi.tools.dataset.common.datastore.Field;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.datastore.Record;
import it.eng.spagobi.tools.dataset.common.metadata.FieldMetadata;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.MetaData;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class ResultSetIterator implements DataIterator {

	private final Connection conn;
	private final Statement stmt;
	private final ResultSet rs;
	private final IMetaData metadata;
	private final int columnCount;

	public ResultSetIterator(Connection conn, Statement stmt, ResultSet rs) throws ClassNotFoundException, SQLException {
		this.conn = conn;
		this.stmt = stmt;
		this.rs = rs;
		this.columnCount = rs.getMetaData().getColumnCount();
		this.metadata = getMetadata(rs.getMetaData());
	}

	@Override
	public boolean hasNext() {
		try {
			return rs.next();
		} catch (SQLException e) {
			throw new SpagoBIRuntimeException(e);
		}
	}

	@Override
	public IRecord next() {
		int columnIndex = 0;
		try {
			IRecord record = new Record();
			for (columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
				Object columnValue = rs.getObject(columnIndex);
				IField field = new Field(columnValue);
				if (columnValue != null) {
					metadata.getFieldMeta(columnIndex - 1).setType(columnValue.getClass());
				}
				record.appendField(field);
			}
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
