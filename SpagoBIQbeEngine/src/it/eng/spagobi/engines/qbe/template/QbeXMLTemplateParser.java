/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.template;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.engines.qbe.externalservices.ExternalServiceConfiguration;
import it.eng.spagobi.engines.qbe.registry.bo.RegistryConfiguration;
import it.eng.spagobi.engines.qbe.registry.parser.RegistryConfigurationXMLParser;
import it.eng.spagobi.utilities.assertion.Assert;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.json.JSONObject;


/**
 * The Class QbeTemplate.
 * 
 * @author Andrea Gioia
 */
public class QbeXMLTemplateParser implements IQbeTemplateParser{
	
	public static String TAG_ROOT_COMPOSITE = "COMPOSITE-QBE";
	public static String TAG_ROOT_NORMAL = "QBE";
	public static String TAG_ROOT_SMART_FILTER = "SMART_FILTER";
	public static String TAG_DATAMART = "DATAMART";
	public static String PROP_DATAMART_NAME = "name";
	public static String PROP_DATAMART_DBLINK = "dblink";
	public static String PROP_DATAMART_MAXRECURSIONLEVEL = "maxRecursionLevel";
	public static String TAG_MODALITY = "MODALITY";
	public static String TAG_MODALITY_TABLE = "TABLE";
	public static String TAG_FUNCTIONALITIES = "FUNCTIONALITIES";
	public static String TAG_QUERY = "QUERY";
	public static String TAG_FORM = "FORM";
	public static String TAG_FORM_VALUES = "FORM_VALUES";
//	public static String TAG_WORKSHEET_DEFINITION = "WORKSHEET_DEFINITION";
	public static String TAG_EXTERNAL_SERVICES = "EXTERNAL_SERVICES";
	public static String TAG_EXTERNAL_SERVICE = "EXTERNAL_SERVICE";
	public static String PROP_SERVICE_DESCRIPTION = "description";
	public static String PROP_SERVICE_ENDPOINT = "endpoint";
	public static String PROP_SERVICE_OPERATION = "operation";
	public static String PROP_SERVICE_REQUIREDCOLUMNS = "requiredcolumns";
	public static String TAG_REGISTRY = "REGISTRY";
	

	/** Logger component. */
    public static transient Logger logger = Logger.getLogger(QbeXMLTemplateParser.class);
	
    public QbeTemplate parse(Object template) {
    	Assert.assertNotNull(template, "Input parameter [template] cannot be null");
    	Assert.assertTrue(template instanceof SourceBean, "Input parameter [template] cannot be of type [" + template.getClass().getName() + "]");
    	return parse((SourceBean)template);
    }
    
