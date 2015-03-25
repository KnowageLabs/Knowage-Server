/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.documentsbrowser.service;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.SerializationException;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.tools.objmetadata.bo.ObjMetadata;
import it.eng.spagobi.tools.objmetadata.dao.IObjMetadataDAO;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GetCategoriesList extends AbstractSpagoBIAction{
	
	private static Logger logger = Logger.getLogger(GetCategoriesList.class);
	@Override
	public void doService() {
		logger.debug("IN");
		try {
			JSONArray toReturn = new JSONArray();
			IObjMetadataDAO metadataDAO = DAOFactory.getObjMetadataDAO();
			List results = metadataDAO.loadAllObjMetadata();
			ArrayList objects = new ArrayList();
			for (int i = 0; i < results.size(); i++) {
				// look for binary content mimetype
				ObjMetadata metadata = (ObjMetadata)results.get(i);			
				objects.add(metadata);
			}
			toReturn = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(objects, null);
			writeBackToClient( new JSONSuccess( toReturn ) ); 
		} catch (EMFUserError e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage(), e);
		} catch (SerializationException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		
		logger.debug("OUT");
		
	}
}
