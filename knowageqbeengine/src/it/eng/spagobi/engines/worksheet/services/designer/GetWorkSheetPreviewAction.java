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

package it.eng.spagobi.engines.worksheet.services.designer;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.worksheet.WorksheetEngineInstance;
import it.eng.spagobi.engines.worksheet.services.AbstractWorksheetEngineAction;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;

import org.apache.log4j.Logger;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *          Davide Zerbetto (davide.zerbetto@eng.it)
 */
public class GetWorkSheetPreviewAction extends AbstractWorksheetEngineAction {

	private static final long serialVersionUID = 4009276536964480679L;
	
	/** Logger component. */
    public static transient Logger logger = Logger.getLogger(GetWorkSheetPreviewAction.class);

	public void service(SourceBean request, SourceBean response) {
		logger.debug("IN");

		try {

			super.service(request, response);

			WorksheetEngineInstance engineInstance = this.getEngineInstance();
			Assert.assertNotNull(
					engineInstance,
					"It's not possible to execute "
							+ this.getActionName()
							+ " service before having properly created an instance of WorksheetEngineInstance class");

			setAttribute(WorksheetEngineInstance.class.getName(),
					engineInstance);

		} catch (Throwable t) {
			throw SpagoBIEngineServiceExceptionHandler.getInstance()
					.getWrappedException(getActionName(), getEngineInstance(),
							t);
		} finally {
			logger.debug("OUT");
		}
	}
}
