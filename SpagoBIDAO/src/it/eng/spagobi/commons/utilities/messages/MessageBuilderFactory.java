/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.utilities.messages;

import it.eng.spago.base.ApplicationContainer;
import it.eng.spagobi.commons.constants.SpagoBIConstants;


public class MessageBuilderFactory {
	
	/**
	 * Gets the message builder.
	 * 
	 * @return the message builder
	 */
	public static IMessageBuilder getMessageBuilder() {
		ApplicationContainer spagoContext = ApplicationContainer.getInstance();
		IMessageBuilder msgBuilder = (IMessageBuilder)spagoContext.getAttribute(SpagoBIConstants.MESSAGE_BUILDER);
		if(msgBuilder==null) {
			msgBuilder = new MessageBuilder();
			spagoContext.setAttribute(SpagoBIConstants.MESSAGE_BUILDER, msgBuilder);
		}	
		return msgBuilder;
	}
	
}
