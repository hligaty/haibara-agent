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
     * Convert table description to displayed entity name
     *
     * @param tableDescription table description
     * @return processed entity name
     */
    String get(String tableDescription);

}
