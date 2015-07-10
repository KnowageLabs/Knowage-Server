package it.eng.spagobi.services.validation;

import java.lang.annotation.Annotation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/* Used to validate fields and methods annotated with annotation that .. TODO scrivere bene... */
public class GenericValidator implements ConstraintValidator<Annotation, String> {

	public void initialize(Annotation arg0) {
	}

	public boolean isValid(String arg0, ConstraintValidatorContext arg1) {
		return true;
	}
}
