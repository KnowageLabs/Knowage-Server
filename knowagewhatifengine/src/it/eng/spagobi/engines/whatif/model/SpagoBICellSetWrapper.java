/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

/**
 * @author Zerbetto Davide (davide.zerbetto@eng.it) 
 */

package it.eng.spagobi.engines.whatif.model;

import it.eng.spagobi.engines.whatif.model.transform.CellTransformation;
import it.eng.spagobi.engines.whatif.model.transform.CellTransformationsAnalyzer;
import it.eng.spagobi.engines.whatif.model.transform.CellTransformationsStack;
import it.eng.spagobi.engines.whatif.model.transform.algorithm.IAllocationAlgorithm;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.olap4j.Cell;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.CellSetMetaData;
import org.olap4j.OlapException;
import org.olap4j.OlapStatement;
import org.olap4j.Position;

/**
 * 
 * @author Zerbetto Davide (davide.zerbetto@eng.it)
 * 
 * @see org.olap4j.CellSet
 */
public class SpagoBICellSetWrapper implements CellSet {

	private CellSet wrapped = null;
	private SpagoBIPivotModel spagoBIPivotModel = null;
	private Map<Integer, SpagoBICellWrapper> modifiedCells = null;

	public SpagoBICellSetWrapper(CellSet cellSet, SpagoBIPivotModel spagoBIPivotModel) {
		this.wrapped = cellSet;
		this.spagoBIPivotModel = spagoBIPivotModel;
		this.modifiedCells = new HashMap<Integer, SpagoBICellWrapper>();
	}

	public SpagoBIPivotModel getSpagoBIPivotModel() {
		return spagoBIPivotModel;
	}

	/**
	 * Restores pending transformations (i.e. transformations not committed into
	 * the database)
	 * 
	 * @param stack
	 *            The pending transformations' stack
	 */
	public void restorePendingTransformations(CellTransformationsStack stack) {
		CellTransformationsAnalyzer analyzer = new CellTransformationsAnalyzer();
		CellTransformationsStack bestStack = analyzer.getShortestTransformationsStack(stack);
		Iterator<CellTransformation> iterator = bestStack.iterator();
		while (iterator.hasNext()) {
			CellTransformation transformation = iterator.next();
			this.restoreTranformation(transformation);
		}
	}

	/**
	 * Restores a pending transformation (i.e. a transformation not committed
	 * into the database)
	 * 
	 * @param transformation
	 *            The pending transformation
	 */
	public void restoreTranformation(CellTransformation transformation) {
		IAllocationAlgorithm algorithm = transformation.getAlgorithm();
		algorithm.apply(transformation.getCell(), transformation.getOldValue(), transformation.getNewValue(), this);
	}

	/**
	 * Applies a transformation
	 * 
	 * @param transformation
	 *            The transformation
	 */
	public void applyTranformation(CellTransformation transformation) {
		IAllocationAlgorithm algorithm = transformation.getAlgorithm();
		algorithm.apply(transformation.getCell(), transformation.getOldValue(), transformation.getNewValue(), this);
		spagoBIPivotModel.addPendingTransformation(transformation);
	}

	public Cell getCell(List<Integer> coordinates) {
		Cell cell = wrapped.getCell(coordinates);
		int ordinal = cell.getOrdinal();
		if (wasModified(ordinal)) {
			SpagoBICellWrapper toReturn = this.getModified(ordinal);
			return toReturn;
		}
		return new SpagoBICellWrapper(cell, this);
	}

	public Cell getCell(int ordinal) {
		if (wasModified(ordinal)) {
			SpagoBICellWrapper toReturn = this.getModified(ordinal);
			return toReturn;
		}
		Cell cell = wrapped.getCell(ordinal);
		return new SpagoBICellWrapper(cell, this);
	}

	public Cell getCell(Position... positions) {
		Cell cell = wrapped.getCell(positions);
		int ordinal = cell.getOrdinal();
		if (wasModified(ordinal)) {
			SpagoBICellWrapper toReturn = this.getModified(ordinal);
			return toReturn;
		}
		return new SpagoBICellWrapper(cell, this);
	}

