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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.eclipse.datatools.connectivity.oda.IConnection;
import org.eclipse.datatools.connectivity.oda.IDataSetMetaData;
import org.eclipse.datatools.connectivity.oda.IQuery;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.icu.util.ULocale;

import it.eng.spagobi.services.proxy.DataSetServiceProxy;
import it.eng.spagobi.services.proxy.MetamodelServiceProxy;
import it.eng.spagobi.utilities.engines.EngineConstants;

/**
 * Implementation class of IConnection for an ODA runtime driver.
 */
public class Connection implements IConnection {

	boolean isOpen;
	DataSetServiceProxy dataSetServiceProxy = null;
	Map pars;
	Map userProfAttrs;
	String resourcePath;
	String groovyFileName;
	String jsFileName;

	public static final String CONN_PROP_SERVER_URL = "ServerUrl";
	public static final String CONN_PROP_USER = "Username";
	public static final String CONN_PROP_PASSWORD = "Password";

	public static String SBI_BIRT_RUNTIME_IS_RUNTIME = "SBI_BIRT_RUNTIME_IS_RUNTIME";
	public static String SBI_BIRT_RUNTIME_USER_ID = "SBI_BIRT_RUNTIME_USER_ID";
	public static String SBI_BIRT_RUNTIME_SECURE_ATTRS = "SBI_BIRT_RUNTIME_SECURE_ATTRS";
	public static String SBI_BIRT_RUNTIME_SERVICE_URL = "SBI_BIRT_RUNTIME_SERVICE_URL";
	public static String SBI_BIRT_RUNTIME_SERVER_URL = "SBI_BIRT_RUNTIME_SERVER_URL";
	public static String SBI_BIRT_RUNTIME_TOKEN = "SBI_BIRT_RUNTIME_TOKEN";
	public static String SBI_BIRT_RUNTIME_PARS_MAP = "SBI_BIRT_RUNTIME_PARS_MAP";
	public static String SBI_BIRT_RUNTIME_PROFILE_USER_ATTRS = "SBI_BIRT_RUNTIME_PROFILE_USER_ATTRS";

	public static String SBI_BIRT_RUNTIME_GROOVY_SCRIPT_FILE_NAME = "SBI_BIRT_RUNTIME_GROOVY_SCRIPT_FILE_NAME";
	public static String SBI_BIRT_RUNTIME_JS_SCRIPT_FILE_NAME = "SBI_BIRT_RUNTIME_JS_SCRIPT_FILE_NAME";

	public static String RESOURCE_PATH_JNDI_NAME = "RESOURCE_PATH_JNDI_NAME";
	public static String SESSION = "SESSION";

	private Object context = null;

	private static Logger logger = LoggerFactory.getLogger(Connection.class);

