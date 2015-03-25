/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.scheduler.dispatcher;

import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.tools.scheduler.to.DispatchContext;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public interface IDocumentDispatchChannel {
	void setDispatchContext(DispatchContext dispatchContext);
	boolean canDispatch(BIObject document);
	boolean dispatch(BIObject document, byte[] executionOutput);
	void close();
}
