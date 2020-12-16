package it.eng.spagobi.services.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.validator.GenericValidator;

public class EmailValidationImpl implements ConstraintValidator<EmailValidation, String> {

	@Override
	public void initialize(EmailValidation constraintAnnotation) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null || value.isEmpty())
			return true;
		return GenericValidator.isEmail(value);
	}

}
