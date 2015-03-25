/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.events.handlers;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.events.bo.EventLog;

public interface IEventPresentationHandler {

	/**
	 * Load event info.
	 * 
	 * @param event the event
	 * @param response the response
	 * 
	 * @throws SourceBeanException the source bean exception
	 * @throws EMFUserError the EMF user error
	 */
	public void loadEventInfo(EventLog event, SourceBean response) throws SourceBeanException, EMFUserError;
	
}
