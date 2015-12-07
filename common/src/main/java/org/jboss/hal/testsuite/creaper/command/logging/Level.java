package org.jboss.hal.testsuite.creaper.command.logging;

public enum Level {

    ALL("ALL"),
    FINEST("FINEST"),
    FINER("FINER"),
    TRACE("TRACE"),
    DEBUG("DEBUG"),
    FINE("FINE"),
    CONFIG("CONFIG"),
    INFO("INFO"),
    WARN("WARN"),
    WARNING("WARNING"),
    ERROR("ERROR"),
    SEVERE("SEVERE"),
    FATAL("FATAL"),
    OFF("OFF");

    private final String val;

    /**
     * @param val
     */
    private Level(final String val) {
        this.val = val;
    }

    public String value() {
        return val;
    }
};
