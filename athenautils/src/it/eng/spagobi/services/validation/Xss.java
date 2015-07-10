package it.eng.spagobi.services.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(value = RetentionPolicy.RUNTIME)
@Constraint(validatedBy = XssValidator.class)
@Documented
public @interface Xss {
	String message() default "could contain dangerous code";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
