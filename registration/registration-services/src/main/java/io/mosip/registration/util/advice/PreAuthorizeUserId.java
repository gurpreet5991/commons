package io.mosip.registration.util.advice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PreAuthorizeUserId {
	/**
	 * List of the roles which needs to accessed for method
	 * 
	 * @return
	 */
	public String[] roles() default {""};
	
}
