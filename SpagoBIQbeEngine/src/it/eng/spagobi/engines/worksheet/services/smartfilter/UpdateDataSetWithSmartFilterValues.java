/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.worksheet.services.smartfilter;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.qbe.query.Query;
import it.eng.qbe.query.serializer.SerializerFactory;
import it.eng.qbe.serializer.SerializationException;
import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.qbe.FormState;
import it.eng.spagobi.engines.qbe.QbeEngineInstance;
import it.eng.spagobi.engines.qbe.services.formviewer.FormViewerQueryTransformer;
import it.eng.spagobi.engines.worksheet.services.AbstractWorksheetEngineAction;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 * The typical scenario:
 * 1) the user fills the form
 * 2) the user clicks the Open Worksheet button
 * 3) the client send a message to the server, and the server starts the worksheet engine 
 * 4) the client sent to the server the form values and the server update the DataSet
 * of the Worksheet engine with this new information..
 * This class performs the point 4
 *
 */
public class UpdateDataSetWithSmartFilterValues extends AbstractWorksheetEngineAction {

	private static final long serialVersionUID = 4107491809439616520L;	
    public static transient Logger logger = Logger.getLogger(UpdateDataSetWithSmartFilterValues.class);
    public static final String FORM_STATE = "formState";
    
    public void service(SourceBean request, SourceBean response)  {		
    	super.service(request, response);	
    	
		//Add the smart filter values
		try {
			QbeEngineInstance qbeEngineInstance = (QbeEngineInstance) getAttributeFromSession(EngineConstants.ENGINE_INSTANCE);
			Assert.assertNotNull(qbeEngineInstance, "It's not possible to execute " + this.getActionName() + " service before having properly created an instance of EngineInstance class");
			JSONObject jsonEncodedFormState = getAttributeAsJSONObject(FORM_STATE);
			
			FormState formState = qbeEngineInstance.getFormState();
			if (formState == null) {
				formState = new FormState();
				qbeEngineInstance.setFormState(formState);
			}
			formState.setFormStateValues(jsonEncodedFormState);
			
			updateDataSetWithSmartFilterQuery();

		} catch (Exception e) {
			logger.debug("Error loading the smart filter values");
			throw new SpagoBIEngineServiceException(getActionName(), "Error loading the smart filter values", e);
		}
    }

	/**
	 * Update the dataset of the WorkSheet engine
	 * @throws Exception
	 */
    private void updateDataSetWithSmartFilterQuery() throws Exception{
		JSONObject jsonFormState = null;
		logger.debug("Updating the data set in the worksheet engine with the smart filter form values..");
		QbeEngineInstance qbeEngineInstance = (QbeEngineInstance) getAttributeFromSession(EngineConstants.ENGINE_INSTANCE);
		Query query = qbeEngineInstance.getQueryCatalogue().getFirstQuery();
		jsonFormState = loadSmartFilterFormValues(qbeEngineInstance);
		//build the query filtered for the smart filter
		if (jsonFormState != null) {
			logger.debug("Form state retrieved as a string: " + jsonFormState);
			// transform the query
			query = updateQbeWithSmartFilterQuery(query, qbeEngineInstance, jsonFormState);
			// update active query on Qbe engine instance
			qbeEngineInstance.setActiveQuery(query);
			// update the data set in the Worksheet engine instance 
			IDataSet smartFilterUpdatedDS = qbeEngineInstance.getActiveQueryAsDataSet();
			getEngineInstance().setDataSet(smartFilterUpdatedDS);
		}
		logger.debug("The data set has been updated");
	}
	
	/**
	 * Loads the values of the form if the calling engine is smart filter
	 * @return
	 * @throws JSONException
	 */
	private JSONObject loadSmartFilterFormValues(QbeEngineInstance qbeEngine) throws JSONException {
		FormState formState = qbeEngine.getFormState();
		if ( formState != null ) {
			return formState.getFormStateValues(); 
		}
		return null;
	}
	
	/**
	 * Clones the query and apply the filters encoded in the jsonEncodedFormState
	 * @param query the source query
	 * @param jsonEncodedFormState the filters
	 * @return a clone of the input query with the filters
	 * @throws SerializationException
	 */
	private Query updateQbeWithSmartFilterQuery(Query query, QbeEngineInstance qbeInstance, JSONObject jsonEncodedFormState) throws SerializationException{
		Query toReturn = null;
		if ( jsonEncodedFormState != null ) {
			logger.debug("Making a deep copy of the original query...");
			String store = ((JSONObject)SerializerFactory.getSerializer("application/json").serialize(query, qbeInstance.getDataSource(), getLocale())).toString();
			Query copy = SerializerFactory.getDeserializer("application/json").deserializeQuery(store, qbeInstance.getDataSource());
			logger.debug("Deep copy of the original query produced");
			//JSONObject formState = new JSONObject(jsonEncodedFormState);
			//logger.debug("Form state converted into a valid JSONObject: " + formState.toString(3));
			JSONObject template = (JSONObject) qbeInstance.getFormState().getConf();
			logger.debug("Form viewer template retrieved.");
			
			FormViewerQueryTransformer formViewerQueryTransformer = new FormViewerQueryTransformer();
			formViewerQueryTransformer.setFormState(jsonEncodedFormState);
			formViewerQueryTransformer.setTemplate(template);
			logger.debug("Applying Form Viewer query transformation...");
			query = formViewerQueryTransformer.execTransformation(copy);
			logger.debug("Applying Form Viewer query transformation...");
			toReturn = copy;
		} else {
			toReturn = query;	
		}
		return toReturn;
	}

}
