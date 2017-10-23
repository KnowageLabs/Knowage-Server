/**

SpagoBI - The Business Intelligence Free Platform

Copyright (C) 2004 - 2011 Engineering Ingegneria Informatica S.p.A.

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

import it.eng.spagobi.services.proxy.DataSetServiceProxy;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Map;

import org.eclipse.datatools.connectivity.oda.IParameterMetaData;
import org.eclipse.datatools.connectivity.oda.IQuery;
import org.eclipse.datatools.connectivity.oda.IResultSet;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.eclipse.datatools.connectivity.oda.SortSpec;
import org.eclipse.datatools.connectivity.oda.spec.QuerySpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spagobi.birt.oda.impl.util.Utilities;

/**
 * Implementation class of IQuery for an ODA runtime driver. <br>
 * For demo purpose, the auto-generated method stubs have hard-coded implementation that returns a pre-defined set of meta-data and query results. A custom ODA
 * driver is expected to implement own data source specific behavior in its place.
 */
public class Query implements IQuery {
	int maxRows;
	String dsLabel;

	DataSetServiceProxy dataSetServiceProxy;
	IDataSet ds;
	IMetaData dataStoreMeta;
	IDataStore dataStore;
	Map params;
	Map userProfAttrs;
	String resourcePath;
	String groovyFileName;
	String jsFileName;

	public static final String QUERY_SCRIPT = "queryScript";
	public static final String FILE_NAME = "fileName";

	private static Logger logger = LoggerFactory.getLogger(Query.class);

