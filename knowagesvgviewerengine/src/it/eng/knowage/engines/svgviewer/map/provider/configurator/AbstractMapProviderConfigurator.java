package it.eng.knowage.engines.svgviewer.map.provider.configurator;

import it.eng.knowage.engines.svgviewer.SvgViewerEngineConstants;
import it.eng.knowage.engines.svgviewer.SvgViewerEngineException;
import it.eng.knowage.engines.svgviewer.map.provider.AbstractMapProvider;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;

import org.apache.log4j.Logger;

// TODO: Auto-generated Javadoc
/**
 * The Class AbstractMapProviderConfigurator.
 *
 * @author Andrea Gioia (andrea.gioia@eng.it)
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
		SourceBean confSB = null;

		if (conf instanceof String) {
			try {
				confSB = SourceBean.fromXMLString((String) conf);
			} catch (SourceBeanException e) {
				logger.error("Impossible to parse configuration block for MapProvider", e);
				throw new SvgViewerEngineException("Impossible to parse configuration block for MapProvider", e);
			}
		} else {
			confSB = (SourceBean) conf;
		}

		if (confSB != null) {
			String mapName = (String) confSB.getAttribute(SvgViewerEngineConstants.MAP_NAME_TAG);
			abstractMapProvider.setSelectedMapName(mapName);
		}
	}

}
