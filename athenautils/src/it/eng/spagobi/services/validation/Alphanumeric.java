package it.eng.spagobi.services.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.Pattern;

@Pattern(regexp = "^([a-zA-Z0-9\\s\\-\\_])*$", message = "is not alphanumeric")
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(value = RetentionPolicy.RUNTIME)
@Constraint(validatedBy = GenericValidator.class)
@Documented
public @interface Alphanumeric {
	String message() default "";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
