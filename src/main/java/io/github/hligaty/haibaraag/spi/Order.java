package io.github.hligaty.haibaraag.spi;

/**
 * Defines the sort order for implemented component.
 * Lower values have higher priority.
 *
 * @author hligaty
 */
public interface Order {

    /**
     * highest precedence.
     */
    int HIGHEST_PRECEDENCE = Integer.MIN_VALUE;

    /**
     * lowest precedence.
     */
    int LOWEST_PRECEDENCE = Integer.MAX_VALUE;

    /**
     * @return priority level.
     */
    default int value() {
        return LOWEST_PRECEDENCE;
    }

}
