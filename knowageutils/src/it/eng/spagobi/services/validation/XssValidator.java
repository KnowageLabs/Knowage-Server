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
