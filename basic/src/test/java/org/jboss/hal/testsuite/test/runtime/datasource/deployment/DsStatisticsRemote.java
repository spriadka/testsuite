package org.jboss.hal.testsuite.test.runtime.datasource.deployment;

import javax.ejb.Remote;

@Remote
public interface DsStatisticsRemote {

    void commit() throws Exception;

    void rollback() throws Exception;

}
