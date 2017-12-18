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
package it.eng.spagobi.tools.scheduler.dispatcher;

import java.util.List;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.Snapshot;
import it.eng.spagobi.analiticalmodel.document.dao.ISnapshotDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.massiveExport.services.StartMassiveScheduleAction;
import it.eng.spagobi.tools.scheduler.to.DispatchContext;
import it.eng.spagobi.tools.scheduler.utils.JavaClassDestination;
import it.eng.spagobi.tools.scheduler.utils.SchedulerUtilities;

import org.apache.log4j.Logger;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class JavaClassDocumentDispatchChannel implements IDocumentDispatchChannel {
	
	private DispatchContext dispatchContext;
	
	// logger component
	private static Logger logger = Logger.getLogger(JavaClassDocumentDispatchChannel.class); 
	
	public JavaClassDocumentDispatchChannel(DispatchContext dispatchContext) {
		this.dispatchContext = dispatchContext;
	}
	
	public void setDispatchContext(DispatchContext dispatchContext) {
		this.dispatchContext = dispatchContext;
	}

	public void close() {
		
	}
	
	public boolean canDispatch(BIObject document)  {
		return true;
	}
	
	public boolean dispatch(BIObject document, byte[] executionOutput) {
		
		logger.debug("IN");

		String javaClass = dispatchContext.getJavaClassPath();
		if( (javaClass==null) || javaClass.trim().equals("")) {
			logger.error("Classe java nons specificata");
			return false;
		}
		// try to get new Instance
		JavaClassDestination jcDest=null;
		try{
			jcDest=(JavaClassDestination)Class.forName(javaClass).newInstance();
		}
		catch (ClassCastException e) {
			logger.error("Class "+javaClass+" does not extend JavaClassDestination class as expected");
			return false;
		}
		catch (Exception e) {
			logger.error("Error while instantiating the class "+javaClass);
			return false;
			}

		logger.debug("Sucessfull instantiation of "+javaClass);

		jcDest.setBiObj(document);
		jcDest.setDocumentByte(executionOutput);

		try{
			jcDest.execute();
		}
		catch (Exception e) {
			logger.error("Error during execution",e);
			return false;
		}


		logger.debug("OUT");

		return true;
	}
}
