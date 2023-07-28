package io.github.hligaty.haibaraag.spi;

import java.util.Map;

/**
 * Provider for generating generic table field descriptions.
 * For example, every table has a primary key,
 * which is usually a Long type field called id,
 * which should be displayed as "${tableName}${descriptionSuffix}" for documents
 *
 * @author hligaty
 * @see DefaultCommonTableFieldDescriptionProvider
 */
public interface CommonTableFieldDescriptionProvider extends Order {

    /**
     * Key: common table field definition, value: descriptionSuffix.
     *
     * @return common table field definition collection.
     */
    Map<CommonTableFieldDefinition, String> get();

}
