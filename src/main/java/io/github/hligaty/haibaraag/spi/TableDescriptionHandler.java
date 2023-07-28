package io.github.hligaty.haibaraag.spi;

/**
 * Table description handler.
 * TableName from {@link io.swagger.v3.oas.annotations.media.Schema#description()} or
 * {@link org.hibernate.annotations.Comment#value()}
 *
 * @author hligaty
 * @see DefaultTableDescriptionHandler
 */
public interface TableDescriptionHandler extends Order {

    /**
     * Convert table name to displayed entity name
     *
     * @param tableName tableName
     * @return processed entity name
     */
    String get(String tableName);

}
