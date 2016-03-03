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

import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.handlers.ExecutionInstance;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.constants.ObjectsTreeConstants;
import it.eng.spagobi.commons.serializer.SerializationException;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class GetParametersForExecutionAction extends AbstractSpagoBIAction {

	public static final String SERVICE_NAME = "GET_PARAMETERS_FOR_EXECUTION_SERVICE";

	// request parameters
	public static String DOCUMENT_ID = ObjectsTreeConstants.OBJECT_ID;
	public static String DOCUMENT_LABEL = ObjectsTreeConstants.OBJECT_LABEL;
	public static String CALLBACK = "callback";
	// logger component
	private static Logger logger = Logger.getLogger(GetParameterValuesForExecutionAction.class);

	@Override
	public void doService() {

		List parametersForExecution = getParameters();

		JSONArray parametersJSON = null;

		try {
			parametersJSON = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(parametersForExecution, getLocale());
		} catch (SerializationException e) {
			e.printStackTrace();
		}

		String callback = getAttributeAsString(CALLBACK);
		logger.debug("Parameter [" + CALLBACK + "] is equals to [" + callback + "]");

		try {
			writeBackToClient(new JSONSuccess(parametersJSON, callback));
		} catch (IOException e) {
			throw new SpagoBIServiceException("Impossible to write back the responce to the client", e);
		}
	}

	public List<ParameterForExecution> getParameters() {

		List parametersForExecution;
		ExecutionInstance executionInstance;

		Assert.assertNotNull(getContext(), "Execution context cannot be null");
		Assert.assertNotNull(getContext().getExecutionInstance(ExecutionInstance.class.getName()), "Execution instance cannot be null");

		parametersForExecution = new ArrayList();

		executionInstance = getContext().getExecutionInstance(ExecutionInstance.class.getName());

		BIObject document = executionInstance.getBIObject();

		List parameters = document.getBiObjectParameters();
		if (parameters != null && parameters.size() > 0) {
			Iterator it = parameters.iterator();
			while (it.hasNext()) {
				BIObjectParameter parameter = (BIObjectParameter) it.next();
				parametersForExecution.add(new ParameterForExecution(parameter, executionInstance));
			}
		}

		return parametersForExecution;
	}

}
