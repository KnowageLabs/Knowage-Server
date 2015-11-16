/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.services.validation;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

public class ObjectValidator {

	public static String validate(Object obj) {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		Set<ConstraintViolation<Object>> violations = validator.validate(obj);

		String error = "";
		if (!violations.isEmpty()) {

			boolean firstIteration = true;
			for (ConstraintViolation<Object> violation : violations) {
				if (firstIteration)
					firstIteration = false;
				else
					error += ", ";

				error += violation.getPropertyPath().toString() + " " + violation.getMessage();
			}

			return error;
		}

		return null;
	}
}
