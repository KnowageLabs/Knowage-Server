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
package it.eng.spagobi.analiticalmodel.execution.service;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.Viewpoint;
import it.eng.spagobi.analiticalmodel.document.dao.IViewpointDAO;
import it.eng.spagobi.analiticalmodel.document.handlers.ExecutionInstance;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.SerializationException;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONSuccess;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class SaveViewpointAction extends AbstractSpagoBIAction {

	public static final String SERVICE_NAME = "SAVE_VIEWPOINTS_SERVICE";

	// request parameters
	private static final String NAME = "name";
	private static final String DESCRIPTION = "description";
	private static final String SCOPE = "scope";
	private static final String VIEWPOINT = "viewpoint";

	// logger component
	private static Logger logger = Logger.getLogger(SaveViewpointAction.class);

	@Override
	public void doService() {

		ExecutionInstance executionInstance;
		IEngUserProfile userProfile;
		Integer biobjectId;

		String viewpointName;
		String viewpointDescription;
		String viewpointScope;
		String viewpointOwner;
		JSONObject viewpointJSON;
		String viewpointString;

		IViewpointDAO viewpointDAO;
		Viewpoint viewpoint;

		logger.debug("IN");

		try {

			viewpointName = getAttributeAsString(NAME);
			viewpointDescription = getAttributeAsString(DESCRIPTION);
			viewpointScope = getAttributeAsString(SCOPE);
			viewpointJSON = getAttributeAsJSONObject(VIEWPOINT);

			logger.debug("Parameter [" + NAME + "] is equals to [" + viewpointName + "]");
			logger.debug("Parameter [" + DESCRIPTION + "] is equals to [" + viewpointDescription + "]");
			logger.debug("Parameter [" + SCOPE + "] is equals to [" + viewpointScope + "]");
			logger.debug("Parameter [" + viewpointScope + "] is equals to [" + viewpointJSON + "]");

			Assert.assertTrue(!StringUtils.isEmpty(viewpointScope), "Viewpoint's name cannot be null or empty");
			Assert.assertNotNull(!StringUtils.isEmpty(viewpointDescription),
					"Viewpoint's description cannot be null or empty");
			Assert.assertNotNull(!StringUtils.isEmpty(viewpointScope), "Viewpoint's scope cannot be null or empty");
			Assert.assertNotNull(viewpointJSON, "Viewpoint's content cannot be null");

			executionInstance = getContext().getExecutionInstance(ExecutionInstance.class.getName());
			Assert.assertNotNull(executionInstance, "Execution instance cannot be null");

			userProfile = this.getUserProfile();
			Assert.assertNotNull(userProfile, "Impossible to retrive user profile");

			biobjectId = executionInstance.getBIObject().getId();
			Assert.assertNotNull(executionInstance, "Impossible to retrive analytical document id");

			logger.debug("User: [" + ((UserProfile) userProfile).getUserId() + "]");
			logger.debug("Document Id:  [" + biobjectId + "]");

			viewpointOwner = (String) ((UserProfile) userProfile).getUserId();

			Iterator it = viewpointJSON.keys();
			Assert.assertTrue(it.hasNext(), "Viewpoint's content cannot be empty");
			viewpointString = "";
			while (it.hasNext()) {
				String parameterName = (String) it.next();
				String parameterValue;
				try {
					parameterValue = viewpointJSON.getString(parameterName);
				} catch (JSONException e) {
					logger.error(
							"Impossible read value for the parameter [" + parameterName + "] into viewpoint's content",
							e);
					throw new SpagoBIServiceException(SERVICE_NAME,
							"Impossible read value for the parameter [" + parameterName + "] into viewpoint's content",
							e);
				}

				// defines the string of parameters to save into db
				if (!StringUtils.isEmpty(parameterValue)) {
					viewpointString += parameterName + "%3D" + parameterValue + "%26";
				}
			}

			if (viewpointString.endsWith("%26")) {
				viewpointString = viewpointString.substring(0, viewpointString.length() - 3);
			}

			logger.debug("Viewpoint's content will be saved on database as: [" + viewpointString + "]");

			try {
				viewpointDAO = DAOFactory.getViewpointDAO();
				viewpoint = viewpointDAO.loadViewpointByNameAndBIObjectId(viewpointName, biobjectId);
				if (viewpoint != null)
					throw new SpagoBIServiceException(SERVICE_NAME,
							"A viewpoint with the name [" + viewpointName + "] alredy exist");
				// Assert.assertTrue(viewpoint == null, "A viewpoint with the name [" + viewpointName + "] alredy exist");
			} catch (EMFUserError e) {
				logger.error("Impossible to check if a viewpoint with name [" + viewpointName + "] already exists", e);
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Impossible to check if a viewpoint with name [" + viewpointName + "] already exists", e);
			}

			try {
				viewpointDAO = DAOFactory.getViewpointDAO();
				viewpointDAO.setUserProfile(userProfile);
				viewpoint = new Viewpoint();
				viewpoint.setBiobjId(biobjectId);
				viewpoint.setVpName(viewpointName);
				viewpoint.setVpOwner(viewpointOwner);
				viewpoint.setVpDesc(viewpointDescription);
				viewpoint.setVpScope(viewpointScope);
				viewpoint.setVpValueParams(viewpointString);
				viewpoint.setVpCreationDate(new Timestamp(System.currentTimeMillis()));
				viewpointDAO.insertViewpoint(viewpoint);

				// reload viewpoint with new ID
				viewpoint = viewpointDAO.loadViewpointByNameAndBIObjectId(viewpointName, biobjectId);

			} catch (EMFUserError e) {
				logger.error("Impossible to save viewpoint [" + viewpointName + "]", e);
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Impossible to check if a viewpoint with name [" + viewpointName + "] already exists", e);
			}

			try {
				JSONObject results = (JSONObject) SerializerFactory.getSerializer("application/json")
						.serialize(viewpoint, null);
				writeBackToClient(new JSONSuccess(results));
			} catch (IOException e) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to write back the responce to the client",
						e);
			} catch (SerializationException e) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Cannot serialize objects", e);
			}

		} finally {
			logger.debug("OUT");
		}

	}

}
