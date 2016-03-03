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
package it.eng.spagobi.sdk.documents;

import it.eng.spagobi.sdk.documents.bo.SDKDocument;
import it.eng.spagobi.sdk.documents.bo.SDKDocumentParameter;
import it.eng.spagobi.sdk.documents.bo.SDKDocumentParameterValue;
import it.eng.spagobi.sdk.documents.bo.SDKExecutedDocumentContent;
import it.eng.spagobi.sdk.documents.bo.SDKFunctionality;
import it.eng.spagobi.sdk.documents.bo.SDKSchema;
import it.eng.spagobi.sdk.documents.bo.SDKTemplate;
import it.eng.spagobi.sdk.exceptions.InvalidParameterValue;
import it.eng.spagobi.sdk.exceptions.MissingParameterValue;
import it.eng.spagobi.sdk.exceptions.NonExecutableDocumentException;
import it.eng.spagobi.sdk.exceptions.NotAllowedOperationException;
import it.eng.spagobi.sdk.exceptions.SDKException;

import java.util.HashMap;

public interface DocumentsService {

	SDKDocument[] getDocumentsAsList(String type, String state, String folderPath);

	SDKDocument getDocumentById(Integer id);

	SDKDocument getDocumentByLabel(String label);

	SDKFunctionality getDocumentsAsTree(String initialPath);

	String[] getCorrectRolesForExecution(Integer documentId) throws NonExecutableDocumentException;

	SDKDocumentParameter[] getDocumentParameters(Integer documentId, String roleName) throws NonExecutableDocumentException;

	SDKDocumentParameterValue[] getAdmissibleValues(Integer documentParameterId, String roleName) throws NonExecutableDocumentException;

	SDKDocumentParameterValue[] getDefaultValues(Integer documentParameterId, String roleName) throws NonExecutableDocumentException;

	SDKTemplate downloadTemplate(Integer documentId) throws NotAllowedOperationException;

	void uploadTemplate(Integer documentId, SDKTemplate template) throws NotAllowedOperationException;

	Integer saveNewDocument(SDKDocument document, SDKTemplate template, Integer functionalityId) throws NotAllowedOperationException;

	SDKExecutedDocumentContent executeDocument(SDKDocument document, SDKDocumentParameter[] parameters, String roleName, String outputType)
			throws NonExecutableDocumentException, NotAllowedOperationException, InvalidParameterValue, MissingParameterValue;

	void uploadDatamartTemplate(SDKTemplate template, SDKTemplate calculatedFields, String dataSourceLabel, String categoryLabel);

	void uploadDatamartModel(SDKTemplate template);

	SDKTemplate downloadDatamartFile(String folderName, String fileName);

	SDKTemplate downloadDatamartModelFiles(String folderName, String fileDatamartName, String fileModelName);

	HashMap<String, String> getAllDatamartModels() throws NotAllowedOperationException;

	void uploadMondrianSchema(SDKSchema schema) throws NotAllowedOperationException, SDKException;
}
