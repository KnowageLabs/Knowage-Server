/**

SpagoBI - The Business Intelligence Free Platform

Copyright (C) 2005-2011 Engineering Ingegneria Informatica S.p.A.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

**/

package spagobi.birt.oda.impl.server;

import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import org.eclipse.datatools.connectivity.oda.IBlob;
import org.eclipse.datatools.connectivity.oda.IClob;
import org.eclipse.datatools.connectivity.oda.IResultSet;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation class of IResultSet for an ODA runtime driver.
 * <br>
 * For demo purpose, the auto-generated method stubs have
 * hard-coded implementation that returns a pre-defined set
 * of meta-data and query results.
 * A custom ODA driver is expected to implement own data source specific
 * behavior in its place. 
 * 
 * @authors  Andrea Gioia (andrea.gioia@eng.it)
 */
public class ResultSet implements IResultSet
{
	private int maxRows;
    private int currentRowIndex;
    private IDataStore dataStore;
    private IMetaData dataStoreMeta;
    
    private static Logger logger = LoggerFactory.getLogger(ResultSet.class);
	
    public ResultSet(IDataStore dataStore, IMetaData dataStoreMeta) {
    	this.dataStore = dataStore;
    	this.dataStoreMeta = dataStoreMeta;
    	this.currentRowIndex = -1;
    }
    
