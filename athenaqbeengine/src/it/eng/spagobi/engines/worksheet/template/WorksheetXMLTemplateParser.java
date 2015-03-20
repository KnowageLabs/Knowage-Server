/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.worksheet.template;

import it.eng.qbe.query.Query;
import it.eng.qbe.query.serializer.SerializerFactory;
import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.qbe.FormState;
import it.eng.spagobi.engines.qbe.QbeEngine;
import it.eng.spagobi.engines.qbe.QbeEngineInstance;
import it.eng.spagobi.engines.qbe.services.formviewer.FormViewerQueryTransformer;
import it.eng.spagobi.engines.qbe.template.QbeTemplate;
import it.eng.spagobi.engines.qbe.template.QbeTemplateParseException;
import it.eng.spagobi.engines.worksheet.bo.WorkSheetDefinition;
import it.eng.spagobi.engines.worksheet.template.loaders.IWorksheetXMLTemplateLoader;
import it.eng.spagobi.engines.worksheet.template.loaders.WorksheetXMLTemplateLoaderFactory;
import it.eng.spagobi.services.proxy.DataSetServiceProxy;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class WorksheetXMLTemplateParser implements IWorksheetTemplateParser{

	public final static String CURRENT_VERSION = "1";
	
	public final static String ATTRIBUTE_VERSION = "version";
	public final static String TAG_WORKSHEET_DEFINITION = "WORKSHEET_DEFINITION";
	public final static String TAG_QBE = "QBE";
	public final static String TAG_QBE_COMPOSITE = "COMPOSITE-QBE";
	public final static String TAG_SMART_FILTER = EngineConstants.SMART_FILTER_TAG;
	public final static String TAG_DATASET = "DATASET";

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(WorksheetXMLTemplateParser.class);
	public static final String QBE_ENGINE_INSTANCE = EngineConstants.ENGINE_INSTANCE;
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.worksheet.template.IWorksheetTemplateParser#parse(java.lang.Object)
	 */
	//@Override
	public WorksheetTemplate parse(Object template,  Map env) {
		Assert.assertNotNull(template, "Input parameter [template] cannot be null");
		Assert.assertTrue(template instanceof SourceBean, "Input parameter [template] cannot be of type [" + template.getClass().getName() + "]");
		return parse((SourceBean)template,  env);
	}

	private WorksheetTemplate parse(SourceBean xml, Map env) {

		WorksheetTemplate worksheetTemplate;
		SourceBean worksheetSB;
		JSONObject worksheetJSONTemplate;
		String encodingFormatVersion;
		SourceBean template;

		try {
			
			Assert.assertNotNull(xml, "SourceBean in input cannot be not be null");
			logger.debug("Parsing template [" + xml.getName() + "] ...");
			
			worksheetTemplate = new WorksheetTemplate();

			encodingFormatVersion = (String) xml.getAttribute(ATTRIBUTE_VERSION);
			
			if (encodingFormatVersion == null) {
				logger.debug("no version found, default is 0");
				encodingFormatVersion = "0";
			}
			
			logger.debug("Row data encoding version  [" + encodingFormatVersion + "]");
			
			if (encodingFormatVersion.equalsIgnoreCase(CURRENT_VERSION)) {				
				template = xml;
			} else {
				logger.warn("Row data encoding version [" + encodingFormatVersion + "] does not match with the current version used by the engine [" + CURRENT_VERSION + "] ");
				logger.debug("Converting from encoding version [" + encodingFormatVersion + "] to encoding version [" + CURRENT_VERSION + "]....");
				IWorksheetXMLTemplateLoader worksheetViewerXMLTemplateLoader;
				worksheetViewerXMLTemplateLoader = WorksheetXMLTemplateLoaderFactory.getInstance().getLoader(encodingFormatVersion);
				if (worksheetViewerXMLTemplateLoader == null) {
					throw new SpagoBIEngineException("Unable to load data stored in format [" + encodingFormatVersion + "] ");
				}
				template = (SourceBean) worksheetViewerXMLTemplateLoader.load(xml);
				logger.debug("Encoding conversion has been executed succesfully");
			}
			
			// QBE block
			if (template.containsAttribute(TAG_QBE)
					|| template.containsAttribute(TAG_QBE_COMPOSITE)
					|| template.containsAttribute(TAG_SMART_FILTER)) {
				SourceBean qbeTemplate = null;
				if (template.containsAttribute(TAG_QBE)) {
					qbeTemplate = (SourceBean) template.getAttribute(TAG_QBE);
				} else if (template.containsAttribute(TAG_SMART_FILTER)) {
					qbeTemplate = (SourceBean) template.getAttribute(TAG_SMART_FILTER);
				} else {
					qbeTemplate = (SourceBean) template.getAttribute(TAG_QBE_COMPOSITE);
				}
				if (qbeTemplate.containsAttribute(TAG_DATASET)) {
					SourceBean datasetSB = (SourceBean) qbeTemplate.getAttribute(TAG_DATASET);
					String datasetLabel = (String) datasetSB.getAttribute("label");
					List<IDataSet> dataSets = new ArrayList<IDataSet>();
					IDataSet dataset = ((DataSetServiceProxy) env.get(EngineConstants.ENV_DATASET_PROXY)).getDataSetByLabel(datasetLabel);
					dataSets.add(dataset);
					env.put(EngineConstants.ENV_DATASETS, dataSets);
				}
				QbeEngineInstance qbeEngineInstance;
				qbeEngineInstance = startQbeEngine(qbeTemplate, env);
				worksheetTemplate.setQbeEngineInstance(qbeEngineInstance);
			}

			// DATASET block
			if(template.containsAttribute(TAG_DATASET)) {}

			// worksheet block
			if(template.containsAttribute(TAG_WORKSHEET_DEFINITION)) {
				worksheetSB = (SourceBean) template.getAttribute(TAG_WORKSHEET_DEFINITION);
				worksheetJSONTemplate = new JSONObject(worksheetSB.getCharacters());
				worksheetTemplate.setWorkSheetDefinition(loadWorksheetDefinition(worksheetJSONTemplate));
			} else {
				logger.debug("The template does not contain tag [" + TAG_WORKSHEET_DEFINITION +"]");
			}

			logger.debug("Templete parsed succesfully");

		} catch(Throwable t) {
			throw new QbeTemplateParseException("Impossible to parse template [" + xml.toString()+ "]", t);
		} finally {
			logger.debug("OUT");
		}	

		return worksheetTemplate;
	}


	private WorkSheetDefinition loadWorksheetDefinition(JSONObject worksheetDefinition) {
		try {
			WorkSheetDefinition workSheetDefinition = new WorkSheetDefinition();
			workSheetDefinition.load( worksheetDefinition.toString().getBytes() );
			return workSheetDefinition;
		} catch(Throwable t) {
			SpagoBIRuntimeException serviceException;
			String msg = "Impossible load worksheet definition [" + worksheetDefinition + "].";
			Throwable rootException = t;
			while(rootException.getCause() != null) {
				rootException = rootException.getCause();
			}
			String str = rootException.getMessage()!=null? rootException.getMessage(): rootException.getClass().getName();
			msg += "\nThe root cause of the error is: " + str;
			serviceException = new SpagoBIRuntimeException(msg, t);

			throw serviceException;
		}
	}
	
    
    public QbeEngineInstance startQbeEngine(SourceBean template,  Map env) throws Exception {
     	logger.debug("Creating engine instance ...");
		QbeEngineInstance qbeEngineInstance = QbeEngine.createInstance(template, env);
		Query query = qbeEngineInstance.getQueryCatalogue().getFirstQuery();
		QbeTemplate qbeTemplate = qbeEngineInstance.getTemplate();
		if ( qbeTemplate.getProperty("formJSONTemplate") != null 
				&& qbeTemplate.getProperty("formValuesJSONTemplate") != null ) {
			// worksheet from smart filter
			query = applyFormViewerQueryTransformation( query, (JSONObject) qbeTemplate.getProperty("formJSONTemplate"), 
					(JSONObject) qbeTemplate.getProperty("formValuesJSONTemplate"), qbeEngineInstance );
		}
		qbeEngineInstance.setActiveQuery(query);
		qbeEngineInstance.getEnv().put("TEMPLATE", template); // TODO controllare a che serve
		return qbeEngineInstance;
    }

	private Query applyFormViewerQueryTransformation(Query query, JSONObject template,
			JSONObject formValues, QbeEngineInstance qbeInstance) throws Exception {
		Query toReturn = null;
		logger.debug("Making a deep copy of the original query...");
		String store = ((JSONObject)SerializerFactory.getSerializer("application/json").serialize(query, qbeInstance.getDataSource(), null)).toString();
		Query copy = SerializerFactory.getDeserializer("application/json").deserializeQuery(store, qbeInstance.getDataSource());
		logger.debug("Deep copy of the original query produced");
		FormState formState = new FormState();
		formState.setConf(template);
		formState.setFormStateValues(formValues);
		qbeInstance.setFormState(formState);
		
		FormViewerQueryTransformer formViewerQueryTransformer = new FormViewerQueryTransformer();
		formViewerQueryTransformer.setFormState(formValues);
		formViewerQueryTransformer.setTemplate(formState.getConf());
		logger.debug("Applying Form Viewer query transformation...");
		query = formViewerQueryTransformer.execTransformation(copy);
		logger.debug("Form Viewer query transformation applied successfully");
		toReturn = copy;
		return toReturn;
	}

	public boolean hasInnerQbeQuery(Object template) {
		Assert.assertNotNull(template, "Input parameter [template] cannot be null");
		Assert.assertTrue(template instanceof SourceBean, "Input parameter [template] cannot be of type [" + template.getClass().getName() + "]");
		SourceBean templateSourceBean = (SourceBean) template;
		boolean toReturn = templateSourceBean.containsAttribute(TAG_QBE) || templateSourceBean.containsAttribute(TAG_QBE_COMPOSITE);
		return toReturn;
	}

}
