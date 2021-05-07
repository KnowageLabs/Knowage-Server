package it.eng.spagobi.services.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UUIDValidator implements ConstraintValidator<Alphanumeric, String> {

	@Override
	public void initialize(Alphanumeric constraintAnnotation) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null || value.isEmpty())
			return false;
		return value.length() != 36 ? false : true;
	}

}