	public void notifyModifiedCell(SpagoBICellWrapper wrappedCell) {
		int ordinal = wrappedCell.getOrdinal();
		modifiedCells.put(ordinal, wrappedCell);
	}

	private boolean wasModified(int ordinal) {
		return this.modifiedCells.containsKey(ordinal);
	}

	private SpagoBICellWrapper getModified(int ordinal) {
		return this.modifiedCells.get(ordinal);
	}

	public CellSet unwrap() {
		return wrapped;
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		return wrapped.unwrap(iface);
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return wrapped.isWrapperFor(iface);
	}

	public OlapStatement getStatement() throws SQLException {
		return wrapped.getStatement();
	}

	public CellSetMetaData getMetaData() throws OlapException {
		return wrapped.getMetaData();
	}

	public List<CellSetAxis> getAxes() {
		return wrapped.getAxes();
	}

	public CellSetAxis getFilterAxis() {
		return wrapped.getFilterAxis();
	}

	public boolean next() throws SQLException {
		return wrapped.next();
	}

	public List<Integer> ordinalToCoordinates(int ordinal) {
		return wrapped.ordinalToCoordinates(ordinal);
	}

	public int coordinatesToOrdinal(List<Integer> coordinates) {
		return wrapped.coordinatesToOrdinal(coordinates);
	}

	public void close() throws SQLException {
		wrapped.close();
	}

	public boolean wasNull() throws SQLException {
		return wrapped.wasNull();
	}

	public String getString(int columnIndex) throws SQLException {
		return wrapped.getString(columnIndex);
	}

	public boolean getBoolean(int columnIndex) throws SQLException {
		return wrapped.getBoolean(columnIndex);
	}

	public byte getByte(int columnIndex) throws SQLException {
		return wrapped.getByte(columnIndex);
	}

	public short getShort(int columnIndex) throws SQLException {
		return wrapped.getShort(columnIndex);
	}

	public int getInt(int columnIndex) throws SQLException {
		return wrapped.getInt(columnIndex);
	}

	public long getLong(int columnIndex) throws SQLException {
		return wrapped.getLong(columnIndex);
	}

	public float getFloat(int columnIndex) throws SQLException {
		return wrapped.getFloat(columnIndex);
	}

	public double getDouble(int columnIndex) throws SQLException {
		return wrapped.getDouble(columnIndex);
	}

	public BigDecimal getBigDecimal(int columnIndex, int scale)
			throws SQLException {
		return wrapped.getBigDecimal(columnIndex, scale);
	}

	public byte[] getBytes(int columnIndex) throws SQLException {
		return wrapped.getBytes(columnIndex);
	}

	public Date getDate(int columnIndex) throws SQLException {
		return wrapped.getDate(columnIndex);
	}

	public Time getTime(int columnIndex) throws SQLException {
		return wrapped.getTime(columnIndex);
	}

	public Timestamp getTimestamp(int columnIndex) throws SQLException {
		return wrapped.getTimestamp(columnIndex);
	}

	public InputStream getAsciiStream(int columnIndex) throws SQLException {
		return wrapped.getAsciiStream(columnIndex);
	}

	public InputStream getUnicodeStream(int columnIndex) throws SQLException {
		return wrapped.getUnicodeStream(columnIndex);
	}

	public InputStream getBinaryStream(int columnIndex) throws SQLException {
		return wrapped.getBinaryStream(columnIndex);
	}

	public String getString(String columnLabel) throws SQLException {
		return wrapped.getString(columnLabel);
	}

	public boolean getBoolean(String columnLabel) throws SQLException {
		return wrapped.getBoolean(columnLabel);
	}

	public byte getByte(String columnLabel) throws SQLException {
		return wrapped.getByte(columnLabel);
	}

	public short getShort(String columnLabel) throws SQLException {
		return wrapped.getShort(columnLabel);
	}

	public int getInt(String columnLabel) throws SQLException {
		return wrapped.getInt(columnLabel);
	}

	public long getLong(String columnLabel) throws SQLException {
		return wrapped.getLong(columnLabel);
	}

