/**
 *
 */
package it.eng.spagobi.engines.whatif.template.initializer;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.whatif.template.WhatIfTemplate;

/**
 * @author Dragan Pirkovic
 *
 */
public interface Initializer {
	public void init(SourceBean template, WhatIfTemplate toReturn);
}
