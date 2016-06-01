package it.eng.knowage.engines.svgviewer.component;

import it.eng.knowage.engines.svgviewer.SvgViewerEngineException;

import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Interface IGeoEngineComponent.
 *
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public interface ISvgViewerEngineComponent {

	/**
	 * Inits the.
	 *
	 * @param conf
	 *            the conf
	 *
	 * @throws GeoEngineException
	 *             the geo engine exception
	 */
	void init(Object conf) throws SvgViewerEngineException;

	/**
	 * Sets the env.
	 *
	 * @param env
	 *            the new env
	 */
	void setEnv(Map env);
}
