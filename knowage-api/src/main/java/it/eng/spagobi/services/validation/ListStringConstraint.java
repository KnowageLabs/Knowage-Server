package it.eng.spagobi.services.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Constraint(validatedBy = ListStringValidator.class)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
@Retention(value = RetentionPolicy.RUNTIME)
public @interface ListStringConstraint {
	String message() default "Value not valid";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}