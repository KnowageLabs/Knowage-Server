/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.events.handlers;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.events.bo.EventLog;

public class DefaultEventPresentationHandler implements
		IEventPresentationHandler {

	/* (non-Javadoc)
	 * @see it.eng.spagobi.events.handlers.IEventPresentationHandler#loadEventInfo(it.eng.spagobi.events.bo.EventLog, it.eng.spago.base.SourceBean)
	 */
	public void loadEventInfo(EventLog event, SourceBean response) throws SourceBeanException, EMFUserError {
		response.setAttribute("firedEvent", event);
	}

}
