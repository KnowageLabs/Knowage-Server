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
package it.eng.knowage.engines.svgviewer.map.provider.configurator;

import org.apache.log4j.Logger;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import it.eng.knowage.engines.svgviewer.SvgViewerEngineConstants;
import it.eng.knowage.engines.svgviewer.SvgViewerEngineException;
import it.eng.knowage.engines.svgviewer.dataset.HierarchyMember;
import it.eng.knowage.engines.svgviewer.map.provider.SOMapProvider;
import it.eng.spago.base.SourceBean;
import it.eng.spagobi.tools.datasource.bo.DataSourceFactory;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.callbacks.mapcatalogue.MapCatalogueAccessUtils;

/**
 * The Class SOMapProviderConfigurator.
 *
 */
public class SOMapProviderConfigurator {

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(SOMapProviderConfigurator.class);

	/**
	 * Configure.
	 *
	 * @param soMapProvider
	 *            the so map provider
	 * @param conf
	 *            the conf
	 *
	 * @throws GeoEngineException
	 *             the geo engine exception
	 */
	public static void configure(SOMapProvider soMapProvider, Object conf) throws SvgViewerEngineException {
		Monitor mapCatalogueServiceProxyMonitor = MonitorFactory.start("GeoEngine.SOMapProviderConfigurator.configure.mapCatalogueServiceProxy");

		MapCatalogueAccessUtils mapCatalogueServiceProxy = (MapCatalogueAccessUtils) soMapProvider.getEnv()
				.get(SvgViewerEngineConstants.ENV_MAPCATALOGUE_SERVICE_PROXY);

		soMapProvider.setMapCatalogueServiceProxy(mapCatalogueServiceProxy);

		if (conf instanceof HierarchyMember)
			soMapProvider.setSelectedHierarchyMember((HierarchyMember) conf);

		mapCatalogueServiceProxyMonitor.stop();

	}

	/**
	 * Gets the data source.
	 *
	 * @param confSB
	 *            the conf sb
	 *
	 * @return the data source
	 *
	 * @throws GeoEngineException
	 *             the geo engine exception
	 */
	public static IDataSource getDataSource(SourceBean confSB) throws SvgViewerEngineException {
		IDataSource dataSource = null;

		SourceBean datasourceSB = (SourceBean) confSB.getAttribute(SvgViewerEngineConstants.DATASOURCE_TAG);
		if (datasourceSB == null) {
			logger.warn("Cannot find datasource configuration settings: tag name " + SvgViewerEngineConstants.DATASOURCE_TAG);
			logger.info("Datasource configuration settings must be injected at execution time");
			return null;
		}

		dataSource = DataSourceFactory.getDataSource();

		String type = (String) datasourceSB.getAttribute(SvgViewerEngineConstants.DATASET_TYPE_ATTRIBUTE);
		if ("connection".equalsIgnoreCase(type)) {
			dataSource.setJndi((String) datasourceSB.getAttribute(SvgViewerEngineConstants.DATASET_NAME_ATTRIBUTE));
			dataSource.setDriver((String) datasourceSB.getAttribute(SvgViewerEngineConstants.DATASET_DRIVER_ATTRIBUTER));
			dataSource.setPwd((String) datasourceSB.getAttribute(SvgViewerEngineConstants.DATASET_PWD_ATTRIBUTE));
			dataSource.setUser((String) datasourceSB.getAttribute(SvgViewerEngineConstants.DATASET_USER_ATTRIBUTE));
			dataSource.setUrlConnection((String) datasourceSB.getAttribute(SvgViewerEngineConstants.DATASET_URL_ATTRIBUTE));
		}

		logger.debug("Datasource jndi name: " + dataSource.getJndi());
		logger.debug("Datasource driver: " + dataSource.getDriver());
		logger.debug("Datasource password: " + dataSource.getPwd());
		logger.debug("Datasource user: " + dataSource.getUser());
		logger.debug("Datasource url: " + dataSource.getUrlConnection());

		if (dataSource.getJndi() != null) {
			logger.info("Datasource is of type jndi connection. Referenced jndi resource is " + dataSource.getJndi());
		} else if (dataSource.getDriver() == null || dataSource.getUrlConnection() == null) {
			logger.error("Missing driver name or url in datasource configuration settings");
			throw new SvgViewerEngineException("Missing driver name or url in datasource configuration settings");
		}

		return dataSource;
	}

	/**
	 * Gets the query.
	 *
	 * @param dataSetSB
	 *            the data set sb
	 *
	 * @return the query
	 */
	private static String getQuery(SourceBean dataSetSB) {
		String query = null;

		SourceBean querySB = (SourceBean) dataSetSB.getAttribute(SvgViewerEngineConstants.QUERY_TAG);
		if (querySB == null) {
			logger.warn("Cannot find query configuration settings: tag name " + SvgViewerEngineConstants.QUERY_TAG);
			logger.info("Datasource configuration settings must be injected at execution time");
			return null;
		}

		query = querySB.getCharacters();

		return query;
	}
}
