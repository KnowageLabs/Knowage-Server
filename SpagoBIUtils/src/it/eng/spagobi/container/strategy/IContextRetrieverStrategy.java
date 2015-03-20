/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.container.strategy;

import it.eng.spagobi.container.Context;
import it.eng.spagobi.container.IBeanContainer;

/**
 * A strategy is delegated to create/retrieve/destroy the it.eng.spagobi.container.Context on a it.eng.spagobi.container.ISessionContainer instance.
 * 
 * @author Zerbetto (davide.zerbetto@eng.it)
 *
 */
public interface IContextRetrieverStrategy {

	public Context getContext(IBeanContainer sessionContainer);
	
	public Context createContext(IBeanContainer sessionContainer);
	
	public void destroyCurrentContext(IBeanContainer sessionContainer);
	
	public void destroyContextsOlderThan(IBeanContainer sessionContainer, int minutes);
	
}
