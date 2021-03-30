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
package it.eng.knowage.engines.svgviewer.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import it.eng.knowage.engines.svgviewer.SvgViewerEngineConstants;
import it.eng.knowage.engines.svgviewer.SvgViewerEngineException;
import it.eng.knowage.engines.svgviewer.datamart.provider.IDataMartProvider;
import it.eng.knowage.engines.svgviewer.dataset.HierarchyMember;
import it.eng.knowage.engines.svgviewer.map.provider.IMapProvider;
import it.eng.knowage.engines.svgviewer.map.renderer.IMapRenderer;
import it.eng.spago.base.SourceBean;

/**
 * The Class GeoEngineComponentFactory.
 *
 */
public class SvgViewerEngineComponentFactory {

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(SvgViewerEngineComponentFactory.class);

	/**
	 * Builds the.
	 *
	 * @param geoEngineComponentClassName the geo engine component class name
	 * @param conf                        the conf
	 * @param env                         the env
	 *
	 * @return the i geo engine component
	 *
	 * @throws GeoEngineException the geo engine exception
	 */
	public static ISvgViewerEngineComponent build(String geoEngineComponentClassName, Object conf, Map env) throws SvgViewerEngineException {

		ISvgViewerEngineComponent geoEngineComponent = null;

		logger.debug("IN");

		try {
			geoEngineComponent = (ISvgViewerEngineComponent) Class.forName(geoEngineComponentClassName).newInstance();
		} catch (InstantiationException e) {
			logger.error("Impossible to instatiate component of type: " + geoEngineComponentClassName);
			throw new SvgViewerEngineException("Impossible to instatiate component of type: " + geoEngineComponentClassName, e);
		} catch (IllegalAccessException e) {
			logger.error("Impossible to instatiate component of type: " + geoEngineComponentClassName);
			throw new SvgViewerEngineException("Impossible to instatiate component of type: " + geoEngineComponentClassName, e);
		} catch (ClassNotFoundException e) {
			SvgViewerEngineException geoException;
			logger.error("Impossible to instatiate component of type: " + geoEngineComponentClassName);
			String description = "Impossible to instatiate component of type: " + geoEngineComponentClassName;
			List hints = new ArrayList();
			hints.add("Check if the class name is wrong or mispelled");
			hints.add("Check if the class is on the class path");
			geoException = new SvgViewerEngineException(description, e);
			// geoException.setHints(hints);
			throw geoException;
		}

		logger.debug("Component " + geoEngineComponentClassName + " created succesfully");
		Monitor setEnvMonitor = MonitorFactory.start("GeoEngine.SvgViewerEngineComponentFactory.setEnv");
		geoEngineComponent.setEnv(env);
		setEnvMonitor.stop();
		Monitor initMonitor = MonitorFactory.start("GeoEngine.SvgViewerEngineComponentFactory.init." + geoEngineComponentClassName);
		geoEngineComponent.init(conf);
		initMonitor.stop();
		logger.debug("Component " + geoEngineComponentClassName + " configurated succesfully");

		logger.debug("OUT");

		return geoEngineComponent;
	}

	/**
	 * Builds the map provider.
	 *
	 * @param template the template
	 * @param env      the env
	 *
	 * @return the i map provider
	 *
	 * @throws GeoEngineException the geo engine exception
	 */
	public static IMapProvider buildMapProvider(HierarchyMember member, Map env) throws SvgViewerEngineException {
		IMapProvider mapProvider = null;
		SourceBean confSB = null;
		String className = null;

		logger.debug("IN");

		className = SvgViewerEngineConstants.DEFAULT_MAP_PROVIDER;
		logger.debug("Map provider class: " + className);
		logger.debug("Map provider configuration: " + confSB);

		// mapProvider = (IMapProvider) build(className, confSB, env);
		mapProvider = (IMapProvider) build(className, member, env);
		logger.debug("IN");

		return mapProvider;
	}

	/**
	 * Builds the map renderer.
	 *
	 * @param template the template
	 * @param env      the env
	 *
	 * @return the i map renderer
	 *
	 * @throws GeoEngineException the geo engine exception
	 */
	public static IMapRenderer buildMapRenderer(SourceBean template, Map env) throws SvgViewerEngineException {
		IMapRenderer mapRenderer = null;

		String className = SvgViewerEngineConstants.DEFAULT_MAP_RENDERER;

		logger.warn("The default MapRenderer implementation will be used: [" + className + "]");
		logger.debug("IN");
		mapRenderer = (IMapRenderer) build(className, null, env);
		logger.debug("OUT");

		return mapRenderer;
	}

	/**
	 * Builds the dataset provider.
	 *
	 * @param template the template
	 * @param env      the env
	 *
	 * @return the i dataset provider
	 *
	 * @throws GeoEngineException the geo engine exception
	 */
	public static IDataMartProvider buildDataMartProvider(SourceBean template, Map env) throws SvgViewerEngineException {
		IDataMartProvider dataMartProvider = null;
		SourceBean confSB = null;
		String className = null;

		logger.debug("IN");
		confSB = (SourceBean) template.getAttribute(SvgViewerEngineConstants.DATAMART_PROVIDER_TAG);
		if (confSB == null) {
			logger.warn("Cannot find DatasetProvider configuration settings: tag name " + SvgViewerEngineConstants.DATAMART_PROVIDER_TAG);
			logger.info("DatasetProvider configuration settings must be injected at execution time");
			return null;
		}
		className = (String) confSB.getAttribute(SvgViewerEngineConstants.CLASS_NAME_ATTRIBUTE);
		if (className == null) {
			className = SvgViewerEngineConstants.DEFAULT_DATAMART_PROVIDER;
			logger.warn("Cannot find DatasetProvider class attribute: " + SvgViewerEngineConstants.CLASS_NAME_ATTRIBUTE);
			logger.warn("The default DataMartProvider implementation will be used: [" + className + "]");
		}

		logger.debug("Dataset provider class: " + className);
		logger.debug("Dataset provider configuration: " + confSB);

		dataMartProvider = (IDataMartProvider) build(className, confSB, env);

		logger.debug("OUT");

		return dataMartProvider;
	}
}