	private QbeTemplate parse(SourceBean template) {
		
		QbeTemplate qbeTemplate = null;
		
		String templateName;
		SourceBean datamartSB, querySB, formSB, worksheetSB;
		String dmName;
		SourceBean modalitySB;
		List modalities;
		SourceBean compositeModalitySB;
		SourceBean functionalitiesSB;
		JSONObject formJSONTemplate, formValuesJSONTemplate, queryJSON, worksheetJSONTemplate;
		
		try {
			
			qbeTemplate = new QbeTemplate();
			
			templateName = template.getName();
			logger.debug("Parsing template [" + templateName + "] ...");
			Assert.assertNotNull(templateName, "Root tag cannot be not be null");
			
			
			if(!TAG_ROOT_COMPOSITE.equalsIgnoreCase(templateName)
					&& !TAG_ROOT_NORMAL.equalsIgnoreCase(templateName)
					&& !TAG_ROOT_SMART_FILTER.equalsIgnoreCase(templateName)){
				
				QbeTemplateParseException e = new QbeTemplateParseException("Malformed template structure");
				e.setDescription("template root tag cannot be equals to [" + templateName +"]. " +
						"It must be equal to [" + TAG_ROOT_NORMAL + "] or [" + TAG_ROOT_COMPOSITE + "]");
				e.addHint("Check document template in document details page");
				throw e;
			}
			
			boolean isComposite;
			isComposite = TAG_ROOT_COMPOSITE.equalsIgnoreCase(templateName);
			qbeTemplate.setComposite( isComposite );
			
			modalities  = new ArrayList();
			dmName = null;
			
			if(qbeTemplate.isComposite()) {
				List qbeList;
				SourceBean qbeSB;
				String dblink;
				String maxRecursionLevel;
				
				logger.debug("The QBE described in the template is of type COMPOSITE");
								
				qbeList = template.getAttributeAsList(TAG_ROOT_NORMAL);
				for(int i = 0; i < qbeList.size(); i++) {
					qbeSB = (SourceBean)qbeList.get(i);
					
					// DATAMART block
					if(qbeSB.containsAttribute(TAG_DATAMART)) {
						datamartSB = (SourceBean)qbeSB.getAttribute(TAG_DATAMART);	
						dmName = (String)datamartSB.getAttribute(PROP_DATAMART_NAME);
						Assert.assertTrue(!StringUtilities.isEmpty(dmName), "Attribute [" + PROP_DATAMART_NAME +"] in tag [" + TAG_DATAMART + "] must be properly defined");
						
						qbeTemplate.addDatamartName( dmName );
										
						dblink = (String)datamartSB.getAttribute(PROP_DATAMART_DBLINK);
						if(dblink != null) {
							qbeTemplate.setDbLink( dmName, dblink );
						}
						
						maxRecursionLevel = (String)datamartSB.getAttribute(PROP_DATAMART_MAXRECURSIONLEVEL);
						if(maxRecursionLevel != null) {
							qbeTemplate.setProperty("maxRecursionLevel", maxRecursionLevel);
						}
						
					} else {
						Assert.assertUnreachable("Missing compolsury tag [" + TAG_DATAMART + "]");
					}
					
					// MODALITY block
					if(qbeSB.containsAttribute(TAG_MODALITY)) {
						modalitySB = (SourceBean)qbeSB.getAttribute(TAG_MODALITY);
						modalities.add(modalitySB);		
					} else {
						logger.debug("Qbe template associated to datamart [" + dmName + "] does not contain tag [" + TAG_MODALITY +"] so it will be not profiled");
					}
				}
				
				// query block 
				if(template.containsAttribute(TAG_QUERY)) {
					querySB = (SourceBean) template.getAttribute(TAG_QUERY);
					queryJSON = new JSONObject(querySB.getCharacters());
					qbeTemplate.setProperty("query", queryJSON);
				} else {
					logger.debug("Qbe template does not contain tag [" + TAG_QUERY +"]");
				}
				
			} else {
				logger.debug("The QBE described in the template is of type STANDARD");
				
				// DATAMART block
				if(template.containsAttribute(TAG_DATAMART)) {
					datamartSB = (SourceBean)template.getAttribute(TAG_DATAMART);
					dmName = (String)datamartSB.getAttribute(PROP_DATAMART_NAME);
					Assert.assertTrue(!StringUtilities.isEmpty(dmName), "Attribute [" + PROP_DATAMART_NAME +"] in tag [" + TAG_DATAMART + "] must be properly defined");
					qbeTemplate.addDatamartName( dmName );
					
					String maxRecursionLevel = (String)datamartSB.getAttribute(PROP_DATAMART_MAXRECURSIONLEVEL);
					if(maxRecursionLevel != null) {
						qbeTemplate.setProperty("maxRecursionLevel", maxRecursionLevel);
					}
				}// else {
					//Assert.assertUnreachable("Missing compolsury tag [" + TAG_DATAMART + "]");
				//}
				
				// MODALITY block
				if(template.containsAttribute(TAG_MODALITY)) {
					modalitySB = (SourceBean)template.getAttribute(TAG_MODALITY);
					modalities.add(modalitySB);
				} else {
					logger.debug("Qbe template does not contain tag [" + TAG_MODALITY +"] so it will be not profiled");
				}
				
				// query block 
				if(template.containsAttribute(TAG_QUERY)) {
					querySB = (SourceBean) template.getAttribute(TAG_QUERY);
					queryJSON = new JSONObject(querySB.getCharacters());
					qbeTemplate.setProperty("query", queryJSON);
				} else {
					logger.debug("Qbe template does not contain tag [" + TAG_QUERY +"]");
				}
				
				// form block
				if(template.containsAttribute(TAG_FORM)) {
					formSB = (SourceBean) template.getAttribute(TAG_FORM);
					formJSONTemplate = new JSONObject(formSB.getCharacters());
					qbeTemplate.setProperty("formJSONTemplate", formJSONTemplate);
				} else {
					logger.debug("Qbe template does not contain tag [" + TAG_FORM +"]");
				}
				
				// form values block
				if(template.containsAttribute(TAG_FORM_VALUES)) {
					formSB = (SourceBean) template.getAttribute(TAG_FORM_VALUES);
					formValuesJSONTemplate = new JSONObject(formSB.getCharacters());
					qbeTemplate.setProperty("formValuesJSONTemplate", formValuesJSONTemplate);
				} else {
					logger.debug("Qbe template does not contain tag [" + TAG_FORM_VALUES +"]");
				}
				
//				// worksheet block
//				if(template.containsAttribute(TAG_WORKSHEET_DEFINITION)) {
//					worksheetSB = (SourceBean) template.getAttribute(TAG_WORKSHEET_DEFINITION);
//					worksheetJSONTemplate = new JSONObject(worksheetSB.getCharacters());
//					qbeTemplate.setProperty("worksheetJSONTemplate", worksheetJSONTemplate);
//				} else {
//					logger.debug("Qbe template does not contain tag [" + TAG_WORKSHEET_DEFINITION +"]");
//				}
			}
			
			compositeModalitySB = new SourceBean(TAG_MODALITY);
			
			for(int i = 0; i < modalities.size(); i++) {
				modalitySB = (SourceBean)modalities.get(i);
				String recursiveFilteringAttr = (String)modalitySB.getAttribute(QbeXMLModelAccessModality.ATTR_RECURSIVE_FILTERING);
				if(!StringUtilities.isEmpty(recursiveFilteringAttr)) {
					compositeModalitySB.setAttribute(QbeXMLModelAccessModality.ATTR_RECURSIVE_FILTERING, recursiveFilteringAttr);
				}
				List tables = modalitySB.getAttributeAsList(TAG_MODALITY_TABLE);
				for(int j = 0; j < tables.size(); j++) {
					SourceBean tableSB = (SourceBean)tables.get(j);
					compositeModalitySB.setAttribute(tableSB);
				}
			}
					
			if(compositeModalitySB != null && compositeModalitySB.getAttribute(TAG_MODALITY_TABLE) != null) { 
				QbeXMLModelAccessModality datamartModelAccessModality = new QbeXMLModelAccessModality(compositeModalitySB);
				qbeTemplate.setDatamartModelAccessModality(datamartModelAccessModality);
			}
			
			if(template.containsAttribute(TAG_EXTERNAL_SERVICES)) {
				SourceBean extServiceConfig = (SourceBean) template.getAttribute(TAG_EXTERNAL_SERVICES);
				List services = extServiceConfig.getAttributeAsList(TAG_EXTERNAL_SERVICE);
				Iterator it = services.iterator();
				while (it.hasNext()) {
					SourceBean aServiceConfig = (SourceBean) it.next();
					logger.debug("Reading external service configuration....");
					String description = (String) aServiceConfig.getAttribute(PROP_SERVICE_DESCRIPTION);
					logger.debug("Description = [" + description + "]");
					String endpoint = (String) aServiceConfig.getAttribute(PROP_SERVICE_ENDPOINT);
					logger.debug("Endpoint = [" + endpoint + "]");
					String operation = (String) aServiceConfig.getAttribute(PROP_SERVICE_OPERATION);
					logger.debug("Operation = [" + operation + "]");
					String requiredColumns = (String) aServiceConfig.getAttribute(PROP_SERVICE_REQUIREDCOLUMNS);
					logger.debug("Required columns = [" + requiredColumns + "]");
					if (description == null || description.trim().equals("")) {
						logger.error("External service configuration is not valid: " + PROP_SERVICE_DESCRIPTION + " attribute is mandatory.");
						QbeTemplateParseException e = new QbeTemplateParseException("Wrong external service configuration");
						e.setDescription("External service configuration is not valid: " + PROP_SERVICE_DESCRIPTION 
								+ " attribute is mandatory.");
						e.addHint("Check document template in external service details section");
						throw e;
					}
					if (endpoint == null || endpoint.trim().equals("")) {
						logger.error("External service configuration is not valid:  " + PROP_SERVICE_ENDPOINT + " attribute is mandatory.");
						QbeTemplateParseException e = new QbeTemplateParseException("Wrong external service configuration");
						e.setDescription("External service configuration is not valid:  " + PROP_SERVICE_ENDPOINT
								+ " attribute is mandatory.");
						e.addHint("Check document template in external service details section");
						throw e;
					}
					
					ExternalServiceConfiguration conf = new ExternalServiceConfiguration();
					String id = UUID.randomUUID().toString();
					logger.debug("Created id = " + id + " for service with description [" + description + "]");
					conf.setId(id);
					conf.setDescription(description);
					conf.setEndpoint(endpoint);
					conf.setOperation(operation);
					if (requiredColumns != null && !requiredColumns.trim().equals("")) {
						conf.setRequiredColumns(requiredColumns.split(","));
					}
					qbeTemplate.addExternalServiceConfiguration(conf);
				}
				
			} else {
				logger.debug("Qbe template does not contain tag [" + TAG_EXTERNAL_SERVICES +"]");
			}
			
			
			if (template.containsAttribute(TAG_REGISTRY)) {
				SourceBean registryConf = (SourceBean) template.getAttribute(TAG_REGISTRY);
				RegistryConfigurationXMLParser parser = new RegistryConfigurationXMLParser();
				RegistryConfiguration conf = parser.parse(registryConf);
				qbeTemplate.setProperty("registryConfiguration", conf);
			} else {
				logger.debug("Qbe template does not contain tag [" + TAG_REGISTRY +"]");
			}
			
			logger.debug("Templete parsed succesfully");
		} catch(Throwable t) {
			throw new QbeTemplateParseException("Impossible to parse template [" + template.toString()+ "]", t);
		} finally {
			logger.debug("OUT");
		}	
		
		return qbeTemplate;
	}
}
