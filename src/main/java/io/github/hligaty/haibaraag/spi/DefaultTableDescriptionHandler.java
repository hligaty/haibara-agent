package io.github.hligaty.haibaraag.spi;

/**
 * @author hligaty
 */
public class DefaultTableDescriptionHandler implements TableDescriptionHandler {

    @Override
    public String get(String tableName) {
        return tableName;
    }

}