	public Connection() {
		isOpen = false;
		dataSetServiceProxy = null;
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IConnection#open(java.util.Properties)
	 */
	@Override
	public void open(Properties connProperties) throws OdaException {
		logger.trace("IN open");
		try {
			logger.debug("Trying to get the DataSetServiceProxy ...");
			dataSetServiceProxy = getDataSetProxy();
			logger.debug("DataSetServiceProxy obtained correctly");
		} catch (RuntimeException e) {
			e.printStackTrace();
			throw (OdaException) new OdaException("Impossible to open connection").initCause(e);
		}
		logger.debug("Data source initialized");
		logger.debug("Connection succesfully opened");
		isOpen = true;
		logger.trace("OUT open");
	}

	private DataSetServiceProxy getDataSetProxy() {
		logger.trace("IN getDataSetProxy");
		DataSetServiceProxy proxy = null;
		if (!isBirtRuntimeContext()) {
			throw new RuntimeException("This method must be invoked in Birt runtime context!!!");
		}
		try {
			HashMap map = (HashMap) context;
			String userId = getUserId();
			String secureAttributes = getSecureAttrs();
			String serviceUrlStr = getServiceUrl();
			String metamodelServiceUrlStr = getMetamodelServiceUrl();
			String spagoBiServerURL = getSpagoBIServerUrl();
			String token = getToken();
			resourcePath = getResPath();
			pars = getParsMap();
			userProfAttrs = getUserProfileMap();
			groovyFileName = getGroovyFileName();
			jsFileName = getJsFileName();
			HttpSession session = getSession();

			MetamodelServiceProxy metamodelproxy = new MetamodelServiceProxy(userId, secureAttributes, metamodelServiceUrlStr, spagoBiServerURL, token);
			proxy = new DataSetServiceProxy(userId, secureAttributes, serviceUrlStr, spagoBiServerURL, token, metamodelproxy, session);

		} catch (Exception e) {
			throw new RuntimeException("Error while getting DataSetServiceProxy from Birt runtime context", e);
		}
		logger.trace("OUT getDataSetProxy");
		return proxy;
	}

	private Map getParsMap() {
		try {
			HashMap map = (HashMap) context;
			Map pars = (Map) map.get(SBI_BIRT_RUNTIME_PARS_MAP);
			return pars;
		} catch (Exception e) {
			throw new RuntimeException("Error while getting user id from Birt runtime context", e);
		}
	}

	private Map getUserProfileMap() {
		try {
			HashMap map = (HashMap) context;
			Map userProfAttrs = (Map) map.get(SBI_BIRT_RUNTIME_PROFILE_USER_ATTRS);
			return userProfAttrs;
		} catch (Exception e) {
			throw new RuntimeException("Error while getting user id from Birt runtime context", e);
		}
	}

	private String getGroovyFileName() {
		try {
			HashMap map = (HashMap) context;
			String groovyFileName = (String) map.get(SBI_BIRT_RUNTIME_GROOVY_SCRIPT_FILE_NAME);
			return groovyFileName;
		} catch (Exception e) {
			throw new RuntimeException("Error while getting user id from Birt runtime context", e);
		}
	}

	private String getJsFileName() {
		try {
			HashMap map = (HashMap) context;
			String jsFileName = (String) map.get(SBI_BIRT_RUNTIME_JS_SCRIPT_FILE_NAME);
			return jsFileName;
		} catch (Exception e) {
			throw new RuntimeException("Error while getting user id from Birt runtime context", e);
		}
	}

	private String getResPath() {
		try {
			HashMap map = (HashMap) context;
			String resPath = (String) map.get(RESOURCE_PATH_JNDI_NAME);
			return resPath;
		} catch (Exception e) {
			throw new RuntimeException("Error while getting user id from Birt runtime context", e);
		}
	}

	private String getUserId() {
		try {
			HashMap map = (HashMap) context;
			String userId = (String) map.get(SBI_BIRT_RUNTIME_USER_ID);
			return userId;
		} catch (Exception e) {
			throw new RuntimeException("Error while getting user id from Birt runtime context", e);
		}
	}

	private String getSecureAttrs() {
		try {
			HashMap map = (HashMap) context;
			String secureAttributes = (String) map.get(SBI_BIRT_RUNTIME_SECURE_ATTRS);
			return secureAttributes;
		} catch (Exception e) {
			throw new RuntimeException("Error while getting user id from Birt runtime context", e);
		}
	}

	private String getServiceUrl() {
		try {
			HashMap map = (HashMap) context;
			String serviceUrlStr = (String) map.get(SBI_BIRT_RUNTIME_SERVICE_URL);
			return serviceUrlStr;
		} catch (Exception e) {
			throw new RuntimeException("Error while getting user id from Birt runtime context", e);
		}
	}

	private String getMetamodelServiceUrl() {
		try {
			HashMap map = (HashMap) context;
			String serviceUrlStr = "/services/MetamodelService";
			return serviceUrlStr;
		} catch (Exception e) {
			throw new RuntimeException("Error while getting user id from Birt runtime context", e);
		}
	}

	private String getSpagoBIServerUrl() {
		try {
			HashMap map = (HashMap) context;
			String spagoBiServerURL = (String) map.get(SBI_BIRT_RUNTIME_SERVER_URL);
			return spagoBiServerURL;
		} catch (Exception e) {
			throw new RuntimeException("Error while getting user id from Birt runtime context", e);
		}
	}

	private String getToken() {
		try {
			HashMap map = (HashMap) context;
			String token = (String) map.get(SBI_BIRT_RUNTIME_TOKEN);
			return token;
		} catch (Exception e) {
			throw new RuntimeException("Error while getting user id from Birt runtime context", e);
		}
	}

	private HttpSession getSession() {
		try {
			HashMap map = (HashMap) context;
			HttpSession session = (HttpSession) map.get(SESSION);
			return session;
		} catch (Exception e) {
			throw new RuntimeException("Error while getting session from Birt runtime context", e);
		}
	}

	private boolean isBirtRuntimeContext() {
		logger.trace("IN open");
		logger.debug("Entering isBirtRuntimeContext method");
		if (context != null && context instanceof HashMap) {
			HashMap map = (HashMap) context;
			String isRuntime = (String) map.get(SBI_BIRT_RUNTIME_IS_RUNTIME);
			if (isRuntime != null && isRuntime.equals("true")) {
				logger.debug("Ok runtime");
				return true;
			} else {
				logger.debug("NOT runtime");
				return false;
			}
		}
		logger.trace("OUT open");
		return false;
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IConnection#setAppContext(java.lang.Object)
	 */
	@Override
	public void setAppContext(Object context) throws OdaException {
		logger.trace("IN setAppContext");
		this.context = context;
		logger.debug("Driver: start setAppContext");
		if (context != null && context instanceof HashMap) {
			HashMap map = (HashMap) context;
			EngineConstants d = null;
			Set<Map.Entry> entries = map.entrySet();
			Iterator<Map.Entry> it = entries.iterator();
			while (it.hasNext()) {
				Map.Entry entry = it.next();
				Object key = entry.getKey();
				Object value = entry.getValue();
				logger.debug("Entry key [" + key + "], value [" + value + "]");
			}
		}
		logger.debug("Driver: end setAppContext");
		logger.trace("OUT setAppContext");
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IConnection#close()
	 */
	@Override
	public void close() throws OdaException {
		logger.debug("IN close");
		isOpen = false;
		dataSetServiceProxy = null;
		logger.debug("OUT close");
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IConnection#isOpen()
	 */
	@Override
	public boolean isOpen() throws OdaException {
		logger.debug("IN-OUT isOpen");
		return isOpen;
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IConnection#getMetaData(java.lang.String)
	 */
	@Override
	public IDataSetMetaData getMetaData(String dataSetType) throws OdaException {
		logger.debug("IN-OUT getMetaData");
		// assumes that this driver supports only one type of data set,
		// ignores the specified dataSetType
		return new DataSetMetaData(this);
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IConnection#newQuery(java.lang.String)
	 */
	@Override
	public IQuery newQuery(String dataSetType) throws OdaException {
		logger.debug("IN-OUT newQuery");
		// assumes that this driver supports only one type of data set,
		// ignores the specified dataSetType
		return new Query(dataSetServiceProxy, pars, resourcePath, userProfAttrs, groovyFileName, jsFileName);
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IConnection#getMaxQueries()
	 */
	@Override
	public int getMaxQueries() throws OdaException {
		logger.debug("IN-OUT getMaxQueries");
		return 0; // no limit
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IConnection#commit()
	 */
	@Override
	public void commit() throws OdaException {
		logger.debug("IN-OUT commit");
		// do nothing; assumes no transaction support needed
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IConnection#rollback()
	 */
	@Override
	public void rollback() throws OdaException {
		logger.debug("IN-OUT rollback");
		// do nothing; assumes no transaction support needed
	}

	@Override
	public void setLocale(ULocale arg0) throws OdaException {
		logger.debug("IN-OUT setLocale");
		// TODO Auto-generated method stub

	}

}
