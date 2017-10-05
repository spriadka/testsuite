package org.jboss.hal.testsuite.test.configuration.elytron.factory;

public interface IndexedAttribute<T> {
    int getIndex();
    String getKey();
    T getValue();
    void setValue(T value);
}
