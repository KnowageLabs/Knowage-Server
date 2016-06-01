package it.eng.knowage.engines.svgviewer.component;

import it.eng.knowage.engines.svgviewer.SvgViewerEngineConstants;
import it.eng.knowage.engines.svgviewer.SvgViewerEngineException;
import it.eng.knowage.engines.svgviewer.datamart.provider.IDataMartProvider;
import it.eng.knowage.engines.svgviewer.map.provider.IMapProvider;
import it.eng.knowage.engines.svgviewer.map.renderer.IMapRenderer;
import it.eng.spago.base.SourceBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

// TODO: Auto-generated Javadoc
/**
 * The Class GeoEngineComponentFactory.
 *
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class SvgViewerEngineComponentFactory {

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(SvgViewerEngineComponentFactory.class);

	/**
	 * Builds the.
	 *
	 * @param geoEngineComponentClassName
	 *            the geo engine component class name
	 * @param conf
	 *            the conf
	 * @param env
	 *            the env
	 *
	 * @return the i geo engine component
	 *
	 * @throws GeoEngineException
	 *             the geo engine exception
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
			geoException = new SvgViewerEngineException("Impossible to instatiate component", e);
			geoException.setDescription(description);
			// geoException.setHints(hints);
			throw geoException;
		}

		logger.debug("Component " + geoEngineComponentClassName + " created succesfully");
		geoEngineComponent.setEnv(env);
		geoEngineComponent.init(conf);
		logger.debug("Component " + geoEngineComponentClassName + " configurated succesfully");

		logger.debug("OUT");

		return geoEngineComponent;
	}

	/**
	 * Builds the map provider.
	 *
	 * @param template
	 *            the template
	 * @param env
	 *            the env
	 *
	 * @return the i map provider
	 *
	 * @throws GeoEngineException
	 *             the geo engine exception
	 */
	public static IMapProvider buildMapProvider(SourceBean template, Map env) throws SvgViewerEngineException {
		IMapProvider mapProvider = null;
		SourceBean confSB = null;
		String className = null;

		logger.debug("IN");
		confSB = (SourceBean) template.getAttribute(SvgViewerEngineConstants.MAP_PROVIDER_TAG);
		if (confSB == null) {
			logger.warn("Cannot find MapProvider configuration settings: tag name " + SvgViewerEngineConstants.MAP_PROVIDER_TAG);
			logger.info("MapProvider configuration settings must be injected at execution time");
			return null;
		}
		className = (String) confSB.getAttribute(SvgViewerEngineConstants.CLASS_NAME_ATTRIBUTE);
		if (className == null) {
			className = SvgViewerEngineConstants.DEFAULT_MAP_PROVIDER;
			logger.warn("Cannot find MapProvider class attribute: " + SvgViewerEngineConstants.CLASS_NAME_ATTRIBUTE);
			logger.warn("The default MapProvider implementation will be used: [" + className + "]");
		}
		logger.debug("Map provider class: " + className);
		logger.debug("Map provider configuration: " + confSB);

		mapProvider = (IMapProvider) build(className, confSB, env);
		logger.debug("IN");

		return mapProvider;
	}

	/**
	 * Builds the map renderer.
	 *
	 * @param template
	 *            the template
	 * @param env
	 *            the env
	 *
	 * @return the i map renderer
	 *
	 * @throws GeoEngineException
	 *             the geo engine exception
	 */
	public static IMapRenderer buildMapRenderer(SourceBean template, Map env) throws SvgViewerEngineException {
		IMapRenderer mapRenderer = null;
		SourceBean confSB = null;
		String className = null;

		logger.debug("IN");
		confSB = (SourceBean) template.getAttribute(SvgViewerEngineConstants.MAP_RENDERER_TAG);
		if (confSB == null) {
			logger.warn("Cannot find MapRenderer configuration settings: tag name " + SvgViewerEngineConstants.MAP_RENDERER_TAG);
			logger.info("MapRenderer configuration settings must be injected at execution time");
			return null;
		}
		className = (String) confSB.getAttribute(SvgViewerEngineConstants.CLASS_NAME_ATTRIBUTE);
		if (className == null) {
			className = SvgViewerEngineConstants.DEFAULT_MAP_RENDERER;
			logger.warn("Cannot find MapRenderer class attribute: " + SvgViewerEngineConstants.CLASS_NAME_ATTRIBUTE);
			logger.warn("The default MapRenderer implementation will be used: [" + className + "]");
		}
		logger.debug("Map renderer class: " + className);
		logger.debug("Map renderer configuration: " + confSB);

		mapRenderer = (IMapRenderer) build(className, confSB, env);
		logger.debug("OUT");

		return mapRenderer;
	}

	/**
	 * Builds the dataset provider.
	 *
	 * @param template
	 *            the template
	 * @param env
	 *            the env
	 *
	 * @return the i dataset provider
	 *
	 * @throws GeoEngineException
	 *             the geo engine exception
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
		;
		logger.debug("OUT");

		return dataMartProvider;
	}
}
