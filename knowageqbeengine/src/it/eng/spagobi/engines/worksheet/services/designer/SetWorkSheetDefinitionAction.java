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
import it.eng.spagobi.engines.worksheet.services.AbstractWorksheetEngineAction;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.service.JSONAcknowledge;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *          Davide Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class SetWorkSheetDefinitionAction extends AbstractWorksheetEngineAction {

	private static final long serialVersionUID = -7253525210753136929L;
	
	// INPUT PARAMETERS
	public static final String WORKSHEET_DEFINITION = "worksheetdefinition";
	
	public static transient Logger logger = Logger.getLogger(SetWorkSheetDefinitionAction.class);
	
	
	/**
	 * Get the definition of the worksheet from the request, serialize and save it into the qbe engine instance
	 */
	public void service(SourceBean request, SourceBean response)  {				

		logger.debug("IN");
		
		super.service(request, response);	
		try {
			//get the worksheet definition from the request
			JSONObject worksheetDefinitionJSON = getAttributeAsJSONObject( WORKSHEET_DEFINITION );
			Assert.assertNotNull(worksheetDefinitionJSON, "Parameter [" +  WORKSHEET_DEFINITION + "] cannot be null in oder to execute " + this.getActionName() + " service");
			logger.debug("Parameter [" + WORKSHEET_DEFINITION + "] is equals to [" + worksheetDefinitionJSON.toString() + "]");

			updateWorksheetDefinition(worksheetDefinitionJSON);
			
		} catch(Throwable t) {
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException(getActionName(), getEngineInstance(), t);
		} finally {
			logger.debug("OUT");
		}	
		try {
			writeBackToClient(new JSONAcknowledge());	
		} catch (IOException e) {
			String message = "Impossible to write back the responce to the client";
			throw new SpagoBIEngineServiceException(getActionName(), message, e);
		}
	}
}
	