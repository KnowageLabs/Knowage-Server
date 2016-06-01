package it.eng.knowage.engines.svgviewer.map.renderer;

import it.eng.knowage.engines.svgviewer.SvgViewerEngineException;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class MapRenderingException extends SvgViewerEngineException {
	public MapRenderingException(String message) {
		super(message);
	}

	public MapRenderingException(String message, Throwable ex) {
		super(message, ex);
	}

	public MapRenderingException(Throwable ex) {
		super(ex);
	}
}
