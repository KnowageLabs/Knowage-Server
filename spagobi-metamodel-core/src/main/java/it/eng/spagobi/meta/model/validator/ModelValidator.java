/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.spagobi.meta.model.validator;

import it.eng.spagobi.meta.model.Model;
import it.eng.spagobi.meta.model.business.BusinessColumn;
import it.eng.spagobi.meta.model.business.BusinessColumnSet;
import it.eng.spagobi.meta.model.business.BusinessIdentifier;
import it.eng.spagobi.meta.model.business.BusinessModel;
import it.eng.spagobi.meta.model.business.BusinessRelationship;
import it.eng.spagobi.meta.model.business.BusinessTable;
import it.eng.spagobi.meta.model.physical.PhysicalModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class ModelValidator {
	
	List<String> diagnosticMessages;
	
	public List<String> getDiagnosticMessages() {
		return diagnosticMessages;
	}
	
	public String getDiagnosticMessage() {
		String msg = "Model contains the following validation errors: ";
		for(String m : getDiagnosticMessages()) {
			msg += "\n - " + m;
		}
		return msg;
	}

	public boolean validate(Model model) {
		boolean isValid = true;
		diagnosticMessages = new ArrayList();
		
		List<PhysicalModel> physicalModels = model.getPhysicalModels();
		if(physicalModels == null || physicalModels.size() < 1) {
			diagnosticMessages.add("Model does not contains any physical model");
		} else {
			for(PhysicalModel physicalModel : physicalModels) {
				isValid = isValid && validate(physicalModel);
			}
		}
		
		List<BusinessModel> businessModels = model.getBusinessModels();
		if(businessModels == null || businessModels.size() < 1) {
			diagnosticMessages.add("Model does not contains any business model");
		} else {
			for(BusinessModel businessModel : businessModels) {
				isValid = isValid && validate(businessModel);
			}
		}
		
		return isValid;
	}
	
	public boolean validate(PhysicalModel physicalModel) {
		boolean isValid = true;
		
		return isValid;
	}
	
	public boolean validate(BusinessModel businessModel) {
		boolean isValid = true;
		
		List<BusinessIdentifier> identifiers = businessModel.getIdentifiers();
		for(BusinessIdentifier identifier : identifiers) {
			isValid = isValid && validate(identifier);
		}
		
		List<BusinessRelationship> relationships = businessModel.getRelationships();
		for(BusinessRelationship relationship : relationships) {
			isValid = isValid && validate(relationship);
		}
		
		List<BusinessTable> tables = businessModel.getBusinessTables();
		for(BusinessTable table : tables) {
			isValid = isValid && validate(table);
		}
		
		return isValid;
	}
	
	public boolean validate(BusinessRelationship relationship) {
		
		if(relationship.getSourceTable() == null) {
			diagnosticMessages.add("Business relationship [" + relationship.getName() + "] does not specifies any source table");
			return false;
		}
		
		if(relationship.getDestinationTable() == null) {
			diagnosticMessages.add("Business relationship  [" + relationship.getName() + "] does not specifies any source table");
			return false;
		}
		
		if(relationship.getSourceColumns() == null || relationship.getSourceColumns().size() < 1) {
			diagnosticMessages.add("Outbound business relationship  [" + relationship.getName() + "] defined on table [" + relationship.getSourceTable().getName() + "] does not specifies any source column");
			return false;
		}
		
		if(relationship.getDestinationColumns() == null || relationship.getDestinationColumns().size() < 1) {
			diagnosticMessages.add("inbound business relationship  [" + relationship.getName() + "] defined on table [" + relationship.getDestinationTable().getName() + "] does not specifies any destination column");
			return false;
		}
		
		
		return true;
	}
	
	public boolean validate(BusinessIdentifier identifier) {
		if(identifier.getModel() == null) {
			diagnosticMessages.add("Business identifier  [" + identifier.getName() + "] does not belong to any model");
			return false;
		}
		BusinessModel model = identifier.getModel();
		
		if(identifier.getTable() == null) {
			diagnosticMessages.add("Business identifier  [" + identifier.getName() + "] is not associated to any table");
			return false;
		}
		BusinessColumnSet table = identifier.getTable();
		
		String tableUniqueName = table.getUniqueName();
		if(model.getTableByUniqueName(tableUniqueName) == null) {
			diagnosticMessages.add("Business identifier  [" + identifier.getName() + "] is defined on table [" + table.getName() + "] that does not belong to the model");
			return false;
		}
		
		if(identifier.getColumns().size() < 1) {
			diagnosticMessages.add("Business table  [" + table.getName() + "] have an empty identifier");
			return false;
		}
		
		for(BusinessColumn column : identifier.getColumns()) {
			if(table.getSimpleBusinessColumnByUniqueName(column.getUniqueName()) == null) {
				diagnosticMessages.add("Column [" + column.getName() + "] of identifier [" + identifier.getName() + "] does not belong to to table [" + table.getName() + "]");
				return false;
			}
			
			if( !(column.isIdentifier() || column.isPartOfCompositeIdentifier()) ) {
				diagnosticMessages.add("Column [" + column.getName() + "] of identifier [" + identifier.getName() + "] is not tagged as key column");
				return false;
			}
		}
		
		return true;
	}
	
	public boolean validate(BusinessTable table) {
		boolean isValid = true;
		
		if(table.getIdentifier() != null) {
			if(table.getIdentifier().getColumns() == null || table.getIdentifier().getColumns().size() < 1) {
				isValid = false;
				diagnosticMessages.add("Business table  [" + table.getName() + "] have an empty identifier");
			}
		}
		
		return isValid;
	}
}
