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
package it.eng.spagobi.sdk.datasources.impl;

import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.sdk.AbstractSDKService;
import it.eng.spagobi.sdk.datasources.bo.SDKDataSource;
import it.eng.spagobi.sdk.exceptions.NotAllowedOperationException;
import it.eng.spagobi.sdk.utilities.SDKObjectsConverter;
import it.eng.spagobi.tools.datasource.bo.IDataSource;

import java.util.List;

import org.apache.log4j.Logger;

public class DataSourcesSDKServiceImpl extends AbstractSDKService implements it.eng.spagobi.sdk.datasources.DataSourcesSDKService {

	static private Logger logger = Logger.getLogger(DataSourcesSDKServiceImpl.class);

	@Override
	public SDKDataSource getDataSource(Integer dataSourceId) throws NotAllowedOperationException {
		SDKDataSource toReturn = null;
		logger.debug("IN: dataSourceId in input = " + dataSourceId);

		this.setTenant();

		try {
			super.checkUserPermissionForFunctionality(SpagoBIConstants.DATASOURCE_READ, "User cannot see datasource congifuration.");
			if (dataSourceId == null) {
				logger.warn("DataSource identifier in input is null!");
				return null;
			}
			IDataSource dataSource = DAOFactory.getDataSourceDAO().loadDataSourceByID(dataSourceId);
			if (dataSource == null) {
				logger.warn("DataSource with identifier [" + dataSourceId + "] not existing.");
				return null;
			}
			toReturn = new SDKObjectsConverter().fromSpagoBiDataSourceToSDKDataSource(dataSource.toSpagoBiDataSource());
		} catch (NotAllowedOperationException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Error while retrieving SDKDataSources list", e);
			logger.debug("Returning null");
			return null;
		} finally {
			this.unsetTenant();
			logger.debug("OUT");
		}
		return toReturn;
	}

	@Override
	public SDKDataSource[] getDataSources() throws NotAllowedOperationException {
		SDKDataSource[] toReturn = null;
		logger.debug("IN");

		this.setTenant();

		try {
			super.checkUserPermissionForFunctionality(SpagoBIConstants.DATASOURCE_READ, "User cannot see datasources congifuration.");
			List dataSourceList = DAOFactory.getDataSourceDAO().loadAllDataSources();
			toReturn = new SDKDataSource[dataSourceList.size()];
			for (int i = 0; i < dataSourceList.size(); i++) {
				IDataSource dataSource = (IDataSource) dataSourceList.get(i);
				SDKDataSource sdkDataSource = new SDKObjectsConverter().fromSpagoBiDataSourceToSDKDataSource(dataSource.toSpagoBiDataSource());
				toReturn[i] = sdkDataSource;
			}
		} catch (NotAllowedOperationException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Error while retrieving SDKSource list", e);
			logger.debug("Returning null");
			return null;
		} finally {
			this.unsetTenant();
			logger.debug("OUT");
		}
		return toReturn;
	}

}
