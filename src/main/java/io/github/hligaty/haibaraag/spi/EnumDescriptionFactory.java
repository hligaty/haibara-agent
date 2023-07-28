package io.github.hligaty.haibaraag.spi;

/**
 * Enumeration description factory.
 *
 * @author hligaty
 * @see DefaultEnumDescriptionFactory
 */
public interface EnumDescriptionFactory extends Order {

    /**
     * Convert enumeration to displayed data.
     *
     * @param enumClass Enum Class
     * @return enumeration description
     */
    String get(Class<? extends Enum<?>> enumClass);

}
