/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

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
