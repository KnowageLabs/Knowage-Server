/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
