package id.ac.ui.cs.advprog.authprofile.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresAuthorization {
    /**
     * The action name to check authorization for
     */
    String action();

    /**
     * The Spring Expression Language (SpEL) expression to extract the resource ID
     * Default is null, meaning no specific resource is being accessed
     */
    String resourceIdExpression() default "null";
}