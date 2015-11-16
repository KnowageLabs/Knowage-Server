package it.eng.spagobi.services.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.Pattern;

@Pattern(regexp = "^([\\w\\s\\-\\_\\(\\)\\[\\]\\;\\:\\!\\?\\{\\,\\}\\.\\'\\\"\\x2F\\x5F])*$", message = "contains invalid characters")
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(value = RetentionPolicy.RUNTIME)
@Constraint(validatedBy = GenericValidator.class)
@Documented
public @interface ExtendedAlphanumeric {
	String message() default "";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
