package it.eng.knowage.engines.svgviewer.component;

import it.eng.knowage.engines.svgviewer.SvgViewerEngineException;

import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class AbstractGeoEngineComponent.
 *
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class AbstractSvgViewerEngineComponent implements ISvgViewerEngineComponent {

	/** The conf. */
	Object conf;

	/** The env. */
	Map env;

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.geo.IGeoEngineComponent#init(java.lang.Object)
	 */
	@Override
	public void init(Object conf) throws SvgViewerEngineException {
		this.conf = conf;
	}

	/**
	 * Gets the conf.
	 *
	 * @return the conf
	 */
	protected Object getConf() {
		return conf;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.geo.IGeoEngineComponent#setEnv(java.util.Map)
	 */
	@Override
	public void setEnv(Map env) {
		this.env = env;
	}

	/**
	 * Gets the env.
	 *
	 * @return the env
	 */
	public Map getEnv() {
		return env;
	}

}
