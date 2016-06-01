package it.eng.knowage.engines.svgviewer.map.renderer.configurator;

import it.eng.knowage.engines.svgviewer.SvgViewerEngineException;
import it.eng.knowage.engines.svgviewer.map.renderer.InteractiveMapRenderer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;

import org.apache.log4j.Logger;

// TODO: Auto-generated Javadoc
/**
 * The Class InteractiveMapRendererConfigurator.
 *
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class InteractiveMapRendererConfigurator {

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(InteractiveMapRendererConfigurator.class);

	/**
	 * Configure.
	 *
	 * @param interactiveMapRenderer
	 *            the interactive map renderer
	 * @param conf
	 *            the conf
	 *
	 * @throws SvgViewerEngineException
	 *             the geo engine exception
	 */
	public static void configure(InteractiveMapRenderer interactiveMapRenderer, Object conf) throws SvgViewerEngineException {
		SourceBean confSB = null;

		if (conf instanceof String) {
			try {
				confSB = SourceBean.fromXMLString((String) conf);
			} catch (SourceBeanException e) {
				logger.error("Impossible to parse configuration block for MapRenderer", e);
				throw new SvgViewerEngineException("Impossible to parse configuration block for MapRenderer", e);
			}
		} else {
			confSB = (SourceBean) conf;
		}
	}

}
