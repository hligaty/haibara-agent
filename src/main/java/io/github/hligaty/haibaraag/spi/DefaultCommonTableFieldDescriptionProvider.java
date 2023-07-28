package io.github.hligaty.haibaraag.spi;

import java.util.Map;

/**
 * @author hligaty
 */
public class DefaultCommonTableFieldDescriptionProvider implements CommonTableFieldDescriptionProvider {

    @Override
    public Map<CommonTableFieldDefinition, String> get() {
        return Map.of(new CommonTableFieldDefinition(Long.class.getName(), "id"), "Id");
    }

}
