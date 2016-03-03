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
package it.eng.spagobi.engines.qbe.services.formbuilder;
		
import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.qbe.QbeEngineInstance;
import it.eng.spagobi.engines.qbe.services.core.AbstractQbeEngineAction;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.service.JSONAcknowledge;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 */
public class SetFormBuilderStateAction  extends AbstractQbeEngineAction {	

	// INPUT PARAMETERS
	public static final String FORM_STATE = "FORM_STATE";
	
	
	/** Logger component. */
    private static transient Logger logger = Logger.getLogger(SetFormBuilderStateAction.class);
   
	public void service(SourceBean request, SourceBean response)  {
		
		logger.debug("IN");
		
		try {		
			super.service(request, response);
			
			QbeEngineInstance engineInstance = getEngineInstance();
			Assert.assertNotNull(engineInstance, "It's not possible to execute " + this.getActionName() + " service before having properly created an instance of EngineInstance class");
			
			String formState = this.getAttributeAsString(FORM_STATE);
			logger.debug(FORM_STATE + " input parameter is " + formState);
			
			JSONObject formJson = new JSONObject(formState);
			logger.debug(FORM_STATE + " input parameter parsed correctly as a JSONObject");
			
			engineInstance.getFormState().setConf(formJson);
			
			try {
				writeBackToClient( new JSONAcknowledge() );
			} catch (IOException e) {
				throw new SpagoBIEngineServiceException(getActionName(), "Impossible to write back the responce to the client", e);
			}
			
		} catch(Throwable t) {
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException(getActionName(), getEngineInstance(), t);
		} finally {			
			logger.debug("OUT");
		}	
	}
}
