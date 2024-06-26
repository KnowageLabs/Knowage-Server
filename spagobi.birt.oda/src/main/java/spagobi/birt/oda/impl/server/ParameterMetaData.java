/*
 *************************************************************************
 * Copyright (c) 2008 <<Your Company Name here>>
 *  
 *************************************************************************
 */

package spagobi.birt.oda.impl.server;

import java.util.Map;
import java.util.Set;

import org.eclipse.datatools.connectivity.oda.IParameterMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spagobi.birt.oda.impl.Driver;

/**
 * Implementation class of IParameterMetaData for an ODA runtime driver.
 * <br>
 * For demo purpose, the auto-generated method stubs have
 * hard-coded implementation that returns a pre-defined set
 * of meta-data and query results.
 * A custom ODA driver is expected to implement own data source specific
 * behavior in its place. 
 */
public class ParameterMetaData implements IParameterMetaData 
{
	Map params;
	
	private static Logger logger = LoggerFactory.getLogger(ParameterMetaData.class);
	
	public ParameterMetaData(Map params) {
		this.params = params;
	}
	/* 
	 * @see org.eclipse.datatools.connectivity.oda.IParameterMetaData#getParameterCount()
	 */
	public int getParameterCount() throws OdaException 
	{
		logger.debug("IN");
		System.out.println("getParameterCount");
		Set s =  params.entrySet();
		if(s!=null){
			System.out.println("Number of pars: "+params.entrySet().size());
			return params.entrySet().size();
		}else{
			System.out.println("Number of pars: 0");
			return 0;
		}
        
	}

    /*
	 * @see org.eclipse.datatools.connectivity.oda.IParameterMetaData#getParameterMode(int)
	 */
	public int getParameterMode( int param ) throws OdaException 
	{
		logger.debug("IN");
		return IParameterMetaData.parameterModeIn;
	}

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IParameterMetaData#getParameterName(int)
     */
    public String getParameterName( int param ) throws OdaException
    {
    	logger.debug("IN");
       // return sdkParametersMeta[param-1].getName(); 
    	return "temp";
    }

	/* 
	 * @see org.eclipse.datatools.connectivity.oda.IParameterMetaData#getParameterType(int)
	 */
	public int getParameterType( int param ) throws OdaException 
	{
		logger.debug("IN");
		//String type = sdkParametersMeta[param-1].getType(); 
		//TypeDesc typeDesc = sdkParametersMeta[param-1].getTypeDesc();
		return 0;
       // return java.sql.Types.CHAR;   // as defined in data set extension manifest
	}

	/* 
	 * @see org.eclipse.datatools.connectivity.oda.IParameterMetaData#getParameterTypeName(int)
	 */
	public String getParameterTypeName( int param ) throws OdaException 
	{
		logger.debug("IN");
        int nativeTypeCode = getParameterType( param );
        return Driver.getNativeDataTypeName( nativeTypeCode );
	}

	/* 
	 * @see org.eclipse.datatools.connectivity.oda.IParameterMetaData#getPrecision(int)
	 */
	public int getPrecision( int param ) throws OdaException 
	{
		logger.debug("IN");
		return -1;
	}

	/* 
	 * @see org.eclipse.datatools.connectivity.oda.IParameterMetaData#getScale(int)
	 */
	public int getScale( int param ) throws OdaException 
	{
		logger.debug("IN");
		return -1;
	}

	/* 
	 * @see org.eclipse.datatools.connectivity.oda.IParameterMetaData#isNullable(int)
	 */
	public int isNullable( int param ) throws OdaException 
	{
		logger.debug("IN");
		return IParameterMetaData.parameterNullableUnknown;
	}

}
