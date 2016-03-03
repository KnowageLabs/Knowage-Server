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
package it.eng.spagobi.engines.worksheet.services.initializers;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.worksheet.services.export.ExportWorksheetAction;
import it.eng.spagobi.services.proxy.SbiDocumentServiceProxy;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineStartupException;

import java.util.Locale;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @authors Giulio Gavardi
 * 
 * 
 */
public class MassiveExportWorksheetEngineStartAction extends
		WorksheetEngineStartAction {

	/** Logger component. */
	private static transient Logger logger = Logger
			.getLogger(MassiveExportWorksheetEngineStartAction.class);

	private final String SERVICE_NAME = "MASSIVE_EXPORT_WORKSHEET_ENGINE_START_ACTION";

	@Override
	public void service(SourceBean serviceRequest, SourceBean serviceResponse) {
		logger.debug("IN");
		try {
			super.service(serviceRequest, serviceResponse);
			Locale locale = getLocale();
			String docId = getDocumentId();
			logger.debug("document Id: " + docId);

			SbiDocumentServiceProxy proxy = new SbiDocumentServiceProxy(
					getUserProfile().getUserUniqueIdentifier().toString(), getHttpSession());
			logger.debug("recover paramters for metadata");
			try {
				String jSonPars = proxy.getDocumentAnalyticalDriversJSON(
						Integer.valueOf(docId), locale.getLanguage(),
						locale.getCountry());
				logger.debug("parameters for metadata " + jSonPars);
				if (jSonPars != null) {
					JSONArray array = new JSONArray(jSonPars);
					// add name and values
					for (int i = 0; i < array.length(); i++) {
						JSONObject par = (JSONObject) array.get(i);
						// String name = par.getString("label");
						String id = par.getString("id");
						String name = par.getString("label");

						String nameDescription = getAttributeAsString(id
								+ "_description");
						String value = getAttributeAsString(id);
						// put name, description and value
						par.put("name", name);
						if (nameDescription != null)
							par.put("description", nameDescription);
						if (value != null)
							par.put("value", value);
						else
							par.put("value", "");
					}
					logger.debug("add parameters JSON array " + array);
					serviceResponse.setAttribute(
							ExportWorksheetAction.PARAMETERS, array);
				}

			} catch (Exception e) {
				logger.debug("Error in retrieving parameters information for metadata purpose, go on aniway");
			}

		} catch (SpagoBIEngineStartupException e) {
			throw new SpagoBIEngineServiceException(SERVICE_NAME,
					"Error during initialziation of worksheet document", e);
		} catch (SpagoBIEngineServiceException e) {
			throw e;
		} catch (Throwable e) {
			logger.error("Error during initialziation of worksheet document ",
					e);
			throw new SpagoBIEngineServiceException(SERVICE_NAME,
					"Error during initialziation of worksheet document ", e);
		}

		logger.debug("OUT");
	}

}
