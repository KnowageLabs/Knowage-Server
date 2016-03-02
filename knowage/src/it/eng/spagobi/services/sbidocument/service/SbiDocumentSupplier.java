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
package it.eng.spagobi.services.sbidocument.service;

import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.services.sbidocument.bo.SpagobiAnalyticalDriver;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class SbiDocumentSupplier {

	static private Logger logger = Logger.getLogger(SbiDocumentSupplier.class);

	/**
	 * Gets the data set.
	 * 
	 * @param documentId the document id
	 * 
	 * @return the data set
	 */
	public SpagobiAnalyticalDriver[] getDocumentAnalyticalDrivers(Integer id, String language, String country) {

		logger.debug("IN");
		SpagobiAnalyticalDriver[] toReturn = null;
		if (id == null) {
			logger.error("document id is null");
			return null;
		}
//		get all analytical drivers
		try{

			List listPars = DAOFactory.getBIObjectParameterDAO().loadBIObjectParametersById(id);
			if (listPars == null) {
				logger.error("Could not retrieve parameters for the object with id " + id + " ");
				return null;
			}
			toReturn = new SpagobiAnalyticalDriver[listPars.size()];
			int i = 0;
			for (Iterator iterator = listPars.iterator(); iterator.hasNext();) {
				BIObjectParameter par = (BIObjectParameter) iterator.next();
				Parameter parameter = DAOFactory.getParameterDAO().loadForDetailByParameterID(par.getParameter().getId());
				SpagobiAnalyticalDriver toAdd = new SpagobiAnalyticalDriver(
						par.getId(),	
						par.getLabel(),
						parameter.getType(),
						par.getParameterUrlName(),
						null		// values, not needed
				);
				toReturn[i] = toAdd;
				i++;

			}

		} catch (Exception e) {
			logger.error("The dataset is not correctly returned", e);	
		} finally {
			logger.debug("OUT");
		}

		return toReturn;
	}

	/**
	 * Gets the data set by label.
	 * 
	 * @param label the ds label
	 * 
	 * @return the data set by label
	 */
	public String getDocumentAnalyticalDriversJSON(Integer id, String language, String country) {
		logger.debug("IN");
		String toReturn = "[{}]";
		if (id == null) {
			logger.error("document id is null");
			return toReturn;
		}
//		get all analytical drivers
		try{

			List listPars = DAOFactory.getBIObjectParameterDAO().loadBIObjectParametersById(id);
			if (listPars == null) {
				logger.error("Could not retrieve parameters for the object with id " + id + " ");
				return toReturn;

			}

			toReturn = addDocumentParametersInfoJSON(listPars, language, country);

		} catch (Exception e) {
			logger.error("The dataset is not correctly returned", e);	
		} finally {
			logger.debug("String returned is "+toReturn);
			logger.debug("OUT");
		}
		return toReturn;	
	}


	/**
	 * Adds a Json representation contaning info about document parameters (url name, label, type)
	 * @param biobject The BIObject under execution
	 * @param map The parameters map
	 * @return the modified map with the new parameter
	 */
	private String addDocumentParametersInfoJSON(List parameters, String language, String country) {
		logger.debug("IN");
		JSONArray parametersJSON = new JSONArray();
		try {
			Locale locale = new Locale(language, country);
//			List parameters = biobject.getBiObjectParameters();
			if (parameters != null && parameters.size() > 0) {
				Iterator iter = parameters.iterator();
				while (iter.hasNext()) {
					BIObjectParameter biparam = (BIObjectParameter) iter.next();
					JSONObject jsonParam = new JSONObject();
					jsonParam.put("id", biparam.getParameterUrlName());
					IMessageBuilder msgBuilder = MessageBuilderFactory.getMessageBuilder();
					//String interLabel = msgBuilder.getUserMessage(biparam.getLabel(), SpagoBIConstants.DEFAULT_USER_BUNDLE, locale);
					String interLabel = msgBuilder.getI18nMessage(locale, biparam.getLabel());
					jsonParam.put("label", interLabel);
					Parameter parameter = DAOFactory.getParameterDAO().loadForDetailByParameterID(biparam.getParameter().getId());
					jsonParam.put("type", parameter.getType());
					parametersJSON.put(jsonParam);
				}
			}
		} catch (Exception e) {
			logger.error("Error while adding document parameters info", e);
		}
		//map.put("SBI_DOCUMENT_PARAMETERS", parametersJSON.toString());
		logger.debug("OUT");
		return parametersJSON.toString();
	}


}