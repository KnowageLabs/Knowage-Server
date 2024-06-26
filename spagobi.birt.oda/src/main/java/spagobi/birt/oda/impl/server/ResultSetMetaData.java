/*
 *************************************************************************
 * Copyright (c) 2008 <<Your Company Name here>>
 *  
 *************************************************************************
 */

package spagobi.birt.oda.impl.server;

import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;

import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spagobi.birt.oda.impl.Driver;

/**
 * Implementation class of IResultSetMetaData for an ODA runtime driver.
 * <br>
 * For demo purpose, the auto-generated method stubs have
 * hard-coded implementation that returns a pre-defined set
 * of meta-data and query results.
 * A custom ODA driver is expected to implement own data source specific
 * behavior in its place. 
 */
public class ResultSetMetaData implements IResultSetMetaData
{
	IMetaData dataStoreMeta;
	
	private static Logger logger = LoggerFactory.getLogger(ResultSetMetaData.class);
	
	public ResultSetMetaData(IMetaData dataStoreMeta) {

		this.dataStoreMeta = dataStoreMeta;
	}
	/*
	 * @see org.eclipse.datatools.connectivity.oda.IResultSetMetaData#getColumnCount()
	 */
	public int getColumnCount() throws OdaException
	{
		logger.debug("IN getColumnCount");
        int columnCount = 0;
		
        try {
        	if(dataStoreMeta!=null){
        		columnCount = dataStoreMeta.getFieldCount();
        		
        	}
		} catch(Throwable t) {
			throw (OdaException) new OdaException("Impossible to extract column count from data store meta").initCause(t);
		}
		logger.debug("OUT getColumnCount Numero colonne: "+columnCount);
		return columnCount;
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IResultSetMetaData#getColumnName(int)
	 */
	public String getColumnName( int index ) throws OdaException
	{
		logger.debug("IN getColumnName");
		String name = "undefined";
		
		 try {
			 name = dataStoreMeta.getFieldName(index-1);
		} catch(Throwable t) {
			throw (OdaException) new OdaException("Impossible to extract column-" + index + "'s name from data store meta").initCause(t);
		}
		logger.debug("OUT getColumnName "+name);
        return name;
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IResultSetMetaData#getColumnLabel(int)
	 */
	public String getColumnLabel( int index ) throws OdaException
	{
		logger.debug("IN getColumnLabel");
		String label = getColumnName( index );	
		logger.debug("OUT getColumnLabel "+ label);
		return label;		// default
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IResultSetMetaData#getColumnType(int)
	 */
	public int getColumnType( int index ) throws OdaException
	{
		logger.debug("IN getColumnType");
		String className =  dataStoreMeta.getFieldType(index-1).getName();
		
        if( className.endsWith("Integer") ) {
            return java.sql.Types.INTEGER;   
        }
        logger.debug("OUT getColumnType "+className);
        return java.sql.Types.CHAR;          
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IResultSetMetaData#getColumnTypeName(int)
	 */
	public String getColumnTypeName( int index ) throws OdaException
	{
		logger.debug("IN getColumnTypeName");
        int nativeTypeCode = getColumnType( index );
        String toReturn =  Driver.getNativeDataTypeName( nativeTypeCode );
        logger.debug("OUT getColumnTypeName "+toReturn);
        return toReturn;
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IResultSetMetaData#getColumnDisplayLength(int)
	 */
	public int getColumnDisplayLength( int index ) throws OdaException
	{
		logger.debug("IN getColumnDisplayLength");
        // hard-coded for demo purpose
		return 8;
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IResultSetMetaData#getPrecision(int)
	 */
	public int getPrecision( int index ) throws OdaException
	{
		logger.debug("IN getPrecision");
        // TODO Auto-generated method stub
		return -1;
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IResultSetMetaData#getScale(int)
	 */
	public int getScale( int index ) throws OdaException
	{
		logger.debug("IN getScale");
        // TODO Auto-generated method stub
		return -1;
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IResultSetMetaData#isNullable(int)
	 */
	public int isNullable( int index ) throws OdaException
	{
		logger.debug("IN isNullable");
        // TODO Auto-generated method stub
		return IResultSetMetaData.columnNullableUnknown;
	}
    
}
