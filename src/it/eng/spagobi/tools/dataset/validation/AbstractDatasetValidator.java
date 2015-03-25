/**

SpagoBI - The Business Intelligence Free Platform

Copyright (C) 2005-2010 Engineering Ingegneria Informatica S.p.A.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

**/
package it.eng.spagobi.tools.dataset.validation;

import java.util.Locale;
import java.util.Map;

import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public abstract class AbstractDatasetValidator implements IDatasetValidator {

    IDatasetValidator childValidator = null;
    Locale locale = null;

    public Locale getLocale() {
    	Locale locale = this.locale;
    	if(locale == null && childValidator != null) {
    		locale = childValidator.getLocale();
    	}
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public ValidationErrors validateDataset(IDataStore dataStore, Map<String, HierarchyLevel> hierarchiesColumnsToCheck) {
    	ValidationErrors errors = new ValidationErrors();
        if(childValidator != null) {
        	ValidationErrors childValidationErrors = childValidator.validateDataset( dataStore, hierarchiesColumnsToCheck );
            errors.addAll( childValidationErrors )  ;    
        }
        ValidationErrors validationErrors = doValidateDataset( dataStore, hierarchiesColumnsToCheck);
        errors.addAll(validationErrors);
        return errors;
    }

    public abstract ValidationErrors doValidateDataset(IDataStore dataStore, Map<String, HierarchyLevel> hierarchiesColumnsToCheck);

}