	/*
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getMetaData()
	 */
	public IResultSetMetaData getMetaData() throws OdaException
	{
		logger.debug("IN getMetaData");
		return new ResultSetMetaData(dataStoreMeta);
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#setMaxRows(int)
	 */
	public void setMaxRows( int max ) throws OdaException
	{
		logger.debug("IN setMaxRows");
		maxRows = max;
	}
	
	/**
	 * Returns the maximum number of rows that can be fetched from this result set.
	 * @return the maximum number of rows to fetch.
	 */
	protected int getMaxRows()
	{
		logger.debug("IN-OUT getMaxRows");
		return maxRows;
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#next()
	 */
	public boolean next() throws OdaException
	{
		logger.debug("IN next");
        int maxRows = getMaxRows();
        int recCount = (int)dataStore.getRecordsCount();
        logger.debug("CurrentRowIndex: "+currentRowIndex);
        logger.debug("MaxRows: "+maxRows);
        logger.debug("recCount: "+recCount);
        if( maxRows <= 0 )  maxRows = recCount;
        
        maxRows = Math.min(maxRows,  recCount);
        
        currentRowIndex++;
       
        if( currentRowIndex < maxRows )
        {   
        	logger.debug("Ancora Righe");
            return true;
        }
        logger.debug("OUT next");
        return false;        
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#close()
	 */
	public void close() throws OdaException
	{
		logger.debug("IN close");
        // TODO Auto-generated method stub       
		currentRowIndex = -1;     // reset row counter
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getRow()
	 */
	public int getRow() throws OdaException
	{
		logger.debug("IN-OUT getRow "+currentRowIndex);
		return currentRowIndex;
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getString(int)
	 */
	public String getString( int index ) throws OdaException {
		logger.debug("IN getString");
		IRecord record = dataStore.getRecordAt(getRow());
		
		if(record == null){
			logger.debug("ODA Exception Record null");
			throw (OdaException) new OdaException("Impossible to read row [" + getRow() + "]. The resultset contains [" + dataStore.getRecordsCount() + "] rows");
		}
		/*String fieldName = dataStoreMeta.getFieldName(index-1);
		logger.debug("fieldName: "+fieldName);
		int fieldIndex = dataStoreMeta.getFieldIndex(fieldName);	*/
		int fieldIndex = index-1;
		logger.debug("fieldIndex: "+fieldIndex);
		
		String toReturn = null;
		try {
			IField field = record.getFieldAt(fieldIndex);
			toReturn = "" + field.getValue();
		} catch (IndexOutOfBoundsException e) {
			logger.warn("Column index not found in the record",e);
		}

		logger.debug("OUT getString: "+toReturn);
		return toReturn;
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getString(java.lang.String)
	 */
	public String getString( String columnName ) throws OdaException {
		logger.debug("IN getString");
	    return getString( findColumn( columnName ) );
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getInt(int)
	 */
	public int getInt( int index ) throws OdaException {
		logger.debug("IN getInt");
		IRecord record = dataStore.getRecordAt(getRow());
		
		if(record == null){
			throw (OdaException) new OdaException("Impossible to read row [" + getRow() + "]. The resultset contains [" + dataStore.getRecordsCount() + "] rows");
		}

		/*String fieldName = dataStoreMeta.getFieldName(index-1);
		int fieldIndex = dataStore.getMetaData().getFieldIndex(fieldName);	*/
		int fieldIndex = index-1;
		IField field = null;
		int value = 0;
		try {
			field = record.getFieldAt(fieldIndex);
			
			if(field == null){
				throw (OdaException) new OdaException("Impossible to read column [" + (index-1) + "]. The resultset contains [" + dataStore.getMetaData().getFieldCount() + "] columns");
			}
			
			try {
				value = Integer.parseInt( "" + field.getValue() );
			} catch(Throwable t) {
				//throw (OdaException) new OdaException("Impossible to convert column value [" + field.getValue() +"] to integer").initCause(t);
				logger.warn("Impossible to convert column value [" + field.getValue() +"] to integer",t);
			}
		} catch (IndexOutOfBoundsException e) {
			logger.warn("Column index not found in the record",e);
			value = 0;
		}
		

		

		
        return value;
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getInt(java.lang.String)
	 */
	public int getInt( String columnName ) throws OdaException {
		logger.debug("IN getInt");
		int value = -1;
		try {
			value = getInt( findColumn( columnName ) );
		} catch(Throwable t) {
			throw (OdaException) new OdaException("Impossible to convert column [" + columnName +"] to integer").initCause(t);
		}
		
	    return value;
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getDouble(int)
	 */
	public double getDouble( int index ) throws OdaException
	{
		logger.debug("IN getDouble");
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getDouble(java.lang.String)
	 */
	public double getDouble( String columnName ) throws OdaException
	{
		logger.debug("IN getDouble");
	    return getDouble( findColumn( columnName ) );
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getBigDecimal(int)
	 */
	public BigDecimal getBigDecimal( int index ) throws OdaException
	{
		logger.debug("IN getBigDecimal");
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getBigDecimal(java.lang.String)
	 */
	public BigDecimal getBigDecimal( String columnName ) throws OdaException
	{
		logger.debug("IN getBigDecimal");
	    return getBigDecimal( findColumn( columnName ) );
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getDate(int)
	 */
	public Date getDate( int index ) throws OdaException
	{
		logger.debug("IN getDate");
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getDate(java.lang.String)
	 */
	public Date getDate( String columnName ) throws OdaException
	{
		logger.debug("IN getDate");
	    return getDate( findColumn( columnName ) );
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getTime(int)
	 */
	public Time getTime( int index ) throws OdaException
	{
		logger.debug("IN getTime");
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getTime(java.lang.String)
	 */
	public Time getTime( String columnName ) throws OdaException
	{
		logger.debug("IN getTime");
	    return getTime( findColumn( columnName ) );
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getTimestamp(int)
	 */
	public Timestamp getTimestamp( int index ) throws OdaException
	{
		logger.debug("IN getTimestamp");
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getTimestamp(java.lang.String)
	 */
	public Timestamp getTimestamp( String columnName ) throws OdaException
	{
		logger.debug("IN getTimestamp");
	    return getTimestamp( findColumn( columnName ) );
	}

    /* 
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getBlob(int)
     */
    public IBlob getBlob( int index ) throws OdaException
    {
    	logger.debug("IN getBlob");
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    /* 
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getBlob(java.lang.String)
     */
    public IBlob getBlob( String columnName ) throws OdaException
    {
    	logger.debug("IN getBlob");
        return getBlob( findColumn( columnName ) );
    }

    /* 
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getClob(int)
     */
    public IClob getClob( int index ) throws OdaException
    {
    	logger.debug("IN getClob");
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    /* 
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getClob(java.lang.String)
     */
    public IClob getClob( String columnName ) throws OdaException
    {
    	logger.debug("IN getClob");
        return getClob( findColumn( columnName ) );
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getBoolean(int)
     */
    public boolean getBoolean( int index ) throws OdaException
    {
    	logger.debug("IN getBoolean");
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getBoolean(java.lang.String)
     */
    public boolean getBoolean( String columnName ) throws OdaException
    {
    	logger.debug("IN getBoolean");
        return getBoolean( findColumn( columnName ) );
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getObject(int)
     */
    public Object getObject( int index ) throws OdaException
    {
    	logger.debug("IN getObject");
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getObject(java.lang.String)
     */
    public Object getObject( String columnName ) throws OdaException
    {
    	logger.debug("IN getObject");
        return getObject( findColumn( columnName ) );
    }

    /*
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#wasNull()
     */
    public boolean wasNull() throws OdaException
    {
    	logger.debug("IN wasNull");
        // TODO Auto-generated method stub
        
        // hard-coded for demo purpose
        return false;
    }

    /*
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#findColumn(java.lang.String)
     */
    public int findColumn( String columnName ) throws OdaException
    {
    	logger.debug("IN findColumn");
        // TODO replace with data source specific implementation
        
        // hard-coded for demo purpose
        int columnId = 1;   // dummy column id
        if( columnName == null || columnName.length() == 0 )
            return columnId;
        String lastChar = columnName.substring( columnName.length()-1, 1 );
        try
        {
            columnId = Integer.parseInt( lastChar );
        }
        catch( NumberFormatException e )
        {
            // ignore, use dummy column id
        }
        return columnId;
    }
    
}
