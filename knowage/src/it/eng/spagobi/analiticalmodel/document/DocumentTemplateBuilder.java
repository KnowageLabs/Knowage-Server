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
package it.eng.spagobi.analiticalmodel.document;

import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.analiticalmodel.document.handlers.ExecutionInstance;
import it.eng.spagobi.analiticalmodel.execution.service.SaveDocumentAction;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.engines.drivers.worksheet.WorksheetDriver;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class DocumentTemplateBuilder {
	
	// logger component
	private static Logger logger = Logger.getLogger(DocumentTemplateBuilder.class);
	
	public ObjTemplate buildDocumentTemplate(String templateName, String templateAuthor, String templateContent) {
		Assert.assertNotNull(templateName, "Input parameter [templateName] cannot be null");
		Assert.assertNotNull(templateName, "Input parameter [templateContent] cannot be null");
		return buildDocumentTemplate(templateName, templateAuthor, templateContent.getBytes());
	}
	
	public ObjTemplate buildDocumentTemplate(String templateName, String templateAuthor, byte[] templateContent) {
		ObjTemplate template = new ObjTemplate();
		 
		try {
			Assert.assertTrue(StringUtilities.isNotEmpty(templateName), "Input parameter [templateName] cannot be null or empty");
			Assert.assertNotNull(templateName, "Input parameter [templateContent] cannot be null");
			
			//template.setName("template.sbiworksheet");
			template.setName( templateName );
			template.setContent( templateContent );
			template.setDimension(Long.toString(templateContent.length/1000) + " KByte");
			if(templateAuthor != null) template.setCreationUser( templateAuthor );
		} catch(Throwable t) {
			throw new SpagoBIRuntimeException("An unexpected error occured while building the template", t);
		}
		
		return template;
	}
	
	
	

	
	public ObjTemplate buildSmartFilterDocumentTemplate(String templateName, String templateAuthor,
			BIObject parentQbeDocument, String query, String worksheetData, JSONObject smartFilterData, String modelName) {
		
		ObjTemplate template;
		
		logger.debug("IN");
		
		template = null;
		try {
			String smartFilterValuesString = null;
			if(smartFilterData != null){
				smartFilterValuesString = smartFilterData.toString();	
			}
			
			WorksheetDriver worksheetDriver = new WorksheetDriver();
			
			String worksheetDocumentTemplate = null;
			if(parentQbeDocument != null) {
				ObjTemplate parentQbeDocumentTemplate = parentQbeDocument.getActiveTemplate();
				String parentQbeDocumentTemplateContent = new String(parentQbeDocumentTemplate.getContent());
				worksheetDocumentTemplate = worksheetDriver.composeWorksheetTemplate(worksheetData, query, smartFilterValuesString, parentQbeDocumentTemplateContent);
			} else if(parentQbeDocument == null && worksheetData != null) {
				worksheetDocumentTemplate = worksheetDriver.createNewWorksheetTemplate(worksheetData, modelName, query);
			} 
			
			template = buildDocumentTemplate(templateName, templateAuthor, worksheetDocumentTemplate);
		} catch(Throwable t) {
			throw new SpagoBIRuntimeException("An unexpected error occured while building template", t);
		} finally {
			logger.debug("OUT");
		}
		
		return template;
	}
}