	public float getFloat(String columnLabel) throws SQLException {
		return wrapped.getFloat(columnLabel);
	}

	public double getDouble(String columnLabel) throws SQLException {
		return wrapped.getDouble(columnLabel);
	}

	public BigDecimal getBigDecimal(String columnLabel, int scale)
			throws SQLException {
		return wrapped.getBigDecimal(columnLabel, scale);
	}

	public byte[] getBytes(String columnLabel) throws SQLException {
		return wrapped.getBytes(columnLabel);
	}

	public Date getDate(String columnLabel) throws SQLException {
		return wrapped.getDate(columnLabel);
	}

	public Time getTime(String columnLabel) throws SQLException {
		return wrapped.getTime(columnLabel);
	}

	public Timestamp getTimestamp(String columnLabel) throws SQLException {
		return wrapped.getTimestamp(columnLabel);
	}

	public InputStream getAsciiStream(String columnLabel) throws SQLException {
		return wrapped.getAsciiStream(columnLabel);
	}

	public InputStream getUnicodeStream(String columnLabel) throws SQLException {
		return wrapped.getUnicodeStream(columnLabel);
	}

	public InputStream getBinaryStream(String columnLabel) throws SQLException {
		return wrapped.getBinaryStream(columnLabel);
	}

	public SQLWarning getWarnings() throws SQLException {
		return wrapped.getWarnings();
	}

	public void clearWarnings() throws SQLException {
		wrapped.clearWarnings();
	}

	public String getCursorName() throws SQLException {
		return wrapped.getCursorName();
	}

	public Object getObject(int columnIndex) throws SQLException {
		return wrapped.getObject(columnIndex);
	}

	public Object getObject(String columnLabel) throws SQLException {
		return wrapped.getObject(columnLabel);
	}

	public int findColumn(String columnLabel) throws SQLException {
		return wrapped.findColumn(columnLabel);
	}

	public Reader getCharacterStream(int columnIndex) throws SQLException {
		return wrapped.getCharacterStream(columnIndex);
	}

	public Reader getCharacterStream(String columnLabel) throws SQLException {
		return wrapped.getCharacterStream(columnLabel);
	}

