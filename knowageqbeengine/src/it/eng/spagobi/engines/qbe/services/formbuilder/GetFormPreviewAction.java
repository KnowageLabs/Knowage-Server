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
import it.eng.spagobi.engines.qbe.FormState;
import it.eng.spagobi.engines.qbe.QbeEngineInstance;
import it.eng.spagobi.engines.qbe.services.core.AbstractQbeEngineAction;
import it.eng.spagobi.engines.qbe.services.initializers.FormEngineStartAction;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.SpagoBIEngineStartupException;

import org.apache.log4j.Logger;

/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 */
public class GetFormPreviewAction extends AbstractQbeEngineAction {
	
	/** Logger component. */
    private static transient Logger logger = Logger.getLogger(GetFormPreviewAction.class);
   
	public void service(SourceBean request, SourceBean response)  {
		
		logger.debug("IN");
		
		try {		
			super.service(request, response);
			
			QbeEngineInstance engineInstance = getEngineInstance();
			Assert.assertNotNull(engineInstance, "It's not possible to execute " + this.getActionName() + " service before having properly created an instance of EngineInstance class");
			
			FormState fs = engineInstance.getFormState();
			Assert.assertTrue(fs != null && fs.getConf() != null, 
					"It's not possible to execute " + this.getActionName() + " service before having properly created a form template");
			
			setAttribute(FormEngineStartAction.ENGINE_INSTANCE, engineInstance);
			
		} catch(Throwable e) {
			SpagoBIEngineStartupException serviceException = null;
			
			if(e instanceof SpagoBIEngineStartupException) {
				serviceException = (SpagoBIEngineStartupException)e;
			} else {
				Throwable rootException = e;
				while(rootException.getCause() != null) {
					rootException = rootException.getCause();
				}
				String str = rootException.getMessage()!=null? rootException.getMessage(): rootException.getClass().getName();
				String message = "An unpredicted error occurred while executing " + this.getActionName() + " service."
								 + "\nThe root cause of the error is: " + str;
				
				serviceException = new SpagoBIEngineStartupException(getActionName(), message, e);
			}
			
			throw serviceException;
		} finally {			
			logger.debug("OUT");
		}	
	}
}
