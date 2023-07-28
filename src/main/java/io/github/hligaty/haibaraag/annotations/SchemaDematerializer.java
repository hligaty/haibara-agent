package io.github.hligaty.haibaraag.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link io.swagger.v3.oas.annotations.media.Schema#description()} dematerialize.
 * When the description is empty, generate a description for it,
 * the source of the description is {@link io.swagger.v3.oas.annotations.media.Schema#description()},
 * {@link jakarta.persistence.Column#columnDefinition()} or {@link org.hibernate.annotations.Comment#value()}
 *
 * @author hligaty
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SchemaDematerializer {

    /**
     * Source of description, prioritize in descending order.
     *
     * @return describe the source class of information.
     */
    Class<?>[] value() default {};

}