	public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
		return wrapped.getBigDecimal(columnIndex);
	}

	public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
		return wrapped.getBigDecimal(columnLabel);
	}

	public boolean isBeforeFirst() throws SQLException {
		return wrapped.isBeforeFirst();
	}

	public boolean isAfterLast() throws SQLException {
		return wrapped.isAfterLast();
	}

	public boolean isFirst() throws SQLException {
		return wrapped.isFirst();
	}

	public boolean isLast() throws SQLException {
		return wrapped.isLast();
	}

	public void beforeFirst() throws SQLException {
		wrapped.beforeFirst();
	}

	public void afterLast() throws SQLException {
		wrapped.afterLast();
	}

	public boolean first() throws SQLException {
		return wrapped.first();
	}

	public boolean last() throws SQLException {
		return wrapped.last();
	}

	public int getRow() throws SQLException {
		return wrapped.getRow();
	}

	public boolean absolute(int row) throws SQLException {
		return wrapped.absolute(row);
	}

	public boolean relative(int rows) throws SQLException {
		return wrapped.relative(rows);
	}

	public boolean previous() throws SQLException {
		return wrapped.previous();
	}

	public void setFetchDirection(int direction) throws SQLException {
		wrapped.setFetchDirection(direction);
	}

	public int getFetchDirection() throws SQLException {
		return wrapped.getFetchDirection();
	}

	public void setFetchSize(int rows) throws SQLException {
		wrapped.setFetchSize(rows);
	}

	public int getFetchSize() throws SQLException {
		return wrapped.getFetchSize();
	}

	public int getType() throws SQLException {
		return wrapped.getType();
	}

	public int getConcurrency() throws SQLException {
		return wrapped.getConcurrency();
	}

	public boolean rowUpdated() throws SQLException {
		return wrapped.rowUpdated();
	}

	public boolean rowInserted() throws SQLException {
		return wrapped.rowInserted();
	}

	public boolean rowDeleted() throws SQLException {
		return wrapped.rowDeleted();
	}

	public void updateNull(int columnIndex) throws SQLException {
		wrapped.updateNull(columnIndex);
	}

	public void updateBoolean(int columnIndex, boolean x) throws SQLException {
		wrapped.updateBoolean(columnIndex, x);
	}

	public void updateByte(int columnIndex, byte x) throws SQLException {
		wrapped.updateByte(columnIndex, x);
	}

	public void updateShort(int columnIndex, short x) throws SQLException {
		wrapped.updateShort(columnIndex, x);
	}

	public void updateInt(int columnIndex, int x) throws SQLException {
		wrapped.updateInt(columnIndex, x);
	}

	public void updateLong(int columnIndex, long x) throws SQLException {
		wrapped.updateLong(columnIndex, x);
	}

	public void updateFloat(int columnIndex, float x) throws SQLException {
		wrapped.updateFloat(columnIndex, x);
	}

	public void updateDouble(int columnIndex, double x) throws SQLException {
		wrapped.updateDouble(columnIndex, x);
	}

	public void updateBigDecimal(int columnIndex, BigDecimal x)
			throws SQLException {
		wrapped.updateBigDecimal(columnIndex, x);
	}

	public void updateString(int columnIndex, String x) throws SQLException {
		wrapped.updateString(columnIndex, x);
	}

	public void updateBytes(int columnIndex, byte[] x) throws SQLException {
		wrapped.updateBytes(columnIndex, x);
	}

	public void updateDate(int columnIndex, Date x) throws SQLException {
		wrapped.updateDate(columnIndex, x);
	}

	public void updateTime(int columnIndex, Time x) throws SQLException {
		wrapped.updateTime(columnIndex, x);
	}

	public void updateTimestamp(int columnIndex, Timestamp x)
			throws SQLException {
		wrapped.updateTimestamp(columnIndex, x);
	}

	public void updateAsciiStream(int columnIndex, InputStream x, int length)
			throws SQLException {
		wrapped.updateAsciiStream(columnIndex, x, length);
	}

	public void updateBinaryStream(int columnIndex, InputStream x, int length)
			throws SQLException {
		wrapped.updateBinaryStream(columnIndex, x, length);
	}

	public void updateCharacterStream(int columnIndex, Reader x, int length)
			throws SQLException {
		wrapped.updateCharacterStream(columnIndex, x, length);
	}

	public void updateObject(int columnIndex, Object x, int scaleOrLength)
			throws SQLException {
		wrapped.updateObject(columnIndex, x, scaleOrLength);
	}

	public void updateObject(int columnIndex, Object x) throws SQLException {
		wrapped.updateObject(columnIndex, x);
	}

	public void updateNull(String columnLabel) throws SQLException {
		wrapped.updateNull(columnLabel);
	}

	public void updateBoolean(String columnLabel, boolean x)
			throws SQLException {
		wrapped.updateBoolean(columnLabel, x);
	}

	public void updateByte(String columnLabel, byte x) throws SQLException {
		wrapped.updateByte(columnLabel, x);
	}

	public void updateShort(String columnLabel, short x) throws SQLException {
		wrapped.updateShort(columnLabel, x);
	}

	public void updateInt(String columnLabel, int x) throws SQLException {
		wrapped.updateInt(columnLabel, x);
	}

	public void updateLong(String columnLabel, long x) throws SQLException {
		wrapped.updateLong(columnLabel, x);
	}

	public void updateFloat(String columnLabel, float x) throws SQLException {
		wrapped.updateFloat(columnLabel, x);
	}

	public void updateDouble(String columnLabel, double x) throws SQLException {
		wrapped.updateDouble(columnLabel, x);
	}

	public void updateBigDecimal(String columnLabel, BigDecimal x)
			throws SQLException {
		wrapped.updateBigDecimal(columnLabel, x);
	}

	public void updateString(String columnLabel, String x) throws SQLException {
		wrapped.updateString(columnLabel, x);
	}

	public void updateBytes(String columnLabel, byte[] x) throws SQLException {
		wrapped.updateBytes(columnLabel, x);
	}

	public void updateDate(String columnLabel, Date x) throws SQLException {
		wrapped.updateDate(columnLabel, x);
	}

	public void updateTime(String columnLabel, Time x) throws SQLException {
		wrapped.updateTime(columnLabel, x);
	}

	public void updateTimestamp(String columnLabel, Timestamp x)
			throws SQLException {
		wrapped.updateTimestamp(columnLabel, x);
	}

	public void updateAsciiStream(String columnLabel, InputStream x, int length)
			throws SQLException {
		wrapped.updateAsciiStream(columnLabel, x, length);
	}

	public void updateBinaryStream(String columnLabel, InputStream x, int length)
			throws SQLException {
		wrapped.updateBinaryStream(columnLabel, x, length);
	}

	public void updateCharacterStream(String columnLabel, Reader reader,
			int length) throws SQLException {
		wrapped.updateCharacterStream(columnLabel, reader, length);
	}

	public void updateObject(String columnLabel, Object x, int scaleOrLength)
			throws SQLException {
		wrapped.updateObject(columnLabel, x, scaleOrLength);
	}

	public void updateObject(String columnLabel, Object x) throws SQLException {
		wrapped.updateObject(columnLabel, x);
	}

	public void insertRow() throws SQLException {
		wrapped.insertRow();
	}

	public void updateRow() throws SQLException {
		wrapped.updateRow();
	}

	public void deleteRow() throws SQLException {
		wrapped.deleteRow();
	}

	public void refreshRow() throws SQLException {
		wrapped.refreshRow();
	}

	public void cancelRowUpdates() throws SQLException {
		wrapped.cancelRowUpdates();
	}

	public void moveToInsertRow() throws SQLException {
		wrapped.moveToInsertRow();
	}

	public void moveToCurrentRow() throws SQLException {
		wrapped.moveToCurrentRow();
	}

	public Object getObject(int columnIndex, Map<String, Class<?>> map)
			throws SQLException {
		return wrapped.getObject(columnIndex, map);
	}

	public Ref getRef(int columnIndex) throws SQLException {
		return wrapped.getRef(columnIndex);
	}

	public Blob getBlob(int columnIndex) throws SQLException {
		return wrapped.getBlob(columnIndex);
	}

	public Clob getClob(int columnIndex) throws SQLException {
		return wrapped.getClob(columnIndex);
	}

	public Array getArray(int columnIndex) throws SQLException {
		return wrapped.getArray(columnIndex);
	}

	public Object getObject(String columnLabel, Map<String, Class<?>> map)
			throws SQLException {
		return wrapped.getObject(columnLabel, map);
	}

	public Ref getRef(String columnLabel) throws SQLException {
		return wrapped.getRef(columnLabel);
	}

	public Blob getBlob(String columnLabel) throws SQLException {
		return wrapped.getBlob(columnLabel);
	}

	public Clob getClob(String columnLabel) throws SQLException {
		return wrapped.getClob(columnLabel);
	}

	public Array getArray(String columnLabel) throws SQLException {
		return wrapped.getArray(columnLabel);
	}

	public Date getDate(int columnIndex, Calendar cal) throws SQLException {
		return wrapped.getDate(columnIndex, cal);
	}

	public Date getDate(String columnLabel, Calendar cal) throws SQLException {
		return wrapped.getDate(columnLabel, cal);
	}

	public Time getTime(int columnIndex, Calendar cal) throws SQLException {
		return wrapped.getTime(columnIndex, cal);
	}

	public Time getTime(String columnLabel, Calendar cal) throws SQLException {
		return wrapped.getTime(columnLabel, cal);
	}

	public Timestamp getTimestamp(int columnIndex, Calendar cal)
			throws SQLException {
		return wrapped.getTimestamp(columnIndex, cal);
	}

	public Timestamp getTimestamp(String columnLabel, Calendar cal)
			throws SQLException {
		return wrapped.getTimestamp(columnLabel, cal);
	}

	public URL getURL(int columnIndex) throws SQLException {
		return wrapped.getURL(columnIndex);
	}

	public URL getURL(String columnLabel) throws SQLException {
		return wrapped.getURL(columnLabel);
	}

	public void updateRef(int columnIndex, Ref x) throws SQLException {
		wrapped.updateRef(columnIndex, x);
	}

	public void updateRef(String columnLabel, Ref x) throws SQLException {
		wrapped.updateRef(columnLabel, x);
	}

	public void updateBlob(int columnIndex, Blob x) throws SQLException {
		wrapped.updateBlob(columnIndex, x);
	}

	public void updateBlob(String columnLabel, Blob x) throws SQLException {
		wrapped.updateBlob(columnLabel, x);
	}

	public void updateClob(int columnIndex, Clob x) throws SQLException {
		wrapped.updateClob(columnIndex, x);
	}

	public void updateClob(String columnLabel, Clob x) throws SQLException {
		wrapped.updateClob(columnLabel, x);
	}

	public void updateArray(int columnIndex, Array x) throws SQLException {
		wrapped.updateArray(columnIndex, x);
	}

	public void updateArray(String columnLabel, Array x) throws SQLException {
		wrapped.updateArray(columnLabel, x);
	}

	public RowId getRowId(int columnIndex) throws SQLException {
		return wrapped.getRowId(columnIndex);
	}

	public RowId getRowId(String columnLabel) throws SQLException {
		return wrapped.getRowId(columnLabel);
	}

	public void updateRowId(int columnIndex, RowId x) throws SQLException {
		wrapped.updateRowId(columnIndex, x);
	}

	public void updateRowId(String columnLabel, RowId x) throws SQLException {
		wrapped.updateRowId(columnLabel, x);
	}

	public int getHoldability() throws SQLException {
		return wrapped.getHoldability();
	}

	public boolean isClosed() throws SQLException {
		return wrapped.isClosed();
	}

	public void updateNString(int columnIndex, String nString)
			throws SQLException {
		wrapped.updateNString(columnIndex, nString);
	}

	public void updateNString(String columnLabel, String nString)
			throws SQLException {
		wrapped.updateNString(columnLabel, nString);
	}

	public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
		wrapped.updateNClob(columnIndex, nClob);
	}

	public void updateNClob(String columnLabel, NClob nClob)
			throws SQLException {
		wrapped.updateNClob(columnLabel, nClob);
	}

	public NClob getNClob(int columnIndex) throws SQLException {
		return wrapped.getNClob(columnIndex);
	}

	public NClob getNClob(String columnLabel) throws SQLException {
		return wrapped.getNClob(columnLabel);
	}

	public SQLXML getSQLXML(int columnIndex) throws SQLException {
		return wrapped.getSQLXML(columnIndex);
	}

	public SQLXML getSQLXML(String columnLabel) throws SQLException {
		return wrapped.getSQLXML(columnLabel);
	}

	public void updateSQLXML(int columnIndex, SQLXML xmlObject)
			throws SQLException {
		wrapped.updateSQLXML(columnIndex, xmlObject);
	}

	public void updateSQLXML(String columnLabel, SQLXML xmlObject)
			throws SQLException {
		wrapped.updateSQLXML(columnLabel, xmlObject);
	}

	public String getNString(int columnIndex) throws SQLException {
		return wrapped.getNString(columnIndex);
	}

	public String getNString(String columnLabel) throws SQLException {
		return wrapped.getNString(columnLabel);
	}

	public Reader getNCharacterStream(int columnIndex) throws SQLException {
		return wrapped.getNCharacterStream(columnIndex);
	}

	public Reader getNCharacterStream(String columnLabel) throws SQLException {
		return wrapped.getNCharacterStream(columnLabel);
	}

	public void updateNCharacterStream(int columnIndex, Reader x, long length)
			throws SQLException {
		wrapped.updateNCharacterStream(columnIndex, x, length);
	}

	public void updateNCharacterStream(String columnLabel, Reader reader,
			long length) throws SQLException {
		wrapped.updateNCharacterStream(columnLabel, reader, length);
	}

	public void updateAsciiStream(int columnIndex, InputStream x, long length)
			throws SQLException {
		wrapped.updateAsciiStream(columnIndex, x, length);
	}

	public void updateBinaryStream(int columnIndex, InputStream x, long length)
			throws SQLException {
		wrapped.updateBinaryStream(columnIndex, x, length);
	}

	public void updateCharacterStream(int columnIndex, Reader x, long length)
			throws SQLException {
		wrapped.updateCharacterStream(columnIndex, x, length);
	}

	public void updateAsciiStream(String columnLabel, InputStream x, long length)
			throws SQLException {
		wrapped.updateAsciiStream(columnLabel, x, length);
	}

	public void updateBinaryStream(String columnLabel, InputStream x,
			long length) throws SQLException {
		wrapped.updateBinaryStream(columnLabel, x, length);
	}

	public void updateCharacterStream(String columnLabel, Reader reader,
			long length) throws SQLException {
		wrapped.updateCharacterStream(columnLabel, reader, length);
	}

	public void updateBlob(int columnIndex, InputStream inputStream, long length)
			throws SQLException {
		wrapped.updateBlob(columnIndex, inputStream, length);
	}

	public void updateBlob(String columnLabel, InputStream inputStream,
			long length) throws SQLException {
		wrapped.updateBlob(columnLabel, inputStream, length);
	}

	public void updateClob(int columnIndex, Reader reader, long length)
			throws SQLException {
		wrapped.updateClob(columnIndex, reader, length);
	}

	public void updateClob(String columnLabel, Reader reader, long length)
			throws SQLException {
		wrapped.updateClob(columnLabel, reader, length);
	}

	public void updateNClob(int columnIndex, Reader reader, long length)
			throws SQLException {
		wrapped.updateNClob(columnIndex, reader, length);
	}

	public void updateNClob(String columnLabel, Reader reader, long length)
			throws SQLException {
		wrapped.updateNClob(columnLabel, reader, length);
	}

	public void updateNCharacterStream(int columnIndex, Reader x)
			throws SQLException {
		wrapped.updateNCharacterStream(columnIndex, x);
	}

	public void updateNCharacterStream(String columnLabel, Reader reader)
			throws SQLException {
		wrapped.updateNCharacterStream(columnLabel, reader);
	}

	public void updateAsciiStream(int columnIndex, InputStream x)
			throws SQLException {
		wrapped.updateAsciiStream(columnIndex, x);
	}

	public void updateBinaryStream(int columnIndex, InputStream x)
			throws SQLException {
		wrapped.updateBinaryStream(columnIndex, x);
	}

	public void updateCharacterStream(int columnIndex, Reader x)
			throws SQLException {
		wrapped.updateCharacterStream(columnIndex, x);
	}

	public void updateAsciiStream(String columnLabel, InputStream x)
			throws SQLException {
		wrapped.updateAsciiStream(columnLabel, x);
	}

	public void updateBinaryStream(String columnLabel, InputStream x)
			throws SQLException {
		wrapped.updateBinaryStream(columnLabel, x);
	}

	public void updateCharacterStream(String columnLabel, Reader reader)
			throws SQLException {
		wrapped.updateCharacterStream(columnLabel, reader);
	}

	public void updateBlob(int columnIndex, InputStream inputStream)
			throws SQLException {
		wrapped.updateBlob(columnIndex, inputStream);
	}

	public void updateBlob(String columnLabel, InputStream inputStream)
			throws SQLException {
		wrapped.updateBlob(columnLabel, inputStream);
	}

	public void updateClob(int columnIndex, Reader reader) throws SQLException {
		wrapped.updateClob(columnIndex, reader);
	}

	public void updateClob(String columnLabel, Reader reader)
			throws SQLException {
		wrapped.updateClob(columnLabel, reader);
	}

	public void updateNClob(int columnIndex, Reader reader) throws SQLException {
		wrapped.updateNClob(columnIndex, reader);
	}

	public void updateNClob(String columnLabel, Reader reader)
			throws SQLException {
		wrapped.updateNClob(columnLabel, reader);
	}

	public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
		return wrapped.getObject(columnIndex, type);
	}

	public <T> T getObject(String columnLabel, Class<T> type)
			throws SQLException {
		return wrapped.getObject(columnLabel, type);
	}

}
