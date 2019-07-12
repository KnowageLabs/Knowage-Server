/**
 *
 */
package it.eng.spagobi.engines.whatif.template.initializer.executor.datasource;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.whatif.template.WhatIfTemplate;
import it.eng.spagobi.engines.whatif.template.initializer.executor.InitializationExecutor;
import it.eng.spagobi.engines.whatif.template.initializer.impl.XMLADataSourceInitializerImpl;

/**
 * @author Dragan Pirkovic
 *
 */
public class XMLDatasourceExecutor extends InitializationExecutor {

	@Override
	public void init(SourceBean template, WhatIfTemplate toReturn) {
		new XMLADataSourceInitializerImpl().init(template, toReturn);
		super.init(template, toReturn);
	}
}