	public Query(DataSetServiceProxy dataSetServiceProxy, Map pars, String resourcePath, Map userProfAttrs, String groovyFileName, String jsFileName) {
		this.maxRows = -1;
		this.dsLabel = null;
		this.params = pars;
		this.dataSetServiceProxy = dataSetServiceProxy;
		this.resourcePath = resourcePath;
		this.userProfAttrs = userProfAttrs;
		this.groovyFileName = groovyFileName;
		this.jsFileName = jsFileName;
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#prepare(java.lang.String)
	 */
	@Override
	public void prepare(String queryText) throws OdaException {
		logger.debug("IN prepare");
		this.dsLabel = queryText;
		logger.debug("Preparing DS with label " + queryText);

		if (this.dsLabel != null) {
			try {
				ds = dataSetServiceProxy.getDataSetByLabel(this.dsLabel);
			} catch (Throwable e) {
				logger.error("Eccezione", e);
			}
			logger.debug("Loaded DS by label");

			logger.debug("Executing DS");
			if (params != null && params.entrySet().size() > 0) {
				ds.setParamsMap(params);
				// ds.setParameters(null);
				logger.debug("Has params associated");
			}

			try {
				ds.setUserProfileAttributes(userProfAttrs);
				logger.debug("Setted User Profile Attrs");

				Utilities.addToConfiguration(ds.getConfiguration(), QUERY_SCRIPT, jsFileName);
				logger.debug("Setted Js File Name: " + jsFileName);

				Utilities.addToConfiguration(ds.getConfiguration(), FILE_NAME, groovyFileName);
				logger.debug("Setted Js File Name: " + jsFileName);

				// previous dataset use

				// ds.setResourcePath(resourcePath);
				// logger.debug("Setted Resource Path: "+resourcePath);

				// ds.setQueryScript(jsFileName);
				// logger.debug("Setted Js File Name: "+jsFileName);
				// ds.setGroovyFileName(groovyFileName);
				// logger.debug("Setted Groovy File Name: "+groovyFileName);
				ds.loadData();
			} catch (Throwable e) {
				logger.error("Eccezione", e);
			}

			logger.debug("Loaded Datastore");
			logger.debug("Method prepare");
			IDataStore dataStoreTemp = ds.getDataStore();
			if (dataStoreTemp != null) {
				long numRec = dataStoreTemp.getRecordsCount();
				logger.debug("Number of record retrieved: " + numRec);
				dataStoreMeta = dataStoreTemp.getMetaData();
				if (dataStoreMeta != null) {
					for (int i = 0; i < dataStoreMeta.getFieldCount(); i++) {
						IFieldMetaData fmd = dataStoreMeta.getFieldMeta(i);
						if (fmd.getAlias() != null) {
							fmd.setName(fmd.getAlias());
						}
					}
				}

				logger.debug("Loaded Datastore Metadata");
			}
		}
		logger.debug("OUT prepare");
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setAppContext(java.lang.Object)
	 */
	@Override
	public void setAppContext(Object context) throws OdaException {
		logger.debug("IN setAppContext");
		// do nothing; assumes no support for pass-through context
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#close()
	 */
	@Override
	public void close() throws OdaException {
		logger.debug("IN close");
		dataSetServiceProxy = null;
		dataStoreMeta = null;
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#getMetaData()
	 */
	@Override
	public IResultSetMetaData getMetaData() throws OdaException {
		logger.debug("IN getMetaData");
		return new ResultSetMetaData(dataStoreMeta);
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#executeQuery()
	 */
	@Override
	public IResultSet executeQuery() throws OdaException {
		logger.debug("IN executeQuery");
		dataStore = ds.getDataStore();
		if (dataStore != null) {
			long numRec = dataStore.getRecordsCount();
			logger.debug("Number of record retrieved: " + numRec);
			/*
			 * dataStoreMeta = dataStore.getMetaData(); logger.debug("Loaded Datastore Metadata");
			 */
		}
		logger.debug("OUT executeQuery");
		return new ResultSet(dataStore, dataStoreMeta);
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setProperty(java.lang.String, java.lang.String)
	 */
	@Override
	public void setProperty(String name, String value) throws OdaException {
		logger.debug("IN setProperty");
		// do nothing; assumes no data set query property
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setMaxRows(int)
	 */
	@Override
	public void setMaxRows(int max) throws OdaException {
		logger.debug("IN setMaxRows");
		maxRows = max;
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#getMaxRows()
	 */
	@Override
	public int getMaxRows() throws OdaException {
		logger.debug("IN getMaxRows");
		return maxRows;
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#clearInParameters()
	 */
	@Override
	public void clearInParameters() throws OdaException {
		logger.debug("IN clearInParameters");
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setInt(java.lang.String, int)
	 */
	@Override
	public void setInt(String parameterName, int value) throws OdaException {
		logger.debug("IN setInt");
		logger.debug("setInt() IN: par Name: " + parameterName + " ; value: " + value);
		params.remove(parameterName);
		params.put(parameterName, new String[] { "" + value });
		logger.debug("setInt() OUT");
		// setInt ( findInParameter( parameterName ), value);
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setInt(int, int)
	 */
	@Override
	public void setInt(int parameterId, int value) throws OdaException {
		logger.debug("IN setInt");
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setDouble(java.lang.String, double)
	 */
	@Override
	public void setDouble(String parameterName, double value) throws OdaException {
		logger.debug("IN setDouble");
		logger.debug("setDouble() IN: par Name: " + parameterName + " ; value: " + value);
		params.remove(parameterName);
		params.put(parameterName, new String[] { "" + value });
		logger.debug("setDouble() OUT");
		// setDouble ( findInParameter( parameterName ), value);
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setDouble(int, double)
	 */
	@Override
	public void setDouble(int parameterId, double value) throws OdaException {
		logger.debug("IN setDouble");
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setBigDecimal(java.lang.String, java.math.BigDecimal)
	 */
	@Override
	public void setBigDecimal(String parameterName, BigDecimal value) throws OdaException {
		logger.debug("IN setBigDecimal");
		logger.debug("setBigDecimal() IN: par Name: " + parameterName + " ; value: " + value);
		params.remove(parameterName);
		params.put(parameterName, new String[] { "" + value });
		logger.debug("setBigDecimal() OUT");
		// setBigDecimal ( findInParameter( parameterName ), value);
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setBigDecimal(int, java.math.BigDecimal)
	 */
	@Override
	public void setBigDecimal(int parameterId, BigDecimal value) throws OdaException {
		logger.debug("IN setBigDecimal");
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setString(java.lang.String, java.lang.String)
	 */
	@Override
	public void setString(String parameterName, String value) throws OdaException {
		logger.debug("IN setString");
		logger.debug("setString() IN: par Name: " + parameterName + " ; value: " + value);
		params.remove(parameterName);
		params.put(parameterName, new String[] { "" + value });
		logger.debug("setString() OUT");
		// setString ( findInParameter( parameterName ), value);
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setString(int, java.lang.String)
	 */
	@Override
	public void setString(int parameterId, String value) throws OdaException {
		logger.debug("IN setString");
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setDate(java.lang.String, java.sql.Date)
	 */
	@Override
	public void setDate(String parameterName, Date value) throws OdaException {
		logger.debug("IN setDate");
		logger.debug("setDate() IN: par Name: " + parameterName + " ; value: " + value);
		params.remove(parameterName);
		params.put(parameterName, new String[] { "" + value });
		logger.debug("setDate() OUT");
		// setDate ( findInParameter( parameterName ), value);
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setDate(int, java.sql.Date)
	 */
	@Override
	public void setDate(int parameterId, Date value) throws OdaException {
		logger.debug("IN setDate");
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setTime(java.lang.String, java.sql.Time)
	 */
	@Override
	public void setTime(String parameterName, Time value) throws OdaException {
		logger.debug("IN setTime");
		logger.debug("setTime() IN: par Name: " + parameterName + " ; value: " + value);
		params.remove(parameterName);
		params.put(parameterName, new String[] { "" + value });
		logger.debug("setTime() OUT");
		// setTime ( findInParameter( parameterName ), value);
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setTime(int, java.sql.Time)
	 */
	@Override
	public void setTime(int parameterId, Time value) throws OdaException {
		logger.debug("IN setTime");
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setTimestamp(java.lang.String, java.sql.Timestamp)
	 */
	@Override
	public void setTimestamp(String parameterName, Timestamp value) throws OdaException {
		logger.debug("IN setTimestamp");
		logger.debug("setTimestamp() IN: par Name: " + parameterName + " ; value: " + value);
		params.remove(parameterName);
		params.put(parameterName, new String[] { "" + value });
		logger.debug("setTimestamp() OUT");
		// setTimestamp ( findInParameter( parameterName ), value);
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setTimestamp(int, java.sql.Timestamp)
	 */
	@Override
	public void setTimestamp(int parameterId, Timestamp value) throws OdaException {
		logger.debug("IN setTimestamp");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setBoolean(java.lang.String, boolean)
	 */
	@Override
	public void setBoolean(String parameterName, boolean value) throws OdaException {
		logger.debug("IN setBoolean");
		logger.debug("setBoolean() IN: par Name: " + parameterName + " ; value: " + value);
		params.remove(parameterName);
		params.put(parameterName, new String[] { "" + value });
		logger.debug("setBoolean() OUT");
		// setBoolean ( findInParameter( parameterName ), value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setBoolean(int, boolean)
	 */
	@Override
	public void setBoolean(int parameterId, boolean value) throws OdaException {
		logger.debug("IN setBoolean");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setNull(java.lang.String)
	 */
	@Override
	public void setNull(String parameterName) throws OdaException {
		logger.debug("IN setNull");
		logger.debug("setNull() IN: par Name: " + parameterName);
		// setNull ( findInParameter( parameterName ));
		params.remove(parameterName);
		params.put(parameterName, new String[] { "" });
		logger.debug("setNull() OUT");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setNull(int)
	 */
	@Override
	public void setNull(int parameterId) throws OdaException {
		logger.debug("IN setNull");
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#findInParameter(java.lang.String)
	 */
	@Override
	public int findInParameter(String parameterName) throws OdaException {
		logger.debug("IN findInParameter");
		return 0;
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#getParameterMetaData()
	 */
	@Override
	public IParameterMetaData getParameterMetaData() throws OdaException {
		logger.debug("IN getParameterMetaData");
		return new ParameterMetaData(params);
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setSortSpec(org.eclipse.datatools.connectivity.oda.SortSpec)
	 */
	@Override
	public void setSortSpec(SortSpec sortBy) throws OdaException {
		logger.debug("IN setSortSpec");
		throw new UnsupportedOperationException();
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#getSortSpec()
	 */
	@Override
	public SortSpec getSortSpec() throws OdaException {
		logger.debug("IN getSortSpec");
		return null;
	}

	@Override
	public void cancel() throws OdaException, UnsupportedOperationException {
		// TODO Auto-generated method stub
		logger.debug("IN cancel");
	}

	@Override
	public String getEffectiveQueryText() {
		// TODO Auto-generated method stub
		logger.debug("IN getEffectiveQueryText");
		return null;
	}

	@Override
	public QuerySpecification getSpecification() {
		// TODO Auto-generated method stub
		logger.debug("IN getSpecification");
		return null;
	}

	@Override
	public void setObject(String arg0, Object arg1) throws OdaException {
		// TODO Auto-generated method stub
		logger.debug("IN setObject");

	}

	@Override
	public void setObject(int arg0, Object arg1) throws OdaException {
		// TODO Auto-generated method stub
		logger.debug("IN setObject");
	}

	@Override
	public void setSpecification(QuerySpecification arg0) throws OdaException, UnsupportedOperationException {
		// TODO Auto-generated method stub
		logger.debug("IN setSpecification");
	}

}
