package io.github.hligaty.haibaraag.spi;

import java.util.Map;

/**
 * @author hligaty
 */
public class DefaultValidationAnnotationDefinitionProvider implements ValidationAnnotationDefinitionProvider {

    @Override
    public Map<String, String> get() {
        return Map.of(
                "jakarta.validation.constraints.NotBlank", "{jakarta.validation.constraints.NotBlank.message}",
                "jakarta.validation.constraints.NotNull", "{jakarta.validation.constraints.NotNull.message}",
                "jakarta.validation.constraints.Null", "{jakarta.validation.constraints.Null.message}"
        );
    }

}
