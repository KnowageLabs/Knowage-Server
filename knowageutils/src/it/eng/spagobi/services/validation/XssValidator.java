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
package it.eng.spagobi.services.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class XssValidator implements ConstraintValidator<Xss, String> {

	public void initialize(Xss arg0) {
	}

	public boolean isValid(String toValidate, ConstraintValidatorContext constraintContext) {
		if (toValidate == null)
			return true;

		String upperCaseString = toValidate.toUpperCase();
		if (upperCaseString.contains("<A") || upperCaseString.contains("<LINK") || upperCaseString.contains("<IMG") || upperCaseString.contains("<SCRIPT")
				|| upperCaseString.contains("&LT;A") || upperCaseString.contains("&LT;LINK") || upperCaseString.contains("&LT;IMG")
				|| upperCaseString.contains("&LT;SCRIPT"))
			return false;

		return true;
	}

}
