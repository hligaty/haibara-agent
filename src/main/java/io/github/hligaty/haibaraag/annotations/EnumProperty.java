package io.github.hligaty.haibaraag.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Add an enumeration value description for the generated description information.
 *
 * @author hligaty
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnumProperty {

    Class<? extends Enum<?>> value();

}
