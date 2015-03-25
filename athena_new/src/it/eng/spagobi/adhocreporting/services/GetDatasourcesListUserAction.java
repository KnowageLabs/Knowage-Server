/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.adhocreporting.services;

import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.tools.datasource.bo.DataSource;
import it.eng.spagobi.tools.datasource.dao.IDataSourceDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Zerbetto Davide (davide.zerbetto@eng.it)
 * 
 *         This action is intended for final users; it doesn't use standard
 *         serializer for datasources, since no sensitive information (URL, JDBC
 *         user and pwd) should be sent to final users. It filters HBase or Hive
 *         datasource, since they cannot be used, at the moment, as datasources
 *         for dataset persistence.
 * 
 */
public class GetDatasourcesListUserAction extends AbstractSpagoBIAction {

	public static final String SERVICE_NAME = "GET_DATASOURCES_LIST_ACTION";
	
	public static final String ID = "id";
	public static final String LABEL = "label";
	public static final String DESCRIPTION = "description";
	
	// logger component
	private static Logger logger = Logger.getLogger(GetDatasourcesListUserAction.class);

	public void doService() {
		logger.debug("IN");
		try {
			
			IDataSourceDAO dao;
			IEngUserProfile profile = getUserProfile();
			try {
				dao = DAOFactory.getDataSourceDAO();
				dao.setUserProfile(profile);
			} catch (EMFUserError e) {				
				throw new SpagoBIServiceException(SERVICE_NAME, "Cannot access database", e);
			}
			
			List<DataSource> items = null;
			try {
				items = dao.loadAllDataSources();
			} catch (Exception e) {
				throw new SpagoBIServiceException("Error while getting datasources' list", e);
			}
			
			JSONObject responseJSON = null;
			try {
				JSONArray itemsJSON = new JSONArray();
				Iterator<DataSource> it = items.iterator();
				while (it.hasNext()) {
					DataSource datasource = it.next();
					String dialect = datasource.getHibDialectClass();
					// HBase and Hive cannot be selected in order to persist a dataset, therefore we exclude them.
					// TODO When implementing dataset persistence on those system, remove this filter.
					if (dialect.contains("hbase") && dialect.contains("hive")) {
						continue;
					}
					JSONObject obj = new JSONObject();
					obj.put(ID, datasource.getDsId());
					obj.put(LABEL, datasource.getLabel());
					obj.put(DESCRIPTION, datasource.getDescr());
					itemsJSON.put(obj);
				}
				responseJSON = createJSONResponse(itemsJSON, itemsJSON.length());
			} catch (Exception e) {
				throw new SpagoBIServiceException(
						"Error while serializing data", e);
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

	protected JSONObject createJSONResponse(JSONArray rows,
			Integer totalResNumber) throws JSONException {
		JSONObject results;
		results = new JSONObject();
		results.put("total", totalResNumber);
		results.put("title", "Datasources");
		results.put("rows", rows);
		return results;
	}
	
}
