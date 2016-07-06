package it.eng.knowage.engines.svgviewer.map.provider.configurator;

import it.eng.knowage.engines.svgviewer.SvgViewerEngineException;
import it.eng.knowage.engines.svgviewer.map.provider.AbstractMapProvider;

import org.apache.log4j.Logger;

/**
 * The Class AbstractMapProviderConfigurator.
 *
 */
public class AbstractMapProviderConfigurator {

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(AbstractMapProviderConfigurator.class);

	/**
	 * Configure.
	 *
	 * @param abstractMapProvider
	 *            the abstract map provider
	 * @param conf
	 *            the conf
	 *
	 * @throws GeoEngineException
	 *             the geo engine exception
	 */
	public static void configure(AbstractMapProvider abstractMapProvider, Object conf) throws SvgViewerEngineException {

	}

}
