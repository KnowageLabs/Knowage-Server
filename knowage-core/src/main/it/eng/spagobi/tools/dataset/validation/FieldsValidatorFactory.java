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
package it.eng.spagobi.tools.dataset.validation;

import it.eng.spagobi.rest.validation.IFieldsValidator;
import it.eng.spagobi.signup.validation.SignupFieldsValidator;

/**
 * @author Monica Franceschini (monica.franceschini@eng.it)
 *
 */
public class FieldsValidatorFactory {
	
	
	public static final String DATASET = "dataset";
	public static final String SIGNUP  = "signup";
	
	public IFieldsValidator getValidator(String type) {
		if ( type.equalsIgnoreCase(DATASET))
			return new DatasetFieldsValidator();
		else if ( type.equalsIgnoreCase(SIGNUP)) 
			return new SignupFieldsValidator();
		else	
			return null;
		
	}

}
