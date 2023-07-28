package io.github.hligaty.haibaraag.spi;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author hligaty
 */
public class DefaultEnumDescriptionFactory implements EnumDescriptionFactory {

    @Override
    public String get(Class<? extends Enum<?>> enumClass) {
        return Stream.of(enumClass.getEnumConstants())
                .map(enumConstant -> enumConstant.ordinal() + "-" + enumConstant.name())
                .collect(Collectors.joining(", ", "(", ")"));
    }

}
