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
package it.eng.spagobi.analiticalmodel.document.presentation;

import org.apache.log4j.Logger;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.presentation.PublisherDispatcherIFace;

public class ExecuteDocumentAngularPublisherJava implements PublisherDispatcherIFace {

	static private Logger logger = Logger.getLogger(ExecuteDocumentAngularPublisherJava.class);

	@Override
	public String getPublisherName(RequestContainer requestContainer, ResponseContainer responseContainer) {
		logger.debug("IN");
		try {
			EMFErrorHandler errorHandler = responseContainer.getErrorHandler();

			if (errorHandler.isOK()) {
				return "ExecuteDocumentAngularPublisher";
			} else {
				return "error";
			}

		} finally {
			logger.debug("OUT");
		}

	}

}
