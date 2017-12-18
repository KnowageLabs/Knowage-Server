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
package it.eng.spagobi.adhocreporting.services;

import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.constants.DataSetConstants;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.dataset.service.ManageDatasets;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Zerbetto Davide (davide.zerbetto@eng.it)
 */
public class GetDatasetsUserListAction extends ManageDatasets {

	public static final String SERVICE_NAME = "GET_DATASETS_USER_LIST";
	
	// logger component
	private static Logger logger = Logger.getLogger(GetDatasetsUserListAction.class);

	public void doService() {
		logger.debug("IN");
		try {
			
			IDataSetDAO dao;
			profile = getUserProfile();
	
			try {
				dao = DAOFactory.getDataSetDAO();
				dao.setUserProfile(profile);
			} catch (EMFUserError e) {				
				throw new SpagoBIServiceException(SERVICE_NAME, "Cannot access database", e);
			}
			Locale locale = getLocale();
			List<IDataSet> items = null;
			getSpagoBIRequestContainer().set( DataSetConstants.START , new Integer(0) );
			getSpagoBIRequestContainer().set( DataSetConstants.LIMIT , Integer.MAX_VALUE );
			
			try {
				items = getListOfGenericDatasets(dao);
			} catch (Exception e) {
				throw new SpagoBIServiceException("Error while getting datasets' list", e);
			}
			logger.debug("Loaded items list");
			Integer totalItemsNum = items.size();
			logger.debug("Items number is " + totalItemsNum);
			
			JSONObject responseJSON = null;
			try {
				JSONArray itemsJSON = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(items, locale);
				responseJSON = createJSONResponse(itemsJSON, totalItemsNum);
			} catch (Exception e) {
				throw new SpagoBIServiceException("Error while serializing data", e);
			}
			
			try {
				writeBackToClient( new JSONSuccess( responseJSON ) );
			} catch (IOException e) {
				throw new SpagoBIServiceException("Impossible to write back the responce to the client", e);
			}
			
		} finally {
			logger.debug("OUT");
		}
	}

}
