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
package it.eng.spagobi.analiticalmodel.document.handlers;

import it.eng.spago.base.SourceBean;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.commons.dao.DAOFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import sun.misc.BASE64Encoder;

/**
 * Manages and implements utilities and task execution for BIObject notes functionality
 */
public class BIObjectNotesManager {

	// logger component
	private static Logger logger = Logger.getLogger(BIObjectNotesManager.class);
	
	public String getExecutionIdentifier(Integer biobjID, String executionRole, HashMap execUrlParMapWithValues ) {
		
		logger.debug("IN");
		BIObject biobj = null;
		try {
			biobj = DAOFactory.getBIObjectDAO().loadBIObjectForExecutionByIdAndRole(biobjID, executionRole);
		} catch (EMFUserError e1) {
			logger.error("EMFUser Error",e1);
			e1.printStackTrace();
		}
		
		BIObjectParameter biobjpar = null;
		String parUrlName = null;
		String identif = null;
		List biobjpars = null;
		Iterator iterBiobjPars = null;
		
		if(biobj!=null){
			identif = "biobject=" + biobj.getLabel() + "&";
			biobjpars = biobj.getBiObjectParameters();
			iterBiobjPars = biobjpars.iterator();
			while(iterBiobjPars.hasNext()){
				biobjpar = (BIObjectParameter)iterBiobjPars.next();
				Parameter par = biobjpar.getParameter();
				if((par==null) || (!par.isFunctional())){
					continue;
				}
				parUrlName = biobjpar.getParameterUrlName();
				String parValue = (String)execUrlParMapWithValues.get(parUrlName);
			 	identif = identif + parUrlName + "=" + parValue;
			 	if(iterBiobjPars.hasNext()){
			 		identif = identif + "&";
			 	}
			}
			logger.debug("identifier produced : " + identif);
		}
		
		BASE64Encoder encoder = new BASE64Encoder();
		
		String ecodedIdentif = "";
		int index = 0;
		while(index<identif.length()){
			String tmpStr = "";
			try{
				tmpStr = identif.substring(index, index + 10);
			} catch (Exception e) {
				tmpStr = identif.substring(index, identif.length());
			}
			String tmpEncoded = encoder.encode(tmpStr.getBytes());
			ecodedIdentif = ecodedIdentif + tmpEncoded;
			index = index + 10;
		}

		logger.debug("end method execution, returning encoded identifier: " + ecodedIdentif);
		logger.debug("OUT");
		return ecodedIdentif;
	}
	
	
	/**
	 * Return an identifier for a specific execution. The identifier is composed using the
	 * parameters url names and values.
	 * 
	 * @param biobj The biobject executed. The biobject must be filled with the parameter value
	 * selected by the user
	 * 
	 * @return String of the biobject execution identifier
	 */
	public String getExecutionIdentifier(BIObject biobj ) {
		logger.debug("start method execution for biobject label " + biobj.getLabel());
		List biparvalues = null;
		BIObjectParameter biobjpar = null;
		String parUrlName = null;
		Iterator iterBiparValues = null;
		String identif = null;
		List biobjpars = null;
		Iterator iterBiobjPars = null;
		String parValueString = null;
		
		identif = "biobject=" + biobj.getLabel() + "&";
		biobjpars = biobj.getBiObjectParameters();
		iterBiobjPars = biobjpars.iterator();
		while(iterBiobjPars.hasNext()){
			biobjpar = (BIObjectParameter)iterBiobjPars.next();
			Parameter par = biobjpar.getParameter();
			
			if((par==null) || (!par.isFunctional())){
				continue;
			}
			parUrlName = biobjpar.getParameterUrlName();
		 	biparvalues = biobjpar.getParameterValues();
		 	if (biparvalues == null) 
		 		continue;
		 	iterBiparValues = biparvalues.iterator();
		 	parValueString = "";
		 	while(iterBiparValues.hasNext()){
		 		String value = iterBiparValues.next().toString();
		 		parValueString = parValueString + value;
		 		if(iterBiparValues.hasNext()){
		 			parValueString = parValueString + ",";
		 		}
		 	}
		 	identif = identif + parUrlName + "=" + parValueString;
		 	if(iterBiobjPars.hasNext()){
		 		identif = identif + "&";
		 	}
		}
		logger.debug("identifier produced : " + identif);
		BASE64Encoder encoder = new BASE64Encoder();
		
		String ecodedIdentif = "";
		int index = 0;
		while(index<identif.length()){
			String tmpStr = "";
			try{
				tmpStr = identif.substring(index, index + 10);
			} catch (Exception e) {
				tmpStr = identif.substring(index, identif.length());
			}
			String tmpEncoded = encoder.encode(tmpStr.getBytes());
			ecodedIdentif = ecodedIdentif + tmpEncoded;
			index = index + 10;
		}

		logger.debug("end method execution, returning encoded identifier: " + ecodedIdentif);
		return ecodedIdentif;
	}
	
}
