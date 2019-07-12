/**
 *
 */
package it.eng.spagobi.engines.whatif.template.initializer.executor;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.whatif.template.initializer.executor.datasource.MondrianDatasourceExecutor;
import it.eng.spagobi.engines.whatif.template.initializer.executor.datasource.XMLDatasourceExecutor;

/**
 * @author Dragan Pirkovic
 *
 */
public class InitializationExecutorFactory {

	public static final String TAG_XMLA_DATASOURCE = "xmlaserver";

	public InitializationExecutorFactory(SourceBean template) {

	}

	public static InitializationExecutor getExecutor(SourceBean template) {
		if (template.getAttribute(TAG_XMLA_DATASOURCE) != null) {
			return new XMLDatasourceExecutor();
		} else {
			return new MondrianDatasourceExecutor();
		}
	}
}
