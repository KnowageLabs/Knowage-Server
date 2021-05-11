package it.eng.spagobi.services.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UUIDAlphanumericNoSpacesValidator implements ConstraintValidator<UUIDAlphanumericNoSpaces, String> {

	@Override
	public void initialize(UUIDAlphanumericNoSpaces constraintAnnotation) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null || value.isEmpty())
			return true;
		return value.length() != 36 ? false : true;
	}

}
