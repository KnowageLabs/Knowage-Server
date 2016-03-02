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
package it.eng.spagobi.engines.kpi.service;

import it.eng.spagobi.analiticalmodel.document.handlers.ExecutionInstance;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.json.JSONObject;


public class ManageTrendAction extends AbstractSpagoBIAction{
	
	private static transient Logger logger = Logger.getLogger(ManageTrendAction.class);
	ExecutionInstance instance = null;
	
	@Override
	public void doService() {
		logger.debug("IN");
		try {
			String format  = GeneralUtilities.getServerTimeStampFormat();			
			logger.debug("Got Date format: "+(format!=null ? format : "null"));
			String timeFromFormat = " 00:00:00"; 
			String timeToFormat = " 23:59:59"; 
			/* it couldn't be necessary:
			if (format.contains("hh")) {
				timeFromFormat = " 01:00:00";
				timeToFormat = " 11:59:59";
			}*/
			
			String kpiInstanceID = this.getAttributeAsString("kpiInstId");
			logger.debug("Got Instance ID: "+ kpiInstanceID);
			String kpiBeginDate = this.getAttributeAsString("dateFrom") + timeFromFormat;
			logger.debug("Got Begin Date: "+ kpiBeginDate);
			String kpiEndDate = this.getAttributeAsString("dateTo") + timeToFormat;
			logger.debug("Got End Date: "+ kpiEndDate);
			
			//converts the params string in date type 
			SimpleDateFormat f = new SimpleDateFormat();
			f.applyPattern(format);	
			Date dKpiBeginDate = new Date();
			dKpiBeginDate = f.parse(kpiBeginDate);
			Date dKpiEndDate = new Date();
			dKpiEndDate = f.parse(kpiEndDate);
			JSONObject jsonData = null;
			//gets the chart's data	
			if (kpiInstanceID!=null){
				jsonData = DAOFactory.getKpiDAO().getKpiTrendJSONResult(Integer.valueOf(kpiInstanceID), dKpiBeginDate, dKpiEndDate);
				if(jsonData!=null){
					try {
						writeBackToClient( new JSONSuccess( jsonData ) );
					} catch (IOException e) {
						throw new SpagoBIServiceException("Impossible to write back the responce to the client", e);
					}
				}else{
					throw new SpagoBIServiceException(SERVICE_NAME,"No data found");
				}
			}
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw new SpagoBIServiceException(SERVICE_NAME,"sbi.ds.testError", e);
		}
		logger.debug("OUT");
		
	}
	
	
}
