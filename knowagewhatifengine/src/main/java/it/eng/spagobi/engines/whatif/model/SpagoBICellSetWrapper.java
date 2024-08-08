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
		this.modifiedCells = new HashMap<>();
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

	@Override
	public Cell getCell(List<Integer> coordinates) {
		Cell cell = wrapped.getCell(coordinates);
		int ordinal = cell.getOrdinal();
		if (wasModified(ordinal)) {
			SpagoBICellWrapper toReturn = this.getModified(ordinal);
			return toReturn;
		}
		return new SpagoBICellWrapper(cell, this);
	}

	@Override
	public Cell getCell(int ordinal) {
		if (wasModified(ordinal)) {
			SpagoBICellWrapper toReturn = this.getModified(ordinal);
			return toReturn;
		}
		Cell cell = wrapped.getCell(ordinal);
		return new SpagoBICellWrapper(cell, this);
	}

	@Override
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

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return wrapped.unwrap(iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return wrapped.isWrapperFor(iface);
	}

	@Override
	public OlapStatement getStatement() throws SQLException {
		return wrapped.getStatement();
	}

	@Override
	public CellSetMetaData getMetaData() throws OlapException {
		return wrapped.getMetaData();
	}

	@Override
	public List<CellSetAxis> getAxes() {
		return wrapped.getAxes();
	}

	@Override
	public CellSetAxis getFilterAxis() {
		return wrapped.getFilterAxis();
	}

	@Override
	public boolean next() throws SQLException {
		return wrapped.next();
	}

	@Override
	public List<Integer> ordinalToCoordinates(int ordinal) {
		return wrapped.ordinalToCoordinates(ordinal);
	}

	@Override
	public int coordinatesToOrdinal(List<Integer> coordinates) {
		return wrapped.coordinatesToOrdinal(coordinates);
	}

	@Override
	public void close() throws SQLException {
		wrapped.close();
	}

	@Override
	public boolean wasNull() throws SQLException {
		return wrapped.wasNull();
	}

	@Override
	public String getString(int columnIndex) throws SQLException {
		return wrapped.getString(columnIndex);
	}

	@Override
	public boolean getBoolean(int columnIndex) throws SQLException {
		return wrapped.getBoolean(columnIndex);
	}

	@Override
	public byte getByte(int columnIndex) throws SQLException {
		return wrapped.getByte(columnIndex);
	}

	@Override
	public short getShort(int columnIndex) throws SQLException {
		return wrapped.getShort(columnIndex);
	}

	@Override
	public int getInt(int columnIndex) throws SQLException {
		return wrapped.getInt(columnIndex);
	}

	@Override
	public long getLong(int columnIndex) throws SQLException {
		return wrapped.getLong(columnIndex);
	}

	@Override
	public float getFloat(int columnIndex) throws SQLException {
		return wrapped.getFloat(columnIndex);
	}

	@Override
	public double getDouble(int columnIndex) throws SQLException {
		return wrapped.getDouble(columnIndex);
	}

	@Override
	public BigDecimal getBigDecimal(int columnIndex, int scale)
			throws SQLException {
		return wrapped.getBigDecimal(columnIndex, scale);
	}

	@Override
	public byte[] getBytes(int columnIndex) throws SQLException {
		return wrapped.getBytes(columnIndex);
	}

	@Override
	public Date getDate(int columnIndex) throws SQLException {
		return wrapped.getDate(columnIndex);
	}

	@Override
	public Time getTime(int columnIndex) throws SQLException {
		return wrapped.getTime(columnIndex);
	}

	@Override
	public Timestamp getTimestamp(int columnIndex) throws SQLException {
		return wrapped.getTimestamp(columnIndex);
	}

	@Override
	public InputStream getAsciiStream(int columnIndex) throws SQLException {
		return wrapped.getAsciiStream(columnIndex);
	}

	@Override
	public InputStream getUnicodeStream(int columnIndex) throws SQLException {
		return wrapped.getUnicodeStream(columnIndex);
	}

	@Override
	public InputStream getBinaryStream(int columnIndex) throws SQLException {
		return wrapped.getBinaryStream(columnIndex);
	}

	@Override
	public String getString(String columnLabel) throws SQLException {
		return wrapped.getString(columnLabel);
	}

	@Override
	public boolean getBoolean(String columnLabel) throws SQLException {
		return wrapped.getBoolean(columnLabel);
	}

	@Override
	public byte getByte(String columnLabel) throws SQLException {
		return wrapped.getByte(columnLabel);
	}

	@Override
	public short getShort(String columnLabel) throws SQLException {
		return wrapped.getShort(columnLabel);
	}

	@Override
	public int getInt(String columnLabel) throws SQLException {
		return wrapped.getInt(columnLabel);
	}

	@Override
	public long getLong(String columnLabel) throws SQLException {
		return wrapped.getLong(columnLabel);
	}

	@Override
	public float getFloat(String columnLabel) throws SQLException {
		return wrapped.getFloat(columnLabel);
	}

	@Override
	public double getDouble(String columnLabel) throws SQLException {
		return wrapped.getDouble(columnLabel);
	}

	@Override
	public BigDecimal getBigDecimal(String columnLabel, int scale)
			throws SQLException {
		return wrapped.getBigDecimal(columnLabel, scale);
	}

	@Override
	public byte[] getBytes(String columnLabel) throws SQLException {
		return wrapped.getBytes(columnLabel);
	}

	@Override
	public Date getDate(String columnLabel) throws SQLException {
		return wrapped.getDate(columnLabel);
	}

	@Override
	public Time getTime(String columnLabel) throws SQLException {
		return wrapped.getTime(columnLabel);
	}

	@Override
	public Timestamp getTimestamp(String columnLabel) throws SQLException {
		return wrapped.getTimestamp(columnLabel);
	}

	@Override
	public InputStream getAsciiStream(String columnLabel) throws SQLException {
		return wrapped.getAsciiStream(columnLabel);
	}

	@Override
	public InputStream getUnicodeStream(String columnLabel) throws SQLException {
		return wrapped.getUnicodeStream(columnLabel);
	}

	@Override
	public InputStream getBinaryStream(String columnLabel) throws SQLException {
		return wrapped.getBinaryStream(columnLabel);
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		return wrapped.getWarnings();
	}

	@Override
	public void clearWarnings() throws SQLException {
		wrapped.clearWarnings();
	}

	@Override
	public String getCursorName() throws SQLException {
		return wrapped.getCursorName();
	}

	@Override
	public Object getObject(int columnIndex) throws SQLException {
		return wrapped.getObject(columnIndex);
	}

	@Override
	public Object getObject(String columnLabel) throws SQLException {
		return wrapped.getObject(columnLabel);
	}

	@Override
	public int findColumn(String columnLabel) throws SQLException {
		return wrapped.findColumn(columnLabel);
	}

	@Override
	public Reader getCharacterStream(int columnIndex) throws SQLException {
		return wrapped.getCharacterStream(columnIndex);
	}

	@Override
	public Reader getCharacterStream(String columnLabel) throws SQLException {
		return wrapped.getCharacterStream(columnLabel);
	}

	@Override
	public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
		return wrapped.getBigDecimal(columnIndex);
	}

	@Override
	public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
		return wrapped.getBigDecimal(columnLabel);
	}

	@Override
	public boolean isBeforeFirst() throws SQLException {
		return wrapped.isBeforeFirst();
	}

	@Override
	public boolean isAfterLast() throws SQLException {
		return wrapped.isAfterLast();
	}

	@Override
	public boolean isFirst() throws SQLException {
		return wrapped.isFirst();
	}

	@Override
	public boolean isLast() throws SQLException {
		return wrapped.isLast();
	}

	@Override
	public void beforeFirst() throws SQLException {
		wrapped.beforeFirst();
	}

	@Override
	public void afterLast() throws SQLException {
		wrapped.afterLast();
	}

	@Override
	public boolean first() throws SQLException {
		return wrapped.first();
	}

	@Override
	public boolean last() throws SQLException {
		return wrapped.last();
	}

	@Override
	public int getRow() throws SQLException {
		return wrapped.getRow();
	}

	@Override
	public boolean absolute(int row) throws SQLException {
		return wrapped.absolute(row);
	}

	@Override
	public boolean relative(int rows) throws SQLException {
		return wrapped.relative(rows);
	}

	@Override
	public boolean previous() throws SQLException {
		return wrapped.previous();
	}

	@Override
	public void setFetchDirection(int direction) throws SQLException {
		wrapped.setFetchDirection(direction);
	}

	@Override
	public int getFetchDirection() throws SQLException {
		return wrapped.getFetchDirection();
	}

	@Override
	public void setFetchSize(int rows) throws SQLException {
		wrapped.setFetchSize(rows);
	}

	@Override
	public int getFetchSize() throws SQLException {
		return wrapped.getFetchSize();
	}

	@Override
	public int getType() throws SQLException {
		return wrapped.getType();
	}

	@Override
	public int getConcurrency() throws SQLException {
		return wrapped.getConcurrency();
	}

	@Override
	public boolean rowUpdated() throws SQLException {
		return wrapped.rowUpdated();
	}

	@Override
	public boolean rowInserted() throws SQLException {
		return wrapped.rowInserted();
	}

	@Override
	public boolean rowDeleted() throws SQLException {
		return wrapped.rowDeleted();
	}

	@Override
	public void updateNull(int columnIndex) throws SQLException {
		wrapped.updateNull(columnIndex);
	}

	@Override
	public void updateBoolean(int columnIndex, boolean x) throws SQLException {
		wrapped.updateBoolean(columnIndex, x);
	}

	@Override
	public void updateByte(int columnIndex, byte x) throws SQLException {
		wrapped.updateByte(columnIndex, x);
	}

	@Override
	public void updateShort(int columnIndex, short x) throws SQLException {
		wrapped.updateShort(columnIndex, x);
	}

	@Override
	public void updateInt(int columnIndex, int x) throws SQLException {
		wrapped.updateInt(columnIndex, x);
	}

	@Override
	public void updateLong(int columnIndex, long x) throws SQLException {
		wrapped.updateLong(columnIndex, x);
	}

	@Override
	public void updateFloat(int columnIndex, float x) throws SQLException {
		wrapped.updateFloat(columnIndex, x);
	}

	@Override
	public void updateDouble(int columnIndex, double x) throws SQLException {
		wrapped.updateDouble(columnIndex, x);
	}

	@Override
	public void updateBigDecimal(int columnIndex, BigDecimal x)
			throws SQLException {
		wrapped.updateBigDecimal(columnIndex, x);
	}

	@Override
	public void updateString(int columnIndex, String x) throws SQLException {
		wrapped.updateString(columnIndex, x);
	}

	@Override
	public void updateBytes(int columnIndex, byte[] x) throws SQLException {
		wrapped.updateBytes(columnIndex, x);
	}

	@Override
	public void updateDate(int columnIndex, Date x) throws SQLException {
		wrapped.updateDate(columnIndex, x);
	}

	@Override
	public void updateTime(int columnIndex, Time x) throws SQLException {
		wrapped.updateTime(columnIndex, x);
	}

	@Override
	public void updateTimestamp(int columnIndex, Timestamp x)
			throws SQLException {
		wrapped.updateTimestamp(columnIndex, x);
	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x, int length)
			throws SQLException {
		wrapped.updateAsciiStream(columnIndex, x, length);
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x, int length)
			throws SQLException {
		wrapped.updateBinaryStream(columnIndex, x, length);
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x, int length)
			throws SQLException {
		wrapped.updateCharacterStream(columnIndex, x, length);
	}

	@Override
	public void updateObject(int columnIndex, Object x, int scaleOrLength)
			throws SQLException {
		wrapped.updateObject(columnIndex, x, scaleOrLength);
	}

	@Override
	public void updateObject(int columnIndex, Object x) throws SQLException {
		wrapped.updateObject(columnIndex, x);
	}

	@Override
	public void updateNull(String columnLabel) throws SQLException {
		wrapped.updateNull(columnLabel);
	}

	@Override
	public void updateBoolean(String columnLabel, boolean x)
			throws SQLException {
		wrapped.updateBoolean(columnLabel, x);
	}

	@Override
	public void updateByte(String columnLabel, byte x) throws SQLException {
		wrapped.updateByte(columnLabel, x);
	}

	@Override
	public void updateShort(String columnLabel, short x) throws SQLException {
		wrapped.updateShort(columnLabel, x);
	}

	@Override
	public void updateInt(String columnLabel, int x) throws SQLException {
		wrapped.updateInt(columnLabel, x);
	}

	@Override
	public void updateLong(String columnLabel, long x) throws SQLException {
		wrapped.updateLong(columnLabel, x);
	}

	@Override
	public void updateFloat(String columnLabel, float x) throws SQLException {
		wrapped.updateFloat(columnLabel, x);
	}

	@Override
	public void updateDouble(String columnLabel, double x) throws SQLException {
		wrapped.updateDouble(columnLabel, x);
	}

	@Override
	public void updateBigDecimal(String columnLabel, BigDecimal x)
			throws SQLException {
		wrapped.updateBigDecimal(columnLabel, x);
	}

	@Override
	public void updateString(String columnLabel, String x) throws SQLException {
		wrapped.updateString(columnLabel, x);
	}

	@Override
	public void updateBytes(String columnLabel, byte[] x) throws SQLException {
		wrapped.updateBytes(columnLabel, x);
	}

	@Override
	public void updateDate(String columnLabel, Date x) throws SQLException {
		wrapped.updateDate(columnLabel, x);
	}

	@Override
	public void updateTime(String columnLabel, Time x) throws SQLException {
		wrapped.updateTime(columnLabel, x);
	}

	@Override
	public void updateTimestamp(String columnLabel, Timestamp x)
			throws SQLException {
		wrapped.updateTimestamp(columnLabel, x);
	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x, int length)
			throws SQLException {
		wrapped.updateAsciiStream(columnLabel, x, length);
	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x, int length)
			throws SQLException {
		wrapped.updateBinaryStream(columnLabel, x, length);
	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader,
			int length) throws SQLException {
		wrapped.updateCharacterStream(columnLabel, reader, length);
	}

	@Override
	public void updateObject(String columnLabel, Object x, int scaleOrLength)
			throws SQLException {
		wrapped.updateObject(columnLabel, x, scaleOrLength);
	}

	@Override
	public void updateObject(String columnLabel, Object x) throws SQLException {
		wrapped.updateObject(columnLabel, x);
	}

	@Override
	public void insertRow() throws SQLException {
		wrapped.insertRow();
	}

	@Override
	public void updateRow() throws SQLException {
		wrapped.updateRow();
	}

	@Override
	public void deleteRow() throws SQLException {
		wrapped.deleteRow();
	}

	@Override
	public void refreshRow() throws SQLException {
		wrapped.refreshRow();
	}

	@Override
	public void cancelRowUpdates() throws SQLException {
		wrapped.cancelRowUpdates();
	}

	@Override
	public void moveToInsertRow() throws SQLException {
		wrapped.moveToInsertRow();
	}

	@Override
	public void moveToCurrentRow() throws SQLException {
		wrapped.moveToCurrentRow();
	}

	@Override
	public Object getObject(int columnIndex, Map<String, Class<?>> map)
			throws SQLException {
		return wrapped.getObject(columnIndex, map);
	}

	@Override
	public Ref getRef(int columnIndex) throws SQLException {
		return wrapped.getRef(columnIndex);
	}

	@Override
	public Blob getBlob(int columnIndex) throws SQLException {
		return wrapped.getBlob(columnIndex);
	}

	@Override
	public Clob getClob(int columnIndex) throws SQLException {
		return wrapped.getClob(columnIndex);
	}

	@Override
	public Array getArray(int columnIndex) throws SQLException {
		return wrapped.getArray(columnIndex);
	}

	@Override
	public Object getObject(String columnLabel, Map<String, Class<?>> map)
			throws SQLException {
		return wrapped.getObject(columnLabel, map);
	}

	@Override
	public Ref getRef(String columnLabel) throws SQLException {
		return wrapped.getRef(columnLabel);
	}

	@Override
	public Blob getBlob(String columnLabel) throws SQLException {
		return wrapped.getBlob(columnLabel);
	}

	@Override
	public Clob getClob(String columnLabel) throws SQLException {
		return wrapped.getClob(columnLabel);
	}

	@Override
	public Array getArray(String columnLabel) throws SQLException {
		return wrapped.getArray(columnLabel);
	}

	@Override
	public Date getDate(int columnIndex, Calendar cal) throws SQLException {
		return wrapped.getDate(columnIndex, cal);
	}

	@Override
	public Date getDate(String columnLabel, Calendar cal) throws SQLException {
		return wrapped.getDate(columnLabel, cal);
	}

	@Override
	public Time getTime(int columnIndex, Calendar cal) throws SQLException {
		return wrapped.getTime(columnIndex, cal);
	}

	@Override
	public Time getTime(String columnLabel, Calendar cal) throws SQLException {
		return wrapped.getTime(columnLabel, cal);
	}

	@Override
	public Timestamp getTimestamp(int columnIndex, Calendar cal)
			throws SQLException {
		return wrapped.getTimestamp(columnIndex, cal);
	}

	@Override
	public Timestamp getTimestamp(String columnLabel, Calendar cal)
			throws SQLException {
		return wrapped.getTimestamp(columnLabel, cal);
	}

	@Override
	public URL getURL(int columnIndex) throws SQLException {
		return wrapped.getURL(columnIndex);
	}

	@Override
	public URL getURL(String columnLabel) throws SQLException {
		return wrapped.getURL(columnLabel);
	}

	@Override
	public void updateRef(int columnIndex, Ref x) throws SQLException {
		wrapped.updateRef(columnIndex, x);
	}

	@Override
	public void updateRef(String columnLabel, Ref x) throws SQLException {
		wrapped.updateRef(columnLabel, x);
	}

	@Override
	public void updateBlob(int columnIndex, Blob x) throws SQLException {
		wrapped.updateBlob(columnIndex, x);
	}

	@Override
	public void updateBlob(String columnLabel, Blob x) throws SQLException {
		wrapped.updateBlob(columnLabel, x);
	}

	@Override
	public void updateClob(int columnIndex, Clob x) throws SQLException {
		wrapped.updateClob(columnIndex, x);
	}

	@Override
	public void updateClob(String columnLabel, Clob x) throws SQLException {
		wrapped.updateClob(columnLabel, x);
	}

	@Override
	public void updateArray(int columnIndex, Array x) throws SQLException {
		wrapped.updateArray(columnIndex, x);
	}

	@Override
	public void updateArray(String columnLabel, Array x) throws SQLException {
		wrapped.updateArray(columnLabel, x);
	}

	@Override
	public RowId getRowId(int columnIndex) throws SQLException {
		return wrapped.getRowId(columnIndex);
	}

	@Override
	public RowId getRowId(String columnLabel) throws SQLException {
		return wrapped.getRowId(columnLabel);
	}

	@Override
	public void updateRowId(int columnIndex, RowId x) throws SQLException {
		wrapped.updateRowId(columnIndex, x);
	}

	@Override
	public void updateRowId(String columnLabel, RowId x) throws SQLException {
		wrapped.updateRowId(columnLabel, x);
	}

	@Override
	public int getHoldability() throws SQLException {
		return wrapped.getHoldability();
	}

	@Override
	public boolean isClosed() throws SQLException {
		return wrapped.isClosed();
	}

	@Override
	public void updateNString(int columnIndex, String nString)
			throws SQLException {
		wrapped.updateNString(columnIndex, nString);
	}

	@Override
	public void updateNString(String columnLabel, String nString)
			throws SQLException {
		wrapped.updateNString(columnLabel, nString);
	}

	@Override
	public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
		wrapped.updateNClob(columnIndex, nClob);
	}

	@Override
	public void updateNClob(String columnLabel, NClob nClob)
			throws SQLException {
		wrapped.updateNClob(columnLabel, nClob);
	}

	@Override
	public NClob getNClob(int columnIndex) throws SQLException {
		return wrapped.getNClob(columnIndex);
	}

	@Override
	public NClob getNClob(String columnLabel) throws SQLException {
		return wrapped.getNClob(columnLabel);
	}

	@Override
	public SQLXML getSQLXML(int columnIndex) throws SQLException {
		return wrapped.getSQLXML(columnIndex);
	}

	@Override
	public SQLXML getSQLXML(String columnLabel) throws SQLException {
		return wrapped.getSQLXML(columnLabel);
	}

	@Override
	public void updateSQLXML(int columnIndex, SQLXML xmlObject)
			throws SQLException {
		wrapped.updateSQLXML(columnIndex, xmlObject);
	}

	@Override
	public void updateSQLXML(String columnLabel, SQLXML xmlObject)
			throws SQLException {
		wrapped.updateSQLXML(columnLabel, xmlObject);
	}

	@Override
	public String getNString(int columnIndex) throws SQLException {
		return wrapped.getNString(columnIndex);
	}

	@Override
	public String getNString(String columnLabel) throws SQLException {
		return wrapped.getNString(columnLabel);
	}

	@Override
	public Reader getNCharacterStream(int columnIndex) throws SQLException {
		return wrapped.getNCharacterStream(columnIndex);
	}

	@Override
	public Reader getNCharacterStream(String columnLabel) throws SQLException {
		return wrapped.getNCharacterStream(columnLabel);
	}

	@Override
	public void updateNCharacterStream(int columnIndex, Reader x, long length)
			throws SQLException {
		wrapped.updateNCharacterStream(columnIndex, x, length);
	}

	@Override
	public void updateNCharacterStream(String columnLabel, Reader reader,
			long length) throws SQLException {
		wrapped.updateNCharacterStream(columnLabel, reader, length);
	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x, long length)
			throws SQLException {
		wrapped.updateAsciiStream(columnIndex, x, length);
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x, long length)
			throws SQLException {
		wrapped.updateBinaryStream(columnIndex, x, length);
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x, long length)
			throws SQLException {
		wrapped.updateCharacterStream(columnIndex, x, length);
	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x, long length)
			throws SQLException {
		wrapped.updateAsciiStream(columnLabel, x, length);
	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x,
			long length) throws SQLException {
		wrapped.updateBinaryStream(columnLabel, x, length);
	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader,
			long length) throws SQLException {
		wrapped.updateCharacterStream(columnLabel, reader, length);
	}

	@Override
	public void updateBlob(int columnIndex, InputStream inputStream, long length)
			throws SQLException {
		wrapped.updateBlob(columnIndex, inputStream, length);
	}

	@Override
	public void updateBlob(String columnLabel, InputStream inputStream,
			long length) throws SQLException {
		wrapped.updateBlob(columnLabel, inputStream, length);
	}

	@Override
	public void updateClob(int columnIndex, Reader reader, long length)
			throws SQLException {
		wrapped.updateClob(columnIndex, reader, length);
	}

	@Override
	public void updateClob(String columnLabel, Reader reader, long length)
			throws SQLException {
		wrapped.updateClob(columnLabel, reader, length);
	}

	@Override
	public void updateNClob(int columnIndex, Reader reader, long length)
			throws SQLException {
		wrapped.updateNClob(columnIndex, reader, length);
	}

	@Override
	public void updateNClob(String columnLabel, Reader reader, long length)
			throws SQLException {
		wrapped.updateNClob(columnLabel, reader, length);
	}

	@Override
	public void updateNCharacterStream(int columnIndex, Reader x)
			throws SQLException {
		wrapped.updateNCharacterStream(columnIndex, x);
	}

	@Override
	public void updateNCharacterStream(String columnLabel, Reader reader)
			throws SQLException {
		wrapped.updateNCharacterStream(columnLabel, reader);
	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x)
			throws SQLException {
		wrapped.updateAsciiStream(columnIndex, x);
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x)
			throws SQLException {
		wrapped.updateBinaryStream(columnIndex, x);
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x)
			throws SQLException {
		wrapped.updateCharacterStream(columnIndex, x);
	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x)
			throws SQLException {
		wrapped.updateAsciiStream(columnLabel, x);
	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x)
			throws SQLException {
		wrapped.updateBinaryStream(columnLabel, x);
	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader)
			throws SQLException {
		wrapped.updateCharacterStream(columnLabel, reader);
	}

	@Override
	public void updateBlob(int columnIndex, InputStream inputStream)
			throws SQLException {
		wrapped.updateBlob(columnIndex, inputStream);
	}

	@Override
	public void updateBlob(String columnLabel, InputStream inputStream)
			throws SQLException {
		wrapped.updateBlob(columnLabel, inputStream);
	}

	@Override
	public void updateClob(int columnIndex, Reader reader) throws SQLException {
		wrapped.updateClob(columnIndex, reader);
	}

	@Override
	public void updateClob(String columnLabel, Reader reader)
			throws SQLException {
		wrapped.updateClob(columnLabel, reader);
	}

	@Override
	public void updateNClob(int columnIndex, Reader reader) throws SQLException {
		wrapped.updateNClob(columnIndex, reader);
	}

	@Override
	public void updateNClob(String columnLabel, Reader reader)
			throws SQLException {
		wrapped.updateNClob(columnLabel, reader);
	}

	@Override
	public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
		return wrapped.getObject(columnIndex, type);
	}

	@Override
	public <T> T getObject(String columnLabel, Class<T> type)
			throws SQLException {
		return wrapped.getObject(columnLabel, type);
	}

}
