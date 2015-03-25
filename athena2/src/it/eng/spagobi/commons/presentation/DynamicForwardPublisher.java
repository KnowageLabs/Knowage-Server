/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.presentation;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.presentation.PublisherDispatcherIFace;

/**
 * @author zoppello
 *
 * This publisher is useful as utilities to forward to a publisher defined in  service request
 * 
 * To use this publisher declare action as follow
 * 
 *  <ACTION name="START_ACTION" class="it.eng.spago.dispatching.action.util.PublishAction" scope="REQUEST">
 *		<CONFIG></CONFIG>
 *	</ACTION>
 * 
 * and associate in publisher with dynamic publisher in file presentation.xml
 * 
 * <MAPPING business_type="ACTION" business_name="PUBLISH_ACTION" publisher_name="DYN_FORWARD_PUBLISHER"/>
 * 
 *	where DYN_FORWARD_PUBLISHER is defined in publicher.xml file
 *
 *	<PUBLISHER name="DYN_FORWARD_PUBLISHER">
 *		<RENDERING channel="HTTP" type="JAVA" mode="">
 *			<RESOURCES>
 *				<ITEM prog="0"
 *					resource="it.eng.spagoextensions.DynamicForwardPublisher" />
 *			</RESOURCES>
 *		</RENDERING>
 *
 *		<RENDERING channel="PORTLET" type="JAVA" mode="">
 *			<RESOURCES>
 *				<ITEM mode="VIEW"
 *					resource="it.eng.spagoextensions.DynamicForwardPublisher" />
 *				<ITEM mode="EDIT"
 *					resource="it.eng.spagoextensions.DynamicForwardPublisher" />
 *			</RESOURCES>
 *		</RENDERING>
 *	</PUBLISHER>
 * 
 */
public class DynamicForwardPublisher implements PublisherDispatcherIFace {
	
	/**
	 * Class constructor.
	 */
	public DynamicForwardPublisher() {
		super();

	}
	
	/**
	 * Given the request at input, gets the name of the reference publisher,driving
	 * the execution into the correct jsp page, or jsp error page, if any error occurred.
	 * 
	 * @param request The request container object containing all request information
	 * @param response The response container object containing all response information
	 * 
	 * @return A string representing the name of the correct publisher, which will
	 * call the correct jsp reference.
	 */
	public String getPublisherName(RequestContainer request,
			ResponseContainer response) {

		String publisherName = (String) request.getServiceRequest().getAttribute(
				"PUBLISHER_NAME");
		
		if (publisherName != null) {
			return publisherName;
		} else {
			return "SERVICE_ERROR_PUBLISHER";
		}
	}
}

