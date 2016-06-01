package it.eng.knowage.engines.svgviewer.map.provider.configurator;

import it.eng.knowage.engines.svgviewer.SvgViewerEngineConstants;
import it.eng.knowage.engines.svgviewer.SvgViewerEngineException;
import it.eng.knowage.engines.svgviewer.map.provider.SOMapProvider;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spagobi.tools.datasource.bo.DataSource;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.callbacks.mapcatalogue.MapCatalogueAccessUtils;

import org.apache.log4j.Logger;

// TODO: Auto-generated Javadoc
/**
 * The Class SOMapProviderConfigurator.
 *
 * @author Andrea Gioia (andrea.gioia@eng.it)
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
		SourceBean confSB = null;

		if (conf instanceof String) {
			try {
				confSB = SourceBean.fromXMLString((String) conf);
			} catch (SourceBeanException e) {
				logger.error("Impossible to parse configuration block for DataSetProvider", e);
				throw new SvgViewerEngineException("Impossible to parse configuration block for DataSetProvider", e);
			}
		} else {
			confSB = (SourceBean) conf;
		}

		if (confSB != null) {
			MapCatalogueAccessUtils mapCatalogueServiceProxy = (MapCatalogueAccessUtils) soMapProvider.getEnv().get(
					SvgViewerEngineConstants.ENV_MAPCATALOGUE_SERVICE_PROXY);
			soMapProvider.setMapCatalogueServiceProxy(mapCatalogueServiceProxy);
		}
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

		dataSource = new DataSource();

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
