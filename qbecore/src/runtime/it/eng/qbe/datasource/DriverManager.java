/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.datasource;

import it.eng.qbe.datasource.configuration.IDataSourceConfiguration;
import it.eng.qbe.datasource.dataset.DataSetDriver;
import it.eng.qbe.datasource.hibernate.HibernateDriver;
import it.eng.qbe.datasource.hibernate.HibernateDriverWithClassLoader;
import it.eng.qbe.datasource.jpa.JPADriver;
import it.eng.qbe.datasource.jpa.JPADriverWithClassLoader;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class DriverManager {
	private static Map<String, IDriver> drivers;
	
	static {
		drivers = new HashMap<String, IDriver>();
		drivers.put(JPADriver.DRIVER_ID, new JPADriver());
		drivers.put(HibernateDriver.DRIVER_ID, new HibernateDriver());
		drivers.put(JPADriverWithClassLoader.DRIVER_ID, new JPADriverWithClassLoader());
		drivers.put(HibernateDriverWithClassLoader.DRIVER_ID, new HibernateDriverWithClassLoader());
		drivers.put(DataSetDriver.DRIVER_ID, new DataSetDriver());
	}

	/**
	 * Get the datasource for the passed driver and with the passed configuration..
	 * If the datasource live in the cache it will not be rebuild
	 * @param driverName name of the driver jpa/hibernate
	 * @param configuration configuration of the datasource
	 * @return
	 */
	public static IDataSource getDataSource(String driverName, IDataSourceConfiguration configuration) {
		return getDataSource(driverName, configuration, true);
	}
	
	/**
	 * Get the datasource for the passed driver and with the passed configuration..
	 * @param driverName name of the driver jpa/hibernate
	 * @param configuration configuration of the datasource
	 * @param cache if true the datasources cache will be enabled
	 * @return
	 */
	public static IDataSource getDataSource(String driverName, IDataSourceConfiguration configuration, boolean cache) {
		
		IDataSource dataSource;
		IDriver driver;
		
		driver = drivers.get(driverName);
		if(driver == null) {
			throw new SpagoBIRuntimeException("No suitable driver for id [" + driverName + "]");
		}
		
		driver.setDataSourceCacheEnabled(cache);
		
		dataSource = driver.getDataSource(configuration);
	
		return dataSource;
	}
}
