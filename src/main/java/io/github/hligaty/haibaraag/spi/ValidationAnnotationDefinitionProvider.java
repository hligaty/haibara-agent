package io.github.hligaty.haibaraag.spi;

import java.util.Map;

/**
 * Validation annotation definition provider.
 * If you have defined your own validation annotation,
 * you can add your validation annotation definition here.
 *
 * @author hligaty
 * @see DefaultValidationAnnotationDefinitionProvider
 */
public interface ValidationAnnotationDefinitionProvider extends Order {

    /**
     * Key: class name, value: message suffix
     *
     * @return validation annotation definition collection
     */
    Map<String, String> get();

}
