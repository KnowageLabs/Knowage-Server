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
package it.eng.knowage.engines.svgviewer.datamart.provider.configurator;

import org.apache.log4j.Logger;

import it.eng.knowage.engines.svgviewer.SvgViewerEngineConstants;
import it.eng.knowage.engines.svgviewer.SvgViewerEngineException;
import it.eng.knowage.engines.svgviewer.datamart.provider.DataMartProvider;
import it.eng.knowage.engines.svgviewer.dataset.HierarchyMember;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.datasource.bo.DataSourceFactory;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.EngineConstants;

/**
 * The Class SQLDatasetProviderConfigurator.
 *
 */
public class DataMartProviderConfigurator {

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(DataMartProviderConfigurator.class);

	/**
	 * Configure.
	 *
	 * @param datamartProvider
	 *            the sql dataset provider
	 * @param conf
	 *            the conf
	 *
	 * @throws GeoEngineException
	 *             the geo engine exception
	 */
	public static void configure(DataMartProvider datamartProvider, Object conf) throws SvgViewerEngineException {
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
			IDataSet dataSet = null;

			// get dataset of active member if present...
			HierarchyMember hierMember = datamartProvider.getHierarchyMember(datamartProvider.getSelectedMemberName());
			Assert.assertNotNull(hierMember, "Hierarchy Member [" + datamartProvider.getSelectedMemberName() + "] cannot be null");
			String labelDsData = hierMember.getDsMeasure();
			try {
				dataSet = DAOFactory.getDataSetDAO().loadDataSetByLabel(labelDsData);
				if (dataSet != null) {
					datamartProvider.setDs(dataSet);
					datamartProvider.getEnv().put(EngineConstants.ENV_DATASET, dataSet);
					return;
				}
			} catch (Exception e) {
				logger.error("Impossible to load dataset with data", e);
				throw new SvgViewerEngineException("Impossible to load dataset with data", e);
			}
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
